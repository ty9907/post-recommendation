package com.example.demo.duplicate.algorithm.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.SimilarityResult;
import com.example.demo.tag.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HybridSimilarityCalculator测试类
 * 测试混合相似度计算器的各种功能
 */
public class HybridSimilarityCalculatorTest {

    private HybridSimilarityCalculator calculator;

    @BeforeEach
    public void setUp() {
        calculator = new HybridSimilarityCalculator();
    }

    /**
     * 创建测试用的文章对象
     * @param id 文章ID
     * @param title 文章标题
     * @param content 文章内容
     * @param tags 标签列表
     * @return 文章对象
     */
    private Article createArticle(Long id, String title, String content, List<Tag> tags) {
        Article article = new Article(id, title, content);
        article.setTags(tags);
        return article;
    }

    /**
     * 创建测试用的标签
     * @param name 标签名称
     * @param weight 标签权重
     * @return 标签对象
     */
    private Tag createTag(String name, double weight) {
        return new Tag(name, weight, 1);
    }

    /**
     * 创建测试用的文章对象（无标签）
     * @param id 文章ID
     * @param title 文章标题
     * @param content 文章内容
     * @return 文章对象
     */
    private Article createArticleWithoutTags(Long id, String title, String content) {
        return new Article(id, title, content);
    }

    /**
     * 测试权重融合机制 - 默认权重
     * 验证默认权重配置正确（标签0.4，文本0.6）
     */
    @Test
    public void testDefaultWeights() {
        assertEquals(0.4, calculator.getTagWeight(), 0.001);
        assertEquals(0.6, calculator.getTextWeight(), 0.001);
    }

    /**
     * 测试权重融合机制 - 权重融合计算
     * 验证混合相似度按权重正确融合
     */
    @Test
    public void testWeightFusionCalculation() {
        List<Tag> tags1 = Arrays.asList(createTag("Java", 1.0), createTag("编程", 0.8));
        List<Tag> tags2 = Arrays.asList(createTag("Java", 1.0), createTag("编程", 0.8));

        Article article1 = createArticle(1L, "Java编程", 
                "Java是一种面向对象的编程语言，广泛应用于企业级开发。", tags1);
        Article article2 = createArticle(2L, "Java开发", 
                "Java是一种流行的编程语言，在企业级开发中应用广泛。", tags2);

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertTrue(similarity > 0.5);
        assertTrue(similarity <= 1.0);
    }

    /**
     * 测试权重融合机制 - 标签主导
     * 验证标签权重较高时，标签相似度主导结果
     */
    @Test
    public void testTagDominantFusion() {
        calculator.setWeights(0.8, 0.2);

        List<Tag> sameTags = Arrays.asList(createTag("Java", 1.0), createTag("编程", 0.8));
        
        Article article1 = createArticle(1L, "Java编程", 
                "Java是一种面向对象的编程语言。", sameTags);
        Article article2 = createArticle(2L, "Java开发", 
                "Python是一种解释型编程语言。", sameTags);

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertTrue(similarity > 0.6);

        calculator.setWeights(0.4, 0.6);
    }

    /**
     * 测试权重融合机制 - 文本主导
     * 验证文本权重较高时，文本相似度主导结果
     */
    @Test
    public void testTextDominantFusion() {
        calculator.setWeights(0.2, 0.8);

        List<Tag> differentTags1 = Arrays.asList(createTag("Java", 1.0));
        List<Tag> differentTags2 = Arrays.asList(createTag("Python", 1.0));
        
        String sameContent = "这是一种面向对象的编程语言，广泛应用于企业级开发。";
        
        Article article1 = createArticle(1L, "编程语言1", sameContent, differentTags1);
        Article article2 = createArticle(2L, "编程语言2", sameContent, differentTags2);

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertTrue(similarity > 0.6);

        calculator.setWeights(0.4, 0.6);
    }

    /**
     * 测试多维度评分输出 - 包含标签相似度
     * 验证结果详情包含标签相似度信息
     */
    @Test
    public void testMultiDimensionOutputWithTagSimilarity() {
        List<Tag> tags = Arrays.asList(createTag("Java", 1.0));
        
        Article article1 = createArticle(1L, "Java编程", "Java是一种编程语言。", tags);
        Article article2 = createArticle(2L, "Java开发", "Java是一种开发语言。", tags);

        List<SimilarityResult> results = calculator.calculateSimilarities(article1, Arrays.asList(article2));

        assertNotNull(results);
        assertEquals(1, results.size());
        
        SimilarityResult result = results.get(0);
        Map<String, Object> details = result.getDetails();
        
        assertNotNull(details);
        assertTrue(details.containsKey("tagSimilarity"));
        assertTrue(details.get("tagSimilarity") instanceof Double);
    }

