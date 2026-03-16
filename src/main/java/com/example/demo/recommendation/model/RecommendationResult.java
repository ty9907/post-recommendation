package com.example.demo.recommendation.model;

import java.util.Map;

/**
 * 推荐结果数据模型类
 * 用于存储推荐计算的结果
 */
public class RecommendationResult {
    private Long postId;                        // 帖子ID
    private double totalScore;                  // 总分
    private Map<String, Double> dimensionScores; // 各维度分数详情

    /**
     * 默认构造器
     */
    public RecommendationResult() {
    }

    /**
     * 带参数的构造器
     * @param postId 帖子ID
     * @param totalScore 总分
     * @param dimensionScores 各维度分数详情
     */
    public RecommendationResult(Long postId, double totalScore, Map<String, Double> dimensionScores) {
        this.postId = postId;
        this.totalScore = totalScore;
        this.dimensionScores = dimensionScores;
    }

    /**
     * 获取帖子ID
     * @return 帖子ID
     */
    public Long getPostId() {
        return postId;
    }

    /**
     * 设置帖子ID
     * @param postId 帖子ID
     */
    public void setPostId(Long postId) {
        this.postId = postId;
    }

    /**
     * 获取总分
     * @return 总分
     */
    public double getTotalScore() {
        return totalScore;
    }

    /**
     * 设置总分
     * @param totalScore 总分
     */
    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    /**
     * 获取各维度分数详情
     * @return 各维度分数详情
     */
    public Map<String, Double> getDimensionScores() {
        return dimensionScores;
    }

    /**
     * 设置各维度分数详情
     * @param dimensionScores 各维度分数详情
     */
    public void setDimensionScores(Map<String, Double> dimensionScores) {
        this.dimensionScores = dimensionScores;
    }

    @Override
    public String toString() {
        return "RecommendationResult{" +
                "postId=" + postId +
                ", totalScore=" + totalScore +
                ", dimensionScores=" + dimensionScores +
                '}';
    }
}
