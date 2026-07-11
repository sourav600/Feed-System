package com.newsfeed.backend.comment.dto;

import com.newsfeed.backend.comment.Comment;
import com.newsfeed.backend.user.dto.UserSummaryResponse;
import java.time.Instant;

public record CommentResponse(
        Long id,
        Long postId,
        Long parentCommentId,
        UserSummaryResponse author,
        String content,
        int likeCount,
        int replyCount,
        boolean likedByCurrentUser,
        Instant createdAt) {

    public static CommentResponse from(Comment comment, UserSummaryResponse author, boolean likedByCurrentUser) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                author,
                comment.getContent(),
                comment.getLikeCount(),
                comment.getReplyCount(),
                likedByCurrentUser,
                comment.getCreatedAt());
    }
}
