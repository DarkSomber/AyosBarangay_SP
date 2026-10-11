package com.example.model;

/** Small helpers so every model checks its fields the same way. */
final class Validation {

    private Validation() {
    }

    /** Fails if the text is null or only spaces. Returns the trimmed text. */
    static String requireText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must be at most " + maxLength + " characters.");
        }
        return trimmed;
    }

    /** Optional text: null or blank becomes null, otherwise trimmed and length-checked. */
    static String optionalText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requireText(value, fieldName, maxLength);
    }

    static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value;
    }

    static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive id.");
        }
        return value;
    }
}
