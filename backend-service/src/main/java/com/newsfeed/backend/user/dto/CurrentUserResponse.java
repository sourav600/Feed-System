package com.newsfeed.backend.user.dto;

import com.newsfeed.backend.user.User;

public record CurrentUserResponse(Long id, String firstName, String lastName, String email, String avatarUrl) {

    public static CurrentUserResponse from(User user) {
        return new CurrentUserResponse(
                user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getAvatarUrl());
    }
}
