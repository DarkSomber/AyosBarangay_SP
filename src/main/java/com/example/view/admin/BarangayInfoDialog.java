package com.example.view.admin;

import org.springframework.dao.DataAccessException;

import com.example.config.BarangayInfo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

public class BarangayInfoDialog extends Dialog {

    private static final String PHONE_PATTERN = "[0-9+()\\- ]{7,20}";

    public BarangayInfoDialog(BarangayInfo info) {
        this(info, false);
    }

    public BarangayInfoDialog(BarangayInfo info, boolean editable) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        H2 title = new H2("Barangay Information");
        title.addClassName("barangay-title");

        TextField name = new TextField("Barangay Name");
        name.setMaxLength(100);
        name.addClassName("barangay-field");

        TextArea location = new TextArea("Barangay Location");
        location.setMaxLength(255);
        location.addClassNames("barangay-field", "barangay-location");

        TextField phone = new TextField("Barangay Phone Number");
        phone.setMaxLength(20);
        phone.addClassName("barangay-field");

        if (editable) {
            name.setValue(info.name() == null ? "" : info.name());
            location.setValue(info.location() == null ? "" : info.location());
            phone.setValue(info.barangayPhone() == null ? "" : info.barangayPhone());
            phone.setHelperText("Used by the Call button on the resident Emergency page.");
        } else {
            name.setValue(info.name() == null ? "Not set yet" : info.name());
            location.setValue(info.location() == null ? "Not set yet" : info.location());
            phone.setValue(info.barangayPhone() == null ? "Not set yet" : info.barangayPhone());
            name.setReadOnly(true);
            location.setReadOnly(true);
            phone.setReadOnly(true);
        }

        Button close = new Button(editable ? "Cancel" : "Close", e -> close());
        getFooter().add(close);

        if (editable) {
            Button save = new Button("Save", e -> {
                String number = phone.getValue().trim();
                if (!number.isEmpty() && !number.matches(PHONE_PATTERN)) {
                    phone.setInvalid(true);
                    phone.setErrorMessage("Use 7-20 digits, spaces, +, - or ().");
                    return;
                }
                try {
                    info.save(name.getValue(), location.getValue(), number);
                    Notification.show("Barangay information saved.");
                    close();
                } catch (DataAccessException ex) {
                    Notification.show("Could not save. Is the barangay_info table created?");
                }
            });
            save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            getFooter().add(save);
        }

        Div root = new Div(title, name, location, phone);
        root.addClassName("barangay-root");
        add(root);
    }
}