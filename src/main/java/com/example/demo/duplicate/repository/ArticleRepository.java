package com.example.demo.duplicate.repository;

import com.example.demo.duplicate.model.Article;
import com.example.demo.tag.model.Tag;
import java.util.List;

/**
 * 文章仓储接口
 * 定义文章数据访问的基本操作
 */
public interface ArticleRepository {
    
    /**
     * 根据ID查找文章
     * @param id 文章ID
     * @return 文章对象，如果不存在则返回null
     */
    Article findById(Long id);
    
    /**
     * 查找最近指定天数内的文章
     * @param days 天数
     * @return 最近的文章列表
     */
    List<Article> findRecentArticles(int days);
    
    /**
     * 保存文章
     * @param article 要保存的文章对象
     */
    void save(Article article);
    
    /**
     * 根据标签列表查找文章
     * @param tags 标签列表
     * @return 包含指定标签的文章列表
     */
    List<Article> findByTags(List<Tag> tags);
    
    /**
     * 查找所有文章
     * @return 所有文章列表
     */
    List<Article> findAll();
    
    /**
     * 根据ID删除文章
     * @param id 要删除的文章ID
     */
    void delete(Long id);
}
