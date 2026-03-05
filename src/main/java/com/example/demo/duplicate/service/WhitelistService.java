package com.example.demo.duplicate.service;

import com.example.demo.duplicate.model.WhitelistEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 白名单服务
 * 
 * 管理文章对的白名单，被加入白名单的文章对不会被标记为抄袭。
 * 
 * 功能：
 * 1. 添加/移除白名单条目
 * 2. 检查文章对是否在白名单中
 * 3. 白名单过期管理
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-06
 */
public class WhitelistService {

    private static final Logger logger = LoggerFactory.getLogger(WhitelistService.class);

    private final Map<Long, WhitelistEntry> entries = new ConcurrentHashMap<>();

    private final Map<Long, Set<Long>> articlePairIndex = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(1);

    public WhitelistService() {
        logger.info("白名单服务初始化完成");
    }

    /**
     * 添加白名单条目
     * 
     * @param articleId1 文章ID1
     * @param articleId2 文章ID2
     * @param reason 原因
     * @param addedBy 添加人
     * @return 白名单条目
     */
    public WhitelistEntry addEntry(Long articleId1, Long articleId2, String reason, String addedBy) {
        WhitelistEntry entry = new WhitelistEntry(articleId1, articleId2, reason, addedBy);
        Long id = generateId();
        entry.setId(id);
        
        entries.put(id, entry);
        
        Long minId = Math.min(articleId1, articleId2);
        Long maxId = Math.max(articleId1, articleId2);
        articlePairIndex.computeIfAbsent(minId, k -> ConcurrentHashMap.newKeySet()).add(maxId);
        
        logger.info("添加白名单条目：文章 {} 和 {}，原因：{}，添加人：{}", 
                articleId1, articleId2, reason, addedBy);
        
        return entry;
    }

    /**
     * 添加带过期时间的白名单条目
     * 
     * @param articleId1 文章ID1
     * @param articleId2 文章ID2
     * @param reason 原因
     * @param addedBy 添加人
     * @param expireHours 过期小时数
     * @return 白名单条目
     */
    public WhitelistEntry addEntryWithExpiration(Long articleId1, Long articleId2, 
                                                   String reason, String addedBy, int expireHours) {
        WhitelistEntry entry = addEntry(articleId1, articleId2, reason, addedBy);
        entry.setExpireTime(LocalDateTime.now().plusHours(expireHours));
        logger.info("设置白名单条目过期时间：{} 小时后", expireHours);
        return entry;
    }

    /**
     * 移除白名单条目
     * 
     * @param id 条目ID
     * @return 是否成功
     */
    public boolean removeEntry(Long id) {
        WhitelistEntry entry = entries.remove(id);
        if (entry != null) {
            Long minId = entry.getArticleId1();
            Long maxId = entry.getArticleId2();
            Set<Long> pairs = articlePairIndex.get(minId);
            if (pairs != null) {
                pairs.remove(maxId);
                if (pairs.isEmpty()) {
                    articlePairIndex.remove(minId);
                }
            }
            logger.info("移除白名单条目：ID={}", id);
            return true;
        }
        return false;
    }

    /**
     * 停用白名单条目
     * 
     * @param id 条目ID
     * @return 是否成功
     */
    public boolean deactivateEntry(Long id) {
        WhitelistEntry entry = entries.get(id);
        if (entry != null) {
            entry.setActive(false);
            logger.info("停用白名单条目：ID={}", id);
            return true;
        }
        return false;
    }

    /**
     * 检查文章对是否在白名单中
     * 
     * @param articleId1 文章ID1
     * @param articleId2 文章ID2
     * @return 是否在白名单中
     */
    public boolean isWhitelisted(Long articleId1, Long articleId2) {
        Long minId = Math.min(articleId1, articleId2);
        Long maxId = Math.max(articleId1, articleId2);
        
        Set<Long> pairs = articlePairIndex.get(minId);
        if (pairs == null || !pairs.contains(maxId)) {
            return false;
        }
        
        return entries.values().stream()
                .filter(WhitelistEntry::isActive)
                .anyMatch(entry -> entry.matches(articleId1, articleId2));
    }

    /**
     * 获取白名单条目
     * 
     * @param articleId1 文章ID1
     * @param articleId2 文章ID2
     * @return 白名单条目（如果存在）
     */
    public Optional<WhitelistEntry> getEntry(Long articleId1, Long articleId2) {
        return entries.values().stream()
                .filter(entry -> entry.matches(articleId1, articleId2))
                .findFirst();
    }

    /**
     * 获取所有活跃的白名单条目
     * 
     * @return 白名单条目列表
     */
    public List<WhitelistEntry> getActiveEntries() {
        return entries.values().stream()
                .filter(WhitelistEntry::isActive)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有白名单条目
     * 
     * @return 白名单条目列表
     */
    public List<WhitelistEntry> getAllEntries() {
        return new ArrayList<>(entries.values());
    }

    /**
     * 获取与指定文章相关的所有白名单条目
     * 
     * @param articleId 文章ID
     * @return 白名单条目列表
     */
    public List<WhitelistEntry> getEntriesForArticle(Long articleId) {
        return entries.values().stream()
                .filter(entry -> entry.getArticleId1().equals(articleId) || 
                                entry.getArticleId2().equals(articleId))
                .collect(Collectors.toList());
    }

    /**
     * 清理过期条目
     * 
     * @return 清理的条目数量
     */
    public int cleanupExpiredEntries() {
        int count = 0;
        Iterator<Map.Entry<Long, WhitelistEntry>> iterator = entries.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<Long, WhitelistEntry> entry = iterator.next();
            WhitelistEntry whitelistEntry = entry.getValue();
            
            if (whitelistEntry.getExpireTime() != null && 
                LocalDateTime.now().isAfter(whitelistEntry.getExpireTime())) {
                iterator.remove();
                
                Long minId = whitelistEntry.getArticleId1();
                Long maxId = whitelistEntry.getArticleId2();
                Set<Long> pairs = articlePairIndex.get(minId);
                if (pairs != null) {
                    pairs.remove(maxId);
                    if (pairs.isEmpty()) {
                        articlePairIndex.remove(minId);
                    }
                }
                count++;
                logger.debug("清理过期白名单条目：ID={}", entry.getKey());
            }
        }
        
        if (count > 0) {
            logger.info("清理了 {} 个过期白名单条目", count);
        }
        
        return count;
    }

    /**
     * 获取白名单条目数量
     * 
     * @return 条目数量
     */
    public int size() {
        return entries.size();
    }

    /**
     * 获取活跃条目数量
     * 
     * @return 活跃条目数量
     */
    public int activeSize() {
        return (int) entries.values().stream()
                .filter(WhitelistEntry::isActive)
                .count();
    }

    /**
     * 清空所有白名单条目
     */
    public void clear() {
        entries.clear();
        articlePairIndex.clear();
        logger.info("清空所有白名单条目");
    }

    /**
     * 生成唯一ID
     * 
     * @return 唯一ID
     */
    private Long generateId() {
        return idGenerator.getAndIncrement();
    }
}
