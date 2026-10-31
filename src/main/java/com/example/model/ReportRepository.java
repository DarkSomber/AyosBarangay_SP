package com.example.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;

/**
 * Data access contract. The controller only knows about this interface,
 * will be swapped in-memory version for a JDBC/MySQL later
 * while not touching any view or controller.
 */
public interface ReportRepository {

    List<Report> findAll();
    Report save(Report report);

    /**
     * Updates description and address only if the report belongs to userId, is not deleted
     * and is still open (Pending / Under Review). Returns false if nothing was updated.
     */
    boolean updateDetails(int reportId, int userId, String description, String address, LocalDateTime now);

    /** Soft-deletes under the same ownership/open rules as updateDetails. Returns false if nothing changed. */
    boolean softDelete(int reportId, int userId, String reason, LocalDateTime now);

    /**
     * Persists a new media attachment (image or video) for a report.
     */
    void saveImage(ReportImage image);

    /**
     * Retrieves all media attachments associated with a specific report.
     */
    List<ReportImage> findImagesByReportId(int reportId);

    /** Attachments of several reports at once, oldest first. */
    default List<ReportImage> findImagesByReportIds(Collection<Integer> reportIds) {
        return reportIds.stream().flatMap(id -> findImagesByReportId(id).stream()).toList();
    }
}