    /**
     * 测试多维度评分输出 - 包含文本相似度
     * 验证结果详情包含文本相似度信息
     */
    @Test
    public void testMultiDimensionOutputWithTextSimilarity() {
        List<Tag> tags = Arrays.asList(createTag("Java", 1.0));
        
        Article article1 = createArticle(1L, "Java编程", "Java是一种编程语言。", tags);
        Article article2 = createArticle(2L, "Java开发", "Java是一种开发语言。", tags);

        List<SimilarityResult> results = calculator.calculateSimilarities(article1, Arrays.asList(article2));

        SimilarityResult result = results.get(0);
        Map<String, Object> details = result.getDetails();
        
        assertTrue(details.containsKey("textSimilarity"));
        assertTrue(details.get("textSimilarity") instanceof Double);
    }

    /**
     * 测试多维度评分输出 - 包含混合相似度
     * 验证结果详情包含混合相似度信息
     */
    @Test
    public void testMultiDimensionOutputWithHybridSimilarity() {
        List<Tag> tags = Arrays.asList(createTag("Java", 1.0));
        
        Article article1 = createArticle(1L, "Java编程", "Java是一种编程语言。", tags);
        Article article2 = createArticle(2L, "Java开发", "Java是一种开发语言。", tags);

        List<SimilarityResult> results = calculator.calculateSimilarities(article1, Arrays.asList(article2));

        SimilarityResult result = results.get(0);
        Map<String, Object> details = result.getDetails();
        
        assertTrue(details.containsKey("hybridSimilarity"));
        assertTrue(details.get("hybridSimilarity") instanceof Double);
    }

    /**
     * 测试多维度评分输出 - 包含权重信息
     * 验证结果详情包含权重配置信息
     */
    @Test
    public void testMultiDimensionOutputWithWeights() {
        List<Tag> tags = Arrays.asList(createTag("Java", 1.0));
        
        Article article1 = createArticle(1L, "Java编程", "Java是一种编程语言。", tags);
        Article article2 = createArticle(2L, "Java开发", "Java是一种开发语言。", tags);

        List<SimilarityResult> results = calculator.calculateSimilarities(article1, Arrays.asList(article2));

        SimilarityResult result = results.get(0);
        Map<String, Object> details = result.getDetails();
        
        assertTrue(details.containsKey("tagWeight"));
        assertTrue(details.containsKey("textWeight"));
        assertEquals(0.4, (Double) details.get("tagWeight"), 0.001);
        assertEquals(0.6, (Double) details.get("textWeight"), 0.001);
    }

    /**
     * 测试多维度评分输出 - 包含算法名称
     * 验证结果详情包含使用的算法名称
     */
    @Test
    public void testMultiDimensionOutputWithAlgorithmNames() {
        List<Tag> tags = Arrays.asList(createTag("Java", 1.0));
        
        Article article1 = createArticle(1L, "Java编程", "Java是一种编程语言。", tags);
        Article article2 = createArticle(2L, "Java开发", "Java是一种开发语言。", tags);

        List<SimilarityResult> results = calculator.calculateSimilarities(article1, Arrays.asList(article2));

        SimilarityResult result = results.get(0);
        Map<String, Object> details = result.getDetails();
        
        assertTrue(details.containsKey("tagAlgorithm"));
        assertTrue(details.containsKey("textAlgorithm"));
        assertEquals("TagBasedSimilarity", details.get("tagAlgorithm"));
        assertEquals("TFIDF", details.get("textAlgorithm"));
    }

    /**
     * 测试多维度评分输出 - 包含相似度等级
     * 验证结果详情包含相似度等级描述
     */
    @Test
    public void testMultiDimensionOutputWithSimilarityLevel() {
        List<Tag> tags = Arrays.asList(createTag("Java", 1.0));
        
        Article article1 = createArticle(1L, "Java编程", "Java是一种编程语言。", tags);
        Article article2 = createArticle(2L, "Java开发", "Java是一种开发语言。", tags);

        List<SimilarityResult> results = calculator.calculateSimilarities(article1, Arrays.asList(article2));

        SimilarityResult result = results.get(0);
        Map<String, Object> details = result.getDetails();
        
        assertTrue(details.containsKey("similarityLevel"));
        assertNotNull(details.get("similarityLevel"));
    }

    /**
     * 测试权重设置 - 有效权重
     * 验证有效权重可以正确设置
     */
    @Test
    public void testSetValidWeights() {
        calculator.setWeights(0.3, 0.7);
        
        assertEquals(0.3, calculator.getTagWeight(), 0.001);
        assertEquals(0.7, calculator.getTextWeight(), 0.001);
        
        calculator.setWeights(0.5, 0.5);
        
        assertEquals(0.5, calculator.getTagWeight(), 0.001);
        assertEquals(0.5, calculator.getTextWeight(), 0.001);
        
        calculator.setWeights(0.4, 0.6);
    }

