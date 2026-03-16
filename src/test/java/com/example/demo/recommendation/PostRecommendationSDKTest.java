package com.example.demo.recommendation;

import com.example.demo.recommendation.config.SDKConfig;
import com.example.demo.recommendation.config.SDKConfigBuilder;
import com.example.demo.recommendation.exception.InvalidRequestException;
import com.example.demo.recommendation.exception.SDKNotInitializedException;
import com.example.demo.recommendation.model.PostTag;
import com.example.demo.recommendation.model.RecommendationRequest;
import com.example.demo.recommendation.model.RecommendationResult;
import com.example.demo.recommendation.model.UserTag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PostRecommendationSDK测试类
 * 测试SDK入口的各种功能
 */
public class PostRecommendationSDKTest {

    private PostRecommendationSDK sdk;

    @BeforeEach
    public void setUp() throws Exception {
        resetSDKSingleton();
        sdk = PostRecommendationSDK.getInstance();
    }

    @AfterEach
    public void tearDown() {
        if (sdk != null && sdk.isInitialized()) {
            sdk.shutdown();
        }
    }

    /**
     * 重置SDK单例状态
     * 通过反射重置单例实例，确保测试之间相互独立
     */
    private void resetSDKSingleton() throws Exception {
        Field instanceField = PostRecommendationSDK.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        java.util.concurrent.atomic.AtomicReference<PostRecommendationSDK> instanceRef =
                (java.util.concurrent.atomic.AtomicReference<PostRecommendationSDK>) instanceField.get(null);
        instanceRef.set(null);
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
     * 创建默认的SDK配置
     * @return SDK配置实例
     */
    private SDKConfig createDefaultConfig() {
        return SDKConfig.builder()
                .addDimensionWeight("TAG_MATCHING", 0.5)
                .addDimensionWeight("BROWSE_HISTORY", 0.5)
                .defaultLimit(10)
                .threadPoolSize(4)
                .build();
    }

    /**
     * 测试初始化
     * 验证SDK能够正确初始化
     */
    @Test
    public void testInitialize() {
        SDKConfig config = createDefaultConfig();

        sdk.initialize(config);

        assertTrue(sdk.isInitialized());
        assertNotNull(sdk.getConfig());
        assertEquals(config, sdk.getConfig());
    }

    /**
     * 测试初始化 - 重复初始化
     * 验证重复初始化不会导致异常
     */
    @Test
    public void testInitializeTwice() {
        SDKConfig config = createDefaultConfig();

        sdk.initialize(config);
        assertTrue(sdk.isInitialized());

        sdk.initialize(config);
        assertTrue(sdk.isInitialized());
    }

    /**
     * 测试初始化 - null配置
     * 验证null配置抛出异常
     */
    @Test
    public void testInitializeWithNullConfig() {
        assertThrows(IllegalArgumentException.class, () -> {
            sdk.initialize(null);
        });
    }

    /**
     * 测试未初始化推荐
     * 验证未初始化时调用推荐抛出异常
     */
    @Test
    public void testRecommendWithoutInitialize() {
        RecommendationRequest request = new RecommendationRequest();
        request.setUserTags(Arrays.asList(createUserTag("Java", 1.0)));
        request.setPostTags(Arrays.asList(createPostTag("Java", 1.0, 1L)));

        assertThrows(SDKNotInitializedException.class, () -> {
            sdk.recommend(request);
        });
    }

    /**
     * 测试推荐
     * 验证推荐功能能够正常工作
     */
    @Test
    public void testRecommend() {
        SDKConfig config = createDefaultConfig();
        sdk.initialize(config);

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

        List<RecommendationResult> results = sdk.recommend(request);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.get(0).getTotalScore() >= results.get(1).getTotalScore());
    }

    /**
     * 测试推荐 - null请求
     * 验证null请求抛出异常
     */
    @Test
    public void testRecommendWithNullRequest() {
        SDKConfig config = createDefaultConfig();
        sdk.initialize(config);

        assertThrows(InvalidRequestException.class, () -> {
            sdk.recommend(null);
        });
    }

