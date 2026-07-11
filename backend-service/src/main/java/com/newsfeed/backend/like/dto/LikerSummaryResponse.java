package com.newsfeed.backend.like.dto;

import com.newsfeed.backend.like.Like;
import java.time.Instant;

public record LikerSummaryResponse(
        Long userId, String firstName, String lastName, String avatarUrl, Instant likedAt) {

    public static LikerSummaryResponse from(Like like) {
        return new LikerSummaryResponse(
                like.getUser().getId(),
                like.getUser().getFirstName(),
                like.getUser().getLastName(),
                like.getUser().getAvatarUrl(),
                like.getCreatedAt());
    }
}
