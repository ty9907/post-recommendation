package com.example.demo.duplicate.candidate;

import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.index.SimHashIndex;
import com.example.demo.duplicate.index.TagInvertedIndex;
import com.example.demo.duplicate.model.Article;

import java.util.List;

/**
 * 候选集管理器接口。
 */
public interface CandidateManager {

    CandidateSelection selectCandidates(Article article, List<Article> existingArticles, DuplicateCheckConfig config);

    void warmUp(List<Article> articles);

    void removeArticle(Long articleId);

    void clear();

    SimHashIndex getSimHashIndex();

    TagInvertedIndex getTagInvertedIndex();
}
