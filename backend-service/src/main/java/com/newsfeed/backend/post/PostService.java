package com.newsfeed.backend.post;

import com.newsfeed.backend.common.exception.ForbiddenException;
import com.newsfeed.backend.common.exception.ResourceNotFoundException;
import com.newsfeed.backend.common.pagination.Cursor;
import com.newsfeed.backend.common.pagination.CursorPageResponse;
import com.newsfeed.backend.like.LikeService;
import com.newsfeed.backend.like.dto.LikeActionResponse;
import com.newsfeed.backend.media.FileStorageService;
import com.newsfeed.backend.post.dto.PostResponse;
import com.newsfeed.backend.user.User;
import com.newsfeed.backend.user.UserRepository;
import com.newsfeed.backend.user.dto.UserSummaryResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeService likeService;
    private final FileStorageService fileStorageService;

    public PostResponse createPost(Long authorId, String content, Visibility visibility, MultipartFile image) {
        String imageUrl = (image != null && !image.isEmpty()) ? fileStorageService.store(image) : null;

        Post post = Post.builder()
                .author(userRepository.getReferenceById(authorId))
                .content(content)
                .imageUrl(imageUrl)
                .visibility(visibility)
                .build();
        postRepository.save(post);

        return toResponse(post, authorId);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<PostResponse> getFeed(Long viewerId, String cursorToken, Integer limit) {
        int pageLimit = CursorPageResponse.clampLimit(limit);
        Cursor cursor = Cursor.decode(cursorToken);

        List<Post> rows = postRepository.findFeedPage(
                viewerId,
                cursor != null ? cursor.createdAt() : null,
                cursor != null ? cursor.id() : null,
                pageLimit + 1);

        return CursorPageResponse.fromOverFetch(
                rows, pageLimit, toResponseMapper(rows, viewerId), post -> new Cursor(post.getCreatedAt(), post.getId()));
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId, Long viewerId) {
        return toResponse(getVisiblePostOrThrow(postId, viewerId), viewerId);
    }

    /** Zero rows = 404, never 403 - a 403 would itself confirm a private post exists at that id. */
    @Transactional(readOnly = true)
    public Post getVisiblePostOrThrow(Long postId, Long viewerId) {
        return postRepository.findVisibleForViewer(postId, viewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found."));
    }

    public void deletePost(Long postId, Long userId) {
        Post post = getVisiblePostOrThrow(postId, userId);
        if (!post.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own posts.");
        }
        post.setDeletedAt(Instant.now());
    }

    public LikeActionResponse likePost(Long postId, Long userId) {
        getVisiblePostOrThrow(postId, userId);
        int count = likeService.likePost(userId, postId);
        return new LikeActionResponse(true, count);
    }

    public LikeActionResponse unlikePost(Long postId, Long userId) {
        getVisiblePostOrThrow(postId, userId);
        int count = likeService.unlikePost(userId, postId);
        return new LikeActionResponse(false, count);
    }

    private PostResponse toResponse(Post post, Long viewerId) {
        // post.getAuthor() is already the association Hibernate needs to load for this one post -
        // no reason to issue a second lookup through UserRepository (that's only worthwhile as a
        // batch, in toResponseMapper below, to avoid N+1 across a whole feed page).
        UserSummaryResponse author = UserSummaryResponse.from(post.getAuthor());
        boolean liked = likeService.isPostLikedByUser(viewerId, post.getId());
        return PostResponse.from(post, author, liked);
    }

    private Function<Post, PostResponse> toResponseMapper(List<Post> posts, Long viewerId) {
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> authorIds = posts.stream().map(p -> p.getAuthor().getId()).distinct().toList();

        Map<Long, UserSummaryResponse> authorsById = userRepository.findAllById(authorIds).stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, UserSummaryResponse::from));
        Set<Long> likedPostIds = likeService.likedPostIds(viewerId, postIds);

        return post -> PostResponse.from(
                post, authorsById.get(post.getAuthor().getId()), likedPostIds.contains(post.getId()));
    }
}
