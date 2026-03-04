package com.example.demo.duplicate.algorithm.impl;

import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.SimilarityResult;
import com.example.demo.tag.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TagBasedSimilarityCalculator测试类
 * 测试基于标签的相似度计算器的各种功能
 */
public class TagBasedSimilarityCalculatorTest {

    private TagBasedSimilarityCalculator calculator;

    @BeforeEach
    public void setUp() {
        calculator = new TagBasedSimilarityCalculator();
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
     * 测试Jaccard相似度计算 - 完全相同的标签
     * 验证相同标签集合的相似度为1.0
     */
    @Test
    public void testJaccardSimilarityWithSameTags() {
        List<Tag> tags = Arrays.asList(
                createTag("Java", 1.0),
                createTag("编程", 0.8),
                createTag("开发", 0.6)
        );

        Article article1 = createArticle(1L, "文章1", "内容1", tags);
        Article article2 = createArticle(2L, "文章2", "内容2", tags);

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertEquals(1.0, similarity, 0.001);
    }

    /**
     * 测试Jaccard相似度计算 - 部分相同标签
     * 验证部分相同标签的相似度计算正确
     */
    @Test
    public void testJaccardSimilarityWithPartialTags() {
        List<Tag> tags1 = Arrays.asList(
                createTag("Java", 1.0),
                createTag("编程", 0.8),
                createTag("开发", 0.6)
        );

        List<Tag> tags2 = Arrays.asList(
                createTag("Java", 1.0),
                createTag("编程", 0.8),
                createTag("测试", 0.5)
        );

        Article article1 = createArticle(1L, "文章1", "内容1", tags1);
        Article article2 = createArticle(2L, "文章2", "内容2", tags2);

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertTrue(similarity > 0.0 && similarity < 1.0);
        assertTrue(similarity > 0.5);
    }

    /**
     * 测试Jaccard相似度计算 - 完全不同的标签
     * 验证完全不同标签的相似度为0.0
     */
    @Test
    public void testJaccardSimilarityWithDifferentTags() {
        List<Tag> tags1 = Arrays.asList(
                createTag("Java", 1.0),
                createTag("编程", 0.8)
        );

        List<Tag> tags2 = Arrays.asList(
                createTag("Python", 1.0),
                createTag("机器学习", 0.8)
        );

        Article article1 = createArticle(1L, "文章1", "内容1", tags1);
        Article article2 = createArticle(2L, "文章2", "内容2", tags2);

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertEquals(0.0, similarity, 0.001);
    }

    /**
     * 测试标签权重加权计算 - 高权重共享标签
     * 验证高权重共享标签对相似度的贡献更大
     */
    @Test
    public void testWeightedSimilarityWithHighWeightSharedTags() {
        List<Tag> tags1 = Arrays.asList(
                createTag("Java", 1.0),
                createTag("编程", 0.8),
                createTag("开发", 0.3)
        );

        List<Tag> tags2 = Arrays.asList(
                createTag("Java", 1.0),
                createTag("编程", 0.8),
                createTag("测试", 0.3)
        );

        Article article1 = createArticle(1L, "文章1", "内容1", tags1);
        Article article2 = createArticle(2L, "文章2", "内容2", tags2);

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertTrue(similarity > 0.6);
    }

    /**
     * 测试标签权重加权计算 - 低权重共享标签
     * 验证低权重共享标签对相似度的贡献较小
     */
    @Test
    public void testWeightedSimilarityWithLowWeightSharedTags() {
        List<Tag> tags1 = Arrays.asList(
                createTag("Java", 0.3),
                createTag("编程", 0.2),
                createTag("开发", 1.0)
        );

        List<Tag> tags2 = Arrays.asList(
                createTag("Java", 0.3),
                createTag("编程", 0.2),
                createTag("测试", 1.0)
        );

        Article article1 = createArticle(1L, "文章1", "内容1", tags1);
        Article article2 = createArticle(2L, "文章2", "内容2", tags2);

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertTrue(similarity > 0.0 && similarity < 0.5);
    }

    /**
     * 测试共享标签识别 - 有共享标签
     * 验证能正确识别共享标签
     */
    @Test
    public void testGetSharedTagsWithCommonTags() {
        List<Tag> tags1 = Arrays.asList(
                createTag("Java", 1.0),
                createTag("编程", 0.8),
                createTag("开发", 0.6)
        );

        List<Tag> tags2 = Arrays.asList(
                createTag("Java", 1.0),
                createTag("编程", 0.8),
                createTag("测试", 0.5)
        );

        Article article1 = createArticle(1L, "文章1", "内容1", tags1);
        Article article2 = createArticle(2L, "文章2", "内容2", tags2);

        Set<Tag> sharedTags = calculator.getSharedTags(article1, article2);

        assertNotNull(sharedTags);
        assertEquals(2, sharedTags.size());
    }

    /**
     * 测试共享标签识别 - 无共享标签
     * 验证无共享标签时返回空集合
     */
    @Test
    public void testGetSharedTagsWithNoCommonTags() {
        List<Tag> tags1 = Arrays.asList(
                createTag("Java", 1.0),
                createTag("编程", 0.8)
        );

        List<Tag> tags2 = Arrays.asList(
                createTag("Python", 1.0),
                createTag("机器学习", 0.8)
        );

        Article article1 = createArticle(1L, "文章1", "内容1", tags1);
        Article article2 = createArticle(2L, "文章2", "内容2", tags2);

        Set<Tag> sharedTags = calculator.getSharedTags(article1, article2);

        assertNotNull(sharedTags);
        assertTrue(sharedTags.isEmpty());
    }

    /**
     * 测试自适应阈值计算 - 短文章
     * 验证短文章（<500字）使用较低阈值
     */
    @Test
    public void testAdaptiveThresholdForShortArticle() {
        String shortContent = "这是一篇短文章，内容很少。";
        List<Tag> tags = Arrays.asList(createTag("Java", 1.0));

        Article article1 = createArticle(1L, "短文章", shortContent, tags);
        Article article2 = createArticle(2L, "短文章2", shortContent, tags);

        List<SimilarityResult> results = calculator.calculateSimilarities(article1, Arrays.asList(article2));

        assertNotNull(results);
        assertFalse(results.isEmpty());
        
        SimilarityResult result = results.get(0);
        assertNotNull(result.getDetails());
        Integer adaptiveThreshold = (Integer) result.getDetails().get("adaptiveThreshold");
        assertEquals(2, adaptiveThreshold.intValue());
    }

    /**
     * 测试自适应阈值计算 - 中等长度文章
     * 验证中等长度文章（500-2000字）使用中等阈值
     */
    @Test
    public void testAdaptiveThresholdForMediumArticle() {
        StringBuilder mediumContent = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            mediumContent.append("这是中等长度文章的内容，用于测试中等文章的自适应阈值。");
        }
        
        List<Tag> tags = Arrays.asList(createTag("Java", 1.0));

        Article article1 = createArticle(1L, "中等文章", mediumContent.toString(), tags);
        Article article2 = createArticle(2L, "中等文章2", mediumContent.toString(), tags);

        List<SimilarityResult> results = calculator.calculateSimilarities(article1, Arrays.asList(article2));

        assertNotNull(results);
        assertFalse(results.isEmpty());
        
        SimilarityResult result = results.get(0);
        assertNotNull(result.getDetails());
        Integer adaptiveThreshold = (Integer) result.getDetails().get("adaptiveThreshold");
        assertEquals(3, adaptiveThreshold.intValue());
    }

