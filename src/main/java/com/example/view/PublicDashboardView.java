package com.example.view;

import java.util.EnumMap;
import java.util.Map;

import com.example.controller.ReportController;
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
@Route("")
@PageTitle("AyosBarangay")
public class PublicDashboardView extends Div {

    private final ReportController controller;

    private ReportStatus activeFilter = null; // null means "All"
    private SortOrder sortOrder = SortOrder.NEWEST;

    private final Div listContainer = new Div();
    private final Map<ReportStatus, Button> chipByStatus = new EnumMap<>(ReportStatus.class);
    private Button allChip;

    public PublicDashboardView(ReportController controller) {
        this.controller = controller;
        addClassName("dashboard");
        add(buildHeader(), buildPanel(), buildCreateReportButton());
        refreshList();
    }

    // ---------- UI building ----------

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
        // Login isn't built yet. Later, check the session and open the Create Report dialog.
        create.addClickListener(e -> Notification.show("Please log in to submit a report."));
        return create;
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

            row.addClickListener(e -> new ComplaintDialog(r).open());
            listContainer.add(row);
        });
    }
}