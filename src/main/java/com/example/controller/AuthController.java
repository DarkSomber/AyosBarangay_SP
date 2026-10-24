package com.example.controller;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.model.User;
import com.example.model.UserRepository;

@Service
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    // Compared when the email is unknown, so response time doesn't reveal which emails exist.
    private final String dummyHash;

    public AuthController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
        this.dummyHash = encoder.encode("not-a-real-password");
    }

    public Optional<User> authenticate(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isEmpty()) {
            return Optional.empty();
        }
        Optional<User> found = users.findByEmail(email);
        String hash = found.map(User::passwordHash).orElse(dummyHash);
        boolean ok = encoder.matches(rawPassword, hash);
        return ok ? found : Optional.empty();
    }

    /**
     * Checks the current password, saves the new one, and returns the refreshed user.
     */
    public User changePassword(User current, String currentPassword, String newPassword) {
        User stored = users.findByEmail(current.email())
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        if (currentPassword == null || !encoder.matches(currentPassword, stored.passwordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (newPassword == null || newPassword.length() < PasswordResetController.MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least "
                    + PasswordResetController.MIN_PASSWORD_LENGTH + " characters.");
        }
        if (encoder.matches(newPassword, stored.passwordHash())) {
            throw new IllegalArgumentException("New password must be different from the current one.");
        }
        users.updatePasswordHash(stored.email(), encoder.encode(newPassword));
        return users.findByEmail(stored.email()).orElseThrow();
    }
}