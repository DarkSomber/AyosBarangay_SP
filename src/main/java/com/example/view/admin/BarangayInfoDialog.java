package com.example.view.admin;

import com.example.config.BarangayInfo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

/** Read-only barangay details, taken from application.properties (ayos.barangay.*). */
public class BarangayInfoDialog extends Dialog {

    public BarangayInfoDialog(BarangayInfo info) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        H2 title = new H2("Barangay Information");
        title.addClassName("barangay-title");

        TextField name = new TextField("Barangay Name");
        name.setValue(info.name() == null ? "Not set yet" : info.name());
        name.setReadOnly(true);
        name.addClassName("barangay-field");

        TextArea location = new TextArea("Barangay Location");
        location.setValue(info.location() == null ? "Not set yet" : info.location());
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