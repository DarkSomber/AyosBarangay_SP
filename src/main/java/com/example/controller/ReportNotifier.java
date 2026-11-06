package com.example.controller;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.example.model.AdminNotificationRepository;
import com.example.model.UserNotificationRepository;
import com.vaadin.flow.shared.Registration;

@Component
public class ReportNotifier {

    private static final Logger log = LoggerFactory.getLogger(ReportNotifier.class);

    public record NewReportAlert(Integer reportId, String description, String address) {
        public String message() {
            String text = description.length() > 90 ? description.substring(0, 90) + "..." : description;
            return (reportId == null ? "New report: " : "New report #" + reportId + ": ") + text;
        }
    }

    public record StatusAlert(int userId, int reportId, String message) { }

    private final AdminNotificationRepository repository;
    private final UserNotificationRepository userRepository;
    private final List<Consumer<NewReportAlert>> listeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<StatusAlert>> statusListeners = new CopyOnWriteArrayList<>();

    public ReportNotifier(AdminNotificationRepository repository, UserNotificationRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Registration registerStatus(Consumer<StatusAlert> listener) {
        statusListeners.add(listener);
        return () -> statusListeners.remove(listener);
    }

    public void publishStatusChange(StatusAlert alert) {
        try {
            userRepository.notifyUser(alert.userId(), alert.reportId(), alert.message());
        } catch (DataAccessException ex) {
            log.warn("Could not save resident notification. Is user_notifications created? {}", ex.getMessage());
        }
        statusListeners.forEach(l -> l.accept(alert));
    }

    public Registration register(Consumer<NewReportAlert> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public void publish(NewReportAlert alert) {
        try {
            repository.notifyAdmins(alert.reportId(), alert.message());
        } catch (DataAccessException ex) {
            log.warn("Could not save admin notifications. Are admin_notifications / admin_settings created? {}",
                    ex.getMessage());
        }
        listeners.forEach(l -> l.accept(alert));
    }
}