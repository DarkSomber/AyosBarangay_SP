package com.example.model;

import java.time.LocalDateTime;

/**
 * A picture attached to a report. A report can have many.
 * Table: report_images
 *
 * Only the file path or URL is stored, not the picture itself.
 * The upload code (not written yet) must check file type and size, and must
 * generate its own safe file names instead of trusting the uploader's name.
 *
 * @param imageId    primary key, null until saved
 * @param reportId   the report it belongs to (FK to reports)
 * @param imagePath  stored file path or URL
 * @param imageType  ORIGINAL (from the resident) or RESOLUTION (from the admin)
 * @param uploadedAt when it was uploaded
 */
public record ReportImage(Integer imageId, int reportId, String imagePath,
                          ImageType imageType, LocalDateTime uploadedAt) {

    public ReportImage {
        Validation.requirePositive(reportId, "Report id");
        imagePath = Validation.requireText(imagePath, "Image path", 500);
        Validation.requireNonNull(imageType, "Image type");
        Validation.requireNonNull(uploadedAt, "Uploaded at");
    }

    public static ReportImage of(int reportId, String imagePath, ImageType imageType) {
        return new ReportImage(null, reportId, imagePath, imageType, LocalDateTime.now());
    }
}
