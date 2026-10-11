package com.example.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.model.Report;
import com.example.model.ReportRepository;
import com.example.model.ReportStatus;
import com.example.model.SortOrder;

/**
 * Handles the logic between the view and the data: filtering and sorting.
 * No UI code lives here.
 */
@Service
public class ReportController {

    private final ReportRepository repository;

    public ReportController(ReportRepository repository) {
        this.repository = repository;
    }

    /** @param status filter by status, or null for all reports */
    public List<Report> getReports(ReportStatus status, SortOrder order) {
        Comparator<Report> byDate = Comparator.comparing(Report::dateSubmitted);
        if (order == SortOrder.NEWEST) {
            byDate = byDate.reversed();
        }
        return repository.findAll().stream()
                .filter(r -> !r.isDeleted())
                .filter(r -> status == null || r.status() == status)
                .sorted(byDate)
                .toList();
    }
}