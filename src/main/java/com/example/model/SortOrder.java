package com.example.model;

public enum SortOrder {
    NEWEST("Sort by: newest"),
    OLDEST("Sort by: oldest");

    private final String label;

    SortOrder(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
