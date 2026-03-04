package com.example.demo.duplicate.algorithm.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.SimilarityResult;
import com.example.demo.duplicate.util.TextPreprocessor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 编辑距离相似度计算器
 * 
 * 使用编辑距离（Levenshtein Distance）算法计算文本相似度。
 * 编辑距离是指将一个字符串转换为另一个字符串所需的最少编辑操作次数。
 * 
 * 算法流程：
 * 1. 文本预处理（去HTML、分词、去停用词）
 * 2. 将词列表转换为字符串
 * 3. 计算编辑距离
 * 4. 将编辑距离转换为相似度分数
 * 
 * 相似度转换公式：
 * similarity = 1 - (editDistance / maxLength)
 * 
 * 特点：
 * - 能够检测字符级别的相似度
 * - 适合检测抄袭、改写等情况
 * - 计算复杂度较高：O(m*n)
 * - 对文本长度敏感
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
public class EditDistanceSimilarityCalculator implements SimilarityCalculator {

    private static final String ALGORITHM_NAME = "EDIT_DISTANCE";

    private final TextPreprocessor preprocessor;

    public EditDistanceSimilarityCalculator() {
        this.preprocessor = new TextPreprocessor();
    }

    @Override
    public double calculateSimilarity(Article article1, Article article2) {
        if (article1 == null || article2 == null) {
            return 0.0;
        }

        String text1 = getArticleText(article1);
        String text2 = getArticleText(article2);

        if (text1.isEmpty() && text2.isEmpty()) {
            return 1.0;
        }

        if (text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }

        String processedText1 = processText(text1);
        String processedText2 = processText(text2);

        return calculateEditDistanceSimilarity(processedText1, processedText2);
    }

    @Override
    public List<SimilarityResult> calculateSimilarities(Article article, List<Article> articles) {
        List<SimilarityResult> results = new ArrayList<>();

        if (article == null || articles == null || articles.isEmpty()) {
            return results;
        }

        String text = getArticleText(article);
        String processedText = processText(text);

        for (Article comparedArticle : articles) {
            if (comparedArticle == null || comparedArticle.getId() == null) {
                continue;
            }

            if (article.getId() != null && article.getId().equals(comparedArticle.getId())) {
                continue;
            }

            double similarity = calculateSimilarityWithText(processedText, comparedArticle);

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

    private String processText(String text) {
        List<String> tokens = preprocessor.preprocessAndTokenize(text);
        return String.join("", tokens);
    }

    private double calculateEditDistanceSimilarity(String text1, String text2) {
        if (text1.isEmpty() && text2.isEmpty()) {
            return 1.0;
        }

        if (text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }

        int editDistance = calculateLevenshteinDistance(text1, text2);
        int maxLength = Math.max(text1.length(), text2.length());

        return 1.0 - ((double) editDistance / maxLength);
    }

    private int calculateLevenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(
                            Math.min(dp[i - 1][j], dp[i][j - 1]),
                            dp[i - 1][j - 1]
                    ) + 1;
                }
            }
        }

        return dp[len1][len2];
    }

    private double calculateSimilarityWithText(String processedText, Article comparedArticle) {
        String comparedText = getArticleText(comparedArticle);
        String processedComparedText = processText(comparedText);

        return calculateEditDistanceSimilarity(processedText, processedComparedText);
    }

    private Map<String, Object> createDetails(Article comparedArticle, double similarity) {
        return Map.of(
                "comparedTitle", comparedArticle.getTitle() != null ? comparedArticle.getTitle() : "",
                "similarityLevel", getSimilarityLevel(similarity)
        );
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
