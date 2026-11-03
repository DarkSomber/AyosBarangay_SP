package com.example.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.example.model.AdminNotification.Status;

/** MySQL access for resident bell notifications (user_notifications): report reviewed or status changed. */
@Repository
public class UserNotificationRepository {

    private final JdbcClient jdbc;

    public UserNotificationRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void notifyUser(int userId, int reportId, String message) {
        jdbc.sql("""
                INSERT INTO user_notifications (user_id, report_id, message, status, created_at)
                VALUES (:user, :report, :message, 'UNREAD', :now)
                """)
                .param("user", userId)
                .param("report", reportId)
                .param("message", message)
                .param("now", LocalDateTime.now())
                .update();
    }

    /** Newest first; ignored ones are hidden. */
    public List<AdminNotification> findRecent(int userId) {
        return jdbc.sql("""
                SELECT * FROM user_notifications WHERE user_id = :id AND status <> 'IGNORED'
                ORDER BY created_at DESC, notification_id DESC LIMIT 15
                """)
                .param("id", userId)
                .query(this::map)
                .list();
    }

    public int countUnread(int userId) {
        return jdbc.sql("SELECT COUNT(*) FROM user_notifications WHERE user_id = :id AND status = 'UNREAD'")
                .param("id", userId)
                .query(Integer.class)
                .single();
    }

    public void setStatus(int notificationId, int userId, Status status) {
        jdbc.sql("UPDATE user_notifications SET status = :status WHERE notification_id = :id AND user_id = :user")
                .param("status", status.name())
                .param("id", notificationId)
                .param("user", userId)
                .update();
    }

    public void markAllRead(int userId) {
        jdbc.sql("UPDATE user_notifications SET status = 'READ' WHERE user_id = :user AND status = 'UNREAD'")
                .param("user", userId)
                .update();
    }

    /** Reuses AdminNotification as the shared bell row; its adminId holds the resident's id here. */
    private AdminNotification map(ResultSet rs, int rowNum) throws SQLException {
        return new AdminNotification(
                rs.getInt("notification_id"),
                rs.getInt("user_id"),
                rs.getInt("report_id"),
                rs.getString("message"),
                Status.valueOf(rs.getString("status")),
                rs.getObject("created_at", LocalDateTime.class));
    }
}
