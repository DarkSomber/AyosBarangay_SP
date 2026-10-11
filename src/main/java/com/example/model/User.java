package com.example.model;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * A resident or an administrator.
 * Table: users
 *
 * Security notes:
 *  - Only the HASH of the password is stored (use BCrypt in the controller/service layer).
 *  - Ordinary registration must go through newResident(), which always gives the USER role.
 *    Admin accounts are created separately (for example by a database seed script).
 *  - toString() leaves out the password hash so it can't leak into logs.
 *
 * @param userId       primary key, null until saved
 * @param fullName     the person's name
 * @param email        unique, used to log in
 * @param passwordHash hashed password, never plain text
 * @param role         USER or ADMIN
 * @param createdAt    when the account was made
 * @param updatedAt    last change to the account
 */
public record User(Integer userId, String fullName, String email, String passwordHash,
                   Role role, LocalDateTime createdAt, LocalDateTime updatedAt) {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public User {
        fullName = Validation.requireText(fullName, "Full name", 100);
        email = Validation.requireText(email, "Email", 254).toLowerCase();
        if (!EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Email is not a valid address.");
        }
        // Length check only. The hash itself is produced and verified elsewhere.
        passwordHash = Validation.requireText(passwordHash, "Password hash", 255);
        Validation.requireNonNull(role, "Role");
        Validation.requireNonNull(createdAt, "Created at");
        Validation.requireNonNull(updatedAt, "Updated at");
    }

    /** Use this for sign-up. There is no role parameter on purpose, so nobody can pick ADMIN. */
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
