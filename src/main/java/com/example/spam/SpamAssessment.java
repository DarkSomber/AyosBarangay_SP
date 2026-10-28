package com.example.spam;

import java.util.List;

public record SpamAssessment(List<String> reasons) {

    public SpamAssessment {
        reasons = List.copyOf(reasons);
    }

    public boolean flagged() {
        return !reasons.isEmpty();
    }
}
