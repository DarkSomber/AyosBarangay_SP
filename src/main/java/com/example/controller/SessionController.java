package com.example.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import com.example.model.User;
import com.example.model.UserRepository;
import com.vaadin.flow.server.VaadinSession;

/**
 * Holds the logged-in user in the server-side VaadinSession.
 * login() is used by the real login form. The demo methods look up seeded
 * accounts, so they satisfy the database foreign keys. Demo login is forced
 * off under the "prod" profile, whatever the property says.
 */
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

    /** Called after credentials were verified. */
    public void login(User user) {
        session().setAttribute(User.class, user);
    }

    // ----- DEV / TESTING ONLY -----

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

    // ----- Session state -----

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

    /** Logs out. */
    public void reset() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(User.class, null); // null removes the attribute
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
