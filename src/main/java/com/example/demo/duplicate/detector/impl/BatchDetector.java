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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 批量检测器
 * 
 * 高吞吐、可并行处理的重复文章检测器。
 * 适用于定时任务和管理员批量操作场景。
 * 
 * 特点：
 * 1. 高吞吐：支持批量检测，减少重复IO操作
 * 2. 并行处理：使用并行流和线程池提升性能
 * 3. 可配置：支持批量大小配置
 * 4. 线程安全：支持并发调用
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
public class BatchDetector implements DuplicateDetector {

    private static final Logger logger = LoggerFactory.getLogger(BatchDetector.class);

    private static final String DETECTOR_NAME = "BatchDetector";

    private static final int DEFAULT_BATCH_SIZE = 100;

    private static final int MAX_PARALLEL_THREADS = Runtime.getRuntime().availableProcessors();

    private final SimilarityCalculator similarityCalculator;
    private final ArticleRepository articleRepository;
    private final SimilarityCacheService cacheService;
    private final int batchSize;

    private final ForkJoinPool forkJoinPool;

    private final ConcurrentHashMap<Long, ReentrantLock> articleLocks = new ConcurrentHashMap<>();

    /**
     * 构造器（使用默认批量大小）
     * 
     * @param similarityCalculator 相似度计算器
     * @param articleRepository 文章仓储
     * @param cacheService 缓存服务
     */
    public BatchDetector(SimilarityCalculator similarityCalculator,
                        ArticleRepository articleRepository,
                        SimilarityCacheService cacheService) {
        this(similarityCalculator, articleRepository, cacheService, DEFAULT_BATCH_SIZE);
    }

    /**
     * 构造器（指定批量大小）
     * 
     * @param similarityCalculator 相似度计算器
     * @param articleRepository 文章仓储
     * @param cacheService 缓存服务
     * @param batchSize 批量大小
     */
    public BatchDetector(SimilarityCalculator similarityCalculator,
                        ArticleRepository articleRepository,
                        SimilarityCacheService cacheService,
                        int batchSize) {
        this.similarityCalculator = Objects.requireNonNull(similarityCalculator, "相似度计算器不能为空");
        this.articleRepository = Objects.requireNonNull(articleRepository, "文章仓储不能为空");
        this.cacheService = cacheService;
        this.batchSize = batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE;
        this.forkJoinPool = new ForkJoinPool(MAX_PARALLEL_THREADS);
        
        logger.info("批量检测器初始化完成，算法：{}，批量大小：{}，并行线程数：{}", 
                similarityCalculator.getName(), this.batchSize, MAX_PARALLEL_THREADS);
    }

    /**
     * 检测单篇文章是否重复
     * 
     * @param article 待检测的文章
     * @param config 检测配置
     * @return 检测报告
     */
    @Override
    public DuplicateCheckReport detect(Article article, DuplicateCheckConfig config) {
        logger.debug("批量检测器处理单篇文章，文章ID：{}", article.getId());
        
        List<Article> singleArticleList = new ArrayList<>();
        singleArticleList.add(article);
        
        List<DuplicateCheckReport> reports = batchDetect(singleArticleList, config);
        
        return reports.isEmpty() ? createEmptyReport(article) : reports.get(0);
    }

