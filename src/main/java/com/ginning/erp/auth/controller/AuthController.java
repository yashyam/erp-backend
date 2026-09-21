package com.ginning.erp.auth.controller;

import com.ginning.erp.auth.dto.JwtResponse;
import com.ginning.erp.auth.dto.LoginRequest;
import com.ginning.erp.auth.dto.RegisterRequest;
import com.ginning.erp.auth.dto.UserDto;
import com.ginning.erp.auth.dto.GoogleLoginRequest;
import com.ginning.erp.auth.service.UserService;
import com.ginning.erp.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        UserDto user = userService.register(request, servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        JwtResponse tokens = userService.authenticate(request, servletRequest);
        return ResponseEntity.ok(ApiResponse.success(tokens, "Login successful"));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<JwtResponse>> google(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.authenticateGoogle(request.getIdToken(), servletRequest),
                "Google login successful"
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest servletRequest
    ) {
        JwtResponse tokens = userService.refreshToken(request.getRefreshToken(), servletRequest);
        return ResponseEntity.ok(ApiResponse.success(tokens, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout(
            Principal principal,
            HttpServletRequest servletRequest
    ) {
        String username = principal != null ? principal.getName() : null;
        userService.logout(username, servletRequest);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("status", "logged_out"),
                "Logout successful"
        ));
    }

    public static class RefreshTokenRequest {

        @NotBlank(message = "Refresh token is required")
        private String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

}