    /**
     * 测试推荐 - 空请求
     * 验证空请求（无用户标签、帖子标签、浏览历史）抛出异常
     */
    @Test
    public void testRecommendWithEmptyRequest() {
        SDKConfig config = createDefaultConfig();
        sdk.initialize(config);

        RecommendationRequest request = new RecommendationRequest();

        assertThrows(InvalidRequestException.class, () -> {
            sdk.recommend(request);
        });
    }

    /**
     * 测试关闭
     * 验证SDK能够正确关闭
     */
    @Test
    public void testShutdown() {
        SDKConfig config = createDefaultConfig();
        sdk.initialize(config);
        assertTrue(sdk.isInitialized());

        sdk.shutdown();

        assertFalse(sdk.isInitialized());
    }

    /**
     * 测试关闭 - 重复关闭
     * 验证重复关闭不会导致异常
     */
    @Test
    public void testShutdownTwice() {
        SDKConfig config = createDefaultConfig();
        sdk.initialize(config);

        sdk.shutdown();
        assertFalse(sdk.isInitialized());

        sdk.shutdown();
        assertFalse(sdk.isInitialized());
    }

    /**
     * 测试关闭后推荐
     * 验证关闭后调用推荐抛出异常
     */
    @Test
    public void testRecommendAfterShutdown() {
        SDKConfig config = createDefaultConfig();
        sdk.initialize(config);
        sdk.shutdown();

        RecommendationRequest request = new RecommendationRequest();
        request.setUserTags(Arrays.asList(createUserTag("Java", 1.0)));
        request.setPostTags(Arrays.asList(createPostTag("Java", 1.0, 1L)));

        assertThrows(SDKNotInitializedException.class, () -> {
            sdk.recommend(request);
        });
    }

