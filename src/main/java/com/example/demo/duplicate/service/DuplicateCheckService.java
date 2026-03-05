package com.example.demo.duplicate.service;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.algorithm.SimilarityCalculatorFactory;
import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.detector.DuplicateDetector;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.DuplicateCheckReport;
import com.example.demo.duplicate.model.SimilarityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 重复检测服务
 * 
 * 核心服务类，协调检测器和算法完成重复检测任务。
 * 提供单篇检测、批量检测和报告生成功能。
 * 
 * 注意：文章数据由调用者直接传入，本服务不负责获取文章。
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
public class DuplicateCheckService {

    private static final Logger logger = LoggerFactory.getLogger(DuplicateCheckService.class);

    private SimilarityCalculator calculator;
    private DuplicateDetector detector;
    private DuplicateCheckConfig config;
    private SimilarityCacheService cacheService;

    /**
     * 默认构造函数
     * 使用默认配置初始化服务
     */
    public DuplicateCheckService() {
        this.config = DuplicateCheckConfig.defaultConfig();
        this.calculator = SimilarityCalculatorFactory.getCalculator(config.getAlgorithmType());
        logger.info("DuplicateCheckService 初始化完成，使用默认配置: {}", this.config);
    }

    /**
     * 带配置参数的构造函数
     * 
     * @param config 检测配置
     */
    public DuplicateCheckService(DuplicateCheckConfig config) {
        this.config = config != null ? config : DuplicateCheckConfig.defaultConfig();
        this.calculator = SimilarityCalculatorFactory.getCalculator(this.config.getAlgorithmType());
        logger.info("DuplicateCheckService 初始化完成，配置: {}", this.config);
    }

    /**
     * 完整参数的构造函数
     * 
     * @param calculator 相似度计算器
     * @param detector 重复检测器
     * @param config 检测配置
     * @param cacheService 缓存服务（可选）
     */
    public DuplicateCheckService(SimilarityCalculator calculator,
                                  DuplicateDetector detector,
                                  DuplicateCheckConfig config,
                                  SimilarityCacheService cacheService) {
        this.calculator = calculator != null ? calculator : SimilarityCalculatorFactory.getCalculator();
        this.detector = detector;
        this.config = config != null ? config : DuplicateCheckConfig.defaultConfig();
        this.cacheService = cacheService;
        logger.info("DuplicateCheckService 初始化完成，配置: {}", this.config);
    }

    /**
     * 单篇检测
     * 使用默认配置检测文章是否重复
     * 
     * @param article 待检测的文章
     * @param existingArticles 已有的文章列表（用于比较）
     * @return 检测报告
     */
    public DuplicateCheckReport checkDuplicate(Article article, List<Article> existingArticles) {
        return checkDuplicate(article, existingArticles, this.config);
    }

    /**
     * 自定义配置检测
     * 使用指定配置检测文章是否重复
     * 
     * @param article 待检测的文章
     * @param existingArticles 已有的文章列表（用于比较）
     * @param config 检测配置
     * @return 检测报告
     */
    public DuplicateCheckReport checkDuplicate(Article article, List<Article> existingArticles, DuplicateCheckConfig config) {
        if (article == null) {
            logger.warn("检测文章为空，返回空报告");
            return createEmptyReport(null);
        }

        if (config == null) {
            config = this.config != null ? this.config : DuplicateCheckConfig.defaultConfig();
        }

        logger.info("开始检测文章重复，文章ID: {}, 标题: {}", article.getId(), article.getTitle());
        long startTime = System.currentTimeMillis();

        try {
            List<SimilarityResult> results = findSimilarArticles(article, existingArticles, config);
            DuplicateCheckReport report = generateReport(article, results);

            long elapsed = System.currentTimeMillis() - startTime;
            logger.info("文章检测完成，文章ID: {}, 耗时: {}ms, 相似文章数: {}, 是否重复: {}", 
                    article.getId(), elapsed, results.size(), report.isHasDuplicate());

            return report;
        } catch (Exception e) {
            logger.error("检测文章重复时发生错误，文章ID: {}", article.getId(), e);
            return createEmptyReport(article.getId());
        }
    }

    /**
     * 批量检测
     * 检测多篇文章是否重复
     * 
     * @param articles 待检测的文章列表
     * @param existingArticles 已有的文章列表（用于比较）
     * @return 检测报告列表
     */
    public List<DuplicateCheckReport> batchCheck(List<Article> articles, List<Article> existingArticles) {
        return batchCheck(articles, existingArticles, this.config);
    }

