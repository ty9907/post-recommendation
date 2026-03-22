package com.example.demo.duplicate.async;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.DuplicateCheckReport;
import com.example.demo.duplicate.model.SimilarityResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("异步检测服务测试")
class AsyncDetectionServiceTest {

    @Test
    @DisplayName("应能异步执行精检任务并回调结果")
    void shouldExecuteAsyncTaskAndReturnReport() throws Exception {
        DuplicateCheckConfig config = DuplicateCheckConfig.defaultConfig();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DuplicateCheckReport> reportRef = new AtomicReference<>();

        SimilarityCalculator calculator = new SimilarityCalculator() {
            @Override
            public double calculateSimilarity(Article article1, Article article2) {
                return 0.9;
            }

            @Override
            public List<SimilarityResult> calculateSimilarities(Article article, List<Article> articles) {
                List<SimilarityResult> results = new ArrayList<>();
                for (Article candidate : articles) {
                    results.add(new SimilarityResult(
                            article.getId(),
                            candidate.getId(),
                            0.9,
                            "TEST",
                            LocalDateTime.now(),
                            Map.of("candidate", candidate.getId())
                    ));
                }
                return results;
            }

            @Override
            public String getName() {
                return "TEST";
            }
        };

        Article article = new Article(1L, "原文", "内容");
        Article candidate = new Article(2L, "候选", "内容");

        AsyncDetectionService service = new AsyncDetectionService(8, 1);
        try {
            boolean submitted = service.submit(article, List.of(candidate), calculator, config, new DetectionResultCallback() {
                @Override
                public void onComplete(DuplicateCheckReport report) {
                    reportRef.set(report);
                    latch.countDown();
                }
            });

            assertTrue(submitted, "任务应提交成功");
            assertTrue(latch.await(5, TimeUnit.SECONDS), "应在超时前收到回调");
            assertNotNull(reportRef.get(), "回调报告不应为空");
            assertTrue(reportRef.get().isHasDuplicate(), "应判定为重复");
        } finally {
            service.shutdown();
        }
    }
}