    /**
     * 测试批量推荐
     * 验证批量推荐功能能够正常工作
     */
    @Test
    public void testBatchRecommend() {
        SDKConfig config = createDefaultConfig();
        sdk.initialize(config);

        List<UserTag> userTags = Arrays.asList(createUserTag("Java", 1.0));
        List<PostTag> postTags1 = Arrays.asList(createPostTag("Java", 1.0, 1L));
        List<PostTag> postTags2 = Arrays.asList(createPostTag("Python", 1.0, 2L));

        RecommendationRequest request1 = new RecommendationRequest();
        request1.setUserTags(userTags);
        request1.setPostTags(postTags1);

        RecommendationRequest request2 = new RecommendationRequest();
        request2.setUserTags(userTags);
        request2.setPostTags(postTags2);

        Map<String, RecommendationRequest> requests = new HashMap<>();
        requests.put("req1", request1);
        requests.put("req2", request2);

        Map<String, List<RecommendationResult>> results = sdk.batchRecommend(requests);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.containsKey("req1"));
        assertTrue(results.containsKey("req2"));
    }

    /**
     * 测试批量推荐 - null请求
     * 验证null请求返回空Map
     */
    @Test
    public void testBatchRecommendWithNull() {
        SDKConfig config = createDefaultConfig();
        sdk.initialize(config);

        Map<String, List<RecommendationResult>> results = sdk.batchRecommend(null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * 测试更新维度权重
     * 验证权重更新功能能够正常工作
     */
    @Test
    public void testUpdateDimensionWeights() {
        SDKConfig config = createDefaultConfig();
        sdk.initialize(config);

        Map<String, Double> weights = new HashMap<>();
        weights.put("TAG_MATCHING", 0.7);
        weights.put("BROWSE_HISTORY", 0.3);

        sdk.updateDimensionWeights(weights);

        assertNotNull(sdk.getDimensionManager());
    }

    /**
     * 测试更新维度权重 - null参数
     * 验证null参数不会导致异常
     */
    @Test
    public void testUpdateDimensionWeightsWithNull() {
        SDKConfig config = createDefaultConfig();
        sdk.initialize(config);

        sdk.updateDimensionWeights(null);

        assertNotNull(sdk.getDimensionManager());
    }

    /**
     * 测试获取维度管理器
     * 验证能够获取维度管理器实例
     */
    @Test
    public void testGetDimensionManager() {
        SDKConfig config = createDefaultConfig();
        sdk.initialize(config);

        assertNotNull(sdk.getDimensionManager());
        assertTrue(sdk.getDimensionManager().containsDimension("TAG_MATCHING"));
        assertTrue(sdk.getDimensionManager().containsDimension("BROWSE_HISTORY"));
    }

    /**
     * 测试获取维度管理器 - 未初始化
     * 验证未初始化时获取维度管理器抛出异常
     */
    @Test
    public void testGetDimensionManagerWithoutInitialize() {
        assertThrows(SDKNotInitializedException.class, () -> {
            sdk.getDimensionManager();
        });
    }

    /**
     * 测试获取配置
     * 验证能够获取SDK配置
     */
    @Test
    public void testGetConfig() {
        SDKConfig config = createDefaultConfig();
        sdk.initialize(config);

        SDKConfig retrievedConfig = sdk.getConfig();

        assertNotNull(retrievedConfig);
        assertEquals(config, retrievedConfig);
    }

    /**
     * 测试获取配置 - 未初始化
     * 验证未初始化时获取配置返回null
     */
    @Test
    public void testGetConfigWithoutInitialize() {
        SDKConfig retrievedConfig = sdk.getConfig();

        assertNull(retrievedConfig);
    }

    /**
     * 测试配置构建器
     * 验证静态配置构建器方法能够正常工作
     */
    @Test
    public void testConfigBuilder() {
        SDKConfigBuilder builder = PostRecommendationSDK.configBuilder();

        assertNotNull(builder);
    }

    /**
     * 测试单例模式
     * 验证多次获取实例返回同一对象
     */
    @Test
    public void testSingleton() throws Exception {
        resetSDKSingleton();

        PostRecommendationSDK instance1 = PostRecommendationSDK.getInstance();
        PostRecommendationSDK instance2 = PostRecommendationSDK.getInstance();

        assertSame(instance1, instance2);
    }

    /**
     * 测试推荐 - 默认限制
     * 验证返回结果数量受默认限制配置影响
     */
    @Test
    public void testRecommendWithDefaultLimit() {
        SDKConfig config = SDKConfig.builder()
                .addDimensionWeight("TAG_MATCHING", 1.0)
                .defaultLimit(2)
                .threadPoolSize(4)
                .build();
        sdk.initialize(config);

        List<UserTag> userTags = Arrays.asList(createUserTag("Java", 1.0));
        List<PostTag> postTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L),
                createPostTag("Java", 0.9, 2L),
                createPostTag("Java", 0.8, 3L),
                createPostTag("Python", 0.7, 4L)
        );

        RecommendationRequest request = new RecommendationRequest();
        request.setUserTags(userTags);
        request.setPostTags(postTags);

        List<RecommendationResult> results = sdk.recommend(request);

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    /**
     * 测试推荐 - 请求级限制覆盖默认限制
     * 验证请求级限制能够覆盖默认限制
     */
    @Test
    public void testRecommendWithRequestLimit() {
        SDKConfig config = SDKConfig.builder()
                .addDimensionWeight("TAG_MATCHING", 1.0)
                .defaultLimit(10)
                .threadPoolSize(4)
                .build();
        sdk.initialize(config);

        List<UserTag> userTags = Arrays.asList(createUserTag("Java", 1.0));
        List<PostTag> postTags = Arrays.asList(
                createPostTag("Java", 1.0, 1L),
                createPostTag("Java", 0.9, 2L),
                createPostTag("Java", 0.8, 3L)
        );

        com.example.demo.recommendation.model.RecommendationConfig requestConfig =
                new com.example.demo.recommendation.model.RecommendationConfig();
        requestConfig.setLimit(1);

        RecommendationRequest request = new RecommendationRequest();
        request.setUserTags(userTags);
        request.setPostTags(postTags);
        request.setConfig(requestConfig);

        List<RecommendationResult> results = sdk.recommend(request);

        assertNotNull(results);
        assertEquals(1, results.size());
    }
}