    /**
     * 批量检测（带配置）
     * 检测多篇文章是否重复
     * 
     * @param articles 待检测的文章列表
     * @param existingArticles 已有的文章列表（用于比较）
     * @param config 检测配置
     * @return 检测报告列表
     */
    public List<DuplicateCheckReport> batchCheck(List<Article> articles, List<Article> existingArticles, DuplicateCheckConfig config) {
        if (articles == null || articles.isEmpty()) {
            logger.warn("检测文章列表为空，返回空列表");
            return new ArrayList<>();
        }

        logger.info("开始批量检测文章重复，文章数量: {}", articles.size());
        long startTime = System.currentTimeMillis();

        DuplicateCheckConfig useConfig = config != null ? config : this.config;

        List<DuplicateCheckReport> reports = articles.stream()
                .map(article -> {
                    try {
                        return checkDuplicate(article, existingArticles, useConfig);
                    } catch (Exception e) {
                        logger.error("批量检测时发生错误，文章ID: {}", 
                                article != null ? article.getId() : null, e);
                        return createEmptyReport(article != null ? article.getId() : null);
                    }
                })
                .collect(Collectors.toList());

        long elapsed = System.currentTimeMillis() - startTime;
        long duplicateCount = reports.stream().filter(DuplicateCheckReport::isHasDuplicate).count();
        logger.info("批量检测完成，总文章数: {}, 重复文章数: {}, 耗时: {}ms", 
                articles.size(), duplicateCount, elapsed);

        return reports;
    }

    /**
     * 生成检测报告
     * 根据相似度结果生成完整的检测报告
     * 
     * @param article 待检测的文章
     * @param results 相似度检测结果列表
     * @return 检测报告
     */
    public DuplicateCheckReport generateReport(Article article, List<SimilarityResult> results) {
        if (article == null) {
            logger.warn("生成报告时文章为空");
            return createEmptyReport(null);
        }

        DuplicateCheckReport report = new DuplicateCheckReport();
        report.setArticleId(article.getId());
        report.setCheckTime(LocalDateTime.now());

        if (results == null || results.isEmpty()) {
            report.setResults(new ArrayList<>());
            report.setHasDuplicate(false);
            report.setSummary("未找到相似文章");
            return report;
        }

        List<SimilarityResult> validResults = results.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(SimilarityResult::getSimilarity).reversed())
                .limit(config.getMaxResults())
                .collect(Collectors.toList());

        report.setResults(validResults);

        boolean hasDuplicate = validResults.stream()
                .anyMatch(r -> r.getSimilarity() >= config.getThreshold());
        report.setHasDuplicate(hasDuplicate);

        String summary = generateSummary(article, validResults, hasDuplicate);
        report.setSummary(summary);

        logger.debug("生成检测报告，文章ID: {}, 相似文章数: {}, 是否重复: {}", 
                article.getId(), validResults.size(), hasDuplicate);

