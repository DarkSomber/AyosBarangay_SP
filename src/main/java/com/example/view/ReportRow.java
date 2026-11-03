package com.example.view;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.example.model.Report;
import com.example.model.ReportImage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.StreamResource;

/** One row in the report list. Pure display, no logic. */
public class ReportRow extends Div {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public ReportRow(Report report) {
        this(report, List.of());
    }

    /** @param images the report's ORIGINAL pictures; the first one that can be shown becomes the thumbnail */
    public ReportRow(Report report, List<ReportImage> images) {
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
        Div status = new Div(statusLabel, dot);
        status.addClassName("report-status");

        add(buildThumb(report, images), text, status);
    }

    /** Display only: upper-cases the first letter so rows look consistent. */
    private static String capitalize(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /** Placeholder box, or the first available photo. Clicking the photo opens a larger preview. */
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
            // Stops the click from reaching the row, which opens the report details.
            thumb.getElement().executeJs("this.addEventListener('click', e => e.stopPropagation())");
            break;
        }
        return thumb;
    }
}