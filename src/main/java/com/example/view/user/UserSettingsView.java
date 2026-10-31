package com.example.view.user;

import com.example.config.BarangayInfo;
import com.example.controller.AuthController;
import com.example.controller.SessionController;
import com.example.view.ChangePasswordDialog;
import com.example.view.EditDetailsDialog;
import com.example.view.admin.BarangayInfoDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * User: Settings. Change user details, change password and view barangay details.
 * Logout lives in the sidebar (UserLayout).
 */
@Route(value = "user/settings", layout = UserLayout.class)
@PageTitle("Settings | AyosBarangay")
public class UserSettingsView extends Div {

    private final AuthController auth;
    private final SessionController session;
    private final BarangayInfo info;

    public UserSettingsView(AuthController auth, SessionController session, BarangayInfo info) {
        this.info = info;
        this.auth = auth;
        this.session = session;
        addClassName("admin-page");
        add(buildPanel());
    }

    private Div buildPanel() {
        Button editDetails = actionButton("Edit",
                () -> new EditDetailsDialog(auth, session).open());
        Button editPassword = actionButton("Edit",
                () -> new ChangePasswordDialog(auth, session).open());
        Button viewDetails = actionButton("View",
                        () -> new BarangayInfoDialog(info).open());

        Div panel = new Div(
                settingRow("Change User Details", editDetails),
                settingRow("Change Password", editPassword),
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