    /**
     * 测试权重设置 - 权重总和不为1
     * 验证权重总和不为1时抛出异常
     */
    @Test
    public void testSetWeightsWithInvalidSum() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.setWeights(0.3, 0.8);
        });
    }

    /**
     * 测试权重设置 - 标签权重超出范围
     * 验证标签权重超出范围时抛出异常
     */
    @Test
    public void testSetWeightsWithInvalidTagWeight() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.setWeights(1.5, -0.5);
        });
    }

    /**
     * 测试权重设置 - 文本权重超出范围
     * 验证文本权重超出范围时抛出异常
     */
    @Test
    public void testSetWeightsWithInvalidTextWeight() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.setWeights(0.5, 1.5);
        });
    }

    /**
     * 测试权重设置 - 负权重
     * 验证负权重抛出异常
     */
    @Test
    public void testSetNegativeWeights() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.setWeights(-0.2, 1.2);
        });
    }

    /**
     * 测试权重设置 - 边界值
     * 验证边界值权重可以正确设置
     */
    @Test
    public void testSetBoundaryWeights() {
        calculator.setWeights(0.0, 1.0);
        assertEquals(0.0, calculator.getTagWeight(), 0.001);
        assertEquals(1.0, calculator.getTextWeight(), 0.001);
        
        calculator.setWeights(1.0, 0.0);
        assertEquals(1.0, calculator.getTagWeight(), 0.001);
        assertEquals(0.0, calculator.getTextWeight(), 0.001);
        
        calculator.setWeights(0.4, 0.6);
    }

    /**
     * 测试自定义计算器构造
     * 验证自定义计算器可以正确初始化
     */
    @Test
    public void testCustomCalculatorConstructor() {
        SimilarityCalculator mockTagCalculator = new TagBasedSimilarityCalculator();
        SimilarityCalculator mockTextCalculator = new TFIDFSimilarityCalculator();
        
        HybridSimilarityCalculator customCalculator = 
                new HybridSimilarityCalculator(mockTagCalculator, mockTextCalculator);
        
        assertNotNull(customCalculator);
        assertEquals(mockTagCalculator, customCalculator.getTagCalculator());
        assertEquals(mockTextCalculator, customCalculator.getTextCalculator());
    }

    /**
     * 测试自定义计算器构造 - null计算器
     * 验证null计算器抛出异常
     */
    @Test
    public void testCustomCalculatorConstructorWithNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HybridSimilarityCalculator(null, new TFIDFSimilarityCalculator());
        });
    }

    /**
     * 测试自定义计算器构造 - 全null计算器
     * 验证全null计算器抛出异常
     */
    @Test
    public void testCustomCalculatorConstructorWithAllNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HybridSimilarityCalculator(null, null);
        });
    }

    /**
     * 测试获取计算器
     * 验证可以正确获取标签和文本计算器
     */
    @Test
    public void testGetCalculators() {
        assertNotNull(calculator.getTagCalculator());
        assertNotNull(calculator.getTextCalculator());
        assertEquals("TagBasedSimilarity", calculator.getTagCalculator().getName());
        assertEquals("TFIDF", calculator.getTextCalculator().getName());
    }

    /**
     * 测试null文章
     * 验证null文章返回相似度0.0
     */
    @Test
    public void testNullArticle() {
        List<Tag> tags = Arrays.asList(createTag("Java", 1.0));
        Article article = createArticle(1L, "测试", "测试内容", tags);

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
        List<Tag> tags1 = Arrays.asList(createTag("Java", 1.0));
        List<Tag> tags2 = Arrays.asList(createTag("Java", 1.0));
        List<Tag> tags3 = Arrays.asList(createTag("Python", 1.0));

        Article article = createArticle(1L, "Java编程", "Java是一种编程语言。", tags1);
        Article article2 = createArticle(2L, "Java开发", "Java是一种开发语言。", tags2);
        Article article3 = createArticle(3L, "Python编程", "Python是一种编程语言。", tags3);

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
        List<Tag> tags = Arrays.asList(createTag("Java", 1.0));
        Article article = createArticle(1L, "测试", "测试内容", tags);

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
        List<Tag> tags = Arrays.asList(createTag("Java", 1.0));
        Article article = createArticle(1L, "测试", "测试内容", tags);

        List<SimilarityResult> results = calculator.calculateSimilarities(article, null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * 测试获取算法名称
     * 验证返回正确的算法名称
     */
    @Test
    public void testGetName() {
        assertEquals("HybridSimilarity", calculator.getName());
    }

    /**
     * 测试完全相同的文章
     * 验证完全相同的文章相似度为1.0
     */
    @Test
    public void testIdenticalArticles() {
        List<Tag> tags = Arrays.asList(createTag("Java", 1.0), createTag("编程", 0.8));
        String content = "Java是一种面向对象的编程语言。";
        
        Article article1 = createArticle(1L, "Java编程", content, tags);
        Article article2 = createArticle(2L, "Java编程", content, tags);

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertEquals(1.0, similarity, 0.001);
    }

    /**
     * 测试完全不同的文章
     * 验证完全不同的文章相似度较低
     */
    @Test
    public void testCompletelyDifferentArticles() {
        List<Tag> tags1 = Arrays.asList(createTag("Java", 1.0));
        List<Tag> tags2 = Arrays.asList(createTag("美食", 1.0));
        
        Article article1 = createArticle(1L, "Java编程", 
                "Java是一种面向对象的编程语言。", tags1);
        Article article2 = createArticle(2L, "美食烹饪", 
                "红烧肉是一道经典的中国菜肴。", tags2);

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertTrue(similarity < 0.3);
    }
}
