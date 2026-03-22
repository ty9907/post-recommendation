package com.example.demo.duplicate.service;

import com.example.demo.duplicate.model.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 相似度缓存服务
 * 提供文章相似度和文章数据的缓存功能
 * 使用 ConcurrentHashMap 实现缓存，支持定时清理过期数据
 */
public class SimilarityCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(SimilarityCacheService.class);
    
    // 相似度缓存：Key格式为 "articleId1-articleId2"（小的ID在前）
    private final ConcurrentHashMap<String, CacheEntry<Double>> similarityCache;
    
    // 文章缓存：Key为文章ID
    private final ConcurrentHashMap<Long, CacheEntry<Article>> articleCache;
    
    // 相似度缓存配置
    private static final int SIMILARITY_CACHE_MAX_SIZE = 10000;
    private static final long SIMILARITY_CACHE_EXPIRE_HOURS = 24;
    
    // 文章缓存配置
    private static final int ARTICLE_CACHE_MAX_SIZE = 1000;
    private static final long ARTICLE_CACHE_EXPIRE_HOURS = 1;
    
    // 定时清理任务执行器
    private final ScheduledExecutorService scheduler;
    
    // 缓存统计信息
    private long similarityCacheHits = 0;
    private long similarityCacheMisses = 0;
    private long articleCacheHits = 0;
    private long articleCacheMisses = 0;
    
    /**
     * 缓存条目内部类
     * @param <T> 缓存值类型
     */
    private static class CacheEntry<T> {
        private final T value;
        private final long createTime;
        private final long expireMillis;
        
        public CacheEntry(T value, long expireHours) {
            this.value = value;
            this.createTime = System.currentTimeMillis();
            this.expireMillis = expireHours * 60 * 60 * 1000;
        }
        
        public T getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - createTime > expireMillis;
        }
    }
    
    /**
     * 构造器
     * 初始化缓存并启动定时清理任务
     */
    public SimilarityCacheService() {
        this.similarityCache = new ConcurrentHashMap<>();
        this.articleCache = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "similarity-cache-cleaner");
            thread.setDaemon(true);
            return thread;
        });
        
        // 启动定时清理任务，每10分钟执行一次
        scheduler.scheduleAtFixedRate(this::cleanExpiredEntries, 10, 10, TimeUnit.MINUTES);
        
        logger.info("相似度缓存服务初始化完成，相似度缓存容量：{}，文章缓存容量：{}", 
                SIMILARITY_CACHE_MAX_SIZE, ARTICLE_CACHE_MAX_SIZE);
    }
    
    /**
     * 生成相似度缓存的Key
     * 确保小的ID在前，大的ID在后
     * @param articleId1 文章ID1
     * @param articleId2 文章ID2
     * @return 缓存Key
     */
    private String generateSimilarityKey(Long articleId1, Long articleId2) {
        long minId = Math.min(articleId1, articleId2);
        long maxId = Math.max(articleId1, articleId2);
        return minId + "-" + maxId;
    }
    
    /**
     * 获取两篇文章的相似度
     * @param articleId1 文章ID1
     * @param articleId2 文章ID2
     * @return 相似度值，如果不存在则返回null
     */
    public Double getSimilarity(Long articleId1, Long articleId2) {
        String key = generateSimilarityKey(articleId1, articleId2);
        CacheEntry<Double> entry = similarityCache.get(key);
        
        if (entry != null && !entry.isExpired()) {
            similarityCacheHits++;
            logger.debug("命中相似度缓存：articleId1={}, articleId2={}, similarity={}", 
                    articleId1, articleId2, entry.getValue());
            return entry.getValue();
        }
        
        similarityCacheMisses++;
        if (entry != null) {
            // 条目已过期，移除
            similarityCache.remove(key);
            logger.debug("相似度缓存条目已过期并移除：key={}", key);
        }
        
        return null;
    }
    
    /**
     * 缓存两篇文章的相似度
     * @param articleId1 文章ID1
     * @param articleId2 文章ID2
     * @param similarity 相似度值
     */
    public void putSimilarity(Long articleId1, Long articleId2, Double similarity) {
        // 检查缓存容量，如果超过最大值则清理部分条目
        if (similarityCache.size() >= SIMILARITY_CACHE_MAX_SIZE) {
            cleanOldestEntries(similarityCache, SIMILARITY_CACHE_MAX_SIZE / 10);
            logger.info("相似度缓存已满，清理了部分旧条目");
        }
        
        String key = generateSimilarityKey(articleId1, articleId2);
        similarityCache.put(key, new CacheEntry<>(similarity, SIMILARITY_CACHE_EXPIRE_HOURS));
        
        logger.debug("缓存相似度：articleId1={}, articleId2={}, similarity={}", 
                articleId1, articleId2, similarity);
    }
    
    /**
     * 获取文章
     * @param id 文章ID
     * @return 文章对象，如果不存在则返回null
     */
    public Article getArticle(Long id) {
        CacheEntry<Article> entry = articleCache.get(id);
        
        if (entry != null && !entry.isExpired()) {
            articleCacheHits++;
            logger.debug("命中文章缓存：articleId={}", id);
            return entry.getValue();
        }
        
        articleCacheMisses++;
        if (entry != null) {
            // 条目已过期，移除
            articleCache.remove(id);
            logger.debug("文章缓存条目已过期并移除：articleId={}", id);
        }
        
        return null;
    }
    
    /**
     * 缓存文章
     * @param id 文章ID
     * @param article 文章对象
     */
    public void putArticle(Long id, Article article) {
        // 检查缓存容量，如果超过最大值则清理部分条目
        if (articleCache.size() >= ARTICLE_CACHE_MAX_SIZE) {
            cleanOldestEntries(articleCache, ARTICLE_CACHE_MAX_SIZE / 10);
            logger.info("文章缓存已满，清理了部分旧条目");
        }
        
        articleCache.put(id, new CacheEntry<>(article, ARTICLE_CACHE_EXPIRE_HOURS));
        
        logger.debug("缓存文章：articleId={}, title={}", id, 
                article != null ? article.getTitle() : null);
    }
    
    /**
     * 清空所有缓存
     */
    public void clear() {
        int similaritySize = similarityCache.size();
        int articleSize = articleCache.size();
        
        similarityCache.clear();
        articleCache.clear();
        
        // 重置统计信息
        similarityCacheHits = 0;
        similarityCacheMisses = 0;
        articleCacheHits = 0;
        articleCacheMisses = 0;
        
        logger.info("已清空所有缓存：相似度缓存{}条，文章缓存{}条", similaritySize, articleSize);
    }
    
    /**
     * 清空相似度缓存
     */
    public void clearSimilarityCache() {
        int size = similarityCache.size();
        similarityCache.clear();
        
        // 重置统计信息
        similarityCacheHits = 0;
        similarityCacheMisses = 0;
        
        logger.info("已清空相似度缓存：{}条", size);
    }
    
    /**
     * 清空文章缓存
     */
    public void clearArticleCache() {
        int size = articleCache.size();
        articleCache.clear();
        
        // 重置统计信息
        articleCacheHits = 0;
        articleCacheMisses = 0;
        
        logger.info("已清空文章缓存：{}条", size);
    }
    
    /**
     * 获取缓存统计信息
     * @return 包含缓存统计信息的Map
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 相似度缓存统计
        Map<String, Object> similarityStats = new HashMap<>();
        similarityStats.put("size", similarityCache.size());
        similarityStats.put("maxSize", SIMILARITY_CACHE_MAX_SIZE);
        similarityStats.put("hits", similarityCacheHits);
        similarityStats.put("misses", similarityCacheMisses);
        similarityStats.put("hitRate", calculateHitRate(similarityCacheHits, similarityCacheMisses));
        similarityStats.put("expireHours", SIMILARITY_CACHE_EXPIRE_HOURS);
        
        // 文章缓存统计
        Map<String, Object> articleStats = new HashMap<>();
        articleStats.put("size", articleCache.size());
        articleStats.put("maxSize", ARTICLE_CACHE_MAX_SIZE);
        articleStats.put("hits", articleCacheHits);
        articleStats.put("misses", articleCacheMisses);
        articleStats.put("hitRate", calculateHitRate(articleCacheHits, articleCacheMisses));
        articleStats.put("expireHours", ARTICLE_CACHE_EXPIRE_HOURS);
        
        stats.put("similarityCache", similarityStats);
        stats.put("articleCache", articleStats);
        
        return stats;
    }
    
    /**
     * 计算缓存命中率
     * @param hits 命中次数
     * @param misses 未命中次数
     * @return 命中率（百分比）
     */
    private double calculateHitRate(long hits, long misses) {
        long total = hits + misses;
        if (total == 0) {
            return 0.0;
        }
        return (double) hits / total * 100;
    }
    
    /**
     * 清理过期的缓存条目
     */
    private void cleanExpiredEntries() {
        logger.debug("开始清理过期的缓存条目...");
        
        // 清理相似度缓存
        int similarityRemoved = cleanExpiredEntries(similarityCache);
        
        // 清理文章缓存
        int articleRemoved = cleanExpiredEntries(articleCache);
        
        if (similarityRemoved > 0 || articleRemoved > 0) {
            logger.info("清理完成：相似度缓存移除{}条，文章缓存移除{}条", 
                    similarityRemoved, articleRemoved);
        }
    }
    
    /**
     * 清理指定缓存中的过期条目
     * @param cache 缓存对象
     * @return 移除的条目数量
     */
    private <K, T> int cleanExpiredEntries(ConcurrentHashMap<K, CacheEntry<T>> cache) {
        int removed = 0;
        var iterator = cache.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }
    
    /**
     * 清理最旧的缓存条目
     * @param cache 缓存对象
     * @param count 要清理的数量
     */
    private <K, T> void cleanOldestEntries(ConcurrentHashMap<K, CacheEntry<T>> cache, int count) {
        // 简单实现：随机移除部分条目
        // 在实际生产环境中，可以考虑使用LRU策略
        var iterator = cache.keySet().iterator();
        int removed = 0;
        while (iterator.hasNext() && removed < count) {
            iterator.next();
            iterator.remove();
            removed++;
        }
    }
    
    /**
     * 关闭缓存服务
     * 停止定时清理任务
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("相似度缓存服务已关闭");
    }
}
