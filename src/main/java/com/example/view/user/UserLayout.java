package com.example.view.user;

import com.example.controller.SessionController;
import com.example.view.NavBar;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;

public class UserLayout extends Div implements RouterLayout, AfterNavigationObserver {

    private final NavBar navBar;
    private final Div contentArea = new Div();

    public UserLayout(SessionController session) {
        this.navBar = new NavBar(session);
        addClassName("admin-shell");
        NavBar.restore(this);
        add(NavBar.menuButton(() -> NavBar.toggle(this)), navBar, buildMain());
    }

    private Div buildMain() {
        H3 title = new H3("AyosBarangay");
        title.addClassName("dashboard-title");

        Div header = new Div(title);
        header.addClassNames("dashboard-header", "user-header");

        contentArea.addClassName("admin-content");

        Div main = new Div(header, contentArea);
        main.addClassName("admin-main");
        return main;
    }

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
        navBar.highlight(event.getLocation().getPath());
    }
}