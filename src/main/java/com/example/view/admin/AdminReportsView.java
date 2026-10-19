package com.example.view.admin;

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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Admin: Unresolved Reports. Same list as the public dashboard (filter chips,
 * sort, report rows) shown inside the admin layout. The view builds the UI and
 * forwards actions to the controller. Styling lives in styles.css.
 */
@Route(value = "admin/reports", layout = AdminLayout.class)
@PageTitle("Unresolved Reports | AyosBarangay")
public class AdminReportsView extends Div {

    private final ReportController controller;

    private ReportStatus activeFilter = null; // null means "All"
    private SortOrder sortOrder = SortOrder.NEWEST;

    private final Div listContainer = new Div();
    private final Map<ReportStatus, Button> chipByStatus = new EnumMap<>(ReportStatus.class);
    private Button allChip;

    public AdminReportsView(ReportController controller) {
        this.controller = controller;
        addClassName("admin-page");
        add(buildPanel());
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

    // ---------- Rendering ----------

    private void refreshList() {
        listContainer.removeAll();
        var reports = controller.getReports(activeFilter, sortOrder);

        if (reports.isEmpty()) {
            Span empty = new Span("No reports found.");
            empty.addClassName("report-empty");
            listContainer.add(empty);
            return;
        }

        reports.forEach(r -> {
            ReportRow row = new ReportRow(r);
            // TODO: swap in an admin version of the dialog that can change the report status.
            row.addClickListener(e -> new ComplaintDialog(r).open());
            listContainer.add(row);
        });
    }
}