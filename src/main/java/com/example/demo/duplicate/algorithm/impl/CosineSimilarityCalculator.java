package com.example.demo.duplicate.algorithm.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.SimilarityResult;
import com.example.demo.duplicate.util.SimilarityUtils;
import com.example.demo.duplicate.util.TextPreprocessor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 余弦相似度计算器
 * 
 * 使用纯余弦相似度算法计算文本相似度，基于词频向量进行计算。
 * 
 * 算法流程：
 * 1. 文本预处理（去HTML、分词、去停用词）
 * 2. 构建词频向量
 * 3. 使用余弦相似度公式计算相似度
 * 
 * 余弦相似度公式：
 * cosine(A, B) = (A · B) / (|A| × |B|)
 * 
 * 特点：
 * - 计算简单，效率高
 * - 不考虑词的重要性差异
 * - 适合短文本相似度计算
 * - 取值范围：[0, 1]
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
public class CosineSimilarityCalculator implements SimilarityCalculator {

    private static final String ALGORITHM_NAME = "COSINE";

    private final TextPreprocessor preprocessor;

    public CosineSimilarityCalculator() {
        this.preprocessor = new TextPreprocessor();
    }

    @Override
    public double calculateSimilarity(Article article1, Article article2) {
        if (article1 == null || article2 == null) {
            return 0.0;
        }

        String text1 = getArticleText(article1);
        String text2 = getArticleText(article2);

        if (text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }

        Map<String, Double> vector1 = buildTermFrequencyVector(text1);
        Map<String, Double> vector2 = buildTermFrequencyVector(text2);

        return SimilarityUtils.calculateCosineSimilarity(vector1, vector2);
    }

    @Override
    public List<SimilarityResult> calculateSimilarities(Article article, List<Article> articles) {
        List<SimilarityResult> results = new ArrayList<>();

        if (article == null || articles == null || articles.isEmpty()) {
            return results;
        }

        String text = getArticleText(article);
        Map<String, Double> articleVector = buildTermFrequencyVector(text);

        for (Article comparedArticle : articles) {
            if (comparedArticle == null || comparedArticle.getId() == null) {
                continue;
            }

            if (article.getId() != null && article.getId().equals(comparedArticle.getId())) {
                continue;
            }

            double similarity = calculateSimilarityWithVector(articleVector, comparedArticle);

            SimilarityResult result = new SimilarityResult(
                    article.getId(),
                    comparedArticle.getId(),
                    similarity,
                    ALGORITHM_NAME,
                    LocalDateTime.now(),
                    createDetails(comparedArticle, similarity)
            );
            results.add(result);
        }

        results.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));

        return results;
    }

    @Override
    public String getName() {
        return ALGORITHM_NAME;
    }

    private String getArticleText(Article article) {
        StringBuilder sb = new StringBuilder();
        
        if (article.getTitle() != null) {
            sb.append(article.getTitle()).append(" ");
        }
        
        if (article.getContent() != null) {
            sb.append(article.getContent());
        }
        
        return sb.toString();
    }

    private Map<String, Double> buildTermFrequencyVector(String text) {
        List<String> tokens = preprocessor.preprocessAndTokenize(text);

        if (tokens.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Integer> termCount = new HashMap<>();
        for (String token : tokens) {
            termCount.merge(token, 1, Integer::sum);
        }

        int totalTerms = tokens.size();
        Map<String, Double> termFrequency = new HashMap<>();

        for (Map.Entry<String, Integer> entry : termCount.entrySet()) {
            double frequency = (double) entry.getValue() / totalTerms;
            termFrequency.put(entry.getKey(), frequency);
        }

        return termFrequency;
    }

    private double calculateSimilarityWithVector(Map<String, Double> articleVector, Article comparedArticle) {
        String comparedText = getArticleText(comparedArticle);
        Map<String, Double> comparedVector = buildTermFrequencyVector(comparedText);

        return SimilarityUtils.calculateCosineSimilarity(articleVector, comparedVector);
    }

    private Map<String, Object> createDetails(Article comparedArticle, double similarity) {
        Map<String, Object> details = new HashMap<>();
        details.put("comparedTitle", comparedArticle.getTitle());
        details.put("similarityLevel", getSimilarityLevel(similarity));
        return details;
    }

    private String getSimilarityLevel(double similarity) {
        if (similarity >= 0.8) {
            return "高度相似";
        } else if (similarity >= 0.6) {
            return "中度相似";
        } else if (similarity >= 0.3) {
            return "轻度相似";
        } else {
            return "不相似";
        }
    }
}
