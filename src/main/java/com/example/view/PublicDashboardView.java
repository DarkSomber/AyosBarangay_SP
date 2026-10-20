package com.example.view;

import java.util.EnumMap;
import java.util.Map;

import com.example.controller.ReportController;
import com.example.controller.SessionController;
import com.example.model.ReportStatus;
import com.example.model.SortOrder;
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
 * Public Dashboard (default screen). The view builds the UI and forwards
 * user actions to the controller. Styling lives in styles.css.
 */
@Route("dashboard")
@PageTitle("AyosBarangay")
public class PublicDashboardView extends Div {

    private final ReportController controller;
    private final SessionController session;

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
        addClassName("dashboard");
        add(buildHeader(), buildDevBar(), buildPanel(), buildCreateReportButton());
        refreshList();
    }

    private Div buildHeader() {
        Button menu = new Button(VaadinIcon.MENU.create());
        menu.addClassName("menu-button");
        menu.getElement().setAttribute("aria-label", "Open menu");
        menu.addClickListener(e -> Notification.show("Navbar coming soon"));

        H3 title = new H3("AyosBarangay");
        title.addClassName("dashboard-title");

        Div header = new Div(menu, title);
        header.addClassName("dashboard-header");
        return header;
    }

    private Div buildDevBar() {
        devBar.addClassName("dev-bar");
        refreshDevBar();
        return devBar;
    }

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
            Span who = new Span("Testing as " + name + " (non-admin)");
            who.addClassName("dev-status");
            Button reset = new Button("Logout / Reset test session", e -> {
                session.reset();
                refreshDevBar();
                refreshList();
                Notification.show("Test session cleared.");
            });
            reset.addClassName("dev-button");
            devBar.add(who, reset);
        } else {
            Button start = new Button("Continue as Non-Admin", e -> {
                session.continueAsNonAdmin();
                refreshDevBar();
                refreshList();
                Notification.show("Testing as " + SessionController.DEMO_NAME + " (non-admin).");
            });
            start.addClassName("dev-button");
            devBar.add(start);
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
                return;
            }
            var user = session.getCurrentUser().orElse(null);
            if (user == null) return;

            if (user.userId() == null) {
                Notification.show("This testing account cannot submit reports (no user id).");
                return;
            }

            new CreateReportDialog(controller, user.userId(), () -> {
                showOnlyMyReports = true;
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

        reports.forEach(r -> {
            ReportRow row = new ReportRow(r);
            row.addClickListener(e -> new ComplaintDialog(r, controller).open());
            listContainer.add(row);
        });
    }
}