package com.example.demo.duplicate.index;

import com.example.demo.duplicate.algorithm.impl.SimHashSimilarityCalculator;
import com.example.demo.duplicate.model.Article;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版 SimHash 倒排索引。
 */
public class InMemorySimHashIndex implements SimHashIndex {

    private static final int HASH_BITS = 64;
    private static final int SEGMENT_COUNT = 4;
    private static final int SEGMENT_BITS = HASH_BITS / SEGMENT_COUNT;

    private final ConcurrentHashMap<Long, BigInteger> fingerprints = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<Long>> segmentIndex = new ConcurrentHashMap<>();
    private final SimHashSimilarityCalculator calculator;

    public InMemorySimHashIndex() {
        this(new SimHashSimilarityCalculator());
    }

    public InMemorySimHashIndex(SimHashSimilarityCalculator calculator) {
        this.calculator = calculator;
    }

    @Override
    public synchronized void addArticle(Article article) {
        if (article == null || article.getId() == null) {
            return;
        }
        addArticle(article, calculator.getSimHash(article));
    }

    @Override
    public synchronized void addArticle(Article article, BigInteger fingerprint) {
        if (article == null || article.getId() == null || fingerprint == null) {
            return;
        }
        removeArticle(article.getId());
        fingerprints.put(article.getId(), fingerprint);
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            segmentIndex.computeIfAbsent(buildSegmentKey(i, extractSegment(fingerprint, i)),
                    key -> ConcurrentHashMap.newKeySet()).add(article.getId());
        }
    }

    @Override
    public synchronized void removeArticle(Long articleId) {
        if (articleId == null) {
            return;
        }
        BigInteger fingerprint = fingerprints.remove(articleId);
        if (fingerprint == null) {
            return;
        }
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            String key = buildSegmentKey(i, extractSegment(fingerprint, i));
            Set<Long> ids = segmentIndex.get(key);
            if (ids != null) {
                ids.remove(articleId);
                if (ids.isEmpty()) {
                    segmentIndex.remove(key);
                }
            }
        }
    }

    @Override
    public Map<Long, Integer> findCandidates(Article article, int maxHammingDistance) {
        if (article == null) {
            return Map.of();
        }
        return findCandidates(calculator.getSimHash(article), maxHammingDistance);
    }

    @Override
    public Map<Long, Integer> findCandidates(BigInteger fingerprint, int maxHammingDistance) {
        if (fingerprint == null) {
            return Map.of();
        }

        Set<Long> candidateIds = new HashSet<>();
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            String key = buildSegmentKey(i, extractSegment(fingerprint, i));
            candidateIds.addAll(segmentIndex.getOrDefault(key, Set.of()));
        }

        List<Map.Entry<Long, Integer>> ranked = new ArrayList<>();
        for (Long candidateId : candidateIds) {
            BigInteger candidateFingerprint = fingerprints.get(candidateId);
            if (candidateFingerprint == null) {
                continue;
            }
            int distance = calculateHammingDistance(fingerprint, candidateFingerprint);
            if (distance <= maxHammingDistance) {
                ranked.add(Map.entry(candidateId, distance));
            }
        }

        ranked.sort(Comparator.<Map.Entry<Long, Integer>, Integer>comparing(Map.Entry::getValue)
                .thenComparing(Map.Entry::getKey));

        Map<Long, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> entry : ranked) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public BigInteger getFingerprint(Long articleId) {
        return fingerprints.get(articleId);
    }

    @Override
    public boolean contains(Long articleId) {
        return fingerprints.containsKey(articleId);
    }

    @Override
    public int size() {
        return fingerprints.size();
    }

    @Override
    public synchronized void clear() {
        fingerprints.clear();
        segmentIndex.clear();
    }

    @Override
    public synchronized void save(Path path) throws IOException {
        if (path == null) {
            return;
        }
        List<String> lines = new ArrayList<>();
        for (Map.Entry<Long, BigInteger> entry : fingerprints.entrySet()) {
            lines.add(entry.getKey() + "," + entry.getValue().toString(16));
        }
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    @Override
    public synchronized void load(Path path) throws IOException {
        clear();
        if (path == null || !Files.exists(path)) {
            return;
        }
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            if (line == null || line.isBlank()) {
                continue;
            }
            String[] parts = line.split(",", 2);
            if (parts.length != 2) {
                continue;
            }
            Long articleId = Long.parseLong(parts[0].trim());
            BigInteger fingerprint = new BigInteger(parts[1].trim(), 16);
            Article article = new Article();
            article.setId(articleId);
            addArticle(article, fingerprint);
        }
    }

    private String buildSegmentKey(int segmentIndex, long value) {
        return segmentIndex + "_" + value;
    }

    private long extractSegment(BigInteger fingerprint, int segmentIndex) {
        BigInteger mask = BigInteger.ONE.shiftLeft(SEGMENT_BITS).subtract(BigInteger.ONE);
        return fingerprint.shiftRight(segmentIndex * SEGMENT_BITS).and(mask).longValue();
    }

    private int calculateHammingDistance(BigInteger fingerprint1, BigInteger fingerprint2) {
        return fingerprint1.xor(fingerprint2).bitCount();
    }
}
