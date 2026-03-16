package com.example.demo.recommendation.dimension;

import java.util.HashMap;
import java.util.Map;

/**
 * 维度结果数据模型类
 * 用于存储维度计算的结果
 */
public class DimensionResult {
    private String dimensionName;       // 维度名称
    private double score;               // 分数
    private Map<String, Object> details; // 详情

    /**
     * 默认构造器
     */
    public DimensionResult() {
        this.details = new HashMap<>();
    }

    /**
     * 带参数的构造器
     * @param dimensionName 维度名称
     * @param score 分数
     */
    public DimensionResult(String dimensionName, double score) {
        this.dimensionName = dimensionName;
        this.score = score;
        this.details = new HashMap<>();
    }

    /**
     * 带参数的构造器
     * @param dimensionName 维度名称
     * @param score 分数
     * @param details 详情
     */
    public DimensionResult(String dimensionName, double score, Map<String, Object> details) {
        this.dimensionName = dimensionName;
        this.score = score;
        this.details = details != null ? details : new HashMap<>();
    }

    /**
     * 获取维度名称
     * @return 维度名称
     */
    public String getDimensionName() {
        return dimensionName;
    }

    /**
     * 设置维度名称
     * @param dimensionName 维度名称
     */
    public void setDimensionName(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    /**
     * 获取分数
     * @return 分数
     */
    public double getScore() {
        return score;
    }

    /**
     * 设置分数
     * @param score 分数
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * 获取详情
     * @return 详情
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * 设置详情
     * @param details 详情
     */
    public void setDetails(Map<String, Object> details) {
        this.details = details != null ? details : new HashMap<>();
    }

    /**
     * 添加详情项
     * @param key 键
     * @param value 值
     */
    public void addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
    }

    @Override
    public String toString() {
        return "DimensionResult{" +
                "dimensionName='" + dimensionName + '\'' +
                ", score=" + score +
                ", details=" + details +
                '}';
    }
}
