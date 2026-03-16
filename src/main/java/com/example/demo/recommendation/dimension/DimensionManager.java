package com.example.demo.recommendation.dimension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 维度管理器
 * 用于管理推荐系统中的各个维度，包括注册、移除、权重配置等功能
 */
public class DimensionManager {

    private final ConcurrentHashMap<String, RecommendationDimension> dimensions;         // 维度映射
    private final ConcurrentHashMap<String, DimensionWeightConfig> weightConfigs;        // 权重配置映射

    /**
     * 默认构造器
     */
    public DimensionManager() {
        this.dimensions = new ConcurrentHashMap<>();
        this.weightConfigs = new ConcurrentHashMap<>();
    }

    /**
     * 注册维度
     * @param name 维度名称
     * @param dimension 维度实例
     * @param weight 维度权重
     */
    public void registerDimension(String name, RecommendationDimension dimension, double weight) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("维度名称不能为空");
        }
        if (dimension == null) {
            throw new IllegalArgumentException("维度实例不能为空");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("权重不能为负数");
        }
        dimensions.put(name, dimension);
        weightConfigs.put(name, new DimensionWeightConfig(name, weight, true));
    }

    /**
     * 移除维度
     * @param name 维度名称
     * @return 被移除的维度实例，如果不存在则返回null
     */
    public RecommendationDimension removeDimension(String name) {
        weightConfigs.remove(name);
        return dimensions.remove(name);
    }

    /**
     * 获取所有维度名称
     * @return 维度名称列表
     */
    public List<String> getDimensionNames() {
        return new ArrayList<>(dimensions.keySet());
    }

    /**
     * 获取所有维度实例
     * @return 维度实例列表
     */
    public List<RecommendationDimension> getDimensions() {
        return new ArrayList<>(dimensions.values());
    }

    /**
     * 获取维度实例
     * @param name 维度名称
     * @return 维度实例，如果不存在则返回null
     */
    public RecommendationDimension getDimension(String name) {
        return dimensions.get(name);
    }

    /**
     * 动态设置维度权重
     * @param name 维度名称
     * @param weight 新的权重值
     */
    public void setDimensionWeight(String name, double weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("权重不能为负数");
        }
        DimensionWeightConfig config = weightConfigs.get(name);
        if (config != null) {
            config.setWeight(weight);
        } else {
            throw new IllegalArgumentException("维度不存在: " + name);
        }
    }

    /**
     * 获取维度权重
     * @param name 维度名称
     * @return 权重值，如果维度不存在则返回0
     */
    public double getDimensionWeight(String name) {
        DimensionWeightConfig config = weightConfigs.get(name);
        return config != null ? config.getWeight() : 0.0;
    }

    /**
     * 获取维度权重配置
     * @param name 维度名称
     * @return 权重配置，如果不存在则返回null
     */
    public DimensionWeightConfig getWeightConfig(String name) {
        return weightConfigs.get(name);
    }

    /**
     * 获取所有权重配置
     * @return 权重配置映射
     */
    public Map<String, DimensionWeightConfig> getAllWeightConfigs() {
        return new ConcurrentHashMap<>(weightConfigs);
    }

    /**
     * 设置维度启用状态
     * @param name 维度名称
     * @param enabled 是否启用
     */
    public void setDimensionEnabled(String name, boolean enabled) {
        DimensionWeightConfig config = weightConfigs.get(name);
        if (config != null) {
            config.setEnabled(enabled);
        }
    }

    /**
     * 判断维度是否启用
     * @param name 维度名称
     * @return 是否启用
     */
    public boolean isDimensionEnabled(String name) {
        DimensionWeightConfig config = weightConfigs.get(name);
        return config != null && config.isEnabled();
    }

    /**
     * 归一化权重
     * 将所有启用的维度权重归一化，使其总和为1
     */
    public void normalizeWeights() {
        double totalWeight = 0.0;
        for (Map.Entry<String, DimensionWeightConfig> entry : weightConfigs.entrySet()) {
            if (entry.getValue().isEnabled()) {
                totalWeight += entry.getValue().getWeight();
            }
        }
        if (totalWeight > 0) {
            for (DimensionWeightConfig config : weightConfigs.values()) {
                if (config.isEnabled()) {
                    config.setWeight(config.getWeight() / totalWeight);
                }
            }
        }
    }

    /**
     * 判断维度是否存在
     * @param name 维度名称
     * @return 是否存在
     */
    public boolean containsDimension(String name) {
        return dimensions.containsKey(name);
    }

    /**
     * 获取维度数量
     * @return 维度数量
     */
    public int getDimensionCount() {
        return dimensions.size();
    }

    /**
     * 清空所有维度
     */
    public void clear() {
        dimensions.clear();
        weightConfigs.clear();
    }
}
