package com.example.model;

import java.time.LocalDateTime;

public record AdminNotification(int notificationId, int adminId, Integer reportId, String message,
                                Status status, LocalDateTime createdAt) {

    public enum Status { UNREAD, READ, IGNORED }
}