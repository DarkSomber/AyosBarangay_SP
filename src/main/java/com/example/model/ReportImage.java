package com.example.model;

import java.time.LocalDateTime;

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
