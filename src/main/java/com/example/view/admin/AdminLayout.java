package com.example.view.admin;

import java.util.LinkedHashMap;
import java.util.Map;

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
 * Shared frame for every admin screen: the sidebar (Settings, Unresolved Reports,
 * AI Summary), the hamburger button that shows or hides it, and the content area
 * where each admin view is displayed. Pure display, no logic.
 */
public class AdminLayout extends Div implements RouterLayout, AfterNavigationObserver {

    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final Div contentArea = new Div();

    public AdminLayout() {
        addClassName("admin-shell");
        add(buildSidebar(), buildMain());
    }

    // ---------- UI building ----------

    private Div buildSidebar() {
        Div avatar = new Div();
        avatar.addClassName("admin-avatar");

        Div nav = new Div();
        nav.addClassName("admin-nav");
        addNavButton(nav, "Settings", "admin/settings", AdminSettingsView.class);
        addNavButton(nav, "Unresolved Reports", "admin/reports", AdminReportsView.class);
        addNavButton(nav, "AI Summary", "admin/summary", AdminSummaryView.class);

        // TODO: when the login screen exists, navigate to it instead of the public dashboard.
        Button userLogin = new Button("User? Log in here", e -> UI.getCurrent().navigate(""));
        userLogin.addClassName("admin-login-button");

        Div sidebar = new Div(avatar, nav, userLogin);
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
        Button menu = new Button(VaadinIcon.MENU.create(), e -> toggleSidebar());
        menu.addClassName("menu-button");
        menu.getElement().setAttribute("aria-label", "Show or hide menu");

        H3 title = new H3("AyosBarangay");
        title.addClassName("dashboard-title");

        // Reuses the same header styling as the public dashboard.
        Div header = new Div(menu, title);
        header.addClassName("dashboard-header");

        contentArea.addClassName("admin-content");

        Div main = new Div(header, contentArea);
        main.addClassName("admin-main");
        return main;
    }

    private void toggleSidebar() {
        setClassName("collapsed", !getClassNames().contains("collapsed"));
    }

    // ---------- Router hooks ----------

    /** Puts the current admin view inside the content area. */
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