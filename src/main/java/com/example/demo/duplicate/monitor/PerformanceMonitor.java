package com.example.demo.duplicate.monitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 性能监控器。
 */
public class PerformanceMonitor {

    private final Queue<PerformanceMetrics> metricsQueue = new ConcurrentLinkedQueue<>();
    private final int maxSamples;

    public PerformanceMonitor() {
        this(1_000);
    }

    public PerformanceMonitor(int maxSamples) {
        this.maxSamples = Math.max(10, maxSamples);
    }

    public void record(PerformanceMetrics metrics) {
        if (metrics == null) {
            return;
        }
        metricsQueue.offer(metrics);
        while (metricsQueue.size() > maxSamples) {
            metricsQueue.poll();
        }
    }

    public List<PerformanceMetrics> getSamples() {
        return new ArrayList<>(metricsQueue);
    }

    public Map<String, Object> generateReport() {
        List<PerformanceMetrics> samples = getSamples();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("sampleCount", samples.size());
        if (samples.isEmpty()) {
            report.put("averageTotalDurationMillis", 0.0);
            report.put("p99TotalDurationMillis", 0L);
            return report;
        }

        List<Long> totalDurations = samples.stream()
                .map(PerformanceMetrics::getTotalDurationMillis)
                .sorted(Comparator.naturalOrder())
                .toList();

        double average = totalDurations.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long p99 = totalDurations.get(Math.min(totalDurations.size() - 1,
                Math.max(0, (int) Math.ceil(totalDurations.size() * 0.99) - 1)));

        report.put("averageTotalDurationMillis", average);
        report.put("p99TotalDurationMillis", p99);
        return report;
    }

    public void clear() {
        metricsQueue.clear();
    }
}
