package com.example.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;

public interface ReportRepository {

    List<Report> findAll();
    Report save(Report report);

    boolean updateDetails(int reportId, int userId, String description, String address, LocalDateTime now);

    boolean softDelete(int reportId, int userId, String reason, LocalDateTime now);

    boolean updatePriority(int reportId, ReportPriority priority);

    void saveImage(ReportImage image);

    List<ReportImage> findImagesByReportId(int reportId);

    default List<ReportImage> findImagesByReportIds(Collection<Integer> reportIds) {
        return reportIds.stream().flatMap(id -> findImagesByReportId(id).stream()).toList();
    }
}