package com.example.demo.recommendation.dimension.impl;

import com.example.demo.recommendation.dimension.DimensionContext;
import com.example.demo.recommendation.dimension.DimensionResult;
import com.example.demo.recommendation.dimension.RecommendationDimension;
import com.example.demo.recommendation.model.PostTag;
import com.example.demo.recommendation.model.RecommendationConfig;
import com.example.demo.recommendation.model.UserTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 标签匹配维度实现类
 * 基于用户标签和帖子标签的匹配程度计算推荐分数
 */
public class TagMatchingDimension implements RecommendationDimension {

    private static final String DIMENSION_NAME = "TAG_MATCHING";

    private RecommendationConfig config;

    @Override
    public String getName() {
        return DIMENSION_NAME;
    }

    @Override
    public DimensionResult calculate(DimensionContext context) {
        DimensionResult result = new DimensionResult(DIMENSION_NAME, 0.0);

        List<UserTag> userTags = context.getUserTags();
        List<PostTag> candidatePostTags = context.getCandidatePostTags();

        if (userTags == null || userTags.isEmpty() ||
            candidatePostTags == null || candidatePostTags.isEmpty()) {
            result.addDetail("reason", "用户标签或候选帖子标签为空");
            result.addDetail("userTagCount", userTags != null ? userTags.size() : 0);
            result.addDetail("postTagCount", candidatePostTags != null ? candidatePostTags.size() : 0);
            return result;
        }

        Set<String> userTagNames = extractTagNames(userTags);
        Set<String> postTagNames = extractTagNames(candidatePostTags);

        double jaccardScore = calculateJaccardSimilarity(userTagNames, postTagNames);
        double weightedScore = calculateWeightedScore(userTags, candidatePostTags);

        double finalScore = (jaccardScore + weightedScore) / 2.0;

        result.setScore(finalScore);
        result.addDetail("jaccardSimilarity", jaccardScore);
        result.addDetail("weightedScore", weightedScore);
        result.addDetail("userTagNames", userTagNames);
        result.addDetail("postTagNames", postTagNames);
        result.addDetail("matchedTags", findMatchedTags(userTagNames, postTagNames));

        return result;
    }

    @Override
    public void initialize(RecommendationConfig config) {
        this.config = config;
    }

    /**
     * 计算两个集合的Jaccard相似度
     * Jaccard相似度 = 交集大小 / 并集大小
     * 取值范围：[0, 1]，值越大表示越相似
     *
     * @param set1 第一个集合
     * @param set2 第二个集合
     * @return Jaccard相似度，如果任一集合为空则返回0.0
     */
    public double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1 == null || set2 == null || set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    /**
     * 计算加权标签匹配分数
     * 综合考虑用户标签权重和帖子标签权重进行加权计算
     * 分数归一化到 [0, 1] 区间
     *
     * @param userTags 用户标签列表
     * @param postTags 帖子标签列表
     * @return 加权匹配分数
     */
    public double calculateWeightedScore(List<UserTag> userTags, List<PostTag> postTags) {
        if (userTags == null || userTags.isEmpty() || postTags == null || postTags.isEmpty()) {
            return 0.0;
        }

        Map<String, Double> userTagWeightMap = new HashMap<>();
        for (UserTag tag : userTags) {
            if (tag.getName() != null) {
                userTagWeightMap.put(tag.getName(), tag.getWeight());
            }
        }

        Map<String, Double> postTagWeightMap = new HashMap<>();
        for (PostTag tag : postTags) {
            if (tag.getName() != null) {
                postTagWeightMap.put(tag.getName(), tag.getWeight());
            }
        }

        double weightedSum = 0.0;
        double maxPossibleScore = 0.0;
        int matchCount = 0;

        for (Map.Entry<String, Double> entry : userTagWeightMap.entrySet()) {
            String tagName = entry.getKey();
            double userWeight = entry.getValue();

            if (postTagWeightMap.containsKey(tagName)) {
                double postWeight = postTagWeightMap.get(tagName);
                weightedSum += userWeight * postWeight;
                matchCount++;
            }

            maxPossibleScore += userWeight;
        }

        if (maxPossibleScore == 0.0 || matchCount == 0) {
            return 0.0;
        }

        return Math.min(1.0, weightedSum / maxPossibleScore);
    }

    /**
     * 从标签列表中提取标签名称集合
     * 支持UserTag和PostTag两种类型
     *
     * @param tags 标签列表
     * @return 标签名称集合
     */
    public Set<String> extractTagNames(List<?> tags) {
        Set<String> tagNames = new HashSet<>();

        if (tags == null || tags.isEmpty()) {
            return tagNames;
        }

        for (Object tag : tags) {
            if (tag instanceof UserTag) {
                String name = ((UserTag) tag).getName();
                if (name != null && !name.trim().isEmpty()) {
                    tagNames.add(name.trim().toLowerCase());
                }
            } else if (tag instanceof PostTag) {
                String name = ((PostTag) tag).getName();
                if (name != null && !name.trim().isEmpty()) {
                    tagNames.add(name.trim().toLowerCase());
                }
            }
        }

        return tagNames;
    }

    /**
     * 查找两个标签集合的交集
     *
     * @param userTagNames 用户标签名称集合
     * @param postTagNames 帖子标签名称集合
     * @return 匹配的标签名称列表
     */
    private List<String> findMatchedTags(Set<String> userTagNames, Set<String> postTagNames) {
        List<String> matchedTags = new ArrayList<>();

        if (userTagNames == null || postTagNames == null) {
            return matchedTags;
        }

        for (String tagName : userTagNames) {
            if (postTagNames.contains(tagName)) {
                matchedTags.add(tagName);
            }
        }

        return matchedTags;
    }
}
