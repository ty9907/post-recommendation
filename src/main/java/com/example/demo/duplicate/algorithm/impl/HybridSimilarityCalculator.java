package com.example.demo.duplicate.algorithm.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.SimilarityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 混合相似度计算器
 * 
 * 融合标签相似度和文本相似度，通过可配置的权重机制实现更准确的文章相似度检测。
 * 
 * 核心功能：
 * 1. 多维度相似度融合：结合标签和文本两种相似度计算方法
 * 2. 可配置权重机制：支持动态调整标签和文本的权重比例
 * 3. 多维度评分输出：提供详细的相似度计算过程信息
 * 4. 灵活的构造方式：支持自定义计算器或使用默认计算器
 * 
 * 默认权重配置：
 * - 标签相似度权重：0.4
 * - 文本相似度权重：0.6
 * 
 * 权重约束：
 * - 权重总和必须为 1.0
 * - 每个权重值必须在 [0.0, 1.0] 范围内
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
public class HybridSimilarityCalculator implements SimilarityCalculator {

    private static final Logger logger = LoggerFactory.getLogger(HybridSimilarityCalculator.class);

    private static final String ALGORITHM_NAME = "HybridSimilarity";

    private static final double DEFAULT_TAG_WEIGHT = 0.4;
    private static final double DEFAULT_TEXT_WEIGHT = 0.6;

    private double tagWeight;
    private double textWeight;

    private final SimilarityCalculator tagCalculator;
    private final SimilarityCalculator textCalculator;

    /**
     * 默认构造函数
     * 
     * 使用 TagBasedSimilarityCalculator 作为标签计算器，
     * TFIDFSimilarityCalculator 作为文本计算器，
     * 默认权重：标签 0.4，文本 0.6
     */
    public HybridSimilarityCalculator() {
        this.tagCalculator = new TagBasedSimilarityCalculator();
        this.textCalculator = new TFIDFSimilarityCalculator();
        this.tagWeight = DEFAULT_TAG_WEIGHT;
        this.textWeight = DEFAULT_TEXT_WEIGHT;
        
        logger.info("创建混合相似度计算器（默认配置）- 标签权重: {}, 文本权重: {}", 
                tagWeight, textWeight);
    }

    /**
     * 带参数的构造函数
     * 
     * 允许自定义标签计算器和文本计算器，使用默认权重配置。
     * 
     * @param tagCalculator 标签相似度计算器
     * @param textCalculator 文本相似度计算器
     */
    public HybridSimilarityCalculator(SimilarityCalculator tagCalculator, 
                                      SimilarityCalculator textCalculator) {
        if (tagCalculator == null || textCalculator == null) {
            throw new IllegalArgumentException("标签计算器和文本计算器不能为空");
        }
        
        this.tagCalculator = tagCalculator;
        this.textCalculator = textCalculator;
        this.tagWeight = DEFAULT_TAG_WEIGHT;
        this.textWeight = DEFAULT_TEXT_WEIGHT;
        
        logger.info("创建混合相似度计算器（自定义计算器）- 标签计算器: {}, 文本计算器: {}, 标签权重: {}, 文本权重: {}", 
                tagCalculator.getName(), textCalculator.getName(), tagWeight, textWeight);
    }

    /**
     * 计算两篇文章之间的混合相似度
     * 
     * 分别计算标签相似度和文本相似度，然后按权重融合得到最终相似度。
     * 
     * @param article1 第一篇文章
     * @param article2 第二篇文章
     * @return 混合相似度值，范围[0, 1]
     */
    @Override
    public double calculateSimilarity(Article article1, Article article2) {
        if (article1 == null || article2 == null) {
            logger.warn("文章对象为空，返回相似度0.0");
            return 0.0;
        }

        double tagSimilarity = tagCalculator.calculateSimilarity(article1, article2);
        double textSimilarity = textCalculator.calculateSimilarity(article1, article2);

        double hybridSimilarity = tagSimilarity * tagWeight + textSimilarity * textWeight;

        logger.debug("文章[{}]和[{}]混合相似度计算完成 - 标签相似度: {}, 文本相似度: {}, 混合相似度: {}", 
                article1.getId(), article2.getId(),
                String.format("%.4f", tagSimilarity),
                String.format("%.4f", textSimilarity),
                String.format("%.4f", hybridSimilarity));

        return hybridSimilarity;
    }

