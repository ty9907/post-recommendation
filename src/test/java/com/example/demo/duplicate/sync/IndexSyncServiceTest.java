package com.example.demo.duplicate.sync;

import com.example.demo.duplicate.index.InMemorySimHashIndex;
import com.example.demo.duplicate.index.InMemoryTagInvertedIndex;
import com.example.demo.duplicate.model.Article;
import com.example.demo.tag.model.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("索引同步服务测试")
class IndexSyncServiceTest {

    @Test
    @DisplayName("应支持新增更新删除索引同步")
    void shouldSyncIndexesOnArticleEvents() {
        InMemorySimHashIndex simHashIndex = new InMemorySimHashIndex();
        InMemoryTagInvertedIndex tagInvertedIndex = new InMemoryTagInvertedIndex();
        IndexSyncService syncService = new IndexSyncService(simHashIndex, tagInvertedIndex);

        Article article = new Article(1L, "Java", "Java Spring Boot");
        article.setTags(List.of(new Tag("java", 1.0, 1)));

        syncService.onArticleAdded(article);
        assertTrue(simHashIndex.contains(1L), "新增文章后应建立 SimHash 索引");
        assertTrue(tagInvertedIndex.findByTag("java").contains(1L), "新增文章后应建立标签索引");

        Article updated = new Article(1L, "Redis", "Redis Cache");
        updated.setTags(List.of(new Tag("redis", 1.0, 1)));
        syncService.onArticleUpdated(updated);
        assertFalse(tagInvertedIndex.findByTag("java").contains(1L), "更新后旧标签应移除");
        assertTrue(tagInvertedIndex.findByTag("redis").contains(1L), "更新后新标签应建立");

        syncService.onArticleDeleted(1L);
        assertFalse(simHashIndex.contains(1L), "删除后应移除 SimHash 索引");
        assertFalse(tagInvertedIndex.containsArticle(1L), "删除后应移除标签索引");
    }
}
