package com.example.view;

import com.example.controller.AuthController;
import com.example.controller.SessionController;
import com.example.model.User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;

/** Shared by the admin and user Settings pages. */
public class ChangePasswordDialog extends Dialog {

    public ChangePasswordDialog(AuthController auth, SessionController session) {
        setHeaderTitle("Change Password");
        setCloseOnOutsideClick(false);

        PasswordField current = new PasswordField("Current password");
        PasswordField next = new PasswordField("New password");
        PasswordField confirm = new PasswordField("Confirm new password");
        current.setWidthFull();
        next.setWidthFull();
        confirm.setWidthFull();

        Button save = new Button("Save", e -> {
            User user = session.getCurrentUser().orElse(null);
            if (user == null) {
                Notification.show("Please log in again.");
                close();
                return;
            }
            if (current.isEmpty() || next.isEmpty() || confirm.isEmpty()) {
                Notification.show("Fill in all three fields.");
                return;
            }
            if (!next.getValue().equals(confirm.getValue())) {
                Notification.show("Passwords do not match.");
                return;
            }
            try {
                User updated = auth.changePassword(user, current.getValue(), next.getValue());
                session.login(updated); // keeps the session's copy of the account up to date
                Notification.show("Password changed.");
                close();
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage());
            }
        });
        Button cancel = new Button("Cancel", e -> close());
        getFooter().add(cancel, save);

        VerticalLayout form = new VerticalLayout(current, next, confirm);
        form.setPadding(false);
        form.setWidth("320px");
        add(form);
    }
}