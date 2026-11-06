package com.example.model;

public enum SortOrder {
    NEWEST("Sort by: newest"),
    OLDEST("Sort by: oldest"),
    OVERDUE_FIRST("Sort by: overdue first");

    private final String label;

    SortOrder(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
