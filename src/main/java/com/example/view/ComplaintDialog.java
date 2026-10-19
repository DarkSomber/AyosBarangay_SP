package com.example.view;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.example.controller.ReportController;
import com.example.model.Report;
import com.example.model.ReportImage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;

public class ComplaintDialog extends Dialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy  h:mm a");

    private final ReportController controller;

    public ComplaintDialog(Report report, ReportController controller) {
        this.controller = controller;
        addClassName("complaint-dialog");
        setCloseOnOutsideClick(true);
        setCloseOnEsc(true);

        Div root = new Div(buildTopBar(report), buildBody(report));
        root.addClassName("complaint-root");
        add(root);
    }

    private Div buildTopBar(Report report) {
        Span statusLabel = new Span("Report #" + report.reportId() + "  |  Status: " + report.status().getLabel());
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

        var images = controller.getImagesForReport(report.reportId());
        Div photo;
        if (images.isEmpty()) {
            photo = new Div(new Span("No media attached"));
            photo.addClassName("complaint-photo");
        } else {
            photo = new Div();
            photo.addClassName("complaint-photo-gallery");

            for (ReportImage img : images) {
                String path = img.imagePath();
                String lowerPath = path.toLowerCase();

                try {
                    Path p = Paths.get(path);
                    // Read file into memory to avoid locking issues and path problems
                    byte[] fileBytes = Files.readAllBytes(p);

                    StreamResource resource = new StreamResource(
                            "media-" + img.imageId(),
                            () -> new ByteArrayInputStream(fileBytes)
                    );

                    if (lowerPath.endsWith(".mp4")) {
                        resource.setContentType("video/mp4");
                        IFrame videoFrame = new IFrame();
                        videoFrame.setSrc(resource);
                        videoFrame.setWidth("100%");
                        videoFrame.setHeight("200px");
                        videoFrame.getElement().setAttribute("style",
                                "border:none; border-radius:4px; margin-bottom:4px; overflow:hidden; pointer-events: auto;");
                        videoFrame.getElement().setAttribute("allow", "autoplay");
                        photo.add(videoFrame);
                    } else {
                        // Assume image
                        String mimeType = lowerPath.endsWith(".png") ? "image/png" : "image/jpeg";
                        resource.setContentType(mimeType);

                        Image pic = new Image(resource, "report media");
                        pic.setHeight("100px");
                        pic.getStyle().set("border-radius", "4px").set("margin-right", "4px").set("object-fit", "cover");
                        photo.add(pic);
                    }
                } catch (Exception e) {
                    // If file is missing or unreadable, just skip it
                    e.printStackTrace();
                }
            }
        }

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