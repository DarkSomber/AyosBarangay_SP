package com.example.view.user;

import com.example.controller.NotificationController;
import com.example.controller.ReportNotifier;
import com.example.controller.SessionController;
import com.example.model.User;
import com.example.view.BrandLogo;
import com.example.view.NavBar;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.HasElement;
import com.example.view.DevToolsBar;
import com.example.view.NotificationBell;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;

public class UserLayout extends Div implements RouterLayout, AfterNavigationObserver {

    private final NavBar navBar;
    private final Div contentArea = new Div();

    private final SessionController session;
    private final ReportNotifier notifier;
    private final NotificationController notifications;
    private final User resident;
    private NotificationBell bell;

    public UserLayout(SessionController session, ReportNotifier notifier, NotificationController notifications) {
        this.session = session;
        this.notifier = notifier;
        this.notifications = notifications;
        this.resident = session.getCurrentUser().filter(u -> !u.isAdmin()).orElse(null);
        this.navBar = new NavBar(session);
        addClassName("admin-shell");
        NavBar.restore(this);
        add(NavBar.menuButton(() -> NavBar.toggle(this)), navBar, buildMain());
    }

    private Div buildMain() {
        H3 title = new H3("AyosBarangay");
        title.addClassName("dashboard-title");

        Div brand = new Div(BrandLogo.create("brand-logo"), title);
        brand.addClassName("brand-lockup");

        Div header = new Div(brand);
        header.addClassNames("dashboard-header", "user-header");
        if (resident != null) {
            bell = new NotificationBell(notifications.userSource(resident));
            header.add(bell);
        }

        contentArea.addClassName("admin-content");

        Div main = new Div(header, new DevToolsBar(session, notifier), contentArea);
        main.addClassName("admin-main");
        return main;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (resident == null) {
            return;
        }
        UI ui = attachEvent.getUI();
        Registration reg = notifier.registerStatus(alert -> {
            if (alert.userId() != resident.userId()) {
                return;
            }
            ui.access(() -> {
                Notification n = new Notification(new Span(alert.message()));
                n.add(new Button("View", e -> {
                    n.close();
                    ui.navigate(UserReportsView.class);
                }));
                n.setPosition(Notification.Position.TOP_END);
                n.setDuration(8000);
                n.open();
                bell.refresh();
            });
        });
        addDetachListener(e -> reg.remove());
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        if (content != null) {
            contentArea.getElement().appendChild(content.getElement());
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        navBar.highlight(event.getLocation().getPath());
    }
}