package com.example.view;

import com.vaadin.flow.component.html.Image;

/** The AyosBarangay logo. File: src/main/resources/META-INF/resources/ImgIcons/AyosBarangay-Logo.png */
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
