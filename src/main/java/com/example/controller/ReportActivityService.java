package com.example.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.example.model.ReportActivity;
import com.example.model.ReportActivityRepository;
import com.example.model.User;
import com.example.model.UserRepository;

@Service
public class ReportActivityService {

    private static final Logger log = LoggerFactory.getLogger(ReportActivityService.class);

    private final ReportActivityRepository repository;
    private final UserRepository users;

    public ReportActivityService(ReportActivityRepository repository, UserRepository users) {
        this.repository = repository;
        this.users = users;
    }

    public void record(ReportActivity activity) {
        try {
            repository.save(activity);
        } catch (DataAccessException ex) {
            log.warn("Could not record report activity. Is the report_activities table created? {}",
                    ex.getMessage());
        }
    }

    public List<ReportActivity> forReport(int reportId) {
        try {
            return repository.findByReport(reportId);
        } catch (DataAccessException ex) {
            log.warn("Could not read report activity: {}", ex.getMessage());
            return List.of();
        }
    }

    public String adminName(Integer adminId) {
        if (adminId == null) {
            return "Barangay admin";
        }
        return users.findAllByIds(List.of(adminId)).stream().findFirst()
                .map(User::fullName).orElse("Barangay admin");
    }
}