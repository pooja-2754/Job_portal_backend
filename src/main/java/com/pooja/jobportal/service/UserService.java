package com.pooja.jobportal.service;

import com.pooja.jobportal.model.User;
import com.pooja.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Create new user (signup)
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // Get user by email (login)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Check if user already exists by email
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Update user profile
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // Delete user
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
