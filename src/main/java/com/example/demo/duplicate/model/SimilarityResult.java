package com.example.demo.duplicate.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 相似度检测结果类
 * 用于存储两篇文章之间的相似度检测结果
 */
public class SimilarityResult {
    private Long articleId;             // 原文章ID
    private Long comparedArticleId;     // 对比文章ID
    private double similarity;          // 相似度（0-1之间）
    private String algorithm;           // 使用的算法名称
    private LocalDateTime checkTime;    // 检测时间
    private Map<String, Object> details; // 详细信息

    /**
     * 默认构造器
     */
    public SimilarityResult() {
    }

    /**
     * 带参数的构造器
     * @param articleId 原文章ID
     * @param comparedArticleId 对比文章ID
     * @param similarity 相似度
     * @param algorithm 使用的算法名称
     * @param checkTime 检测时间
     * @param details 详细信息
     */
    public SimilarityResult(Long articleId, Long comparedArticleId, double similarity, 
                            String algorithm, LocalDateTime checkTime, Map<String, Object> details) {
        this.articleId = articleId;
        this.comparedArticleId = comparedArticleId;
        this.similarity = similarity;
        this.algorithm = algorithm;
        this.checkTime = checkTime;
        this.details = details;
    }

    /**
     * 获取原文章ID
     * @return 原文章ID
     */
    public Long getArticleId() {
        return articleId;
    }

    /**
     * 设置原文章ID
     * @param articleId 原文章ID
     */
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    /**
     * 获取对比文章ID
     * @return 对比文章ID
     */
    public Long getComparedArticleId() {
        return comparedArticleId;
    }

    /**
     * 设置对比文章ID
     * @param comparedArticleId 对比文章ID
     */
    public void setComparedArticleId(Long comparedArticleId) {
        this.comparedArticleId = comparedArticleId;
    }

    /**
     * 获取相似度
     * @return 相似度
     */
    public double getSimilarity() {
        return similarity;
    }

    /**
     * 设置相似度
     * @param similarity 相似度
     */
    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    /**
     * 获取使用的算法名称
     * @return 算法名称
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * 设置使用的算法名称
     * @param algorithm 算法名称
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * 获取检测时间
     * @return 检测时间
     */
    public LocalDateTime getCheckTime() {
        return checkTime;
    }

    /**
     * 设置检测时间
     * @param checkTime 检测时间
     */
    public void setCheckTime(LocalDateTime checkTime) {
        this.checkTime = checkTime;
    }

    /**
     * 获取详细信息
     * @return 详细信息
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * 设置详细信息
     * @param details 详细信息
     */
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "SimilarityResult{" +
                "articleId=" + articleId +
                ", comparedArticleId=" + comparedArticleId +
                ", similarity=" + similarity +
                ", algorithm='" + algorithm + '\'' +
                ", checkTime=" + checkTime +
                ", details=" + details +
                '}';
    }
}
