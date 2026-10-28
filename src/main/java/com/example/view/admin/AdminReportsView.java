package com.example.view.admin;

import java.util.EnumMap;
import java.util.Map;
import java.util.List;

import com.example.ai.ReportAnalysisService;
import com.example.controller.ReportController;
import com.example.duplicate.DuplicateReportService;
import com.example.media.AttachmentCheckService;
import com.example.model.ReportStatus;
import com.example.model.SortOrder;
import com.example.spam.ReportSpamCheckService;
import com.example.view.ComplaintDialog;
import com.example.view.ReportRow;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Admin: reports that still need attention (Pending / Under Review). */
@Route(value = "admin/reports", layout = AdminLayout.class)
@PageTitle("Unresolved Reports | AyosBarangay")
public class AdminReportsView extends Div {

    private final ReportController controller;
    private final ReportAnalysisService analysisService;
    private final DuplicateReportService duplicateReportService;
    private final ReportSpamCheckService spamCheckService;
    private final AttachmentCheckService attachmentCheckService;

    private ReportStatus activeFilter = null; // null means "All" open reports
    private SortOrder sortOrder = SortOrder.NEWEST;

    private final Div listContainer = new Div();
    private final Map<ReportStatus, Button> chipByStatus = new EnumMap<>(ReportStatus.class);
    private Button allChip;

    public AdminReportsView(ReportController controller, ReportAnalysisService analysisService,
                            DuplicateReportService duplicateReportService,
                            ReportSpamCheckService spamCheckService,
                            AttachmentCheckService attachmentCheckService) {
        this.controller = controller;
        this.analysisService = analysisService;
        this.duplicateReportService = duplicateReportService;
        this.spamCheckService = spamCheckService;
        this.attachmentCheckService = attachmentCheckService;
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
        // Only offer chips for statuses that can appear on this page.
        for (ReportStatus s : ReportController.OPEN_STATUSES) {
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
        var reports = controller.getOpenReports(activeFilter, sortOrder);

        if (reports.isEmpty()) {
            Span empty = new Span("No unresolved reports.");
            empty.addClassName("report-empty");
            listContainer.add(empty);
            return;
        }

        var images = controller.getOriginalImages(reports);
        reports.forEach(r -> {
            ReportRow row = new ReportRow(r, images.getOrDefault(r.reportId(), List.of()));
            // TODO: swap in an admin version of the dialog that can change the report status.
            row.addClickListener(e -> new ComplaintDialog(
                    r, controller, analysisService, duplicateReportService,
                    spamCheckService, attachmentCheckService).open());
            listContainer.add(row);
        });
    }
}