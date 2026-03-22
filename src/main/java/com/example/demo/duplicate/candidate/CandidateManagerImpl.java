package com.example.demo.duplicate.candidate;

import com.example.demo.duplicate.algorithm.impl.SimHashSimilarityCalculator;
import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.index.InMemorySimHashIndex;
import com.example.demo.duplicate.index.InMemoryTagInvertedIndex;
import com.example.demo.duplicate.index.SimHashIndex;
import com.example.demo.duplicate.index.TagInvertedIndex;
import com.example.demo.duplicate.model.Article;
import com.example.demo.tag.model.Tag;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 候选集管理器实现。
 */
public class CandidateManagerImpl implements CandidateManager {

    private final SimHashIndex simHashIndex;
    private final TagInvertedIndex tagInvertedIndex;
    private final SimHashSimilarityCalculator simHashCalculator;
    private final ConcurrentHashMap<Long, Article> articleStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CachedSelection> candidateCache = new ConcurrentHashMap<>();

    private static final class CachedSelection {
        private final CandidateSelection selection;
        private final long createTime;

        private CachedSelection(CandidateSelection selection) {
            this.selection = selection;
            this.createTime = System.currentTimeMillis();
        }
    }

    public CandidateManagerImpl() {
        this(new InMemorySimHashIndex(), new InMemoryTagInvertedIndex(), new SimHashSimilarityCalculator());
    }

    public CandidateManagerImpl(SimHashIndex simHashIndex,
                                TagInvertedIndex tagInvertedIndex,
                                SimHashSimilarityCalculator simHashCalculator) {
        this.simHashIndex = simHashIndex;
        this.tagInvertedIndex = tagInvertedIndex;
        this.simHashCalculator = simHashCalculator;
    }

    @Override
    public CandidateSelection selectCandidates(Article article, List<Article> existingArticles, DuplicateCheckConfig config) {
        DuplicateCheckConfig useConfig = config != null ? config : DuplicateCheckConfig.defaultConfig();
        if (article == null) {
            return new CandidateSelection(List.of(), Map.of(), Map.of(), false, 0, 0);
        }

        if (existingArticles != null) {
            warmUp(existingArticles);
        }

        String cacheKey = buildCacheKey(article, useConfig);
        CandidateSelection cached = getCachedSelection(cacheKey, useConfig);
        if (cached != null) {
            return cached;
        }

        Map<Long, Integer> hammingDistances = simHashIndex.findCandidates(article, useConfig.getSimHashHammingThreshold());
        Map<Long, Integer> sharedTagCounts = tagInvertedIndex.findByAnyTagsWithCount(extractTagNames(article));

        Set<Long> orderedIds = new LinkedHashSet<>();
        if (!hammingDistances.isEmpty()) {
            orderedIds.addAll(hammingDistances.keySet());
        }
        if (!sharedTagCounts.isEmpty()) {
            if (orderedIds.isEmpty()) {
                orderedIds.addAll(sharedTagCounts.keySet());
            } else {
                orderedIds.retainAll(sharedTagCounts.keySet());
            }
        }

        if (orderedIds.isEmpty() && useConfig.isEnableFullScanFallback()) {
            orderedIds.addAll(articleStore.keySet());
        }
        orderedIds.remove(article.getId());

        List<Long> rankedIds = orderedIds.stream()
                .sorted(buildCandidateComparator(sharedTagCounts, hammingDistances))
                .limit(Math.max(1, useConfig.getMaxCandidateSize()))
                .toList();

        List<Article> candidates = new ArrayList<>();
        for (Long candidateId : rankedIds) {
            Article candidate = articleStore.get(candidateId);
            if (candidate != null) {
                candidates.add(candidate);
            }
        }

        CandidateSelection selection = new CandidateSelection(
                candidates,
                filterMapByIds(hammingDistances, rankedIds),
                filterMapByIds(sharedTagCounts, rankedIds),
                false,
                hammingDistances.size(),
                sharedTagCounts.size()
        );
        candidateCache.put(cacheKey, new CachedSelection(selection));
        return selection;
    }

    @Override
    public synchronized void warmUp(List<Article> articles) {
        clear();
        if (articles == null || articles.isEmpty()) {
            return;
        }
        for (Article article : articles) {
            if (article == null || article.getId() == null) {
                continue;
            }
            articleStore.put(article.getId(), article);
            simHashIndex.addArticle(article, simHashCalculator.getSimHash(article));
            tagInvertedIndex.addArticle(article);
        }
    }

    @Override
    public synchronized void removeArticle(Long articleId) {
        if (articleId == null) {
            return;
        }
        articleStore.remove(articleId);
        simHashIndex.removeArticle(articleId);
        tagInvertedIndex.removeArticle(articleId);
        candidateCache.clear();
    }

    @Override
    public synchronized void clear() {
        articleStore.clear();
        simHashIndex.clear();
        tagInvertedIndex.clear();
        candidateCache.clear();
    }

    @Override
    public SimHashIndex getSimHashIndex() {
        return simHashIndex;
    }

    @Override
    public TagInvertedIndex getTagInvertedIndex() {
        return tagInvertedIndex;
    }

    private CandidateSelection getCachedSelection(String cacheKey, DuplicateCheckConfig config) {
        CachedSelection cachedSelection = candidateCache.get(cacheKey);
        if (cachedSelection == null) {
            return null;
        }
        long expireMillis = Math.max(1, config.getCandidateCacheExpireMinutes()) * 60_000L;
        if (System.currentTimeMillis() - cachedSelection.createTime > expireMillis) {
            candidateCache.remove(cacheKey);
            return null;
        }
        CandidateSelection selection = cachedSelection.selection;
        return new CandidateSelection(
                selection.getCandidates(),
                selection.getHammingDistances(),
                selection.getSharedTagCounts(),
                true,
                selection.getSimHashCandidateCount(),
                selection.getTagCandidateCount()
        );
    }

    private Comparator<Long> buildCandidateComparator(Map<Long, Integer> sharedTagCounts,
                                                      Map<Long, Integer> hammingDistances) {
        return Comparator.<Long, Integer>comparing(id -> sharedTagCounts.getOrDefault(id, 0), Comparator.reverseOrder())
                .thenComparing(id -> hammingDistances.getOrDefault(id, Integer.MAX_VALUE))
                .thenComparing(Long::longValue);
    }

    private Map<Long, Integer> filterMapByIds(Map<Long, Integer> source, Collection<Long> ids) {
        Map<Long, Integer> result = new LinkedHashMap<>();
        for (Long id : ids) {
            if (source.containsKey(id)) {
                result.put(id, source.get(id));
            }
        }
        return result;
    }

    private String buildCacheKey(Article article, DuplicateCheckConfig config) {
        BigInteger fingerprint = simHashCalculator.getSimHash(article);
        List<String> tags = new ArrayList<>(extractTagNames(article));
        tags.sort(String::compareTo);
        return fingerprint.toString(16)
                + "|" + tags
                + "|" + config.getSimHashHammingThreshold()
                + "|" + config.getMaxCandidateSize();
    }

    private List<String> extractTagNames(Article article) {
        List<String> tags = new ArrayList<>();
        if (article == null || article.getTags() == null) {
            return tags;
        }
        for (Tag tag : article.getTags()) {
            if (tag != null && tag.getName() != null && !tag.getName().isBlank()) {
                tags.add(tag.getName().trim().toLowerCase());
            }
        }
        return tags;
    }
}
