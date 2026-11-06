package com.example.view.admin;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.ai.ReportAnalysisService;
import com.example.controller.ReportController;
import com.example.controller.ReportController.ReportItem;
import com.example.controller.SessionController;
import com.example.duplicate.DuplicateReportService;
import com.example.duplicate.ReportClusterService;
import com.example.media.AttachmentCheckService;
import com.example.model.Report;
import com.example.model.ReportPriority;
import com.example.model.ReportStatus;
import com.example.model.User;
import com.example.spam.ReportSpamCheckService;
import com.example.view.ComplaintDialog;
import com.example.view.ReportRow;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "admin/reports", layout = AdminLayout.class)
@PageTitle("Reports | AyosBarangay")
public class AdminReportsView extends Div {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private enum Section {
        OPEN("Open", ReportController.OPEN_STATUSES),
        ARCHIVE("Archive", ReportController.ARCHIVE_STATUSES),
        ALL("All Reports", List.of(ReportStatus.values()));

        final String label;
        final List<ReportStatus> statuses;

        Section(String label, List<ReportStatus> statuses) {
            this.label = label;
            this.statuses = statuses;
        }
    }

    private final ReportController controller;
    private final ReportAnalysisService analysisService;
    private final DuplicateReportService duplicateReportService;
    private final ReportClusterService clusterService;
    private final ReportSpamCheckService spamCheckService;
    private final AttachmentCheckService attachmentCheckService;
    private final SessionController session;

    private Section section = Section.OPEN;
    private ReportStatus activeFilter = null;
    private boolean overdueOnly = false;

    private final Map<Tab, Section> tabs = new LinkedHashMap<>();
    private final Div filterBar = new Div();
    private final TextField search = new TextField();
    private final Grid<ReportItem> grid = new Grid<>();
    private final Checkbox group = new Checkbox("Group similar reports");
    private final Map<Integer, List<ReportItem>> clusters = new HashMap<>();
    private Grid.Column<ReportItem> groupColumn;

    public AdminReportsView(ReportController controller, ReportAnalysisService analysisService,
                            DuplicateReportService duplicateReportService,
                            ReportClusterService clusterService,
                            ReportSpamCheckService spamCheckService,
                            AttachmentCheckService attachmentCheckService,
                            SessionController session) {
        this.session = session;
        this.controller = controller;
        this.analysisService = analysisService;
        this.duplicateReportService = duplicateReportService;
        this.clusterService = clusterService;
        this.spamCheckService = spamCheckService;
        this.attachmentCheckService = attachmentCheckService;
        addClassName("admin-page");
        group.addValueChangeListener(e -> refreshList());

        Div panel = new Div(buildTabs(), filterBar, buildGrid());
        panel.addClassName("dashboard-panel");
        add(panel);

        buildFilterBar();
        refreshList();
    }


    private Tabs buildTabs() {
        Tabs bar = new Tabs();
        for (Section s : Section.values()) {
            Tab tab = new Tab(s.label);
            tabs.put(tab, s);
            bar.add(tab);
        }
        bar.addSelectedChangeListener(e -> {
            section = tabs.get(e.getSelectedTab());
            activeFilter = null;
            overdueOnly = false;
            buildFilterBar();
            refreshList();
        });
        return bar;
    }

    private void buildFilterBar() {
        filterBar.removeAll();
        filterBar.addClassName("filter-bar");

        filterBar.add(makeChip("All", null, false));
        for (ReportStatus s : section.statuses) {
            filterBar.add(makeChip(s.getLabel(), s, false));
        }
        if (section.statuses.contains(ReportStatus.PENDING)) {
            filterBar.add(makeChip("Overdue", null, true));
        }
        filterBar.add(group);

        search.setPlaceholder("Search description, report ID or resident");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.setWidth("320px");
        search.setMaxWidth("100%");
        search.setAriaLabel("Search reports");
        search.addValueChangeListener(e -> refreshList());
        filterBar.add(search);
    }

    private Button makeChip(String text, ReportStatus status, boolean overdue) {
        Button chip = new Button(text, e -> {
            activeFilter = status;
            overdueOnly = overdue;
            filterBar.getChildren().filter(Button.class::isInstance)
                    .forEach(c -> ((Button) c).removeClassName("active"));
            e.getSource().addClassName("active");
            refreshList();
        });
        chip.addClassName("filter-chip");
        chip.setClassName("active", activeFilter == status && overdueOnly == overdue);
        return chip;
    }

    private Grid<ReportItem> buildGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("65vh");
        grid.setEmptyStateText("No reports found.");

