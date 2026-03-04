package com.example.demo.duplicate.detector.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.DuplicateCheckReport;
import com.example.demo.duplicate.model.SimilarityResult;
import com.example.demo.duplicate.repository.ArticleRepository;
import com.example.demo.duplicate.service.SimilarityCacheService;
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
 * RealTimeDetector 测试类
 * 
 * 测试实时检测器的核心功能：
 * 1. 实时检测功能
 * 2. 缓存优化效果
 * 3. 性能（响应时间）
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("实时检测器测试")
class RealTimeDetectorTest {

    @Mock
    private SimilarityCalculator similarityCalculator;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private SimilarityCacheService cacheService;

    private DuplicateCheckConfig config;
    private Article testArticle;
    private Article similarArticle1;
    private Article similarArticle2;

    @BeforeEach
    void setUp() {
        config = DuplicateCheckConfig.defaultConfig();
        
        lenient().when(similarityCalculator.getName()).thenReturn("SimHash");
        
        testArticle = new Article(1L, "测试文章标题", "这是一篇测试文章的内容，用于验证实时检测功能。");
        testArticle.setCreateTime(LocalDateTime.now());
        
        similarArticle1 = new Article(2L, "相似文章1", "这是一篇相似文章的内容，用于验证实时检测功能。");
        similarArticle1.setCreateTime(LocalDateTime.now());
        
        similarArticle2 = new Article(3L, "相似文章2", "这是另一篇相似文章的内容，用于验证实时检测功能。");
        similarArticle2.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试实时检测 - 正常情况")
    void testDetect_Normal() {
        List<Article> recentArticles = Arrays.asList(similarArticle1, similarArticle2);
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(recentArticles);
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.85, 0.65);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, cacheService);
        DuplicateCheckReport report = detector.detect(testArticle, config);
        
