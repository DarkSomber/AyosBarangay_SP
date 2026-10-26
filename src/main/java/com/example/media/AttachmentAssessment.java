package com.example.media;

import java.util.List;
import java.util.Set;

public record AttachmentAssessment(boolean attachmentProvided, List<AttachmentFinding> findings,
                                    Set<String> validPaths) {

    public AttachmentAssessment {
        findings = List.copyOf(findings);
        validPaths = Set.copyOf(validPaths);
    }

    public boolean hasWarnings() {
        return !findings.isEmpty();
    }
}
