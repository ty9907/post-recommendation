package com.example.demo.duplicate.async;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.DuplicateCheckReport;
import com.example.demo.duplicate.model.SimilarityResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 异步检测任务。
 */
public class AsyncDetectionTask implements Runnable {

    private final Article article;
    private final List<Article> existingArticles;
    private final SimilarityCalculator calculator;
    private final DuplicateCheckConfig config;
    private final DetectionResultCallback callback;

    public AsyncDetectionTask(Article article,
                              List<Article> existingArticles,
                              SimilarityCalculator calculator,
                              DuplicateCheckConfig config,
                              DetectionResultCallback callback) {
        this.article = article;
        this.existingArticles = existingArticles != null ? new ArrayList<>(existingArticles) : new ArrayList<>();
        this.calculator = calculator;
        this.config = config != null ? config : DuplicateCheckConfig.defaultConfig();
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            DuplicateCheckReport report = execute();
            if (callback != null) {
                callback.onComplete(report);
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e);
            }
        }
    }

    public DuplicateCheckReport execute() {
        DuplicateCheckReport report = new DuplicateCheckReport();
        report.setArticleId(article != null ? article.getId() : null);
        report.setCheckTime(LocalDateTime.now());

        if (article == null || calculator == null) {
            report.setHasDuplicate(false);
            report.setSummary("异步检测未执行：缺少必要参数");
            return report;
        }

        List<Article> candidates = existingArticles.stream()
                .filter(candidate -> candidate != null
                        && candidate.getId() != null
                        && !candidate.getId().equals(article.getId()))
                .toList();

        List<SimilarityResult> results = calculator.calculateSimilarities(article, candidates);
        results = results.stream()
                .filter(result -> result.getSimilarity() >= config.getSensitivity())
                .sorted(Comparator.comparingDouble(SimilarityResult::getSimilarity).reversed())
                .limit(config.getMaxResults())
                .toList();

        report.setResults(new ArrayList<>(results));
        boolean hasDuplicate = results.stream().anyMatch(result -> result.getSimilarity() >= config.getThreshold());
        report.setHasDuplicate(hasDuplicate);
        report.setSummary(hasDuplicate ? "异步精检发现高相似内容" : "异步精检完成，未发现重复");
        return report;
    }
}
