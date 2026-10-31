package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Barangay details and emergency numbers, read from application.properties / hush.env. Blank = not set. */
@Component
public class BarangayInfo {

    private final String name;
    private final String location;
    private final String police;
    private final String hotline;
    private final String redCross;
    private final String barangayPhone;
    private final String ncmh;

    public BarangayInfo(@Value("${ayos.barangay.name:}") String name,
                        @Value("${ayos.barangay.location:}") String location,
                        @Value("${ayos.emergency.police:}") String police,
                        @Value("${ayos.emergency.hotline:911}") String hotline,
                        @Value("${ayos.emergency.red-cross:}") String redCross,
                        @Value("${ayos.emergency.barangay:}") String barangayPhone,
                        @Value("${ayos.emergency.ncmh:0917-899-8727}") String ncmh) {
        this.name = clean(name);
        this.location = clean(location);
        this.police = clean(police);
        this.hotline = clean(hotline);
        this.redCross = clean(redCross);
        this.barangayPhone = clean(barangayPhone);
        this.ncmh = clean(ncmh);
    }

    public String name() { return name; }
    public String location() { return location; }
    public String police() { return police; }
    public String hotline() { return hotline; }
    public String redCross() { return redCross; }
    public String barangayPhone() { return barangayPhone; }
    public String ncmh() { return ncmh; }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
