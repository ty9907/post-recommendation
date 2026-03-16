package com.example.demo.recommendation.dimension.impl;

import com.example.demo.recommendation.dimension.DimensionContext;
import com.example.demo.recommendation.dimension.DimensionResult;
import com.example.demo.recommendation.model.BrowseHistory;
import com.example.demo.recommendation.model.PostTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BrowseHistoryDimension测试类
 * 测试浏览历史维度计算器的各种功能
 */
public class BrowseHistoryDimensionTest {

    private BrowseHistoryDimension dimension;

    @BeforeEach
    public void setUp() {
        dimension = new BrowseHistoryDimension();
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
     * 创建测试用的浏览历史
     * @param postId 帖子ID
     * @param browseTime 浏览时间戳
     * @param tags 帖子标签列表
     * @return 浏览历史对象
     */
    private BrowseHistory createBrowseHistory(Long postId, Long browseTime, List<PostTag> tags) {
        return new BrowseHistory(postId, browseTime, tags);
    }

    /**
     * 测试浏览历史 - 有浏览历史
     * 验证有浏览历史时分数计算正确
     */
    @Test
    public void testCalculate_WithBrowseHistory() {
        long currentTime = System.currentTimeMillis();
        long oneHourAgo = currentTime - (1 * 60 * 60 * 1000);

        List<PostTag> historyTags = Arrays.asList(
                createPostTag("Java", 1.0, 100L),
                createPostTag("编程", 0.8, 100L)
        );

        List<PostTag> candidateTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L),
                createPostTag("开发", 0.7, 1L)
        );

        BrowseHistory history = createBrowseHistory(100L, oneHourAgo, historyTags);
        List<BrowseHistory> browseHistoryList = Arrays.asList(history);

        DimensionContext context = DimensionContext.builder()
                .browseHistory(browseHistoryList)
                .candidatePostTags(candidateTags)
                .build();

        DimensionResult result = dimension.calculate(context);