        grid.addColumn(i -> "#" + i.report().reportId()).setHeader("ID")
                .setComparator(i -> i.report().reportId()).setWidth("90px").setFlexGrow(0);
        grid.addColumn(ReportItem::residentName).setHeader("Resident")
                .setComparator(ReportItem::residentName).setAutoWidth(true);
        grid.addColumn(i -> i.report().description()).setHeader("Description").setFlexGrow(2);
        grid.addColumn(i -> i.report().address()).setHeader("Address").setFlexGrow(1);
        grid.addColumn(i -> i.report().dateSubmitted().format(DATE)).setHeader("Submitted")
                .setComparator(i -> i.report().dateSubmitted()).setAutoWidth(true);
        grid.addComponentColumn(AdminReportsView::statusCell).setHeader("Status")
                .setComparator(i -> i.report().status()).setAutoWidth(true);
        grid.addComponentColumn(i -> ReportRow.priorityBadge(highestPriority(i))).setHeader("Priority")
                .setComparator(i -> highestPriority(i)).setAutoWidth(true);
        groupColumn = grid.addComponentColumn(this::clusterBadge).setHeader("Group").setAutoWidth(true);
        grid.addColumn(i -> i.report().reviewDate() == null ? "-" : i.report().reviewDate().format(DATE))
                .setHeader("Reviewed").setAutoWidth(true);

        grid.addItemClickListener(e -> {
            List<ReportItem> members = clusters.get(e.getItem().report().reportId());
            if (members == null) {
                openReport(e.getItem());
            } else {
                openCluster(members);
            }
        });
        return grid;
    }

    private static Span statusCell(ReportItem item) {
        ReportStatus status = item.report().status();
        Span dot = new Span();
        dot.addClassNames("status-dot", "status-" + status.name().toLowerCase().replace('_', '-'));
        Span cell = new Span(dot, new Span(status.getLabel()));
        cell.getStyle().set("display", "inline-flex").set("align-items", "center").set("gap", "8px");
        return cell;
    }


    private ReportPriority highestPriority(ReportItem item) {
        return clusters.getOrDefault(item.report().reportId(), List.of(item)).stream()
                .map(i -> controller.effectivePriority(i.report()))
                .max(Comparator.naturalOrder())
                .orElseThrow();
    }

    private Span clusterBadge(ReportItem item) {
        List<ReportItem> members = clusters.get(item.report().reportId());
        Span badge = new Span(members == null ? "" : members.size() + " reports");
        badge.setVisible(members != null);
        badge.addClassName("cluster-badge");
        return badge;
    }

    private List<ReportItem> groupItems(List<ReportItem> items) {
        Map<Integer, ReportItem> byId = new HashMap<>();
        items.forEach(i -> byId.put(i.report().reportId(), i));
        List<ReportItem> rows = new ArrayList<>();
        Set<Integer> grouped = new HashSet<>();
        for (List<Report> cluster : clusterService.cluster(items.stream().map(ReportItem::report).toList())) {
            List<ReportItem> members = cluster.stream().map(r -> byId.get(r.reportId())).toList();
            rows.add(members.get(0));
            if (members.size() > 1) {
                clusters.put(members.get(0).report().reportId(), members);
            }
            cluster.forEach(r -> grouped.add(r.reportId()));
        }
        items.stream().filter(i -> !grouped.contains(i.report().reportId())).forEach(rows::add);
        rows.sort(Comparator.comparing((ReportItem i) -> i.report().dateSubmitted()).reversed());
        return rows;
    }

    private void refreshList() {
        List<ReportItem> items = controller.searchReports(section.statuses, activeFilter, search.getValue());
        if (overdueOnly) {
            items = items.stream().filter(i -> controller.isOverdue(i.report())).toList();
        }
        clusters.clear();
        groupColumn.setVisible(group.getValue());
        grid.setItems(group.getValue() ? groupItems(items) : items);
    }

    private void openCluster(List<ReportItem> members) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(members.size() + " similar reports");
        dialog.setWidth("720px");
        Div list = new Div();
        list.addClassName("report-list");
        members.forEach(member -> {
            ReportRow row = new ReportRow(member.report(), controller);
            row.addClickListener(e -> {
                dialog.close();
                openReport(member);
            });
            list.add(row);
        });
        dialog.add(list);
        dialog.getFooter().add(new Button("Close", e -> dialog.close()));
        dialog.open();
    }

    private void openReport(ReportItem item) {
        boolean open = ReportController.OPEN_STATUSES.contains(item.report().status());
        ComplaintDialog dialog = open
                ? new ComplaintDialog(item.report(), controller, analysisService,
                duplicateReportService, spamCheckService, attachmentCheckService)
                : new ComplaintDialog(item.report(), controller);

        User resident = item.resident();
        String who = resident == null ? "Resident: Unknown"
                : "Resident: " + resident.fullName() + " (" + resident.email() + ")";
        if (item.report().reviewDate() == null) {
            dialog.setAdminInfo(who);
        } else {
            String reviewer = item.reviewer() == null ? "an admin" : item.reviewer().fullName();
            dialog.setAdminInfo(who, "Reviewed by " + reviewer + " on "
                    + item.report().reviewDate().format(DATE_TIME));
        }
        session.getCurrentUser().ifPresent(admin -> dialog.setReviewer(admin, this::refreshList));
        dialog.open();
    }
}