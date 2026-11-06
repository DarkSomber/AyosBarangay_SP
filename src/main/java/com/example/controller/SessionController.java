package com.example.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import com.example.model.User;
import com.example.model.UserRepository;
import com.vaadin.flow.server.VaadinSession;

@Service
public class SessionController {

    public static final String DEMO_NAME = "Demo Resident";
    public static final String DEMO_RESIDENT_EMAIL = "demo.resident@example.test";
    public static final String DEMO_ADMIN_EMAIL = "demo.admin@example.test";

    private final boolean demoLoginEnabled;
    private final UserRepository users;

    public SessionController(@Value("${ayos.dev.demo-login:false}") boolean demoLoginFlag,
                             Environment env, UserRepository users) {
        this.demoLoginEnabled = demoLoginFlag && !env.acceptsProfiles(Profiles.of("prod"));
        this.users = users;
    }

    public boolean isDemoLoginEnabled() {
        return demoLoginEnabled;
    }

    public void login(User user) {
        session().setAttribute(User.class, user);
    }


    public void continueAsNonAdmin() {
        loginDemo(DEMO_RESIDENT_EMAIL);
    }

    public void continueAsAdmin() {
        loginDemo(DEMO_ADMIN_EMAIL);
    }

    private void loginDemo(String email) {
        if (!demoLoginEnabled) {
            throw new IllegalStateException("Demo login is disabled.");
        }
        User demo = users.findByEmail(email).orElseThrow(() -> new IllegalStateException(
                "Demo account missing. Start MySQL, apply schema.sql, then restart the app."));
        login(demo);
    }


    public Optional<User> getCurrentUser() {
        VaadinSession session = VaadinSession.getCurrent();
        return session == null ? Optional.empty() : Optional.ofNullable(session.getAttribute(User.class));
    }

    public boolean isLoggedIn() {
        return getCurrentUser().isPresent();
    }

    public boolean isAdmin() {
        return getCurrentUser().map(User::isAdmin).orElse(false);
    }

    public void reset() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(User.class, null);
        }
    }

    private VaadinSession session() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            throw new IllegalStateException("No Vaadin session is active.");
        }
        return session;
    }
}