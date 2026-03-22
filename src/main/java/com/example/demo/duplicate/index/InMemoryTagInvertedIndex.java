package com.example.demo.duplicate.index;

import com.example.demo.duplicate.model.Article;
import com.example.demo.tag.model.Tag;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版标签倒排索引。
 */
public class InMemoryTagInvertedIndex implements TagInvertedIndex {

    private final ConcurrentHashMap<String, Set<Long>> tagToArticleIds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Set<String>> articleToTags = new ConcurrentHashMap<>();

    @Override
    public synchronized void addArticle(Article article) {
        if (article == null || article.getId() == null) {
            return;
        }

        removeArticle(article.getId());

        Set<String> tags = extractTagNames(article);
        articleToTags.put(article.getId(), tags);
        for (String tag : tags) {
            tagToArticleIds.computeIfAbsent(tag, key -> ConcurrentHashMap.newKeySet()).add(article.getId());
        }
    }

    @Override
    public synchronized void removeArticle(Long articleId) {
        if (articleId == null) {
            return;
        }

        Set<String> tags = articleToTags.remove(articleId);
        if (tags == null) {
            return;
        }

        for (String tag : tags) {
            Set<Long> ids = tagToArticleIds.get(tag);
            if (ids != null) {
                ids.remove(articleId);
                if (ids.isEmpty()) {
                    tagToArticleIds.remove(tag);
                }
            }
        }
    }

    @Override
    public Set<Long> findByTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return Collections.emptySet();
        }
        return new HashSet<>(tagToArticleIds.getOrDefault(normalize(tag), Set.of()));
    }

    @Override
    public Set<Long> findByAnyTags(Collection<String> tags) {
        return findByAnyTagsWithCount(tags).keySet();
    }

    @Override
    public Map<Long, Integer> findByAnyTagsWithCount(Collection<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Integer> result = new HashMap<>();
        for (String tag : tags) {
            for (Long articleId : findByTag(tag)) {
                result.merge(articleId, 1, Integer::sum);
            }
        }
        return result;
    }

    @Override
    public boolean containsArticle(Long articleId) {
        return articleToTags.containsKey(articleId);
    }

    @Override
    public int size() {
        return articleToTags.size();
    }

    @Override
    public synchronized void clear() {
        tagToArticleIds.clear();
        articleToTags.clear();
    }

    private Set<String> extractTagNames(Article article) {
        if (article.getTags() == null || article.getTags().isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> result = new HashSet<>();
        for (Tag tag : article.getTags()) {
            if (tag != null && tag.getName() != null && !tag.getName().isBlank()) {
                result.add(normalize(tag.getName()));
            }
        }
        return result;
    }

    private String normalize(String tag) {
        return tag.trim().toLowerCase();
    }
}
