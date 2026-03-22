package com.example.demo.duplicate.async;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 异步检测工作线程。
 */
public class AsyncDetectionWorker implements Runnable {

    private final AsyncDetectionQueue queue;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public AsyncDetectionWorker(AsyncDetectionQueue queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                AsyncDetectionTask task = queue.poll(200, TimeUnit.MILLISECONDS);
                if (task != null) {
                    task.run();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        running.set(false);
    }
}
