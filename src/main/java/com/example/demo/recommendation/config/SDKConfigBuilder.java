package com.example.demo.recommendation.config;

import java.util.HashMap;
import java.util.Map;

/**
 * SDK配置构建器
 * 提供流式API构建SDKConfig实例
 */
public class SDKConfigBuilder {

    private Map<String, Double> dimensionWeights = new HashMap<>();
    private boolean enableCache = true;
    private int defaultLimit = 10;
    private int threadPoolSize = Runtime.getRuntime().availableProcessors();

    /**
     * 默认构造器
     */
    public SDKConfigBuilder() {
    }

    /**
     * 添加维度权重
     * @param name 维度名称
     * @param weight 权重值
     * @return 构建器实例
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    public SDKConfigBuilder addDimensionWeight(String name, double weight) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("维度名称不能为空");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("权重值不能为负数: " + weight);
        }
        dimensionWeights.put(name.trim(), weight);
        return this;
    }

    /**
     * 批量设置维度权重
     * @param weights 维度权重映射
     * @return 构建器实例
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    public SDKConfigBuilder dimensionWeights(Map<String, Double> weights) {
        if (weights == null) {
            throw new IllegalArgumentException("权重映射不能为空");
        }
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            addDimensionWeight(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * 设置是否启用缓存
     * @param enable 是否启用缓存
     * @return 构建器实例
     */
    public SDKConfigBuilder enableCache(boolean enable) {
        this.enableCache = enable;
        return this;
    }

    /**
     * 设置默认返回结果数量限制
     * @param limit 限制数量，必须大于等于0
     * @return 构建器实例
     * @throws IllegalArgumentException 当limit小于0时抛出
     */
    public SDKConfigBuilder defaultLimit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("默认限制数量不能为负数: " + limit);
        }
        this.defaultLimit = limit;
        return this;
    }

    /**
     * 设置线程池大小
     * @param size 线程池大小，必须大于0
     * @return 构建器实例
     * @throws IllegalArgumentException 当size小于等于0时抛出
     */
    public SDKConfigBuilder threadPoolSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("线程池大小必须大于0: " + size);
        }
        this.threadPoolSize = size;
        return this;
    }

    /**
     * 构建SDKConfig实例
     * @return SDKConfig实例
     */
    public SDKConfig build() {
        SDKConfig.Builder builder = SDKConfig.builder();
        builder.dimensionWeights(this.dimensionWeights);
        builder.enableCache(this.enableCache);
        builder.defaultLimit(this.defaultLimit);
        builder.threadPoolSize(this.threadPoolSize);
        return builder.build();
    }

    /**
     * 重置构建器状态
     * @return 构建器实例
     */
    public SDKConfigBuilder reset() {
        this.dimensionWeights = new HashMap<>();
        this.enableCache = true;
        this.defaultLimit = 10;
        this.threadPoolSize = Runtime.getRuntime().availableProcessors();
        return this;
    }
}
