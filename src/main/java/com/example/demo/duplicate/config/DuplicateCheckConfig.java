package com.example.demo.duplicate.config;

import com.example.demo.duplicate.algorithm.SimilarityCalculatorFactory;

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
    private int simHashHammingThreshold; // SimHash海明距离阈值
    private int maxCandidateSize;        // 候选集最大大小
    private double highRiskThreshold;    // 高风险阈值
    private double mediumRiskThreshold;  // 中风险阈值
    private int asyncQueueSize;          // 异步检测队列大小
    private int asyncWorkerCount;        // 异步检测工作线程数
    private int candidateCacheExpireMinutes; // 候选集缓存过期时间
    private boolean enableLayeredDetection;  // 是否启用分层检测
    private boolean enableAsyncDetection;    // 是否启用异步精检
    private boolean enableFullScanFallback;  // 分层筛选为空时是否回退全量扫描
    private String preciseAlgorithmType;     // 异步精检算法
    private String indexPersistencePath;     // 索引持久化路径

    /**
     * 默认构造器
     */
    public DuplicateCheckConfig() {
        applyOptimizationDefaults();
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
        applyOptimizationDefaults();
    }

    /**
     * 完整配置构造器
     *
     * @param threshold 相似度阈值
     * @param recentDays 检测最近多少天的文章
     * @param algorithmType 快速检测算法类型
     * @param maxResults 最大返回结果数
     * @param enableCache 是否启用缓存
     * @param sensitivity 检测敏感度
     * @param simHashHammingThreshold SimHash海明距离阈值
     * @param maxCandidateSize 候选集最大大小
     * @param highRiskThreshold 高风险阈值
     * @param mediumRiskThreshold 中风险阈值
     * @param asyncQueueSize 异步检测队列大小
     * @param asyncWorkerCount 异步检测工作线程数
     * @param candidateCacheExpireMinutes 候选集缓存过期时间（分钟）
     * @param enableLayeredDetection 是否启用分层检测
     * @param enableAsyncDetection 是否启用异步精检
     * @param enableFullScanFallback 分层筛选为空时是否回退全量扫描
     * @param preciseAlgorithmType 异步精检算法
     * @param indexPersistencePath 索引持久化路径
     */
    public DuplicateCheckConfig(double threshold, int recentDays, String algorithmType,
                                int maxResults, boolean enableCache, double sensitivity,
                                int simHashHammingThreshold, int maxCandidateSize,
                                double highRiskThreshold, double mediumRiskThreshold,
                                int asyncQueueSize, int asyncWorkerCount,
                                int candidateCacheExpireMinutes, boolean enableLayeredDetection,
                                boolean enableAsyncDetection, boolean enableFullScanFallback,
                                String preciseAlgorithmType, String indexPersistencePath) {
        this.threshold = threshold;
        this.recentDays = recentDays;
        this.algorithmType = algorithmType;
        this.maxResults = maxResults;
        this.enableCache = enableCache;
        this.sensitivity = sensitivity;
        this.simHashHammingThreshold = simHashHammingThreshold;
        this.maxCandidateSize = maxCandidateSize;
        this.highRiskThreshold = highRiskThreshold;
        this.mediumRiskThreshold = mediumRiskThreshold;
        this.asyncQueueSize = asyncQueueSize;
        this.asyncWorkerCount = asyncWorkerCount;
        this.candidateCacheExpireMinutes = candidateCacheExpireMinutes;
        this.enableLayeredDetection = enableLayeredDetection;
        this.enableAsyncDetection = enableAsyncDetection;
        this.enableFullScanFallback = enableFullScanFallback;
        this.preciseAlgorithmType = preciseAlgorithmType;
        this.indexPersistencePath = indexPersistencePath;
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
            "HYBRID",   // 使用混合算法
            20,         // 最多返回20个结果
            true,       // 启用缓存
            0.8         // 高敏感度
        );
    }

    private void applyOptimizationDefaults() {
        this.simHashHammingThreshold = 3;
        this.maxCandidateSize = 50;
        this.highRiskThreshold = 0.7;
        this.mediumRiskThreshold = 0.4;
        this.asyncQueueSize = 256;
        this.asyncWorkerCount = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        this.candidateCacheExpireMinutes = 15;
        this.enableLayeredDetection = true;
        this.enableAsyncDetection = true;
        this.enableFullScanFallback = true;
        this.preciseAlgorithmType = SimilarityCalculatorFactory.HYBRID;
        this.indexPersistencePath = null;
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

    public int getSimHashHammingThreshold() {
        return simHashHammingThreshold;
    }

    public void setSimHashHammingThreshold(int simHashHammingThreshold) {
        this.simHashHammingThreshold = simHashHammingThreshold;
    }

    public int getMaxCandidateSize() {
        return maxCandidateSize;
    }

    public void setMaxCandidateSize(int maxCandidateSize) {
        this.maxCandidateSize = maxCandidateSize;
    }

    public double getHighRiskThreshold() {
        return highRiskThreshold;
    }

    public void setHighRiskThreshold(double highRiskThreshold) {
        this.highRiskThreshold = highRiskThreshold;
    }

    public double getMediumRiskThreshold() {
        return mediumRiskThreshold;
    }

    public void setMediumRiskThreshold(double mediumRiskThreshold) {
        this.mediumRiskThreshold = mediumRiskThreshold;
    }

    public int getAsyncQueueSize() {
        return asyncQueueSize;
    }

    public void setAsyncQueueSize(int asyncQueueSize) {
        this.asyncQueueSize = asyncQueueSize;
    }

    public int getAsyncWorkerCount() {
        return asyncWorkerCount;
    }

    public void setAsyncWorkerCount(int asyncWorkerCount) {
        this.asyncWorkerCount = asyncWorkerCount;
    }

    public int getCandidateCacheExpireMinutes() {
        return candidateCacheExpireMinutes;
    }

    public void setCandidateCacheExpireMinutes(int candidateCacheExpireMinutes) {
        this.candidateCacheExpireMinutes = candidateCacheExpireMinutes;
    }

    public boolean isEnableLayeredDetection() {
        return enableLayeredDetection;
    }

    public void setEnableLayeredDetection(boolean enableLayeredDetection) {
        this.enableLayeredDetection = enableLayeredDetection;
    }

    public boolean isEnableAsyncDetection() {
        return enableAsyncDetection;
    }

    public void setEnableAsyncDetection(boolean enableAsyncDetection) {
        this.enableAsyncDetection = enableAsyncDetection;
    }

    public boolean isEnableFullScanFallback() {
        return enableFullScanFallback;
    }

    public void setEnableFullScanFallback(boolean enableFullScanFallback) {
        this.enableFullScanFallback = enableFullScanFallback;
    }

    public String getPreciseAlgorithmType() {
        return preciseAlgorithmType;
    }

    public void setPreciseAlgorithmType(String preciseAlgorithmType) {
        this.preciseAlgorithmType = preciseAlgorithmType;
    }

    public String getIndexPersistencePath() {
        return indexPersistencePath;
    }

    public void setIndexPersistencePath(String indexPersistencePath) {
        this.indexPersistencePath = indexPersistencePath;
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
                ", simHashHammingThreshold=" + simHashHammingThreshold +
                ", maxCandidateSize=" + maxCandidateSize +
                ", highRiskThreshold=" + highRiskThreshold +
                ", mediumRiskThreshold=" + mediumRiskThreshold +
                ", asyncQueueSize=" + asyncQueueSize +
                ", asyncWorkerCount=" + asyncWorkerCount +
                ", candidateCacheExpireMinutes=" + candidateCacheExpireMinutes +
                ", enableLayeredDetection=" + enableLayeredDetection +
                ", enableAsyncDetection=" + enableAsyncDetection +
                ", enableFullScanFallback=" + enableFullScanFallback +
                ", preciseAlgorithmType='" + preciseAlgorithmType + '\'' +
                ", indexPersistencePath='" + indexPersistencePath + '\'' +
                '}';
    }
}
