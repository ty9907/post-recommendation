package com.example.demo.duplicate.candidate;

import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.model.Article;
import com.example.demo.tag.model.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("候选集管理器测试")
class CandidateManagerImplTest {

    @Test
    @DisplayName("应优先筛选出高相似候选")
    void shouldSelectMostRelevantCandidates() {
        CandidateManagerImpl candidateManager = new CandidateManagerImpl();
        DuplicateCheckConfig config = DuplicateCheckConfig.defaultConfig();
        config.setEnableFullScanFallback(false);

        Article candidate1 = createArticle(1L, "Java 微服务实践", "Java Spring Boot 微服务 架构 实践", "java", "spring");
        Article candidate2 = createArticle(2L, "Redis 指南", "Redis 缓存 中间件 实战", "redis", "cache");
        Article query = createArticle(99L, "Java 微服务实践", "Java Spring Boot 微服务 架构 实践", "java", "spring");

        CandidateSelection selection = candidateManager.selectCandidates(query, List.of(candidate1, candidate2), config);

        assertFalse(selection.getCandidates().isEmpty(), "应至少命中一个候选");
        assertEquals(1L, selection.getCandidates().get(0).getId(), "最相关文章应排在首位");
    }

    @Test
    @DisplayName("重复查询应命中候选缓存")
    void shouldHitCandidateCache() {
        CandidateManagerImpl candidateManager = new CandidateManagerImpl();
        DuplicateCheckConfig config = DuplicateCheckConfig.defaultConfig();

        Article candidate = createArticle(1L, "Java 微服务实践", "Java Spring Boot 微服务 架构 实践", "java", "spring");
        Article query = createArticle(99L, "Java 微服务实践", "Java Spring Boot 微服务 架构 实践", "java", "spring");

        candidateManager.selectCandidates(query, List.of(candidate), config);
        CandidateSelection secondSelection = candidateManager.selectCandidates(query, null, config);

        assertTrue(secondSelection.isCacheHit(), "第二次查询应命中缓存");
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
