package com.example.view;

import java.time.format.DateTimeFormatter;

import com.example.model.Report;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

/** One row in the report list. Pure display, no logic. */
public class ReportRow extends Div {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public ReportRow(Report report) {
        addClassName("report-row");

        Div thumb = new Div();
        thumb.addClassName("report-thumb");

        Span desc = new Span(report.description());
        desc.addClassName("report-title");

        Span meta = new Span(report.dateSubmitted().format(DATE_FMT) + "  |  " + report.address());
        meta.addClassName("report-meta");

        Div text = new Div(desc, meta);
        text.addClassName("report-text");

        Span statusLabel = new Span(report.status().getLabel());
        Span dot = new Span();
        dot.addClassNames("status-dot",
                "status-" + report.status().name().toLowerCase().replace('_', '-'));
        Div status = new Div(statusLabel, dot);
        status.addClassName("report-status");

        add(thumb, text, status);
    }
}