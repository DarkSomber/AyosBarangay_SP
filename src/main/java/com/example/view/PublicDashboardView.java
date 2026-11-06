package com.example.view;

import java.util.EnumMap;
import java.util.Map;
import java.util.List;

import com.example.controller.NotificationController;
import com.example.controller.ReportController;
import com.example.controller.ReportNotifier;
import com.example.controller.SessionController;
import com.example.model.ReportStatus;
import com.example.model.SortOrder;
import com.example.view.Login.LoginView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;


@Route("")
@PageTitle("AyosBarangay")
public class PublicDashboardView extends Div {

    private final ReportController controller;
    private final SessionController session;
    private final NavBar navBar;
    private final NotificationController notifications;
    private final ReportNotifier notifier;
    private NotificationBell bell;

    private ReportStatus activeFilter = null;
    private SortOrder sortOrder = SortOrder.NEWEST;
    private boolean showOnlyMyReports = false;

    private final Div listContainer = new Div();
    private final Map<ReportStatus, Button> chipByStatus = new EnumMap<>(ReportStatus.class);
    private Button allChip;
    private Button scopeToggle;

    public PublicDashboardView(ReportController controller, SessionController session,
                               NotificationController notifications, ReportNotifier notifier) {
        this.controller = controller;
        this.session = session;
        this.notifications = notifications;
        this.notifier = notifier;
        this.navBar = new NavBar(session);

        addClassName("admin-shell");
        NavBar.restore(this);
        add(NavBar.menuButton(() -> NavBar.toggle(this)), navBar, buildMain());
        navBar.highlight("");
        refreshList();
    }

    private Div buildMain() {
        Div main = new Div(buildHeader(), buildPanel(), buildCreateReportButton());
        main.addClassNames("dashboard", "admin-main");
        return main;
    }

    private Div buildHeader() {
        Image logo = BrandLogo.create("brand-logo");

        H3 title = new H3("AyosBarangay");
        title.addClassName("dashboard-title");

        Div brand = new Div(logo, title);
        brand.addClassName("brand-lockup");

        Div header = new Div(brand);
        header.addClassNames("dashboard-header", "user-header");
        session.getCurrentUser().ifPresent(u -> {
            bell = new NotificationBell(u.isAdmin() ? notifications.adminSource(u) : notifications.userSource(u));
            header.add(bell);
        });
        return header;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (bell == null) {
            return;
        }
        UI ui = attachEvent.getUI();
        Registration newReports = notifier.register(alert -> ui.access(bell::refresh));
        Registration changes = notifier.registerStatus(alert -> ui.access(bell::refresh));
        addDetachListener(e -> {
            newReports.remove();
            changes.remove();
        });
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
        create.setVisible(!session.isAdmin());
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
            ReportRow row = new ReportRow(r, images.getOrDefault(r.reportId(), List.of()), controller);
            row.addClickListener(e -> new ComplaintDialog(r, controller).open());
            listContainer.add(row);
        });
    }
}