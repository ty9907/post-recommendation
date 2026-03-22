package com.example.demo.duplicate.async;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.model.Article;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 异步检测服务。
 */
public class AsyncDetectionService {

    private final AsyncDetectionQueue queue;
    private final ExecutorService executorService;
    private final List<AsyncDetectionWorker> workers = new ArrayList<>();

    public AsyncDetectionService(int queueSize, int workerCount) {
        this.queue = new AsyncDetectionQueue(queueSize);
        int actualWorkerCount = Math.max(1, workerCount);
        this.executorService = Executors.newFixedThreadPool(actualWorkerCount);
        for (int i = 0; i < actualWorkerCount; i++) {
            AsyncDetectionWorker worker = new AsyncDetectionWorker(queue);
            workers.add(worker);
            executorService.submit(worker);
        }
    }

    public AsyncDetectionService(DuplicateCheckConfig config) {
        this(config != null ? config.getAsyncQueueSize() : DuplicateCheckConfig.defaultConfig().getAsyncQueueSize(),
                config != null ? config.getAsyncWorkerCount() : DuplicateCheckConfig.defaultConfig().getAsyncWorkerCount());
    }

    public boolean submit(AsyncDetectionTask task) {
        return queue.offer(task);
    }

    public boolean submit(Article article,
                          List<Article> existingArticles,
                          SimilarityCalculator calculator,
                          DuplicateCheckConfig config,
                          DetectionResultCallback callback) {
        return submit(new AsyncDetectionTask(article, existingArticles, calculator, config, callback));
    }

    public int getQueueSize() {
        return queue.size();
    }

    public void shutdown() {
        for (AsyncDetectionWorker worker : workers) {
            worker.shutdown();
        }
        executorService.shutdownNow();
    }

    public boolean awaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return executorService.awaitTermination(timeout, timeUnit);
    }
}
