package com.example.demo.duplicate.detector.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.DuplicateCheckReport;
import com.example.demo.duplicate.model.SimilarityResult;
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
    private SimilarityCacheService cacheService;

    private DuplicateCheckConfig config;
    private Article testArticle;
    private Article similarArticle;
    private List<Article> existingArticles;
    private BatchDetector detector;

    @BeforeEach
    void setUp() {
        config = DuplicateCheckConfig.defaultConfig();
        
        lenient().when(similarityCalculator.getName()).thenReturn("SimHash");
        
        testArticle = new Article(1L, "测试文章", "这是一篇测试文章的内容。");
        testArticle.setCreateTime(LocalDateTime.now());
        
        similarArticle = new Article(2L, "相似文章", "这是一篇相似文章的内容。");
        similarArticle.setCreateTime(LocalDateTime.now());
        
        existingArticles = Arrays.asList(similarArticle);
        
        detector = new BatchDetector(similarityCalculator, cacheService);
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
        Article article1 = new Article(1L, "文章1", "内容1");
        Article article2 = new Article(2L, "文章2", "内容2");
        List<Article> articles = Arrays.asList(article1, article2);
        
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.5);
        
        List<DuplicateCheckReport> reports = detector.batchDetect(articles, existingArticles, config);
        
        assertNotNull(reports, "报告列表不应为空");
        assertEquals(2, reports.size(), "应返回2份报告");
    }

    @Test
    @DisplayName("测试批量检测 - 空列表")
    void testBatchDetect_EmptyList() {
        List<DuplicateCheckReport> reports = detector.batchDetect(Collections.emptyList(), existingArticles, config);
        
        assertNotNull(reports, "报告列表不应为空");
        assertTrue(reports.isEmpty(), "报告列表应为空");
    }

    @Test
    @DisplayName("测试批量检测 - 默认配置")
    void testBatchDetect_DefaultConfig() {
        Article article1 = new Article(1L, "文章1", "内容1");
        List<Article> articles = Arrays.asList(article1);
        
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.5);
        
        List<DuplicateCheckReport> reports = detector.batchDetect(articles, existingArticles, null);
        
        assertNotNull(reports, "报告列表不应为空");
        assertEquals(1, reports.size(), "应返回1份报告");
    }

    @Test
    @DisplayName("测试单篇检测功能")
    void testDetect_SingleArticle() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.5);
        
        DuplicateCheckReport report = detector.detect(testArticle, existingArticles, config);
        
        assertNotNull(report, "报告不应为空");
        assertEquals(1L, report.getArticleId(), "文章ID应匹配");
    }

    @Test
    @DisplayName("测试单篇检测 - 无已有文章")
    void testDetect_NoExistingArticles() {
        DuplicateCheckReport report = detector.detect(testArticle, Collections.emptyList(), config);
        
        assertNotNull(report, "报告不应为空");
        assertFalse(report.isHasDuplicate(), "不应标记为重复");
    }

    @Test
    @DisplayName("测试查找相似文章")
    void testFindSimilarArticles() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.85);
        
        List<SimilarityResult> results = detector.findSimilarArticles(testArticle, existingArticles, config);
        
        assertNotNull(results, "结果列表不应为空");
        assertFalse(results.isEmpty(), "结果列表不应为空");
    }

    @Test
    @DisplayName("测试判断两篇文章是否重复 - 重复")
    void testIsDuplicate_True() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.85);
        
        boolean isDuplicate = detector.isDuplicate(testArticle, similarArticle, 0.8);
        
        assertTrue(isDuplicate, "应判定为重复");
    }

    @Test
    @DisplayName("测试判断两篇文章是否重复 - 不重复")
    void testIsDuplicate_False() {
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.5);
        
        boolean isDuplicate = detector.isDuplicate(testArticle, similarArticle, 0.8);
        
        assertFalse(isDuplicate, "不应判定为重复");
    }

    @Test
    @DisplayName("测试并行处理效果 - 大批量检测")
    void testParallelProcessing_LargeBatch() {
        List<Article> articles = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            articles.add(new Article((long) i, "文章" + i, "内容" + i));
        }
        
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.3);
        
        long startTime = System.currentTimeMillis();
        List<DuplicateCheckReport> reports = detector.batchDetect(articles, existingArticles, config);
        long elapsed = System.currentTimeMillis() - startTime;
        
        assertEquals(100, reports.size(), "应返回100份报告");
        assertTrue(elapsed < 10000, "批量检测应在10秒内完成，实际耗时: " + elapsed + "ms");
    }

    @Test
    @DisplayName("测试批量大小配置 - 默认值")
    void testBatchSize_Default() {
        assertEquals(100, detector.getBatchSize(), "默认批量大小应为100");
    }

    @Test
    @DisplayName("测试批量大小配置 - 自定义值")
    void testBatchSize_Custom() {
        BatchDetector customDetector = new BatchDetector(similarityCalculator, cacheService, 50);
        assertEquals(50, customDetector.getBatchSize(), "批量大小应为50");
        customDetector.shutdown();
    }

    @Test
    @DisplayName("测试批量大小配置 - 无效值使用默认值")
    void testBatchSize_Invalid() {
        BatchDetector invalidDetector = new BatchDetector(similarityCalculator, cacheService, -1);
        assertEquals(100, invalidDetector.getBatchSize(), "无效批量大小应使用默认值100");
        invalidDetector.shutdown();
    }

    @Test
    @DisplayName("测试缓存功能")
    void testCache() {
        when(cacheService.getSimilarity(anyLong(), anyLong())).thenReturn(0.75);
        
        detector.detect(testArticle, existingArticles, config);
        
        verify(cacheService, atLeastOnce()).getSimilarity(anyLong(), anyLong());
    }

    @Test
    @DisplayName("测试性能 - 批量检测响应时间")
    void testPerformance_BatchDetect() {
        List<Article> articles = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            articles.add(new Article((long) i, "文章" + i, "内容" + i));
        }
        
        when(similarityCalculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.3);
        
        long startTime = System.currentTimeMillis();
        detector.batchDetect(articles, existingArticles, config);
        long elapsed = System.currentTimeMillis() - startTime;
        
        assertTrue(elapsed < 5000, "批量检测应在5秒内完成，实际耗时: " + elapsed + "ms");
    }

    @Test
    @DisplayName("测试构造器参数校验 - 计算器为空")
    void testConstructor_NullCalculator() {
        assertThrows(NullPointerException.class, () -> {
            new BatchDetector(null, cacheService);
        }, "计算器为空时应抛出异常");
    }

    @Test
    @DisplayName("测试获取检测器名称")
    void testGetName() {
        assertEquals("BatchDetector", detector.getName(), "检测器名称应匹配");
    }

    @Test
    @DisplayName("测试关闭检测器")
    void testShutdown() {
        BatchDetector shutdownDetector = new BatchDetector(similarityCalculator, cacheService);
        assertDoesNotThrow(() -> shutdownDetector.shutdown(), "关闭检测器不应抛出异常");
    }
}
