package com.example.view;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.model.ReportImage;
import com.vaadin.flow.server.StreamResource;

/**
 * Turns a stored report image into something a browser can load. Only png and
 * jpeg files inside the upload folder are served. When an image is skipped, the
 * reason is logged as a warning so missing thumbnails can be traced.
 */
final class ReportImages {

    private static final Logger log = LoggerFactory.getLogger(ReportImages.class);

    private ReportImages() {
    }

    static Optional<StreamResource> resource(ReportImage image) {
        try {
            Path root = CreateReportDialog.UPLOAD_DIR.toAbsolutePath().normalize();
            // Photos uploaded before the upload folder was configurable live in the temp folder.
            Path legacyRoot = Paths.get(System.getProperty("java.io.tmpdir"), "ayosbarangay-uploads")
                    .toAbsolutePath().normalize();
            Path file = Paths.get(image.imagePath()).toAbsolutePath().normalize();
            String mime = mimeType(file.getFileName().toString());
            if (mime == null) {
                log.warn("Image {} skipped: unsupported type ({})", image.imageId(), file);
                return Optional.empty();
            }
            if (!file.startsWith(root) && !file.startsWith(legacyRoot)) {
                log.warn("Image {} skipped: {} is outside upload folder {}", image.imageId(), file, root);
                return Optional.empty();
            }
            if (!Files.isRegularFile(file)) {
                log.warn("Image {} skipped: file not found at {}", image.imageId(), file);
                return Optional.empty();
            }
            StreamResource resource = new StreamResource(file.getFileName().toString(), () -> open(file));
            resource.setContentType(mime);
            return Optional.of(resource);
        } catch (InvalidPathException ex) {
            log.warn("Image {} skipped: invalid path {}", image.imageId(), image.imagePath());
            return Optional.empty();
        }
    }

    private static String mimeType(String fileName) {
        String name = fileName.toLowerCase(Locale.ROOT);
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return null;
    }

    private static InputStream open(Path file) {
        try {
            return Files.newInputStream(file);
        } catch (IOException ex) {
            return InputStream.nullInputStream();
        }
    }
}