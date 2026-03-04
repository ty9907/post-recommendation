package com.example.demo.duplicate.detector.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.DuplicateCheckReport;
import com.example.demo.duplicate.model.SimilarityResult;
import com.example.demo.duplicate.repository.ArticleRepository;
import com.example.demo.duplicate.service.SimilarityCacheService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BatchDetector 测试类
 * 
 * 测试批量检测器的核心功能：
 * 1. 批量检测功能
 * 2. 并行处理效果
 * 3. 批量大小配置
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("批量检测器测试")
class BatchDetectorTest {

    @Mock
    private SimilarityCalculator similarityCalculator;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private SimilarityCacheService cacheService;

    private BatchDetector detector;
    private DuplicateCheckConfig config;
    private Article testArticle;
    private Article similarArticle1;
    private Article similarArticle2;

    @BeforeEach
    void setUp() {
        config = DuplicateCheckConfig.defaultConfig();
        
        lenient().when(similarityCalculator.getName()).thenReturn("SimHash");
        
        detector = new BatchDetector(similarityCalculator, articleRepository, cacheService, 10);
        
        testArticle = new Article(1L, "测试文章标题", "这是一篇测试文章的内容，用于验证批量检测功能。");
        testArticle.setCreateTime(LocalDateTime.now());
        
        similarArticle1 = new Article(2L, "相似文章1", "这是一篇相似文章的内容，用于验证批量检测功能。");
        similarArticle1.setCreateTime(LocalDateTime.now());
        
        similarArticle2 = new Article(3L, "相似文章2", "这是另一篇相似文章的内容，用于验证批量检测功能。");
        similarArticle2.setCreateTime(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        if (detector != null) {
            detector.shutdown();
        }
    }

    @Test
    @DisplayName("测试批量检测 - 正常情况")
    void testBatchDetect_Normal() {
        List<Article> articlesToCheck = Arrays.asList(
            new Article(1L, "文章1", "内容1"),
            new Article(2L, "文章2", "内容2"),
            new Article(3L, "文章3", "内容3")
        );
        
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(Arrays.asList(similarArticle1, similarArticle2));
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.5);
        
        List<DuplicateCheckReport> reports = detector.batchDetect(articlesToCheck, config);
        
        assertNotNull(reports, "报告列表不应为空");
        assertEquals(3, reports.size(), "应返回3份报告");
        
        for (DuplicateCheckReport report : reports) {
            assertNotNull(report, "每份报告不应为空");
            assertNotNull(report.getCheckTime(), "检测时间不应为空");
        }
    }

    @Test
    @DisplayName("测试批量检测 - 空列表")
    void testBatchDetect_EmptyList() {
        List<DuplicateCheckReport> reports = detector.batchDetect(Collections.emptyList(), config);
        
        assertNotNull(reports, "报告列表不应为空");
        assertTrue(reports.isEmpty(), "报告列表应为空");
    }

    @Test
    @DisplayName("测试批量检测 - 使用默认配置")
    void testBatchDetect_DefaultConfig() {
        List<Article> articlesToCheck = Arrays.asList(testArticle);
        
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(Arrays.asList(similarArticle1));
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.5);
        
        List<DuplicateCheckReport> reports = detector.batchDetect(articlesToCheck, null);
        
