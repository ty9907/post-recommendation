package com.example.demo.duplicate.sync;

import com.example.demo.duplicate.index.SimHashIndex;
import com.example.demo.duplicate.index.TagInvertedIndex;
import com.example.demo.duplicate.model.Article;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 索引同步服务。
 */
public class IndexSyncService {

    private final SimHashIndex simHashIndex;
    private final TagInvertedIndex tagInvertedIndex;
    private final Map<Long, Article> articleStore = new ConcurrentHashMap<>();

    public IndexSyncService(SimHashIndex simHashIndex, TagInvertedIndex tagInvertedIndex) {
        this.simHashIndex = simHashIndex;
        this.tagInvertedIndex = tagInvertedIndex;
    }

    public synchronized void rebuildIndexes(Collection<Article> articles) {
        clear();
        if (articles == null) {
            return;
        }
        for (Article article : articles) {
            onArticleAdded(article);
        }
    }

    public synchronized void onArticleAdded(Article article) {
        if (article == null || article.getId() == null) {
            return;
        }
        articleStore.put(article.getId(), article);
        simHashIndex.addArticle(article);
        tagInvertedIndex.addArticle(article);
    }

    public synchronized void onArticleUpdated(Article article) {
        if (article == null || article.getId() == null) {
            return;
        }
        onArticleDeleted(article.getId());
        onArticleAdded(article);
    }

    public synchronized void onArticleDeleted(Long articleId) {
        if (articleId == null) {
            return;
        }
        articleStore.remove(articleId);
        simHashIndex.removeArticle(articleId);
        tagInvertedIndex.removeArticle(articleId);
    }

    public List<Article> getArticlesByIds(Collection<Long> articleIds) {
        List<Article> result = new ArrayList<>();
        if (articleIds == null) {
            return result;
        }
        for (Long articleId : articleIds) {
            Article article = articleStore.get(articleId);
            if (article != null) {
                result.add(article);
            }
        }
        return result;
    }

    public List<Article> getAllArticles() {
        return new ArrayList<>(articleStore.values());
    }

    public SimHashIndex getSimHashIndex() {
        return simHashIndex;
    }

    public TagInvertedIndex getTagInvertedIndex() {
        return tagInvertedIndex;
    }

    public synchronized void clear() {
        articleStore.clear();
        simHashIndex.clear();
        tagInvertedIndex.clear();
    }
}
