package com.example.view.admin;

import com.example.config.BarangayInfo;
import com.example.controller.AuthController;
import com.example.controller.SessionController;
import com.example.view.ChangePasswordDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


@Route(value = "admin/settings", layout = AdminLayout.class)
@PageTitle("Settings | AyosBarangay")
public class AdminSettingsView extends Div {

    private final AuthController auth;
    private final SessionController session;
    private final BarangayInfo info;

    public AdminSettingsView(AuthController auth, SessionController session, BarangayInfo info) {
        this.info = info;
        this.auth = auth;
        this.session = session;
        addClassName("admin-page");
        add(buildPanel());
    }

    private Div buildPanel() {
        Button editPassword = actionButton("Edit",
                () -> new ChangePasswordDialog(auth, session).open());
        Button viewDetails = actionButton("View",
                () -> new BarangayInfoDialog(info).open());

        Checkbox notifications = new Checkbox();
        notifications.setAriaLabel("New report notifications");
        notifications.addValueChangeListener(e -> Notification.show(
                e.getValue() ? "New report notifications turned on." : "New report notifications turned off."));
        // TODO: save this preference once it has a table to live in.

        Div panel = new Div(
                settingRow("Change Password", editPassword),
                settingRow("View Barangay Details", viewDetails),
                settingRow("New Report Notifications", notifications));
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