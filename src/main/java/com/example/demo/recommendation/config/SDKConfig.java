package com.example.demo.recommendation.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * SDK配置类
 * 用于存储SDK的各项配置参数
 */
public class SDKConfig {

    private final Map<String, Double> dimensionWeights;
    private final boolean enableCache;
    private final int defaultLimit;
    private final int threadPoolSize;

    /**
     * 私有构造器，通过Builder创建实例
     * @param builder 配置构建器
     */
    private SDKConfig(Builder builder) {
        this.dimensionWeights = Collections.unmodifiableMap(new HashMap<>(builder.dimensionWeights));
        this.enableCache = builder.enableCache;
        this.defaultLimit = builder.defaultLimit;
        this.threadPoolSize = builder.threadPoolSize;
    }

    /**
     * 创建默认配置
     * @return 默认配置实例
     */
    public static SDKConfig defaultConfig() {
        return builder().build();
    }

    /**
     * 创建配置构建器
     * @return 构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 获取维度权重配置
     * @return 维度权重映射，不可修改
     */
    public Map<String, Double> getDimensionWeights() {
        return dimensionWeights;
    }

    /**
     * 是否启用缓存
     * @return 是否启用缓存
     */
    public boolean isEnableCache() {
        return enableCache;
    }

    /**
     * 获取默认返回结果数量限制
     * @return 默认限制数量
     */
    public int getDefaultLimit() {
        return defaultLimit;
    }

    /**
     * 获取线程池大小
     * @return 线程池大小
     */
    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    @Override
    public String toString() {
        return "SDKConfig{" +
                "dimensionWeights=" + dimensionWeights +
                ", enableCache=" + enableCache +
                ", defaultLimit=" + defaultLimit +
                ", threadPoolSize=" + threadPoolSize +
                '}';
    }

    /**
     * SDK配置构建器
     * 使用Builder模式构建SDKConfig实例
     */
    public static class Builder {

        private Map<String, Double> dimensionWeights = new HashMap<>();
        private boolean enableCache = true;
        private int defaultLimit = 10;
        private int threadPoolSize = Runtime.getRuntime().availableProcessors();

        /**
         * 默认构造器
         */
        public Builder() {
        }

        /**
         * 从现有配置创建构建器
         * @param config 现有配置
         */
        public Builder(SDKConfig config) {
            if (config != null) {
                this.dimensionWeights = new HashMap<>(config.dimensionWeights);
                this.enableCache = config.enableCache;
                this.defaultLimit = config.defaultLimit;
                this.threadPoolSize = config.threadPoolSize;
            }
        }

        /**
         * 添加维度权重
         * @param name 维度名称
         * @param weight 权重值
         * @return 构建器实例
         */
        public Builder addDimensionWeight(String name, double weight) {
            if (name != null && !name.trim().isEmpty() && weight >= 0) {
                dimensionWeights.put(name, weight);
            }
            return this;
        }

        /**
         * 设置维度权重映射
         * @param weights 维度权重映射
         * @return 构建器实例
         */
        public Builder dimensionWeights(Map<String, Double> weights) {
            if (weights != null) {
                this.dimensionWeights = new HashMap<>(weights);
            }
            return this;
        }

        /**
         * 设置是否启用缓存
         * @param enable 是否启用
         * @return 构建器实例
         */
        public Builder enableCache(boolean enable) {
            this.enableCache = enable;
            return this;
        }

        /**
         * 设置默认返回结果数量限制
         * @param limit 限制数量
         * @return 构建器实例
         */
        public Builder defaultLimit(int limit) {
            if (limit >= 0) {
                this.defaultLimit = limit;
            }
            return this;
        }

        /**
         * 设置线程池大小
         * @param size 线程池大小
         * @return 构建器实例
         */
        public Builder threadPoolSize(int size) {
            if (size > 0) {
                this.threadPoolSize = size;
            }
            return this;
        }

        /**
         * 构建SDKConfig实例
         * @return SDKConfig实例
         */
        public SDKConfig build() {
            return new SDKConfig(this);
        }
    }
}
