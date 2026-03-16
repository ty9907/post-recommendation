package com.example.demo.recommendation.performance;

import com.example.demo.recommendation.PostRecommendationSDK;
import com.example.demo.recommendation.config.SDKConfig;
import com.example.demo.recommendation.model.BrowseHistory;
import com.example.demo.recommendation.model.PostTag;
import com.example.demo.recommendation.model.RecommendationConfig;
import com.example.demo.recommendation.model.RecommendationRequest;
import com.example.demo.recommendation.model.RecommendationResult;
import com.example.demo.recommendation.model.UserTag;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 推荐性能测试类
 * 测试帖子推荐SDK的性能指标
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RecommendationPerformanceTest {

    private static final int POST_COUNT = 1000;
    private static final int USER_TAG_COUNT = 20;
    private static final int POST_TAG_COUNT = 5;
    private static final int BROWSE_HISTORY_COUNT = 50;
    private static final int CONCURRENT_REQUEST_COUNT = 1000;
    private static final long PERFORMANCE_THRESHOLD_MS = 1000;
    private static final double SUCCESS_RATE_THRESHOLD = 0.99;

    private PostRecommendationSDK sdk;
    private List<PostTag> allPostTags;
    private List<UserTag> userTags;
    private List<BrowseHistory> browseHistory;
    private Random random;

    @BeforeAll
    void setUp() {
        random = new Random(42);
        
        SDKConfig config = SDKConfig.builder()
                .addDimensionWeight("TAG_MATCHING", 0.5)
                .addDimensionWeight("BROWSE_HISTORY", 0.5)
                .defaultLimit(100)
                .threadPoolSize(8)
                .enableCache(true)
                .build();
        
        sdk = PostRecommendationSDK.getInstance();
        sdk.initialize(config);
        
        generateTestData();
    }

    @AfterAll
    void tearDown() {
        if (sdk != null) {
            sdk.shutdown();
        }
    }

    /**
     * 生成测试数据
     */
    private void generateTestData() {
        allPostTags = new ArrayList<>();
        for (long postId = 1; postId <= POST_COUNT; postId++) {
            for (int i = 0; i < POST_TAG_COUNT; i++) {
                String tagName = "tag_" + (random.nextInt(100) + 1);
                double weight = 0.5 + random.nextDouble() * 0.5;
                allPostTags.add(new PostTag(tagName, weight, postId));
            }
        }
        
        userTags = new ArrayList<>();
        for (int i = 0; i < USER_TAG_COUNT; i++) {
            String tagName = "tag_" + (random.nextInt(100) + 1);
            double weight = 0.3 + random.nextDouble() * 0.7;
            userTags.add(new UserTag(tagName, weight, "INTEREST"));
        }
        
        browseHistory = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < BROWSE_HISTORY_COUNT; i++) {
            long postId = random.nextInt(POST_COUNT) + 1L;
            long browseTime = currentTime - random.nextInt(7 * 24 * 60 * 60 * 1000);
            
            List<PostTag> historyTags = new ArrayList<>();
            for (int j = 0; j < POST_TAG_COUNT; j++) {
                String tagName = "tag_" + (random.nextInt(100) + 1);
                double weight = 0.5 + random.nextDouble() * 0.5;
                historyTags.add(new PostTag(tagName, weight, postId));
            }
            
            browseHistory.add(new BrowseHistory(postId, browseTime, historyTags));
        }
    }

    /**
     * 创建推荐请求
     */
    private RecommendationRequest createRequest() {
        RecommendationConfig config = new RecommendationConfig();
        return new RecommendationRequest(userTags, allPostTags, browseHistory, config);
    }

    @Test
    @DisplayName("单次推荐性能测试 - 1000帖子响应时间应小于1000ms")
    void testSingleRecommendationPerformance() {
        RecommendationRequest request = createRequest();
        
        long startTime = System.nanoTime();
        List<RecommendationResult> results = sdk.recommend(request);
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.println("========================================");
        System.out.println("单次推荐性能测试报告");
        System.out.println("========================================");
        System.out.println("候选帖子数量: " + POST_COUNT);
        System.out.println("用户标签数量: " + USER_TAG_COUNT);
        System.out.println("浏览历史数量: " + BROWSE_HISTORY_COUNT);
        System.out.println("响应时间: " + durationMs + " ms");
        System.out.println("返回结果数量: " + results.size());
        System.out.println("性能要求: < " + PERFORMANCE_THRESHOLD_MS + " ms");
        System.out.println("测试结果: " + (durationMs < PERFORMANCE_THRESHOLD_MS ? "通过" : "失败"));
        System.out.println("========================================");
        
        assertNotNull(results, "推荐结果不应为空");
        assertFalse(results.isEmpty(), "推荐结果列表不应为空");
        assertTrue(durationMs < PERFORMANCE_THRESHOLD_MS, 
                "单次推荐响应时间应小于" + PERFORMANCE_THRESHOLD_MS + "ms，实际耗时: " + durationMs + "ms");
        
        if (!results.isEmpty()) {
            System.out.println("Top 5 推荐结果:");
            for (int i = 0; i < Math.min(5, results.size()); i++) {
                RecommendationResult result = results.get(i);
                System.out.println("  " + (i + 1) + ". 帖子ID: " + result.getPostId() + 
                        ", 总分: " + String.format("%.4f", result.getTotalScore()));
            }
        }
    }

    @Test
    @DisplayName("并发推荐性能测试 - 1000并发请求成功率应大于99%")
    void testConcurrentRecommendationPerformance() throws InterruptedException {
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUEST_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Long> responseTimes = new ArrayList<>();
        
        long testStartTime = System.nanoTime();
        
        for (int i = 0; i < CONCURRENT_REQUEST_COUNT; i++) {
            executor.submit(() -> {
                try {
                    long requestStart = System.nanoTime();
                    RecommendationRequest request = createRequest();
                    List<RecommendationResult> results = sdk.recommend(request);
                    long requestEnd = System.nanoTime();
                    
                    synchronized (responseTimes) {
                        responseTimes.add((requestEnd - requestStart) / 1_000_000);
                    }
                    
                    if (results != null && !results.isEmpty()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        long testEndTime = System.nanoTime();
        executor.shutdown();
        
        long totalTestTimeMs = (testEndTime - testStartTime) / 1_000_000;
        double successRate = (double) successCount.get() / CONCURRENT_REQUEST_COUNT;
        double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
        long maxResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
        long minResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0);
        
        System.out.println("========================================");
        System.out.println("并发推荐性能测试报告");
        System.out.println("========================================");
        System.out.println("并发请求数量: " + CONCURRENT_REQUEST_COUNT);
        System.out.println("线程池大小: " + threadCount);
        System.out.println("成功请求数: " + successCount.get());
        System.out.println("失败请求数: " + failureCount.get());
        System.out.println("成功率: " + String.format("%.2f%%", successRate * 100));
        System.out.println("总耗时: " + totalTestTimeMs + " ms");
        System.out.println("平均响应时间: " + String.format("%.2f", avgResponseTime) + " ms");
        System.out.println("最大响应时间: " + maxResponseTime + " ms");
        System.out.println("最小响应时间: " + minResponseTime + " ms");
        System.out.println("吞吐量: " + String.format("%.2f", (double) CONCURRENT_REQUEST_COUNT / totalTestTimeMs * 1000) + " 请求/秒");
        System.out.println("成功率要求: > " + (SUCCESS_RATE_THRESHOLD * 100) + "%");
        System.out.println("测试结果: " + (successRate > SUCCESS_RATE_THRESHOLD ? "通过" : "失败"));
        System.out.println("========================================");
        
        assertTrue(successRate > SUCCESS_RATE_THRESHOLD, 
                "并发请求成功率应大于" + (SUCCESS_RATE_THRESHOLD * 100) + "%，实际成功率: " + String.format("%.2f%%", successRate * 100));
    }

    @Test
    @DisplayName("批量推荐性能测试")
    void testBatchRecommendationPerformance() {
        int batchSize = 100;
        Map<String, RecommendationRequest> requests = new HashMap<>();
        
        for (int i = 0; i < batchSize; i++) {
            String requestId = "batch_" + i;
            requests.put(requestId, createRequest());
        }
        
        long startTime = System.nanoTime();
        Map<String, List<RecommendationResult>> results = sdk.batchRecommend(requests);
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        double avgTimePerRequest = (double) durationMs / batchSize;
        
        System.out.println("========================================");
        System.out.println("批量推荐性能测试报告");
        System.out.println("========================================");
        System.out.println("批量请求数量: " + batchSize);
        System.out.println("总耗时: " + durationMs + " ms");
        System.out.println("平均每个请求耗时: " + String.format("%.2f", avgTimePerRequest) + " ms");
        System.out.println("返回结果数量: " + results.size());
        System.out.println("========================================");
        
        assertEquals(batchSize, results.size(), "返回结果数量应与请求数量一致");
        
        for (Map.Entry<String, List<RecommendationResult>> entry : results.entrySet()) {
            assertNotNull(entry.getValue(), "每个请求的结果不应为空");
        }
    }

    @Test
    @DisplayName("内存使用测试")
    void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        
        runtime.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        int iterations = 100;
        List<List<RecommendationResult>> allResults = new ArrayList<>();
        
        for (int i = 0; i < iterations; i++) {
            RecommendationRequest request = createRequest();
            List<RecommendationResult> results = sdk.recommend(request);
            allResults.add(results);
        }
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        long memoryUsedMB = memoryUsed / (1024 * 1024);
        double avgMemoryPerRequest = (double) memoryUsed / iterations;
        
        System.out.println("========================================");
        System.out.println("内存使用测试报告");
        System.out.println("========================================");
        System.out.println("测试迭代次数: " + iterations);
        System.out.println("内存使用前: " + (memoryBefore / 1024 / 1024) + " MB");
        System.out.println("内存使用后: " + (memoryAfter / 1024 / 1024) + " MB");
        System.out.println("总内存增量: " + memoryUsedMB + " MB");
        System.out.println("平均每次请求内存增量: " + String.format("%.2f", avgMemoryPerRequest / 1024) + " KB");
        System.out.println("JVM最大内存: " + (runtime.maxMemory() / 1024 / 1024) + " MB");
        System.out.println("内存使用比例: " + String.format("%.2f%%", (double) memoryAfter / runtime.maxMemory() * 100));
        System.out.println("========================================");
        
        assertTrue(memoryUsedMB < 500, "内存使用应在合理范围内（<500MB），实际使用: " + memoryUsedMB + "MB");
        
        allResults.clear();
        runtime.gc();
    }

    @Test
    @DisplayName("多次推荐性能稳定性测试")
    void testRecommendationStability() {
        int iterations = 50;
        List<Long> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < iterations; i++) {
            RecommendationRequest request = createRequest();
            long startTime = System.nanoTime();
            List<RecommendationResult> results = sdk.recommend(request);
            long endTime = System.nanoTime();
            
            responseTimes.add((endTime - startTime) / 1_000_000);
            assertNotNull(results, "第" + (i + 1) + "次推荐结果不应为空");
        }
        
        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double variance = responseTimes.stream()
                .mapToDouble(t -> Math.pow(t - avgTime, 2))
                .average()
                .orElse(0);
        double stdDev = Math.sqrt(variance);
        long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        
        System.out.println("========================================");
        System.out.println("性能稳定性测试报告");
        System.out.println("========================================");
        System.out.println("测试迭代次数: " + iterations);
        System.out.println("平均响应时间: " + String.format("%.2f", avgTime) + " ms");
        System.out.println("响应时间标准差: " + String.format("%.2f", stdDev) + " ms");
        System.out.println("最大响应时间: " + maxTime + " ms");
        System.out.println("最小响应时间: " + minTime + " ms");
        System.out.println("响应时间范围: " + (maxTime - minTime) + " ms");
        System.out.println("稳定性评估: " + (stdDev < avgTime * 0.3 ? "稳定" : "波动较大"));
        System.out.println("========================================");
        
        assertTrue(stdDev < avgTime * 0.5, "响应时间波动应在合理范围内");
    }
}
