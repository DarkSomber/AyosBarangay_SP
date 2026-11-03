package com.example.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Repository;

/**
 * Accounts kept in memory (lost on restart). DevDataSeeder re-creates the demo
 * accounts on every start, so the demo login buttons still work.
 * TEMP-DEV: active while the database is off. To go back to MySQL, remove @Repository
 * from this class and restore @Repository on JdbcUserRepository.
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> byEmail = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(byEmail.get(email.trim().toLowerCase()));
    }

    @Override
    public List<User> findAllByIds(Collection<Integer> ids) {
        return byEmail.values().stream().filter(u -> ids.contains(u.userId())).toList();
    }

    @Override
    public synchronized User save(User u) {
        if (byEmail.containsKey(u.email())) {
            throw new IllegalArgumentException("That email is already registered.");
        }
        User stored = new User(nextId.getAndIncrement(), u.fullName(), u.email(), u.passwordHash(),
                u.role(), u.createdAt(), u.updatedAt());
        byEmail.put(stored.email(), stored);
        return stored;
    }

    @Override
    public synchronized void updateDetails(int userId, String fullName, String email) {
        byEmail.values().stream().filter(u -> u.userId() == userId).findFirst().ifPresent(u -> {
            byEmail.remove(u.email());
            byEmail.put(email, new User(u.userId(), fullName, email, u.passwordHash(),
                    u.role(), u.createdAt(), LocalDateTime.now()));
        });
    }

    @Override
    public synchronized void updatePasswordHash(String email, String passwordHash) {
        String key = email.trim().toLowerCase();
        User u = byEmail.get(key);
        if (u != null) {
            byEmail.put(key, new User(u.userId(), u.fullName(), u.email(), passwordHash,
                    u.role(), u.createdAt(), LocalDateTime.now()));
        }
    }
}