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