package com.example.view.Login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Login screen. The view builds the UI only; authentication is not wired yet.
 * Styling lives in styles.css (see login-styles.css for the rules to paste in).
 */
@Route("")
@PageTitle("Log in | AyosBarangay")
public class LoginView extends Div {

    private final EmailField email = new EmailField("Email");
    private final PasswordField password = new PasswordField("Password");

    public LoginView() {
        addClassName("login-page");
        add(buildBrand(), buildCard());
    }

    // ---------- UI building ----------

    /** Left side: logo circle + app name. */
    private Div buildBrand() {
        // Placeholder circle. Later: replace with
        //   Image logo = new Image("images/logo.png", "AyosBarangay logo");
        //   logo.addClassName("login-logo");
        // and put the file in src/main/resources/META-INF/resources/images/
        Div logo = new Div();
        logo.addClassNames("login-logo", "login-logo-placeholder");

        H3 name = new H3("AyosBarangay");
        name.addClassName("login-brand-name");

        Div brand = new Div(logo, name);
        brand.addClassName("login-brand");
        return brand;
    }

    /** Right side: grey card with the form. */
    private Div buildCard() {
        H2 heading = new H2("Dito sa Serbisyong Tunay");
        heading.addClassName("login-heading");

        email.addClassName("login-field");
        email.setRequiredIndicatorVisible(true);
        email.setErrorMessage("Enter a valid email address");

        password.addClassName("login-field");
        password.setRequiredIndicatorVisible(true);

        Button login = new Button("Log In", e -> attemptLogin());
        login.addClassName("login-button");

        Button forgot = new Button("Forgot password", e -> new ForgotPasswordDialog().open());
        forgot.addClassName("login-forgot");

        Div form = new Div(email, password);
        form.addClassName("login-form");

        Div actions = new Div(login, forgot);
        actions.addClassName("login-actions");

        Div card = new Div(heading, form, actions);
        card.addClassName("login-card");
        return card;
    }

    // ---------- Actions ----------

    private void attemptLogin() {
        // TEMP-DEV: validation disabled so the screens can be tested freely. Restore later.
        // if (email.isEmpty() || email.isInvalid() || password.isEmpty()) {
        //     Notification.show("Enter your email and password.");
        //     return;
        // }
        // TEMP-DEV: goes straight to the user dashboard without checking credentials.
        // TODO: forward email/password to the controller or Spring Security, then navigate only on success.
        UI.getCurrent().navigate("user/reports");
    }
}