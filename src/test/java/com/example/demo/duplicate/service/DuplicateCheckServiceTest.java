package com.example.demo.duplicate.service;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.detector.DuplicateDetector;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.DuplicateCheckReport;
import com.example.demo.duplicate.model.SimilarityResult;
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
 * DuplicateCheckService 测试类
 * 
 * 测试重复检测服务的核心功能：
 * 1. 单篇检测功能
 * 2. 批量检测功能
 * 3. 报告生成功能
 * 4. 配置更新功能
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("重复检测服务测试")
class DuplicateCheckServiceTest {

    @Mock
    private SimilarityCalculator calculator;

    @Mock
    private DuplicateDetector detector;

    @Mock
    private SimilarityCacheService cacheService;

    private DuplicateCheckConfig config;
    private Article testArticle;
    private Article similarArticle;
    private List<Article> existingArticles;

    @BeforeEach
    void setUp() {
        config = DuplicateCheckConfig.defaultConfig();
        
        lenient().when(calculator.getName()).thenReturn("SimHash");
        
        testArticle = new Article(1L, "测试文章标题", "这是一篇测试文章的内容，用于验证重复检测功能。");
        testArticle.setCreateTime(LocalDateTime.now());
        
        similarArticle = new Article(2L, "相似文章标题", "这是一篇相似文章的内容，用于验证重复检测功能。");
        similarArticle.setCreateTime(LocalDateTime.now());
        
        existingArticles = Arrays.asList(similarArticle);
    }

    @Test
    @DisplayName("测试单篇检测 - 正常情况")
    void testCheckDuplicate_Normal() {
        when(calculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.85);
        
        DuplicateCheckService service = new DuplicateCheckService(calculator, null, config, cacheService);
        DuplicateCheckReport report = service.checkDuplicate(testArticle, existingArticles);
        
        assertNotNull(report, "检测报告不应为空");
        assertEquals(1L, report.getArticleId(), "文章ID应匹配");
        assertNotNull(report.getCheckTime(), "检测时间不应为空");
        assertNotNull(report.getSummary(), "检测摘要不应为空");
    }

    @Test
    @DisplayName("测试单篇检测 - 文章为空")
    void testCheckDuplicate_NullArticle() {
        DuplicateCheckService service = new DuplicateCheckService(calculator, detector, config, cacheService);
        DuplicateCheckReport report = service.checkDuplicate(null, existingArticles);
        
        assertNotNull(report, "检测报告不应为空");
        assertNull(report.getArticleId(), "文章ID应为空");
        assertFalse(report.isHasDuplicate(), "不应标记为重复");
        assertNotNull(report.getSummary(), "检测摘要不应为空");
    }

    @Test
    @DisplayName("测试单篇检测 - 无相似文章")
    void testCheckDuplicate_NoSimilarArticles() {
        DuplicateCheckService service = new DuplicateCheckService(calculator, null, config, cacheService);
        DuplicateCheckReport report = service.checkDuplicate(testArticle, Collections.emptyList());
        
        assertNotNull(report, "检测报告不应为空");
        assertFalse(report.isHasDuplicate(), "不应标记为重复");
        assertTrue(report.getResults().isEmpty(), "结果列表应为空");
    }

    @Test
    @DisplayName("测试单篇检测 - 存在重复文章")
    void testCheckDuplicate_HasDuplicate() {
        when(cacheService.getSimilarity(anyLong(), anyLong())).thenReturn(null);
        when(calculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.85);
        
        DuplicateCheckService service = new DuplicateCheckService(calculator, null, config, cacheService);
        DuplicateCheckReport report = service.checkDuplicate(testArticle, existingArticles);
        
        assertNotNull(report, "检测报告不应为空");
        assertTrue(report.isHasDuplicate(), "应标记为重复");
        assertFalse(report.getResults().isEmpty(), "结果列表不应为空");
    }

    @Test
    @DisplayName("测试单篇检测 - 使用自定义配置")
    void testCheckDuplicate_CustomConfig() {
        DuplicateCheckConfig customConfig = new DuplicateCheckConfig(
            0.8, 60, "COSINE", 5, true, 0.6
        );
        
        when(calculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.75);
        
        DuplicateCheckService service = new DuplicateCheckService(calculator, null, config, cacheService);
        DuplicateCheckReport report = service.checkDuplicate(testArticle, existingArticles, customConfig);
        
        assertNotNull(report, "检测报告不应为空");
        assertEquals(1L, report.getArticleId(), "文章ID应匹配");
    }

