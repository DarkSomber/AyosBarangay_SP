package com.example.view;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.example.model.Report;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Complaint Screen: a dialog showing one report's status, date, details,
 * and a map of where it was reported. Pure display, no logic.
 */
public class ComplaintDialog extends Dialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy  h:mm a");

    public ComplaintDialog(Report report) {
        addClassName("complaint-dialog");
        setCloseOnOutsideClick(true);
        setCloseOnEsc(true);

        Div root = new Div(buildTopBar(report), buildBody(report));
        root.addClassName("complaint-root"); // fixed size, set in styles.css
        add(root);
    }

    private Div buildTopBar(Report report) {
        Span statusLabel = new Span("Status: " + report.status().getLabel());
        Span dot = new Span();
        dot.addClassNames("status-dot",
                "status-" + report.status().name().toLowerCase().replace('_', '-'));
        Div status = new Div(statusLabel, dot);
        status.addClassName("complaint-status");

        Button close = new Button(VaadinIcon.CLOSE.create(), e -> close());
        close.addClassName("complaint-close");
        close.getElement().setAttribute("aria-label", "Close");

        Div top = new Div(status, close);
        top.addClassName("complaint-top");
        return top;
    }

    private Div buildBody(Report report) {
        Span date = new Span("Date: " + report.dateSubmitted().format(DATE_FMT));
        date.addClassName("complaint-date");

        Span details = new Span(report.description());
        details.addClassName("complaint-details");

        Div photo = new Div(new Span("No photo attached"));
        photo.addClassName("complaint-photo");

        Div left = new Div(date, details, photo);
        left.addClassName("complaint-left");

        Span address = new Span(report.address());
        address.addClassName("complaint-address");

        Div right = new Div(buildMap(report), address);
        right.addClassName("complaint-right");

        Div body = new Div(left, right);
        body.addClassName("complaint-body");
        return body;
    }

    /** OpenStreetMap embed with a marker. No extra dependency or API key needed. */
    private IFrame buildMap(Report report) {
        double lat = report.latitude();
        double lon = report.longitude();
        double dLat = 0.002;
        double dLon = 0.003;
        String url = String.format(Locale.ROOT,
                "https://www.openstreetmap.org/export/embed.html?bbox=%.6f,%.6f,%.6f,%.6f&layer=mapnik&marker=%.6f,%.6f",
                lon - dLon, lat - dLat, lon + dLon, lat + dLat, lat, lon);
        IFrame map = new IFrame(url);
        map.addClassName("complaint-map");
        map.getElement().setAttribute("title", "Report location map");
        map.getElement().setAttribute("loading", "lazy");
        return map;
    }
}