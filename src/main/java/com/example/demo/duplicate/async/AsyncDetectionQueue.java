package com.example.demo.duplicate.async;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 异步检测任务队列。
 */
public class AsyncDetectionQueue {

    private final BlockingQueue<AsyncDetectionTask> queue;

    public AsyncDetectionQueue(int capacity) {
        this.queue = new ArrayBlockingQueue<>(Math.max(1, capacity));
    }

    public boolean offer(AsyncDetectionTask task) {
        return task != null && queue.offer(task);
    }

    public AsyncDetectionTask poll(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return queue.poll(timeout, timeUnit);
    }

    public int size() {
        return queue.size();
    }

    public int remainingCapacity() {
        return queue.remainingCapacity();
    }
}
