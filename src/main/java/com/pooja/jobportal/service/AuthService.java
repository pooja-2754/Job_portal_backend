package com.pooja.jobportal.service;

import com.pooja.jobportal.dto.LoginRequest;
import com.pooja.jobportal.dto.SignupRequest;
import com.pooja.jobportal.dto.AuthResponse;
import com.pooja.jobportal.dto.TokenValidationResponse;
import com.pooja.jobportal.dto.RefreshTokenRequest;
import com.pooja.jobportal.dto.UserResponse;
import com.pooja.jobportal.model.User;
import com.pooja.jobportal.repository.UserRepository;
import com.pooja.jobportal.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse signup(SignupRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            return new AuthResponse(null, null, "Email already exists");
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole() != null ? req.getRole() : com.pooja.jobportal.model.Role.JOB_SEEKER)
                .skills(req.getSkills())
                .build();

        userRepository.save(user);

        return new AuthResponse(null, null, "User registered successfully");
    }

    public AuthResponse login(LoginRequest req) {

        User user = userRepository.findByEmail(req.getEmail())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return new AuthResponse(null, null, "Invalid Email or Password");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());
        
        UserResponse userResponse = new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRole().toString(),
            user.getSkills()
        );

        return new AuthResponse(token, userResponse, "Login successful");
    }

    public TokenValidationResponse validateToken(String token) {
        // Remove "Bearer " prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        JwtTokenProvider.TokenValidationResult result = jwtTokenProvider.validateTokenDetailed(token);
        
        if (!result.isValid()) {
            return new TokenValidationResponse(false, null, 0, result.getMessage(), null);
        }

        // Get user details
        String email = result.getEmail();
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            return new TokenValidationResponse(false, null, 0, "User not found", null);
        }

        return new TokenValidationResponse(
            true,
            email,
            result.getExpiration().getTime(),
            result.getMessage(),
            user.getRole().toString()
        );
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getToken();
        
        // Remove "Bearer " prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate the current token with detailed validation
        JwtTokenProvider.TokenValidationResult validationResult = jwtTokenProvider.validateTokenDetailed(token);
        
        if (!validationResult.isValid()) {
            return new AuthResponse(null, null, validationResult.getMessage());
        }

        // Extract email from validation result
        String email = validationResult.getEmail();
        
        // Check if user still exists
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return new AuthResponse(null, null, "User not found");
        }

        // Generate new token
        String newToken = jwtTokenProvider.generateToken(email);
        
        return new AuthResponse(newToken, null, "Token refreshed successfully");
    }
}
