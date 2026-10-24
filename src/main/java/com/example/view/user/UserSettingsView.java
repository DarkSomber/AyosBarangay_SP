package com.example.view.user;

import com.example.controller.AuthController;
import com.example.controller.SessionController;
import com.example.view.ChangePasswordDialog;
import com.example.view.admin.BarangayInfoDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * User: Settings. Change user details and view barangay details.
 * Neither is wired to real data yet; each shows a message until the
 * login and user features exist. Logout lives in the sidebar (UserLayout).
 */
@Route(value = "user/settings", layout = UserLayout.class)
@PageTitle("Settings | AyosBarangay")
public class UserSettingsView extends Div {

    private final AuthController auth;
    private final SessionController session;

    public UserSettingsView(AuthController auth, SessionController session) {
        this.auth = auth;
        this.session = session;
        addClassName("admin-page");
        add(buildPanel());
    }

    private Div buildPanel() {
        Button editDetails = actionButton("Edit",
                () -> new ChangePasswordDialog(auth, session).open());
        Button viewDetails = actionButton("View",
                        () -> new BarangayInfoDialog().open());

        Div panel = new Div(
                settingRow("Change User Details", editDetails),
                settingRow("View Barangay Details", viewDetails));
        panel.addClassNames("dashboard-panel", "settings-panel");
        return panel;
    }

    private Div settingRow(String label, Component control) {
        Span text = new Span(label);
        text.addClassName("settings-label");
        Div row = new Div(text, control);
        row.addClassName("settings-row");
        return row;
    }

    private Button actionButton(String text, Runnable action) {
        Button button = new Button(text, e -> action.run());
        button.addClassName("settings-action");
        return button;
    }
}