package com.example.model;

import java.util.Optional;

/** Data access contract for accounts. */
public interface UserRepository {

    Optional<User> findByEmail(String email);

    /** Inserts a new user and returns it with its generated userId. */
    User save(User user);

    /** Replaces the stored password hash of the account with this email. */
    void updatePasswordHash(String email, String passwordHash);
}