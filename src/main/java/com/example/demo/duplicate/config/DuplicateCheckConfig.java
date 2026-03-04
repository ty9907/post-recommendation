package com.example.demo.duplicate.config;

/**
 * 查重检测配置类
 * 用于配置查重检测的各项参数
 */
public class DuplicateCheckConfig {
    private double threshold;       // 相似度阈值，超过此值认为存在重复
    private int recentDays;         // 检测最近多少天的文章
    private String algorithmType;   // 算法类型
    private int maxResults;         // 最大返回结果数
    private boolean enableCache;    // 是否启用缓存
    private double sensitivity;     // 检测敏感度

    /**
     * 默认构造器
     */
    public DuplicateCheckConfig() {
    }

    /**
     * 带参数的构造器
     * @param threshold 相似度阈值
     * @param recentDays 检测最近多少天的文章
     * @param algorithmType 算法类型
     * @param maxResults 最大返回结果数
     * @param enableCache 是否启用缓存
     * @param sensitivity 检测敏感度
     */
    public DuplicateCheckConfig(double threshold, int recentDays, String algorithmType, 
                                int maxResults, boolean enableCache, double sensitivity) {
        this.threshold = threshold;
        this.recentDays = recentDays;
        this.algorithmType = algorithmType;
        this.maxResults = maxResults;
        this.enableCache = enableCache;
        this.sensitivity = sensitivity;
    }

    /**
     * 获取默认配置
     * @return 默认配置实例
     */
    public static DuplicateCheckConfig defaultConfig() {
        return new DuplicateCheckConfig(
            0.7,        // 默认阈值70%
            30,         // 检测最近30天
            "SIMHASH",  // 默认使用SimHash算法
            10,         // 最多返回10个结果
            true,       // 默认启用缓存
            0.5         // 默认敏感度
        );
    }

    /**
     * 获取严格配置
     * @return 严格配置实例
     */
    public static DuplicateCheckConfig strictConfig() {
        return new DuplicateCheckConfig(
            0.5,        // 严格阈值50%
            90,         // 检测最近90天
            "COMBINED", // 使用组合算法
            20,         // 最多返回20个结果
            true,       // 启用缓存
            0.8         // 高敏感度
        );
    }

    /**
     * 获取相似度阈值
     * @return 相似度阈值
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * 设置相似度阈值
     * @param threshold 相似度阈值
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    /**
     * 获取检测最近天数
     * @return 检测最近天数
     */
    public int getRecentDays() {
        return recentDays;
    }

    /**
     * 设置检测最近天数
     * @param recentDays 检测最近天数
     */
    public void setRecentDays(int recentDays) {
        this.recentDays = recentDays;
    }

    /**
     * 获取算法类型
     * @return 算法类型
     */
    public String getAlgorithmType() {
        return algorithmType;
    }

    /**
     * 设置算法类型
     * @param algorithmType 算法类型
     */
    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }

    /**
     * 获取最大返回结果数
     * @return 最大返回结果数
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * 设置最大返回结果数
     * @param maxResults 最大返回结果数
     */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * 获取是否启用缓存
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
     * 获取检测敏感度
     * @return 检测敏感度
     */
    public double getSensitivity() {
        return sensitivity;
    }

    /**
     * 设置检测敏感度
     * @param sensitivity 检测敏感度
     */
    public void setSensitivity(double sensitivity) {
        this.sensitivity = sensitivity;
    }

    @Override
    public String toString() {
        return "DuplicateCheckConfig{" +
                "threshold=" + threshold +
                ", recentDays=" + recentDays +
                ", algorithmType='" + algorithmType + '\'' +
                ", maxResults=" + maxResults +
                ", enableCache=" + enableCache +
                ", sensitivity=" + sensitivity +
                '}';
    }
}
