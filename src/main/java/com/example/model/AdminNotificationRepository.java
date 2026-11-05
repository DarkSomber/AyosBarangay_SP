package com.example.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.example.model.AdminNotification.Status;

@Repository
public class AdminNotificationRepository {

    private final JdbcClient jdbc;

    public AdminNotificationRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }


    public boolean isNewReportAlertsEnabled(int adminId) {
        return jdbc.sql("SELECT new_report_alerts FROM admin_settings WHERE user_id = :id")
                .param("id", adminId)
                .query(Boolean.class)
                .optional()
                .orElse(true);
    }

    public void setNewReportAlerts(int adminId, boolean on) {
        jdbc.sql("""
                INSERT INTO admin_settings (user_id, new_report_alerts) VALUES (:id, :on)
                ON DUPLICATE KEY UPDATE new_report_alerts = :on
                """)
                .param("id", adminId)
                .param("on", on)
                .update();
    }


    public void notifyAdmins(Integer reportId, String message) {
        jdbc.sql("""
                INSERT INTO admin_notifications (admin_id, report_id, message, status, created_at)
                SELECT u.user_id, :reportId, :message, 'UNREAD', :now
                FROM users u LEFT JOIN admin_settings s ON s.user_id = u.user_id
                WHERE u.role = 'ADMIN' AND COALESCE(s.new_report_alerts, TRUE) = TRUE
                """)
                .param("reportId", reportId)
                .param("message", message)
                .param("now", LocalDateTime.now())
                .update();
    }

    public List<AdminNotification> findRecent(int adminId) {
        return jdbc.sql("""
                SELECT * FROM admin_notifications WHERE admin_id = :id AND status <> 'IGNORED'
                ORDER BY created_at DESC, notification_id DESC LIMIT 15
                """)
                .param("id", adminId)
                .query(this::map)
                .list();
    }

    public int countUnread(int adminId) {
        return jdbc.sql("SELECT COUNT(*) FROM admin_notifications WHERE admin_id = :id AND status = 'UNREAD'")
                .param("id", adminId)
                .query(Integer.class)
                .single();
    }

    public void setStatus(int notificationId, int adminId, Status status) {
        jdbc.sql("UPDATE admin_notifications SET status = :status WHERE notification_id = :id AND admin_id = :admin")
                .param("status", status.name())
                .param("id", notificationId)
                .param("admin", adminId)
                .update();
    }

    public void markAllRead(int adminId) {
        jdbc.sql("UPDATE admin_notifications SET status = 'READ' WHERE admin_id = :admin AND status = 'UNREAD'")
                .param("admin", adminId)
                .update();
    }

    public void markReportRead(int adminId, Integer reportId) {
        jdbc.sql("""
                UPDATE admin_notifications SET status = 'READ'
                WHERE admin_id = :admin AND status = 'UNREAD' AND report_id <=> :reportId
                """)
                .param("admin", adminId)
                .param("reportId", reportId)
                .update();
    }

    public void ignoreReport(int adminId, Integer reportId) {
        jdbc.sql("""
                UPDATE admin_notifications SET status = 'IGNORED'
                WHERE admin_id = :admin AND status = 'UNREAD' AND report_id <=> :reportId
                """)
                .param("admin", adminId)
                .param("reportId", reportId)
                .update();
    }

    private AdminNotification map(ResultSet rs, int rowNum) throws SQLException {
        return new AdminNotification(
                rs.getInt("notification_id"),
                rs.getInt("admin_id"),
                rs.getObject("report_id", Integer.class),
                rs.getString("message"),
                Status.valueOf(rs.getString("status")),
                rs.getObject("created_at", LocalDateTime.class));
    }
}