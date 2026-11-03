package com.example.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** MySQL access for the report activity timeline. Insert and read only, by design. */
@Repository
public class ReportActivityRepository {

    private final JdbcClient jdbc;

    public ReportActivityRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void save(ReportActivity a) {
        jdbc.sql("""
                INSERT INTO report_activities (report_id, action, old_status, new_status, performed_by,
                                               remarks, created_at)
                VALUES (:reportId, :action, :oldStatus, :newStatus, :by, :remarks, :createdAt)
                """)
                .param("reportId", a.reportId())
                .param("action", a.action().name())
                .param("oldStatus", a.oldStatus() == null ? null : a.oldStatus().name())
                .param("newStatus", a.newStatus() == null ? null : a.newStatus().name())
                .param("by", a.performedBy())
                .param("remarks", a.remarks())
                .param("createdAt", a.createdAt())
                .update();
    }

    public List<ReportActivity> findByReport(int reportId) {
        return jdbc.sql("SELECT * FROM report_activities WHERE report_id = :id ORDER BY created_at, activity_id")
                .param("id", reportId)
                .query(this::map)
                .list();
    }

    private ReportActivity map(ResultSet rs, int rowNum) throws SQLException {
        String oldStatus = rs.getString("old_status");
        String newStatus = rs.getString("new_status");
        return new ReportActivity(
                rs.getInt("activity_id"),
                rs.getInt("report_id"),
                ReportAction.valueOf(rs.getString("action")),
                oldStatus == null ? null : ReportStatus.valueOf(oldStatus),
                newStatus == null ? null : ReportStatus.valueOf(newStatus),
                rs.getObject("performed_by", Integer.class),
                rs.getString("remarks"),
                rs.getObject("created_at", LocalDateTime.class));
    }
}