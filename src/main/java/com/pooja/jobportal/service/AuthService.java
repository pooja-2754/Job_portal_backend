package com.pooja.jobportal.service;

import com.pooja.jobportal.dto.LoginRequest;
import com.pooja.jobportal.dto.SignupRequest;
import com.pooja.jobportal.dto.AuthResponse;
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
            return new AuthResponse(null, "Email already exists");
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .skills(req.getSkills())
                .build();

        userRepository.save(user);

        return new AuthResponse(null, "User registered successfully");
    }

    public AuthResponse login(LoginRequest req) {

        User user = userRepository.findByEmail(req.getEmail())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return new AuthResponse(null, "Invalid Email or Password");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        return new AuthResponse(token, "Login successful");
    }
}
