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

/**
 * The one sidebar used by every screen (public, user and admin), so the menu
 * works the same everywhere. It is the only place with a login or logout button.
 * Its items depend on who is logged in:
 *
 *   guest : Public Dashboard                                   | Log in
 *   user  : Public Dashboard, Settings, View Submitted Reports | Logout
 *   admin : Public Dashboard, Settings, Unresolved Reports,
 *           AI Summary                                         | Logout
 *
 * The page that owns the NavBar calls highlight(path) to mark the current item,
 * and rebuild() if the login state changes while the page stays open.
 * The menu starts closed on every screen, and restore(shell) keeps it open or
 * closed as you move between screens. Styling reuses the admin-* classes in styles.css.
 */
public class NavBar extends Div {

    private static final String COLLAPSED_KEY = "ayos.navCollapsed";

    private final SessionController session;
    private final Map<String, Button> navButtons = new LinkedHashMap<>();

    public NavBar(SessionController session) {
        this.session = session;
        addClassName("admin-sidebar");
        rebuild();
    }

    /** Rebuilds the items for the current login state. */
    public void rebuild() {
        removeAll();
        navButtons.clear();

        // Empty space where the circle menu button floats above the sidebar.
        Div menuSpace = new Div();
        menuSpace.addClassName("user-menu-space");

        Div nav = new Div();
        nav.addClassName("admin-nav");
        addNavButton(nav, "Public Dashboard", "", PublicDashboardView.class);

        if (session.isAdmin()) {
            addNavButton(nav, "Settings", "admin/settings", AdminSettingsView.class);
            addNavButton(nav, "Unresolved Reports", "admin/reports", AdminReportsView.class);
            addNavButton(nav, "AI Summary", "admin/summary", AdminSummaryView.class);
        } else if (session.isLoggedIn()) {
            addNavButton(nav, "Settings", "user/settings", UserSettingsView.class);
            addNavButton(nav, "View Submitted Reports", "user/reports", UserReportsView.class);
        }

        add(menuSpace, nav, bottomButton());
    }

    /** Marks the item whose route matches the given path ("" is the public dashboard). */
    public void highlight(String path) {
        navButtons.forEach((p, button) -> button.setClassName("active", p.equals(path)));
    }

    // ---------- Helpers shared by the pages that own a NavBar ----------

    /** The circle in the top-left corner. Position is set in styles.css. */
    public static Button menuButton(Runnable onClick) {
        Button menu = new Button(VaadinIcon.MENU.create(), e -> onClick.run());
        menu.addClassNames("menu-button", "user-menu-button");
        menu.getElement().setAttribute("aria-label", "Open or close menu");
        return menu;
    }

    /** Applies the remembered menu state to a page shell. Closed until the person opens it. */
    public static void restore(Div shell) {
        VaadinSession vaadin = VaadinSession.getCurrent();
        Object saved = vaadin == null ? null : vaadin.getAttribute(COLLAPSED_KEY);
        shell.setClassName("collapsed", !Boolean.FALSE.equals(saved));
    }

    /** Shows or hides the sidebar of the given page shell and remembers the choice. */
    public static void toggle(Div shell) {
        boolean collapsed = !shell.getClassNames().contains("collapsed");
        shell.setClassName("collapsed", collapsed);
        VaadinSession vaadin = VaadinSession.getCurrent();
        if (vaadin != null) {
            vaadin.setAttribute(COLLAPSED_KEY, collapsed);
        }
    }

    // ---------- Building ----------

    private void addNavButton(Div nav, String label, String path, Class<? extends Component> target) {
        Button button = new Button(label, e -> UI.getCurrent().navigate(target));
        button.addClassName("admin-nav-button");
        navButtons.put(path, button);
        nav.add(button);
    }

    /** Log in for guests. Logout for residents and admins: ends the session, then opens the login screen. */
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