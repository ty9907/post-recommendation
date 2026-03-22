package com.example.demo.duplicate.candidate;

import com.example.demo.duplicate.model.Article;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 候选集筛选结果。
 */
public class CandidateSelection {

    private final List<Article> candidates;
    private final Map<Long, Integer> hammingDistances;
    private final Map<Long, Integer> sharedTagCounts;
    private final boolean cacheHit;
    private final int simHashCandidateCount;
    private final int tagCandidateCount;

    public CandidateSelection(List<Article> candidates,
                              Map<Long, Integer> hammingDistances,
                              Map<Long, Integer> sharedTagCounts,
                              boolean cacheHit,
                              int simHashCandidateCount,
                              int tagCandidateCount) {
        this.candidates = candidates != null ? new ArrayList<>(candidates) : new ArrayList<>();
        this.hammingDistances = hammingDistances != null ? new LinkedHashMap<>(hammingDistances) : new LinkedHashMap<>();
        this.sharedTagCounts = sharedTagCounts != null ? new LinkedHashMap<>(sharedTagCounts) : new LinkedHashMap<>();
        this.cacheHit = cacheHit;
        this.simHashCandidateCount = simHashCandidateCount;
        this.tagCandidateCount = tagCandidateCount;
    }

    public List<Article> getCandidates() {
        return new ArrayList<>(candidates);
    }

    public Map<Long, Integer> getHammingDistances() {
        return new LinkedHashMap<>(hammingDistances);
    }

    public Map<Long, Integer> getSharedTagCounts() {
        return new LinkedHashMap<>(sharedTagCounts);
    }

    public boolean isCacheHit() {
        return cacheHit;
    }

    public int getSimHashCandidateCount() {
        return simHashCandidateCount;
    }

    public int getTagCandidateCount() {
        return tagCandidateCount;
    }

    public Map<String, Object> toDiagnostics() {
        Map<String, Object> diagnostics = new LinkedHashMap<>();
        diagnostics.put("cacheHit", cacheHit);
        diagnostics.put("simHashCandidateCount", simHashCandidateCount);
        diagnostics.put("tagCandidateCount", tagCandidateCount);
        diagnostics.put("finalCandidateCount", candidates.size());
        diagnostics.put("hammingDistances", getHammingDistances());
        diagnostics.put("sharedTagCounts", getSharedTagCounts());
        diagnostics.put("candidateIds", candidates.stream().map(Article::getId).toList());
        return diagnostics;
    }
}
