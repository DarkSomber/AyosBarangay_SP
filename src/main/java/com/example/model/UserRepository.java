package com.example.model;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Data access contract for accounts. */
public interface UserRepository {

    Optional<User> findByEmail(String email);

    /** The accounts with these ids (ids that don't exist are skipped). */
    List<User> findAllByIds(Collection<Integer> ids);

    /** Inserts a new user and returns it with its generated userId. */
    User save(User user);

    /** Replaces the stored password hash of the account with this email. */
    void updatePasswordHash(String email, String passwordHash);

    void updateDetails(int userId, String fullName, String email);
}