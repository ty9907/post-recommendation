package com.example.demo.duplicate.algorithm.impl;

import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.SimilarityResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TFIDFSimilarityCalculator测试类
 * 测试基于TF-IDF的相似度计算器的各种功能
 */
public class TFIDFSimilarityCalculatorTest {

    private TFIDFSimilarityCalculator calculator;

    @BeforeEach
    public void setUp() {
        calculator = new TFIDFSimilarityCalculator();
    }

    /**
     * 创建测试用的文章对象
     * @param id 文章ID
     * @param title 文章标题
     * @param content 文章内容
     * @return 文章对象
     */
    private Article createArticle(Long id, String title, String content) {
        return new Article(id, title, content);
    }

    /**
     * 测试TF-IDF向量化 - 相同内容
     * 验证相同内容的文章相似度为1.0
     */
    @Test
    public void testTFIDFVectorizationWithSameContent() {
        String content = "Java是一种面向对象的编程语言，具有跨平台、安全性高等特点。";
        
        Article article1 = createArticle(1L, "Java介绍", content);
        Article article2 = createArticle(2L, "Java介绍", content);

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertEquals(1.0, similarity, 0.001);
    }

    /**
     * 测试TF-IDF向量化 - 相似内容
     * 验证相似内容的文章具有较高的相似度
     */
    @Test
    public void testTFIDFVectorizationWithSimilarContent() {
        Article article1 = createArticle(1L, "Java编程", 
                "Java是一种面向对象的编程语言。Java具有跨平台特性，广泛应用于企业级开发。");
        Article article2 = createArticle(2L, "Java开发", 
                "Java是一种流行的编程语言。Java的跨平台特性使其在企业级开发中广泛应用。");

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertTrue(similarity > 0.5);
        assertTrue(similarity <= 1.0);
    }

    /**
     * 测试TF-IDF向量化 - 不同内容
     * 验证不同内容的文章具有较低的相似度
     */
    @Test
    public void testTFIDFVectorizationWithDifferentContent() {
        Article article1 = createArticle(1L, "Java编程", 
                "Java是一种面向对象的编程语言，广泛应用于企业级开发。");
        Article article2 = createArticle(2L, "美食烹饪", 
                "红烧肉是一道经典的中国菜肴，需要选用五花肉，慢火炖煮。");

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertTrue(similarity < 0.3);
    }

    /**
     * 测试余弦相似度计算 - 正交向量
     * 验证完全不同的词汇返回较低的相似度
     */
    @Test
    public void testCosineSimilarityWithOrthogonalVectors() {
        Article article1 = createArticle(1L, "技术文章", 
                "编程开发算法数据结构");
        Article article2 = createArticle(2L, "美食文章", 
                "烹饪食材菜谱美食");

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertTrue(similarity < 0.2);
    }

    /**
     * 测试余弦相似度计算 - 相同向量
     * 验证相同内容返回相似度1.0
     */
    @Test
    public void testCosineSimilarityWithSameVectors() {
        String content = "机器学习是人工智能的一个分支，深度学习是机器学习的一种方法。";
        
        Article article1 = createArticle(1L, "AI文章", content);
        Article article2 = createArticle(2L, "AI文章", content);

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertEquals(1.0, similarity, 0.001);
    }

    /**
     * 测试相同文章
     * 验证同一篇文章的相似度为1.0
     */
    @Test
    public void testSameArticle() {
        Article article = createArticle(1L, "Java编程", 
                "Java是一种面向对象的编程语言。");

        double similarity = calculator.calculateSimilarity(article, article);

        assertEquals(1.0, similarity, 0.001);
    }

    /**
     * 测试不同文章
     * 验证完全不同的文章相似度较低
     */
    @Test
    public void testDifferentArticles() {
        Article article1 = createArticle(1L, "技术文档", 
                "Spring框架是Java企业级开发的主流框架，提供了依赖注入和面向切面编程。");
        Article article2 = createArticle(2L, "旅游攻略", 
                "北京是中国的首都，有着悠久的历史和丰富的文化遗产。");

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertTrue(similarity < 0.3);
    }

    /**
     * 测试相似文章
     * 验证内容相似的文章具有较高的相似度
     */
    @Test
    public void testSimilarArticles() {
        Article article1 = createArticle(1L, "Spring框架介绍", 
                "Spring框架是Java平台上的开源框架，提供了控制反转和面向切面编程的支持。");
        Article article2 = createArticle(2L, "Spring框架概述", 
                "Spring是一个Java开源框架，支持控制反转和面向切面编程，简化了企业级开发。");

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertTrue(similarity > 0.4);
    }

    /**
     * 测试空内容文章
     * 验证空内容返回相似度0.0
     */
    @Test
    public void testEmptyContent() {
        Article article1 = createArticle(1L, "", "");
        Article article2 = createArticle(2L, "测试文章", "这是测试内容");

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertEquals(0.0, similarity, 0.001);
    }

