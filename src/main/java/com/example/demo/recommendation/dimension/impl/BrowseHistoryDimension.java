package com.example.demo.recommendation.dimension.impl;

import com.example.demo.recommendation.dimension.DimensionContext;
import com.example.demo.recommendation.dimension.DimensionResult;
import com.example.demo.recommendation.dimension.RecommendationDimension;
import com.example.demo.recommendation.model.BrowseHistory;
import com.example.demo.recommendation.model.PostTag;
import com.example.demo.recommendation.model.RecommendationConfig;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 浏览历史维度实现类
 * 基于用户浏览历史计算候选帖子的推荐分数
 */
public class BrowseHistoryDimension implements RecommendationDimension {

    private static final String DIMENSION_NAME = "BROWSE_HISTORY";

    private static final double DEFAULT_DECAY_RATE = 0.1;

    private static final double BASE_WEIGHT = 1.0;

    private double decayRate = DEFAULT_DECAY_RATE;

    @Override
    public String getName() {
        return DIMENSION_NAME;
    }

    @Override
    public DimensionResult calculate(DimensionContext context) {
        List<BrowseHistory> browseHistoryList = context.getBrowseHistory();
        List<PostTag> candidatePostTags = context.getCandidatePostTags();

        Map<String, Object> details = new HashMap<>();
        details.put("browseHistoryCount", browseHistoryList.size());
        details.put("candidatePostTagCount", candidatePostTags.size());

        if (browseHistoryList == null || browseHistoryList.isEmpty()) {
            details.put("reason", "无浏览历史记录");
            return new DimensionResult(DIMENSION_NAME, 0.0, details);
        }

        if (candidatePostTags == null || candidatePostTags.isEmpty()) {
            details.put("reason", "候选帖子无标签信息");
            return new DimensionResult(DIMENSION_NAME, 0.0, details);
        }

        List<Double> scores = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        List<Map<String, Object>> historyDetails = new ArrayList<>();

        for (BrowseHistory history : browseHistoryList) {
            List<PostTag> historyTags = history.getTags();
            if (historyTags == null || historyTags.isEmpty()) {
                continue;
            }

            double similarity = calculatePostSimilarity(candidatePostTags, historyTags);
            double weight = calculateTimeDecayWeight(history.getBrowseTime());

            scores.add(similarity);
            weights.add(weight);

            Map<String, Object> historyDetail = new HashMap<>();
            historyDetail.put("postId", history.getPostId());
            historyDetail.put("similarity", similarity);
            historyDetail.put("weight", weight);
            historyDetails.add(historyDetail);
        }

        double finalScore;
        if (scores.isEmpty()) {
            finalScore = 0.0;
            details.put("reason", "浏览历史中无有效标签信息");
        } else {
            finalScore = aggregateScores(scores, weights);
            finalScore = normalizeScore(finalScore);
        }

        details.put("historyDetails", historyDetails);
        details.put("rawScore", finalScore);

        return new DimensionResult(DIMENSION_NAME, finalScore, details);
    }

    @Override
    public void initialize(RecommendationConfig config) {
        if (config != null && config.getDimensionWeights() != null) {
            Map<String, Double> weights = config.getDimensionWeights();
            if (weights.containsKey("browseHistoryDecayRate")) {
                this.decayRate = weights.get("browseHistoryDecayRate");
            }
        }
    }

    /**
     * 计算时间衰减权重
     * 使用指数衰减函数：weight = baseWeight * exp(-decayRate * hoursAgo)
     * @param browseTime 浏览时间戳（毫秒）
     * @return 时间衰减权重
     */
    public double calculateTimeDecayWeight(Long browseTime) {
        if (browseTime == null) {
            return 0.0;
        }

        long currentTime = System.currentTimeMillis();
        long diffMillis = currentTime - browseTime;
        double hoursAgo = diffMillis / (1000.0 * 60 * 60);

        double weight = BASE_WEIGHT * Math.exp(-decayRate * hoursAgo);

        return Math.max(0.0, weight);
    }

    /**
     * 计算两个帖子的标签相似度
     * 使用加权Jaccard相似度，考虑标签权重
     * @param post1Tags 帖子1的标签列表
     * @param post2Tags 帖子2的标签列表
     * @return 相似度分数 [0, 1]
     */
    public double calculatePostSimilarity(List<PostTag> post1Tags, List<PostTag> post2Tags) {
        if (post1Tags == null || post1Tags.isEmpty() || post2Tags == null || post2Tags.isEmpty()) {
            return 0.0;
        }

        Map<String, Double> tags1 = new HashMap<>();
        for (PostTag tag : post1Tags) {
            if (tag.getName() != null) {
                tags1.put(tag.getName(), tag.getWeight());
            }
        }

        Map<String, Double> tags2 = new HashMap<>();
        for (PostTag tag : post2Tags) {
            if (tag.getName() != null) {
                tags2.put(tag.getName(), tag.getWeight());
            }
        }

        Set<String> allTags = new HashSet<>();
        allTags.addAll(tags1.keySet());
        allTags.addAll(tags2.keySet());

        if (allTags.isEmpty()) {
            return 0.0;
        }

        double intersection = 0.0;
        double union = 0.0;

        for (String tag : allTags) {
            double weight1 = tags1.getOrDefault(tag, 0.0);
            double weight2 = tags2.getOrDefault(tag, 0.0);

            intersection += Math.min(weight1, weight2);
            union += Math.max(weight1, weight2);
        }

        if (union == 0.0) {
            return 0.0;
        }

        return intersection / union;
    }

    /**
     * 聚合多个分数
     * 使用加权平均方法
     * @param scores 分数列表
     * @param weights 权重列表
     * @return 聚合后的分数
     */
    public double aggregateScores(List<Double> scores, List<Double> weights) {
        if (scores == null || scores.isEmpty()) {
            return 0.0;
        }

        if (weights == null || weights.size() != scores.size()) {
            return scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (int i = 0; i < scores.size(); i++) {
            double weight = weights.get(i);
            weightedSum += scores.get(i) * weight;
            totalWeight += weight;
        }

        if (totalWeight == 0.0) {
            return 0.0;
        }

        return weightedSum / totalWeight;
    }

    /**
     * 归一化分数到 [0, 1] 区间
     * @param score 原始分数
     * @return 归一化后的分数
     */
    private double normalizeScore(double score) {
        return Math.max(0.0, Math.min(1.0, score));
    }

    /**
     * 获取衰减率
     * @return 衰减率
     */
    public double getDecayRate() {
        return decayRate;
    }

    /**
     * 设置衰减率
     * @param decayRate 衰减率
     */
    public void setDecayRate(double decayRate) {
        this.decayRate = decayRate;
    }
}
