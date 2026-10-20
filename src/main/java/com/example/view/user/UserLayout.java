package com.example.view.user;

import java.util.LinkedHashMap;
import java.util.Map;

import com.example.view.Login.LoginView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;

/**
 * Shared frame for every user screen: the sidebar (Settings, View Submitted Reports,
 * Logout), the circle button in the top-left corner that opens and closes it, and the
 * content area where each user view is displayed. Pure display, no logic.
 * Reuses the admin-* CSS classes, since the frame looks the same as the admin one.
 */
public class UserLayout extends Div implements RouterLayout, AfterNavigationObserver {

    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final Div contentArea = new Div();

    public UserLayout() {
        addClassName("admin-shell");
        add(buildMenuButton(), buildSidebar(), buildMain());
    }

    // ---------- UI building ----------

    /**
     * The circle in the top-left corner. It stays in place whether the sidebar is
     * open or closed (position is set in styles.css), so it can always reopen it.
     */
    private Button buildMenuButton() {
        Button menu = new Button(VaadinIcon.MENU.create(), e -> toggleSidebar());
        menu.addClassNames("menu-button", "user-menu-button");
        menu.getElement().setAttribute("aria-label", "Open or close menu");
        return menu;
    }

    private Div buildSidebar() {
        // Empty space where the menu button floats above the sidebar.
        Div menuSpace = new Div();
        menuSpace.addClassName("user-menu-space");

        Div nav = new Div();
        nav.addClassName("admin-nav");
        addNavButton(nav, "Settings", "user/settings", UserSettingsView.class);
        addNavButton(nav, "View Submitted Reports", "user/reports", UserReportsView.class);

        // TODO: when login exists, end the user session here before leaving.
        Button logout = new Button("Logout", e -> UI.getCurrent().navigate(LoginView.class));
        logout.addClassName("sidebar-logout");

        Div sidebar = new Div(menuSpace, nav, logout);
        sidebar.addClassName("admin-sidebar");
        return sidebar;
    }

    private void addNavButton(Div nav, String label, String path, Class<? extends Component> target) {
        Button button = new Button(label, e -> UI.getCurrent().navigate(target));
        button.addClassName("admin-nav-button");
        navButtons.put(path, button);
        nav.add(button);
    }

    private Div buildMain() {
        H3 title = new H3("AyosBarangay");
        title.addClassName("dashboard-title");

        // Same header styling as the public dashboard; the title sits on the right.
        Div header = new Div(title);
        header.addClassNames("dashboard-header", "user-header");

        contentArea.addClassName("admin-content");

        Div main = new Div(header, contentArea);
        main.addClassName("admin-main");
        return main;
    }

    private void toggleSidebar() {
        setClassName("collapsed", !getClassNames().contains("collapsed"));
    }

    // ---------- Router hooks ----------

    /** Puts the current user view inside the content area. */
    @Override
    public void showRouterLayoutContent(HasElement content) {
        if (content != null) {
            contentArea.getElement().appendChild(content.getElement());
        }
    }

    /** Highlights the sidebar button that matches the current page. */
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String path = event.getLocation().getPath();
        navButtons.forEach((p, button) -> button.setClassName("active", p.equals(path)));
    }
}