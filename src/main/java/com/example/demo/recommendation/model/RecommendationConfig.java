package com.example.demo.recommendation.model;

import java.util.Map;

/**
 * 推荐配置数据模型类
 * 用于配置推荐系统的参数
 */
public class RecommendationConfig {
    private Map<String, Double> dimensionWeights;   // 维度权重配置
    private boolean enableCache;                    // 是否启用缓存
    private int limit;                              // 返回结果数量限制

    /**
     * 默认构造器
     */
    public RecommendationConfig() {
    }

    /**
     * 带参数的构造器
     * @param dimensionWeights 维度权重配置
     * @param enableCache 是否启用缓存
     * @param limit 返回结果数量限制
     */
    public RecommendationConfig(Map<String, Double> dimensionWeights, boolean enableCache, int limit) {
        this.dimensionWeights = dimensionWeights;
        this.enableCache = enableCache;
        this.limit = limit;
    }

    /**
     * 获取维度权重配置
     * @return 维度权重配置
     */
    public Map<String, Double> getDimensionWeights() {
        return dimensionWeights;
    }

    /**
     * 设置维度权重配置
     * @param dimensionWeights 维度权重配置
     */
    public void setDimensionWeights(Map<String, Double> dimensionWeights) {
        this.dimensionWeights = dimensionWeights;
    }

    /**
     * 是否启用缓存
     * @return 是否启用缓存
     */
    public boolean isEnableCache() {
        return enableCache;
    }

    /**
     * 设置是否启用缓存
     * @param enableCache 是否启用缓存
     */
    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    /**
     * 获取返回结果数量限制
     * @return 返回结果数量限制
     */
    public int getLimit() {
        return limit;
    }

    /**
     * 设置返回结果数量限制
     * @param limit 返回结果数量限制
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "RecommendationConfig{" +
                "dimensionWeights=" + dimensionWeights +
                ", enableCache=" + enableCache +
                ", limit=" + limit +
                '}';
    }
}
