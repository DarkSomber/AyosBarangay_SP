package com.example.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** MySQL-backed implementation of UserRepository. */
@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcClient jdbc;

    public JdbcUserRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jdbc.sql("SELECT * FROM users WHERE email = :email")
                .param("email", email.trim().toLowerCase())
                .query(this::mapUser)
                .optional();
    }

    @Override
    public List<User> findAllByIds(Collection<Integer> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jdbc.sql("SELECT * FROM users WHERE user_id IN (:ids)")
                .param("ids", ids)
                .query(this::mapUser)
                .list();
    }

    @Override
    public User save(User u) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.sql("""
                INSERT INTO users (full_name, email, password_hash, role, created_at, updated_at)
                VALUES (:name, :email, :hash, :role, :created, :updated)
                """)
                .param("name", u.fullName())
                .param("email", u.email())
                .param("hash", u.passwordHash())
                .param("role", u.role().name())
                .param("created", u.createdAt())
                .param("updated", u.updatedAt())
                .update(keys);

        int id = keys.getKey().intValue();
        return new User(id, u.fullName(), u.email(), u.passwordHash(), u.role(),
                u.createdAt(), u.updatedAt());
    }

    @Override
    public void updatePasswordHash(String email, String passwordHash) {
        jdbc.sql("UPDATE users SET password_hash = :hash WHERE email = :email")
                .param("hash", passwordHash)
                .param("email", email.trim().toLowerCase())
                .update();
    }

    @Override
    public void updateDetails(int userId, String fullName, String email) {
        jdbc.sql("UPDATE users SET full_name = :name, email = :email, updated_at = :now WHERE user_id = :id")
                .param("name", fullName)
                .param("email", email)
                .param("now", LocalDateTime.now())
                .param("id", userId)
                .update();
    }

    private User mapUser(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role")),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("updated_at", LocalDateTime.class));
    }
}