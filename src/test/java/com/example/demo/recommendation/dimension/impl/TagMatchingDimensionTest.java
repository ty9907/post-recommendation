package com.example.demo.recommendation.dimension.impl;

import com.example.demo.recommendation.dimension.DimensionContext;
import com.example.demo.recommendation.dimension.DimensionResult;
import com.example.demo.recommendation.model.PostTag;
import com.example.demo.recommendation.model.UserTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TagMatchingDimension测试类
 * 测试标签匹配维度计算器的各种功能
 */
public class TagMatchingDimensionTest {

    private TagMatchingDimension dimension;

    @BeforeEach
    public void setUp() {
        dimension = new TagMatchingDimension();
    }

    /**
     * 创建测试用的用户标签
     * @param name 标签名称
     * @param weight 标签权重
     * @return 用户标签对象
     */
    private UserTag createUserTag(String name, double weight) {
        return new UserTag(name, weight, "user");
    }

    /**
     * 创建测试用的帖子标签
     * @param name 标签名称
     * @param weight 标签权重
     * @param postId 帖子ID
     * @return 帖子标签对象
     */
    private PostTag createPostTag(String name, double weight, Long postId) {
        return new PostTag(name, weight, postId);
    }

    /**
     * 测试标签匹配 - 有匹配标签
     * 验证用户标签与帖子标签匹配时分数计算正确
     */
    @Test
    public void testCalculate_WithMatchingTags() {
        List<UserTag> userTags = Arrays.asList(
                createUserTag("Java", 1.0),
                createUserTag("编程", 0.8),
                createUserTag("开发", 0.6)
        );

        List<PostTag> postTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L),
                createPostTag("编程", 0.8, 1L),
                createPostTag("测试", 0.5, 1L)
        );

        DimensionContext context = DimensionContext.builder()
                .userTags(userTags)
                .candidatePostTags(postTags)
                .build();

        DimensionResult result = dimension.calculate(context);

        assertNotNull(result);
        assertEquals("TAG_MATCHING", result.getDimensionName());
        assertTrue(result.getScore() > 0.0);
        assertTrue(result.getScore() <= 1.0);
        assertNotNull(result.getDetails());
        assertTrue(result.getDetails().containsKey("jaccardSimilarity"));
        assertTrue(result.getDetails().containsKey("weightedScore"));
    }

    /**
     * 测试标签匹配 - 无匹配标签
     * 验证用户标签与帖子标签无匹配时分数为0
     */
    @Test
    public void testCalculate_WithNoMatchingTags() {
        List<UserTag> userTags = Arrays.asList(
                createUserTag("Java", 1.0),
                createUserTag("编程", 0.8)
        );

        List<PostTag> postTags = Arrays.asList(
                createPostTag("Python", 1.0, 1L),
                createPostTag("机器学习", 0.9, 1L)
        );

        DimensionContext context = DimensionContext.builder()
                .userTags(userTags)
                .candidatePostTags(postTags)
                .build();

        DimensionResult result = dimension.calculate(context);

        assertNotNull(result);
        assertEquals("TAG_MATCHING", result.getDimensionName());
        assertEquals(0.0, result.getScore(), 0.001);
    }

    /**
     * 测试标签匹配 - 空用户标签
     * 验证用户标签为空时返回分数0
     */
    @Test
    public void testCalculate_WithEmptyUserTags() {
        List<PostTag> postTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L),
                createPostTag("编程", 0.8, 1L)
        );

        DimensionContext context = DimensionContext.builder()
                .userTags(new ArrayList<>())
                .candidatePostTags(postTags)
                .build();

        DimensionResult result = dimension.calculate(context);

        assertNotNull(result);
        assertEquals("TAG_MATCHING", result.getDimensionName());
        assertEquals(0.0, result.getScore(), 0.001);
        assertEquals("用户标签或候选帖子标签为空", result.getDetails().get("reason"));
    }

    /**
     * 测试标签匹配 - 空帖子标签
     * 验证帖子标签为空时返回分数0
     */
    @Test
    public void testCalculate_WithEmptyPostTags() {
        List<UserTag> userTags = Arrays.asList(
                createUserTag("Java", 1.0),
                createUserTag("编程", 0.8)
        );

        DimensionContext context = DimensionContext.builder()
                .userTags(userTags)
                .candidatePostTags(new ArrayList<>())
                .build();

        DimensionResult result = dimension.calculate(context);

        assertNotNull(result);
        assertEquals("TAG_MATCHING", result.getDimensionName());
        assertEquals(0.0, result.getScore(), 0.001);
        assertEquals("用户标签或候选帖子标签为空", result.getDetails().get("reason"));
    }

    /**
     * 测试标签匹配 - 加权标签
     * 验证高权重匹配标签对分数的贡献更大
     */
    @Test
    public void testCalculate_WithWeightedTags() {
        List<UserTag> userTags = Arrays.asList(
                createUserTag("Java", 1.0),
                createUserTag("编程", 0.8),
                createUserTag("开发", 0.3)
        );

        List<PostTag> postTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L),
                createPostTag("编程", 0.8, 1L),
                createPostTag("测试", 0.3, 1L)
        );

        DimensionContext context = DimensionContext.builder()
                .userTags(userTags)
                .candidatePostTags(postTags)
                .build();

        DimensionResult result = dimension.calculate(context);

        assertNotNull(result);
        assertTrue(result.getScore() > 0.6);

        Double weightedScore = (Double) result.getDetails().get("weightedScore");
        assertNotNull(weightedScore);
        assertTrue(weightedScore > 0.6);
    }

    /**
     * 测试Jaccard相似度计算 - 完全相同
     */
    @Test
    public void testJaccardSimilarityWithSameTags() {
        Set<String> set1 = new HashSet<>(Arrays.asList("java", "编程", "开发"));
        Set<String> set2 = new HashSet<>(Arrays.asList("java", "编程", "开发"));

        double similarity = dimension.calculateJaccardSimilarity(set1, set2);

        assertEquals(1.0, similarity, 0.001);
    }

    /**
     * 测试Jaccard相似度计算 - 部分相同
     */
    @Test
    public void testJaccardSimilarityWithPartialTags() {
        Set<String> set1 = new HashSet<>(Arrays.asList("java", "编程", "开发"));
        Set<String> set2 = new HashSet<>(Arrays.asList("java", "编程", "测试"));

        double similarity = dimension.calculateJaccardSimilarity(set1, set2);

        assertTrue(similarity > 0.0 && similarity < 1.0);
        assertEquals(0.5, similarity, 0.001);
    }

    /**
     * 测试Jaccard相似度计算 - 空集合
     */
    @Test
    public void testJaccardSimilarityWithEmptySet() {
        Set<String> set1 = new HashSet<>(Arrays.asList("java", "编程"));
        Set<String> set2 = new HashSet<>();

        double similarity = dimension.calculateJaccardSimilarity(set1, set2);

        assertEquals(0.0, similarity, 0.001);
    }

    /**
     * 测试加权分数计算 - 高权重匹配
     */
    @Test
    public void testWeightedScoreWithHighWeightMatch() {
        List<UserTag> userTags = Arrays.asList(
                createUserTag("Java", 1.0),
                createUserTag("编程", 0.9)
        );

        List<PostTag> postTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L),
                createPostTag("编程", 0.9, 1L)
        );

        double score = dimension.calculateWeightedScore(userTags, postTags);

        assertTrue(score > 0.8);
    }

    /**
     * 测试加权分数计算 - 低权重匹配
     */
    @Test
    public void testWeightedScoreWithLowWeightMatch() {
        List<UserTag> userTags = Arrays.asList(
                createUserTag("Java", 0.3),
                createUserTag("编程", 0.2)
        );

        List<PostTag> postTags = Arrays.asList(
                createPostTag("Java", 0.3, 1L),
                createPostTag("编程", 0.2, 1L)
        );

        double score = dimension.calculateWeightedScore(userTags, postTags);

        assertTrue(score < 0.5);
    }

    /**
     * 测试标签名称提取 - 用户标签
     */
    @Test
    public void testExtractTagNamesFromUserTags() {
        List<UserTag> userTags = Arrays.asList(
                createUserTag("Java", 1.0),
                createUserTag("编程", 0.8),
                createUserTag("  开发  ", 0.6)
        );

        Set<String> tagNames = dimension.extractTagNames(userTags);

        assertNotNull(tagNames);
        assertEquals(3, tagNames.size());
        assertTrue(tagNames.contains("java"));
        assertTrue(tagNames.contains("编程"));
        assertTrue(tagNames.contains("开发"));
    }

    /**
     * 测试标签名称提取 - 帖子标签
     */
    @Test
    public void testExtractTagNamesFromPostTags() {
        List<PostTag> postTags = Arrays.asList(
                createPostTag("Python", 1.0, 1L),
                createPostTag("机器学习", 0.9, 1L)
        );

        Set<String> tagNames = dimension.extractTagNames(postTags);

        assertNotNull(tagNames);
        assertEquals(2, tagNames.size());
        assertTrue(tagNames.contains("python"));
        assertTrue(tagNames.contains("机器学习"));
    }

    /**
     * 测试获取维度名称
     */
    @Test
    public void testGetName() {
        assertEquals("TAG_MATCHING", dimension.getName());
    }

    /**
     * 测试null用户标签列表
     */
    @Test
    public void testCalculate_WithNullUserTags() {
        List<PostTag> postTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L)
        );

        DimensionContext context = DimensionContext.builder()
                .userTags(null)
                .candidatePostTags(postTags)
                .build();

        DimensionResult result = dimension.calculate(context);

        assertNotNull(result);
        assertEquals(0.0, result.getScore(), 0.001);
    }

    /**
     * 测试null帖子标签列表
     */
    @Test
    public void testCalculate_WithNullPostTags() {
        List<UserTag> userTags = Arrays.asList(
                createUserTag("Java", 1.0)
        );

        DimensionContext context = DimensionContext.builder()
                .userTags(userTags)
                .candidatePostTags(null)
                .build();

        DimensionResult result = dimension.calculate(context);

        assertNotNull(result);
        assertEquals(0.0, result.getScore(), 0.001);
    }
}
