package com.example.view.Login;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.example.controller.PasswordResetController;
import com.vaadin.flow.data.value.ValueChangeMode;

public class ForgotPasswordDialog extends Dialog {

    private static final int CODE_LENGTH = 6;
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final Div content = new Div();

    private final EmailField email = new EmailField("Email Address");
    private final List<TextField> codeBoxes = new ArrayList<>();
    private final PasswordResetController reset;
    private final PasswordField newPassword = new PasswordField("Password");
    private final PasswordField confirmPassword = new PasswordField("Confirm Password");

    public ForgotPasswordDialog(PasswordResetController reset) {
        this.reset = reset;
        addClassName("forgot-dialog");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setCloseOnOutsideClick(true);
        setCloseOnEsc(true);

        content.addClassName("forgot-root");
        add(content);
        showEmailStep();
    }


    private void showEmailStep() {
        email.addClassName("forgot-field");
        email.setErrorMessage("Enter a valid email address");

        Button submit = primaryButton("Submit", e -> {
            if (email.isEmpty() || email.isInvalid()) {
                Notification.show("Enter a valid email address.");
                return;
            }
            if (!requestCode()) {
                return;
            }
            Notification.show("If that email is registered, a verification code has been generated.");
            showCodeStep();
        });

        showPanel("Forgot Your Password?",
                "Please enter your email address to get a verification code to reset your password.",
                new Div(email),
                submit);
    }

    private void showCodeStep() {
        Div boxes = new Div();
        boxes.addClassName("forgot-code-row");
        buildCodeBoxes(boxes);

        Button verify = primaryButton("Verify", e -> {
            String code = readCode();
            if (code.length() < CODE_LENGTH) {
                Notification.show("Enter the " + CODE_LENGTH + "-digit code.");
                return;
            }
            if (!reset.verifyResetCode(email.getValue(), code)) {
                codeBoxes.forEach(TextField::clear);
                codeBoxes.get(0).focus();
                Notification.show("Incorrect or expired code.");
                return;
            }
            showPasswordStep();
        });

        Button resend = new Button("Resend Code", e -> {
            if (!requestCode()) {
                return;
            }
            codeBoxes.forEach(TextField::clear);
            codeBoxes.get(0).focus();
            Notification.show("If that email is registered, a new code has been generated.");
        });
        resend.addClassName("forgot-link");

        showPanel("Enter Verification Code",
                "Enter the " + CODE_LENGTH + "-digit verification code.",
                boxes,
                verify, resend);
        codeBoxes.get(0).focus();
    }

    private void showPasswordStep() {
        newPassword.clear();
        confirmPassword.clear();
        newPassword.addClassName("forgot-field");
        confirmPassword.addClassName("forgot-field");

        Div fields = new Div(newPassword, confirmPassword);
        fields.addClassName("forgot-fields");

        Button confirm = primaryButton("Confirm", e -> {
            String pw = newPassword.getValue();
            if (pw.length() < MIN_PASSWORD_LENGTH) {
                Notification.show("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
                return;
            }
            if (!pw.equals(confirmPassword.getValue())) {
                Notification.show("Passwords do not match.");
                return;
            }
            try {
                reset.resetPassword(email.getValue(), readCode(), pw);
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage());
                showEmailStep();
                return;
            }
            showDoneStep();
        });

        showPanel("Create New Password",
                "Enter your new password for your account.",
                fields,
                confirm);
    }

    private void showDoneStep() {
        Button back = primaryButton("Back to Login", e -> close());
        showPanel("Your Password Has Been Updated!", null, null, back);
    }


    private void showPanel(String title, String subtitle, Component body, Component... actions) {
        H2 heading = new H2(title);
        heading.addClassName("forgot-heading");

        Div panel = new Div(heading);
        panel.addClassName("forgot-step");

        if (subtitle != null) {
            Span sub = new Span(subtitle);
            sub.addClassName("forgot-subtitle");
            panel.add(sub);
        }
        if (body != null) {
            panel.add(body);
        }

        Div actionBar = new Div(actions);
        actionBar.addClassName("forgot-actions");
        panel.add(actionBar);

        content.removeAll();
        content.add(panel);
    }

    private Button primaryButton(String text, ComponentEventListener<ClickEvent<Button>> onClick) {
        Button b = new Button(text, onClick);
        b.addClassName("forgot-button");
        return b;
    }

    private void buildCodeBoxes(Div row) {
        codeBoxes.clear();
        for (int i = 0; i < CODE_LENGTH; i++) {
            final int index = i;
            TextField box = new TextField();
            box.addClassName("code-box");
            box.setMaxLength(1);
            box.setValueChangeMode(ValueChangeMode.EAGER);
            box.setAllowedCharPattern("[0-9]");
            box.setAriaLabel("Digit " + (i + 1));

            box.addValueChangeListener(e -> {
                if (!e.getValue().isEmpty() && index < CODE_LENGTH - 1) {
                    codeBoxes.get(index + 1).focus();
                }
            });
            box.addKeyDownListener(Key.BACKSPACE, e -> {
                if (box.isEmpty() && index > 0) {
                    codeBoxes.get(index - 1).focus();
                }
            });

            codeBoxes.add(box);
            row.add(box);
        }
    }

    private boolean requestCode() {
        try {
            reset.sendResetCode(email.getValue()).ifPresent(this::showCodeNotification);
            return true;
        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage());
            return false;
        }
    }

    private void showCodeNotification(String code) {
        Notification n = new Notification();
        n.add(new Span("Your verification code: " + code + " (valid for 10 minutes)"));
        n.setPosition(Notification.Position.TOP_CENTER);
        n.setDuration(30000);
        n.open();
    }

    private String readCode() {
        StringBuilder sb = new StringBuilder();
        codeBoxes.forEach(b -> sb.append(b.getValue()));
        return sb.toString();
    }
}