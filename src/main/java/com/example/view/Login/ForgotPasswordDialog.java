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

/**
 * Forgot Password flow: one dialog that swaps its panel through four steps.
 *   1. Enter email  2. Enter verification code  3. Create new password  4. Done
 * The dialog only builds the UI and checks the input. The backend calls are
 * marked TODO until the controller/auth exists.
 */
public class ForgotPasswordDialog extends Dialog {

    private static final int CODE_LENGTH = 6;
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final Div content = new Div();

    private final EmailField email = new EmailField("Email Address");
    private final List<TextField> codeBoxes = new ArrayList<>();
    private final PasswordField newPassword = new PasswordField("Password");
    private final PasswordField confirmPassword = new PasswordField("Confirm Password");

    public ForgotPasswordDialog() {
        addClassName("forgot-dialog");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING); // the grey panel provides its own padding
        setCloseOnOutsideClick(true);
        setCloseOnEsc(true);

        content.addClassName("forgot-root"); // fixed size, set in styles.css
        add(content);
        showEmailStep();
    }

    // ---------- Steps ----------

    /** Step 1: ask for the email address. */
    private void showEmailStep() {
        email.addClassName("forgot-field");
        email.setErrorMessage("Enter a valid email address");

        Button submit = primaryButton("Submit", e -> {
            // TEMP-DEV: validation disabled so the screens can be tested freely. Restore later.
            // if (email.isEmpty() || email.isInvalid()) {
            //     Notification.show("Enter a valid email address.");
            //     return;
            // }
            // TODO: controller.sendResetCode(email.getValue());
            showCodeStep();
        });

        showPanel("Forgot Your Password?",
                "Please enter your email address to receive a verification code to reset your password.",
                new Div(email),
                submit);
    }

    /** Step 2: six single-digit boxes. */
    private void showCodeStep() {
        Div boxes = new Div();
        boxes.addClassName("forgot-code-row");
        buildCodeBoxes(boxes);

        Button verify = primaryButton("Verify", e -> {
            // TEMP-DEV: code check disabled so the screens can be tested freely. Restore later.
            // String code = readCode();
            // if (code.length() < CODE_LENGTH) {
            //     Notification.show("Enter the " + CODE_LENGTH + "-digit code.");
            //     return;
            // }
            // TODO: if (!controller.verifyResetCode(email.getValue(), code)) { show error; return; }
            showPasswordStep();
        });

        Button resend = new Button("Resend Code", e -> {
            // TODO: controller.sendResetCode(email.getValue());
            codeBoxes.forEach(TextField::clear);
            codeBoxes.get(0).focus();
            Notification.show("A new code was sent to your email.");
        });
        resend.addClassName("forgot-link");

        showPanel("Enter Verification Code",
                "Enter the verification code sent to your email address.",
                boxes,
                verify, resend);
        codeBoxes.get(0).focus();
    }

    /** Step 3: new password + confirmation. */
    private void showPasswordStep() {
        newPassword.clear();
        confirmPassword.clear();
        newPassword.addClassName("forgot-field");
        confirmPassword.addClassName("forgot-field");

        Div fields = new Div(newPassword, confirmPassword);
        fields.addClassName("forgot-fields");

        Button confirm = primaryButton("Confirm", e -> {
            // TEMP-DEV: password checks disabled so the screens can be tested freely. Restore later.
            // String pw = newPassword.getValue();
            // if (pw.length() < MIN_PASSWORD_LENGTH) {
            //     Notification.show("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
            //     return;
            // }
            // if (!pw.equals(confirmPassword.getValue())) {
            //     Notification.show("Passwords do not match.");
            //     return;
            // }
            // TODO: controller.resetPassword(email.getValue(), readCode(), pw);
            showDoneStep();
        });

        showPanel("Create New Password",
                "Enter your new password for your account.",
                fields,
                confirm);
    }

    /** Step 4: confirmation. */
    private void showDoneStep() {
        Button back = primaryButton("Back to Login", e -> close());
        showPanel("Your Password Has Been Updated!", null, null, back);
    }

    // ---------- Helpers ----------

    /** Replaces the dialog content with one step panel. Subtitle and body may be null. */
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
            box.setAllowedCharPattern("[0-9]");
            box.setAriaLabel("Digit " + (i + 1));

            // typing a digit jumps to the next box
            box.addValueChangeListener(e -> {
                if (!e.getValue().isEmpty() && index < CODE_LENGTH - 1) {
                    codeBoxes.get(index + 1).focus();
                }
            });
            // backspace on an empty box jumps back
            box.addKeyDownListener(Key.BACKSPACE, e -> {
                if (box.isEmpty() && index > 0) {
                    codeBoxes.get(index - 1).focus();
                }
            });

            codeBoxes.add(box);
            row.add(box);
        }
    }

    private String readCode() {
        StringBuilder sb = new StringBuilder();
        codeBoxes.forEach(b -> sb.append(b.getValue()));
        return sb.toString();
    }
}