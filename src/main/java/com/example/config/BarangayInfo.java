package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class BarangayInfo {

    private record Row(String name, String location, String phone) {}

    private final JdbcClient jdbc;
    private final String name;
    private final String location;
    private final String police;
    private final String hotline;
    private final String redCross;
    private final String barangayPhone;
    private final String ncmh;

    public BarangayInfo(JdbcClient jdbc,
                        @Value("${ayos.barangay.name:}") String name,
                        @Value("${ayos.barangay.location:}") String location,
                        @Value("${ayos.emergency.police:}") String police,
                        @Value("${ayos.emergency.hotline:911}") String hotline,
                        @Value("${ayos.emergency.red-cross:}") String redCross,
                        @Value("${ayos.emergency.barangay:}") String barangayPhone,
                        @Value("${ayos.emergency.ncmh:0917-899-8727}") String ncmh) {
        this.jdbc = jdbc;
        this.name = clean(name);
        this.location = clean(location);
        this.police = clean(police);
        this.hotline = clean(hotline);
        this.redCross = clean(redCross);
        this.barangayPhone = clean(barangayPhone);
        this.ncmh = clean(ncmh);
    }

    // Admin-edited values (barangay_info table) win; properties are the fallback.
    public String name() { Row r = row(); return r == null ? name : r.name(); }
    public String location() { Row r = row(); return r == null ? location : r.location(); }
    public String barangayPhone() { Row r = row(); return r == null ? barangayPhone : r.phone(); }
    public String police() { return police; }
    public String hotline() { return hotline; }
    public String redCross() { return redCross; }
    public String ncmh() { return ncmh; }

    public void save(String name, String location, String phone) {
        jdbc.sql("""
                INSERT INTO barangay_info (id, name, location, barangay_phone)
                VALUES (1, :name, :location, :phone)
                ON DUPLICATE KEY UPDATE name = :name, location = :location, barangay_phone = :phone
                """)
                .param("name", clean(name))
                .param("location", clean(location))
                .param("phone", clean(phone))
                .update();
    }

    private Row row() {
        try {
            return jdbc.sql("SELECT name, location, barangay_phone FROM barangay_info WHERE id = 1")
                    .query((rs, i) -> new Row(clean(rs.getString(1)), clean(rs.getString(2)), clean(rs.getString(3))))
                    .optional().orElse(null);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}