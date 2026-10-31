package com.example.view.admin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.example.ai.AISummaryService;
import com.example.controller.SessionController;
import com.example.model.AISummary;
import com.example.model.User;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Admin: generate and reopen AI summaries of reports in a date range. */
@Route(value = "admin/summary", layout = AdminLayout.class)
@PageTitle("AI Summary | AyosBarangay")
public class AdminSummaryView extends Div {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final AISummaryService service;
    private final SessionController session;

    private final DatePicker start = new DatePicker("From");
    private final DatePicker end = new DatePicker("To");
    private final Button generate = new Button("Generate summary");
    private final Span error = new Span();
    private final Div result = new Div();
    private final Div history = new Div();

    public AdminSummaryView(AISummaryService service, SessionController session) {
        this.service = service;
        this.session = session;
        addClassName("admin-page");

        end.setValue(LocalDate.now());
        start.setValue(LocalDate.now().minusDays(7));

        error.addClassName("report-empty");
        error.setVisible(false);
        generate.addClickListener(e -> generate());

        Div form = new Div(start, end, generate);
        form.addClassName("filter-bar");

        Div panel = new Div(form, error, result, history);
        panel.addClassName("dashboard-panel");
        add(panel);
        refreshHistory();
    }

    private void generate() {
        User admin = session.getCurrentUser().orElse(null);
        UI ui = getUI().orElse(null);
        if (admin == null || ui == null) {
            return;
        }
        LocalDate from = start.getValue();
        LocalDate to = end.getValue();

        generate.setEnabled(false);
        generate.setText("Generating...");
        error.setVisible(false);

        new Thread(() -> {
            AISummary summary = null;
            String message = null;
            try {
                summary = service.generate(admin, from, to);
            } catch (IllegalArgumentException ex) {
                message = ex.getMessage();
            } catch (Exception ex) {
                message = "The summary could not be generated. Please try again.";
            }
            AISummary done = summary;
            String failure = message;
            ui.access(() -> {
                generate.setEnabled(true);
                generate.setText("Generate summary");
                if (failure != null) {
                    error.setText(failure);
                    error.setVisible(true);
                    return;
                }
                show(done);
                refreshHistory();
            });
        }).start();
    }

    private void show(AISummary s) {
        result.removeAll();
        Span title = new Span(s.summaryPeriodStart().format(DATE) + " - "
                + s.summaryPeriodEnd().format(DATE) + " (" + s.reportCount() + " reports)");
        title.addClassName("report-title");
        Div body = new Div();
        body.setText(s.summaryContent());
        body.getStyle().set("white-space", "pre-wrap");
        result.add(title, body);
    }

    private void refreshHistory() {
        history.removeAll();
        for (AISummary s : service.recent(5)) {
            Button b = new Button(s.summaryPeriodStart().format(DATE) + " - "
                    + s.summaryPeriodEnd().format(DATE), e -> show(s));
            b.addClassName("filter-chip");
            history.add(b);
        }
    }
}