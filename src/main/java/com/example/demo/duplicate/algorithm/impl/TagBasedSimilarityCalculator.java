package com.example.demo.duplicate.algorithm.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.SimilarityResult;
import com.example.demo.duplicate.util.SimilarityUtils;
import com.example.demo.tag.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于标签的相似度计算器
 * 
 * 使用Jaccard算法计算文章标签之间的相似度，支持标签权重加权计算。
 * 根据文章长度动态调整相似度阈值，实现自适应的重复检测。
 * 
 * 核心功能：
 * 1. Jaccard相似度计算：基于标签集合的交集和并集
 * 2. 标签权重加权：考虑标签重要性进行加权计算
 * 3. 共享标签识别：识别并统计两篇文章的共享标签
 * 4. 自适应阈值：根据文章长度动态调整判定阈值
 * 
 * 自适应阈值策略：
 * - 短文章（<500字）：较低阈值（2个共享标签）
 * - 中等文章（500-2000字）：中等阈值（3个共享标签）
 * - 长文章（>2000字）：较高阈值（5个共享标签）
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
public class TagBasedSimilarityCalculator implements SimilarityCalculator {

    private static final Logger logger = LoggerFactory.getLogger(TagBasedSimilarityCalculator.class);

    private static final String ALGORITHM_NAME = "TagBasedSimilarity";

    private static final int SHORT_ARTICLE_THRESHOLD = 500;
    private static final int MEDIUM_ARTICLE_THRESHOLD = 2000;
    private static final int SHORT_ARTICLE_SHARED_TAGS = 2;
    private static final int MEDIUM_ARTICLE_SHARED_TAGS = 3;
    private static final int LONG_ARTICLE_SHARED_TAGS = 5;

    /**
     * 计算两篇文章之间的相似度
     * 
     * 使用Jaccard算法计算标签相似度，并支持权重加权。
     * 
     * @param article1 第一篇文章
     * @param article2 第二篇文章
     * @return 相似度值，范围[0, 1]
     */
    @Override
    public double calculateSimilarity(Article article1, Article article2) {
        if (article1 == null || article2 == null) {
            logger.warn("文章对象为空，返回相似度0.0");
            return 0.0;
        }

        Set<Tag> sharedTags = getSharedTags(article1, article2);
        
        if (sharedTags.isEmpty()) {
            logger.debug("文章[{}]和[{}]没有共享标签，相似度为0.0", 
                    article1.getId(), article2.getId());
            return 0.0;
        }

        Set<String> tagNames1 = extractTagNames(article1);
        Set<String> tagNames2 = extractTagNames(article2);

        double jaccardSimilarity = SimilarityUtils.calculateJaccard(tagNames1, tagNames2);
        double weightedSimilarity = calculateWeightedSimilarity(article1, article2, sharedTags);
        
        double finalSimilarity = (jaccardSimilarity + weightedSimilarity) / 2.0;
        
        logger.debug("文章[{}]和[{}]相似度计算完成 - Jaccard: {}, 加权: {}, 最终: {}", 
                article1.getId(), article2.getId(), 
                String.format("%.4f", jaccardSimilarity),
                String.format("%.4f", weightedSimilarity),
                String.format("%.4f", finalSimilarity));
        
        return finalSimilarity;
    }

