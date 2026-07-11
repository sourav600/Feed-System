package com.newsfeed.backend.auth;

import com.newsfeed.backend.auth.dto.LoginRequest;
import com.newsfeed.backend.auth.dto.RegisterRequest;
import com.newsfeed.backend.security.CookieService;
import com.newsfeed.backend.user.dto.CurrentUserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<CurrentUserResponse> register(
            @Valid @RequestBody RegisterRequest body, HttpServletRequest request, HttpServletResponse response) {
        CurrentUserResponse user = authService.register(
                body.firstName(), body.lastName(), body.email(), body.password(), request, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public CurrentUserResponse login(
            @Valid @RequestBody LoginRequest body, HttpServletRequest request, HttpServletResponse response) {
        return authService.login(body.email(), body.password(), request, response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = CookieService.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletRequest request, HttpServletResponse response) {
        authService.refresh(refreshToken, request, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = CookieService.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken, response);
        return ResponseEntity.noContent().build();
    }
}
