package com.example.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;

/** Enlarged view of one report photo. Closes with the X button, Escape or a click outside. */
public class ImagePreviewDialog extends Dialog {

    public ImagePreviewDialog(StreamResource source, String altText) {
        addClassName("image-preview-dialog");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        Button close = new Button(VaadinIcon.CLOSE.create(), e -> close());
        close.addClassName("complaint-close");
        close.getElement().setAttribute("aria-label", "Close");

        Div top = new Div(close);
        top.addClassName("image-preview-top");

        Image image = new Image(source, altText);
        image.addClassName("image-preview-image");

        Div root = new Div(top, image);
        root.addClassName("image-preview-root");
        add(root);
    }
}