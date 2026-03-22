package com.example.demo.duplicate.detector.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.candidate.CandidateManagerImpl;
import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.DuplicateCheckReport;
import com.example.demo.duplicate.model.SimilarityResult;
import com.example.demo.duplicate.monitor.PerformanceMonitor;
import com.example.demo.duplicate.risk.RiskAssessor;
import com.example.demo.duplicate.risk.RiskLevel;
import com.example.demo.tag.model.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("分层检测集成测试")
class LayeredDetectionIntegrationTest {

    @Test
    @DisplayName("实时检测应输出分层细节和性能指标")
    void shouldProduceLayerDetailsAndMetrics() {
        DuplicateCheckConfig config = DuplicateCheckConfig.defaultConfig();
        config.setEnableAsyncDetection(false);

        SimilarityCalculator calculator = new SimilarityCalculator() {
            @Override
            public double calculateSimilarity(Article article1, Article article2) {
                return "Java 微服务实践".equals(article2.getTitle()) ? 0.92 : 0.15;
            }

            @Override
            public List<SimilarityResult> calculateSimilarities(Article article, List<Article> articles) {
                List<SimilarityResult> results = new ArrayList<>();
                for (Article candidate : articles) {
                    results.add(new SimilarityResult(
                            article.getId(),
                            candidate.getId(),
                            calculateSimilarity(article, candidate),
                            "TEST",
                            LocalDateTime.now(),
                            Map.of()
                    ));
                }
                return results;
            }

            @Override
            public String getName() {
                return "TEST";
            }
        };

        Article query = createArticle(100L, "Java 微服务实践", "Java Spring Boot 微服务 架构 实践", "java", "spring");
        Article candidate1 = createArticle(1L, "Java 微服务实践", "Java Spring Boot 微服务 架构 实践", "java", "spring");
        Article candidate2 = createArticle(2L, "Redis 缓存指南", "Redis 缓存 中间件 实战", "redis", "cache");

        RealTimeDetector detector = new RealTimeDetector(
                calculator,
                config,
                null,
                new CandidateManagerImpl(),
                new PerformanceMonitor(),
                new RiskAssessor(),
                null
        );

        DuplicateCheckReport report = detector.detect(query, List.of(candidate1, candidate2), config);

        assertTrue(report.isHasDuplicate(), "应识别到重复内容");
        assertEquals(RiskLevel.HIGH, report.getRiskLevel(), "应评估为高风险");
        assertFalse(report.getLayerDetails().isEmpty(), "应包含分层筛选信息");
        assertFalse(report.getPerformanceMetrics().isEmpty(), "应包含性能指标");
    }

    private Article createArticle(Long id, String title, String content, String... tags) {
        Article article = new Article(id, title, content);
        article.setTags(List.of(
                new Tag(tags[0], 1.0, 1),
                new Tag(tags[1], 1.0, 1)
        ));
        return article;
    }
}
