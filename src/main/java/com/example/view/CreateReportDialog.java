package com.example.view;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.example.controller.ReportController;
import com.example.model.Report;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

public class CreateReportDialog extends Dialog {

    static final Path UPLOAD_DIR = UploadDir.get();

    private final ReportController controller;
    private final int userId;
    private final Runnable onSubmitted;

    private final TextArea description = new TextArea();
    private final TextField address = new TextField();
    private final Button automaticToggle = new Button("Automatic");

    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Upload upload = new Upload(buffer);
    private final List<Path> pendingMediaPaths = new ArrayList<>();
    private final Div mediaThumbs = new Div();

    private Double latitude;
    private Double longitude;
    private boolean automatic = true;
    private boolean submitted = false;
    private boolean submitting = false;

    public CreateReportDialog(ReportController controller, int userId, Runnable onSubmitted) {
        this.controller = controller;
        this.userId = userId;
        this.onSubmitted = onSubmitted;

        addClassName("create-report-dialog");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setHeaderTitle("AyosBarangay");

        setWidth("850px");
        setMaxWidth("88vw");
        setHeight("480px");
        setMaxHeight("85vh");

        add(buildForm());

        addAttachListener(e -> {
            initMap();
            getElement().executeJs("setTimeout(() => this.$server.triggerInitialLocation(), 800);");
        });
    }

    private Div buildForm() {
        Div left = new Div(buildMediaZone(), buildDescriptionField());
        left.addClassName("create-left");

        Div right = new Div(buildLocationHeader(), buildMapContainer(),
                buildAddressField(), buildSubmitButton());
        right.addClassName("create-right");

        Div body = new Div(left, right);
        body.addClassName("create-body");

        return new Div(body);
    }

    private Div buildMediaZone() {
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg", "video/mp4");
        upload.setMaxFiles(5);
        upload.setMaxFileSize(20 * 1024 * 1024); // 20 MB limit for video
        upload.addClassName("create-upload");

        upload.setUploadButton(new Button("Add Media"));
        upload.setDropLabel(new Span("Drop images or MP4 here"));

        upload.addSucceededListener(e -> {
            try {
                Path saved = saveUploadedFile(e.getFileName(), buffer.getInputStream());
                pendingMediaPaths.add(saved);
                addMediaChip(e.getFileName());
            } catch (Exception ex) {
                showError("Could not save the file: " + ex.getMessage());
            }
        });

        upload.addFileRejectedListener(e -> showError("Rejected: " + e.getErrorMessage()));

        mediaThumbs.addClassName("create-image-thumbs");
        mediaThumbs.setVisible(false);

        Div zone = new Div(upload, mediaThumbs);
        zone.addClassName("create-image-zone");
        return zone;
    }

