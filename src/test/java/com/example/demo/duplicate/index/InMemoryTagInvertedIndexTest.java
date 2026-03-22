package com.example.demo.duplicate.index;

import com.example.demo.duplicate.model.Article;
import com.example.demo.tag.model.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("标签倒排索引测试")
class InMemoryTagInvertedIndexTest {

    @Test
    @DisplayName("应能根据标签查找文章")
    void shouldFindArticlesByTag() {
        InMemoryTagInvertedIndex index = new InMemoryTagInvertedIndex();

        Article article1 = createArticle(1L, "Java", "Spring");
        Article article2 = createArticle(2L, "Java", "Redis");

        index.addArticle(article1);
        index.addArticle(article2);

        Set<Long> javaArticles = index.findByTag("java");

        assertEquals(Set.of(1L, 2L), javaArticles, "Java 标签应命中两篇文章");
    }

    @Test
    @DisplayName("应统计多标签命中次数")
    void shouldCountMatchedTags() {
        InMemoryTagInvertedIndex index = new InMemoryTagInvertedIndex();

        Article article1 = createArticle(1L, "Java", "Spring");
        Article article2 = createArticle(2L, "Java", "Redis");

        index.addArticle(article1);
        index.addArticle(article2);

        Map<Long, Integer> result = index.findByAnyTagsWithCount(List.of("java", "spring"));

        assertEquals(2, result.get(1L), "同时命中 Java 和 Spring 的文章计数应为2");
        assertEquals(1, result.get(2L), "只命中 Java 的文章计数应为1");
    }

    private Article createArticle(Long id, String... tags) {
        Article article = new Article(id, "标题" + id, "内容" + id);
        article.setTags(List.of(
                new Tag(tags[0], 1.0, 1),
                new Tag(tags[1], 1.0, 1)
        ));
        return article;
    }
}
