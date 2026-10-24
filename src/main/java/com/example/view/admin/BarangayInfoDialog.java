package com.example.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

/** Read-only barangay details. Values are placeholders until a data source exists. */
public class BarangayInfoDialog extends Dialog {

    public BarangayInfoDialog() {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        H2 title = new H2("Barangay Information");
        title.addClassName("barangay-title");

        TextField name = new TextField("Barangay Name");
        name.setValue("Sample Barangay"); // TODO: real value
        name.setReadOnly(true);
        name.addClassName("barangay-field");

        TextArea location = new TextArea("Barangay Location");
        location.setValue("Sample address, Quezon City"); // TODO: real value
        location.setReadOnly(true);
        location.addClassName("barangay-field");
        location.addClassName("barangay-location");

        Button close = new Button("Close", e -> close());
        close.addClassName("barangay-close");

        Div root = new Div(title, name, location, close);
        root.addClassName("barangay-root");
        add(root);
    }
}