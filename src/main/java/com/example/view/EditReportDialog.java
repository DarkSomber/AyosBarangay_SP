package com.example.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import com.example.controller.ReportController;
import com.example.model.Report;
import com.example.model.User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

/** Lets a resident change the description and address of their own open report. */
public class EditReportDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(EditReportDialog.class);

    public EditReportDialog(ReportController controller, User resident, Report report, Runnable onSaved) {
        setHeaderTitle("Edit Report #" + report.reportId());
        setCloseOnOutsideClick(false);

        TextArea description = new TextArea("Description");
        description.setValue(report.description());
        description.setMaxLength(2000);
        description.setWidthFull();
        description.setMinHeight("140px");

        TextField address = new TextField("Address");
        address.setValue(report.address());
        address.setMaxLength(255);
        address.setWidthFull();

        Button save = new Button("Save", e -> {
            try {
                controller.editReport(resident, report, description.getValue(), address.getValue());
                Notification.show("Report updated.");
                close();
                onSaved.run();
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage());
            } catch (DataAccessException ex) {
                log.error("Could not edit report {}", report.reportId(), ex);
                Notification.show("Could not save the changes. Please try again.");
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button("Cancel", e -> close()), save);

        VerticalLayout form = new VerticalLayout(description, address);
        form.setPadding(false);
        form.setWidth("420px");
        form.setMaxWidth("100%");
        add(form);
    }
}