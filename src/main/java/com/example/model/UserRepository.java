package com.example.model;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    List<User> findAllByIds(Collection<Integer> ids);

    User save(User user);

    void updatePasswordHash(String email, String passwordHash);

    void updateDetails(int userId, String fullName, String email);
}