package com.newsfeed.backend.comment;

import com.newsfeed.backend.comment.dto.CommentResponse;
import com.newsfeed.backend.comment.dto.CreateCommentRequest;
import com.newsfeed.backend.common.pagination.CursorPageResponse;
import com.newsfeed.backend.like.LikeService;
import com.newsfeed.backend.like.dto.LikeActionResponse;
import com.newsfeed.backend.like.dto.LikerSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves both {@code /api/posts/{postId}/comments} (top-level comments) and {@code
 * /api/comments/{commentId}/...} (replies + likes, on either a top-level comment or a reply -
 * both are just rows in the same table, see {@link Comment}).
 */
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final LikeService likeService;

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal Long userId, @PathVariable Long postId, @Valid @RequestBody CreateCommentRequest body) {
        CommentResponse comment = commentService.createTopLevelComment(postId, userId, body.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/api/posts/{postId}/comments")
    public CursorPageResponse<CommentResponse> getComments(
            @AuthenticationPrincipal Long userId, @PathVariable Long postId,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) Integer limit) {
        return commentService.getTopLevelComments(postId, userId, cursor, limit);
    }

    @PostMapping("/api/comments/{commentId}/replies")
    public ResponseEntity<CommentResponse> createReply(
            @AuthenticationPrincipal Long userId, @PathVariable Long commentId, @Valid @RequestBody CreateCommentRequest body) {
        CommentResponse reply = commentService.createReply(commentId, userId, body.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }

    @GetMapping("/api/comments/{commentId}/replies")
    public CursorPageResponse<CommentResponse> getReplies(
            @AuthenticationPrincipal Long userId, @PathVariable Long commentId,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) Integer limit) {
        return commentService.getReplies(commentId, userId, cursor, limit);
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal Long userId, @PathVariable Long commentId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/comments/{commentId}/likes")
    public LikeActionResponse likeComment(@AuthenticationPrincipal Long userId, @PathVariable Long commentId) {
        return commentService.likeComment(commentId, userId);
    }

    @DeleteMapping("/api/comments/{commentId}/likes")
    public LikeActionResponse unlikeComment(@AuthenticationPrincipal Long userId, @PathVariable Long commentId) {
        return commentService.unlikeComment(commentId, userId);
    }

    @GetMapping("/api/comments/{commentId}/likes")
    public CursorPageResponse<LikerSummaryResponse> getCommentLikers(
            @AuthenticationPrincipal Long userId, @PathVariable Long commentId,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) Integer limit) {
        commentService.getVisibleCommentOrThrow(commentId, userId);
        return likeService.getCommentLikers(commentId, cursor, limit);
    }
}
