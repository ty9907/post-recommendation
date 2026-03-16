package com.example.demo.recommendation;

import com.example.demo.recommendation.config.SDKConfig;
import com.example.demo.recommendation.config.SDKConfigBuilder;
import com.example.demo.recommendation.dimension.DimensionManager;
import com.example.demo.recommendation.dimension.RecommendationDimension;
import com.example.demo.recommendation.dimension.impl.BrowseHistoryDimension;
import com.example.demo.recommendation.dimension.impl.TagMatchingDimension;
import com.example.demo.recommendation.exception.InvalidRequestException;
import com.example.demo.recommendation.exception.SDKNotInitializedException;
import com.example.demo.recommendation.model.RecommendationRequest;
import com.example.demo.recommendation.model.RecommendationResult;
import com.example.demo.recommendation.service.RecommendationService;
import com.example.demo.recommendation.service.impl.CompositeRecommendationServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 帖子推荐SDK入口类
 * 提供统一的推荐服务访问接口，采用单例模式实现
 */
public class PostRecommendationSDK {

    private static final AtomicReference<PostRecommendationSDK> INSTANCE = new AtomicReference<>();

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    private SDKConfig config;
    private DimensionManager dimensionManager;
    private RecommendationService recommendationService;
    private ExecutorService executorService;

    /**
     * 私有构造器
     */
    private PostRecommendationSDK() {
    }

    /**
     * 获取单例实例
     * @return SDK单例实例
     */
    public static PostRecommendationSDK getInstance() {
        PostRecommendationSDK instance = INSTANCE.get();
        if (instance == null) {
            instance = new PostRecommendationSDK();
            if (INSTANCE.compareAndSet(null, instance)) {
                return instance;
            }
            return INSTANCE.get();
        }
        return instance;
    }

    /**
     * 初始化SDK
     * @param config SDK配置
     * @throws IllegalArgumentException 当配置为空时抛出
     */
    public synchronized void initialize(SDKConfig config) {
        if (initialized.get()) {
            return;
        }

        if (config == null) {
            throw new IllegalArgumentException("SDK配置不能为空");
        }

        this.config = config;

        this.dimensionManager = new DimensionManager();

        registerDefaultDimensions();

        this.recommendationService = new CompositeRecommendationServiceImpl(dimensionManager);

        this.executorService = Executors.newFixedThreadPool(config.getThreadPoolSize());

        initialized.set(true);
        shutdown.set(false);
    }

    /**
     * 注册默认维度
     */
    private void registerDefaultDimensions() {
        Map<String, Double> weights = config.getDimensionWeights();

        double tagMatchingWeight = weights.getOrDefault("TAG_MATCHING", 0.5);
        double browseHistoryWeight = weights.getOrDefault("BROWSE_HISTORY", 0.5);

        RecommendationDimension tagMatchingDimension = new TagMatchingDimension();
        dimensionManager.registerDimension(tagMatchingDimension.getName(), tagMatchingDimension, tagMatchingWeight);

        RecommendationDimension browseHistoryDimension = new BrowseHistoryDimension();
        dimensionManager.registerDimension(browseHistoryDimension.getName(), browseHistoryDimension, browseHistoryWeight);

        dimensionManager.normalizeWeights();
    }

    /**
     * 关闭SDK
     * 释放资源，SDK关闭后需要重新初始化才能使用
     */
    public synchronized void shutdown() {
        if (!initialized.get() || shutdown.get()) {
            return;
        }

        shutdown.set(true);

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (dimensionManager != null) {
            dimensionManager.clear();
        }

        initialized.set(false);
    }

    /**
     * 推荐接口
     * 根据推荐请求计算推荐结果
     * @param request 推荐请求
     * @return 推荐结果列表，按总分降序排列
     * @throws SDKNotInitializedException 当SDK未初始化时抛出
     * @throws InvalidRequestException 当请求无效时抛出
     */
    public List<RecommendationResult> recommend(RecommendationRequest request) {
        checkInitialized();
        validateRequest(request);

        List<RecommendationResult> results = recommendationService.recommend(request);

        return applyDefaultLimit(results);
    }

