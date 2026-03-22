package com.example.demo.duplicate.detector.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 批量检测器。
 *
 * 在保留并行处理能力的同时，引入索引加速与候选集筛选。
 */
public class BatchDetector implements DuplicateDetector {

    private static final Logger logger = LoggerFactory.getLogger(BatchDetector.class);

    private static final String DETECTOR_NAME = "BatchDetector";

    private static final int DEFAULT_BATCH_SIZE = 100;

    private static final int MAX_PARALLEL_THREADS = Runtime.getRuntime().availableProcessors();

    private final SimilarityCalculator similarityCalculator;
    private final SimilarityCacheService cacheService;
    private final int batchSize;
    private final ForkJoinPool forkJoinPool;
    private final CandidateManager candidateManager;
    private final PerformanceMonitor performanceMonitor;
    private final RiskAssessor riskAssessor;
    private final ConcurrentHashMap<Long, ReentrantLock> articleLocks = new ConcurrentHashMap<>();

    public BatchDetector(SimilarityCalculator similarityCalculator,
                         SimilarityCacheService cacheService,
                         int batchSize,
                         CandidateManager candidateManager,
                         PerformanceMonitor performanceMonitor,
                         RiskAssessor riskAssessor) {
        this.similarityCalculator = Objects.requireNonNull(similarityCalculator, "相似度计算器不能为空");
        this.cacheService = cacheService;
        this.batchSize = batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE;
        this.candidateManager = candidateManager != null ? candidateManager : new CandidateManagerImpl();
        this.performanceMonitor = performanceMonitor != null ? performanceMonitor : new PerformanceMonitor();
        this.riskAssessor = riskAssessor != null ? riskAssessor : new RiskAssessor();
        this.forkJoinPool = new ForkJoinPool(MAX_PARALLEL_THREADS);

        logger.info("批量检测器初始化完成，算法：{}，批量大小：{}，并行线程数：{}",
                similarityCalculator.getName(), this.batchSize, MAX_PARALLEL_THREADS);
    }

    public BatchDetector(SimilarityCalculator similarityCalculator,
                         SimilarityCacheService cacheService,
                         int batchSize) {
        this(similarityCalculator, cacheService, batchSize, new CandidateManagerImpl(), new PerformanceMonitor(), new RiskAssessor());
    }

    public BatchDetector(SimilarityCalculator similarityCalculator,
                         SimilarityCacheService cacheService) {
        this(similarityCalculator, cacheService, DEFAULT_BATCH_SIZE);
    }

    public BatchDetector(SimilarityCalculator similarityCalculator) {
        this(similarityCalculator, null, DEFAULT_BATCH_SIZE);
    }

    @Override
    public DuplicateCheckReport detect(Article article, List<Article> existingArticles, DuplicateCheckConfig config) {
        if (article == null) {
            DuplicateCheckReport report = new DuplicateCheckReport();
            report.setArticleId(null);
            report.setCheckTime(LocalDateTime.now());
            report.setHasDuplicate(false);
            report.setSummary("文章为空，跳过检测");
            return report;
        }

        List<Article> singleArticleList = new ArrayList<>();
        singleArticleList.add(article);
        List<DuplicateCheckReport> reports = batchDetect(singleArticleList, existingArticles, config);
        return reports.isEmpty() ? createEmptyReport(article) : reports.get(0);
    }

