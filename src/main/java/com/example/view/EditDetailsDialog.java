package com.example.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import com.example.controller.AuthController;
import com.example.controller.SessionController;
import com.example.model.User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

/** Lets the signed-in user change their name and email. */
public class EditDetailsDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(EditDetailsDialog.class);

    public EditDetailsDialog(AuthController auth, SessionController session) {
        setHeaderTitle("Change User Details");
        setCloseOnOutsideClick(false);

        User current = session.getCurrentUser().orElse(null);
        TextField name = new TextField("Full name");
        EmailField email = new EmailField("Email");
        if (current != null) {
            name.setValue(current.fullName());
            email.setValue(current.email());
        }
        name.setWidthFull();
        email.setWidthFull();

        Button save = new Button("Save", e -> {
            User user = session.getCurrentUser().orElse(null);
            if (user == null) {
                Notification.show("Please log in again.");
                close();
                return;
            }
            try {
                session.login(auth.updateDetails(user, name.getValue(), email.getValue()));
                Notification.show("Details updated.");
                close();
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage());
            } catch (DataAccessException ex) {
                log.error("Could not update user details", ex);
                Notification.show("Could not save your details. Please try again.");
            }
        });
        Button cancel = new Button("Cancel", e -> close());
        getFooter().add(cancel, save);

        VerticalLayout form = new VerticalLayout(name, email);
        form.setPadding(false);
        form.setWidth("320px");
        add(form);
    }
}
