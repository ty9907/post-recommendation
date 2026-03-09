package com.example.demo.duplicate;

import com.example.demo.duplicate.algorithm.impl.CosineSimilarityCalculator;
import com.example.demo.duplicate.algorithm.impl.SimHashSimilarityCalculator;
import com.example.demo.duplicate.algorithm.impl.TFIDFSimilarityCalculator;
import com.example.demo.duplicate.algorithm.impl.Word2VecSimilarityCalculator;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.SimilarityResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 重复检测资源文件测试类
 * 测试使用新增资源文件进行重复内容检测的功能
 */
@DisplayName("重复检测资源文件测试")
public class DuplicateDetectionResourceTest {

    private static final Logger logger = LoggerFactory.getLogger(DuplicateDetectionResourceTest.class);
    
    private SimHashSimilarityCalculator simHashCalculator;
    private TFIDFSimilarityCalculator tfidfCalculator;
    private CosineSimilarityCalculator cosineCalculator;
    private Word2VecSimilarityCalculator word2VecCalculator;

    @BeforeEach
    void setUp() {
        simHashCalculator = new SimHashSimilarityCalculator();
        tfidfCalculator = new TFIDFSimilarityCalculator();
        cosineCalculator = new CosineSimilarityCalculator();
        word2VecCalculator = new Word2VecSimilarityCalculator();
    }

