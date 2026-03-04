package com.example.demo.duplicate.repository.impl;

import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.repository.ArticleRepository;
import com.example.demo.tag.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于内存的文章仓储实现类
 * 使用Map存储文章数据，提供基本的CRUD操作
 */
public class InMemoryArticleRepository implements ArticleRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryArticleRepository.class);
    
    private final Map<Long, Article> articleStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    /**
     * 根据ID查找文章
     * @param id 文章ID
     * @return 文章对象，如果不存在则返回null
     */
    @Override
    public Article findById(Long id) {
        logger.debug("查找文章，ID: {}", id);
        Article article = articleStore.get(id);
        if (article != null) {
            logger.debug("找到文章: {}", article.getTitle());
        } else {
            logger.debug("未找到文章，ID: {}", id);
        }
        return article;
    }
    
    /**
     * 查找最近指定天数内的文章
     * @param days 天数
     * @return 最近的文章列表
     */
    @Override
    public List<Article> findRecentArticles(int days) {
        logger.debug("查找最近 {} 天内的文章", days);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<Article> recentArticles = new ArrayList<>();
        
        for (Article article : articleStore.values()) {
            if (article.getCreateTime() != null && article.getCreateTime().isAfter(cutoffDate)) {
                recentArticles.add(article);
            }
        }
        
        logger.debug("找到 {} 篇最近 {} 天内的文章", recentArticles.size(), days);
        return recentArticles;
    }
    
    /**
     * 保存文章
     * @param article 要保存的文章对象
     */
    @Override
    public void save(Article article) {
        if (article == null) {
            logger.warn("尝试保存空文章对象");
            return;
        }
        
        if (article.getId() == null) {
            article.setId(idGenerator.getAndIncrement());
            logger.debug("为新文章分配ID: {}", article.getId());
        }
        
        article.setUpdateTime(LocalDateTime.now());
        articleStore.put(article.getId(), article);
        logger.info("保存文章成功，ID: {}, 标题: {}", article.getId(), article.getTitle());
    }
    
    /**
     * 根据标签列表查找文章
     * @param tags 标签列表
     * @return 包含指定标签的文章列表
     */
    @Override
    public List<Article> findByTags(List<Tag> tags) {
        logger.debug("根据标签查找文章，标签数量: {}", tags != null ? tags.size() : 0);
        List<Article> matchedArticles = new ArrayList<>();
        
        if (tags == null || tags.isEmpty()) {
            logger.debug("标签列表为空，返回空结果");
            return matchedArticles;
        }
        
        for (Article article : articleStore.values()) {
            if (article.getTags() != null) {
                for (Tag articleTag : article.getTags()) {
                    for (Tag searchTag : tags) {
                        if (articleTag.getName() != null && articleTag.getName().equals(searchTag.getName())) {
                            matchedArticles.add(article);
                            break;
                        }
                    }
                }
            }
        }
        
        logger.debug("找到 {} 篇匹配标签的文章", matchedArticles.size());
        return matchedArticles;
    }
    
    /**
     * 查找所有文章
     * @return 所有文章列表
     */
    @Override
    public List<Article> findAll() {
        logger.debug("查找所有文章");
        List<Article> allArticles = new ArrayList<>(articleStore.values());
        logger.debug("共找到 {} 篇文章", allArticles.size());
        return allArticles;
    }
    
    /**
     * 根据ID删除文章
     * @param id 要删除的文章ID
     */
    @Override
    public void delete(Long id) {
        logger.debug("删除文章，ID: {}", id);
        Article removed = articleStore.remove(id);
        if (removed != null) {
            logger.info("删除文章成功，ID: {}, 标题: {}", id, removed.getTitle());
        } else {
            logger.warn("删除文章失败，未找到ID为 {} 的文章", id);
        }
    }
}