    /**
     * 测试null文章对象
     * 验证null文章返回相似度0.0
     */
    @Test
    public void testNullArticle() {
        Article article = createArticle(1L, "测试文章", "测试内容");

        double similarity1 = calculator.calculateSimilarity(null, article);
        double similarity2 = calculator.calculateSimilarity(article, null);
        double similarity3 = calculator.calculateSimilarity(null, null);

        assertEquals(0.0, similarity1, 0.001);
        assertEquals(0.0, similarity2, 0.001);
        assertEquals(0.0, similarity3, 0.001);
    }

    /**
     * 测试批量相似度计算
     * 验证批量计算返回正确的结果列表
     */
    @Test
    public void testCalculateSimilarities() {
        Article article = createArticle(1L, "Java编程", 
                "Java是一种面向对象的编程语言，广泛应用于企业级开发。");
        Article article2 = createArticle(2L, "Java开发", 
                "Java是一种流行的编程语言，在企业级开发中应用广泛。");
        Article article3 = createArticle(3L, "美食烹饪", 
                "红烧肉是一道经典的中国菜肴，需要选用五花肉。");

        List<SimilarityResult> results = calculator.calculateSimilarities(
                article, Arrays.asList(article2, article3));

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.get(0).getSimilarity() >= results.get(1).getSimilarity());
    }

    /**
     * 测试批量计算 - 空列表
     * 验证空列表返回空结果
     */
    @Test
    public void testCalculateSimilaritiesWithEmptyList() {
        Article article = createArticle(1L, "测试文章", "测试内容");

        List<SimilarityResult> results = calculator.calculateSimilarities(article, new ArrayList<>());

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * 测试批量计算 - null列表
     * 验证null列表返回空结果
     */
    @Test
    public void testCalculateSimilaritiesWithNullList() {
        Article article = createArticle(1L, "测试文章", "测试内容");

        List<SimilarityResult> results = calculator.calculateSimilarities(article, null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * 测试批量计算 - 排除自身
     * 验证批量计算时排除与自身的比较
     */
    @Test
    public void testCalculateSimilaritiesExcludeSelf() {
        Article article = createArticle(1L, "Java编程", "Java是一种编程语言。");
        Article article2 = createArticle(2L, "Python编程", "Python是一种编程语言。");

        List<SimilarityResult> results = calculator.calculateSimilarities(
                article, Arrays.asList(article, article2));

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(Long.valueOf(2L), results.get(0).getComparedArticleId());
    }

    /**
     * 测试获取算法名称
     * 验证返回正确的算法名称
     */
    @Test
    public void testGetName() {
        assertEquals("TFIDF", calculator.getName());
    }

    /**
     * 测试缓存清理
     * 验证缓存可以正常清理
     */
    @Test
    public void testClearCache() {
        Article article1 = createArticle(1L, "测试", "测试内容测试");
        Article article2 = createArticle(2L, "测试2", "测试内容测试");
        
        calculator.calculateSimilarity(article1, article2);
        
        int cacheSizeBefore = calculator.getCacheSize();
        assertTrue(cacheSizeBefore >= 0);
        
        calculator.clearCache();
        
        int cacheSizeAfter = calculator.getCacheSize();
        assertEquals(0, cacheSizeAfter);
    }

    /**
     * 测试标题和内容的组合
     * 验证标题和内容都参与相似度计算
     */
    @Test
    public void testTitleAndContentCombination() {
        Article article1 = createArticle(1L, "Java编程入门", 
                "本文介绍Java编程的基础知识。");
        Article article2 = createArticle(2L, "Java编程入门", 
                "本文介绍Java编程的基础知识。");

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertEquals(1.0, similarity, 0.001);
    }

    /**
     * 测试长文本相似度计算
     * 验证长文本的相似度计算正确
     */
    @Test
    public void testLongTextSimilarity() {
        StringBuilder longContent1 = new StringBuilder();
        StringBuilder longContent2 = new StringBuilder();
        
        for (int i = 0; i < 100; i++) {
            longContent1.append("Java是一种面向对象的编程语言，具有跨平台特性。");
            longContent2.append("Java是一种面向对象的编程语言，具有跨平台特性。");
        }

        Article article1 = createArticle(1L, "长文章1", longContent1.toString());
        Article article2 = createArticle(2L, "长文章2", longContent2.toString());

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertEquals(1.0, similarity, 0.001);
    }

    /**
     * 测试相似度结果详情
     * 验证批量计算结果包含正确的详情信息
     */
    @Test
    public void testSimilarityResultDetails() {
        Article article = createArticle(1L, "Java编程", "Java是一种编程语言。");
        Article article2 = createArticle(2L, "Python编程", "Python是一种编程语言。");

        List<SimilarityResult> results = calculator.calculateSimilarities(
                article, Arrays.asList(article2));

        assertNotNull(results);
        assertEquals(1, results.size());
        
        SimilarityResult result = results.get(0);
        assertNotNull(result.getDetails());
        assertNotNull(result.getDetails().get("similarityLevel"));
        assertNotNull(result.getDetails().get("comparedTitle"));
    }
}