        assertNotNull(result);
        assertEquals("BROWSE_HISTORY", result.getDimensionName());
        assertTrue(result.getScore() > 0.0);
        assertTrue(result.getScore() <= 1.0);
        assertNotNull(result.getDetails());
        assertEquals(1, result.getDetails().get("browseHistoryCount"));
    }

    /**
     * 测试浏览历史 - 无浏览历史
     * 验证无浏览历史时返回分数0
     */
    @Test
    public void testCalculate_WithEmptyBrowseHistory() {
        List<PostTag> candidateTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L),
                createPostTag("编程", 0.8, 1L)
        );

        DimensionContext context = DimensionContext.builder()
                .browseHistory(new ArrayList<>())
                .candidatePostTags(candidateTags)
                .build();

        DimensionResult result = dimension.calculate(context);

        assertNotNull(result);
        assertEquals("BROWSE_HISTORY", result.getDimensionName());
        assertEquals(0.0, result.getScore(), 0.001);
        assertEquals("无浏览历史记录", result.getDetails().get("reason"));
    }

    /**
     * 测试浏览历史 - 时间衰减
     * 验证较早的浏览历史对分数的贡献较小
     */
    @Test
    public void testCalculate_WithTimeDecay() {
        long currentTime = System.currentTimeMillis();
        long oneHourAgo = currentTime - (1 * 60 * 60 * 1000);
        long oneWeekAgo = currentTime - (7 * 24 * 60 * 60 * 1000);

        List<PostTag> historyTags = Arrays.asList(
                createPostTag("Java", 1.0, 100L)
        );

        List<PostTag> candidateTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L)
        );

        BrowseHistory recentHistory = createBrowseHistory(100L, oneHourAgo, historyTags);
        BrowseHistory oldHistory = createBrowseHistory(101L, oneWeekAgo, historyTags);

        double recentWeight = dimension.calculateTimeDecayWeight(oneHourAgo);
        double oldWeight = dimension.calculateTimeDecayWeight(oneWeekAgo);

        assertTrue(recentWeight > oldWeight, "较近的时间应该有更大的权重");
        assertTrue(recentWeight > 0.5, "1小时前的权重应该大于0.5");
        assertTrue(oldWeight < 0.5, "一周前的权重应该小于0.5");
    }

    /**
     * 测试浏览历史 - 多条浏览历史
     * 验证多条浏览历史时分数聚合正确
     */
    @Test
    public void testCalculate_WithMultipleBrowseHistory() {
        long currentTime = System.currentTimeMillis();
        long oneHourAgo = currentTime - (1 * 60 * 60 * 1000);
        long twoHoursAgo = currentTime - (2 * 60 * 60 * 1000);

        List<PostTag> historyTags1 = Arrays.asList(
                createPostTag("Java", 1.0, 100L),
                createPostTag("编程", 0.8, 100L)
        );

        List<PostTag> historyTags2 = Arrays.asList(
                createPostTag("Java", 0.9, 101L),
                createPostTag("开发", 0.7, 101L)
        );

        List<PostTag> candidateTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L),
                createPostTag("编程", 0.8, 1L)
        );

        BrowseHistory history1 = createBrowseHistory(100L, oneHourAgo, historyTags1);
        BrowseHistory history2 = createBrowseHistory(101L, twoHoursAgo, historyTags2);
        List<BrowseHistory> browseHistoryList = Arrays.asList(history1, history2);

        DimensionContext context = DimensionContext.builder()
                .browseHistory(browseHistoryList)
                .candidatePostTags(candidateTags)
                .build();

        DimensionResult result = dimension.calculate(context);

        assertNotNull(result);
        assertTrue(result.getScore() > 0.0);
        assertEquals(2, result.getDetails().get("browseHistoryCount"));
    }

    /**
     * 测试时间衰减权重计算 - 最近时间
     */
    @Test
    public void testTimeDecayWeightRecent() {
        long currentTime = System.currentTimeMillis();

        double weight = dimension.calculateTimeDecayWeight(currentTime);

        assertTrue(weight > 0.9);
        assertTrue(weight <= 1.0);
    }

    /**
     * 测试时间衰减权重计算 - 较早时间
     */
    @Test
    public void testTimeDecayWeightOld() {
        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

        double weight = dimension.calculateTimeDecayWeight(oneDayAgo);

        assertTrue(weight > 0.0);
        assertTrue(weight < 0.5);
    }

    /**
     * 测试时间衰减权重计算 - null时间
     */
    @Test
    public void testTimeDecayWeightNull() {
        double weight = dimension.calculateTimeDecayWeight(null);

        assertEquals(0.0, weight, 0.001);
    }

    /**
     * 测试帖子相似度计算 - 完全相同
     */
    @Test
    public void testPostSimilarityWithSameTags() {
        List<PostTag> tags1 = Arrays.asList(
                createPostTag("Java", 1.0, 1L),
                createPostTag("编程", 0.8, 1L)
        );

        List<PostTag> tags2 = Arrays.asList(
                createPostTag("Java", 1.0, 2L),
                createPostTag("编程", 0.8, 2L)
        );

        double similarity = dimension.calculatePostSimilarity(tags1, tags2);

        assertEquals(1.0, similarity, 0.001);
    }

    /**
     * 测试帖子相似度计算 - 部分相同
     */
    @Test
    public void testPostSimilarityWithPartialTags() {
        List<PostTag> tags1 = Arrays.asList(
                createPostTag("Java", 1.0, 1L),
                createPostTag("编程", 0.8, 1L)
        );

        List<PostTag> tags2 = Arrays.asList(
                createPostTag("Java", 1.0, 2L),
                createPostTag("开发", 0.7, 2L)
        );

        double similarity = dimension.calculatePostSimilarity(tags1, tags2);

        assertTrue(similarity > 0.0 && similarity < 1.0);
    }

    /**
     * 测试帖子相似度计算 - 完全不同
     */
    @Test
    public void testPostSimilarityWithDifferentTags() {
        List<PostTag> tags1 = Arrays.asList(
                createPostTag("Java", 1.0, 1L),
                createPostTag("编程", 0.8, 1L)
        );

        List<PostTag> tags2 = Arrays.asList(
                createPostTag("Python", 1.0, 2L),
                createPostTag("机器学习", 0.9, 2L)
        );

        double similarity = dimension.calculatePostSimilarity(tags1, tags2);

        assertEquals(0.0, similarity, 0.001);
    }

    /**
     * 测试帖子相似度计算 - 空标签
     */
    @Test
    public void testPostSimilarityWithEmptyTags() {
        List<PostTag> tags1 = Arrays.asList(
                createPostTag("Java", 1.0, 1L)
        );

        double similarity = dimension.calculatePostSimilarity(tags1, new ArrayList<>());

        assertEquals(0.0, similarity, 0.001);
    }

    /**
     * 测试分数聚合 - 正常情况
     */
    @Test
    public void testAggregateScores() {
        List<Double> scores = Arrays.asList(0.8, 0.6, 0.4);
        List<Double> weights = Arrays.asList(1.0, 0.8, 0.6);

        double aggregated = dimension.aggregateScores(scores, weights);

        assertTrue(aggregated > 0.0 && aggregated <= 1.0);
    }

    /**
     * 测试分数聚合 - 空分数列表
     */
    @Test
    public void testAggregateScoresWithEmptyList() {
        double aggregated = dimension.aggregateScores(new ArrayList<>(), new ArrayList<>());

        assertEquals(0.0, aggregated, 0.001);
    }

    /**
     * 测试分数聚合 - null权重
     */
    @Test
    public void testAggregateScoresWithNullWeights() {
        List<Double> scores = Arrays.asList(0.8, 0.6);

        double aggregated = dimension.aggregateScores(scores, null);

        assertEquals(0.7, aggregated, 0.001);
    }

    /**
     * 测试获取维度名称
     */
    @Test
    public void testGetName() {
        assertEquals("BROWSE_HISTORY", dimension.getName());
    }

    /**
     * 测试衰减率设置
     */
    @Test
    public void testDecayRate() {
        double defaultRate = dimension.getDecayRate();
        assertEquals(0.1, defaultRate, 0.001);

        dimension.setDecayRate(0.2);
        assertEquals(0.2, dimension.getDecayRate(), 0.001);
    }

    /**
     * 测试空候选帖子标签
     */
    @Test
    public void testCalculate_WithEmptyCandidatePostTags() {
        long currentTime = System.currentTimeMillis();
        List<PostTag> historyTags = Arrays.asList(
                createPostTag("Java", 1.0, 100L)
        );

        BrowseHistory history = createBrowseHistory(100L, currentTime, historyTags);
        List<BrowseHistory> browseHistoryList = Arrays.asList(history);

        DimensionContext context = DimensionContext.builder()
                .browseHistory(browseHistoryList)
                .candidatePostTags(new ArrayList<>())
                .build();

        DimensionResult result = dimension.calculate(context);

        assertNotNull(result);
        assertEquals(0.0, result.getScore(), 0.001);
        assertEquals("候选帖子无标签信息", result.getDetails().get("reason"));
    }

    /**
     * 测试浏览历史中无有效标签
     */
    @Test
    public void testCalculate_WithBrowseHistoryNoTags() {
        long currentTime = System.currentTimeMillis();
        List<PostTag> candidateTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L)
        );

        BrowseHistory history = createBrowseHistory(100L, currentTime, new ArrayList<>());
        List<BrowseHistory> browseHistoryList = Arrays.asList(history);

        DimensionContext context = DimensionContext.builder()
                .browseHistory(browseHistoryList)
                .candidatePostTags(candidateTags)
                .build();

        DimensionResult result = dimension.calculate(context);

        assertNotNull(result);
        assertEquals(0.0, result.getScore(), 0.001);
        assertEquals("浏览历史中无有效标签信息", result.getDetails().get("reason"));
    }
}
