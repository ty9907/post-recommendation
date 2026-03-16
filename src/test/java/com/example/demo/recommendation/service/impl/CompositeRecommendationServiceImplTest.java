package com.example.demo.recommendation.service.impl;

import com.example.demo.recommendation.dimension.DimensionContext;
import com.example.demo.recommendation.dimension.DimensionManager;
import com.example.demo.recommendation.dimension.DimensionResult;
import com.example.demo.recommendation.dimension.RecommendationDimension;
import com.example.demo.recommendation.model.BrowseHistory;
import com.example.demo.recommendation.model.PostTag;
import com.example.demo.recommendation.model.RecommendationConfig;
import com.example.demo.recommendation.model.RecommendationRequest;
import com.example.demo.recommendation.model.RecommendationResult;
import com.example.demo.recommendation.model.UserTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CompositeRecommendationServiceImpl测试类
 * 测试组合推荐服务的各种功能
 */
public class CompositeRecommendationServiceImplTest {

    private CompositeRecommendationServiceImpl service;

    @Mock
    private DimensionManager dimensionManager;

    @Mock
    private RecommendationDimension mockDimension;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CompositeRecommendationServiceImpl(dimensionManager);
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
     * 设置模拟维度
     */
    private void setupMockDimension(String name, double weight, double score) {
        when(dimensionManager.containsDimension(name)).thenReturn(true);
        when(dimensionManager.isDimensionEnabled(name)).thenReturn(true);
        when(dimensionManager.getDimension(name)).thenReturn(mockDimension);
        when(dimensionManager.getDimensionWeight(name)).thenReturn(weight);
        when(dimensionManager.getDimensionNames()).thenReturn(Arrays.asList(name));

        DimensionResult result = new DimensionResult(name, score);
        result.addDetail("test", true);
        when(mockDimension.calculate(any(DimensionContext.class))).thenReturn(result);
    }