    @Override
    public List<DuplicateCheckReport> batchDetect(List<Article> articles, List<Article> existingArticles, DuplicateCheckConfig config) {
        long startTime = System.currentTimeMillis();
        List<DuplicateCheckReport> allReports = new ArrayList<>();

        if (articles == null || articles.isEmpty()) {
            return allReports;
        }

        DuplicateCheckConfig effectiveConfig = config != null ? config : DuplicateCheckConfig.defaultConfig();
        List<Article> articlesToCompare = existingArticles != null ? existingArticles : new ArrayList<>();

        if (effectiveConfig.isEnableLayeredDetection()) {
            candidateManager.warmUp(articlesToCompare);
        }

        int totalBatches = (articles.size() + batchSize - 1) / batchSize;
        for (int i = 0; i < articles.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, articles.size());
            List<Article> batch = articles.subList(i, endIndex);

            int currentBatch = i / batchSize + 1;
            logger.info("处理第{}/{}批次，文章数量：{}", currentBatch, totalBatches, batch.size());

            List<DuplicateCheckReport> batchReports = processBatch(batch, articlesToCompare, effectiveConfig);
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

    @Override
    public List<SimilarityResult> findSimilarArticles(Article article, List<Article> existingArticles, DuplicateCheckConfig config) {
        DuplicateCheckConfig effectiveConfig = config != null ? config : DuplicateCheckConfig.defaultConfig();
        List<Article> articlesToCompare = filterCurrentArticle(article, existingArticles);

        if (article == null || articlesToCompare.isEmpty()) {
            return new ArrayList<>();
        }

        if (effectiveConfig.isEnableLayeredDetection()) {
            candidateManager.warmUp(articlesToCompare);
        }

        CandidateSelection selection = buildCandidateSelection(article, articlesToCompare, effectiveConfig);
        return calculateSimilarities(article, selection.getCandidates(), effectiveConfig);
    }

    @Override
    public boolean isDuplicate(Article article1, Article article2, double threshold) {
        if (article1 == null || article2 == null) {
            return false;
        }
        double similarity = similarityCalculator.calculateSimilarity(article1, article2);
        return similarity >= threshold;
    }

    @Override
    public String getName() {
        return DETECTOR_NAME;
    }

    public List<DuplicateCheckReport> processBatch(List<Article> batch, List<Article> existingArticles, DuplicateCheckConfig config) {
        if (batch == null || batch.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return forkJoinPool.submit(() ->
                    batch.parallelStream()
                            .map(article -> detectArticle(article, existingArticles, config))
                            .collect(Collectors.toList())
            ).get();
        } catch (Exception e) {
            logger.error("并行处理批次时发生异常", e);
            return batch.stream()
                    .map(article -> createErrorReport(article, e))
                    .collect(Collectors.toList());
        }
    }

    private DuplicateCheckReport detectArticle(Article article, List<Article> existingArticles, DuplicateCheckConfig config) {
        ReentrantLock lock = articleLocks.computeIfAbsent(article.getId(), key -> new ReentrantLock());
        lock.lock();
        try {
            return doDetectArticle(article, existingArticles, config);
        } finally {
            lock.unlock();
            articleLocks.remove(article.getId());
        }
    }

    private DuplicateCheckReport doDetectArticle(Article article, List<Article> existingArticles, DuplicateCheckConfig config) {
        long totalStart = System.currentTimeMillis();
        PerformanceMetrics performanceMetrics = new PerformanceMetrics();

        DuplicateCheckReport report = new DuplicateCheckReport();
        report.setArticleId(article.getId());
        report.setCheckTime(LocalDateTime.now());

        try {
            List<Article> filteredArticles = filterCurrentArticle(article, existingArticles);
            if (filteredArticles.isEmpty()) {
                report.setHasDuplicate(false);
                report.setRiskLevel(RiskLevel.LOW);
                report.setSummary("无文章可比较，检测通过");
                finalizeMetrics(report, performanceMetrics, totalStart);
                return report;
            }

            long candidateStart = System.currentTimeMillis();
            CandidateSelection selection = buildCandidateSelection(article, filteredArticles, config);
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
            List<SimilarityResult> results = calculateSimilarities(article, candidates, config);
            performanceMetrics.recordStage("preciseCalculation", System.currentTimeMillis() - preciseStart);

            boolean hasDuplicate = results.stream().anyMatch(result -> result.getSimilarity() >= config.getThreshold());
            RiskLevel riskLevel = riskAssessor.assess(results, config);

            report.setResults(results);
            report.setHasDuplicate(hasDuplicate);
            report.setRiskLevel(riskLevel);
            report.setSummary(generateSummary(results, hasDuplicate, config, riskLevel, candidates.size()));

            if (hasDuplicate) {
                logger.warn("检测到重复文章，文章ID：{}，最高相似度：{}",
                        article.getId(), results.get(0).getSimilarity());
            }

            finalizeMetrics(report, performanceMetrics, totalStart);
            return report;
        } catch (Exception e) {
            logger.error("检测文章时发生异常，文章ID：{}", article.getId(), e);
            report.setHasDuplicate(false);
            report.setRiskLevel(RiskLevel.LOW);
            report.setSummary("检测过程中发生异常：" + e.getMessage());
            finalizeMetrics(report, performanceMetrics, totalStart);
            return report;
        }
    }

    private CandidateSelection buildCandidateSelection(Article article,
                                                       List<Article> filteredArticles,
                                                       DuplicateCheckConfig config) {
        if (!config.isEnableLayeredDetection()) {
            return new CandidateSelection(filteredArticles, Map.of(), Map.of(), false,
                    filteredArticles.size(), filteredArticles.size());
        }
        CandidateSelection selection = candidateManager.selectCandidates(article, null, config);
        if (!selection.getCandidates().isEmpty()) {
            return selection;
        }
        if (!config.isEnableFullScanFallback()) {
            return selection;
        }
        return new CandidateSelection(filteredArticles,
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
                                                         List<Article> existingArticles,
                                                         DuplicateCheckConfig config) {
        if (existingArticles == null || existingArticles.isEmpty()) {
            return new ArrayList<>();
        }

        List<SimilarityResult> results = existingArticles.parallelStream()
                .map(existingArticle -> calculateSimilarityResult(article, existingArticle, config))
                .sorted(Comparator.comparingDouble(SimilarityResult::getSimilarity).reversed())
                .collect(Collectors.toList());

        if (results.size() > config.getMaxResults()) {
            return new ArrayList<>(results.subList(0, config.getMaxResults()));
        }
        return results;
    }

    private SimilarityResult calculateSimilarityResult(Article article1, Article article2, DuplicateCheckConfig config) {
        double similarity = calculateSimilarity(article1, article2, config);

        SimilarityResult result = new SimilarityResult();
        result.setArticleId(article1.getId());
        result.setComparedArticleId(article2.getId());
        result.setSimilarity(similarity);
        result.setAlgorithm(similarityCalculator.getName());
        result.setCheckTime(LocalDateTime.now());
        result.setDetails(Map.of(
                "comparedTitle", article2.getTitle() != null ? article2.getTitle() : "",
                "threshold", config.getThreshold()
        ));
        return result;
    }

    private double calculateSimilarity(Article article1, Article article2, DuplicateCheckConfig config) {
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

    private DuplicateCheckReport createEmptyReport(Article article) {
        DuplicateCheckReport report = new DuplicateCheckReport();
        report.setArticleId(article.getId());
        report.setCheckTime(LocalDateTime.now());
        report.setHasDuplicate(false);
        report.setRiskLevel(RiskLevel.LOW);
        report.setSummary("无法生成检测报告");
        return report;
    }

    private DuplicateCheckReport createErrorReport(Article article, Exception e) {
        DuplicateCheckReport report = new DuplicateCheckReport();
        report.setArticleId(article != null ? article.getId() : null);
        report.setCheckTime(LocalDateTime.now());
        report.setHasDuplicate(false);
        report.setRiskLevel(RiskLevel.LOW);
        report.setSummary("检测过程中发生异常：" + e.getMessage());
        return report;
    }

    private void finalizeMetrics(DuplicateCheckReport report, PerformanceMetrics performanceMetrics, long totalStart) {
        performanceMetrics.setTotalDurationMillis(System.currentTimeMillis() - totalStart);
        report.setPerformanceMetrics(performanceMetrics.toMap());
        if (performanceMonitor != null) {
            performanceMonitor.record(performanceMetrics);
        }
    }

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

    public int getBatchSize() {
        return batchSize;
    }
}
