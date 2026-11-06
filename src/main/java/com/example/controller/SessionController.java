package com.example.controller;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.model.User;
import com.example.model.UserRepository;
import com.vaadin.flow.server.VaadinSession;

@Service
public class SessionController {

    private final UserRepository users;

    public SessionController(UserRepository users) {
        this.users = users;
    }

    public void login(User user) {
        session().setAttribute(User.class, user);
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