    /**
     * 测试推荐功能
     * 验证推荐服务能够正确计算推荐结果
     */
    @Test
    public void testRecommend() {
        setupMockDimension("TAG_MATCHING", 1.0, 0.8);

        List<UserTag> userTags = Arrays.asList(
                createUserTag("Java", 1.0),
                createUserTag("编程", 0.8)
        );

        List<PostTag> postTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L),
                createPostTag("编程", 0.8, 1L),
                createPostTag("Python", 0.9, 2L)
        );

        RecommendationRequest request = new RecommendationRequest();
        request.setUserTags(userTags);
        request.setPostTags(postTags);

        List<RecommendationResult> results = service.recommend(request);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.get(0).getTotalScore() >= results.get(1).getTotalScore());
    }

    /**
     * 测试批量推荐
     * 验证批量推荐能够正确处理多个请求
     */
    @Test
    public void testBatchRecommend() {
        setupMockDimension("TAG_MATCHING", 1.0, 0.8);

        List<UserTag> userTags = Arrays.asList(
                createUserTag("Java", 1.0)
        );

        List<PostTag> postTags1 = Arrays.asList(
                createPostTag("Java", 1.0, 1L)
        );

        List<PostTag> postTags2 = Arrays.asList(
                createPostTag("Python", 1.0, 2L)
        );

        RecommendationRequest request1 = new RecommendationRequest();
        request1.setUserTags(userTags);
        request1.setPostTags(postTags1);

        RecommendationRequest request2 = new RecommendationRequest();
        request2.setUserTags(userTags);
        request2.setPostTags(postTags2);

        Map<String, RecommendationRequest> requests = new HashMap<>();
        requests.put("req1", request1);
        requests.put("req2", request2);

        Map<String, List<RecommendationResult>> results = service.batchRecommend(requests);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.containsKey("req1"));
        assertTrue(results.containsKey("req2"));
    }

    /**
     * 测试更新权重
     * 验证权重更新能够正确传递到维度管理器
     */
    @Test
    public void testUpdateDimensionWeights() {
        when(dimensionManager.containsDimension("DIM1")).thenReturn(true);
        when(dimensionManager.containsDimension("DIM2")).thenReturn(true);

        Map<String, Double> weights = new HashMap<>();
        weights.put("DIM1", 0.6);
        weights.put("DIM2", 0.4);

        service.updateDimensionWeights(weights);

        verify(dimensionManager).setDimensionWeight("DIM1", 0.6);
        verify(dimensionManager).setDimensionWeight("DIM2", 0.4);
        verify(dimensionManager).normalizeWeights();
    }

    /**
     * 测试更新权重 - null参数
     * 验证null参数不会导致异常
     */
    @Test
    public void testUpdateDimensionWeightsNull() {
        service.updateDimensionWeights(null);

        verify(dimensionManager, never()).setDimensionWeight(anyString(), anyDouble());
    }

    /**
     * 测试更新权重 - 空Map
     * 验证空Map不会导致异常
     */
    @Test
    public void testUpdateDimensionWeightsEmpty() {
        service.updateDimensionWeights(new HashMap<>());

        verify(dimensionManager, never()).setDimensionWeight(anyString(), anyDouble());
    }

    /**
     * 测试空请求
     * 验证空请求抛出异常
     */
    @Test
    public void testRecommendWithEmptyRequest() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.recommend(null);
        });
    }

    /**
     * 测试推荐 - 无候选帖子
     * 验证无候选帖子时返回空列表
     */
    @Test
    public void testRecommendWithNoCandidatePosts() {
        List<UserTag> userTags = Arrays.asList(
                createUserTag("Java", 1.0)
        );

        RecommendationRequest request = new RecommendationRequest();
        request.setUserTags(userTags);
        request.setPostTags(new ArrayList<>());

        List<RecommendationResult> results = service.recommend(request);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * 测试推荐 - 带限制数量
     * 验证返回结果数量受配置限制
     */
    @Test
    public void testRecommendWithLimit() {
        setupMockDimension("TAG_MATCHING", 1.0, 0.8);

        List<UserTag> userTags = Arrays.asList(
                createUserTag("Java", 1.0)
        );

        List<PostTag> postTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L),
                createPostTag("Python", 0.9, 2L),
                createPostTag("Go", 0.8, 3L)
        );

        RecommendationConfig config = new RecommendationConfig();
        config.setLimit(2);

        RecommendationRequest request = new RecommendationRequest();
        request.setUserTags(userTags);
        request.setPostTags(postTags);
        request.setConfig(config);

        List<RecommendationResult> results = service.recommend(request);

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    /**
     * 测试批量推荐 - null请求
     * 验证null请求返回空Map
     */
    @Test
    public void testBatchRecommendNull() {
        Map<String, List<RecommendationResult>> results = service.batchRecommend(null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * 测试批量推荐 - 空请求Map
     * 验证空请求Map返回空Map
     */
    @Test
    public void testBatchRecommendEmpty() {
        Map<String, List<RecommendationResult>> results = service.batchRecommend(new HashMap<>());

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * 测试推荐 - 多维度
     * 验证多维度分数计算正确
     */
    @Test
    public void testRecommendWithMultipleDimensions() {
        RecommendationDimension dimension1 = mock(RecommendationDimension.class);
        RecommendationDimension dimension2 = mock(RecommendationDimension.class);

        when(dimensionManager.getDimensionNames()).thenReturn(Arrays.asList("DIM1", "DIM2"));
        when(dimensionManager.isDimensionEnabled("DIM1")).thenReturn(true);
        when(dimensionManager.isDimensionEnabled("DIM2")).thenReturn(true);
        when(dimensionManager.getDimension("DIM1")).thenReturn(dimension1);
        when(dimensionManager.getDimension("DIM2")).thenReturn(dimension2);
        when(dimensionManager.getDimensionWeight("DIM1")).thenReturn(0.6);
        when(dimensionManager.getDimensionWeight("DIM2")).thenReturn(0.4);
        when(dimensionManager.containsDimension("DIM1")).thenReturn(true);
        when(dimensionManager.containsDimension("DIM2")).thenReturn(true);

        DimensionResult result1 = new DimensionResult("DIM1", 0.8);
        DimensionResult result2 = new DimensionResult("DIM2", 0.6);
        when(dimension1.calculate(any(DimensionContext.class))).thenReturn(result1);
        when(dimension2.calculate(any(DimensionContext.class))).thenReturn(result2);

        List<UserTag> userTags = Arrays.asList(createUserTag("Java", 1.0));
        List<PostTag> postTags = Arrays.asList(createPostTag("Java", 1.0, 1L));

        RecommendationRequest request = new RecommendationRequest();
        request.setUserTags(userTags);
        request.setPostTags(postTags);

        List<RecommendationResult> results = service.recommend(request);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).getTotalScore() > 0);
        assertTrue(results.get(0).getDimensionScores().containsKey("DIM1"));
        assertTrue(results.get(0).getDimensionScores().containsKey("DIM2"));
    }

    /**
     * 测试推荐 - 禁用维度
     * 验证禁用的维度不参与计算
     */
    @Test
    public void testRecommendWithDisabledDimension() {
        when(dimensionManager.getDimensionNames()).thenReturn(Arrays.asList("DIM1"));
        when(dimensionManager.isDimensionEnabled("DIM1")).thenReturn(false);

        List<UserTag> userTags = Arrays.asList(createUserTag("Java", 1.0));
        List<PostTag> postTags = Arrays.asList(createPostTag("Java", 1.0, 1L));

        RecommendationRequest request = new RecommendationRequest();
        request.setUserTags(userTags);
        request.setPostTags(postTags);

        List<RecommendationResult> results = service.recommend(request);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * 测试推荐 - 使用浏览历史
     * 验证浏览历史能够正确参与推荐计算
     */
    @Test
    public void testRecommendWithBrowseHistory() {
        setupMockDimension("BROWSE_HISTORY", 1.0, 0.7);

        long currentTime = System.currentTimeMillis();
        List<PostTag> historyTags = Arrays.asList(
                createPostTag("Java", 1.0, 100L)
        );

        BrowseHistory history = createBrowseHistory(100L, currentTime, historyTags);
        List<BrowseHistory> browseHistory = Arrays.asList(history);

        List<PostTag> postTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L)
        );

        RecommendationRequest request = new RecommendationRequest();
        request.setBrowseHistory(browseHistory);
        request.setPostTags(postTags);

        List<RecommendationResult> results = service.recommend(request);

        assertNotNull(results);
        assertEquals(1, results.size());
    }
}
