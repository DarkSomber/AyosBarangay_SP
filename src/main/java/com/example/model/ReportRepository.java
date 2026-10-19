package com.example.model;

import java.util.List;

/**
 * Data access contract. The controller only knows about this interface,
 * will be swapped in-memory version for a JDBC/MySQL later
 * while not touching any view or controller.
 */
public interface ReportRepository {

    List<Report> findAll();

    /**
     * Persists a new report. Returns the saved report with its assigned reportId.
     * Implementations decide how IDs are assigned (in-memory counter now,
     * AUTO_INCREMENT later).
     */
    Report save(Report report);

    /**
     * Persists a new media attachment (image or video) for a report.
     */
    void saveImage(ReportImage image);

    /**
     * Retrieves all media attachments associated with a specific report.
     */
    List<ReportImage> findImagesByReportId(int reportId);
}