    /**
     * 测试自适应阈值计算 - 长文章
     * 验证长文章（>2000字）使用较高阈值
     */
    @Test
    public void testAdaptiveThresholdForLongArticle() {
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            longContent.append("这是一篇长文章的内容，用于测试长文章的自适应阈值计算功能。");
        }
        
        List<Tag> tags = Arrays.asList(createTag("Java", 1.0));

        Article article1 = createArticle(1L, "长文章", longContent.toString(), tags);
        Article article2 = createArticle(2L, "长文章2", longContent.toString(), tags);

        List<SimilarityResult> results = calculator.calculateSimilarities(article1, Arrays.asList(article2));

        assertNotNull(results);
        assertFalse(results.isEmpty());
        
        SimilarityResult result = results.get(0);
        assertNotNull(result.getDetails());
        Integer adaptiveThreshold = (Integer) result.getDetails().get("adaptiveThreshold");
        assertEquals(5, adaptiveThreshold.intValue());
    }

    /**
     * 测试边界情况 - 空标签列表
     * 验证空标签列表返回相似度0.0
     */
    @Test
    public void testEmptyTags() {
        Article article1 = createArticle(1L, "文章1", "内容1", new ArrayList<>());
        Article article2 = createArticle(2L, "文章2", "内容2", new ArrayList<>());

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertEquals(0.0, similarity, 0.001);
    }

    /**
     * 测试边界情况 - null标签列表
     * 验证null标签列表返回相似度0.0
     */
    @Test
    public void testNullTags() {
        Article article1 = createArticle(1L, "文章1", "内容1", null);
        article1.setTags(null);
        Article article2 = createArticle(2L, "文章2", "内容2", null);
        article2.setTags(null);

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertEquals(0.0, similarity, 0.001);
    }

    /**
     * 测试边界情况 - null文章对象
     * 验证null文章对象返回相似度0.0
     */
    @Test
    public void testNullArticle() {
        Article article = createArticle(1L, "文章", "内容", Arrays.asList(createTag("Java", 1.0)));

        double similarity1 = calculator.calculateSimilarity(null, article);
        double similarity2 = calculator.calculateSimilarity(article, null);
        double similarity3 = calculator.calculateSimilarity(null, null);

        assertEquals(0.0, similarity1, 0.001);
        assertEquals(0.0, similarity2, 0.001);
        assertEquals(0.0, similarity3, 0.001);
    }

    /**
     * 测试边界情况 - 一方空标签
     * 验证一方空标签返回相似度0.0
     */
    @Test
    public void testOneEmptyTags() {
        List<Tag> tags = Arrays.asList(createTag("Java", 1.0));
        
        Article article1 = createArticle(1L, "文章1", "内容1", tags);
        Article article2 = createArticle(2L, "文章2", "内容2", new ArrayList<>());

        double similarity = calculator.calculateSimilarity(article1, article2);

        assertEquals(0.0, similarity, 0.001);
    }

    /**
     * 测试批量相似度计算
     * 验证批量计算返回正确的结果列表
     */
    @Test
    public void testCalculateSimilarities() {
        List<Tag> tags1 = Arrays.asList(createTag("Java", 1.0), createTag("编程", 0.8));
        List<Tag> tags2 = Arrays.asList(createTag("Java", 1.0), createTag("开发", 0.7));
        List<Tag> tags3 = Arrays.asList(createTag("Python", 1.0), createTag("机器学习", 0.9));

        Article article = createArticle(1L, "目标文章", "内容", tags1);
        Article article2 = createArticle(2L, "文章2", "内容2", tags2);
        Article article3 = createArticle(3L, "文章3", "内容3", tags3);

        List<SimilarityResult> results = calculator.calculateSimilarities(article, Arrays.asList(article2, article3));

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.get(0).getSimilarity() >= results.get(1).getSimilarity());
    }

    /**
     * 测试获取算法名称
     * 验证返回正确的算法名称
     */
    @Test
    public void testGetName() {
        assertEquals("TagBasedSimilarity", calculator.getName());
    }
}
