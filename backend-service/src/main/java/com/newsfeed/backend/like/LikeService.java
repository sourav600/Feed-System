package com.newsfeed.backend.like;

import com.newsfeed.backend.comment.Comment;
import com.newsfeed.backend.comment.CommentRepository;
import com.newsfeed.backend.common.pagination.Cursor;
import com.newsfeed.backend.common.pagination.CursorPageResponse;
import com.newsfeed.backend.like.dto.LikerSummaryResponse;
import com.newsfeed.backend.post.PostRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Shared like/unlike + likers-listing logic for both posts and comments (a reply is just a
 * {@link Comment} with a parent - see {@link Comment}). Exposed to clients only as sub-resources
 * of post/comment controllers ({@code /api/posts/{id}/likes}, {@code /api/comments/{id}/likes}),
 * never as a standalone {@code /api/likes} resource - a like is never meaningfully addressed
 * independent of its target.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /** Idempotent: liking an already-liked post is a no-op, not an error - concurrent double-clicks included. */
    public int likePost(Long userId, Long postId) {
        if (likeRepository.insertPostLikeIfAbsent(userId, postId) > 0) {
            postRepository.incrementLikeCount(postId);
        }
        return (int) likeRepository.countByPost_Id(postId);
    }

    public int unlikePost(Long userId, Long postId) {
        long deleted = likeRepository.deleteByUserIdAndPostId(userId, postId);
        if (deleted > 0) {
            postRepository.decrementLikeCount(postId);
        }
        return (int) likeRepository.countByPost_Id(postId);
    }

    public int likeComment(Long userId, Long commentId) {
        if (likeRepository.insertCommentLikeIfAbsent(userId, commentId) > 0) {
            commentRepository.incrementLikeCount(commentId);
        }
        return (int) likeRepository.countByComment_Id(commentId);
    }

    public int unlikeComment(Long userId, Long commentId) {
        long deleted = likeRepository.deleteByUserIdAndCommentId(userId, commentId);
        if (deleted > 0) {
            commentRepository.decrementLikeCount(commentId);
        }
        return (int) likeRepository.countByComment_Id(commentId);
    }

    @Transactional(readOnly = true)
    public boolean isPostLikedByUser(Long userId, Long postId) {
        return likeRepository.findByUser_IdAndPost_Id(userId, postId).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean isCommentLikedByUser(Long userId, Long commentId) {
        return likeRepository.findByUser_IdAndComment_Id(userId, commentId).isPresent();
    }

    @Transactional(readOnly = true)
    public Set<Long> likedPostIds(Long userId, List<Long> postIds) {
        return postIds.isEmpty() ? Set.of() : likeRepository.findLikedPostIds(userId, postIds);
    }

    @Transactional(readOnly = true)
    public Set<Long> likedCommentIds(Long userId, List<Long> commentIds) {
        return commentIds.isEmpty() ? Set.of() : likeRepository.findLikedCommentIds(userId, commentIds);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<LikerSummaryResponse> getPostLikers(Long postId, String cursorToken, Integer limit) {
        int pageLimit = CursorPageResponse.clampLimit(limit);
        Cursor cursor = Cursor.decode(cursorToken);
        List<Like> rows = likeRepository.findPostLikersPage(
                postId,
                cursor != null ? cursor.createdAt() : null,
                cursor != null ? cursor.id() : null,
                PageRequest.of(0, pageLimit + 1));
        return CursorPageResponse.fromOverFetch(
                rows, pageLimit, LikerSummaryResponse::from, like -> new Cursor(like.getCreatedAt(), like.getId()));
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<LikerSummaryResponse> getCommentLikers(Long commentId, String cursorToken, Integer limit) {
        int pageLimit = CursorPageResponse.clampLimit(limit);
        Cursor cursor = Cursor.decode(cursorToken);
        List<Like> rows = likeRepository.findCommentLikersPage(
                commentId,
                cursor != null ? cursor.createdAt() : null,
                cursor != null ? cursor.id() : null,
                PageRequest.of(0, pageLimit + 1));
        return CursorPageResponse.fromOverFetch(
                rows, pageLimit, LikerSummaryResponse::from, like -> new Cursor(like.getCreatedAt(), like.getId()));
    }
}
