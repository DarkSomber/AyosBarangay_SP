package com.example.view;

import com.example.controller.SessionController;
import com.example.view.Login.LoginView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.html.Image;

/**
 * User Emergency panel: quick-call buttons for emergency services, the barangay
 * and the NCMH crisis hotline. View only for now. Every Call button goes through
 * call(...), which is where the controller hook will go later.
 * Uses the same frame (admin-shell + UserNavBar) as the other user screens.
 */
@Route("user/emergency")
@PageTitle("Emergency | AyosBarangay")
public class UserEmergencyView extends Div implements BeforeEnterObserver {

    // TODO (controller): load these from configuration or the barangay record.
    // null = number not set yet.
    private static final String POLICE_NUMBER = null;
    private static final String HOTLINE_NUMBER = "911";
    private static final String RED_CROSS_NUMBER = null;
    private static final String BARANGAY_NUMBER = null;
    private static final String NCMH_NUMBER = "0917-899-8727";

    private final SessionController session;
    private final NavBar navBar;

    public UserEmergencyView(SessionController session) {
        this.session = session;
        this.navBar = new NavBar(session);

        // Same frame as the other user screens, with the same remembered menu state.
        addClassName("admin-shell");
        NavBar.restore(this);
        add(NavBar.menuButton(() -> NavBar.toggle(this)), navBar, buildMain());
        navBar.highlight("user/emergency");
    }

    /** Guests have no sidebar entry for this page, so send them to the login screen. */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!session.isLoggedIn()) {
            event.forwardTo(LoginView.class);
        }
    }

    // ---------- UI building ----------

    private Div buildMain() {
        Div main = new Div(buildHeader(), buildPanel());
        main.addClassNames("dashboard", "admin-main");
        return main;
    }

    private Div buildHeader() {
        H3 title = new H3("AyosBarangay");
        title.addClassName("dashboard-title");

        Div header = new Div(title);
        header.addClassNames("dashboard-header", "user-header");
        return header;
    }

    private Div buildPanel() {
        Div panel = new Div(buildEmergencyCard(), buildBottomRow());
        panel.addClassNames("dashboard-panel", "emergency-panel");
        return panel;
    }

    /** Top card: police, national hotline, Red Cross. */
    private Div buildEmergencyCard() {
        H3 title = new H3("CALL FOR EMERGENCIES");
        title.addClassName("emergency-title");

        // Placeholder artwork is drawn in CSS. Swap for real images later if wanted.
        Image police = new Image("ImgIcons/map_police.png", "Philippine National Police");
        police.addClassNames("emergency-icon", "emergency-icon-light");

        Image redCross = new Image("ImgIcons/mdi_hospital-box.png", "Philippine Red Cross");
        redCross.addClassNames("emergency-icon", "emergency-icon-light");

        Span hotlineText = new Span("911");
        hotlineText.addClassName("emergency-hotline-text");

        Div circles = new Div(
                contact(police, "Philippine National Police", POLICE_NUMBER, false),
                contact(hotlineText, "National Hotline", HOTLINE_NUMBER, true),
                contact(redCross, "Philippine Red Cross", RED_CROSS_NUMBER, false));
        circles.addClassName("emergency-circles");

        Div card = new Div(title, circles);
        card.addClassNames("emergency-card", "emergency-top");
        return card;
    }

    /** One big circle button with a caption under it. */
    private Div contact(Component icon, String name, String number, boolean red) {
        Button circle = new Button(icon, e -> call(name, number));
        circle.addClassName("emergency-circle");
        circle.setClassName("hotline", red);
        circle.getElement().setAttribute("aria-label", "Call " + name);

        Span caption = new Span(name);
        caption.addClassName("emergency-caption");

        Div wrapper = new Div(circle, caption);
        wrapper.addClassName("emergency-contact");
        return wrapper;
    }

    /** Bottom row: Call the Barangay, NCMH Crisis Hotline. */
    private Div buildBottomRow() {
        Div row = new Div(buildBarangayCard(), buildNcmhCard());
        row.addClassName("emergency-bottom");
        return row;
    }

    private Div buildBarangayCard() {
        H3 title = new H3("Call the Barangay");
        title.addClassName("emergency-card-title");

        Button call = new Button("Call", e -> call("the Barangay", BARANGAY_NUMBER));
        call.addClassName("emergency-call-button");

        Div card = new Div(title, call);
        card.addClassNames("emergency-card", "emergency-barangay");
        return card;
    }

    private Div buildNcmhCard() {
        Span name = new Span("NCMH Crisis Hotline");
        name.addClassName("emergency-ncmh-name");
        Span number = new Span(NCMH_NUMBER);
        number.addClassName("emergency-ncmh-number");

        Div text = new Div(name, number);
        text.addClassName("emergency-ncmh-text");

        Button call = new Button("Call", e -> call("NCMH Crisis Hotline", NCMH_NUMBER));
        call.addClassName("emergency-call-circle");

        Div card = new Div(text, call);
        card.addClassNames("emergency-card", "emergency-ncmh");
        return card;
    }

    // ---------- Actions ----------

    /** Placeholder: the controller will do the real dialing later. */
    private void call(String name, String number) {
        if (number == null) {
            Notification.show("No number set yet for " + name + ".");
        } else {
            Notification.show("Calling " + name + " at " + number + "...");
        }
    }
}