package com.example.controller;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.model.AdminNotification;
import com.example.model.AdminNotification.Status;
import com.example.model.AdminNotificationRepository;
import com.example.model.User;
import com.example.model.UserNotificationRepository;

@Component
public class NotificationController {

    public interface Source {
        List<AdminNotification> recent();
        int unreadCount();
        void markRead(int notificationId);
        void ignore(int notificationId);
        void markAllRead();
    }

    private final AdminNotificationRepository repository;
    private final UserNotificationRepository userRepository;

    public NotificationController(AdminNotificationRepository repository,
                                  UserNotificationRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Source adminSource(User admin) {
        return new Source() {
            public List<AdminNotification> recent() { return NotificationController.this.recent(admin); }
            public int unreadCount() { return NotificationController.this.unreadCount(admin); }
            public void markRead(int id) { NotificationController.this.markRead(admin, id); }
            public void ignore(int id) { NotificationController.this.ignore(admin, id); }
            public void markAllRead() { NotificationController.this.markAllRead(admin); }
        };
    }

    public Source userSource(User user) {
        if (user == null || user.isAdmin()) {
            throw new IllegalArgumentException("Only residents have report updates.");
        }
        int id = user.userId();
        return new Source() {
            public List<AdminNotification> recent() { return userRepository.findRecent(id); }
            public int unreadCount() { return userRepository.countUnread(id); }
            public void markRead(int n) { userRepository.setStatus(n, id, Status.READ); }
            public void ignore(int n) { userRepository.setStatus(n, id, Status.IGNORED); }
            public void markAllRead() { userRepository.markAllRead(id); }
        };
    }

    public boolean isEnabled(User admin) {
        return repository.isNewReportAlertsEnabled(requireAdmin(admin));
    }

    public void setEnabled(User admin, boolean on) {
        repository.setNewReportAlerts(requireAdmin(admin), on);
    }

    public List<AdminNotification> recent(User admin) {
        return repository.findRecent(requireAdmin(admin));
    }

    public int unreadCount(User admin) {
        return repository.countUnread(requireAdmin(admin));
    }

    public void markRead(User admin, int notificationId) {
        repository.setStatus(notificationId, requireAdmin(admin), Status.READ);
    }

    public void ignore(User admin, int notificationId) {
        repository.setStatus(notificationId, requireAdmin(admin), Status.IGNORED);
    }

    public void markAllRead(User admin) {
        repository.markAllRead(requireAdmin(admin));
    }

    public void markReportRead(User admin, Integer reportId) {
        repository.markReportRead(requireAdmin(admin), reportId);
    }

    public void ignoreReport(User admin, Integer reportId) {
        repository.ignoreReport(requireAdmin(admin), reportId);
    }

    private int requireAdmin(User admin) {
        if (admin == null || !admin.isAdmin()) {
            throw new IllegalArgumentException("Only admins have notifications.");
        }
        return admin.userId();
    }
}