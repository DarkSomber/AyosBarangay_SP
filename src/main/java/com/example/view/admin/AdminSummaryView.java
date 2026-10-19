package com.example.view.admin;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Admin: AI Summary. Placeholder until the design and the summary feature exist.
 */
@Route(value = "admin/summary", layout = AdminLayout.class)
@PageTitle("AI Summary | AyosBarangay")
public class AdminSummaryView extends Div {

    public AdminSummaryView() {
        addClassName("admin-page");

        Span message = new Span("AI summaries of reports will appear here.");
        message.addClassName("report-empty");

        Div panel = new Div(message);
        panel.addClassName("dashboard-panel");
        add(panel);
    }
}