        assertNotNull(reports, "报告列表不应为空");
        assertEquals(1, reports.size(), "应返回1份报告");
    }

    @Test
    @DisplayName("测试单篇检测 - 通过批量检测实现")
    void testDetect_SingleArticle() {
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(Arrays.asList(similarArticle1));
        when(cacheService.getSimilarity(anyLong(), anyLong())).thenReturn(null);
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.85);
        
        DuplicateCheckReport report = detector.detect(testArticle, config);
        
        assertNotNull(report, "检测报告不应为空");
        assertEquals(1L, report.getArticleId(), "文章ID应匹配");
        assertTrue(report.isHasDuplicate(), "应标记为重复");
    }

    @Test
    @DisplayName("测试单篇检测 - 无近期文章")
    void testDetect_NoRecentArticles() {
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(Collections.emptyList());
        
        DuplicateCheckReport report = detector.detect(testArticle, config);
        
        assertNotNull(report, "检测报告不应为空");
        assertFalse(report.isHasDuplicate(), "不应标记为重复");
    }

    @Test
    @DisplayName("测试查找相似文章 - 正常情况")
    void testFindSimilarArticles_Normal() {
        List<Article> recentArticles = Arrays.asList(similarArticle1, similarArticle2);
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(recentArticles);
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.85, 0.65);
        
        List<SimilarityResult> results = detector.findSimilarArticles(testArticle, config);
        
        assertNotNull(results, "结果列表不应为空");
        assertEquals(2, results.size(), "应有2个结果");
    }

    @Test
    @DisplayName("测试查找相似文章 - 无近期文章")
    void testFindSimilarArticles_NoRecentArticles() {
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(Collections.emptyList());
        
        List<SimilarityResult> results = detector.findSimilarArticles(testArticle, config);
        
        assertNotNull(results, "结果列表不应为空");
        assertTrue(results.isEmpty(), "结果列表应为空");
    }

    @Test
    @DisplayName("测试判断两篇文章是否重复 - 重复")
    void testIsDuplicate_True() {
        when(cacheService.getSimilarity(anyLong(), anyLong())).thenReturn(null);
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.85);
        
        boolean isDuplicate = detector.isDuplicate(testArticle, similarArticle1, 0.7);
        
        assertTrue(isDuplicate, "应判定为重复");
    }

    @Test
    @DisplayName("测试判断两篇文章是否重复 - 不重复")
    void testIsDuplicate_False() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.5);
        
        boolean isDuplicate = detector.isDuplicate(testArticle, similarArticle1, 0.7);
        
        assertFalse(isDuplicate, "不应判定为重复");
    }

    @Test
    @DisplayName("测试获取检测器名称")
    void testGetName() {
        assertEquals("BatchDetector", detector.getName(), "检测器名称应为BatchDetector");
    }

    @Test
    @DisplayName("测试获取批量大小")
    void testGetBatchSize() {
        assertEquals(10, detector.getBatchSize(), "批量大小应为10");
    }

    @Test
    @DisplayName("测试并行处理效果 - 大批量检测")
    void testParallelProcessing_LargeBatch() {
        List<Article> articlesToCheck = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Article article = new Article((long) (i + 100), "文章" + i, "内容" + i);
            article.setCreateTime(LocalDateTime.now());
            articlesToCheck.add(article);
        }
        
        List<Article> recentArticles = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Article article = new Article((long) (i + 200), "近期文章" + i, "内容" + i);
            article.setCreateTime(LocalDateTime.now());
            recentArticles.add(article);
        }
        
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(recentArticles);
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.5);
        
        long startTime = System.currentTimeMillis();
        List<DuplicateCheckReport> reports = detector.batchDetect(articlesToCheck, config);
        long elapsedTime = System.currentTimeMillis() - startTime;
        
        assertEquals(50, reports.size(), "应返回50份报告");
        assertTrue(elapsedTime < 30000, "批量检测应在30秒内完成，实际耗时: " + elapsedTime + "ms");
    }

    @Test
    @DisplayName("测试批量大小配置 - 默认批量大小")
    void testBatchSize_Default() {
        BatchDetector defaultDetector = new BatchDetector(
            similarityCalculator, articleRepository, cacheService);
        
        assertEquals(100, defaultDetector.getBatchSize(), "默认批量大小应为100");
        
        defaultDetector.shutdown();
    }

    @Test
    @DisplayName("测试批量大小配置 - 自定义批量大小")
    void testBatchSize_Custom() {
        BatchDetector customDetector = new BatchDetector(
            similarityCalculator, articleRepository, cacheService, 25);
        
        assertEquals(25, customDetector.getBatchSize(), "自定义批量大小应为25");
        
        customDetector.shutdown();
    }

    @Test
    @DisplayName("测试批量大小配置 - 无效批量大小使用默认值")
    void testBatchSize_Invalid() {
        BatchDetector invalidDetector = new BatchDetector(
            similarityCalculator, articleRepository, cacheService, -1);
        
        assertEquals(100, invalidDetector.getBatchSize(), "无效批量大小应使用默认值100");
        
        invalidDetector.shutdown();
    }

    @Test
    @DisplayName("测试缓存功能 - 启用缓存")
    void testCacheFunction_Enabled() {
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(Arrays.asList(similarArticle1));
        when(cacheService.getSimilarity(anyLong(), anyLong())).thenReturn(0.75);
        
        DuplicateCheckReport report = detector.detect(testArticle, config);
        
        assertNotNull(report, "检测报告不应为空");
        verify(cacheService, atLeastOnce()).getSimilarity(anyLong(), anyLong());
    }

    @Test
    @DisplayName("测试缓存功能 - 无缓存服务")
    void testCacheFunction_NoCacheService() {
        BatchDetector noCacheDetector = new BatchDetector(
            similarityCalculator, articleRepository, null);
        
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(Arrays.asList(similarArticle1));
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.75);
        
        DuplicateCheckReport report = noCacheDetector.detect(testArticle, config);
        
        assertNotNull(report, "检测报告不应为空");
        
        noCacheDetector.shutdown();
    }

    @Test
    @DisplayName("测试构造器 - 必要参数校验")
    void testConstructor_ParameterValidation() {
        assertThrows(NullPointerException.class, () -> {
            new BatchDetector(null, articleRepository, cacheService);
        }, "相似度计算器为空时应抛出异常");
        
        assertThrows(NullPointerException.class, () -> {
            new BatchDetector(similarityCalculator, null, cacheService);
        }, "文章仓储为空时应抛出异常");
    }

    @Test
    @DisplayName("测试关闭检测器")
    void testShutdown() {
        BatchDetector shutdownDetector = new BatchDetector(
            similarityCalculator, articleRepository, cacheService);
        
        assertDoesNotThrow(() -> shutdownDetector.shutdown(), "关闭检测器不应抛出异常");
    }

    @Test
    @DisplayName("测试性能 - 批量检测响应时间")
    void testPerformance_BatchDetection() {
        List<Article> articlesToCheck = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Article article = new Article((long) (i + 100), "待检测文章" + i, "内容" + i);
            article.setCreateTime(LocalDateTime.now());
            articlesToCheck.add(article);
        }
        
        List<Article> recentArticles = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Article article = new Article((long) (i + 200), "近期文章" + i, "内容" + i);
            article.setCreateTime(LocalDateTime.now());
            recentArticles.add(article);
        }
        
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(recentArticles);
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.5);
        
        long startTime = System.currentTimeMillis();
        List<DuplicateCheckReport> reports = detector.batchDetect(articlesToCheck, config);
        long elapsedTime = System.currentTimeMillis() - startTime;
        
        assertEquals(100, reports.size(), "应返回100份报告");
        assertTrue(elapsedTime < 60000, "批量检测应在60秒内完成，实际耗时: " + elapsedTime + "ms");
    }

    @Test
    @DisplayName("测试处理批次方法")
    void testProcessBatch() {
        List<Article> batch = Arrays.asList(
            new Article(1L, "文章1", "内容1"),
            new Article(2L, "文章2", "内容2")
        );
        
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(Arrays.asList(similarArticle1));
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.5);
        
        List<DuplicateCheckReport> reports = detector.processBatch(batch, config);
        
        assertNotNull(reports, "报告列表不应为空");
        assertEquals(2, reports.size(), "应返回2份报告");
    }
}
