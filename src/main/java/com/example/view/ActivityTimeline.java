package com.example.view;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

import com.example.model.ReportActivity;
import com.vaadin.flow.component.html.Div;

/** Vertical timeline of a report's activity. Pass adminName only for admin viewers; the public sees "Barangay admin". */
public final class ActivityTimeline {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private ActivityTimeline() {
    }

    public static Div of(List<ReportActivity> items, Function<Integer, String> adminName) {
        Div timeline = new Div();
        timeline.addClassName("activity-timeline");
        if (items.isEmpty()) {
            timeline.add(line("No activity recorded yet.", "activity-meta"));
            return timeline;
        }
        items.forEach(a -> timeline.add(item(a, adminName)));
        return timeline;
    }

    private static Div item(ReportActivity a, Function<Integer, String> adminName) {
        Div content = new Div(line(a.action().getLabel(), "activity-title"));
        if (a.newStatus() != null) {
            content.add(line(a.oldStatus() == null ? "Status: " + a.newStatus().getLabel()
                    : a.oldStatus().getLabel() + " \u2192 " + a.newStatus().getLabel(), "activity-status"));
        }
        String who = a.performedBy() == null ? ""
                : "  |  " + (adminName == null ? "Barangay admin" : adminName.apply(a.performedBy()));
        content.add(line(a.createdAt().format(WHEN) + who, "activity-meta"));
        if (a.remarks() != null) {
            content.add(line(a.remarks(), "activity-remarks"));
        }
        Div dot = new Div();
        dot.addClassName("activity-dot");
        Div item = new Div(dot, content);
        item.addClassName("activity-item");
        return item;
    }

    private static Div line(String text, String cssClass) {
        Div d = new Div();
        d.setText(text);
        d.addClassName(cssClass);
        return d;
    }
}