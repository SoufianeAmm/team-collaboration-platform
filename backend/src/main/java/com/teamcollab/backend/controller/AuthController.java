package com.teamcollab.backend.controller;

import com.teamcollab.backend.dto.AuthResponse;
import com.teamcollab.backend.dto.LoginRequest;
import com.teamcollab.backend.dto.RegisterRequest;
import com.teamcollab.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // Stateless JWT: the server holds no session to destroy. "Logging out" is really just the
    // frontend deleting its stored token. This endpoint exists for API completeness / symmetry.
    @PostMapping("/logout")
    public void logout() {
    }
}
