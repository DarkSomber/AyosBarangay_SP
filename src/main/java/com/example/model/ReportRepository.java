package com.example.model;

import java.util.List;

/**
 * Data access contract. The controller only knows about this interface,
 * will be swapped in-memory version for a JDBC/MySQL later
 * while not touching any view or controller
 */
public interface ReportRepository {

    List<Report> findAll();
}