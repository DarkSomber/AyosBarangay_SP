package com.example.model;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public record User(Integer userId, String fullName, String email, String passwordHash,
                   Role role, LocalDateTime createdAt, LocalDateTime updatedAt) {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public User {
        fullName = Validation.requireText(fullName, "Full name", 100);
        email = Validation.requireText(email, "Email", 254).toLowerCase();
        if (!EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Email is not a valid address.");
        }
        passwordHash = Validation.requireText(passwordHash, "Password hash", 255);
        Validation.requireNonNull(role, "Role");
        Validation.requireNonNull(createdAt, "Created at");
        Validation.requireNonNull(updatedAt, "Updated at");
    }

    public static User newResident(String fullName, String email, String passwordHash) {
        LocalDateTime now = LocalDateTime.now();
        return new User(null, fullName, email, passwordHash, Role.USER, now, now);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    @Override
    public String toString() {
        return "User[userId=" + userId + ", fullName=" + fullName + ", email=" + email + ", role=" + role + "]";
    }
}
