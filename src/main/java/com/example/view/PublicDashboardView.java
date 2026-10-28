package com.example.view;

import java.util.EnumMap;
import java.util.Map;
import java.util.List;

import com.example.controller.ReportController;
import com.example.controller.SessionController;
import com.example.model.ReportStatus;
import com.example.model.SortOrder;
import com.example.view.Login.LoginView;
import com.example.view.admin.AdminReportsView;
import com.example.view.user.UserReportsView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


/**
 * Public Dashboard: the start page of the app (route ""). Anyone can view
 * reports; submitting one needs a logged-in resident. It uses the same NavBar
 * and frame as the user and admin panels, which is also where Log in and
 * Logout live. The view builds the UI and forwards user actions to the controller.
 */
@Route("")
@PageTitle("AyosBarangay")
public class PublicDashboardView extends Div {

    private final ReportController controller;
    private final SessionController session;
    private final NavBar navBar;

    private ReportStatus activeFilter = null;
    private SortOrder sortOrder = SortOrder.NEWEST;
    private boolean showOnlyMyReports = false;

    private final Div listContainer = new Div();
    private final Div devBar = new Div();
    private final Map<ReportStatus, Button> chipByStatus = new EnumMap<>(ReportStatus.class);
    private Button allChip;
    private Button scopeToggle;

    public PublicDashboardView(ReportController controller, SessionController session) {
        this.controller = controller;
        this.session = session;
        this.navBar = new NavBar(session);

        // Same frame as the user/admin panels, with the same remembered menu state.
        addClassName("admin-shell");
        NavBar.restore(this);
        add(NavBar.menuButton(() -> NavBar.toggle(this)), navBar, buildMain());
        navBar.highlight("");
        refreshList();
    }

    private Div buildMain() {
        Div main = new Div(buildHeader(), buildDevBar(), buildPanel(), buildCreateReportButton());
        main.addClassNames("dashboard", "admin-main");
        return main;
    }

    private Div buildHeader() {
        // Placeholder circle. Later: swap for new Image("images/logo.png", "AyosBarangay")
        // with the same "brand-logo" class, or set background-image on .brand-logo in CSS.
        Div logo = new Div();
        logo.addClassNames("brand-logo", "brand-logo-placeholder");

        H3 title = new H3("AyosBarangay");
        title.addClassName("dashboard-title");

        Div brand = new Div(logo, title);
        brand.addClassName("brand-lockup");

        Div header = new Div(brand);
        header.addClassNames("dashboard-header", "user-header");
        return header;
    }

    private void goToPanel() {
        if (session.isAdmin()) {
            UI.getCurrent().navigate(AdminReportsView.class);
        } else {
            UI.getCurrent().navigate(UserReportsView.class);
        }
    }

    private Div buildDevBar() {
        devBar.addClassName("dev-bar");
        refreshDevBar();
        return devBar;
    }

    /** DEV / TESTING ONLY. One-click test logins that jump straight into each panel. */
    private void refreshDevBar() {
        devBar.removeAll();
        devBar.setVisible(session.isDemoLoginEnabled());
        if (!session.isDemoLoginEnabled()) {
            return;
        }

        Span badge = new Span("DEV / TESTING ONLY");
        badge.addClassName("dev-badge");
        devBar.add(badge);

        if (session.isLoggedIn()) {
            String name = session.getCurrentUser().map(u -> u.fullName()).orElse("?");
            String role = session.isAdmin() ? "admin" : "non-admin";
            Span who = new Span("Testing as " + name + " (" + role + ")");
            who.addClassName("dev-status");

            Button open = new Button("Open my panel", e -> goToPanel());
            open.addClassName("dev-button");
            devBar.add(who, open);
        } else {
            Button asResident = new Button("Test as Resident", e -> demoLogin(false));
            asResident.addClassName("dev-button");
            Button asAdmin = new Button("Test as Admin", e -> demoLogin(true));
            asAdmin.addClassName("dev-button");
            devBar.add(asResident, asAdmin);
        }
    }

