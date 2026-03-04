package com.example.demo.duplicate.detector.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.detector.DuplicateDetector;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.DuplicateCheckReport;
import com.example.demo.duplicate.model.SimilarityResult;
import com.example.demo.duplicate.repository.ArticleRepository;
import com.example.demo.duplicate.service.SimilarityCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 实时检测器
 * 
 * 快速响应、低延迟的重复文章检测器。
 * 适用于用户发布文章时的实时检测场景。
 * 
 * 特点：
 * 1. 快速响应：优先使用缓存，减少重复计算
 * 2. 低延迟：只检测近期文章，控制检测范围
 * 3. 线程安全：支持并发调用
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
public class RealTimeDetector implements DuplicateDetector {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeDetector.class);

    private static final String DETECTOR_NAME = "RealTimeDetector";

    private final SimilarityCalculator similarityCalculator;
    private final ArticleRepository articleRepository;
    private final DuplicateCheckConfig config;
    private final SimilarityCacheService cacheService;

    /**
     * 构造器
     * 
     * @param similarityCalculator 相似度计算器
     * @param articleRepository 文章仓储
     * @param config 检测配置
     * @param cacheService 缓存服务
     */
    public RealTimeDetector(SimilarityCalculator similarityCalculator,
                           ArticleRepository articleRepository,
                           DuplicateCheckConfig config,
                           SimilarityCacheService cacheService) {
        this.similarityCalculator = Objects.requireNonNull(similarityCalculator, "相似度计算器不能为空");
        this.articleRepository = Objects.requireNonNull(articleRepository, "文章仓储不能为空");
        this.config = config != null ? config : DuplicateCheckConfig.defaultConfig();
        this.cacheService = cacheService;
        
        logger.info("实时检测器初始化完成，算法：{}，阈值：{}", 
                similarityCalculator.getName(), this.config.getThreshold());
    }

    /**
     * 检测文章是否与已有文章重复
     * 
     * @param article 待检测的文章
     * @param config 检测配置
     * @return 检测报告
     */
    @Override
    public DuplicateCheckReport detect(Article article, DuplicateCheckConfig config) {
        long startTime = System.currentTimeMillis();
        DuplicateCheckConfig useConfig = config != null ? config : this.config;
        
        logger.info("开始实时检测文章，文章ID：{}，标题：{}", 
                article.getId(), article.getTitle());
        
        DuplicateCheckReport report = new DuplicateCheckReport();
        report.setArticleId(article.getId());
        report.setCheckTime(LocalDateTime.now());
        
        try {
            List<Article> recentArticles = getRecentArticles(article, useConfig);
            
            logger.debug("获取到{}篇近期文章用于比较", recentArticles.size());
            
            if (recentArticles.isEmpty()) {
                report.setHasDuplicate(false);
                report.setSummary("无近期文章可比较，检测通过");
                logger.info("无近期文章可比较，文章ID：{} 检测通过", article.getId());
                return report;
            }
            
            List<SimilarityResult> results = calculateSimilarities(article, recentArticles, useConfig);
            
            results.sort(Comparator.comparingDouble(SimilarityResult::getSimilarity).reversed());
            
            int maxResults = useConfig.getMaxResults();
            if (results.size() > maxResults) {
                results = results.subList(0, maxResults);
            }
            
            for (SimilarityResult result : results) {
                report.addResult(result);
            }
            
            boolean hasDuplicate = results.stream()
                    .anyMatch(r -> r.getSimilarity() >= useConfig.getThreshold());
            
            report.setHasDuplicate(hasDuplicate);
            report.setSummary(generateSummary(results, hasDuplicate, useConfig));
            
            long elapsed = System.currentTimeMillis() - startTime;
            logger.info("实时检测完成，文章ID：{}，是否存在重复：{}，耗时：{}ms，比较文章数：{}", 
                    article.getId(), hasDuplicate, elapsed, recentArticles.size());
            
            if (hasDuplicate) {
                logger.warn("检测到重复文章，文章ID：{}，最高相似度：{}", 
                        article.getId(), results.get(0).getSimilarity());
            }
            
        } catch (Exception e) {
            logger.error("实时检测发生异常，文章ID：{}", article.getId(), e);
            report.setHasDuplicate(false);
            report.setSummary("检测过程中发生异常：" + e.getMessage());
        }
        
        return report;
    }

    /**
     * 批量检测文章是否重复
     * 实时检测器逐篇处理，保证低延迟
     * 
     * @param articles 待检测的文章列表
     * @param config 检测配置
     * @return 检测报告列表
     */
    @Override
    public List<DuplicateCheckReport> batchDetect(List<Article> articles, DuplicateCheckConfig config) {
        long startTime = System.currentTimeMillis();
        DuplicateCheckConfig useConfig = config != null ? config : this.config;
        
        logger.info("实时检测器开始批量检测，文章数量：{}", articles.size());
        
        List<DuplicateCheckReport> reports = new ArrayList<>();
        
        for (Article article : articles) {
            DuplicateCheckReport report = detect(article, useConfig);
            reports.add(report);
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("实时检测器批量检测完成，文章数量：{}，耗时：{}ms", articles.size(), elapsed);
        
        return reports;
    }

    /**
     * 查找与指定文章相似的文章
     * 
     * @param article 待检测的文章
     * @param config 检测配置
     * @return 相似度结果列表
     */
    @Override
    public List<SimilarityResult> findSimilarArticles(Article article, DuplicateCheckConfig config) {
        DuplicateCheckConfig useConfig = config != null ? config : this.config;
        
        List<Article> recentArticles = getRecentArticles(article, useConfig);
        
        if (recentArticles.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<SimilarityResult> results = calculateSimilarities(article, recentArticles, useConfig);
        
        results.sort(Comparator.comparingDouble(SimilarityResult::getSimilarity).reversed());
        
        int maxResults = useConfig.getMaxResults();
        if (results.size() > maxResults) {
            results = results.subList(0, maxResults);
        }
        
        return results;
    }

    /**
     * 判断两篇文章是否重复
     * 
     * @param article1 第一篇文章
     * @param article2 第二篇文章
     * @param threshold 相似度阈值
     * @return true表示重复，false表示不重复
     */
    @Override
    public boolean isDuplicate(Article article1, Article article2, double threshold) {
        if (article1 == null || article2 == null) {
            return false;
        }
        double similarity = similarityCalculator.calculateSimilarity(article1, article2);
        boolean isDuplicate = similarity >= threshold;
        
        logger.debug("判断两篇文章是否重复，文章ID1：{}，文章ID2：{}，相似度：{}，阈值：{}，结果：{}", 
                article1.getId(), article2.getId(), similarity, threshold, isDuplicate);
        
        return isDuplicate;
    }

    /**
     * 获取检测器名称
     * 
     * @return 检测器名称
     */
    @Override
    public String getName() {
        return DETECTOR_NAME;
    }

    /**
     * 获取近期文章列表
     * 排除当前正在检测的文章
     * 
     * @param currentArticle 当前文章
     * @param config 检测配置
     * @return 近期文章列表
     */
    private List<Article> getRecentArticles(Article currentArticle, DuplicateCheckConfig config) {
        List<Article> recentArticles = articleRepository.findRecentArticles(config.getRecentDays());
        
        List<Article> filteredArticles = new ArrayList<>();
        for (Article article : recentArticles) {
            if (!article.getId().equals(currentArticle.getId())) {
                filteredArticles.add(article);
            }
        }
        
        return filteredArticles;
    }

    /**
     * 计算文章与近期文章的相似度
     * 优先使用缓存
     * 
     * @param article 待检测文章
     * @param recentArticles 近期文章列表
     * @param config 检测配置
     * @return 相似度结果列表
     */
    private List<SimilarityResult> calculateSimilarities(Article article, List<Article> recentArticles, 
                                                         DuplicateCheckConfig config) {
        List<SimilarityResult> results = new ArrayList<>();
        
        for (Article recentArticle : recentArticles) {
            double similarity = calculateSimilarityWithCache(article, recentArticle, config);
            
            SimilarityResult result = new SimilarityResult();
            result.setArticleId(article.getId());
            result.setComparedArticleId(recentArticle.getId());
            result.setSimilarity(similarity);
            result.setAlgorithm(similarityCalculator.getName());
            result.setCheckTime(LocalDateTime.now());
            
            results.add(result);
        }
        
        return results;
    }

    /**
     * 计算两篇文章的相似度（带缓存）
     * 
     * @param article1 文章1
     * @param article2 文章2
     * @param config 检测配置
     * @return 相似度值
     */
    private double calculateSimilarityWithCache(Article article1, Article article2, DuplicateCheckConfig config) {
        if (cacheService != null && config.isEnableCache()) {
            Double cachedSimilarity = cacheService.getSimilarity(article1.getId(), article2.getId());
            
            if (cachedSimilarity != null) {
                logger.debug("使用缓存的相似度：文章ID {} 和 {}，相似度：{}", 
                        article1.getId(), article2.getId(), cachedSimilarity);
                return cachedSimilarity;
            }
            
            double similarity = similarityCalculator.calculateSimilarity(article1, article2);
            cacheService.putSimilarity(article1.getId(), article2.getId(), similarity);
            logger.debug("计算并缓存相似度：文章ID {} 和 {}，相似度：{}", 
                    article1.getId(), article2.getId(), similarity);
            return similarity;
        }
        
        return similarityCalculator.calculateSimilarity(article1, article2);
    }

    /**
     * 生成检测摘要
     * 
     * @param results 相似度结果列表
     * @param hasDuplicate 是否存在重复
     * @param config 检测配置
     * @return 检测摘要
     */
    private String generateSummary(List<SimilarityResult> results, boolean hasDuplicate, DuplicateCheckConfig config) {
        if (results.isEmpty()) {
            return "无相似文章";
        }
        
        double maxSimilarity = results.stream()
                .mapToDouble(SimilarityResult::getSimilarity)
                .max()
                .orElse(0.0);
        
        long duplicateCount = results.stream()
                .filter(r -> r.getSimilarity() >= config.getThreshold())
                .count();
        
        if (hasDuplicate) {
            return String.format("检测到%d篇相似文章，最高相似度：%.2f%%，超过阈值%.2f%%", 
                    duplicateCount, maxSimilarity * 100, config.getThreshold() * 100);
        } else {
            return String.format("检测完成，最高相似度：%.2f%%，未超过阈值%.2f%%", 
                    maxSimilarity * 100, config.getThreshold() * 100);
        }
    }
}