    /**
     * 批量推荐
     * 并行处理多个推荐请求
     * @param requests 请求映射，键为请求ID，值为推荐请求
     * @return 结果映射，键为请求ID，值为推荐结果列表
     * @throws SDKNotInitializedException 当SDK未初始化时抛出
     * @throws InvalidRequestException 当请求无效时抛出
     */
    public Map<String, List<RecommendationResult>> batchRecommend(Map<String, RecommendationRequest> requests) {
        checkInitialized();

        if (requests == null || requests.isEmpty()) {
            return Map.of();
        }

        for (Map.Entry<String, RecommendationRequest> entry : requests.entrySet()) {
            if (entry.getValue() == null) {
                throw InvalidRequestException.nullField("requests[" + entry.getKey() + "]");
            }
        }

        Map<String, List<RecommendationResult>> results = recommendationService.batchRecommend(requests);

        for (Map.Entry<String, List<RecommendationResult>> entry : results.entrySet()) {
            entry.setValue(applyDefaultLimit(entry.getValue()));
        }

        return results;
    }

    /**
     * 动态更新维度权重
     * @param weights 维度权重映射，键为维度名称，值为权重值
     * @throws SDKNotInitializedException 当SDK未初始化时抛出
     */
    public void updateDimensionWeights(Map<String, Double> weights) {
        checkInitialized();

        if (weights == null || weights.isEmpty()) {
            return;
        }

        recommendationService.updateDimensionWeights(weights);
    }

    /**
     * 获取维度管理器
     * @return 维度管理器实例
     * @throws SDKNotInitializedException 当SDK未初始化时抛出
     */
    public DimensionManager getDimensionManager() {
        checkInitialized();
        return dimensionManager;
    }

    /**
     * 检查SDK是否已初始化
     * @return 是否已初始化
     */
    public boolean isInitialized() {
        return initialized.get() && !shutdown.get();
    }

    /**
     * 获取当前配置
     * @return SDK配置，未初始化时返回null
     */
    public SDKConfig getConfig() {
        return config;
    }

    /**
     * 检查SDK是否已初始化
     * @throws SDKNotInitializedException 当SDK未初始化时抛出
     */
    private void checkInitialized() {
        if (!initialized.get() || shutdown.get()) {
            throw new SDKNotInitializedException();
        }
    }

    /**
     * 验证推荐请求
     * @param request 推荐请求
     * @throws InvalidRequestException 当请求无效时抛出
     */
    private void validateRequest(RecommendationRequest request) {
        if (request == null) {
            throw InvalidRequestException.nullField("request");
        }

        boolean hasUserTags = request.getUserTags() != null && !request.getUserTags().isEmpty();
        boolean hasPostTags = request.getPostTags() != null && !request.getPostTags().isEmpty();
        boolean hasBrowseHistory = request.getBrowseHistory() != null && !request.getBrowseHistory().isEmpty();

        if (!hasUserTags && !hasPostTags && !hasBrowseHistory) {
            throw new InvalidRequestException("推荐请求必须包含用户标签、帖子标签或浏览历史中的至少一项");
        }
    }

    /**
     * 应用默认限制
     * @param results 推荐结果列表
     * @return 限制后的结果列表
     */
    private List<RecommendationResult> applyDefaultLimit(List<RecommendationResult> results) {
        if (results == null || results.isEmpty()) {
            return new ArrayList<>();
        }

        int limit = config.getDefaultLimit();
        if (limit > 0 && results.size() > limit) {
            return new ArrayList<>(results.subList(0, limit));
        }

        return results;
    }

    /**
     * 创建配置构建器
     * @return 配置构建器实例
     */
    public static SDKConfigBuilder configBuilder() {
        return new SDKConfigBuilder();
    }
}
