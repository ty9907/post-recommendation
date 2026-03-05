package com.example.demo.duplicate.detector.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.DuplicateCheckReport;
import com.example.demo.duplicate.model.SimilarityResult;
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
    private SimilarityCacheService cacheService;

    private DuplicateCheckConfig config;
    private Article testArticle;
    private Article similarArticle;
    private List<Article> existingArticles;

    @BeforeEach
    void setUp() {
        config = DuplicateCheckConfig.defaultConfig();
        
        lenient().when(similarityCalculator.getName()).thenReturn("SimHash");
        
        testArticle = new Article(1L, "测试文章", "这是一篇测试文章的内容。");
        testArticle.setCreateTime(LocalDateTime.now());
        
        similarArticle = new Article(2L, "相似文章", "这是一篇相似文章的内容。");
        similarArticle.setCreateTime(LocalDateTime.now());
        
        existingArticles = Arrays.asList(similarArticle);
    }

    @Test
    @DisplayName("测试实时检测 - 正常情况")
    void testDetect_Normal() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.5);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, config, cacheService);
        DuplicateCheckReport report = detector.detect(testArticle, existingArticles, config);
        
        assertNotNull(report, "报告不应为空");
        assertEquals(1L, report.getArticleId(), "文章ID应匹配");
    }

    @Test
    @DisplayName("测试实时检测 - 无已有文章")
    void testDetect_NoExistingArticles() {
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, config, cacheService);
        DuplicateCheckReport report = detector.detect(testArticle, Collections.emptyList(), config);
        
        assertNotNull(report, "报告不应为空");
        assertFalse(report.isHasDuplicate(), "不应标记为重复");
    }

    @Test
    @DisplayName("测试实时检测 - 存在重复")
    void testDetect_HasDuplicate() {
        when(cacheService.getSimilarity(anyLong(), anyLong())).thenReturn(null);
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.85);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, config, cacheService);
        DuplicateCheckReport report = detector.detect(testArticle, existingArticles, config);
        
        assertNotNull(report, "报告不应为空");
        assertTrue(report.isHasDuplicate(), "应标记为重复");
    }

    @Test
    @DisplayName("测试实时检测 - 无重复")
    void testDetect_NoDuplicate() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.3);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, config, cacheService);
        DuplicateCheckReport report = detector.detect(testArticle, existingArticles, config);
        
        assertNotNull(report, "报告不应为空");
        assertFalse(report.isHasDuplicate(), "不应标记为重复");
    }

    @Test
    @DisplayName("测试实时检测 - 默认配置")
    void testDetect_DefaultConfig() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.5);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, config, cacheService);
        DuplicateCheckReport report = detector.detect(testArticle, existingArticles, null);
        
        assertNotNull(report, "报告不应为空");
    }

    @Test
    @DisplayName("测试批量检测")
    void testBatchDetect() {
        Article article1 = new Article(1L, "文章1", "内容1");
        Article article2 = new Article(2L, "文章2", "内容2");
        List<Article> articles = Arrays.asList(article1, article2);
        
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.5);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, config, cacheService);
        List<DuplicateCheckReport> reports = detector.batchDetect(articles, existingArticles, config);
        
        assertNotNull(reports, "报告列表不应为空");
        assertEquals(2, reports.size(), "应返回2份报告");
    }

    @Test
    @DisplayName("测试查找相似文章")
    void testFindSimilarArticles() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.85);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, config, cacheService);
        List<SimilarityResult> results = detector.findSimilarArticles(testArticle, existingArticles, config);
        
        assertNotNull(results, "结果列表不应为空");
        assertFalse(results.isEmpty(), "结果列表不应为空");
    }

    @Test
    @DisplayName("测试判断两篇文章是否重复 - 重复")
    void testIsDuplicate_True() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.85);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, config, cacheService);
        boolean isDuplicate = detector.isDuplicate(testArticle, similarArticle, 0.8);
        
        assertTrue(isDuplicate, "应判定为重复");
    }

    @Test
    @DisplayName("测试判断两篇文章是否重复 - 不重复")
    void testIsDuplicate_False() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.5);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, config, cacheService);
        boolean isDuplicate = detector.isDuplicate(testArticle, similarArticle, 0.8);
        
        assertFalse(isDuplicate, "不应判定为重复");
    }

    @Test
    @DisplayName("测试判断两篇文章是否重复 - 文章为空")
    void testIsDuplicate_NullArticle() {
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, config, cacheService);
        
        assertFalse(detector.isDuplicate(null, similarArticle, 0.8), "不应判定为重复");
        assertFalse(detector.isDuplicate(testArticle, null, 0.8), "不应判定为重复");
    }

    @Test
    @DisplayName("测试缓存优化效果 - 命中缓存")
    void testCache_Hit() {
        when(cacheService.getSimilarity(anyLong(), anyLong())).thenReturn(0.75);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, config, cacheService);
        detector.detect(testArticle, existingArticles, config);
        
        verify(cacheService, atLeastOnce()).getSimilarity(anyLong(), anyLong());
        verify(similarityCalculator, never()).calculateSimilarity(any(Article.class), any(Article.class));
    }

    @Test
    @DisplayName("测试缓存优化效果 - 无缓存服务")
    void testCache_NoCacheService() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.5);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, config, null);
        detector.detect(testArticle, existingArticles, config);
        
        verify(similarityCalculator, times(1)).calculateSimilarity(any(Article.class), any(Article.class));
    }

    @Test
    @DisplayName("测试性能 - 单次检测响应时间")
    void testPerformance_SingleDetect() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.5);
        
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, config, cacheService);
        
        long startTime = System.currentTimeMillis();
        detector.detect(testArticle, existingArticles, config);
        long elapsed = System.currentTimeMillis() - startTime;
        
        assertTrue(elapsed < 100, "单次检测应在100ms内完成，实际耗时: " + elapsed + "ms");
    }

    @Test
    @DisplayName("测试构造器参数校验")
    void testConstructor_NullCalculator() {
        assertThrows(NullPointerException.class, () -> {
            new RealTimeDetector(null, config, cacheService);
        }, "计算器为空时应抛出异常");
    }

    @Test
    @DisplayName("测试获取检测器名称")
    void testGetName() {
        RealTimeDetector detector = new RealTimeDetector(similarityCalculator, config, cacheService);
        assertEquals("RealTimeDetector", detector.getName(), "检测器名称应匹配");
    }
}
