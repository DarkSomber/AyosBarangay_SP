package com.example.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** MySQL-backed implementation of ReportRepository. */
@Repository
public class JdbcReportRepository implements ReportRepository {

    private final JdbcClient jdbc;

    public JdbcReportRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Report> findAll() {
        return jdbc.sql("SELECT * FROM reports WHERE deleted_at IS NULL")
                .query(this::mapReport)
                .list();
    }

    @Override
    public Report save(Report r) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.sql("""
                INSERT INTO reports (user_id, description, status, date_submitted, updated_at,
                                     address, latitude, longitude, reviewed_by, review_date,
                                     deleted_at, deletion_reason)
                VALUES (:userId, :description, :status, :submitted, :updated,
                        :address, :lat, :lng, :reviewedBy, :reviewDate,
                        :deletedAt, :deletionReason)
                """)
                .param("userId", r.userId())
                .param("description", r.description())
                .param("status", r.status().name())
                .param("submitted", r.dateSubmitted())
                .param("updated", r.updatedAt())
                .param("address", r.address())
                .param("lat", r.latitude())
                .param("lng", r.longitude())
                .param("reviewedBy", r.reviewedBy())
                .param("reviewDate", r.reviewDate())
                .param("deletedAt", r.deletedAt())
                .param("deletionReason", r.deletionReason())
                .update(keys);

        int id = keys.getKey().intValue();
        return new Report(id, r.userId(), r.description(), r.status(), r.dateSubmitted(),
                r.updatedAt(), r.address(), r.latitude(), r.longitude(), r.reviewedBy(),
                r.reviewDate(), r.deletedAt(), r.deletionReason());
    }

    private static final String OPEN_OWNED = """
            WHERE report_id = :id AND user_id = :userId AND deleted_at IS NULL
              AND status IN ('PENDING', 'UNDER_REVIEW')
            """;

    @Override
    public boolean updateDetails(int reportId, int userId, String description, String address,
                                 LocalDateTime now) {
        return jdbc.sql("UPDATE reports SET description = :description, address = :address, "
                        + "updated_at = :now " + OPEN_OWNED)
                .param("description", description)
                .param("address", address)
                .param("now", now)
                .param("id", reportId)
                .param("userId", userId)
                .update() > 0;
    }

    @Override
    public boolean softDelete(int reportId, int userId, String reason, LocalDateTime now) {
        return jdbc.sql("UPDATE reports SET deleted_at = :now, deletion_reason = :reason, "
                        + "updated_at = :now " + OPEN_OWNED)
                .param("reason", reason)
                .param("now", now)
                .param("id", reportId)
                .param("userId", userId)
                .update() > 0;
    }

    @Override
    public void saveImage(ReportImage img) {
        jdbc.sql("""
                INSERT INTO report_images (report_id, image_path, image_type, uploaded_at)
                VALUES (:reportId, :path, :type, :uploadedAt)
                """)
                .param("reportId", img.reportId())
                .param("path", img.imagePath())
                .param("type", img.imageType().name())
                .param("uploadedAt", img.uploadedAt())
                .update();
    }

    @Override
    public List<ReportImage> findImagesByReportId(int reportId) {
        return jdbc.sql("SELECT * FROM report_images WHERE report_id = :id ORDER BY image_id")
                .param("id", reportId)
                .query(this::mapImage)
                .list();
    }

    @Override
    public List<ReportImage> findImagesByReportIds(Collection<Integer> reportIds) {
        if (reportIds.isEmpty()) {
            return List.of();
        }
        return jdbc.sql("SELECT * FROM report_images WHERE report_id IN (:ids) ORDER BY image_id")
                .param("ids", reportIds)
                .query(this::mapImage)
                .list();
    }

    private ReportImage mapImage(ResultSet rs, int rowNum) throws SQLException {
        return new ReportImage(
                rs.getInt("image_id"),
                rs.getInt("report_id"),
                rs.getString("image_path"),
                ImageType.valueOf(rs.getString("image_type")),
                rs.getObject("uploaded_at", LocalDateTime.class));
    }

    private Report mapReport(ResultSet rs, int rowNum) throws SQLException {
        return new Report(
                rs.getInt("report_id"),
                rs.getInt("user_id"),
                rs.getString("description"),
                ReportStatus.valueOf(rs.getString("status")),
                rs.getObject("date_submitted", LocalDateTime.class),
                rs.getObject("updated_at", LocalDateTime.class),
                rs.getString("address"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getObject("reviewed_by", Integer.class),
                rs.getObject("review_date", LocalDateTime.class),
                rs.getObject("deleted_at", LocalDateTime.class),
                rs.getString("deletion_reason"));
    }
}