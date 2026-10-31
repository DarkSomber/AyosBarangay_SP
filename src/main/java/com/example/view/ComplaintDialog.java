package com.example.view;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.slf4j.LoggerFactory;

import com.example.ai.ReportAnalysis;
import com.example.ai.ReportAnalysisException;
import com.example.ai.ReportAnalysisService;
import com.example.controller.ReportController;
import com.example.duplicate.DuplicateCandidate;
import com.example.duplicate.DuplicateReportService;
import com.example.media.AttachmentAssessment;
import com.example.media.AttachmentCheckService;
import com.example.model.Report;
import com.example.model.ReportHistory;
import com.example.model.ReportImage;
import com.example.model.ReportStatus;
import com.example.model.User;
import com.example.spam.ReportSpamCheckService;
import com.example.spam.SpamAssessment;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.server.StreamResource;

public class ComplaintDialog extends Dialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy  h:mm a");
    private static final Logger log = LoggerFactory.getLogger(ComplaintDialog.class);

    private final ReportController controller;
    private final ReportAnalysisService analysisService;
    private final DuplicateReportService duplicateReportService;
    private final ReportSpamCheckService spamCheckService;
    private final AttachmentCheckService attachmentCheckService;
    private final Report report;
    private final Div outcome = new Div();
    private final Div adminInfo = new Div();
    private User reviewer;
    private Runnable onStatusChanged;

    public ComplaintDialog(Report report, ReportController controller) {
        this(report, controller, null, null, null, null);
    }

    public ComplaintDialog(Report report, ReportController controller,
                           ReportAnalysisService analysisService) {
        this(report, controller, analysisService, null, null, null);
    }

    public ComplaintDialog(Report report, ReportController controller,
                           ReportAnalysisService analysisService,
                           DuplicateReportService duplicateReportService) {
        this(report, controller, analysisService, duplicateReportService, null, null);
    }

    public ComplaintDialog(Report report, ReportController controller,
                           ReportAnalysisService analysisService,
                           DuplicateReportService duplicateReportService,
                           ReportSpamCheckService spamCheckService) {
        this(report, controller, analysisService, duplicateReportService, spamCheckService, null);
    }

    public ComplaintDialog(Report report, ReportController controller,
                           ReportAnalysisService analysisService,
                           DuplicateReportService duplicateReportService,
                           ReportSpamCheckService spamCheckService,
                           AttachmentCheckService attachmentCheckService) {
        this.controller = controller;
        this.report = report;
        outcome.addClassNames("complaint-duplicate-section", "complaint-outcome");
        this.analysisService = analysisService;
        this.duplicateReportService = duplicateReportService;
        this.spamCheckService = spamCheckService;
        this.attachmentCheckService = attachmentCheckService;
        addClassName("complaint-dialog");
        setCloseOnOutsideClick(true);
        setCloseOnEsc(true);

        Div root = new Div(buildTopBar(report), buildBody(report));
        root.addClassNames("complaint-root");
        if (analysisService != null) {
            root.addClassName("complaint-root-with-analysis");
        }
        add(root);
        showOutcome();
    }

    /** Admin: extra lines under the date (resident, reviewer). Call before open(). */
    public void setAdminInfo(String... lines) {
        adminInfo.removeAll();
        for (String line : lines) {
            adminInfo.add(new Div(new Span(line)));
        }
    }

    /** Admin tools: lets this dialog change the report's status. Call before open(). */
    public void setReviewer(User admin, Runnable onStatusChanged) {
        this.reviewer = admin;
        this.onStatusChanged = onStatusChanged;
        showOutcome();
    }

    /** Resident tools: Edit and Cancel buttons for their own open report. Call before open(). */
    public void setResident(User resident, Runnable onChanged) {
        if (!controller.canManage(resident, report)) {
            return;
        }
        Button edit = new Button("Edit", VaadinIcon.EDIT.create(), e -> {
            close();
            new EditReportDialog(controller, resident, report, onChanged).open();
        });
        Button cancel = new Button("Cancel report", VaadinIcon.TRASH.create(), e -> confirmCancel(resident, onChanged));
        cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
        outcome.add(new Div(edit, cancel));
    }

    private void confirmCancel(User resident, Runnable onChanged) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Cancel report #" + report.reportId() + "?");
        confirm.setText("This report will be removed from your list and the barangay will no longer review it.");
        confirm.setCancelable(true);
        confirm.setCancelText("Keep report");
        confirm.setConfirmText("Cancel report");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(e -> {
            try {
                controller.cancelReport(resident, report);
                Notification.show("Report cancelled.");
                close();
                onChanged.run();
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage());
            } catch (DataAccessException ex) {
                log.error("Could not cancel report {}", report.reportId(), ex);
                Notification.show("Could not cancel the report. Please try again.");
            }
        });
        confirm.open();
    }

    /** Status history and resolution for everyone; admins also get the status form. */
    private void showOutcome() {
        outcome.removeAll();
        outcome.add(new H4("Status history"));
        var history = controller.getHistory(report.reportId());
        if (history.isEmpty()) {
            outcome.add(new Span("No status changes yet."));
        }
        for (ReportHistory h : history) {
            String from = h.oldStatus() == null ? "" : h.oldStatus().getLabel() + " -> ";
            outcome.add(new Span(from + h.newStatus().getLabel() + "  |  " + h.changedAt().format(DATE_FMT)));
            if (reviewer != null && h.changeReason() != null) {
                outcome.add(new Span("Note: " + h.changeReason()));
            }
        }
        controller.getResolution(report.reportId()).ifPresent(r -> outcome.add(analysisField(
                r.isUnresolvable() ? "Why it can't be resolved" : "Resolution",
                r.isUnresolvable() ? r.unresolvableReason() : r.resolutionDescription())));
        if (reviewer != null && ReportController.OPEN_STATUSES.contains(report.status())) {
            outcome.add(buildStatusForm());
        }
    }

    private Div buildStatusForm() {
        Select<ReportStatus> next = new Select<>();
        next.setLabel("Change status to");
        next.setItems(Stream.of(ReportStatus.UNDER_REVIEW, ReportStatus.RESOLVED, ReportStatus.UNRESOLVABLE)
                .filter(s -> s != report.status()).toList());
        next.setItemLabelGenerator(ReportStatus::getLabel);

        TextArea text = new TextArea(textLabel(null));
        text.setWidthFull();
        text.setMaxLength(2000);
        next.addValueChangeListener(e -> text.setLabel(textLabel(e.getValue())));

        Button save = new Button("Update status", e -> {
            try {
                controller.updateStatus(report, reviewer, next.getValue(), text.getValue());
                Notification.show("Report #" + report.reportId() + " updated.");
                close();
                if (onStatusChanged != null) {
                    onStatusChanged.run();
                }
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage());
            } catch (DataAccessException ex) {
                log.error("Could not update report {}", report.reportId(), ex);
                Notification.show("Could not save the change. Please try again.");
            }
        });
        save.addClassName("primary");
        return new Div(next, text, save);
    }

    private static String textLabel(ReportStatus status) {
        if (status == ReportStatus.RESOLVED) {
            return "How was it resolved? (required)";
        }
        if (status == ReportStatus.UNRESOLVABLE) {
            return "Why can't it be resolved? (required)";
        }
        return "Note (optional)";
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
        AttachmentAssessment attachmentAssessment = attachmentCheckService == null
                ? null : attachmentCheckService.inspect(images);
        Div photo;
        if (images.isEmpty()) {
            photo = new Div(new Span("No media attached"));
            photo.addClassName("complaint-photo");
        } else {
            photo = new Div();
            photo.addClassName("complaint-photo-gallery");

            for (ReportImage img : images) {
                if (attachmentAssessment != null
                        && !attachmentAssessment.validPaths().contains(img.imagePath())) {
                    continue;
                }
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
                    log.warn("Skipping unreadable attachment {} for report {}",
                            img.imageId(), report.reportId());
                }
            }
        }

        adminInfo.addClassName("complaint-date");
        Div left = new Div(date, adminInfo, details, photo, outcome);
        if (analysisService != null) {
            left.add(buildAnalysisSection(report));
        }
        if (duplicateReportService != null) {
            left.add(buildDuplicateSection(report));
        }
        if (spamCheckService != null) {
            left.add(buildSpamSection(report));
        }
        if (attachmentCheckService != null) {
            left.add(buildAttachmentSection(attachmentAssessment, images.size()));
        }
        left.addClassName("complaint-left");
        if (analysisService != null || duplicateReportService != null
                || spamCheckService != null || attachmentCheckService != null) {
            left.addClassName("complaint-left-with-review-tools");
        }

        Span address = new Span(report.address());
        address.addClassName("complaint-address");

        Div right = new Div(buildMap(report), address);
        right.addClassName("complaint-right");

        Div body = new Div(left, right);
        body.addClassName("complaint-body");
        return body;
    }

    private Div buildAnalysisSection(Report report) {
        Button analyze = new Button("Analyze report");
        analyze.addClassNames("complaint-analysis-button", "primary");
        Span state = new Span();
        state.addClassName("complaint-analysis-state");
        state.setVisible(false);
        Span error = new Span();
        error.addClassName("complaint-analysis-error");
        error.setVisible(false);
        Div result = new Div();
        result.addClassName("complaint-analysis-result");
        result.setVisible(false);
        analysisService.cachedAnalysis(report).ifPresent(analysis -> showAnalysis(result, analysis));
        analyze.addClickListener(event -> startAnalysis(report, analyze, state, error, result));

        Div section = new Div(new H4("AI report analysis"), analyze, state, error, result);
        section.addClassName("complaint-analysis");
        return section;
    }

    private Div buildDuplicateSection(Report report) {
        Div section = new Div();
        section.addClassName("complaint-duplicate-section");
        section.add(new H4("Potential duplicate reports - review only"));

        var candidates = duplicateReportService.findCandidates(report);
        if (candidates.isEmpty()) {
            section.add(new Span("No recent potential duplicates found."));
            return section;
        }

        candidates.forEach(candidate -> section.add(duplicateCandidate(candidate)));
        return section;
    }

    private Div duplicateCandidate(DuplicateCandidate candidate) {
        Report possibleDuplicate = candidate.report();
        Span title = new Span("Report #" + possibleDuplicate.reportId()
                + " - " + possibleDuplicate.status().getLabel());
        Span details = new Span(possibleDuplicate.dateSubmitted().format(DATE_FMT)
                + " | " + possibleDuplicate.address());
        Span reason = new Span("Match: " + candidate.reason());
        Div item = new Div(title, details, reason);
        item.addClassName("complaint-duplicate-candidate");
        return item;
    }

    private Div buildSpamSection(Report report) {
        Div section = new Div();
        section.addClassName("complaint-spam-section");
        section.add(new H4("Submission quality - advisory review"));
        section.add(new Span("Flags do not reject reports or change their status."));

        SpamAssessment assessment = spamCheckService.assess(report.description());
        if (!assessment.flagged()) {
            section.add(new Span("No obvious junk-text patterns detected."));
            return section;
        }
        assessment.reasons().forEach(reason -> section.add(new Span(reason)));
        return section;
    }

    private Div buildAttachmentSection(AttachmentAssessment assessment, int attachmentCount) {
        Div section = new Div();
        section.addClassName("complaint-attachment-section");
        section.add(new H4("Attachment checks - advisory review"));
        section.add(new Span("Warnings do not reject reports or change their status."));
        section.add(new Span("MP4 checks verify the container signature, not full video playback."));

        if (!assessment.attachmentProvided()) {
            section.add(new Span("No attachment provided."));
            return section;
        }
        if (!assessment.hasWarnings()) {
            section.add(new Span("No attachment issues detected in " + attachmentCount + " file(s)."));
            return section;
        }
        assessment.findings().forEach(finding -> {
            Span warning = new Span(finding.fileName() + ": " + finding.reason());
            warning.addClassName("complaint-attachment-warning");
            section.add(warning);
        });
        return section;
    }

    private void startAnalysis(Report report, Button button, Span state, Span error, Div result) {
        UI ui = getUI().orElse(null);
        if (ui == null) {
            error.setText("Analysis cannot start because this dialog is no longer active.");
            error.setVisible(true);
            return;
        }

        button.setEnabled(false);
        button.setText("Analyzing...");
        state.setText("Reviewing the report description...");
        state.setVisible(true);
        error.setVisible(false);
        result.setVisible(false);

        analysisService.analyze(report).whenComplete((analysis, failure) ->
                ui.access(() -> {
                    button.setEnabled(true);
                    button.setText("Analyze report");
                    state.setVisible(false);
                    if (failure != null) {
                        Throwable cause = failure;
                        while (cause instanceof CompletionException && cause.getCause() != null) {
                            cause = cause.getCause();
                        }
                        error.setText(cause instanceof ReportAnalysisException
                                ? cause.getMessage()
                                : "Analysis could not be completed. Please try again.");
                        error.setVisible(true);
                        return;
                    }
                    showAnalysis(result, analysis);
                }));
    }

    private void showAnalysis(Div result, ReportAnalysis analysis) {
        result.removeAll();
        result.add(
                analysisField("Urgency", analysis.urgency().name()),
                analysisField("Urgency evidence", analysis.urgencyReason()),
                analysisField("Category", analysis.category().name().replace('_', ' ')),
                analysisField("Summary", analysis.summary()),
                analysisField("Recommended next action", analysis.recommendedAction()),
                analysisField("Suspicious-report review signal",
                        (analysis.suspicious() ? "Flagged for human review" : "No specific indicators identified")
                                + " - not proof"),
                analysisField("Suspicious-report evidence", analysis.suspiciousReason()));
        result.setVisible(true);
    }

    private Div analysisField(String label, String value) {
        Div field = new Div(new Span(label), new Span(value));
        field.addClassName("complaint-analysis-field");
        return field;
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