package com.example.demo.recommendation.performance;

import com.example.demo.recommendation.dimension.DimensionContext;
import com.example.demo.recommendation.dimension.DimensionResult;
import com.example.demo.recommendation.dimension.impl.BrowseHistoryDimension;
import com.example.demo.recommendation.dimension.impl.TagMatchingDimension;
import com.example.demo.recommendation.model.BrowseHistory;
import com.example.demo.recommendation.model.PostTag;
import com.example.demo.recommendation.model.UserTag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 维度计算性能测试类
 * 测试各个推荐维度计算的性能指标
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DimensionPerformanceTest {

    private static final int TAG_COUNT = 1000;
    private static final int USER_TAG_COUNT = 100;
    private static final int POST_TAG_COUNT = 500;
    private static final int BROWSE_HISTORY_COUNT = 200;
    private static final int ITERATIONS = 1000;
    private static final long SINGLE_OP_THRESHOLD_MS = 10;

    private TagMatchingDimension tagMatchingDimension;
    private BrowseHistoryDimension browseHistoryDimension;
    private List<UserTag> userTags;
    private List<PostTag> postTags;
    private List<BrowseHistory> browseHistoryList;
    private Random random;

    @BeforeAll
    void setUp() {
        random = new Random(42);
        tagMatchingDimension = new TagMatchingDimension();
        browseHistoryDimension = new BrowseHistoryDimension();
        
        generateTestData();
    }

    /**
     * 生成测试数据
     */
    private void generateTestData() {
        userTags = new ArrayList<>();
        for (int i = 0; i < USER_TAG_COUNT; i++) {
            String tagName = "userTag_" + i;
            double weight = 0.3 + random.nextDouble() * 0.7;
            userTags.add(new UserTag(tagName, weight, "INTEREST"));
        }
        
        postTags = new ArrayList<>();
        for (int i = 0; i < POST_TAG_COUNT; i++) {
            String tagName = "postTag_" + (i % 50);
            double weight = 0.5 + random.nextDouble() * 0.5;
            postTags.add(new PostTag(tagName, weight, (long) i));
        }
        
        browseHistoryList = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < BROWSE_HISTORY_COUNT; i++) {
            long postId = (long) i;
            long browseTime = currentTime - random.nextInt(7 * 24 * 60 * 60 * 1000);
            
            List<PostTag> historyTags = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                String tagName = "historyTag_" + random.nextInt(50);
                double weight = 0.5 + random.nextDouble() * 0.5;
                historyTags.add(new PostTag(tagName, weight, postId));
            }
            
            browseHistoryList.add(new BrowseHistory(postId, browseTime, historyTags));
        }
    }

    @Test
    @DisplayName("标签匹配维度性能测试")
    void testTagMatchingDimensionPerformance() {
        List<Long> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < ITERATIONS; i++) {
            DimensionContext context = DimensionContext.builder()
                    .userTags(userTags)
                    .candidatePostTags(postTags)
                    .build();
            
            long startTime = System.nanoTime();
            DimensionResult result = tagMatchingDimension.calculate(context);
            long endTime = System.nanoTime();
            
            responseTimes.add((endTime - startTime) / 1_000_000);
            assertNotNull(result, "维度计算结果不应为空");
        }
        
        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        double variance = responseTimes.stream()
                .mapToDouble(t -> Math.pow(t - avgTime, 2))
                .average()
                .orElse(0);
        double stdDev = Math.sqrt(variance);
        
        System.out.println("========================================");
        System.out.println("标签匹配维度性能测试报告");
        System.out.println("========================================");
        System.out.println("测试迭代次数: " + ITERATIONS);
        System.out.println("用户标签数量: " + USER_TAG_COUNT);
        System.out.println("候选帖子标签数量: " + POST_TAG_COUNT);
        System.out.println("平均响应时间: " + String.format("%.4f", avgTime) + " ms");
        System.out.println("最大响应时间: " + maxTime + " ms");
        System.out.println("最小响应时间: " + minTime + " ms");
        System.out.println("响应时间标准差: " + String.format("%.4f", stdDev) + " ms");
        System.out.println("性能要求: < " + SINGLE_OP_THRESHOLD_MS + " ms");
        System.out.println("测试结果: " + (avgTime < SINGLE_OP_THRESHOLD_MS ? "通过" : "失败"));
        System.out.println("========================================");
        
        assertTrue(avgTime < SINGLE_OP_THRESHOLD_MS, 
                "标签匹配维度平均响应时间应小于" + SINGLE_OP_THRESHOLD_MS + "ms，实际耗时: " + String.format("%.4f", avgTime) + "ms");
    }

    @Test
    @DisplayName("浏览历史维度性能测试")
    void testBrowseHistoryDimensionPerformance() {
        List<Long> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < ITERATIONS; i++) {
            DimensionContext context = DimensionContext.builder()
                    .browseHistory(browseHistoryList)
                    .candidatePostTags(postTags)
                    .build();
            
            long startTime = System.nanoTime();
            DimensionResult result = browseHistoryDimension.calculate(context);
            long endTime = System.nanoTime();
            
            responseTimes.add((endTime - startTime) / 1_000_000);
            assertNotNull(result, "维度计算结果不应为空");
        }
        
        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        double variance = responseTimes.stream()
                .mapToDouble(t -> Math.pow(t - avgTime, 2))
                .average()
                .orElse(0);
        double stdDev = Math.sqrt(variance);
        
        System.out.println("========================================");
        System.out.println("浏览历史维度性能测试报告");
        System.out.println("========================================");
        System.out.println("测试迭代次数: " + ITERATIONS);
        System.out.println("浏览历史数量: " + BROWSE_HISTORY_COUNT);
        System.out.println("候选帖子标签数量: " + POST_TAG_COUNT);
        System.out.println("平均响应时间: " + String.format("%.4f", avgTime) + " ms");
        System.out.println("最大响应时间: " + maxTime + " ms");
        System.out.println("最小响应时间: " + minTime + " ms");
        System.out.println("响应时间标准差: " + String.format("%.4f", stdDev) + " ms");
        System.out.println("性能要求: < " + SINGLE_OP_THRESHOLD_MS + " ms");
        System.out.println("测试结果: " + (avgTime < SINGLE_OP_THRESHOLD_MS ? "通过" : "失败"));
        System.out.println("========================================");
        
        assertTrue(avgTime < SINGLE_OP_THRESHOLD_MS, 
                "浏览历史维度平均响应时间应小于" + SINGLE_OP_THRESHOLD_MS + "ms，实际耗时: " + String.format("%.4f", avgTime) + "ms");
    }

    @Test
    @DisplayName("组合维度性能测试")
    void testCombinedDimensionPerformance() {
        List<Long> tagMatchingTimes = new ArrayList<>();
        List<Long> browseHistoryTimes = new ArrayList<>();
        List<Long> combinedTimes = new ArrayList<>();
        
        for (int i = 0; i < ITERATIONS; i++) {
            DimensionContext context = DimensionContext.builder()
                    .userTags(userTags)
                    .browseHistory(browseHistoryList)
                    .candidatePostTags(postTags)
                    .build();
            
            long startTime1 = System.nanoTime();
            DimensionResult tagResult = tagMatchingDimension.calculate(context);
            long endTime1 = System.nanoTime();
            tagMatchingTimes.add((endTime1 - startTime1) / 1_000_000);
            
            long startTime2 = System.nanoTime();
            DimensionResult browseResult = browseHistoryDimension.calculate(context);
            long endTime2 = System.nanoTime();
            browseHistoryTimes.add((endTime2 - startTime2) / 1_000_000);
            
            combinedTimes.add(((endTime1 - startTime1) + (endTime2 - startTime2)) / 1_000_000);
            
            assertNotNull(tagResult, "标签匹配维度计算结果不应为空");
            assertNotNull(browseResult, "浏览历史维度计算结果不应为空");
        }
        
        double avgTagTime = tagMatchingTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgBrowseTime = browseHistoryTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgCombinedTime = combinedTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        
        System.out.println("========================================");
        System.out.println("组合维度性能测试报告");
        System.out.println("========================================");
        System.out.println("测试迭代次数: " + ITERATIONS);
        System.out.println("标签匹配维度平均时间: " + String.format("%.4f", avgTagTime) + " ms");
        System.out.println("浏览历史维度平均时间: " + String.format("%.4f", avgBrowseTime) + " ms");
        System.out.println("组合维度总平均时间: " + String.format("%.4f", avgCombinedTime) + " ms");
        System.out.println("性能要求: < " + (SINGLE_OP_THRESHOLD_MS * 2) + " ms");
        System.out.println("测试结果: " + (avgCombinedTime < SINGLE_OP_THRESHOLD_MS * 2 ? "通过" : "失败"));
        System.out.println("========================================");
        
        assertTrue(avgCombinedTime < SINGLE_OP_THRESHOLD_MS * 2, 
                "组合维度平均响应时间应小于" + (SINGLE_OP_THRESHOLD_MS * 2) + "ms，实际耗时: " + String.format("%.4f", avgCombinedTime) + "ms");
    }

    @Test
    @DisplayName("Jaccard相似度计算性能测试")
    void testJaccardSimilarityPerformance() {
        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();
        
        for (int i = 0; i < TAG_COUNT; i++) {
            set1.add("tag_" + i);
            if (i < TAG_COUNT * 0.6) {
                set2.add("tag_" + i);
            }
            if (i >= TAG_COUNT * 0.4) {
                set2.add("tag_alt_" + i);
            }
        }
        
        List<Long> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < ITERATIONS; i++) {
            long startTime = System.nanoTime();
            double similarity = tagMatchingDimension.calculateJaccardSimilarity(set1, set2);
            long endTime = System.nanoTime();
            
            responseTimes.add((endTime - startTime) / 1_000_000);
            assertTrue(similarity >= 0 && similarity <= 1, "相似度应在[0,1]范围内");
        }
        
        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        
        System.out.println("========================================");
        System.out.println("Jaccard相似度计算性能测试报告");
        System.out.println("========================================");
        System.out.println("测试迭代次数: " + ITERATIONS);
        System.out.println("集合大小: " + TAG_COUNT);
        System.out.println("平均响应时间: " + String.format("%.4f", avgTime) + " ms");
        System.out.println("最大响应时间: " + maxTime + " ms");
        System.out.println("========================================");
        
        assertTrue(avgTime < 1, "Jaccard相似度计算应非常快速，实际耗时: " + String.format("%.4f", avgTime) + "ms");
    }

    @Test
    @DisplayName("加权分数计算性能测试")
    void testWeightedScorePerformance() {
        List<UserTag> largeUserTags = new ArrayList<>();
        List<PostTag> largePostTags = new ArrayList<>();
        
        for (int i = 0; i < TAG_COUNT; i++) {
            largeUserTags.add(new UserTag("tag_" + i, 0.5 + random.nextDouble() * 0.5, "INTEREST"));
            largePostTags.add(new PostTag("tag_" + (i % 500), 0.5 + random.nextDouble() * 0.5, (long) i));
        }
        
        List<Long> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < ITERATIONS; i++) {
            long startTime = System.nanoTime();
            double score = tagMatchingDimension.calculateWeightedScore(largeUserTags, largePostTags);
            long endTime = System.nanoTime();
            
            responseTimes.add((endTime - startTime) / 1_000_000);
            assertTrue(score >= 0 && score <= 1, "加权分数应在[0,1]范围内");
        }
        
        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        
        System.out.println("========================================");
        System.out.println("加权分数计算性能测试报告");
        System.out.println("========================================");
        System.out.println("测试迭代次数: " + ITERATIONS);
        System.out.println("用户标签数量: " + TAG_COUNT);
        System.out.println("帖子标签数量: " + TAG_COUNT);
        System.out.println("平均响应时间: " + String.format("%.4f", avgTime) + " ms");
        System.out.println("最大响应时间: " + maxTime + " ms");
        System.out.println("========================================");
        
        assertTrue(avgTime < 5, "加权分数计算应快速完成，实际耗时: " + String.format("%.4f", avgTime) + "ms");
    }

    @Test
    @DisplayName("时间衰减权重计算性能测试")
    void testTimeDecayWeightPerformance() {
        long currentTime = System.currentTimeMillis();
        List<Long> browseTimes = new ArrayList<>();
        
        long maxTimeOffset = 30L * 24 * 60 * 60 * 1000;
        for (int i = 0; i < ITERATIONS; i++) {
            browseTimes.add(currentTime - random.nextLong(maxTimeOffset));
        }
        
        List<Long> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < ITERATIONS; i++) {
            long startTime = System.nanoTime();
            double weight = browseHistoryDimension.calculateTimeDecayWeight(browseTimes.get(i));
            long endTime = System.nanoTime();
            
            responseTimes.add((endTime - startTime) / 1_000_000);
            assertTrue(weight >= 0 && weight <= 1, "时间衰减权重应在合理范围内");
        }
        
        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        
        System.out.println("========================================");
        System.out.println("时间衰减权重计算性能测试报告");
        System.out.println("========================================");
        System.out.println("测试迭代次数: " + ITERATIONS);
        System.out.println("平均响应时间: " + String.format("%.6f", avgTime) + " ms");
        System.out.println("========================================");
        
        assertTrue(avgTime < 0.1, "时间衰减权重计算应非常快速，实际耗时: " + String.format("%.6f", avgTime) + "ms");
    }

    @Test
    @DisplayName("帖子相似度计算性能测试")
    void testPostSimilarityPerformance() {
        List<PostTag> post1Tags = new ArrayList<>();
        List<PostTag> post2Tags = new ArrayList<>();
        
        for (int i = 0; i < 50; i++) {
            post1Tags.add(new PostTag("tag_" + i, 0.5 + random.nextDouble() * 0.5, 1L));
            post2Tags.add(new PostTag("tag_" + (i + 25), 0.5 + random.nextDouble() * 0.5, 2L));
        }
        
        List<Long> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < ITERATIONS; i++) {
            long startTime = System.nanoTime();
            double similarity = browseHistoryDimension.calculatePostSimilarity(post1Tags, post2Tags);
            long endTime = System.nanoTime();
            
            responseTimes.add((endTime - startTime) / 1_000_000);
            assertTrue(similarity >= 0 && similarity <= 1, "相似度应在[0,1]范围内");
        }
        
        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        
        System.out.println("========================================");
        System.out.println("帖子相似度计算性能测试报告");
        System.out.println("========================================");
        System.out.println("测试迭代次数: " + ITERATIONS);
        System.out.println("每个帖子标签数量: 50");
        System.out.println("平均响应时间: " + String.format("%.4f", avgTime) + " ms");
        System.out.println("========================================");
        
        assertTrue(avgTime < 1, "帖子相似度计算应非常快速，实际耗时: " + String.format("%.4f", avgTime) + "ms");
    }

    @Test
    @DisplayName("大规模数据维度计算压力测试")
    void testLargeScaleDimensionStress() {
        int largeUserTagCount = 500;
        int largePostTagCount = 2000;
        int largeHistoryCount = 500;
        
        List<UserTag> largeUserTags = new ArrayList<>();
        for (int i = 0; i < largeUserTagCount; i++) {
            largeUserTags.add(new UserTag("userTag_" + i, random.nextDouble(), "INTEREST"));
        }
        
        List<PostTag> largePostTags = new ArrayList<>();
        for (int i = 0; i < largePostTagCount; i++) {
            largePostTags.add(new PostTag("postTag_" + (i % 100), random.nextDouble(), (long) i));
        }
        
        List<BrowseHistory> largeHistory = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long maxTimeOffset = 30L * 24 * 60 * 60 * 1000;
        for (int i = 0; i < largeHistoryCount; i++) {
            List<PostTag> historyTags = new ArrayList<>();
            for (int j = 0; j < 20; j++) {
                historyTags.add(new PostTag("historyTag_" + random.nextInt(100), random.nextDouble(), (long) i));
            }
            largeHistory.add(new BrowseHistory((long) i, currentTime - random.nextLong(maxTimeOffset), historyTags));
        }
        
        DimensionContext context = DimensionContext.builder()
                .userTags(largeUserTags)
                .browseHistory(largeHistory)
                .candidatePostTags(largePostTags)
                .build();
        
        List<Long> responseTimes = new ArrayList<>();
        int stressIterations = 100;
        
        for (int i = 0; i < stressIterations; i++) {
            long startTime = System.nanoTime();
            DimensionResult tagResult = tagMatchingDimension.calculate(context);
            DimensionResult browseResult = browseHistoryDimension.calculate(context);
            long endTime = System.nanoTime();
            
            responseTimes.add((endTime - startTime) / 1_000_000);
            assertNotNull(tagResult);
            assertNotNull(browseResult);
        }
        
        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        
        System.out.println("========================================");
        System.out.println("大规模数据维度计算压力测试报告");
        System.out.println("========================================");
        System.out.println("测试迭代次数: " + stressIterations);
        System.out.println("用户标签数量: " + largeUserTagCount);
        System.out.println("候选帖子标签数量: " + largePostTagCount);
        System.out.println("浏览历史数量: " + largeHistoryCount);
        System.out.println("平均响应时间: " + String.format("%.2f", avgTime) + " ms");
        System.out.println("最大响应时间: " + maxTime + " ms");
        System.out.println("最小响应时间: " + minTime + " ms");
        System.out.println("========================================");
        
        assertTrue(avgTime < 100, "大规模数据计算应在合理时间内完成，实际耗时: " + String.format("%.2f", avgTime) + "ms");
    }
}
