package com.example.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.model.Role;
import com.example.model.User;
import com.vaadin.flow.server.VaadinSession;

/**
 * TEMPORARY (development/testing only). Keeps a demo, non-admin identity in the
 * server-side VaadinSession so the public reporting features can be tested before
 * real login exists. No database, no password, no registration.
 *
 * The demo user is never saved anywhere and is always Role.USER, so this class
 * cannot be used to become an admin. Turn the feature off with
 * ayos.dev.demo-login=false (see application.properties).
 *
 * When real login is built, replace the body of continueAsNonAdmin() and keep
 * getCurrentUser()/isLoggedIn()/reset() so the views don't change.
 */
@Service
public class SessionController {

    public static final String DEMO_NAME = "Demo Resident";

    /**
     * Placeholder id for the demo resident. Not persisted. Exists only because
     * Report.submit(...) requires a positive user id. Kept well outside the
     * range a real users table would use, and never written to any repository.
     */
    private static final int DEMO_USER_ID = 999_999;

    private final boolean demoLoginEnabled;

    public SessionController(@Value("${ayos.dev.demo-login:false}") boolean demoLoginEnabled) {
        this.demoLoginEnabled = demoLoginEnabled;
    }

    public boolean isDemoLoginEnabled() {
        return demoLoginEnabled;
    }

    /** Stores the demo resident in the current Vaadin session. */
    public void continueAsNonAdmin() {
        if (!demoLoginEnabled) {
            throw new IllegalStateException("Demo login is disabled.");
        }
        LocalDateTime now = LocalDateTime.now();
        // Nothing is saved to a users table. The passwordHash is a placeholder only
        // to satisfy the record's validation; nothing ever checks it.
        User demo = new User(DEMO_USER_ID, DEMO_NAME, "demo.resident@example.test",
                "DEMO-NOT-A-REAL-HASH", Role.USER, now, now);
        session().setAttribute(User.class, demo);
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

    /** Clears the testing state (logs out). */
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