package com.newsfeed.backend.comment;

import com.newsfeed.backend.comment.dto.CommentResponse;
import com.newsfeed.backend.common.exception.ForbiddenException;
import com.newsfeed.backend.common.exception.ResourceNotFoundException;
import com.newsfeed.backend.common.pagination.Cursor;
import com.newsfeed.backend.common.pagination.CursorPageResponse;
import com.newsfeed.backend.like.LikeService;
import com.newsfeed.backend.like.dto.LikeActionResponse;
import com.newsfeed.backend.post.Post;
import com.newsfeed.backend.post.PostRepository;
import com.newsfeed.backend.post.PostService;
import com.newsfeed.backend.user.UserRepository;
import com.newsfeed.backend.user.dto.UserSummaryResponse;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PostService postService;
    private final UserRepository userRepository;
    private final LikeService likeService;

    public CommentResponse createTopLevelComment(Long postId, Long authorId, String content) {
        Post post = postService.getVisiblePostOrThrow(postId, authorId);

        Comment comment = Comment.builder()
                .post(post)
                .author(userRepository.getReferenceById(authorId))
                .content(content)
                .build();
        commentRepository.save(comment);
        postRepository.incrementCommentCount(postId);

        return toResponse(comment, authorId);
    }

    public CommentResponse createReply(Long parentCommentId, Long authorId, String content) {
        Comment parent = getVisibleCommentOrThrow(parentCommentId, authorId);
        if (!parent.isTopLevel()) {
            throw new IllegalArgumentException("Cannot reply to a reply - only one level of nesting is supported.");
        }

        Comment reply = Comment.builder()
                .post(parent.getPost())
                .parentComment(parent)
                .author(userRepository.getReferenceById(authorId))
                .content(content)
                .build();
        commentRepository.save(reply);
        commentRepository.incrementReplyCount(parent.getId());
        postRepository.incrementCommentCount(parent.getPost().getId());

        return toResponse(reply, authorId);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<CommentResponse> getTopLevelComments(
            Long postId, Long viewerId, String cursorToken, Integer limit) {
        int pageLimit = CursorPageResponse.clampLimit(limit);
        Cursor cursor = Cursor.decode(cursorToken);

        List<Comment> rows = commentRepository.findTopLevelPage(
                postId, viewerId,
                cursor != null ? cursor.createdAt() : null,
                cursor != null ? cursor.id() : null,
                PageRequest.of(0, pageLimit + 1));

        return CursorPageResponse.fromOverFetch(
                rows, pageLimit, toResponseMapper(rows, viewerId), c -> new Cursor(c.getCreatedAt(), c.getId()));
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<CommentResponse> getReplies(
            Long parentCommentId, Long viewerId, String cursorToken, Integer limit) {
        // Re-check visibility on the parent before listing its replies (same rule as everywhere
        // else keyed by a comment/post id - see CommentRepository.findVisibleForViewer doc).
        getVisibleCommentOrThrow(parentCommentId, viewerId);

        int pageLimit = CursorPageResponse.clampLimit(limit);
        Cursor cursor = Cursor.decode(cursorToken);

        List<Comment> rows = commentRepository.findRepliesPage(
                parentCommentId, viewerId,
                cursor != null ? cursor.createdAt() : null,
                cursor != null ? cursor.id() : null,
                PageRequest.of(0, pageLimit + 1));

        return CursorPageResponse.fromOverFetch(
                rows, pageLimit, toResponseMapper(rows, viewerId), c -> new Cursor(c.getCreatedAt(), c.getId()));
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getVisibleCommentOrThrow(commentId, userId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own comments.");
        }
        comment.setDeletedAt(Instant.now());
    }

    public LikeActionResponse likeComment(Long commentId, Long userId) {
        getVisibleCommentOrThrow(commentId, userId);
        int count = likeService.likeComment(userId, commentId);
        return new LikeActionResponse(true, count);
    }

    public LikeActionResponse unlikeComment(Long commentId, Long userId) {
        getVisibleCommentOrThrow(commentId, userId);
        int count = likeService.unlikeComment(userId, commentId);
        return new LikeActionResponse(false, count);
    }

    @Transactional(readOnly = true)
    public Comment getVisibleCommentOrThrow(Long commentId, Long viewerId) {
        return commentRepository.findVisibleForViewer(commentId, viewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found."));
    }

    private CommentResponse toResponse(Comment comment, Long viewerId) {
        UserSummaryResponse author = UserSummaryResponse.from(comment.getAuthor());
        boolean liked = likeService.isCommentLikedByUser(viewerId, comment.getId());
        return CommentResponse.from(comment, author, liked);
    }

    private Function<Comment, CommentResponse> toResponseMapper(List<Comment> comments, Long viewerId) {
        // Unlike PostService's feed mapper, no batched author lookup is needed here: the
        // repository queries already `JOIN FETCH c.author`, so comment.getAuthor() is already
        // initialized (a native-SQL query, like the posts feed, can't do that - JPQL can).
        List<Long> commentIds = comments.stream().map(Comment::getId).toList();
        Set<Long> likedCommentIds = likeService.likedCommentIds(viewerId, commentIds);

        return comment -> CommentResponse.from(
                comment, UserSummaryResponse.from(comment.getAuthor()), likedCommentIds.contains(comment.getId()));
    }
}