    /**
     * 批量计算一篇文章与多篇文章的相似度
     * 
     * @param article 目标文章
     * @param articles 待比较的文章列表
     * @return 相似度结果列表
     */
    @Override
    public List<SimilarityResult> calculateSimilarities(Article article, List<Article> articles) {
        List<SimilarityResult> results = new ArrayList<>();
        
        if (article == null || articles == null || articles.isEmpty()) {
            logger.warn("输入参数无效，返回空结果列表");
            return results;
        }

        logger.info("开始批量计算文章[{}]与{}篇文章的相似度", article.getId(), articles.size());

        for (Article comparedArticle : articles) {
            if (comparedArticle == null || 
                (article.getId() != null && article.getId().equals(comparedArticle.getId()))) {
                continue;
            }

            double similarity = calculateSimilarity(article, comparedArticle);
            
            Set<Tag> sharedTags = getSharedTags(article, comparedArticle);
            int adaptiveThreshold = calculateAdaptiveThreshold(article, comparedArticle);
            
            Map<String, Object> details = new HashMap<>();
            details.put("sharedTagsCount", sharedTags.size());
            details.put("adaptiveThreshold", adaptiveThreshold);
            details.put("isPotentiallyDuplicate", sharedTags.size() >= adaptiveThreshold);
            details.put("sharedTagNames", sharedTags.stream().map(Tag::getName).toList());

            SimilarityResult result = new SimilarityResult(
                    article.getId(),
                    comparedArticle.getId(),
                    similarity,
                    ALGORITHM_NAME,
                    LocalDateTime.now(),
                    details
            );
            
            results.add(result);
        }

        results.sort((r1, r2) -> Double.compare(r2.getSimilarity(), r1.getSimilarity()));
        
        logger.info("批量相似度计算完成，共{}个结果", results.size());
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
     * 计算自适应标签阈值
     * 
     * 根据两篇文章的长度动态调整判定重复所需的共享标签数量阈值。
     * 较短的文章使用较低的阈值，较长的文章使用较高的阈值。
     * 
     * @param article1 第一篇文章
     * @param article2 第二篇文章
     * @return 自适应阈值（共享标签数量）
     */
    public int calculateAdaptiveThreshold(Article article1, Article article2) {
        if (article1 == null || article2 == null) {
            return MEDIUM_ARTICLE_SHARED_TAGS;
        }

        int length1 = getArticleLength(article1);
        int length2 = getArticleLength(article2);
        int avgLength = (length1 + length2) / 2;

        int threshold;
        if (avgLength < SHORT_ARTICLE_THRESHOLD) {
            threshold = SHORT_ARTICLE_SHARED_TAGS;
            logger.debug("短文章（平均长度: {}），使用低阈值: {}", avgLength, threshold);
        } else if (avgLength < MEDIUM_ARTICLE_THRESHOLD) {
            threshold = MEDIUM_ARTICLE_SHARED_TAGS;
            logger.debug("中等文章（平均长度: {}），使用中等阈值: {}", avgLength, threshold);
        } else {
            threshold = LONG_ARTICLE_SHARED_TAGS;
            logger.debug("长文章（平均长度: {}），使用高阈值: {}", avgLength, threshold);
        }

        return threshold;
    }

    /**
     * 获取两篇文章的共享标签集合
     * 
     * @param article1 第一篇文章
     * @param article2 第二篇文章
     * @return 共享标签集合
     */
    public Set<Tag> getSharedTags(Article article1, Article article2) {
        Set<Tag> sharedTags = new HashSet<>();
        
        if (article1 == null || article2 == null) {
            return sharedTags;
        }

        List<Tag> tags1 = article1.getTags();
        List<Tag> tags2 = article2.getTags();

        if (tags1 == null || tags2 == null || tags1.isEmpty() || tags2.isEmpty()) {
            return sharedTags;
        }

        Map<String, Tag> tagMap1 = new HashMap<>();
        for (Tag tag : tags1) {
            if (tag != null && tag.getName() != null) {
                tagMap1.put(tag.getName(), tag);
            }
        }

        for (Tag tag : tags2) {
            if (tag != null && tag.getName() != null && tagMap1.containsKey(tag.getName())) {
                sharedTags.add(tagMap1.get(tag.getName()));
            }
        }

        logger.debug("文章[{}]和[{}]共有{}个共享标签", 
                article1.getId(), article2.getId(), sharedTags.size());
        
        return sharedTags;
    }

    /**
     * 提取文章的标签名称集合
     * 
     * @param article 文章对象
     * @return 标签名称集合
     */
    private Set<String> extractTagNames(Article article) {
        Set<String> tagNames = new HashSet<>();
        
        if (article == null || article.getTags() == null) {
            return tagNames;
        }

        for (Tag tag : article.getTags()) {
            if (tag != null && tag.getName() != null) {
                tagNames.add(tag.getName());
            }
        }

        return tagNames;
    }

    /**
     * 计算加权相似度
     * 
     * 考虑标签权重，计算加权后的相似度值。
     * 权重较高的共享标签对相似度贡献更大。
     * 
     * @param article1 第一篇文章
     * @param article2 第二篇文章
     * @param sharedTags 共享标签集合
     * @return 加权相似度
     */
    private double calculateWeightedSimilarity(Article article1, Article article2, Set<Tag> sharedTags) {
        if (sharedTags.isEmpty()) {
            return 0.0;
        }

        double sharedWeight = 0.0;
        double totalWeight1 = 0.0;
        double totalWeight2 = 0.0;

        for (Tag tag : sharedTags) {
            sharedWeight += tag.getWeight();
        }

        if (article1.getTags() != null) {
            for (Tag tag : article1.getTags()) {
                if (tag != null) {
                    totalWeight1 += tag.getWeight();
                }
            }
        }

        if (article2.getTags() != null) {
            for (Tag tag : article2.getTags()) {
                if (tag != null) {
                    totalWeight2 += tag.getWeight();
                }
            }
        }

        if (totalWeight1 == 0.0 || totalWeight2 == 0.0) {
            return 0.0;
        }

        double similarity1 = sharedWeight / totalWeight1;
        double similarity2 = sharedWeight / totalWeight2;

        return (similarity1 + similarity2) / 2.0;
    }

    /**
     * 获取文章内容长度
     * 
     * @param article 文章对象
     * @return 内容长度（字符数）
     */
    private int getArticleLength(Article article) {
        if (article == null || article.getContent() == null) {
            return 0;
        }
        return article.getContent().length();
    }
}
