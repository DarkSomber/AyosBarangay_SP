package com.example.view;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.example.controller.ReportController;
import com.example.model.Report;
import com.example.model.ReportImage;
import com.example.model.ReportPriority;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.StreamResource;

public class ReportRow extends Div {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public ReportRow(Report report, ReportController controller) {
        this(report, List.of(), controller);
    }

    public ReportRow(Report report, List<ReportImage> images, ReportController controller) {
        addClassName("report-row");

        Span desc = new Span(capitalize(report.description()));
        desc.addClassName("report-title");

        Span meta = new Span(report.dateSubmitted().format(DATE_FMT) + "  |  " + capitalize(report.address()));
        meta.addClassName("report-meta");

        Div text = new Div(desc, meta);
        text.addClassName("report-text");

        Span statusLabel = new Span(report.status().getLabel());
        Span dot = new Span();
        dot.addClassNames("status-dot",
                "status-" + report.status().name().toLowerCase().replace('_', '-'));
        Div status = new Div(priorityBadge(controller.effectivePriority(report)));
        if (controller.isOverdue(report)) {
            status.add(overdueBadge());
        }
        status.add(statusLabel, dot);
        status.addClassName("report-status");

        add(buildThumb(report, images), text, status);
    }

    public static Span priorityBadge(ReportPriority priority) {
        Span badge = new Span(priority.getLabel());
        badge.addClassNames("priority-badge", "priority-" + priority.name().toLowerCase());
        return badge;
    }

    public static Span overdueBadge() {
        Span badge = new Span("Overdue");
        badge.addClassName("overdue-badge");
        return badge;
    }

    private static String capitalize(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static Div buildThumb(Report report, List<ReportImage> images) {
        Div thumb = new Div();
        thumb.addClassName("report-thumb");

        for (ReportImage image : images) {
            Optional<StreamResource> source = ReportImages.resource(image);
            if (source.isEmpty()) {
                continue;
            }
            String alt = "Photo for report #" + report.reportId();
            Image pic = new Image(source.get(), alt);
            pic.addClassName("report-thumb-image");
            thumb.add(pic);
            thumb.addClassName("has-image");
            thumb.addClickListener(e -> new ImagePreviewDialog(source.get(), alt).open());
            thumb.getElement().executeJs("this.addEventListener('click', e => e.stopPropagation())");
            break;
        }
        return thumb;
    }
}