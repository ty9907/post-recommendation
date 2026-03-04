package com.example.demo.duplicate.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 查重检测报告类
 * 用于存储文章查重检测的完整报告
 */
public class DuplicateCheckReport {
    private Long articleId;                  // 被检测文章ID
    private List<SimilarityResult> results;  // 相似度检测结果列表
    private boolean hasDuplicate;            // 是否存在重复
    private LocalDateTime checkTime;         // 检测时间
    private String summary;                  // 检测摘要

    /**
     * 默认构造器
     */
    public DuplicateCheckReport() {
        this.results = new ArrayList<>();
    }

    /**
     * 带参数的构造器
     * @param articleId 被检测文章ID
     * @param results 相似度检测结果列表
     * @param hasDuplicate 是否存在重复
     * @param checkTime 检测时间
     * @param summary 检测摘要
     */
    public DuplicateCheckReport(Long articleId, List<SimilarityResult> results, 
                                boolean hasDuplicate, LocalDateTime checkTime, String summary) {
        this.articleId = articleId;
        this.results = results != null ? results : new ArrayList<>();
        this.hasDuplicate = hasDuplicate;
        this.checkTime = checkTime;
        this.summary = summary;
    }

    /**
     * 添加相似度检测结果
     * @param result 相似度检测结果
     */
    public void addResult(SimilarityResult result) {
        if (this.results == null) {
            this.results = new ArrayList<>();
        }
        this.results.add(result);
    }

    /**
     * 获取被检测文章ID
     * @return 被检测文章ID
     */
    public Long getArticleId() {
        return articleId;
    }

    /**
     * 设置被检测文章ID
     * @param articleId 被检测文章ID
     */
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    /**
     * 获取相似度检测结果列表
     * @return 相似度检测结果列表
     */
    public List<SimilarityResult> getResults() {
        return results;
    }

    /**
     * 设置相似度检测结果列表
     * @param results 相似度检测结果列表
     */
    public void setResults(List<SimilarityResult> results) {
        this.results = results;
    }

    /**
     * 获取是否存在重复
     * @return 是否存在重复
     */
    public boolean isHasDuplicate() {
        return hasDuplicate;
    }

    /**
     * 设置是否存在重复
     * @param hasDuplicate 是否存在重复
     */
    public void setHasDuplicate(boolean hasDuplicate) {
        this.hasDuplicate = hasDuplicate;
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
     * 获取检测摘要
     * @return 检测摘要
     */
    public String getSummary() {
        return summary;
    }

    /**
     * 设置检测摘要
     * @param summary 检测摘要
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "DuplicateCheckReport{" +
                "articleId=" + articleId +
                ", results=" + results +
                ", hasDuplicate=" + hasDuplicate +
                ", checkTime=" + checkTime +
                ", summary='" + summary + '\'' +
                '}';
    }
}