    private String loadResourceContent(String resourcePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Resource file not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + resourcePath, e);
        }
    }

    private Article createArticleFromResource(Long id, String title, String resourcePath) {
        String content = loadResourceContent(resourcePath);
        return new Article(id, title, content);
    }

    @Test
    @DisplayName("测试RESTful API文章相似度检测 - 中度相似")
    void testRestfulApiSimilarity_MediumSimilarity() {
        Article original = createArticleFromResource(1L, "RESTful API设计", 
                "duplicate-detection/restful-api-design.txt");
        Article similar = createArticleFromResource(2L, "RESTful API设计(相似)", 
                "duplicate-detection/restful-api-design-similar.txt");
        
        double simHashSimilarity = simHashCalculator.calculateSimilarity(original, similar);
        double tfidfSimilarity = tfidfCalculator.calculateSimilarity(original, similar);
        double cosineSimilarity = cosineCalculator.calculateSimilarity(original, similar);
        
        logger.info("=== RESTful API文章相似度 ===");
        logger.info("SimHash相似度: {}", String.format("%.4f", simHashSimilarity));
        logger.info("TF-IDF相似度: {}", String.format("%.4f", tfidfSimilarity));
        logger.info("余弦相似度: {}", String.format("%.4f", cosineSimilarity));
        
        assertTrue(simHashSimilarity > 0.5, "SimHash相似度应大于0.5");
        assertTrue(tfidfSimilarity > 0.5, "TF-IDF相似度应大于0.5");
        assertTrue(cosineSimilarity > 0.5, "余弦相似度应大于0.5");
        assertTrue(tfidfSimilarity < 0.9, "深度改写后相似度应降低");
    }

    @Test
    @DisplayName("测试Redis文章相似度检测 - 中度相似")
    void testRedisSimilarity_MediumSimilarity() {
        Article original = createArticleFromResource(1L, "Redis缓存指南", 
                "duplicate-detection/redis-cache-guide.txt");
        Article similar = createArticleFromResource(2L, "Redis缓存指南(相似)", 
                "duplicate-detection/redis-cache-guide-similar.txt");
        
        double simHashSimilarity = simHashCalculator.calculateSimilarity(original, similar);
        double tfidfSimilarity = tfidfCalculator.calculateSimilarity(original, similar);
        double cosineSimilarity = cosineCalculator.calculateSimilarity(original, similar);
        
        logger.info("=== Redis文章相似度 ===");
        logger.info("SimHash相似度: {}", String.format("%.4f", simHashSimilarity));
        logger.info("TF-IDF相似度: {}", String.format("%.4f", tfidfSimilarity));
        logger.info("余弦相似度: {}", String.format("%.4f", cosineSimilarity));
        
        assertTrue(simHashSimilarity > 0.5, "SimHash相似度应大于0.5");
        assertTrue(tfidfSimilarity > 0.5, "TF-IDF相似度应大于0.5");
        assertTrue(cosineSimilarity > 0.5, "余弦相似度应大于0.5");
        assertTrue(tfidfSimilarity < 0.9, "深度改写后相似度应降低");
    }

    @Test
    @DisplayName("测试消息队列文章相似度检测 - 中度相似")
    void testMessageQueueSimilarity_MediumSimilarity() {
        Article original = createArticleFromResource(1L, "消息队列介绍", 
                "duplicate-detection/message-queue-intro.txt");
        Article similar = createArticleFromResource(2L, "消息队列介绍(相似)", 
                "duplicate-detection/message-queue-intro-similar.txt");
        
        double simHashSimilarity = simHashCalculator.calculateSimilarity(original, similar);
        double tfidfSimilarity = tfidfCalculator.calculateSimilarity(original, similar);
        double cosineSimilarity = cosineCalculator.calculateSimilarity(original, similar);
        
        logger.info("=== 消息队列文章相似度 ===");
        logger.info("SimHash相似度: {}", String.format("%.4f", simHashSimilarity));
        logger.info("TF-IDF相似度: {}", String.format("%.4f", tfidfSimilarity));
        logger.info("余弦相似度: {}", String.format("%.4f", cosineSimilarity));
        
        assertTrue(simHashSimilarity > 0.5, "SimHash相似度应大于0.5");
        assertTrue(tfidfSimilarity > 0.5, "TF-IDF相似度应大于0.5");
        assertTrue(cosineSimilarity > 0.5, "余弦相似度应大于0.5");
        assertTrue(tfidfSimilarity < 0.9, "深度改写后相似度应降低");
    }

    @Test
    @DisplayName("测试不同主题文章相似度检测 - 低相似度")
    void testDifferentTopics_LowSimilarity() {
        Article restfulApi = createArticleFromResource(1L, "RESTful API", 
                "duplicate-detection/restful-api-design.txt");
        Article redis = createArticleFromResource(2L, "Redis缓存", 
                "duplicate-detection/redis-cache-guide.txt");
        Article messageQueue = createArticleFromResource(3L, "消息队列", 
                "duplicate-detection/message-queue-intro.txt");
        
        double similarity1 = tfidfCalculator.calculateSimilarity(restfulApi, redis);
        double similarity2 = tfidfCalculator.calculateSimilarity(restfulApi, messageQueue);
        double similarity3 = tfidfCalculator.calculateSimilarity(redis, messageQueue);
        
        logger.info("=== 不同主题文章相似度 ===");
        logger.info("RESTful API vs Redis: {}", String.format("%.4f", similarity1));
        logger.info("RESTful API vs 消息队列: {}", String.format("%.4f", similarity2));
        logger.info("Redis vs 消息队列: {}", String.format("%.4f", similarity3));
        
        assertTrue(similarity1 < 0.5, "不同主题文章相似度应较低");
        assertTrue(similarity2 < 0.5, "不同主题文章相似度应较低");
        assertTrue(similarity3 < 0.5, "不同主题文章相似度应较低");
    }

    @Test
    @DisplayName("测试批量相似度检测")
    void testBatchSimilarityDetection() {
        Article target = createArticleFromResource(1L, "RESTful API设计", 
                "duplicate-detection/restful-api-design.txt");
        
        List<Article> candidates = new ArrayList<>();
        candidates.add(createArticleFromResource(2L, "RESTful API(相似)", 
                "duplicate-detection/restful-api-design-similar.txt"));
        candidates.add(createArticleFromResource(3L, "Redis缓存", 
                "duplicate-detection/redis-cache-guide.txt"));
        candidates.add(createArticleFromResource(4L, "消息队列", 
                "duplicate-detection/message-queue-intro.txt"));
        
        List<SimilarityResult> results = tfidfCalculator.calculateSimilarities(target, candidates);
        
        assertNotNull(results, "结果列表不应为空");
        assertEquals(3, results.size(), "应返回3个结果");
        
        assertTrue(results.get(0).getSimilarity() >= results.get(1).getSimilarity(),
                "结果应按相似度降序排列");
        
        logger.info("=== 批量相似度检测结果 ===");
        for (SimilarityResult result : results) {
            logger.info("文章ID: {}, 相似度: {}", 
                    result.getComparedArticleId(), 
                    String.format("%.4f", result.getSimilarity()));
        }
        
        assertTrue(results.get(0).getSimilarity() > 0.5, 
                "最相似的文章应具有中度相似度");
    }

    @Test
    @DisplayName("测试多种算法结果对比")
    void testMultipleAlgorithmsComparison() {
        Article original = createArticleFromResource(1L, "Redis缓存指南", 
                "duplicate-detection/redis-cache-guide.txt");
        Article similar = createArticleFromResource(2L, "Redis缓存指南(相似)", 
                "duplicate-detection/redis-cache-guide-similar.txt");
        
        double simHash = simHashCalculator.calculateSimilarity(original, similar);
        double tfidf = tfidfCalculator.calculateSimilarity(original, similar);
        double cosine = cosineCalculator.calculateSimilarity(original, similar);
        
        logger.info("=== 多算法结果对比 ===");
        logger.info("SimHash: {}", String.format("%.4f", simHash));
        logger.info("TF-IDF: {}", String.format("%.4f", tfidf));
        logger.info("Cosine: {}", String.format("%.4f", cosine));
        
        double avgSimilarity = (simHash + tfidf + cosine) / 3;
        logger.info("平均相似度: {}", String.format("%.4f", avgSimilarity));
        
        assertTrue(avgSimilarity > 0.5, "平均相似度应大于0.5");
        assertTrue(avgSimilarity < 0.9, "深度改写后平均相似度应降低");
    }

    @Test
    @DisplayName("测试重复检测阈值判断")
    void testDuplicateThresholdJudgment() {
        double suspiciousThreshold = 0.5;
        
        Article original = createArticleFromResource(1L, "消息队列", 
                "duplicate-detection/message-queue-intro.txt");
        Article similar = createArticleFromResource(2L, "消息队列(相似)", 
                "duplicate-detection/message-queue-intro-similar.txt");
        Article different = createArticleFromResource(3L, "Redis", 
                "duplicate-detection/redis-cache-guide.txt");
        
        double similarity1 = tfidfCalculator.calculateSimilarity(original, similar);
        double similarity2 = tfidfCalculator.calculateSimilarity(original, different);
        
        logger.info("=== 重复检测阈值判断 ===");
        logger.info("相似文章相似度: {}", 
                String.format("%.4f", similarity1));
        logger.info("不同文章相似度: {}", 
                String.format("%.4f", similarity2));
        
        assertTrue(similarity1 > suspiciousThreshold, "相似文章相似度应大于0.5");
        assertTrue(similarity2 < suspiciousThreshold, "不同文章相似度应较低");
    }

    @Test
    @DisplayName("测试长文本相似度计算性能")
    void testLongTextSimilarityPerformance() {
        Article original = createArticleFromResource(1L, "RESTful API", 
                "duplicate-detection/restful-api-design.txt");
        Article similar = createArticleFromResource(2L, "RESTful API(相似)", 
                "duplicate-detection/restful-api-design-similar.txt");
        
        long startTime = System.currentTimeMillis();
        double similarity = tfidfCalculator.calculateSimilarity(original, similar);
        long endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        
        logger.info("=== 长文本相似度计算性能 ===");
        logger.info("文本长度: {} 字符", original.getContent().length());
        logger.info("计算时间: {} ms", duration);
        logger.info("相似度: {}", String.format("%.4f", similarity));
        
        assertTrue(duration < 10000, "相似度计算应在10秒内完成");
    }

    @Test
    @DisplayName("测试所有重复检测资源文件")
    void testAllDuplicateDetectionResources() {
        String[][] resourcePairs = {
            {"duplicate-detection/restful-api-design.txt", "duplicate-detection/restful-api-design-similar.txt"},
            {"duplicate-detection/redis-cache-guide.txt", "duplicate-detection/redis-cache-guide-similar.txt"},
            {"duplicate-detection/message-queue-intro.txt", "duplicate-detection/message-queue-intro-similar.txt"}
        };
        
        for (String[] pair : resourcePairs) {
            Article original = createArticleFromResource(1L, "原文", pair[0]);
            Article similar = createArticleFromResource(2L, "相似文", pair[1]);
            
            double similarity = tfidfCalculator.calculateSimilarity(original, similar);
            
            logger.info("=== {} ===", pair[0]);
            logger.info("原文长度: {} 字符", original.getContent().length());
            logger.info("相似文长度: {} 字符", similar.getContent().length());
            logger.info("相似度: {}", String.format("%.4f", similarity));
            
            assertTrue(similarity > 0.5, pair[0] + " 相似度应大于0.5");
            assertTrue(similarity < 0.9, pair[0] + " 深度改写后相似度应降低");
        }
    }

    @Test
    @DisplayName("测试相似度结果详情")
    void testSimilarityResultDetails() {
        Article original = createArticleFromResource(1L, "RESTful API", 
                "duplicate-detection/restful-api-design.txt");
        Article similar = createArticleFromResource(2L, "RESTful API(相似)", 
                "duplicate-detection/restful-api-design-similar.txt");
        
        List<SimilarityResult> results = tfidfCalculator.calculateSimilarities(original, List.of(similar));
        
        assertEquals(1, results.size(), "应返回1个结果");
        
        SimilarityResult result = results.get(0);
        assertNotNull(result.getDetails(), "详情不应为空");
        assertNotNull(result.getAlgorithm(), "算法名称不应为空");
        assertNotNull(result.getCheckTime(), "检测时间不应为空");
        
        logger.info("=== 相似度结果详情 ===");
        logger.info("算法: {}", result.getAlgorithm());
        logger.info("相似度: {}", String.format("%.4f", result.getSimilarity()));
        logger.info("检测时间: {}", result.getCheckTime());
        logger.info("详情: {}", result.getDetails());
    }

    @Test
    @DisplayName("测试相同内容相似度")
    void testSameContentSimilarity() {
        Article article = createArticleFromResource(1L, "Redis缓存", 
                "duplicate-detection/redis-cache-guide.txt");
        
        double simHash = simHashCalculator.calculateSimilarity(article, article);
        double tfidf = tfidfCalculator.calculateSimilarity(article, article);
        double cosine = cosineCalculator.calculateSimilarity(article, article);
        
        logger.info("=== 相同内容相似度 ===");
        logger.info("SimHash: {}", String.format("%.4f", simHash));
        logger.info("TF-IDF: {}", String.format("%.4f", tfidf));
        logger.info("Cosine: {}", String.format("%.4f", cosine));
        
        assertEquals(1.0, simHash, 0.001, "相同内容SimHash相似度应为1.0");
        assertEquals(1.0, tfidf, 0.001, "相同内容TF-IDF相似度应为1.0");
        assertEquals(1.0, cosine, 0.001, "相同内容余弦相似度应为1.0");
    }

    @Test
    @DisplayName("Word2Vec - 测试相似文章语义相似度")
    void testWord2Vec_SimilarArticles() {
        Article original = createArticleFromResource(1L, "Redis缓存指南", 
                "duplicate-detection/redis-cache-guide.txt");
        Article similar = createArticleFromResource(2L, "Redis缓存指南(相似)", 
                "duplicate-detection/redis-cache-guide-similar.txt");
        
        double similarity = word2VecCalculator.calculateSimilarity(original, similar);
        
        logger.info("=== Word2Vec 相似文章语义相似度 ===");
        logger.info("相似度: {}", String.format("%.4f", similarity));
        
        assertTrue(similarity > 0.5, "相似文章语义相似度应大于0.5");
        assertTrue(similarity < 1.0, "不同文章相似度应小于1.0");
    }

    @Test
    @DisplayName("Word2Vec - 测试不同主题文章相似度")
    void testWord2Vec_DifferentTopics() {
        Article redis = createArticleFromResource(1L, "Redis缓存", 
                "duplicate-detection/redis-cache-guide.txt");
        Article messageQueue = createArticleFromResource(2L, "消息队列", 
                "duplicate-detection/message-queue-intro.txt");
        
        double similarity = word2VecCalculator.calculateSimilarity(redis, messageQueue);
        
        logger.info("=== Word2Vec 不同主题文章相似度 ===");
        logger.info("相似度: {}", String.format("%.4f", similarity));
        
        assertTrue(similarity < 0.6, "不同主题文章相似度应较低");
    }

    @Test
    @DisplayName("Word2Vec - 测试相同文章相似度")
    void testWord2Vec_SameArticle() {
        Article article = createArticleFromResource(1L, "RESTful API设计", 
                "duplicate-detection/restful-api-design.txt");
        
        double similarity = word2VecCalculator.calculateSimilarity(article, article);
        
        logger.info("=== Word2Vec 相同文章相似度 ===");
        logger.info("相似度: {}", String.format("%.4f", similarity));
        
        assertEquals(1.0, similarity, 0.001, "相同文章相似度应为1.0");
    }

    @Test
    @DisplayName("Word2Vec - 测试空文章处理")
    void testWord2Vec_EmptyArticle() {
        Article emptyArticle = new Article(1L, "", "");
        Article normalArticle = createArticleFromResource(2L, "Redis缓存", 
                "duplicate-detection/redis-cache-guide.txt");
        
        double similarity = word2VecCalculator.calculateSimilarity(emptyArticle, normalArticle);
        
        logger.info("=== Word2Vec 空文章处理 ===");
        logger.info("相似度: {}", String.format("%.4f", similarity));
        
        assertEquals(0.0, similarity, 0.001, "空文章相似度应为0");
    }

    @Test
    @DisplayName("Word2Vec - 测试null文章处理")
    void testWord2Vec_NullArticle() {
        Article normalArticle = createArticleFromResource(1L, "Redis缓存", 
                "duplicate-detection/redis-cache-guide.txt");
        
        double similarity1 = word2VecCalculator.calculateSimilarity(null, normalArticle);
        double similarity2 = word2VecCalculator.calculateSimilarity(normalArticle, null);
        double similarity3 = word2VecCalculator.calculateSimilarity(null, null);
        
        logger.info("=== Word2Vec null文章处理 ===");
        logger.info("null vs 正常: {}", String.format("%.4f", similarity1));
        logger.info("正常 vs null: {}", String.format("%.4f", similarity2));
        logger.info("null vs null: {}", String.format("%.4f", similarity3));
        
        assertEquals(0.0, similarity1, 0.001, "null文章相似度应为0");
        assertEquals(0.0, similarity2, 0.001, "null文章相似度应为0");
        assertEquals(0.0, similarity3, 0.001, "null文章相似度应为0");
    }

    @Test
    @DisplayName("Word2Vec - 测试批量相似度计算")
    void testWord2Vec_BatchCalculation() {
        Article target = createArticleFromResource(1L, "RESTful API设计", 
                "duplicate-detection/restful-api-design.txt");
        
        List<Article> candidates = new ArrayList<>();
        candidates.add(createArticleFromResource(2L, "RESTful API(相似)", 
                "duplicate-detection/restful-api-design-similar.txt"));
        candidates.add(createArticleFromResource(3L, "Redis缓存", 
                "duplicate-detection/redis-cache-guide.txt"));
        candidates.add(createArticleFromResource(4L, "消息队列", 
                "duplicate-detection/message-queue-intro.txt"));
        
        List<SimilarityResult> results = word2VecCalculator.calculateSimilarities(target, candidates);
        
        assertNotNull(results, "结果列表不应为空");
        assertEquals(3, results.size(), "应返回3个结果");
        
        logger.info("=== Word2Vec 批量相似度计算 ===");
        for (SimilarityResult result : results) {
            logger.info("文章ID: {}, 相似度: {}", 
                    result.getComparedArticleId(), 
                    String.format("%.4f", result.getSimilarity()));
        }
        
        for (SimilarityResult result : results) {
            assertNotNull(result.getAlgorithm(), "算法名称不应为空");
            assertEquals("Word2Vec", result.getAlgorithm(), "算法名称应为Word2Vec");
            assertNotNull(result.getCheckTime(), "检测时间不应为空");
        }
    }

    @Test
    @DisplayName("Word2Vec - 测试批量计算空列表处理")
    void testWord2Vec_BatchEmptyList() {
        Article target = createArticleFromResource(1L, "Redis缓存", 
                "duplicate-detection/redis-cache-guide.txt");
        
        List<SimilarityResult> results = word2VecCalculator.calculateSimilarities(target, new ArrayList<>());
        
        assertNotNull(results, "结果列表不应为空");
        assertTrue(results.isEmpty(), "空候选列表应返回空结果");
    }

    @Test
    @DisplayName("Word2Vec - 测试批量计算null列表处理")
    void testWord2Vec_BatchNullList() {
        Article target = createArticleFromResource(1L, "Redis缓存", 
                "duplicate-detection/redis-cache-guide.txt");
        
        List<SimilarityResult> results = word2VecCalculator.calculateSimilarities(target, null);
        
        assertNotNull(results, "结果列表不应为空");
        assertTrue(results.isEmpty(), "null候选列表应返回空结果");
    }

    @Test
    @DisplayName("Word2Vec - 测试获取计算器名称")
    void testWord2Vec_GetName() {
        String name = word2VecCalculator.getName();
        
        logger.info("=== Word2Vec 计算器名称 ===");
        logger.info("名称: {}", name);
        
        assertEquals("Word2Vec", name, "计算器名称应为Word2Vec");
    }

    @Test
    @DisplayName("Word2Vec - 测试模型加载状态")
    void testWord2Vec_ModelStatus() {
        boolean modelLoaded = word2VecCalculator.isModelLoaded();
        int vocabSize = word2VecCalculator.getVocabSize();
        int vectorSize = word2VecCalculator.getVectorSize();
        
        logger.info("=== Word2Vec 模型状态 ===");
        logger.info("模型已加载: {}", modelLoaded);
        logger.info("词汇表大小: {}", vocabSize);
        logger.info("向量维度: {}", vectorSize);
        
        assertFalse(modelLoaded, "默认构造器不应加载预训练模型");
        assertEquals(0, vocabSize, "无模型时词汇表大小应为0");
        assertEquals(0, vectorSize, "无模型时向量维度应为0");
    }

    @Test
    @DisplayName("Word2Vec - 测试所有资源文件相似度")
    void testWord2Vec_AllResourceFiles() {
        String[][] resourcePairs = {
            {"duplicate-detection/restful-api-design.txt", "duplicate-detection/restful-api-design-similar.txt"},
            {"duplicate-detection/redis-cache-guide.txt", "duplicate-detection/redis-cache-guide-similar.txt"},
            {"duplicate-detection/message-queue-intro.txt", "duplicate-detection/message-queue-intro-similar.txt"}
        };
        
        for (String[] pair : resourcePairs) {
            Article original = createArticleFromResource(1L, "原文", pair[0]);
            Article similar = createArticleFromResource(2L, "相似文", pair[1]);
            
            double similarity = word2VecCalculator.calculateSimilarity(original, similar);
            
            logger.info("=== Word2Vec {} ===", pair[0]);
            logger.info("相似度: {}", String.format("%.4f", similarity));
            
            assertTrue(similarity > 0.3, pair[0] + " 相似度应大于0.3");
            assertTrue(similarity < 1.0, pair[0] + " 相似度应小于1.0");
        }
    }

    @Test
    @DisplayName("Word2Vec - 测试与TF-IDF算法对比")
    void testWord2Vec_CompareWithTFIDF() {
        Article original = createArticleFromResource(1L, "Redis缓存指南", 
                "duplicate-detection/redis-cache-guide.txt");
        Article similar = createArticleFromResource(2L, "Redis缓存指南(相似)", 
                "duplicate-detection/redis-cache-guide-similar.txt");
        
        double word2VecSimilarity = word2VecCalculator.calculateSimilarity(original, similar);
        double tfidfSimilarity = tfidfCalculator.calculateSimilarity(original, similar);
        
        logger.info("=== Word2Vec vs TF-IDF 对比 ===");
        logger.info("Word2Vec相似度: {}", String.format("%.4f", word2VecSimilarity));
        logger.info("TF-IDF相似度: {}", String.format("%.4f", tfidfSimilarity));
        
        assertTrue(word2VecSimilarity > 0.3, "Word2Vec相似度应大于0.3");
        assertTrue(tfidfSimilarity > 0.3, "TF-IDF相似度应大于0.3");
    }

    @Test
    @DisplayName("Word2Vec - 测试性能")
    void testWord2Vec_Performance() {
        Article original = createArticleFromResource(1L, "RESTful API设计", 
                "duplicate-detection/restful-api-design.txt");
        Article similar = createArticleFromResource(2L, "RESTful API设计(相似)", 
                "duplicate-detection/restful-api-design-similar.txt");
        
        long startTime = System.currentTimeMillis();
        double similarity = word2VecCalculator.calculateSimilarity(original, similar);
        long endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        
        logger.info("=== Word2Vec 性能测试 ===");
        logger.info("文本长度: {} 字符", original.getContent().length());
        logger.info("计算时间: {} ms", duration);
        logger.info("相似度: {}", String.format("%.4f", similarity));
        
        assertTrue(duration < 10000, "相似度计算应在10秒内完成");
    }
}