    @Test
    @DisplayName("测试批量检测 - 正常情况")
    void testBatchCheck_Normal() {
        Article article1 = new Article(1L, "文章1", "内容1");
        Article article2 = new Article(2L, "文章2", "内容2");
        Article article3 = new Article(3L, "文章3", "内容3");
        List<Article> articles = Arrays.asList(article1, article2, article3);
        
        when(calculator.calculateSimilarity(any(Article.class), any(Article.class))).thenReturn(0.5);
        
        DuplicateCheckService service = new DuplicateCheckService(calculator, null, config, cacheService);
        List<DuplicateCheckReport> reports = service.batchCheck(articles, existingArticles);
        
        assertNotNull(reports, "报告列表不应为空");
        assertEquals(3, reports.size(), "应返回3份报告");
        
        for (DuplicateCheckReport report : reports) {
            assertNotNull(report, "每份报告不应为空");
            assertNotNull(report.getCheckTime(), "检测时间不应为空");
        }
    }

    @Test
    @DisplayName("测试批量检测 - 空列表")
    void testBatchCheck_EmptyList() {
        DuplicateCheckService service = new DuplicateCheckService(calculator, detector, config, cacheService);
        List<DuplicateCheckReport> reports = service.batchCheck(Collections.emptyList(), existingArticles);
        
        assertNotNull(reports, "报告列表不应为空");
        assertTrue(reports.isEmpty(), "报告列表应为空");
    }

    @Test
    @DisplayName("测试批量检测 - 空参数")
    void testBatchCheck_Null() {
        DuplicateCheckService service = new DuplicateCheckService(calculator, detector, config, cacheService);
        List<DuplicateCheckReport> reports = service.batchCheck(null, existingArticles);
        
        assertNotNull(reports, "报告列表不应为空");
        assertTrue(reports.isEmpty(), "报告列表应为空");
    }

    @Test
    @DisplayName("测试报告生成 - 正常情况")
    void testGenerateReport_Normal() {
        SimilarityResult result1 = new SimilarityResult();
        result1.setArticleId(1L);
        result1.setComparedArticleId(2L);
        result1.setSimilarity(0.85);
        result1.setAlgorithm("SIMHASH");
        result1.setCheckTime(LocalDateTime.now());
        
        SimilarityResult result2 = new SimilarityResult();
        result2.setArticleId(1L);
        result2.setComparedArticleId(3L);
        result2.setSimilarity(0.65);
        result2.setAlgorithm("SIMHASH");
        result2.setCheckTime(LocalDateTime.now());
        
        List<SimilarityResult> results = Arrays.asList(result1, result2);
        
        DuplicateCheckService service = new DuplicateCheckService(calculator, detector, config, cacheService);
        DuplicateCheckReport report = service.generateReport(testArticle, results);
        
        assertNotNull(report, "报告不应为空");
        assertEquals(1L, report.getArticleId(), "文章ID应匹配");
        assertTrue(report.isHasDuplicate(), "应标记为重复");
        assertEquals(2, report.getResults().size(), "应有2个结果");
        assertNotNull(report.getSummary(), "摘要不应为空");
    }

    @Test
    @DisplayName("测试报告生成 - 空结果")
    void testGenerateReport_EmptyResults() {
        DuplicateCheckService service = new DuplicateCheckService(calculator, detector, config, cacheService);
        DuplicateCheckReport report = service.generateReport(testArticle, Collections.emptyList());
        
        assertNotNull(report, "报告不应为空");
        assertFalse(report.isHasDuplicate(), "不应标记为重复");
        assertTrue(report.getResults().isEmpty(), "结果列表应为空");
        assertNotNull(report.getSummary(), "摘要不应为空");
    }

    @Test
    @DisplayName("测试报告生成 - 文章为空")
    void testGenerateReport_NullArticle() {
        DuplicateCheckService service = new DuplicateCheckService(calculator, detector, config, cacheService);
        DuplicateCheckReport report = service.generateReport(null, new ArrayList<>());
        
        assertNotNull(report, "报告不应为空");
        assertNull(report.getArticleId(), "文章ID应为空");
        assertFalse(report.isHasDuplicate(), "不应标记为重复");
    }

    @Test
    @DisplayName("测试报告生成 - 结果按相似度排序")
    void testGenerateReport_ResultsSorted() {
        SimilarityResult result1 = new SimilarityResult();
        result1.setArticleId(1L);
        result1.setComparedArticleId(2L);
        result1.setSimilarity(0.5);
        
        SimilarityResult result2 = new SimilarityResult();
        result2.setArticleId(1L);
        result2.setComparedArticleId(3L);
        result2.setSimilarity(0.9);
        
        SimilarityResult result3 = new SimilarityResult();
        result3.setArticleId(1L);
        result3.setComparedArticleId(4L);
        result3.setSimilarity(0.7);
        
        List<SimilarityResult> results = Arrays.asList(result1, result2, result3);
        
        DuplicateCheckService service = new DuplicateCheckService(calculator, detector, config, cacheService);
        DuplicateCheckReport report = service.generateReport(testArticle, results);
        
        assertEquals(3, report.getResults().size(), "应有3个结果");
        assertEquals(0.9, report.getResults().get(0).getSimilarity(), 0.001, "第一个结果应为最高相似度");
        assertEquals(0.7, report.getResults().get(1).getSimilarity(), 0.001, "第二个结果应为中等相似度");
        assertEquals(0.5, report.getResults().get(2).getSimilarity(), 0.001, "第三个结果应为最低相似度");
    }

