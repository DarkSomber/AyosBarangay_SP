package com.example;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
 * Public Dashboard (default screen).
 * Uses the "View Submitted Reports" layout: filter chips, sort dropdown,
 * a scrolling list of report rows, and a floating Create Report button.
 * Anyone can view the list. Creating a report needs a logged-in account.
 */
@Route("")
@PageTitle("AyosBarangay")
public class PublicDashboardView extends Div {

    // ---------- Data model (swap for your JDBC/MySQL results later) ----------

    public enum Status {
        PENDING("Pending", "#e0a100"),
        IN_PROGRESS("In Progress", "#2f6fed"),
        RESOLVED("Resolved", "#2e9e5b");

        final String label;
        final String color;

        Status(String label, String color) {
            this.label = label;
            this.color = color;
        }
    }

    public record Report(int id, String title, String description,
                         String address, LocalDate date, Status status) {
    }

    // ---------- State ----------

    private final List<Report> reports = List.of(
            new Report(1, "Broken streetlight", "The streetlight near the basketball court has been out for a week.",
                    "Purok 3, Main St.", LocalDate.of(2026, 10, 8), Status.PENDING),
            new Report(2, "Flooded drainage", "Water pools on the road every time it rains.",
                    "Purok 1, Riverside Rd.", LocalDate.of(2026, 10, 5), Status.IN_PROGRESS),
            new Report(3, "Uncollected garbage", "Trash has not been picked up since Monday.",
                    "Purok 5, Mabini St.", LocalDate.of(2026, 10, 3), Status.IN_PROGRESS),
            new Report(4, "Pothole on road", "Large pothole near the barangay hall entrance.",
                    "Barangay Hall Rd.", LocalDate.of(2026, 9, 28), Status.RESOLVED),
            new Report(5, "Stray dogs near school", "A group of stray dogs gathers by the school gate.",
                    "Purok 2, School Ave.", LocalDate.of(2026, 9, 25), Status.PENDING));

    private Status activeFilter = null; // null means "All"
    private boolean newestFirst = true;

    private final Div filterBar = new Div();
    private final Div listContainer = new Div();
    private final Map<Status, Button> chipByStatus = new EnumMap<>(Status.class);
    private Button allChip;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    // UI

    public PublicDashboardView() {
        setSizeFull();
        getStyle().set("background", "#d9d9d9")
                .set("min-height", "100vh")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("flex-direction", "column");

        add(buildHeader(), buildPanel(), buildCreateReportButton());
        refreshList();
    }

    private Div buildHeader() {
        Button menu = new Button(VaadinIcon.MENU.create());
        menu.getElement().setAttribute("aria-label", "Open menu");
        menu.getStyle().set("border-radius", "50%")
                .set("background", "#8a8a8a")
                .set("color", "white")
                .set("width", "40px")
                .set("height", "40px")
                .set("min-width", "40px")
                .set("padding", "0");
        // Navbar drawer comes later
        menu.addClickListener(e -> Notification.show("Navbar coming soon"));

        H3 title = new H3("AyosBarangay");
        title.getStyle().set("margin", "0").set("font-weight", "800");

        Div header = new Div(menu, title);
        header.getStyle().set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("padding", "12px 20px");
        return header;
    }