    /**
     * 批量检测文章是否重复
     * 
     * @param articles 待检测的文章列表
     * @param config 检测配置
     * @return 检测报告列表
     */
    @Override
    public List<DuplicateCheckReport> batchDetect(List<Article> articles, DuplicateCheckConfig config) {
        long startTime = System.currentTimeMillis();
        
        DuplicateCheckConfig effectiveConfig = config != null ? config : DuplicateCheckConfig.defaultConfig();
        
        logger.info("开始批量检测，文章数量：{}，阈值：{}", articles.size(), effectiveConfig.getThreshold());
        
        List<DuplicateCheckReport> allReports = new ArrayList<>();
        
        int totalBatches = (articles.size() + batchSize - 1) / batchSize;
        
        for (int i = 0; i < articles.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, articles.size());
            List<Article> batch = articles.subList(i, endIndex);
            
            int currentBatch = i / batchSize + 1;
            logger.info("处理第{}/{}批次，文章数量：{}", currentBatch, totalBatches, batch.size());
            
            List<DuplicateCheckReport> batchReports = processBatch(batch, effectiveConfig);
            allReports.addAll(batchReports);
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        long duplicateCount = allReports.stream()
                .filter(DuplicateCheckReport::isHasDuplicate)
                .count();
        
        logger.info("批量检测完成，总文章数：{}，重复文章数：{}，耗时：{}ms", 
                articles.size(), duplicateCount, elapsed);
        
        return allReports;
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
        DuplicateCheckConfig effectiveConfig = config != null ? config : DuplicateCheckConfig.defaultConfig();
        
        List<Article> recentArticles = getRecentArticles(article, effectiveConfig);
        
        if (recentArticles.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<SimilarityResult> results = calculateSimilaritiesParallel(article, recentArticles, effectiveConfig);
        
        results.sort(Comparator.comparingDouble(SimilarityResult::getSimilarity).reversed());
        
        int maxResults = effectiveConfig.getMaxResults();
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
        double similarity = calculateSimilarity(article1, article2, DuplicateCheckConfig.defaultConfig());
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
     * 处理单个批次的文章
     * 使用并行流提升处理性能
     * 
     * @param batch 文章批次
     * @param config 检测配置
     * @return 检测报告列表
     */
    public List<DuplicateCheckReport> processBatch(List<Article> batch, DuplicateCheckConfig config) {
        long batchStartTime = System.currentTimeMillis();
        
        logger.debug("开始处理批次，文章数量：{}", batch.size());
        
        List<Article> recentArticles = articleRepository.findRecentArticles(config.getRecentDays());
        logger.debug("获取到{}篇近期文章用于比较", recentArticles.size());
        
        List<DuplicateCheckReport> reports;
        
        try {
            reports = forkJoinPool.submit(() -> 
                batch.parallelStream()
                    .map(article -> detectArticle(article, recentArticles, config))
                    .collect(Collectors.toList())
            ).get();
        } catch (Exception e) {
            logger.error("并行处理批次时发生异常", e);
            reports = batch.stream()
                    .map(article -> createErrorReport(article, e))
                    .collect(Collectors.toList());
        }
        
        long elapsed = System.currentTimeMillis() - batchStartTime;
        logger.debug("批次处理完成，文章数量：{}，耗时：{}ms", batch.size(), elapsed);
        
        return reports;
    }

    /**
     * 检测单篇文章（线程安全）
     * 
     * @param article 待检测文章
     * @param recentArticles 近期文章列表
     * @param config 检测配置
     * @return 检测报告
     */
    private DuplicateCheckReport detectArticle(Article article, List<Article> recentArticles, DuplicateCheckConfig config) {
        ReentrantLock lock = articleLocks.computeIfAbsent(article.getId(), k -> new ReentrantLock());
        
        lock.lock();
        try {
            return doDetectArticle(article, recentArticles, config);
        } finally {
            lock.unlock();
            articleLocks.remove(article.getId());
        }
    }

    /**
     * 执行单篇文章检测
     * 
     * @param article 待检测文章
     * @param recentArticles 近期文章列表
     * @param config 检测配置
     * @return 检测报告
     */
    private DuplicateCheckReport doDetectArticle(Article article, List<Article> recentArticles, DuplicateCheckConfig config) {
        DuplicateCheckReport report = new DuplicateCheckReport();
        report.setArticleId(article.getId());
        report.setCheckTime(LocalDateTime.now());
        
        try {
            List<Article> filteredArticles = recentArticles.stream()
                    .filter(a -> !a.getId().equals(article.getId()))
                    .collect(Collectors.toList());
            
            if (filteredArticles.isEmpty()) {
                report.setHasDuplicate(false);
                report.setSummary("无近期文章可比较，检测通过");
                return report;
            }
            
            List<SimilarityResult> results = calculateSimilaritiesParallel(article, filteredArticles, config);
            
            results.sort(Comparator.comparingDouble(SimilarityResult::getSimilarity).reversed());
            
            int maxResults = config.getMaxResults();
            if (results.size() > maxResults) {
                results = results.subList(0, maxResults);
            }
            
            for (SimilarityResult result : results) {
                report.addResult(result);
            }
            
            boolean hasDuplicate = results.stream()
                    .anyMatch(r -> r.getSimilarity() >= config.getThreshold());
            
            report.setHasDuplicate(hasDuplicate);
            report.setSummary(generateSummary(results, hasDuplicate, config));
            
            if (hasDuplicate) {
                logger.warn("检测到重复文章，文章ID：{}，最高相似度：{}", 
                        article.getId(), results.get(0).getSimilarity());
            }
            
        } catch (Exception e) {
            logger.error("检测文章时发生异常，文章ID：{}", article.getId(), e);
            report.setHasDuplicate(false);
            report.setSummary("检测过程中发生异常：" + e.getMessage());
        }
        
        return report;
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
     * 并行计算文章相似度
     * 
     * @param article 待检测文章
     * @param recentArticles 近期文章列表
     * @param config 检测配置
     * @return 相似度结果列表
     */
    private List<SimilarityResult> calculateSimilaritiesParallel(Article article, List<Article> recentArticles, 
                                                                  DuplicateCheckConfig config) {
        return recentArticles.parallelStream()
                .map(recentArticle -> calculateSimilarityResult(article, recentArticle, config))
                .collect(Collectors.toList());
    }

    /**
     * 计算两篇文章的相似度结果
     * 
     * @param article1 文章1
     * @param article2 文章2
     * @param config 检测配置
     * @return 相似度结果
     */
    private SimilarityResult calculateSimilarityResult(Article article1, Article article2, DuplicateCheckConfig config) {
        double similarity = calculateSimilarity(article1, article2, config);
        
        SimilarityResult result = new SimilarityResult();
        result.setArticleId(article1.getId());
        result.setComparedArticleId(article2.getId());
        result.setSimilarity(similarity);
        result.setAlgorithm(similarityCalculator.getName());
        result.setCheckTime(LocalDateTime.now());
        
        return result;
    }

    /**
     * 计算两篇文章的相似度
     * 优先使用缓存
     * 
     * @param article1 文章1
     * @param article2 文章2
     * @param config 检测配置
     * @return 相似度值
     */
    private double calculateSimilarity(Article article1, Article article2, DuplicateCheckConfig config) {
        if (cacheService != null && config.isEnableCache()) {
            Double cachedSimilarity = cacheService.getSimilarity(article1.getId(), article2.getId());
            
            if (cachedSimilarity != null) {
                return cachedSimilarity;
            }
            
            double similarity = similarityCalculator.calculateSimilarity(article1, article2);
            cacheService.putSimilarity(article1.getId(), article2.getId(), similarity);
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

    /**
     * 创建空报告
     * 
     * @param article 文章
     * @return 空检测报告
     */
    private DuplicateCheckReport createEmptyReport(Article article) {
        DuplicateCheckReport report = new DuplicateCheckReport();
        report.setArticleId(article.getId());
        report.setCheckTime(LocalDateTime.now());
        report.setHasDuplicate(false);
        report.setSummary("无法生成检测报告");
        return report;
    }

    /**
     * 创建错误报告
     * 
     * @param article 文章
     * @param e 异常
     * @return 错误检测报告
     */
    private DuplicateCheckReport createErrorReport(Article article, Exception e) {
        DuplicateCheckReport report = new DuplicateCheckReport();
        report.setArticleId(article.getId());
        report.setCheckTime(LocalDateTime.now());
        report.setHasDuplicate(false);
        report.setSummary("检测过程中发生异常：" + e.getMessage());
        return report;
    }

    /**
     * 关闭检测器
     * 释放线程池资源
     */
    public void shutdown() {
        forkJoinPool.shutdown();
        try {
            if (!forkJoinPool.awaitTermination(60, TimeUnit.SECONDS)) {
                forkJoinPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            forkJoinPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("批量检测器已关闭");
    }

    /**
     * 获取批量大小
     * 
     * @return 批量大小
     */
    public int getBatchSize() {
        return batchSize;
    }
}
