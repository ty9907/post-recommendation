package com.example.demo.duplicate.preprocess;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 预处理文本缓存。
 */
public class PreprocessedTextCache {

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final int maxSize;
    private final long expireMillis;

    private static final class CacheEntry {
        private final String value;
        private final long createTime;

        private CacheEntry(String value) {
            this.value = value;
            this.createTime = System.currentTimeMillis();
        }
    }

    public PreprocessedTextCache() {
        this(10_000, 15 * 60 * 1000L);
    }

    public PreprocessedTextCache(int maxSize, long expireMillis) {
        this.maxSize = Math.max(1, maxSize);
        this.expireMillis = Math.max(1, expireMillis);
    }

    public String get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (isExpired(entry)) {
            cache.remove(key);
            return null;
        }
        return entry.value;
    }

    public void put(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        if (cache.size() >= maxSize) {
            cleanup(maxSize / 10);
        }
        cache.put(key, new CacheEntry(value));
    }

    public String getOrCompute(String key, Supplier<String> supplier) {
        String cached = get(key);
        if (cached != null) {
            return cached;
        }
        String computed = supplier.get();
        put(key, computed);
        return computed;
    }

    public void clear() {
        cache.clear();
    }

    private boolean isExpired(CacheEntry entry) {
        return System.currentTimeMillis() - entry.createTime > expireMillis;
    }

    private void cleanup(int maxRemove) {
        int removed = 0;
        Iterator<Map.Entry<String, CacheEntry>> iterator = cache.entrySet().iterator();
        while (iterator.hasNext() && removed < maxRemove) {
            iterator.next();
            iterator.remove();
            removed++;
        }
    }
}