    private Path saveUploadedFile(String originalName, InputStream input) throws Exception {
        Files.createDirectories(UPLOAD_DIR);
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) ext = originalName.substring(dot).toLowerCase();
        Path dest = UPLOAD_DIR.resolve(UUID.randomUUID() + ext);
        try (FileOutputStream out = new FileOutputStream(dest.toFile())) {
            input.transferTo(out);
        }
        return dest;
    }

    private void addMediaChip(String name) {
        mediaThumbs.setVisible(true);
        Span chip = new Span("📎 " + name);
        chip.addClassName("create-image-chip");
        mediaThumbs.add(chip);
    }

    private TextArea buildDescriptionField() {
        description.setPlaceholder("Write your report");
        description.setMaxLength(2000);
        description.addClassName("create-description");
        description.setWidthFull();
        return description;
    }

    private Div buildLocationHeader() {
        automaticToggle.addClassNames("create-auto-toggle", "on");
        automaticToggle.addClickListener(e -> setAutomatic(!automatic));

        Div header = new Div(automaticToggle);
        header.addClassName("create-location-header");
        return header;
    }

    private Div buildMapContainer() {
        Div mapContainer = new Div();
        mapContainer.setId("leaflet-map-container");
        mapContainer.addClassName("create-map");
        return mapContainer;
    }

    private TextField buildAddressField() {
        address.setPlaceholder("Text address");
        address.setMaxLength(255);
        address.addClassName("create-address");
        address.setWidthFull();

        address.addKeyDownListener(Key.ENTER, e -> {
            if (address.getValue() != null && !address.getValue().isBlank()) {
                forwardGeocode(address.getValue());
            }
        });
        return address;
    }

    private Div buildSubmitButton() {
        Button submit = new Button("SUBMIT", e -> submit());
        submit.addClassName("create-submit");

        Button cancel = new Button("Cancel", e -> confirmCancel());
        cancel.addClassName("create-cancel");

        Div footer = new Div(cancel, submit);
        footer.addClassName("create-footer");

        getElement().addEventListener("close-requested", ev -> confirmCancel());
        return footer;
    }

    private void initMap() {
        getElement().executeJs(
                "if (!document.querySelector('link[href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\"]')) {" +
                        "  let link = document.createElement('link');" +
                        "  link.rel = 'stylesheet'; link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';" +
                        "  document.head.appendChild(link);" +
                        "}" +
                        "if (typeof L === 'undefined') {" +
                        "  let script = document.createElement('script');" +
                        "  script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';" +
                        "  script.onload = () => this.$server.retryInitMap();" +
                        "  document.head.appendChild(script);" +
                        "  return;" +
                        "}" +
                        "let container = this.querySelector('#leaflet-map-container');" +
                        "if (!container || container._leaflet_id) return;" +
                        "let map = L.map(container).setView([14.5995, 120.9842], 13);" +
                        "window.ayosMap = map;" +
                        "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                        "  attribution: '&copy; OpenStreetMap'" +
                        "}).addTo(map);" +
                        "window.currentMarker = null;" +
                        "map.on('click', function(e) {" +
                        "  if (window.currentMarker) map.removeLayer(window.currentMarker);" +
                        "  window.currentMarker = L.marker(e.latlng).addTo(map);" +
                        "  this.$server.setMapLocation(e.latlng.lat, e.latlng.lng);" +
                        "}.bind(this));" +
                        "setTimeout(() => map.invalidateSize(), 100);"
        );
    }

    @ClientCallable public void retryInitMap() { initMap(); }
    @ClientCallable public void triggerInitialLocation() { if (automatic) requestBrowserLocation(); }

    private void setAutomatic(boolean automatic) {
        this.automatic = automatic;
        automaticToggle.setText(automatic ? "Automatic" : "Manual");
        automaticToggle.setClassName("create-auto-toggle" + (automatic ? " on" : ""));

        if (automatic) {
            requestBrowserLocation();
        } else {
            this.latitude = null;
            this.longitude = null;
            address.clear();
        }
    }

    private void requestBrowserLocation() {
        getElement().executeJs(
                "navigator.geolocation.getCurrentPosition(" +
                        "  p => this.$server.setBrowserLocation(p.coords.latitude, p.coords.longitude)," +
                        "  (err) => { " +
                        "    this.$server.locationFailed(err.message); " +
                        "  }" +
                        ")");
    }

    @ClientCallable
    public void locationFailed(String errorMessage) {
        showError("Could not get location: " + errorMessage);
        setAutomatic(false);
    }

    @ClientCallable
    public void setMapLocation(double lat, double lng) {
        this.latitude = lat;
        this.longitude = lng;
        reverseGeocode(lat, lng);
    }

    @ClientCallable
    public void setBrowserLocation(double lat, double lng) {
        this.latitude = lat;
        this.longitude = lng;
        getElement().executeJs(
                "let map = window.ayosMap;" +
                        "if (map) {" +
                        "  map.setView([$0, $1], 16);" +
                        "  if (window.currentMarker) map.removeLayer(window.currentMarker);" +
                        "  window.currentMarker = L.marker([$0, $1]).addTo(map);" +
                        "  map.invalidateSize();" +
                        "}", lat, lng
        );
        reverseGeocode(lat, lng);
    }

    private void reverseGeocode(double lat, double lng) {
        new Thread(() -> {
            try {
                String url = String.format(Locale.ROOT,
                        "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f", lat, lng);
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "AyosBarangay-App/1.0")
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                String json = response.body();
                int start = json.indexOf("\"display_name\":\"");
                if (start != -1) {
                    start += 16;
                    int end = json.indexOf("\"", start);
                    String displayName = json.substring(start, end);
                    getUI().ifPresent(ui -> ui.access(() -> address.setValue(displayName)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void forwardGeocode(String query) {
        new Thread(() -> {
            try {
                String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
                String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encodedQuery;

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "AyosBarangay-App/1.0")
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                String json = response.body();
                int latStart = json.indexOf("\"lat\":\"");
                int lonStart = json.indexOf("\"lon\":\"");

                if (latStart != -1 && lonStart != -1) {
                    latStart += 7;
                    int latEnd = json.indexOf("\"", latStart);
                    double lat = Double.parseDouble(json.substring(latStart, latEnd));

                    lonStart += 7;
                    int lonEnd = json.indexOf("\"", lonStart);
                    double lon = Double.parseDouble(json.substring(lonStart, lonEnd));

                    getUI().ifPresent(ui -> ui.access(() -> {
                        this.latitude = lat;
                        this.longitude = lon;
                        getElement().executeJs(
                                "let map = window.ayosMap;" +
                                        "if (map) {" +
                                        "  map.setView([$0, $1], 16);" +
                                        "  if (window.currentMarker) map.removeLayer(window.currentMarker);" +
                                        "  window.currentMarker = L.marker([$0, $1]).addTo(map);" +
                                        "}", lat, lon
                        );
                    }));
                } else {
                    showError("Address not found.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void submit() {
        if (submitting) return;

        String desc = description.getValue() == null ? "" : description.getValue().trim();
        String addr = address.getValue() == null ? "" : address.getValue().trim();

        String error = null;
        if (desc.isEmpty()) {
            error = "Please describe the issue.";
        } else if (addr.isEmpty()) {
            error = "Please enter the location address.";
        } else if (latitude == null || longitude == null) {
            error = "Please click on the map to set a location.";
        }

        if (error != null) {
            showError(error);
            return;
        }

        submitting = true;
        try {
            // Saves the report and all its media to the database in one transaction.
            Report saved = controller.submitReportWithMedia(userId, desc, addr, latitude, longitude,
                    pendingMediaPaths.stream().map(Path::toString).toList());

            submitted = true;
            Notification.show(String.format(Locale.ROOT, "Report #%d submitted with %d file(s).", saved.reportId(), pendingMediaPaths.size()), 3000, Notification.Position.BOTTOM_START);
            if (onSubmitted != null) onSubmitted.run();
            close();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (RuntimeException ex) {
            ex.printStackTrace(); // shows the real database error in the console
            showError("The report could not be saved. Please try again.");
        } finally {
            submitting = false;
        }
    }

    private void showError(String message) {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.add(new Span(message));
        notification.setPosition(Notification.Position.MIDDLE);
        notification.setDuration(1500);
        notification.open();
    }

    private boolean hasUnsavedData() {
        return (description.getValue() != null && !description.getValue().isBlank())
                || (address.getValue() != null && !address.getValue().isBlank())
                || !pendingMediaPaths.isEmpty();
    }

    private void confirmCancel() {
        if (submitted || !hasUnsavedData()) {
            close();
            return;
        }
        Dialog confirm = new Dialog();
        confirm.addClassName("cancel-warning");
        Span text = new Span("Are you sure you want to cancel this submission?");
        text.addClassName("cancel-warning-text");
        Button yes = new Button("Yes", e -> { confirm.close(); close(); });
        Button no = new Button("No", e -> confirm.close());
        Div actions = new Div(yes, no);
        actions.addClassName("cancel-warning-actions");
        confirm.add(text, actions);
        confirm.open();
    }
}