    /**
     * 批量计算一篇文章与多篇文章的混合相似度
     * 
     * @param article 目标文章
     * @param articles 待比较的文章列表
     * @return 相似度结果列表，包含详细的相似度信息
     */
    @Override
    public List<SimilarityResult> calculateSimilarities(Article article, List<Article> articles) {
        List<SimilarityResult> results = new ArrayList<>();

        if (article == null || articles == null || articles.isEmpty()) {
            logger.warn("输入参数无效，返回空结果列表");
            return results;
        }

        logger.info("开始批量计算文章[{}]与{}篇文章的混合相似度", article.getId(), articles.size());

        for (Article comparedArticle : articles) {
            if (comparedArticle == null || 
                (article.getId() != null && article.getId().equals(comparedArticle.getId()))) {
                continue;
            }

            double tagSimilarity = tagCalculator.calculateSimilarity(article, comparedArticle);
            double textSimilarity = textCalculator.calculateSimilarity(article, comparedArticle);
            double hybridSimilarity = tagSimilarity * tagWeight + textSimilarity * textWeight;

            Map<String, Object> details = new HashMap<>();
            details.put("tagSimilarity", tagSimilarity);
            details.put("textSimilarity", textSimilarity);
            details.put("hybridSimilarity", hybridSimilarity);
            details.put("tagWeight", tagWeight);
            details.put("textWeight", textWeight);
            details.put("tagAlgorithm", tagCalculator.getName());
            details.put("textAlgorithm", textCalculator.getName());
            details.put("similarityLevel", getSimilarityLevel(hybridSimilarity));

            SimilarityResult result = new SimilarityResult(
                    article.getId(),
                    comparedArticle.getId(),
                    hybridSimilarity,
                    ALGORITHM_NAME,
                    LocalDateTime.now(),
                    details
            );

            results.add(result);

            logger.debug("文章[{}]与[{}]相似度 - 标签: {}, 文本: {}, 混合: {}", 
                    article.getId(), comparedArticle.getId(),
                    String.format("%.4f", tagSimilarity),
                    String.format("%.4f", textSimilarity),
                    String.format("%.4f", hybridSimilarity));
        }

        results.sort((r1, r2) -> Double.compare(r2.getSimilarity(), r1.getSimilarity()));

        logger.info("批量混合相似度计算完成，共{}个结果", results.size());
        return results;
    }

    /**
     * 获取算法名称
     * 
     * @return 算法名称
     */
    @Override
    public String getName() {
        return ALGORITHM_NAME;
    }

    /**
     * 设置权重配置
     * 
     * 动态调整标签相似度和文本相似度的权重比例。
     * 权重总和必须为 1.0，且每个权重值必须在 [0.0, 1.0] 范围内。
     * 
     * @param tagWeight 标签相似度权重
     * @param textWeight 文本相似度权重
     * @throws IllegalArgumentException 当权重不满足约束条件时抛出
     */
    public void setWeights(double tagWeight, double textWeight) {
        validateWeights(tagWeight, textWeight);

        this.tagWeight = tagWeight;
        this.textWeight = textWeight;

        logger.info("权重配置已更新 - 标签权重: {}, 文本权重: {}", tagWeight, textWeight);
    }

    /**
     * 获取当前标签权重
     * 
     * @return 标签权重
     */
    public double getTagWeight() {
        return tagWeight;
    }

    /**
     * 获取当前文本权重
     * 
     * @return 文本权重
     */
    public double getTextWeight() {
        return textWeight;
    }

    /**
     * 获取标签计算器
     * 
     * @return 标签相似度计算器
     */
    public SimilarityCalculator getTagCalculator() {
        return tagCalculator;
    }

    /**
     * 获取文本计算器
     * 
     * @return 文本相似度计算器
     */
    public SimilarityCalculator getTextCalculator() {
        return textCalculator;
    }

    /**
     * 验证权重参数的有效性
     * 
     * @param tagWeight 标签权重
     * @param textWeight 文本权重
     * @throws IllegalArgumentException 当权重不满足约束条件时抛出
     */
    private void validateWeights(double tagWeight, double textWeight) {
        if (tagWeight < 0.0 || tagWeight > 1.0) {
            throw new IllegalArgumentException(
                    String.format("标签权重必须在[0.0, 1.0]范围内，当前值: %.2f", tagWeight));
        }

        if (textWeight < 0.0 || textWeight > 1.0) {
            throw new IllegalArgumentException(
                    String.format("文本权重必须在[0.0, 1.0]范围内，当前值: %.2f", textWeight));
        }

        double sum = tagWeight + textWeight;
        if (Math.abs(sum - 1.0) > 0.0001) {
            throw new IllegalArgumentException(
                    String.format("权重总和必须为1.0，当前总和: %.4f", sum));
        }
    }

    /**
     * 获取相似度等级描述
     * 
     * @param similarity 相似度值
     * @return 相似度等级描述
     */
    private String getSimilarityLevel(double similarity) {
        if (similarity >= 0.8) {
            return "高度相似";
        } else if (similarity >= 0.6) {
            return "中度相似";
        } else if (similarity >= 0.4) {
            return "轻度相似";
        } else if (similarity >= 0.2) {
            return "微弱相似";
        } else {
            return "不相似";
        }
    }
}
