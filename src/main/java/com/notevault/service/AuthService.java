package com.notevault.service;

import com.notevault.dto.AuthResponse;
import com.notevault.dto.LoginRequest;
import com.notevault.dto.RegisterRequest;
import com.notevault.exception.UserAlreadyExistsException;
import com.notevault.model.User;
import com.notevault.repository.UserRepository;
import com.notevault.security.CustomUserDetails;
import com.notevault.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user registration and authentication workflows.
 * Demonstrates:
 * - Spring Boot @Service component and Dependency Injection.
 * - Password hashing with BCrypt.
 * - JWT issuance.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Registers a new user account with hashed password and generates an initial JWT token.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("An account with email '" + normalizedEmail + "' already exists");
        }

        User user = new User(
                request.getName().trim(),
                normalizedEmail,
                passwordEncoder.encode(request.getPassword())
        );

        User savedUser = userRepository.save(user);

        String token = jwtUtils.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getName());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    /**
     * Authenticates existing user credentials and returns a valid JWT token.
     */
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String token = jwtUtils.generateToken(
                userDetails.getId(),
                userDetails.getEmail(),
                userDetails.getName()
        );

        return new AuthResponse(
                token,
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getEmail()
        );
    }
}
