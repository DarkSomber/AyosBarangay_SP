package com.example.view.user;

import java.util.EnumMap;
import java.util.Map;

import com.example.controller.ReportController;
import com.example.model.ReportStatus;
import com.example.model.SortOrder;
import com.example.view.ComplaintDialog;
import com.example.view.ReportRow;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * User: View Submitted Reports. Same list as the public dashboard (filter chips,
 * sort, report rows) shown inside the user layout, plus the Create Report button.
 * The view builds the UI and forwards actions to the controller.
 * Styling lives in styles.css.
 */
@Route(value = "user/reports", layout = UserLayout.class)
@PageTitle("My Reports | AyosBarangay")
public class UserReportsView extends Div {

    private final ReportController controller;

    private ReportStatus activeFilter = null; // null means "All"
    private SortOrder sortOrder = SortOrder.NEWEST;

    private final Div listContainer = new Div();
    private final Map<ReportStatus, Button> chipByStatus = new EnumMap<>(ReportStatus.class);
    private Button allChip;

    public UserReportsView(ReportController controller) {
        this.controller = controller;
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
        // TODO: open the Create Report dialog once it is built.
        create.addClickListener(e -> Notification.show("Create Report is coming soon."));
        return create;
    }

    // ---------- Rendering ----------

    private void refreshList() {
        listContainer.removeAll();
        // TODO: once login exists, only load the reports submitted by the logged-in user.
        var reports = controller.getReports(activeFilter, sortOrder);

        if (reports.isEmpty()) {
            Span empty = new Span("No reports found.");
            empty.addClassName("report-empty");
            listContainer.add(empty);
            return;
        }

        reports.forEach(r -> {
            ReportRow row = new ReportRow(r);
            row.addClickListener(e -> new ComplaintDialog(r).open());
            listContainer.add(row);
        });
    }
}