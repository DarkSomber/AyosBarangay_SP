package com.example.view.user;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.example.controller.ReportController;
import com.example.controller.SessionController;
import com.example.model.Report;
import com.example.model.ReportStatus;
import com.example.model.SortOrder;
import com.example.view.ComplaintDialog;
import com.example.view.CreateReportDialog;
import com.example.view.ReportRow;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * User: View Submitted Reports. Shows only the reports submitted by the logged-in
 * user (filter chips, sort, report rows) plus the Create Report button.
 * The view builds the UI and forwards actions to the controller.
 */
@Route(value = "user/reports", layout = UserLayout.class)
@PageTitle("My Reports | AyosBarangay")
public class UserReportsView extends Div {

    private final ReportController controller;
    private final SessionController session;

    private ReportStatus activeFilter = null; // null means "All"
    private SortOrder sortOrder = SortOrder.NEWEST;

    private final Div listContainer = new Div();
    private final Map<ReportStatus, Button> chipByStatus = new EnumMap<>(ReportStatus.class);
    private Button allChip;

    public UserReportsView(ReportController controller, SessionController session) {
        this.controller = controller;
        this.session = session;
        addClassName("admin-page");
        add(buildPanel(), buildCreateReportButton());
        refreshList();
    }

    // ---------- UI building ----------

    private Div buildPanel() {
        listContainer.addClassName("report-list");
        Div panel = new Div(buildFilterBar(), listContainer);
        panel.addClassName("dashboard-panel");
        return panel;
    }

    private Div buildFilterBar() {
        Div filterBar = new Div();
        filterBar.addClassName("filter-bar");

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
    }

    private Button buildCreateReportButton() {
        Button create = new Button("Create Report", VaadinIcon.PLUS.create());
        create.addClassName("create-report-button");
        create.addClickListener(e -> session.getCurrentUser().ifPresent(user ->
                new CreateReportDialog(controller, user.userId(), this::refreshList).open()));
        return create;
    }

    // ---------- Rendering ----------

    private void refreshList() {
        listContainer.removeAll();

        var me = session.getCurrentUser();
        List<Report> reports = me.isPresent()
                ? controller.getReportsForUser(me.get().userId(), activeFilter, sortOrder)
                : List.of();

        if (reports.isEmpty()) {
            Span empty = new Span("You haven't submitted any reports yet.");
            empty.addClassName("report-empty");
            listContainer.add(empty);
            return;
        }

        var images = controller.getOriginalImages(reports);
        reports.forEach(r -> {
            ReportRow row = new ReportRow(r, images.getOrDefault(r.reportId(), List.of()));
            row.addClickListener(e -> {
                ComplaintDialog dialog = new ComplaintDialog(r, controller);
                me.ifPresent(u -> dialog.setResident(u, this::refreshList));
                dialog.open();
            });
            listContainer.add(row);
        });
    }
}