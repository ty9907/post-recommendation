package com.example.demo.duplicate.detector.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.algorithm.SimilarityCalculatorFactory;
import com.example.demo.duplicate.async.AsyncDetectionService;
import com.example.demo.duplicate.async.DetectionResultCallback;
import com.example.demo.duplicate.candidate.CandidateManager;
import com.example.demo.duplicate.candidate.CandidateManagerImpl;
import com.example.demo.duplicate.candidate.CandidateSelection;
import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.detector.DuplicateDetector;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.DuplicateCheckReport;
import com.example.demo.duplicate.model.SimilarityResult;
import com.example.demo.duplicate.monitor.PerformanceMetrics;
import com.example.demo.duplicate.monitor.PerformanceMonitor;
import com.example.demo.duplicate.risk.RiskAssessor;
import com.example.demo.duplicate.risk.RiskLevel;
import com.example.demo.duplicate.service.SimilarityCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 实时检测器。
 *
 * 采用分层筛选策略进行低延迟检测：
 * 1. SimHash 候选集筛选
 * 2. 标签倒排索引过滤
 * 3. 精确相似度计算
 * 4. 风险等级评估
 */
public class RealTimeDetector implements DuplicateDetector {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeDetector.class);

    private static final String DETECTOR_NAME = "RealTimeDetector";

    private final SimilarityCalculator similarityCalculator;
    private final DuplicateCheckConfig config;
    private final SimilarityCacheService cacheService;
    private final CandidateManager candidateManager;
    private final PerformanceMonitor performanceMonitor;
    private final RiskAssessor riskAssessor;
    private final AsyncDetectionService asyncDetectionService;

    /**
     * 构造器。
     *
     * @param similarityCalculator 相似度计算器
     * @param config 检测配置
     * @param cacheService 缓存服务
     * @param candidateManager 候选集管理器
     * @param performanceMonitor 性能监控器
     * @param riskAssessor 风险评估器
     * @param asyncDetectionService 异步检测服务
     */
    public RealTimeDetector(SimilarityCalculator similarityCalculator,
                            DuplicateCheckConfig config,
                            SimilarityCacheService cacheService,
                            CandidateManager candidateManager,
                            PerformanceMonitor performanceMonitor,
                            RiskAssessor riskAssessor,
                            AsyncDetectionService asyncDetectionService) {
        this.similarityCalculator = Objects.requireNonNull(similarityCalculator, "相似度计算器不能为空");
        this.config = config != null ? config : DuplicateCheckConfig.defaultConfig();
        this.cacheService = cacheService;
        this.candidateManager = candidateManager != null ? candidateManager : new CandidateManagerImpl();
        this.performanceMonitor = performanceMonitor != null ? performanceMonitor : new PerformanceMonitor();
        this.riskAssessor = riskAssessor != null ? riskAssessor : new RiskAssessor();
        this.asyncDetectionService = asyncDetectionService;

        logger.info("实时检测器初始化完成，算法：{}，阈值：{}，分层检测：{}",
                similarityCalculator.getName(), this.config.getThreshold(), this.config.isEnableLayeredDetection());
    }

    /**
     * 简化构造器。
     */
    public RealTimeDetector(SimilarityCalculator similarityCalculator,
                            DuplicateCheckConfig config,
                            SimilarityCacheService cacheService) {
        this(similarityCalculator, config, cacheService, new CandidateManagerImpl(), new PerformanceMonitor(), new RiskAssessor(), null);
    }

    /**
     * 简化构造器（无缓存）。
     */
    public RealTimeDetector(SimilarityCalculator similarityCalculator, DuplicateCheckConfig config) {
        this(similarityCalculator, config, null);
    }

    /**
     * 简化构造器（使用默认配置）。
     */
    public RealTimeDetector(SimilarityCalculator similarityCalculator) {
        this(similarityCalculator, null, null);
    }

    @Override
    public DuplicateCheckReport detect(Article article, List<Article> existingArticles, DuplicateCheckConfig config) {
        DuplicateCheckConfig useConfig = config != null ? config : this.config;
        long totalStart = System.currentTimeMillis();
        PerformanceMetrics performanceMetrics = new PerformanceMetrics();

        DuplicateCheckReport report = new DuplicateCheckReport();
        report.setArticleId(article != null ? article.getId() : null);
        report.setCheckTime(LocalDateTime.now());

        if (article == null) {
            report.setHasDuplicate(false);
            report.setSummary("文章为空，跳过检测");
            finalizeMetrics(report, performanceMetrics, totalStart);
            return report;
        }

        try {
            List<Article> articlesToCompare = filterCurrentArticle(article, existingArticles);
            if (articlesToCompare.isEmpty()) {
                report.setHasDuplicate(false);
                report.setSummary("无文章可比较，检测通过");
                finalizeMetrics(report, performanceMetrics, totalStart);
                return report;
            }

            long candidateStart = System.currentTimeMillis();
            CandidateSelection selection = buildCandidateSelection(article, articlesToCompare, useConfig);
            List<Article> candidates = selection.getCandidates();
            performanceMetrics.recordStage("candidateSelection", System.currentTimeMillis() - candidateStart);
            performanceMetrics.recordCount("simHashCandidates", selection.getSimHashCandidateCount());
            performanceMetrics.recordCount("tagCandidates", selection.getTagCandidateCount());
            performanceMetrics.recordCount("finalCandidates", candidates.size());
            report.setLayerDetails(selection.toDiagnostics());

            if (candidates.isEmpty()) {
                report.setHasDuplicate(false);
                report.setRiskLevel(RiskLevel.LOW);
                report.setSummary("候选集为空，检测通过");
                finalizeMetrics(report, performanceMetrics, totalStart);
                return report;
            }

            long preciseStart = System.currentTimeMillis();
            List<SimilarityResult> results = calculateSimilarities(article, candidates, useConfig);
            performanceMetrics.recordStage("preciseCalculation", System.currentTimeMillis() - preciseStart);

            boolean hasDuplicate = results.stream().anyMatch(result -> result.getSimilarity() >= useConfig.getThreshold());
            RiskLevel riskLevel = riskAssessor.assess(results, useConfig);

            report.setResults(results);
            report.setHasDuplicate(hasDuplicate);
            report.setRiskLevel(riskLevel);
            report.setSummary(generateSummary(results, hasDuplicate, useConfig, riskLevel, candidates.size()));

            submitAsyncTaskIfNecessary(article, articlesToCompare, useConfig, riskLevel, report);
            finalizeMetrics(report, performanceMetrics, totalStart);

            logger.info("实时检测完成，文章ID：{}，候选集：{}，是否存在重复：{}，风险等级：{}，耗时：{}ms",
                    article.getId(), candidates.size(), hasDuplicate, riskLevel, performanceMetrics.getTotalDurationMillis());

            return report;
        } catch (Exception e) {
            logger.error("实时检测发生异常，文章ID：{}", article.getId(), e);
            report.setHasDuplicate(false);
            report.setRiskLevel(RiskLevel.LOW);
            report.setSummary("检测过程中发生异常：" + e.getMessage());
            finalizeMetrics(report, performanceMetrics, totalStart);
            return report;
        }
    }

    @Override
    public List<DuplicateCheckReport> batchDetect(List<Article> articles, List<Article> existingArticles, DuplicateCheckConfig config) {
        List<DuplicateCheckReport> reports = new ArrayList<>();
        if (articles == null || articles.isEmpty()) {
            return reports;
        }
        DuplicateCheckConfig useConfig = config != null ? config : this.config;
        for (Article article : articles) {
            reports.add(detect(article, existingArticles, useConfig));
        }
        return reports;
    }

    @Override
    public List<SimilarityResult> findSimilarArticles(Article article, List<Article> existingArticles, DuplicateCheckConfig config) {
        DuplicateCheckConfig useConfig = config != null ? config : this.config;
        List<Article> articlesToCompare = filterCurrentArticle(article, existingArticles);
        if (article == null || articlesToCompare.isEmpty()) {
            return new ArrayList<>();
        }

        CandidateSelection selection = buildCandidateSelection(article, articlesToCompare, useConfig);
        return calculateSimilarities(article, selection.getCandidates(), useConfig);
    }

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

    @Override
    public String getName() {
        return DETECTOR_NAME;
    }

    private CandidateSelection buildCandidateSelection(Article article,
                                                       List<Article> articlesToCompare,
                                                       DuplicateCheckConfig config) {
        if (!config.isEnableLayeredDetection()) {
            return new CandidateSelection(articlesToCompare, Map.of(), Map.of(), false,
                    articlesToCompare.size(), articlesToCompare.size());
        }
        CandidateSelection selection = candidateManager.selectCandidates(article, articlesToCompare, config);
        if (!selection.getCandidates().isEmpty()) {
            return selection;
        }
        if (!config.isEnableFullScanFallback()) {
            return selection;
        }
        return new CandidateSelection(articlesToCompare,
                selection.getHammingDistances(),
                selection.getSharedTagCounts(),
                selection.isCacheHit(),
                selection.getSimHashCandidateCount(),
                selection.getTagCandidateCount());
    }

    private List<Article> filterCurrentArticle(Article currentArticle, List<Article> articles) {
        if (currentArticle == null || articles == null || articles.isEmpty()) {
            return new ArrayList<>();
        }

        List<Article> filteredArticles = new ArrayList<>();
        for (Article article : articles) {
            if (article != null
                    && article.getId() != null
                    && !article.getId().equals(currentArticle.getId())) {
                filteredArticles.add(article);
            }
        }
        return filteredArticles;
    }

    private List<SimilarityResult> calculateSimilarities(Article article,
                                                         List<Article> candidates,
                                                         DuplicateCheckConfig config) {
        List<SimilarityResult> results = new ArrayList<>();
        if (candidates == null || candidates.isEmpty()) {
            return results;
        }

        for (Article candidate : candidates) {
            double similarity = calculateSimilarityWithCache(article, candidate, config);
            SimilarityResult result = new SimilarityResult();
            result.setArticleId(article.getId());
            result.setComparedArticleId(candidate.getId());
            result.setSimilarity(similarity);
            result.setAlgorithm(similarityCalculator.getName());
            result.setCheckTime(LocalDateTime.now());
            result.setDetails(Map.of(
                    "comparedTitle", candidate.getTitle() != null ? candidate.getTitle() : "",
                    "threshold", config.getThreshold()
            ));
            results.add(result);
        }

        results.sort(Comparator.comparingDouble(SimilarityResult::getSimilarity).reversed());
        if (results.size() > config.getMaxResults()) {
            return new ArrayList<>(results.subList(0, config.getMaxResults()));
        }
        return results;
    }

    private double calculateSimilarityWithCache(Article article1, Article article2, DuplicateCheckConfig config) {
        if (cacheService != null && config.isEnableCache()) {
            Double cachedSimilarity = cacheService.getSimilarity(article1.getId(), article2.getId());
            if (cachedSimilarity != null) {
                return cachedSimilarity;
            }
        }

        double similarity = similarityCalculator.calculateSimilarity(article1, article2);
        if (cacheService != null && config.isEnableCache()) {
            cacheService.putSimilarity(article1.getId(), article2.getId(), similarity);
        }
        return similarity;
    }

    private void submitAsyncTaskIfNecessary(Article article,
                                            List<Article> existingArticles,
                                            DuplicateCheckConfig config,
                                            RiskLevel riskLevel,
                                            DuplicateCheckReport report) {
        if (!config.isEnableAsyncDetection() || asyncDetectionService == null || riskLevel == RiskLevel.HIGH) {
            return;
        }

        SimilarityCalculator preciseCalculator;
        try {
            preciseCalculator = SimilarityCalculatorFactory.getCalculator(config.getPreciseAlgorithmType());
        } catch (IllegalArgumentException ex) {
            preciseCalculator = similarityCalculator;
        }

        boolean submitted = asyncDetectionService.submit(article, existingArticles, preciseCalculator, config,
                new DetectionResultCallback() {
                    @Override
                    public void onComplete(DuplicateCheckReport asyncReport) {
                        logger.debug("异步精检完成，文章ID：{}，是否重复：{}",
                                asyncReport.getArticleId(), asyncReport.isHasDuplicate());
                    }
                });
        report.setAsyncSubmitted(submitted);
    }

    private String generateSummary(List<SimilarityResult> results,
                                   boolean hasDuplicate,
                                   DuplicateCheckConfig config,
                                   RiskLevel riskLevel,
                                   int candidateCount) {
        if (results == null || results.isEmpty()) {
            return "无相似文章";
        }

        double maxSimilarity = results.stream()
                .mapToDouble(SimilarityResult::getSimilarity)
                .max()
                .orElse(0.0);
        long duplicateCount = results.stream()
                .filter(result -> result.getSimilarity() >= config.getThreshold())
                .count();

        if (hasDuplicate) {
            return String.format("检测到%d篇相似文章，最高相似度：%.2f%%，候选集大小：%d，风险等级：%s",
                    duplicateCount, maxSimilarity * 100, candidateCount, riskLevel);
        }
        return String.format("检测完成，最高相似度：%.2f%%，候选集大小：%d，风险等级：%s",
                maxSimilarity * 100, candidateCount, riskLevel);
    }

    private void finalizeMetrics(DuplicateCheckReport report, PerformanceMetrics performanceMetrics, long totalStart) {
        performanceMetrics.setTotalDurationMillis(System.currentTimeMillis() - totalStart);
        report.setPerformanceMetrics(performanceMetrics.toMap());
        if (performanceMonitor != null) {
            performanceMonitor.record(performanceMetrics);
        }
    }
}
