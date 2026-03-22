package com.example.demo.duplicate.monitor;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 检测性能指标。
 */
public class PerformanceMetrics {

    private final LocalDateTime createTime = LocalDateTime.now();
    private final Map<String, Long> stageDurations = new LinkedHashMap<>();
    private final Map<String, Integer> counters = new LinkedHashMap<>();
    private long totalDurationMillis;

    public void recordStage(String stage, long durationMillis) {
        if (stage != null) {
            stageDurations.put(stage, Math.max(0L, durationMillis));
        }
    }

    public void recordCount(String name, int value) {
        if (name != null) {
            counters.put(name, Math.max(0, value));
        }
    }

    public Map<String, Long> getStageDurations() {
        return new LinkedHashMap<>(stageDurations);
    }

    public Map<String, Integer> getCounters() {
        return new LinkedHashMap<>(counters);
    }

    public long getTotalDurationMillis() {
        return totalDurationMillis;
    }

    public void setTotalDurationMillis(long totalDurationMillis) {
        this.totalDurationMillis = Math.max(0L, totalDurationMillis);
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("createTime", createTime);
        result.put("stageDurations", getStageDurations());
        result.put("counters", getCounters());
        result.put("totalDurationMillis", totalDurationMillis);
        return result;
    }
}
