package com.example.demo.duplicate.index;

import com.example.demo.duplicate.model.Article;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Map;

/**
 * SimHash 倒排索引接口。
 */
public interface SimHashIndex {

    void addArticle(Article article);

    void addArticle(Article article, BigInteger fingerprint);

    void removeArticle(Long articleId);

    Map<Long, Integer> findCandidates(Article article, int maxHammingDistance);

    Map<Long, Integer> findCandidates(BigInteger fingerprint, int maxHammingDistance);

    BigInteger getFingerprint(Long articleId);

    boolean contains(Long articleId);

    int size();

    void clear();

    void save(Path path) throws IOException;

    void load(Path path) throws IOException;
}
