package com.example.config;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.controller.SessionController;
import com.example.model.Role;
import com.example.model.User;
import com.example.model.UserRepository;

/**
 * DEV ONLY. Creates a demo resident and a demo admin so the test buttons work
 * with real database rows. Never runs under the "prod" profile or when
 * ayos.dev.demo-login=false.
 */
@Component
@Profile("!prod")
public class DevDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final boolean enabled;
    private final String password;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public DevDataSeeder(@Value("${ayos.dev.demo-login:false}") boolean enabled,
                         @Value("${ayos.dev.demo-password:demo1234}") String password,
                         UserRepository users, PasswordEncoder encoder) {
        this.enabled = enabled;
        this.password = password;
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        try {
            seed(SessionController.DEMO_NAME, SessionController.DEMO_RESIDENT_EMAIL, Role.USER);
            seed("Demo Admin", SessionController.DEMO_ADMIN_EMAIL, Role.ADMIN);
        } catch (DataAccessException ex) {
            log.warn("Could not seed demo accounts. Is the database running and schema.sql applied? {}",
                    ex.getMessage());
        }
    }

    private void seed(String name, String email, Role role) {
        if (users.findByEmail(email).isPresent()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        users.save(new User(null, name, email, encoder.encode(password), role, now, now));
    }
}
