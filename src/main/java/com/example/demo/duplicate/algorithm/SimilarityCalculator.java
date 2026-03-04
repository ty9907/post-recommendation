package com.example.demo.duplicate.algorithm;

import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.SimilarityResult;
import java.util.List;

/**
 * 相似度计算器接口
 * 
 * 定义文章相似度计算的标准方法，支持多种相似度算法实现。
 * 不同的实现类可以基于标签、文本内容或混合策略计算相似度。
 * 
 * 核心功能：
 * 1. 计算两篇文章之间的相似度
 * 2. 批量计算一篇文章与多篇文章的相似度
 * 3. 获取算法名称用于标识和日志记录
 * 
 * 实现类：
 * - TagBasedSimilarityCalculator：基于标签的相似度计算
 * - TextBasedSimilarityCalculator：基于文本内容的相似度计算
 * - HybridSimilarityCalculator：混合相似度计算
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
public interface SimilarityCalculator {

    /**
     * 计算两篇文章之间的相似度
     * 
     * @param article1 第一篇文章
     * @param article2 第二篇文章
     * @return 相似度值，范围[0, 1]，值越大表示越相似
     */
    double calculateSimilarity(Article article1, Article article2);

    /**
     * 批量计算一篇文章与多篇文章的相似度
     * 
     * @param article 目标文章
     * @param articles 待比较的文章列表
     * @return 相似度结果列表，包含每篇文章的相似度信息
     */
    List<SimilarityResult> calculateSimilarities(Article article, List<Article> articles);

    /**
     * 获取算法名称
     * 
     * @return 算法名称，用于标识和日志记录
     */
    String getName();
}
