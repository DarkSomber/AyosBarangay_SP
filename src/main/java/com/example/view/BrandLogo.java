package com.example.view;

import com.vaadin.flow.component.html.Image;

public final class BrandLogo {

    public static final String PATH = "ImgIcons/AyosBarangay-Logo.png";

    private BrandLogo() {
    }

    public static Image create(String cssClass) {
        Image logo = new Image(PATH, "AyosBarangay logo");
        logo.addClassName(cssClass);
        return logo;
    }
}
