package com.example.view;

import com.example.controller.ReportNotifier;
import com.example.controller.ReportNotifier.NewReportAlert;
import com.example.controller.SessionController;
import com.example.view.admin.AdminReportsView;
import com.example.view.user.UserReportsView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;

public class DevToolsBar extends Div {

    public DevToolsBar(SessionController session, ReportNotifier notifier) {
        addClassName("dev-bar");
        setVisible(session.isDemoLoginEnabled());
        if (!isVisible()) {
            return;
        }

        Span badge = new Span("DEV / TESTING ONLY");
        badge.addClassName("dev-badge");

        String who = session.getCurrentUser()
                .map(u -> "Testing as " + u.fullName() + (u.isAdmin() ? " (admin)" : " (resident)"))
                .orElse("Not logged in");
        Span status = new Span(who);
        status.addClassName("dev-status");

        Button resident = devButton("As Resident", () -> {
            session.continueAsNonAdmin();
            UI.getCurrent().navigate(UserReportsView.class);
        });
        Button admin = devButton("As Admin", () -> {
            session.continueAsAdmin();
            UI.getCurrent().navigate(AdminReportsView.class);
        });
        Button alert = devButton("Send test notification", () -> notifier.publish(new NewReportAlert(
                null, "Test report: broken streetlight near the barangay hall.", "Barangay Hall Road (test)")));

        add(badge, status, resident, admin, alert);
    }

    private Button devButton(String text, Runnable action) {
        Button b = new Button(text, e -> {
            try {
                action.run();
            } catch (IllegalStateException ex) {
                Notification.show(ex.getMessage());
            }
        });
        b.addClassName("dev-button");
        return b;
    }
}