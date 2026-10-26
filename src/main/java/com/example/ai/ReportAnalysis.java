package com.example.ai;

import java.util.Objects;

public record ReportAnalysis(
        ReportUrgency urgency,
        ReportCategory category,
        String summary,
        String recommendedAction,
        boolean suspicious,
        String urgencyReason,
        String suspiciousReason) {

    public ReportAnalysis {
        Objects.requireNonNull(urgency, "Urgency is required.");
        Objects.requireNonNull(category, "Category is required.");
        summary = requireText(summary, "Summary", 500);
        recommendedAction = requireText(recommendedAction, "Recommended action", 500);
        urgencyReason = requireText(urgencyReason, "Urgency reason", 300);
        suspiciousReason = requireText(suspiciousReason, "Suspicious-report reason", 300);
    }

    private static String requireText(String value, String name, int maxLength) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new IllegalArgumentException(name + " must contain 1 to " + maxLength + " characters.");
        }
        return value.trim();
    }
}
