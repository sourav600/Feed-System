package com.newsfeed.backend.post.dto;

import com.newsfeed.backend.post.Post;
import com.newsfeed.backend.post.Visibility;
import com.newsfeed.backend.user.dto.UserSummaryResponse;
import java.time.Instant;

public record PostResponse(
        Long id,
        UserSummaryResponse author,
        String content,
        String imageUrl,
        Visibility visibility,
        int likeCount,
        int commentCount,
        boolean likedByCurrentUser,
        Instant createdAt) {

    public static PostResponse from(Post post, UserSummaryResponse author, boolean likedByCurrentUser) {
        return new PostResponse(
                post.getId(),
                author,
                post.getContent(),
                post.getImageUrl(),
                post.getVisibility(),
                post.getLikeCount(),
                post.getCommentCount(),
                likedByCurrentUser,
                post.getCreatedAt());
    }
}
