package com.example.demo.duplicate.algorithm.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.SimilarityResult;
import com.example.demo.duplicate.util.TextPreprocessor;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SimHash相似度计算器
 * 
 * 使用SimHash算法生成文本指纹，然后使用汉明距离计算相似度。
 * SimHash是一种局部敏感哈希（LSH）算法，适合快速检测近似重复文档。
 * 
 * 算法流程：
 * 1. 文本预处理（去HTML、分词、去停用词）
 * 2. 计算每个词的哈希值
 * 3. 加权合并所有词的哈希值
 * 4. 生成64位的SimHash指纹
 * 5. 计算汉明距离
 * 6. 将汉明距离转换为相似度
 * 
 * 相似度转换公式：
 * similarity = 1 - (hammingDistance / 64)
 * 
 * 特点：
 * - 计算速度快，适合大规模文档去重
 * - 对小范围修改不敏感
 * - 支持快速指纹比对
 * - 汉明距离阈值通常设为3
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
public class SimHashSimilarityCalculator implements SimilarityCalculator {

    private static final String ALGORITHM_NAME = "SIMHASH";

    private static final int HASH_BITS = 64;

    private static final int HAMMING_THRESHOLD = 3;

    private final TextPreprocessor preprocessor;

    public SimHashSimilarityCalculator() {
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

        BigInteger simHash1 = calculateSimHash(text1);
        BigInteger simHash2 = calculateSimHash(text2);

        return calculateSimilarityFromHash(simHash1, simHash2);
    }

    @Override
    public List<SimilarityResult> calculateSimilarities(Article article, List<Article> articles) {
        List<SimilarityResult> results = new ArrayList<>();

        if (article == null || articles == null || articles.isEmpty()) {
            return results;
        }

        String text = getArticleText(article);
        BigInteger articleSimHash = calculateSimHash(text);

        for (Article comparedArticle : articles) {
            if (comparedArticle == null || comparedArticle.getId() == null) {
                continue;
            }

            if (article.getId() != null && article.getId().equals(comparedArticle.getId())) {
                continue;
            }

            double similarity = calculateSimilarityWithHash(articleSimHash, comparedArticle);

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

    private BigInteger calculateSimHash(String text) {
        List<String> tokens = preprocessor.preprocessAndTokenize(text);

        if (tokens.isEmpty()) {
            return BigInteger.ZERO;
        }

        Map<String, Integer> termFrequency = new HashMap<>();
        for (String token : tokens) {
            termFrequency.merge(token, 1, Integer::sum);
        }

        int[] hashBits = new int[HASH_BITS];

        for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
            String term = entry.getKey();
            int weight = entry.getValue();

            BigInteger termHash = hash(term);

            for (int i = 0; i < HASH_BITS; i++) {
                BigInteger bitMask = BigInteger.ONE.shiftLeft(i);
                if (termHash.and(bitMask).compareTo(BigInteger.ZERO) != 0) {
                    hashBits[i] += weight;
                } else {
                    hashBits[i] -= weight;
                }
            }
        }

        BigInteger fingerprint = BigInteger.ZERO;
        for (int i = 0; i < HASH_BITS; i++) {
            if (hashBits[i] > 0) {
                fingerprint = fingerprint.or(BigInteger.ONE.shiftLeft(i));
            }
        }

        return fingerprint;
    }

    private BigInteger hash(String str) {
        if (str == null || str.isEmpty()) {
            return BigInteger.ZERO;
        }

        char[] chars = str.toCharArray();
        BigInteger hash = BigInteger.valueOf(5381);

        for (char c : chars) {
            hash = hash.shiftLeft(5).add(hash).add(BigInteger.valueOf(c));
        }

        return hash.and(BigInteger.ONE.shiftLeft(HASH_BITS).subtract(BigInteger.ONE));
    }

    private int calculateHammingDistance(BigInteger hash1, BigInteger hash2) {
        BigInteger xor = hash1.xor(hash2);
        return xor.bitCount();
    }

    private double calculateSimilarityFromHash(BigInteger hash1, BigInteger hash2) {
        int hammingDistance = calculateHammingDistance(hash1, hash2);
        return 1.0 - ((double) hammingDistance / HASH_BITS);
    }

    private double calculateSimilarityWithHash(BigInteger articleSimHash, Article comparedArticle) {
        String comparedText = getArticleText(comparedArticle);
        BigInteger comparedSimHash = calculateSimHash(comparedText);

        return calculateSimilarityFromHash(articleSimHash, comparedSimHash);
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

    public boolean isNearDuplicate(BigInteger hash1, BigInteger hash2) {
        return calculateHammingDistance(hash1, hash2) <= HAMMING_THRESHOLD;
    }

    public BigInteger getSimHash(Article article) {
        String text = getArticleText(article);
        return calculateSimHash(text);
    }
}
