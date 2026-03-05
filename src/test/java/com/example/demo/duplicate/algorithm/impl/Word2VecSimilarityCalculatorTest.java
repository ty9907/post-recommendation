package com.example.demo.duplicate.algorithm.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.algorithm.SimilarityCalculatorFactory;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.SimilarityResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Word2VecSimilarityCalculator 测试类
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-06
 */
@DisplayName("Word2Vec相似度计算器测试")
class Word2VecSimilarityCalculatorTest {

    private Word2VecSimilarityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Word2VecSimilarityCalculator();
    }

    @Test
    @DisplayName("测试计算器名称")
    void testGetName() {
        assertEquals("Word2Vec", calculator.getName(), "计算器名称应为 Word2Vec");
    }

    @Test
    @DisplayName("测试相同文章相似度")
    void testCalculateSimilarity_SameArticle() {
        Article article = new Article(1L, "Java编程", "Java是一种面向对象的编程语言，广泛应用于企业级开发。");
        
        double similarity = calculator.calculateSimilarity(article, article);
        
        assertEquals(1.0, similarity, 0.001, "相同文章相似度应为1.0");
    }

    @Test
    @DisplayName("测试相似文章相似度")
    void testCalculateSimilarity_SimilarArticles() {
        Article article1 = new Article(1L, "Java编程", "Java是一种面向对象的编程语言，广泛应用于企业级开发。");
        Article article2 = new Article(2L, "Java开发", "Java是一种面向对象的编程语言，在企业级应用中广泛使用。");
        
        double similarity = calculator.calculateSimilarity(article1, article2);
        
        assertTrue(similarity > 0.5, "相似文章的相似度应大于0.5，实际值: " + similarity);
        assertTrue(similarity <= 1.0, "相似度应小于等于1.0，实际值: " + similarity);
    }

    @Test
    @DisplayName("测试不同文章相似度")
    void testCalculateSimilarity_DifferentArticles() {
        Article article1 = new Article(1L, "Java编程", "Java是一种面向对象的编程语言。");
        Article article2 = new Article(2L, "烹饪技巧", "今天我们来学习如何制作美味的红烧肉。");
        
        double similarity = calculator.calculateSimilarity(article1, article2);
        
        assertTrue(similarity < 0.5, "不同主题文章的相似度应较低，实际值: " + similarity);
        assertTrue(similarity >= 0.0, "相似度应大于等于0.0，实际值: " + similarity);
    }

    @Test
    @DisplayName("测试空文章相似度")
    void testCalculateSimilarity_EmptyArticles() {
        Article article1 = new Article(1L, "", "");
        Article article2 = new Article(2L, "Java编程", "Java是一种编程语言。");
        
        double similarity = calculator.calculateSimilarity(article1, article2);
        
        assertEquals(0.0, similarity, 0.001, "空文章相似度应为0");
    }

    @Test
    @DisplayName("测试空文章相似度 - 两个都为空")
    void testCalculateSimilarity_BothEmpty() {
        Article article1 = new Article(1L, "", "");
        Article article2 = new Article(2L, "", "");
        
        double similarity = calculator.calculateSimilarity(article1, article2);
        
        assertEquals(0.0, similarity, 0.001, "两个空文章相似度应为0");
    }

    @Test
    @DisplayName("测试null文章相似度")
    void testCalculateSimilarity_NullArticles() {
        Article article = new Article(1L, "测试", "测试内容");
        
        assertEquals(0.0, calculator.calculateSimilarity(null, article), 0.001, "null文章相似度应为0");
        assertEquals(0.0, calculator.calculateSimilarity(article, null), 0.001, "null文章相似度应为0");
        assertEquals(0.0, calculator.calculateSimilarity(null, null), 0.001, "两个null文章相似度应为0");
    }

    @Test
    @DisplayName("测试批量计算相似度")
    void testCalculateSimilarities() {
        Article article1 = new Article(1L, "Java编程", "Java是一种面向对象的编程语言。");
        Article article2 = new Article(2L, "Python编程", "Python是一种简洁的编程语言。");
        Article article3 = new Article(3L, "烹饪技巧", "今天学习制作红烧肉。");
        
        List<Article> articles = Arrays.asList(article2, article3);
        
        List<SimilarityResult> results = calculator.calculateSimilarities(article1, articles);
        
        assertNotNull(results, "结果列表不应为空");
        assertEquals(2, results.size(), "应返回2个结果");
        
        for (SimilarityResult result : results) {
            assertEquals(1L, result.getArticleId(), "文章ID应为1");
            assertTrue(result.getSimilarity() >= 0.0 && result.getSimilarity() <= 1.0, 
                    "相似度应在0-1范围内");
            assertEquals("Word2Vec", result.getAlgorithm(), "算法名称应为 Word2Vec");
        }
    }

    @Test
    @DisplayName("测试批量计算 - 空列表")
    void testCalculateSimilarities_EmptyList() {
        Article article = new Article(1L, "测试", "测试内容");
        
        List<SimilarityResult> results = calculator.calculateSimilarities(article, Arrays.asList());
        
        assertTrue(results.isEmpty(), "空列表应返回空结果");
    }

    @Test
    @DisplayName("测试批量计算 - null列表")
    void testCalculateSimilarities_NullList() {
        Article article = new Article(1L, "测试", "测试内容");
        
        List<SimilarityResult> results = calculator.calculateSimilarities(article, null);
        
        assertTrue(results.isEmpty(), "null列表应返回空结果");
    }

    @Test
    @DisplayName("测试模型加载状态")
    void testIsModelLoaded() {
        assertFalse(calculator.isModelLoaded(), "无预训练模型时应返回false");
    }

    @Test
    @DisplayName("测试词汇表大小")
    void testGetVocabSize() {
        assertEquals(0, calculator.getVocabSize(), "无模型时词汇表大小应为0");
    }

    @Test
    @DisplayName("测试向量维度")
    void testGetVectorSize() {
        assertEquals(0, calculator.getVectorSize(), "无模型时向量维度应为0");
    }

    @Test
    @DisplayName("测试查找相似词 - 无模型")
    void testFindSimilarWords_NoModel() {
        List<String> similarWords = calculator.findSimilarWords("Java", 5);
        
        assertTrue(similarWords.isEmpty(), "无模型时应返回空列表");
    }

    @Test
    @DisplayName("测试工厂获取Word2Vec计算器")
    void testFactoryGetWord2Vec() {
        SimilarityCalculator calc = SimilarityCalculatorFactory.getCalculator("WORD2VEC");
        
        assertNotNull(calc, "计算器不应为空");
        assertEquals("Word2Vec", calc.getName(), "计算器名称应为 Word2Vec");
    }

    @Test
    @DisplayName("测试工厂支持Word2Vec")
    void testFactoryIsSupported() {
        assertTrue(SimilarityCalculatorFactory.isSupported("WORD2VEC"), "应支持 WORD2VEC");
        assertTrue(SimilarityCalculatorFactory.isSupported("word2vec"), "应支持小写 word2vec");
    }

    @Test
    @DisplayName("测试工厂算法描述")
    void testFactoryAlgorithmDescription() {
        String description = SimilarityCalculatorFactory.getAlgorithmDescription("WORD2VEC");
        
        assertNotNull(description, "描述不应为空");
        assertTrue(description.contains("词向量"), "描述应包含'词向量'");
    }

    @Test
    @DisplayName("测试工厂支持的算法列表")
    void testFactorySupportedAlgorithms() {
        String[] algorithms = SimilarityCalculatorFactory.getSupportedAlgorithms();
        
        assertTrue(Arrays.asList(algorithms).contains("WORD2VEC"), "应包含 WORD2VEC");
    }

    @Test
    @DisplayName("测试语义相似度")
    void testSemanticSimilarity() {
        Article article1 = new Article(1L, "编程学习", "学习编程需要掌握基础知识和实践练习。");
        Article article2 = new Article(2L, "代码训练", "编写代码需要理解基本概念并进行实际操作。");
        
        double similarity = calculator.calculateSimilarity(article1, article2);
        
        assertTrue(similarity >= 0.0 && similarity <= 1.0, 
                "语义相似度应在0-1范围内，实际值: " + similarity);
    }

    @Test
    @DisplayName("测试长文本相似度")
    void testLongTextSimilarity() {
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        
        for (int i = 0; i < 100; i++) {
            sb1.append("Java是一种面向对象的编程语言，具有跨平台特性。");
            sb2.append("Java是一种面向对象的编程语言，具有平台无关性。");
        }
        
        Article article1 = new Article(1L, "Java介绍", sb1.toString());
        Article article2 = new Article(2L, "Java说明", sb2.toString());
        
        double similarity = calculator.calculateSimilarity(article1, article2);
        
        assertTrue(similarity > 0.6, "长文本相似文章的相似度应较高，实际值: " + similarity);
    }

    @Test
    @DisplayName("测试性能 - 单次计算")
    void testPerformance_SingleCalculation() {
        Article article1 = new Article(1L, "测试文章1", "这是一篇测试文章的内容，用于验证性能。");
        Article article2 = new Article(2L, "测试文章2", "这是另一篇测试文章的内容，用于验证性能。");
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 10; i++) {
            calculator.calculateSimilarity(article1, article2);
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        long avgTime = elapsed / 10;
        
        assertTrue(avgTime < 100, "单次计算平均时间应小于100ms，实际值: " + avgTime + "ms");
    }
}
