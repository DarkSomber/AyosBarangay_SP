package com.example.view.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Admin: Settings. Change password, barangay details, new-report notifications
 * and logout. None of these are wired to real data yet; each shows a message
 * until the login and user features exist.
 */
@Route(value = "admin/settings", layout = AdminLayout.class)
@PageTitle("Settings | AyosBarangay")
public class AdminSettingsView extends Div {

    public AdminSettingsView() {
        addClassName("admin-page");
        add(buildPanel());
    }

    private Div buildPanel() {
        Button editPassword = actionButton("Edit",
                () -> Notification.show("Change password is coming soon."));
        Button viewDetails = actionButton("View",
                () -> Notification.show("Barangay details are coming soon."));

        Checkbox notifications = new Checkbox();
        notifications.setAriaLabel("New report notifications");
        notifications.addValueChangeListener(e -> Notification.show(
                e.getValue() ? "New report notifications turned on." : "New report notifications turned off."));
        // TODO: save this preference once admin accounts exist.

        // TODO: when login exists, end the admin session here before leaving.
        Button logout = new Button("Logout", e -> UI.getCurrent().navigate(""));
        logout.addClassName("settings-logout");

        Div panel = new Div(
                settingRow("Change Password", editPassword),
                settingRow("View Barangay Details", viewDetails),
                settingRow("New Report Notifications", notifications),
                logout);
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