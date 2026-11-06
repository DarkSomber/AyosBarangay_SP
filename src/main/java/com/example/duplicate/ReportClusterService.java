package com.example.duplicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.model.Report;
import com.example.model.ReportStatus;

@Service
public class ReportClusterService {

    static final double CLUSTER_SIMILARITY_THRESHOLD = 0.5;

    private final DuplicateReportService duplicates;

    public ReportClusterService(DuplicateReportService duplicates) {
        this.duplicates = duplicates;
    }

    public List<List<Report>> cluster(Collection<Report> reports) {
        List<Report> open = reports.stream()
                .filter(r -> !r.isDeleted())
                .filter(r -> r.status() == ReportStatus.PENDING || r.status() == ReportStatus.UNDER_REVIEW)
                .sorted(Comparator.comparing(Report::dateSubmitted))
                .toList();
        int size = open.size();
        List<String> descriptions = open.stream().map(r -> duplicates.normalize(r.description())).toList();
        List<Set<String>> words = descriptions.stream().map(duplicates::words).toList();
        List<String> addresses = open.stream().map(r -> duplicates.normalize(r.address())).toList();

        int[] parent = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }
        for (int i = 0; i < size; i++) {
            Report first = open.get(i);
            for (int j = i + 1; j < size; j++) {
                Report second = open.get(j);
                if (second.dateSubmitted().isAfter(
                        first.dateSubmitted().plusDays(DuplicateReportService.RECENT_WINDOW_DAYS))) {
                    break;
                }
                boolean similar = descriptions.get(i).equals(descriptions.get(j))
                        || duplicates.jaccard(words.get(i), words.get(j)) >= CLUSTER_SIMILARITY_THRESHOLD;
                if (similar && sameArea(first, second, addresses.get(i), addresses.get(j))) {
                    parent[find(parent, i)] = find(parent, j);
                }
            }
        }

        Map<Integer, List<Report>> groups = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            groups.computeIfAbsent(find(parent, i), root -> new ArrayList<>()).add(open.get(i));
        }
        return List.copyOf(groups.values());
    }

    private boolean sameArea(Report first, Report second, String firstAddress, String secondAddress) {
        double distance = duplicates.distanceMetres(first, second);
        return (!firstAddress.isEmpty() && firstAddress.equals(secondAddress))
                || (distance >= 0 && distance <= DuplicateReportService.LOCATION_RADIUS_METRES);
    }

    private static int find(int[] parent, int index) {
        while (parent[index] != index) {
            parent[index] = parent[parent[index]];
            index = parent[index];
        }
        return index;
    }
}
