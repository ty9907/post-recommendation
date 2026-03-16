package com.example.demo.recommendation.dimension;

/**
 * 维度权重配置数据模型类
 * 用于配置维度的权重和启用状态
 */
public class DimensionWeightConfig {
    private String dimensionName;   // 维度名称
    private double weight;          // 权重
    private boolean enabled;        // 是否启用

    /**
     * 默认构造器
     */
    public DimensionWeightConfig() {
        this.enabled = true;
    }

    /**
     * 带参数的构造器
     * @param dimensionName 维度名称
     * @param weight 权重
     */
    public DimensionWeightConfig(String dimensionName, double weight) {
        this.dimensionName = dimensionName;
        this.weight = weight;
        this.enabled = true;
    }

    /**
     * 带参数的构造器
     * @param dimensionName 维度名称
     * @param weight 权重
     * @param enabled 是否启用
     */
    public DimensionWeightConfig(String dimensionName, double weight, boolean enabled) {
        this.dimensionName = dimensionName;
        this.weight = weight;
        this.enabled = enabled;
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
     * 获取权重
     * @return 权重
     */
    public double getWeight() {
        return weight;
    }

    /**
     * 设置权重
     * @param weight 权重
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * 是否启用
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "DimensionWeightConfig{" +
                "dimensionName='" + dimensionName + '\'' +
                ", weight=" + weight +
                ", enabled=" + enabled +
                '}';
    }
}
