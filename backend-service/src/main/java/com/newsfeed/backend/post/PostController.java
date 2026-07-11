package com.newsfeed.backend.post;

import com.newsfeed.backend.common.pagination.CursorPageResponse;
import com.newsfeed.backend.like.LikeService;
import com.newsfeed.backend.like.dto.LikeActionResponse;
import com.newsfeed.backend.like.dto.LikerSummaryResponse;
import com.newsfeed.backend.post.dto.PostResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Validated
public class PostController {

    private final PostService postService;
    private final LikeService likeService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal Long userId,
            @RequestParam @NotBlank @Size(max = 5000) String content,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(defaultValue = "PUBLIC") Visibility visibility) {
        PostResponse post = postService.createPost(userId, content, visibility, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @GetMapping("/feed")
    public CursorPageResponse<PostResponse> feed(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit) {
        return postService.getFeed(userId, cursor, limit);
    }

    @GetMapping("/{postId}")
    public PostResponse getPost(@AuthenticationPrincipal Long userId, @PathVariable Long postId) {
        return postService.getPost(postId, userId);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@AuthenticationPrincipal Long userId, @PathVariable Long postId) {
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/likes")
    public LikeActionResponse likePost(@AuthenticationPrincipal Long userId, @PathVariable Long postId) {
        return postService.likePost(postId, userId);
    }

    @DeleteMapping("/{postId}/likes")
    public LikeActionResponse unlikePost(@AuthenticationPrincipal Long userId, @PathVariable Long postId) {
        return postService.unlikePost(postId, userId);
    }

    @GetMapping("/{postId}/likes")
    public CursorPageResponse<LikerSummaryResponse> getPostLikers(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit) {
        // Re-check visibility before listing likers - a private post's likers must be exactly as
        // hidden as the post itself (see PostService.getVisiblePostOrThrow doc).
        postService.getVisiblePostOrThrow(postId, userId);
        return likeService.getPostLikers(postId, cursor, limit);
    }
}