    @Test
    @DisplayName("测试配置更新 - 设置新配置")
    void testSetConfig() {
        DuplicateCheckConfig newConfig = new DuplicateCheckConfig(
            0.6, 45, "SimHash", 15, false, 0.7
        );
        
        DuplicateCheckService service = new DuplicateCheckService(calculator, detector, config, cacheService);
        service.setConfig(newConfig);
        
        assertEquals(newConfig, service.getConfig(), "配置应更新");
    }

    @Test
    @DisplayName("测试配置更新 - 设置空配置使用默认值")
    void testSetConfig_Null() {
        DuplicateCheckService service = new DuplicateCheckService(calculator, detector, config, cacheService);
        service.setConfig(null);
        
        assertNotNull(service.getConfig(), "配置不应为空");
        assertEquals(DuplicateCheckConfig.defaultConfig().getThreshold(), 
                     service.getConfig().getThreshold(), 0.001, "应使用默认阈值");
    }

    @Test
    @DisplayName("测试设置检测器")
    void testSetDetector() {
        DuplicateDetector newDetector = mock(DuplicateDetector.class);
        when(newDetector.getName()).thenReturn("TestDetector");
        
        DuplicateCheckService service = new DuplicateCheckService(calculator, detector, config, cacheService);
        service.setDetector(newDetector);
        
        assertEquals(newDetector, service.getDetector(), "检测器应更新");
    }

    @Test
    @DisplayName("测试设置相似度计算器")
    void testSetCalculator() {
        SimilarityCalculator newCalculator = mock(SimilarityCalculator.class);
        when(newCalculator.getName()).thenReturn("TestCalculator");
        
        DuplicateCheckService service = new DuplicateCheckService(calculator, detector, config, cacheService);
        service.setCalculator(newCalculator);
        
        assertEquals(newCalculator, service.getCalculator(), "计算器应更新");
    }

    @Test
    @DisplayName("测试设置缓存服务")
    void testSetCacheService() {
        SimilarityCacheService newCacheService = mock(SimilarityCacheService.class);
        
        DuplicateCheckService service = new DuplicateCheckService(calculator, detector, config, cacheService);
        service.setCacheService(newCacheService);
        
        assertEquals(newCacheService, service.getCacheService(), "缓存服务应更新");
    }

    @Test
    @DisplayName("测试使用检测器进行检测")
    void testCheckDuplicate_WithDetector() {
        List<SimilarityResult> mockResults = new ArrayList<>();
        SimilarityResult result = new SimilarityResult();
        result.setArticleId(1L);
        result.setComparedArticleId(2L);
        result.setSimilarity(0.85);
        result.setAlgorithm("SimHash");
        result.setCheckTime(LocalDateTime.now());
        mockResults.add(result);
        
        when(detector.findSimilarArticles(any(Article.class), anyList(), any(DuplicateCheckConfig.class)))
            .thenReturn(mockResults);
        
        DuplicateCheckService service = new DuplicateCheckService(calculator, detector, config, cacheService);
        DuplicateCheckReport report = service.checkDuplicate(testArticle, existingArticles);
        
        assertNotNull(report, "报告不应为空");
        assertTrue(report.isHasDuplicate(), "应标记为重复");
        verify(detector, times(1)).findSimilarArticles(any(Article.class), anyList(), any(DuplicateCheckConfig.class));
    }

    @Test
    @DisplayName("测试缓存功能 - 启用缓存")
    void testCheckDuplicate_WithCacheEnabled() {
        when(cacheService.getSimilarity(anyLong(), anyLong())).thenReturn(0.75);
        
        DuplicateCheckService service = new DuplicateCheckService(calculator, null, config, cacheService);
        DuplicateCheckReport report = service.checkDuplicate(testArticle, existingArticles);
        
        assertNotNull(report, "报告不应为空");
        verify(cacheService, atLeastOnce()).getSimilarity(anyLong(), anyLong());
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        DuplicateCheckService defaultService = new DuplicateCheckService();
        
        assertNotNull(defaultService, "服务不应为空");
        assertNotNull(defaultService.getConfig(), "配置不应为空");
        assertNotNull(defaultService.getCalculator(), "计算器不应为空");
    }

    @Test
    @DisplayName("测试带配置的构造函数")
    void testConstructorWithConfig() {
        DuplicateCheckConfig customConfig = new DuplicateCheckConfig(
            0.8, 60, "COSINE", 20, true, 0.6
        );
        
        DuplicateCheckService configService = new DuplicateCheckService(customConfig);
        
        assertNotNull(configService, "服务不应为空");
        assertEquals(customConfig, configService.getConfig(), "配置应匹配");
    }
}
