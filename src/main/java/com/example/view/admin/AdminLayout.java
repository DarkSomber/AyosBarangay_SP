package com.example.view.admin;

import org.springframework.dao.DataAccessException;

import com.example.controller.NotificationController;
import com.example.controller.ReportNotifier;
import com.example.controller.SessionController;
import com.example.view.BrandLogo;
import com.example.view.NavBar;
import com.vaadin.flow.component.HasElement;
import com.example.model.User;
import com.example.view.DevToolsBar;
import com.example.view.NotificationBell;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;

public class AdminLayout extends Div implements RouterLayout, AfterNavigationObserver {

    private final NavBar navBar;
    private final Div contentArea = new Div();

    private final SessionController session;
    private final ReportNotifier notifier;
    private final NotificationController notifications;
    private final User admin;
    private NotificationBell bell;

    public AdminLayout(SessionController session, ReportNotifier notifier, NotificationController notifications) {
        this.session = session;
        this.notifier = notifier;
        this.notifications = notifications;
        this.admin = session.getCurrentUser().filter(User::isAdmin).orElse(null);
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
        if (admin != null) {
            bell = new NotificationBell(notifications.adminSource(admin));
            header.add(bell);
        }

        contentArea.addClassName("admin-content");

        Div main = new Div(header, new DevToolsBar(session, notifier), contentArea);
        main.addClassName("admin-main");
        return main;
    }

    /** While open, shows a toast, sound and screen flash for each new report (if the setting is on). */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (admin == null) {
            return;
        }
        UI ui = attachEvent.getUI();
        Registration reg = notifier.register(alert -> ui.access(() -> {
            boolean on;
            try {
                on = notifications.isEnabled(admin);
            } catch (DataAccessException ex) {
                on = false;
            }
            if (on) {
                showAlert(ui, alert);
                attention(ui);
            }
            bell.refresh();
        }));
        addDetachListener(e -> reg.remove());
    }

    private void showAlert(UI ui, ReportNotifier.NewReportAlert alert) {
        Notification n = new Notification(new Span(alert.message()));
        Button view = new Button("View", e -> {
            update(() -> notifications.markReportRead(admin, alert.reportId()), n);
            ui.navigate(AdminReportsView.class);
        });
        Button markRead = new Button("Mark as read",
                e -> update(() -> notifications.markReportRead(admin, alert.reportId()), n));
        Button ignore = new Button("Ignore",
                e -> update(() -> notifications.ignoreReport(admin, alert.reportId()), n));
        n.add(view, markRead, ignore);
        n.setPosition(Notification.Position.TOP_END);
        n.setDuration(8000);
        n.open();
    }

    /** Runs a notification action, closes the toast and refreshes the bell. */
    private void update(Runnable action, Notification toast) {
        try {
            action.run();
        } catch (DataAccessException ex) {
            // the bell keeps its dot; nothing else to do
        }
        bell.refresh();
        toast.close();
    }

    /** Short beep (if the browser allows audio) and a red flash over the whole page. */
    private void attention(UI ui) {
        ui.getPage().executeJs("""
                document.body.classList.add('ayos-flash');
                setTimeout(() => document.body.classList.remove('ayos-flash'), 1600);
                try {
                  const c = new (window.AudioContext || window.webkitAudioContext)();
                  const o = c.createOscillator(), g = c.createGain();
                  o.frequency.value = 880;
                  g.gain.setValueAtTime(0.15, c.currentTime);
                  g.gain.exponentialRampToValueAtTime(0.001, c.currentTime + 0.6);
                  o.connect(g); g.connect(c.destination);
                  o.start(); o.stop(c.currentTime + 0.6);
                } catch (e) { }
                """);
    }

    /** Puts the current admin view inside the content area. */
    @Override
    public void showRouterLayoutContent(HasElement content) {
        if (content != null) {
            contentArea.getElement().appendChild(content.getElement());
        }
    }

    /** Highlights the sidebar button that matches the current page. */
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        navBar.highlight(event.getLocation().getPath());
    }
}