        return report;
    }

    /**
     * 设置重复检测器
     * 
     * @param detector 重复检测器
     */
    public void setDetector(DuplicateDetector detector) {
        this.detector = detector;
        logger.info("设置重复检测器: {}", detector != null ? detector.getName() : "null");
    }

    /**
     * 设置相似度计算器
     * 
     * @param calculator 相似度计算器
     */
    public void setCalculator(SimilarityCalculator calculator) {
        this.calculator = calculator;
        logger.info("设置相似度计算器: {}", calculator != null ? calculator.getName() : "null");
    }

    /**
     * 设置检测配置
     * 
     * @param config 检测配置
     */
    public void setConfig(DuplicateCheckConfig config) {
        this.config = config != null ? config : DuplicateCheckConfig.defaultConfig();
        if (this.calculator == null || 
            (config != null && !this.calculator.getName().equalsIgnoreCase(config.getAlgorithmType()))) {
            this.calculator = SimilarityCalculatorFactory.getCalculator(this.config.getAlgorithmType());
        }
        logger.info("更新检测配置: {}", this.config);
    }

    /**
     * 设置缓存服务
     * 
     * @param cacheService 缓存服务
     */
    public void setCacheService(SimilarityCacheService cacheService) {
        this.cacheService = cacheService;
        logger.info("设置缓存服务: {}", cacheService != null ? "已启用" : "已禁用");
    }

    /**
     * 查找相似文章
     * 
     * @param article 待检测的文章
     * @param existingArticles 已有的文章列表（用于比较）
     * @param config 检测配置
     * @return 相似度结果列表
     */
    private List<SimilarityResult> findSimilarArticles(Article article, List<Article> existingArticles, DuplicateCheckConfig config) {
        if (existingArticles == null || existingArticles.isEmpty()) {
            logger.debug("未提供已有文章列表，返回空列表");
            return new ArrayList<>();
        }

        List<Article> articlesToCompare = existingArticles.stream()
                .filter(a -> a != null && !a.getId().equals(article.getId()))
                .collect(Collectors.toList());

        logger.debug("找到 {} 篇待比较文章", articlesToCompare.size());

        if (detector != null) {
            return detector.findSimilarArticles(article, articlesToCompare, config);
        }

        return calculateSimilarities(article, articlesToCompare, config);
    }

    /**
     * 计算相似度
     * 
     * @param article 目标文章
     * @param articles 待比较的文章列表
     * @param config 检测配置
     * @return 相似度结果列表
     */
    private List<SimilarityResult> calculateSimilarities(Article article, 
                                                          List<Article> articles, 
                                                          DuplicateCheckConfig config) {
        if (calculator == null) {
            logger.warn("相似度计算器未设置");
            return new ArrayList<>();
        }

        List<SimilarityResult> results = new ArrayList<>();

        for (Article compareArticle : articles) {
            if (compareArticle == null || compareArticle.getId() == null) {
                continue;
            }

            try {
                Double cachedSimilarity = null;
                if (config.isEnableCache() && cacheService != null) {
                    cachedSimilarity = cacheService.getSimilarity(article.getId(), compareArticle.getId());
                }

                double similarity;
                if (cachedSimilarity != null) {
                    similarity = cachedSimilarity;
                    logger.debug("使用缓存相似度: {} vs {} = {}", 
                            article.getId(), compareArticle.getId(), similarity);
                } else {
                    similarity = calculator.calculateSimilarity(article, compareArticle);
                    if (config.isEnableCache() && cacheService != null) {
                        cacheService.putSimilarity(article.getId(), compareArticle.getId(), similarity);
                    }
                }

                if (similarity >= config.getSensitivity()) {
                    SimilarityResult result = new SimilarityResult();
                    result.setArticleId(article.getId());
                    result.setComparedArticleId(compareArticle.getId());
                    result.setSimilarity(similarity);
                    result.setAlgorithm(calculator.getName());
                    result.setCheckTime(LocalDateTime.now());

                    Map<String, Object> details = new HashMap<>();
                    details.put("comparedTitle", compareArticle.getTitle());
                    details.put("threshold", config.getThreshold());
                    result.setDetails(details);

                    results.add(result);
                }
            } catch (Exception e) {
                logger.error("计算相似度时发生错误，文章ID: {} vs {}", 
                        article.getId(), compareArticle.getId(), e);
            }
        }

        return results;
    }

    /**
     * 生成检测摘要
     * 
     * @param article 目标文章
     * @param results 相似度结果列表
     * @param hasDuplicate 是否存在重复
     * @return 检测摘要
     */
    private String generateSummary(Article article, List<SimilarityResult> results, boolean hasDuplicate) {
        if (results.isEmpty()) {
            return String.format("文章《%s》未找到相似文章", 
                    article.getTitle() != null ? article.getTitle() : "ID:" + article.getId());
        }

        SimilarityResult topResult = results.get(0);
        String topSimilarity = String.format("%.2f%%", topResult.getSimilarity() * 100);

        if (hasDuplicate) {
            long duplicateCount = results.stream()
                    .filter(r -> r.getSimilarity() >= config.getThreshold())
                    .count();
            return String.format("文章《%s》存在重复，共发现 %d 篇相似文章，最高相似度: %s", 
                    article.getTitle() != null ? article.getTitle() : "ID:" + article.getId(),
                    duplicateCount, topSimilarity);
        } else {
            return String.format("文章《%s》未发现重复，共找到 %d 篇相似文章，最高相似度: %s", 
                    article.getTitle() != null ? article.getTitle() : "ID:" + article.getId(),
                    results.size(), topSimilarity);
        }
    }

    /**
     * 创建空报告
     * 
     * @param articleId 文章ID
     * @return 空的检测报告
     */
    private DuplicateCheckReport createEmptyReport(Long articleId) {
        DuplicateCheckReport report = new DuplicateCheckReport();
        report.setArticleId(articleId);
        report.setResults(new ArrayList<>());
        report.setHasDuplicate(false);
        report.setCheckTime(LocalDateTime.now());
        report.setSummary(articleId != null ? "检测失败或文章不存在" : "文章为空");
        return report;
    }

    /**
     * 获取当前配置
     * 
     * @return 当前检测配置
     */
    public DuplicateCheckConfig getConfig() {
        return config;
    }

    /**
     * 获取当前相似度计算器
     * 
     * @return 相似度计算器
     */
    public SimilarityCalculator getCalculator() {
        return calculator;
    }

    /**
     * 获取当前重复检测器
     * 
     * @return 重复检测器
     */
    public DuplicateDetector getDetector() {
        return detector;
    }

    /**
     * 获取当前缓存服务
     * 
     * @return 缓存服务
     */
    public SimilarityCacheService getCacheService() {
        return cacheService;
    }
}
