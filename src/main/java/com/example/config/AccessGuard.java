package com.example.config;

import org.springframework.stereotype.Component;

import com.example.controller.SessionController;
import com.example.view.Login.LoginView;
import com.example.view.PublicDashboardView;
import com.example.view.admin.AdminReportsView;
import com.example.view.user.UserReportsView;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

/**
 * Route protection and start page.
 *   first page of every load/reload -> always the public dashboard
 *   admin/*  -> admins only
 *   user/*   -> logged-in non-admins only
 *   ""       -> public dashboard, open to everyone (the app's start page)
 *   login    -> login screen; people already logged in go to their own panel
 *
 * A browser reload or a newly opened tab creates a new UI, so the first
 * navigation of each UI is sent to the public dashboard. Moving around inside
 * the app (menu, login, logout) is not a new UI and works as before.
 */
@Component
public class AccessGuard implements VaadinServiceInitListener {

    private final SessionController session;

    public AccessGuard(SessionController session) {
        this.session = session;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            boolean[] firstNavigation = {true};
            uiEvent.getUI().addBeforeEnterListener(e -> {
                boolean first = firstNavigation[0];
                firstNavigation[0] = false;
                beforeEnter(e, first);
            });
        });
    }

    private void beforeEnter(BeforeEnterEvent event, boolean firstNavigation) {
        // Reload or fresh open: start on the public dashboard, whatever the URL was.
        if (firstNavigation && !event.getLocation().getPath().isEmpty()) {
            event.forwardTo(PublicDashboardView.class);
            return;
        }

        boolean loggedIn = session.isLoggedIn();
        boolean admin = session.isAdmin();

        switch (event.getLocation().getFirstSegment()) {
            case "admin" -> {
                if (!loggedIn) {
                    event.forwardTo(LoginView.class);
                } else if (!admin) {
                    event.forwardTo(UserReportsView.class);
                }
            }
            case "user" -> {
                if (!loggedIn) {
                    event.forwardTo(LoginView.class);
                } else if (admin) {
                    event.forwardTo(AdminReportsView.class);
                }
            }
            case "login" -> {
                // Already logged in: skip the login screen and go to your own panel.
                if (loggedIn) {
                    if (admin) {
                        event.forwardTo(AdminReportsView.class);
                    } else {
                        event.forwardTo(UserReportsView.class);
                    }
                }
            }
            default -> { }
        }
    }
}