package com.example.model;

import java.time.LocalDateTime;

/** One bell notification for one admin. Table: admin_notifications. reportId is null for test notifications. */
public record AdminNotification(int notificationId, int adminId, Integer reportId, String message,
                                Status status, LocalDateTime createdAt) {

    public enum Status { UNREAD, READ, IGNORED }
}