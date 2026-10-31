package com.example.view.Login;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import com.example.controller.AuthController;
import com.example.controller.PasswordResetController;
import com.example.model.User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

/** Sign-up form for residents. Calls onRegistered with the new account. */
public class RegisterDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(RegisterDialog.class);

    public RegisterDialog(AuthController auth, Consumer<User> onRegistered) {
        setHeaderTitle("Create Account");
        setCloseOnOutsideClick(false);

        TextField name = new TextField("Full name");
        EmailField email = new EmailField("Email");
        PasswordField password = new PasswordField("Password");
        PasswordField confirm = new PasswordField("Confirm password");
        password.setHelperText("At least " + PasswordResetController.MIN_PASSWORD_LENGTH + " characters");
        name.setWidthFull();
        email.setWidthFull();
        password.setWidthFull();
        confirm.setWidthFull();

        Button create = new Button("Create account", e -> {
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Notification.show("Fill in all fields.");
                return;
            }
            if (!password.getValue().equals(confirm.getValue())) {
                Notification.show("Passwords do not match.");
                return;
            }
            try {
                User user = auth.register(name.getValue(), email.getValue(), password.getValue());
                close();
                onRegistered.accept(user);
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage());
            } catch (DataAccessException ex) {
                log.error("Could not create account", ex);
                Notification.show("Could not create the account. Please try again.");
            }
        });
        Button cancel = new Button("Cancel", e -> close());
        getFooter().add(cancel, create);

        VerticalLayout form = new VerticalLayout(name, email, password, confirm);
        form.setPadding(false);
        form.setWidth("320px");
        add(form);
    }
}
