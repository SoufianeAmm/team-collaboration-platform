package com.teamcollab.backend.service;

import com.teamcollab.backend.dto.AuthResponse;
import com.teamcollab.backend.dto.LoginRequest;
import com.teamcollab.backend.dto.RegisterRequest;
import com.teamcollab.backend.dto.UserResponse;
import com.teamcollab.backend.entity.User;
import com.teamcollab.backend.exception.EmailAlreadyExistsException;
import com.teamcollab.backend.exception.UsernameAlreadyExistsException;
import com.teamcollab.backend.repository.UserRepository;
import com.teamcollab.backend.security.JwtService;
import com.teamcollab.backend.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(request.getUsername());
        }

        User user = new User(
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()) // never store the raw password
        );
        userRepository.save(user);

        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, UserResponse.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        // Delegates to DaoAuthenticationProvider, which loads the user via CustomUserDetailsService
        // and checks the password with PasswordEncoder. Throws BadCredentialsException on mismatch.
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);
        return new AuthResponse(token, UserResponse.from(principal.getUser()));
    }
}
