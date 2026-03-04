package com.example.demo.duplicate.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 相似度计算工具类
 * 
 * 提供多种相似度和距离计算方法，用于文本相似度分析。
 * 包含Jaccard相似度、余弦相似度、欧几里得距离等常用算法。
 * 
 * 核心功能：
 * 1. Jaccard相似度：基于集合的相似度计算
 * 2. 余弦相似度：基于向量的相似度计算
 * 3. 欧几里得距离：基于向量的距离计算
 * 4. 向量操作：归一化、点积、模长计算
 * 
 * 使用示例：
 * <pre>
 * // 计算Jaccard相似度
 * Set&lt;String&gt; set1 = Set.of("Java", "编程", "语言");
 * Set&lt;String&gt; set2 = Set.of("Java", "开发", "语言");
 * double jaccard = SimilarityUtils.calculateJaccard(set1, set2);
 * 
 * // 计算余弦相似度
 * Map&lt;String, Double&gt; vec1 = Map.of("Java", 0.5, "编程", 0.3);
 * Map&lt;String, Double&gt; vec2 = Map.of("Java", 0.4, "开发", 0.4);
 * double cosine = SimilarityUtils.calculateCosineSimilarity(vec1, vec2);
 * </pre>
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
public final class SimilarityUtils {

    private SimilarityUtils() {
    }

    /**
     * 计算两个集合的Jaccard相似度
     * 
     * Jaccard相似度 = 交集大小 / 并集大小
     * 取值范围：[0, 1]，值越大表示越相似
     * 
     * @param set1 第一个集合
     * @param set2 第二个集合
     * @return Jaccard相似度，如果两个集合都为空则返回0.0
     */
    public static double calculateJaccard(Set<String> set1, Set<String> set2) {
        if (set1 == null || set2 == null) {
            return 0.0;
        }
        
        if (set1.isEmpty() && set2.isEmpty()) {
            return 0.0;
        }
        
        int intersectionSize = 0;
        for (String item : set1) {
            if (set2.contains(item)) {
                intersectionSize++;
            }
        }
        
        int unionSize = set1.size() + set2.size() - intersectionSize;
        
        if (unionSize == 0) {
            return 0.0;
        }
        
        return (double) intersectionSize / unionSize;
    }

    /**
     * 计算两个向量的余弦相似度
     * 
     * 余弦相似度 = (A · B) / (|A| * |B|)
     * 取值范围：[-1, 1]，值越大表示越相似
     * 
     * @param vec1 第一个向量（词到权重的映射）
     * @param vec2 第二个向量（词到权重的映射）
     * @return 余弦相似度，如果任一向量为空或模长为0则返回0.0
     */
    public static double calculateCosineSimilarity(Map<String, Double> vec1, Map<String, Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.isEmpty() || vec2.isEmpty()) {
            return 0.0;
        }
        
        double dotProduct = dotProduct(vec1, vec2);
        double magnitude1 = magnitude(vec1);
        double magnitude2 = magnitude(vec2);
        
        if (magnitude1 == 0.0 || magnitude2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (magnitude1 * magnitude2);
    }

    /**
     * 计算两个向量的欧几里得距离
     * 
     * 欧几里得距离 = sqrt(Σ(xi - yi)²)
     * 取值范围：[0, +∞)，值越小表示越相似
     * 
     * @param vec1 第一个向量（词到权重的映射）
     * @param vec2 第二个向量（词到权重的映射）
     * @return 欧几里得距离，如果两个向量都为空则返回0.0
     */
    public static double calculateEuclideanDistance(Map<String, Double> vec1, Map<String, Double> vec2) {
        if (vec1 == null || vec2 == null) {
            return 0.0;
        }
        
        Map<String, Double> allKeys = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : vec1.entrySet()) {
            allKeys.put(entry.getKey(), entry.getValue());
        }
        
        for (Map.Entry<String, Double> entry : vec2.entrySet()) {
            if (!allKeys.containsKey(entry.getKey())) {
                allKeys.put(entry.getKey(), 0.0);
            }
        }
        
        double sumSquared = 0.0;
        for (String key : allKeys.keySet()) {
            double val1 = vec1.getOrDefault(key, 0.0);
            double val2 = vec2.getOrDefault(key, 0.0);
            double diff = val1 - val2;
            sumSquared += diff * diff;
        }
        
        return Math.sqrt(sumSquared);
    }

    /**
     * 归一化向量（L2归一化）
     * 
     * 将向量转换为单位向量，使其模长为1
     * 归一化后的向量 = 原向量 / 向量模长
     * 
     * @param vector 原始向量（词到权重的映射）
     * @return 归一化后的向量，如果原向量为空或模长为0则返回空Map
     */
    public static Map<String, Double> normalizeVector(Map<String, Double> vector) {
        if (vector == null || vector.isEmpty()) {
            return new HashMap<>();
        }
        
        double mag = magnitude(vector);
        
        if (mag == 0.0) {
            return new HashMap<>();
        }
        
        Map<String, Double> normalized = new HashMap<>();
        for (Map.Entry<String, Double> entry : vector.entrySet()) {
            normalized.put(entry.getKey(), entry.getValue() / mag);
        }
        
        return normalized;
    }

    /**
     * 计算两个向量的点积（内积）
     * 
     * 点积 = Σ(xi * yi)
     * 只计算两个向量共有的键
     * 
     * @param vec1 第一个向量（词到权重的映射）
     * @param vec2 第二个向量（词到权重的映射）
     * @return 点积值，如果任一向量为空则返回0.0
     */
    public static double dotProduct(Map<String, Double> vec1, Map<String, Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.isEmpty() || vec2.isEmpty()) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        
        Map<String, Double> smaller = vec1.size() <= vec2.size() ? vec1 : vec2;
        Map<String, Double> larger = vec1.size() > vec2.size() ? vec1 : vec2;
        
        for (Map.Entry<String, Double> entry : smaller.entrySet()) {
            String key = entry.getKey();
            if (larger.containsKey(key)) {
                dotProduct += entry.getValue() * larger.get(key);
            }
        }
        
        return dotProduct;
    }

    /**
     * 计算向量的模长（L2范数）
     * 
     * 模长 = sqrt(Σxi²)
     * 
     * @param vector 向量（词到权重的映射）
     * @return 向量模长，如果向量为空则返回0.0
     */
    public static double magnitude(Map<String, Double> vector) {
        if (vector == null || vector.isEmpty()) {
            return 0.0;
        }
        
        double sumSquared = 0.0;
        for (Double value : vector.values()) {
            sumSquared += value * value;
        }
        
        return Math.sqrt(sumSquared);
    }
}
