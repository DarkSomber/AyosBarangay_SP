package com.example.view.Login;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.controller.PasswordResetController;
import com.example.controller.AuthController;
import com.example.controller.SessionController;
import com.example.model.User;
import com.example.view.admin.AdminReportsView;
import com.example.view.user.UserReportsView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("Log in | AyosBarangay")
public class LoginView extends Div {

    private static final Logger log = LoggerFactory.getLogger(LoginView.class);

    private final PasswordResetController passwordReset;
    private final AuthController auth;
    private final SessionController session;

    private final EmailField email = new EmailField("Email");
    private final PasswordField password = new PasswordField("Password");

    public LoginView(AuthController auth, SessionController session, PasswordResetController passwordReset) {
        this.auth = auth;
        this.session = session;
        this.passwordReset = passwordReset;
        addClassName("login-page");
        add(buildBubbles(), buildBrand(), buildCard());
        if (session.isDemoLoginEnabled()) {
            add(buildDevBar());
        }
    }

    // ---------- UI building ----------

    /** Decorative glass bubbles in the background (purely visual, positioned in CSS). */
    private Div buildBubbles() {
        Div bubbles = new Div();
        bubbles.addClassName("login-bubbles");
        bubbles.getElement().setAttribute("aria-hidden", "true");
        for (int i = 1; i <= 8; i++) {
            Div bubble = new Div();
            bubble.addClassNames("login-bubble", "login-bubble-" + i);
            bubbles.add(bubble);
        }
        return bubbles;
    }

    /** Left side: logo circle + app name. */
    private Div buildBrand() {
        // The grey circle. To use a real image later, either:
        //   (a) set background-image on .login-logo in CSS and drop the placeholder class, or
        //   (b) replace this Div with: Image logo = new Image("images/logo.png", "AyosBarangay logo");
        //       logo.addClassName("login-logo");
        Div logo = new Div();
        logo.addClassNames("login-logo", "login-logo-placeholder");

        H1 name = new H1("AyosBarangay");
        name.addClassName("login-brand-name");

        Div brand = new Div(logo, name);
        brand.addClassName("login-brand");
        return brand;
    }

    /** Right side: frosted card with the form. */
    private Div buildCard() {
        H2 heading = new H2("Login");
        heading.addClassName("login-heading");

        email.addClassName("login-field");
        email.setPlaceholder("example@gmail.com");
        email.setRequiredIndicatorVisible(true);
        email.setErrorMessage("Enter a valid email address");

        password.addClassName("login-field");
        password.setRequiredIndicatorVisible(true);
        password.setHelperText("*at least 1 of each: capital letter, small letter, special character, number");

        Button login = new Button("Log In", e -> attemptLogin());
        login.addClassName("login-button");

        Button forgot = new Button("Forgot password", e -> new ForgotPasswordDialog(passwordReset).open());
        forgot.addClassName("login-forgot");

        // Back to the public dashboard (visitors can view reports without an account).
        Button guest = new Button("Back to public view", e -> UI.getCurrent().navigate(""));
        guest.addClassName("login-forgot");

        Div form = new Div(email, password);
        form.addClassName("login-form");

        Div actions = new Div(login, forgot, guest);
        actions.addClassName("login-actions");

        Div card = new Div(heading, form, actions);
        card.addClassName("login-card");
        return card;
    }

    /** DEV / TESTING ONLY. One-click logins for the seeded demo accounts. */
    private Div buildDevBar() {
        Span badge = new Span("DEV / TESTING ONLY");
        badge.addClassName("dev-badge");

        Button asResident = new Button("Test as Resident", e -> demoLogin(false));
        asResident.addClassName("dev-button");
        Button asAdmin = new Button("Test as Admin", e -> demoLogin(true));
        asAdmin.addClassName("dev-button");

        Span hint = new Span("Or type " + SessionController.DEMO_RESIDENT_EMAIL + " / "
                + SessionController.DEMO_ADMIN_EMAIL + " with the demo password.");
        hint.addClassName("dev-status");

        Div bar = new Div(badge, asResident, asAdmin, hint);
        bar.addClassNames("dev-bar", "login-dev-bar");
        return bar;
    }

    // ---------- Actions ----------

    private void attemptLogin() {
        if (email.isEmpty() || email.isInvalid() || password.isEmpty()) {
            Notification.show("Enter your email and password.");
            return;
        }

        Optional<User> user;
        try {
            user = auth.authenticate(email.getValue(), password.getValue());
        } catch (RuntimeException ex) {
            log.error("Login failed due to a server error", ex);
            Notification.show("Login is unavailable right now. Please try again.");
            return;
        }

        if (user.isEmpty()) {
            password.clear();
            Notification.show("Incorrect email or password.");
            return;
        }

        session.login(user.get());
        enterApp();
    }

    private void demoLogin(boolean admin) {
        try {
            if (admin) {
                session.continueAsAdmin();
            } else {
                session.continueAsNonAdmin();
            }
            enterApp();
        } catch (IllegalStateException ex) {
            Notification.show(ex.getMessage());
        }
    }

    private void enterApp() {
        if (session.isAdmin()) {
            UI.getCurrent().navigate(AdminReportsView.class);
        } else {
            UI.getCurrent().navigate(UserReportsView.class);
        }
    }
}