package com.newsfeed.backend.user.dto;

import com.newsfeed.backend.user.User;

public record UserSummaryResponse(Long id, String firstName, String lastName, String avatarUrl) {

    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getAvatarUrl());
    }
}
