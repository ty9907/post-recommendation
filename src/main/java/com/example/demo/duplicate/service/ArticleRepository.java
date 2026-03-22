package com.example.demo.duplicate.service;

import com.example.demo.duplicate.model.Article;

import java.util.List;
import java.util.Optional;

/**
 * 文章仓储接口。
 *
 * 由接入方实现，用于从真实业务系统中加载待比较文章。
 */
public interface ArticleRepository {

    Optional<Article> findById(Long id);

    List<Article> findAll();

    List<Article> findRecentArticles(int recentDays);

    List<Article> findByTag(String tag);

    void save(Article article);

    void deleteById(Long id);
}
