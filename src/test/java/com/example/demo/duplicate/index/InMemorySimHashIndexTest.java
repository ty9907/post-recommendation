package com.example.demo.duplicate.index;

import com.example.demo.duplicate.model.Article;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SimHash 倒排索引测试")
class InMemorySimHashIndexTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("应能根据指纹查询相似候选")
    void shouldFindCandidatesByFingerprint() {
        InMemorySimHashIndex index = new InMemorySimHashIndex();

        Article article1 = new Article(1L, "Java 微服务实践", "Java Spring Boot 微服务 架构 实践");
        Article article2 = new Article(2L, "Redis 缓存指南", "Redis 缓存 中间件 实战");
        Article query = new Article(99L, "Java 微服务实践", "Java Spring Boot 微服务 架构 实践");

        index.addArticle(article1);
        index.addArticle(article2);

        Map<Long, Integer> candidates = index.findCandidates(query, 0);

        assertEquals(1, candidates.size(), "完全相同内容应只命中一个候选");
        assertTrue(candidates.containsKey(1L), "应命中相同文章");
        assertEquals(0, candidates.get(1L), "相同文章海明距离应为0");
    }

    @Test
    @DisplayName("应支持索引持久化与加载")
    void shouldPersistAndLoadIndex() throws Exception {
        InMemorySimHashIndex index = new InMemorySimHashIndex();
        Article article = new Article(1L, "Java 指南", "Java 并发 编程 指南");
        index.addArticle(article);

        BigInteger originalFingerprint = index.getFingerprint(1L);
        Path indexFile = tempDir.resolve("simhash-index.txt");
        index.save(indexFile);

        InMemorySimHashIndex loadedIndex = new InMemorySimHashIndex();
        loadedIndex.load(indexFile);

        assertTrue(loadedIndex.contains(1L), "加载后应包含原有文章");
        assertEquals(originalFingerprint, loadedIndex.getFingerprint(1L), "加载前后指纹应一致");
    }
}
