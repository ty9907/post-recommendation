package com.example.demo.recommendation.service.impl;

import com.example.demo.recommendation.dimension.DimensionContext;
import com.example.demo.recommendation.dimension.DimensionManager;
import com.example.demo.recommendation.dimension.DimensionResult;
import com.example.demo.recommendation.dimension.RecommendationDimension;
import com.example.demo.recommendation.model.PostTag;
import com.example.demo.recommendation.model.RecommendationConfig;
import com.example.demo.recommendation.model.RecommendationRequest;
import com.example.demo.recommendation.model.RecommendationResult;
import com.example.demo.recommendation.service.RecommendationService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组合推荐服务实现类
 * 实现多维度加权组合推荐功能
 */
public class CompositeRecommendationServiceImpl implements RecommendationService {

    private final DimensionManager dimensionManager;

    /**
     * 构造器
     * @param dimensionManager 维度管理器
     */
    public CompositeRecommendationServiceImpl(DimensionManager dimensionManager) {
        this.dimensionManager = dimensionManager;
    }

    @Override
    public List<RecommendationResult> recommend(RecommendationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("推荐请求不能为空");
        }

        List<Long> candidatePostIds = extractCandidatePostIds(request);
        if (candidatePostIds.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, Map<String, PostTag>> postTagMap = buildPostTagMap(request.getPostTags());

        List<RecommendationResult> results = new ArrayList<>();
        for (Long postId : candidatePostIds) {
            RecommendationResult result = calculatePostScore(request, postId, postTagMap);
            if (result != null) {
                results.add(result);
            }
        }

        results.sort(Comparator.comparingDouble(RecommendationResult::getTotalScore).reversed());

        int limit = getLimit(request);
        if (limit > 0 && results.size() > limit) {
            results = results.subList(0, limit);
        }

        return results;
    }

    @Override
    public Map<String, List<RecommendationResult>> batchRecommend(Map<String, RecommendationRequest> requests) {
        if (requests == null) {
            return new HashMap<>();
        }

        return requests.entrySet().parallelStream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> recommend(entry.getValue())
                ));
    }

    @Override
    public void updateDimensionWeights(Map<String, Double> weights) {
        if (weights == null) {
            return;
        }

        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            String dimensionName = entry.getKey();
            Double weight = entry.getValue();
            if (dimensionName != null && weight != null && dimensionManager.containsDimension(dimensionName)) {
                dimensionManager.setDimensionWeight(dimensionName, weight);
            }
        }

        dimensionManager.normalizeWeights();
    }

    /**
     * 提取候选帖子ID列表
     * @param request 推荐请求
     * @return 候选帖子ID列表
     */
    private List<Long> extractCandidatePostIds(RecommendationRequest request) {
        List<Long> postIds = new ArrayList<>();
        if (request.getPostTags() != null) {
            for (var postTag : request.getPostTags()) {
                if (postTag.getPostId() != null && !postIds.contains(postTag.getPostId())) {
                    postIds.add(postTag.getPostId());
                }
            }
        }
        return postIds;
    }

    /**
     * 构建帖子标签映射
     * @param postTags 帖子标签列表
     * @return 帖子ID到标签的映射
     */
    private Map<Long, Map<String, PostTag>> buildPostTagMap(List<PostTag> postTags) {
        Map<Long, Map<String, PostTag>> postTagMap = new HashMap<>();
        if (postTags != null) {
            for (PostTag postTag : postTags) {
                postTagMap.computeIfAbsent(postTag.getPostId(), k -> new HashMap<>())
                        .put(postTag.getName(), postTag);
            }
        }
        return postTagMap;
    }

    /**
     * 计算单个帖子的推荐分数
     * @param request 推荐请求
     * @param postId 帖子ID
     * @param postTagMap 帖子标签映射
     * @return 推荐结果
     */
    private RecommendationResult calculatePostScore(RecommendationRequest request, Long postId,
                                                     Map<Long, Map<String, PostTag>> postTagMap) {
        List<String> enabledDimensions = getEnabledDimensions();
        if (enabledDimensions.isEmpty()) {
            return null;
        }

        Map<String, Double> dimensionScores = new HashMap<>();
        double totalScore = 0.0;
        double totalWeight = 0.0;

        List<PostTag> candidatePostTags = new ArrayList<>();
        Map<String, PostTag> tagMap = postTagMap.get(postId);
        if (tagMap != null) {
            candidatePostTags.addAll(tagMap.values());
        }

        DimensionContext context = DimensionContext.builder()
                .userTags(request.getUserTags())
                .postTags(request.getPostTags())
                .browseHistory(request.getBrowseHistory())
                .candidatePostId(postId)
                .candidatePostTags(candidatePostTags)
                .build();

        for (String dimensionName : enabledDimensions) {
            RecommendationDimension dimension = dimensionManager.getDimension(dimensionName);
            if (dimension == null) {
                continue;
            }

            DimensionResult dimensionResult = dimension.calculate(context);
            if (dimensionResult != null) {
                double score = dimensionResult.getScore();
                double weight = dimensionManager.getDimensionWeight(dimensionName);

                dimensionScores.put(dimensionName, score);
                totalScore += score * weight;
                totalWeight += weight;
            }
        }

        if (totalWeight > 0) {
            totalScore = totalScore / totalWeight;
        }

        RecommendationResult result = new RecommendationResult();
        result.setPostId(postId);
        result.setTotalScore(totalScore);
        result.setDimensionScores(dimensionScores);

        return result;
    }

    /**
     * 获取启用的维度列表
     * @return 启用的维度名称列表
     */
    private List<String> getEnabledDimensions() {
        List<String> enabledDimensions = new ArrayList<>();
        for (String dimensionName : dimensionManager.getDimensionNames()) {
            if (dimensionManager.isDimensionEnabled(dimensionName)) {
                enabledDimensions.add(dimensionName);
            }
        }
        return enabledDimensions;
    }

    /**
     * 获取返回结果数量限制
     * @param request 推荐请求
     * @return 数量限制，0表示不限制
     */
    private int getLimit(RecommendationRequest request) {
        RecommendationConfig config = request.getConfig();
        return config != null ? config.getLimit() : 0;
    }
}
