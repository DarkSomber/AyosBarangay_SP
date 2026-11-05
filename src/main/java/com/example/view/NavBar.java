package com.example.view;

import java.util.LinkedHashMap;
import java.util.Map;

import com.example.controller.SessionController;
import com.example.view.Login.LoginView;
import com.example.view.admin.AdminReportsView;
import com.example.view.admin.AdminSettingsView;
import com.example.view.admin.AdminSummaryView;
import com.example.view.user.UserReportsView;
import com.example.view.user.UserSettingsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.VaadinSession;

public class NavBar extends Div {

    private static final String COLLAPSED_KEY = "ayos.navCollapsed";

    private final SessionController session;
    private final Map<String, Button> navButtons = new LinkedHashMap<>();

    public NavBar(SessionController session) {
        this.session = session;
        addClassName("admin-sidebar");
        rebuild();
    }

    public void rebuild() {
        removeAll();
        navButtons.clear();

        Div menuSpace = new Div();
        menuSpace.addClassName("user-menu-space");

        Div nav = new Div();
        nav.addClassName("admin-nav");
        addNavButton(nav, "Public Dashboard", "", PublicDashboardView.class);

        if (session.isAdmin()) {
            addNavButton(nav, "Settings", "admin/settings", AdminSettingsView.class);
            addNavButton(nav, "Reports", "admin/reports", AdminReportsView.class);
            addNavButton(nav, "AI Summary", "admin/summary", AdminSummaryView.class);

        } else if (session.isLoggedIn()) {
            addNavButton(nav, "Settings", "user/settings", UserSettingsView.class);
            addNavButton(nav, "My Reports", "user/reports", UserReportsView.class);
            addNavButton(nav, "Emergency", "user/emergency", UserEmergencyView.class);
        }

        add(menuSpace, nav, bottomButton());
    }

    public void highlight(String path) {
        navButtons.forEach((p, button) -> button.setClassName("active", p.equals(path)));
    }


    public static Button menuButton(Runnable onClick) {
        Button menu = new Button(VaadinIcon.MENU.create(), e -> onClick.run());
        menu.addClassNames("menu-button", "user-menu-button");
        menu.getElement().setAttribute("aria-label", "Open or close menu");
        return menu;
    }

    public static void restore(Div shell) {
        VaadinSession vaadin = VaadinSession.getCurrent();
        Object saved = vaadin == null ? null : vaadin.getAttribute(COLLAPSED_KEY);
        shell.setClassName("collapsed", !Boolean.FALSE.equals(saved));
    }

    public static void toggle(Div shell) {
        boolean collapsed = !shell.getClassNames().contains("collapsed");
        shell.setClassName("collapsed", collapsed);
        VaadinSession vaadin = VaadinSession.getCurrent();
        if (vaadin != null) {
            vaadin.setAttribute(COLLAPSED_KEY, collapsed);
        }
    }


    private void addNavButton(Div nav, String label, String path, Class<? extends Component> target) {
        Button button = new Button(label, e -> UI.getCurrent().navigate(target));
        button.addClassName("admin-nav-button");
        navButtons.put(path, button);
        nav.add(button);
    }

    private Button bottomButton() {
        boolean loggedIn = session.isLoggedIn();
        Button button = new Button(loggedIn ? "Logout" : "Log in", e -> {
            if (loggedIn) {
                session.reset();
            }
            UI.getCurrent().navigate(LoginView.class);
        });
        button.addClassName("admin-login-button");
        return button;
    }
}