    private Div buildPanel() {
        Div panel = new Div(buildFilterBar(), listContainer);
        panel.getStyle().set("background", "white")
                .set("margin", "0 20px 20px 20px")
                .set("padding", "16px")
                .set("border-radius", "6px")
                .set("flex", "1")
                .set("overflow-y", "auto")
                .set("box-sizing", "border-box");

        listContainer.getStyle().set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "10px")
                .set("margin-top", "12px")
                .set("padding-bottom", "70px"); // room for the floating button
        return panel;
    }

    private Div buildFilterBar() {
        filterBar.getStyle().set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("align-items", "center")
                .set("gap", "8px");

        allChip = makeChip("All", () -> setFilter(null));
        filterBar.add(allChip);

        for (Status s : Status.values()) {
            Button chip = makeChip(s.label, () -> setFilter(s));
            chipByStatus.put(s, chip);
            filterBar.add(chip);
        }

        Select<String> sort = new Select<>();
        sort.setItems("Sort by: newest", "Sort by: oldest");
        sort.setValue("Sort by: newest");
        sort.setAriaLabel("Sort reports");
        sort.addValueChangeListener(e -> {
            newestFirst = "Sort by: newest".equals(e.getValue());
            refreshList();
        });
        sort.getStyle().set("margin-left", "auto").set("width", "180px");
        filterBar.add(sort);

        updateChipStyles();
        return filterBar;
    }

    private Button makeChip(String text, Runnable onClick) {
        Button chip = new Button(text, e -> onClick.run());
        chip.getStyle().set("border-radius", "999px")
                .set("cursor", "pointer");
        return chip;
    }

    private void setFilter(Status status) {
        activeFilter = status;
        updateChipStyles();
        refreshList();
    }

    private void updateChipStyles() {
        styleChip(allChip, activeFilter == null);
        chipByStatus.forEach((s, chip) -> styleChip(chip, activeFilter == s));
    }

    private void styleChip(Button chip, boolean active) {
        chip.getStyle().set("background", active ? "#3a3a3a" : "#e6e6e6")
                .set("color", active ? "white" : "#222");
    }

    private Button buildCreateReportButton() {
        Button create = new Button("Create Report", VaadinIcon.PLUS.create());
        create.getStyle().set("position", "fixed")
                .set("right", "32px")
                .set("bottom", "32px")
                .set("border-radius", "999px")
                .set("background", "white")
                .set("color", "#222")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.3)")
                .set("z-index", "10");
        // Login isn't built yet. Later, check the session and open the Create Report dialog.
        create.addClickListener(e -> Notification.show("Please log in to submit a report."));
        return create;
    }

    // ---------- List rendering ----------

    private void refreshList() {
        listContainer.removeAll();

        Comparator<Report> byDate = Comparator.comparing(Report::date);
        if (newestFirst) {
            byDate = byDate.reversed();
        }

        List<Report> visible = reports.stream()
                .filter(r -> activeFilter == null || r.status() == activeFilter)
                .sorted(byDate)
                .toList();

        if (visible.isEmpty()) {
            Span empty = new Span("No reports found.");
            empty.getStyle().set("color", "#777").set("padding", "24px").set("text-align", "center");
            listContainer.add(empty);
            return;
        }

        visible.forEach(r -> listContainer.add(buildRow(r)));
    }

    private Div buildRow(Report r) {
        // Thumbnail placeholder (swap for an Image when reports have photos)
        Div thumb = new Div();
        thumb.getStyle().set("background", "#8a8a8a")
                .set("width", "84px")
                .set("height", "60px")
                .set("flex-shrink", "0")
                .set("border-radius", "4px");

        Span title = new Span(r.title());
        title.getStyle().set("font-weight", "700");

        Span desc = new Span(r.description());
        desc.getStyle().set("color", "#444")
                .set("font-size", "0.9em")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis");

        Span meta = new Span(r.date().format(DATE_FMT) + "  |  " + r.address());
        meta.getStyle().set("color", "#777").set("font-size", "0.8em");

        Div text = new Div(title, desc, meta);
        text.getStyle().set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "2px")
                .set("flex", "1")
                .set("min-width", "0"); // lets the ellipsis work inside flex

        Span dot = new Span();
        dot.getStyle().set("width", "12px")
                .set("height", "12px")
                .set("border-radius", "50%")
                .set("background", r.status().color)
                .set("display", "inline-block");
        Span statusLabel = new Span(r.status().label);
        statusLabel.getStyle().set("font-size", "0.85em");

        Div status = new Div(statusLabel, dot);
        status.getStyle().set("display", "flex")
                .set("align-items", "center")
                .set("gap", "6px")
                .set("flex-shrink", "0");

        Div row = new Div(thumb, text, status);
        row.getStyle().set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px")
                .set("background", "#e6e6e6")
                .set("padding", "10px")
                .set("border-radius", "4px")
                .set("cursor", "pointer");

        // Complaint Screen (dialog with map) comes next
        row.addClickListener(e -> Notification.show("Opening: " + r.title()));
        return row;
    }
}