    private void demoLogin(boolean admin) {
        try {
            if (admin) {
                session.continueAsAdmin();
            } else {
                session.continueAsNonAdmin();
            }
            goToPanel();
        } catch (IllegalStateException ex) {
            Notification.show(ex.getMessage());
        }
    }

    private Div buildPanel() {
        listContainer.addClassName("report-list");
        Div panel = new Div(buildFilterBar(), listContainer);
        panel.addClassName("dashboard-panel");
        return panel;
    }

    private Div buildFilterBar() {
        Div filterBar = new Div();
        filterBar.addClassName("filter-bar");

        scopeToggle = new Button("My Reports");
        scopeToggle.addClassName("filter-chip");
        scopeToggle.setVisible(session.isLoggedIn());
        scopeToggle.addClickListener(e -> {
            showOnlyMyReports = !showOnlyMyReports;
            updateChips();
            refreshList();
        });
        filterBar.add(scopeToggle);

        allChip = makeChip("All", null);
        filterBar.add(allChip);
        for (ReportStatus s : ReportStatus.values()) {
            Button chip = makeChip(s.getLabel(), s);
            chipByStatus.put(s, chip);
            filterBar.add(chip);
        }

        Select<SortOrder> sort = new Select<>();
        sort.addClassName("sort-select");
        sort.setItems(SortOrder.values());
        sort.setItemLabelGenerator(SortOrder::getLabel);
        sort.setValue(sortOrder);
        sort.setAriaLabel("Sort reports");
        sort.addValueChangeListener(e -> {
            sortOrder = e.getValue();
            refreshList();
        });
        filterBar.add(sort);

        updateChips();
        return filterBar;
    }

    private Button makeChip(String text, ReportStatus status) {
        Button chip = new Button(text, e -> {
            activeFilter = status;
            updateChips();
            refreshList();
        });
        chip.addClassName("filter-chip");
        return chip;
    }

    private void updateChips() {
        allChip.setClassName("active", activeFilter == null);
        chipByStatus.forEach((s, chip) -> chip.setClassName("active", activeFilter == s));

        if (scopeToggle != null) {
            scopeToggle.setVisible(session.isLoggedIn());
            scopeToggle.setClassName("active", showOnlyMyReports);
        }
    }

    private Button buildCreateReportButton() {
        Button create = new Button("Create Report", VaadinIcon.PLUS.create());
        create.addClassName("create-report-button");
        create.addClickListener(e -> {
            if (!session.isLoggedIn()) {
                Notification.show("Please log in to submit a report.");
                UI.getCurrent().navigate(LoginView.class);
                return;
            }
            if (session.isAdmin()) {
                Notification.show("Admins can't submit reports. Log in as a resident.");
                return;
            }
            var user = session.getCurrentUser().orElse(null);
            if (user == null || user.userId() == null) return;

            new CreateReportDialog(controller, user.userId(), () -> {
                showOnlyMyReports = true;
                updateChips();
                refreshList();
            }).open();
        });
        return create;
    }

    private void refreshList() {
        listContainer.removeAll();

        var reports = controller.getReports(activeFilter, sortOrder);
        if (showOnlyMyReports && session.isLoggedIn()) {
            var currentUser = session.getCurrentUser().orElse(null);
            if (currentUser != null && currentUser.userId() != null) {
                reports = controller.getReportsForUser(currentUser.userId(), activeFilter, sortOrder);
            }
        }

        if (reports.isEmpty()) {
            String msg = showOnlyMyReports ? "You haven't submitted any reports yet." : "No reports found.";
            Span empty = new Span(msg);
            empty.addClassName("report-empty");
            listContainer.add(empty);
            return;
        }

        var images = controller.getOriginalImages(reports);
        reports.forEach(r -> {
            ReportRow row = new ReportRow(r, images.getOrDefault(r.reportId(), List.of()));
            row.addClickListener(e -> new ComplaintDialog(r, controller).open());
            listContainer.add(row);
        });
    }
}