package com.example.controller;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.model.UserRepository;

/** Forgot-password logic: issue a code, check it, then set the new password. */
@Service
public class PasswordResetController {

    public static final int CODE_LENGTH = 6;
    public static final int MIN_PASSWORD_LENGTH = 8;
    private static final Duration CODE_LIFETIME = Duration.ofMinutes(10);
    private static final int MAX_ATTEMPTS = 5;

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);

    private static final class PendingReset {
        final String codeHash;
        final Instant expiresAt;
        int attempts;

        PendingReset(String codeHash, Instant expiresAt) {
            this.codeHash = codeHash;
            this.expiresAt = expiresAt;
        }
    }

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final boolean showCodes;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, PendingReset> pending = new ConcurrentHashMap<>();

    public PasswordResetController(UserRepository users, PasswordEncoder encoder,
                                   @Value("${ayos.dev.show-reset-codes:false}") boolean showCodes) {
        this.users = users;
        this.encoder = encoder;
        this.showCodes = showCodes;
    }

    /** Returns the code only when dev display is on, so the screen can show it as a notification. */
    public Optional<String> sendResetCode(String email) {
        String key = normalize(email);
        String code = String.format("%0" + CODE_LENGTH + "d", random.nextInt((int) Math.pow(10, CODE_LENGTH)));
        String hash = encoder.encode(code);
        if (key.isEmpty() || users.findByEmail(key).isEmpty()) {
            return Optional.empty();
        }
        pending.put(key, new PendingReset(hash, Instant.now().plus(CODE_LIFETIME)));
        // TODO: send by email here when that's added; then turn show-reset-codes off.
        return showCodes ? Optional.of(code) : Optional.empty();
    }

    /** Step 2: true if the code is right and not expired. Wrong guesses are limited. */
    public boolean verifyResetCode(String email, String code) {
        return checkCode(email, code);
    }

    /** Step 3: re-checks the code itself, then replaces the password hash. */
    public synchronized void resetPassword(String email, String code, String newPassword) {
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        if (!checkCode(email, code)) {
            throw new IllegalArgumentException("That code is wrong or has expired. Please request a new one.");
        }
        String key = normalize(email);
        users.updatePasswordHash(key, encoder.encode(newPassword));
        pending.remove(key);
    }

    private synchronized boolean checkCode(String email, String code) {
        String key = normalize(email);
        PendingReset p = pending.get(key);
        if (p == null || code == null) {
            return false;
        }
        if (Instant.now().isAfter(p.expiresAt)) {
            pending.remove(key);
            return false;
        }
        if (encoder.matches(code, p.codeHash)) {
            return true;
        }
        if (++p.attempts >= MAX_ATTEMPTS) {
            pending.remove(key);
        }
        return false;
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}