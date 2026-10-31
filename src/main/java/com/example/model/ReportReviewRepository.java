package com.example.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** MySQL access for status changes, their history, and resolutions. */
@Repository
public class ReportReviewRepository {

    private final JdbcClient jdbc;

    public ReportReviewRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Returns false if the report is no longer in oldStatus (someone else changed it first). */
    public boolean updateStatus(int reportId, ReportStatus oldStatus, ReportStatus newStatus, int adminId) {
        LocalDateTime now = LocalDateTime.now();
        return jdbc.sql("""
                UPDATE reports
                SET status = :newStatus, reviewed_by = :admin, review_date = :now, updated_at = :now
                WHERE report_id = :id AND status = :oldStatus
                """)
                .param("newStatus", newStatus.name())
                .param("admin", adminId)
                .param("now", now)
                .param("id", reportId)
                .param("oldStatus", oldStatus.name())
                .update() == 1;
    }

    public void saveHistory(ReportHistory h) {
        jdbc.sql("""
                INSERT INTO report_history (report_id, changed_by, old_status, new_status, change_reason, changed_at)
                VALUES (:reportId, :changedBy, :oldStatus, :newStatus, :reason, :changedAt)
                """)
                .param("reportId", h.reportId())
                .param("changedBy", h.changedBy())
                .param("oldStatus", h.oldStatus() == null ? null : h.oldStatus().name())
                .param("newStatus", h.newStatus().name())
                .param("reason", h.changeReason())
                .param("changedAt", h.changedAt())
                .update();
    }

    public void saveResolution(Resolution r) {
        jdbc.sql("""
                INSERT INTO resolutions (report_id, admin_id, resolution_description, resolution_date, unresolvable_reason)
                VALUES (:reportId, :adminId, :description, :date, :unresolvable)
                """)
                .param("reportId", r.reportId())
                .param("adminId", r.adminId())
                .param("description", r.resolutionDescription())
                .param("date", r.resolutionDate())
                .param("unresolvable", r.unresolvableReason())
                .update();
    }

    public List<ReportHistory> findHistory(int reportId) {
        return jdbc.sql("SELECT * FROM report_history WHERE report_id = :id ORDER BY changed_at, history_id")
                .param("id", reportId)
                .query(this::mapHistory)
                .list();
    }

    /** The latest resolution row for the report, if it has been closed. */
    public Optional<Resolution> findResolution(int reportId) {
        return jdbc.sql("""
                SELECT * FROM resolutions WHERE report_id = :id
                ORDER BY resolution_date DESC, resolution_id DESC LIMIT 1
                """)
                .param("id", reportId)
                .query(this::mapResolution)
                .optional();
    }

    private ReportHistory mapHistory(ResultSet rs, int rowNum) throws SQLException {
        String old = rs.getString("old_status");
        return new ReportHistory(
                rs.getInt("history_id"),
                rs.getInt("report_id"),
                rs.getInt("changed_by"),
                old == null ? null : ReportStatus.valueOf(old),
                ReportStatus.valueOf(rs.getString("new_status")),
                rs.getString("change_reason"),
                rs.getObject("changed_at", LocalDateTime.class));
    }

    private Resolution mapResolution(ResultSet rs, int rowNum) throws SQLException {
        return new Resolution(
                rs.getInt("resolution_id"),
                rs.getInt("report_id"),
                rs.getInt("admin_id"),
                rs.getString("resolution_description"),
                rs.getObject("resolution_date", LocalDateTime.class),
                rs.getString("unresolvable_reason"));
    }
}
