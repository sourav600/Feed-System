package com.newsfeed.backend.user;

import com.newsfeed.backend.user.dto.CurrentUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal Long userId) {
        return CurrentUserResponse.from(userService.getById(userId));
    }
}
