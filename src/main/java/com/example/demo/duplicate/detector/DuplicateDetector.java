package com.example.demo.duplicate.detector;

import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.DuplicateCheckReport;
import com.example.demo.duplicate.model.SimilarityResult;

import java.util.List;

/**
 * 重复检测器接口
 * 
 * 定义文章重复检测的标准方法，支持多种检测策略。
 * 不同的实现类可以基于不同的检测逻辑和算法。
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
public interface DuplicateDetector {

    /**
     * 检测单篇文章是否重复
     * 
     * @param article 待检测的文章
     * @param config 检测配置
     * @return 检测报告
     */
    DuplicateCheckReport detect(Article article, DuplicateCheckConfig config);

    /**
     * 批量检测文章是否重复
     * 
     * @param articles 待检测的文章列表
     * @param config 检测配置
     * @return 检测报告列表
     */
    List<DuplicateCheckReport> batchDetect(List<Article> articles, DuplicateCheckConfig config);

    /**
     * 查找与指定文章相似的文章
     * 
     * @param article 待检测的文章
     * @param config 检测配置
     * @return 相似度结果列表
     */
    List<SimilarityResult> findSimilarArticles(Article article, DuplicateCheckConfig config);

    /**
     * 判断两篇文章是否重复
     * 
     * @param article1 第一篇文章
     * @param article2 第二篇文章
     * @param threshold 相似度阈值
     * @return true表示重复，false表示不重复
     */
    boolean isDuplicate(Article article1, Article article2, double threshold);

    /**
     * 获取检测器名称
     * 
     * @return 检测器名称
     */
    String getName();
}
