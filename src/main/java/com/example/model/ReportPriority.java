package com.example.model;

public enum ReportPriority {
    LOW("Low"),
    NORMAL("Normal"),
    HIGH("High"),
    URGENT("Urgent");

    private final String label;

    ReportPriority(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public ReportPriority raised() {
        return values()[Math.min(ordinal() + 1, values().length - 1)];
    }
}
