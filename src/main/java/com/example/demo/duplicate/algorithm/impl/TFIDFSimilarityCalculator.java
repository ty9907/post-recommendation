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
import java.util.concurrent.ConcurrentHashMap;

/**
 * TF-IDF相似度计算器
 * 
 * 使用TF-IDF（词频-逆文档频率）算法向量化文本，然后使用余弦相似度计算相似度。
 * 
 * 算法流程：
 * 1. 文本预处理（去HTML、分词、去停用词）
 * 2. 计算词频（TF）
 * 3. 计算逆文档频率（IDF），使用缓存优化
 * 4. 计算TF-IDF向量
 * 5. 使用余弦相似度计算相似度
 * 
 * 特点：
 * - 能够识别文章中的关键词
 * - 对常见词降权，对稀有词提权
 * - 使用IDF缓存提升性能
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
public class TFIDFSimilarityCalculator implements SimilarityCalculator {

    private static final String ALGORITHM_NAME = "TFIDF";

    private final TextPreprocessor preprocessor;

    private final Map<String, Double> idfCache;

    private final Map<Long, Map<String, Double>> tfidfCache;

    private int documentCount;

    public TFIDFSimilarityCalculator() {
        this.preprocessor = new TextPreprocessor();
        this.idfCache = new ConcurrentHashMap<>();
        this.tfidfCache = new ConcurrentHashMap<>();
        this.documentCount = 0;
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

        Map<String, Double> tfidf1 = calculateTFIDF(text1);
        Map<String, Double> tfidf2 = calculateTFIDF(text2);

        return SimilarityUtils.calculateCosineSimilarity(tfidf1, tfidf2);
    }

    @Override
    public List<SimilarityResult> calculateSimilarities(Article article, List<Article> articles) {
        List<SimilarityResult> results = new ArrayList<>();

        if (article == null || articles == null || articles.isEmpty()) {
            return results;
        }

        updateIDFCache(articles);

        String text = getArticleText(article);
        Map<String, Double> articleTFIDF = calculateTFIDF(text);

        for (Article comparedArticle : articles) {
            if (comparedArticle == null || comparedArticle.getId() == null) {
                continue;
            }

            if (article.getId() != null && article.getId().equals(comparedArticle.getId())) {
                continue;
            }

            double similarity = calculateSimilarityWithTFIDF(articleTFIDF, comparedArticle);

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

    private Map<String, Double> calculateTFIDF(String text) {
        List<String> tokens = preprocessor.preprocessAndTokenize(text);

        if (tokens.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Integer> termFrequency = new HashMap<>();
        for (String token : tokens) {
            termFrequency.merge(token, 1, Integer::sum);
        }

        int totalTerms = tokens.size();
        Map<String, Double> tfidf = new HashMap<>();

        for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
            String term = entry.getKey();
            int freq = entry.getValue();

            double tf = (double) freq / totalTerms;

            double idf = idfCache.getOrDefault(term, Math.log(1.0 + 1));

            tfidf.put(term, tf * idf);
        }

        return tfidf;
    }

    private double calculateSimilarityWithTFIDF(Map<String, Double> articleTFIDF, Article comparedArticle) {
        String comparedText = getArticleText(comparedArticle);
        Map<String, Double> comparedTFIDF = calculateTFIDF(comparedText);

        return SimilarityUtils.calculateCosineSimilarity(articleTFIDF, comparedTFIDF);
    }

    private void updateIDFCache(List<Article> articles) {
        Map<String, Integer> documentFrequency = new HashMap<>();
        int newDocumentCount = 0;

        for (Article article : articles) {
            String text = getArticleText(article);
            List<String> tokens = preprocessor.preprocessAndTokenize(text);

            if (!tokens.isEmpty()) {
                newDocumentCount++;
                Map<String, Boolean> uniqueTerms = new HashMap<>();
                
                for (String token : tokens) {
                    uniqueTerms.put(token, true);
                }
                
                for (String term : uniqueTerms.keySet()) {
                    documentFrequency.merge(term, 1, Integer::sum);
                }
            }
        }

        if (newDocumentCount > 0) {
            this.documentCount = newDocumentCount;

            for (Map.Entry<String, Integer> entry : documentFrequency.entrySet()) {
                String term = entry.getKey();
                int df = entry.getValue();

                double idf = Math.log((double) documentCount / (1 + df)) + 1.0;
                idfCache.put(term, idf);
            }
        }
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

    public void clearCache() {
        idfCache.clear();
        tfidfCache.clear();
        documentCount = 0;
    }

    public int getCacheSize() {
        return idfCache.size();
    }
}