        assertNotNull(report, "检测报告不应为空");
        assertEquals(1L, report.getArticleId(), "文章ID应匹配");
        assertNotNull(report.getCheckTime(), "检测时间不应为空");
        assertNotNull(report.getSummary(), "检测摘要不应为空");
    }

    @Test
    @DisplayName("测试实时检测 - 无近期文章")
    void testDetect_NoRecentArticles() {
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(Collections.emptyList());
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, cacheService);
        DuplicateCheckReport report = detector.detect(testArticle, config);
        
        assertNotNull(report, "检测报告不应为空");
        assertFalse(report.isHasDuplicate(), "不应标记为重复");
        assertTrue(report.getResults().isEmpty(), "结果列表应为空");
        assertNotNull(report.getSummary(), "检测摘要不应为空");
    }

    @Test
    @DisplayName("测试实时检测 - 存在重复文章")
    void testDetect_HasDuplicate() {
        List<Article> recentArticles = Arrays.asList(similarArticle1);
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(recentArticles);
        when(cacheService.getSimilarity(anyLong(), anyLong())).thenReturn(null);
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.85);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, cacheService);
        DuplicateCheckReport report = detector.detect(testArticle, config);
        
        assertNotNull(report, "检测报告不应为空");
        assertTrue(report.isHasDuplicate(), "应标记为重复");
        assertFalse(report.getResults().isEmpty(), "结果列表不应为空");
    }

    @Test
    @DisplayName("测试实时检测 - 无重复文章")
    void testDetect_NoDuplicate() {
        List<Article> recentArticles = Arrays.asList(similarArticle1);
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(recentArticles);
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.5);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, cacheService);
        DuplicateCheckReport report = detector.detect(testArticle, config);
        
        assertNotNull(report, "检测报告不应为空");
        assertFalse(report.isHasDuplicate(), "不应标记为重复");
    }

    @Test
    @DisplayName("测试实时检测 - 使用默认配置")
    void testDetect_DefaultConfig() {
        List<Article> recentArticles = Arrays.asList(similarArticle1);
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(recentArticles);
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.75);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, cacheService);
        DuplicateCheckReport report = detector.detect(testArticle, null);
        
        assertNotNull(report, "检测报告不应为空");
        assertEquals(1L, report.getArticleId(), "文章ID应匹配");
    }

    @Test
    @DisplayName("测试批量检测 - 正常情况")
    void testBatchDetect_Normal() {
        Article article1 = new Article(1L, "文章1", "内容1");
        Article article2 = new Article(2L, "文章2", "内容2");
        List<Article> articles = Arrays.asList(article1, article2);
        
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(Arrays.asList(similarArticle1));
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.5);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, cacheService);
        List<DuplicateCheckReport> reports = detector.batchDetect(articles, config);
        
        assertNotNull(reports, "报告列表不应为空");
        assertEquals(2, reports.size(), "应返回2份报告");
        
        for (DuplicateCheckReport report : reports) {
            assertNotNull(report, "每份报告不应为空");
            assertNotNull(report.getCheckTime(), "检测时间不应为空");
        }
    }

    @Test
    @DisplayName("测试查找相似文章 - 正常情况")
    void testFindSimilarArticles_Normal() {
        List<Article> recentArticles = Arrays.asList(similarArticle1, similarArticle2);
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(recentArticles);
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.85, 0.65);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, cacheService);
        List<SimilarityResult> results = detector.findSimilarArticles(testArticle, config);
        
        assertNotNull(results, "结果列表不应为空");
        assertFalse(results.isEmpty(), "结果列表不应为空");
        assertEquals(2, results.size(), "应有2个结果");
    }

    @Test
    @DisplayName("测试查找相似文章 - 无近期文章")
    void testFindSimilarArticles_NoRecentArticles() {
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(Collections.emptyList());
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, cacheService);
        List<SimilarityResult> results = detector.findSimilarArticles(testArticle, config);
        
        assertNotNull(results, "结果列表不应为空");
        assertTrue(results.isEmpty(), "结果列表应为空");
    }

    @Test
    @DisplayName("测试判断两篇文章是否重复 - 重复")
    void testIsDuplicate_True() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.85);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, cacheService);
        boolean isDuplicate = detector.isDuplicate(testArticle, similarArticle1, 0.7);
        
        assertTrue(isDuplicate, "应判定为重复");
    }

    @Test
    @DisplayName("测试判断两篇文章是否重复 - 不重复")
    void testIsDuplicate_False() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.5);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, cacheService);
        boolean isDuplicate = detector.isDuplicate(testArticle, similarArticle1, 0.7);
        
        assertFalse(isDuplicate, "不应判定为重复");
    }

    @Test
    @DisplayName("测试判断两篇文章是否重复 - 文章为空")
    void testIsDuplicate_NullArticle() {
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, cacheService);
        
        boolean isDuplicate = detector.isDuplicate(null, similarArticle1, 0.7);
        assertFalse(isDuplicate, "文章为空时应返回false");
        
        isDuplicate = detector.isDuplicate(testArticle, null, 0.7);
        assertFalse(isDuplicate, "文章为空时应返回false");
    }

    @Test
    @DisplayName("测试获取检测器名称")
    void testGetName() {
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, cacheService);
        assertEquals("RealTimeDetector", detector.getName(), "检测器名称应为RealTimeDetector");
    }

    @Test
    @DisplayName("测试缓存优化 - 命中缓存")
    void testCacheOptimization_CacheHit() {
        List<Article> recentArticles = Arrays.asList(similarArticle1);
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(recentArticles);
        when(cacheService.getSimilarity(1L, 2L)).thenReturn(0.75);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, cacheService);
        DuplicateCheckReport report = detector.detect(testArticle, config);
        
        assertNotNull(report, "检测报告不应为空");
        verify(cacheService, times(1)).getSimilarity(1L, 2L);
    }

    @Test
    @DisplayName("测试缓存优化 - 无缓存服务")
    void testCacheOptimization_NoCacheService() {
        List<Article> recentArticles = Arrays.asList(similarArticle1);
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(recentArticles);
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.75);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, null);
        DuplicateCheckReport report = detector.detect(testArticle, config);
        
        assertNotNull(report, "检测报告不应为空");
        verify(similarityCalculator, times(1)).calculateSimilarity(any(Article.class), any(Article.class));
    }

    @Test
    @DisplayName("测试性能 - 单次检测响应时间")
    void testPerformance_SingleDetection() {
        List<Article> recentArticles = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Article article = new Article((long) (i + 10), "文章" + i, "内容" + i);
            article.setCreateTime(LocalDateTime.now());
            recentArticles.add(article);
        }
        
        when(articleRepository.findRecentArticles(anyInt())).thenReturn(recentArticles);
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class)))
            .thenReturn(0.5);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, articleRepository, config, cacheService);
        
        long startTime = System.currentTimeMillis();
        DuplicateCheckReport report = detector.detect(testArticle, config);
        long elapsedTime = System.currentTimeMillis() - startTime;
        
        assertNotNull(report, "检测报告不应为空");
        assertTrue(elapsedTime < 5000, "单次检测应在5秒内完成，实际耗时: " + elapsedTime + "ms");
    }

    @Test
    @DisplayName("测试构造器 - 必要参数校验")
    void testConstructor_ParameterValidation() {
        assertThrows(NullPointerException.class, () -> {
            new RealTimeDetector(null, articleRepository, config, cacheService);
        }, "相似度计算器为空时应抛出异常");
        
        assertThrows(NullPointerException.class, () -> {
            new RealTimeDetector(similarityCalculator, null, config, cacheService);
        }, "文章仓储为空时应抛出异常");
    }

    @Test
    @DisplayName("测试构造器 - 默认配置")
    void testConstructor_DefaultConfig() {
        RealTimeDetector detectorWithNullConfig = new RealTimeDetector(
            similarityCalculator, articleRepository, null, cacheService);
        
        assertNotNull(detectorWithNullConfig, "检测器不应为空");
        assertEquals("RealTimeDetector", detectorWithNullConfig.getName(), "检测器名称应正确");
    }
}
