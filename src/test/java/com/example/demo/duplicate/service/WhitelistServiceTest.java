package com.example.demo.duplicate.service;

import com.example.demo.duplicate.model.WhitelistEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WhitelistService 测试类
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-06
 */
@DisplayName("白名单服务测试")
class WhitelistServiceTest {

    private WhitelistService whitelistService;

    @BeforeEach
    void setUp() {
        whitelistService = new WhitelistService();
    }

    @Test
    @DisplayName("测试添加白名单条目")
    void testAddEntry() {
        WhitelistEntry entry = whitelistService.addEntry(1L, 2L, "测试原因", "admin");
        
        assertNotNull(entry, "条目不应为空");
        assertNotNull(entry.getId(), "ID不应为空");
        assertEquals(1L, entry.getArticleId1(), "文章ID1应匹配");
        assertEquals(2L, entry.getArticleId2(), "文章ID2应匹配");
        assertEquals("测试原因", entry.getReason(), "原因应匹配");
        assertEquals("admin", entry.getAddedBy(), "添加人应匹配");
        assertTrue(entry.isActive(), "条目应处于活跃状态");
    }

    @Test
    @DisplayName("测试添加白名单条目 - ID自动排序")
    void testAddEntry_IdOrdering() {
        WhitelistEntry entry = whitelistService.addEntry(5L, 3L, "测试", "admin");
        
        assertEquals(3L, entry.getArticleId1(), "较小的ID应为articleId1");
        assertEquals(5L, entry.getArticleId2(), "较大的ID应为articleId2");
    }

    @Test
    @DisplayName("测试添加带过期时间的白名单条目")
    void testAddEntryWithExpiration() {
        WhitelistEntry entry = whitelistService.addEntryWithExpiration(1L, 2L, "测试", "admin", 24);
        
        assertNotNull(entry.getExpireTime(), "过期时间不应为空");
        assertTrue(entry.isActive(), "条目应处于活跃状态");
    }

    @Test
    @DisplayName("测试检查白名单 - 在白名单中")
    void testIsWhitelisted_True() {
        whitelistService.addEntry(1L, 2L, "测试", "admin");
        
        assertTrue(whitelistService.isWhitelisted(1L, 2L), "应在白名单中");
        assertTrue(whitelistService.isWhitelisted(2L, 1L), "顺序不影响结果");
    }

    @Test
    @DisplayName("测试检查白名单 - 不在白名单中")
    void testIsWhitelisted_False() {
        whitelistService.addEntry(1L, 2L, "测试", "admin");
        
        assertFalse(whitelistService.isWhitelisted(1L, 3L), "不应在白名单中");
        assertFalse(whitelistService.isWhitelisted(3L, 4L), "不应在白名单中");
    }

    @Test
    @DisplayName("测试移除白名单条目")
    void testRemoveEntry() {
        WhitelistEntry entry = whitelistService.addEntry(1L, 2L, "测试", "admin");
        
        assertTrue(whitelistService.removeEntry(entry.getId()), "应成功移除");
        assertFalse(whitelistService.isWhitelisted(1L, 2L), "移除后不应在白名单中");
    }

    @Test
    @DisplayName("测试移除不存在的条目")
    void testRemoveEntry_NotExists() {
        assertFalse(whitelistService.removeEntry(999L), "不应成功移除不存在的条目");
    }

    @Test
    @DisplayName("测试停用白名单条目")
    void testDeactivateEntry() {
        WhitelistEntry entry = whitelistService.addEntry(1L, 2L, "测试", "admin");
        
        assertTrue(whitelistService.deactivateEntry(entry.getId()), "应成功停用");
        assertFalse(whitelistService.isWhitelisted(1L, 2L), "停用后不应在白名单中");
    }

    @Test
    @DisplayName("测试获取白名单条目")
    void testGetEntry() {
        whitelistService.addEntry(1L, 2L, "测试", "admin");
        
        Optional<WhitelistEntry> found = whitelistService.getEntry(1L, 2L);
        
        assertTrue(found.isPresent(), "应找到条目");
        assertEquals("测试", found.get().getReason(), "原因应匹配");
    }

    @Test
    @DisplayName("测试获取所有活跃条目")
    void testGetActiveEntries() {
        whitelistService.addEntry(1L, 2L, "测试1", "admin");
        whitelistService.addEntry(3L, 4L, "测试2", "admin");
        
        List<WhitelistEntry> entries = whitelistService.getActiveEntries();
        
        assertEquals(2, entries.size(), "应有2个活跃条目");
    }

    @Test
    @DisplayName("测试获取与文章相关的条目")
    void testGetEntriesForArticle() {
        whitelistService.addEntry(1L, 2L, "测试1", "admin");
        whitelistService.addEntry(1L, 3L, "测试2", "admin");
        whitelistService.addEntry(4L, 5L, "测试3", "admin");
        
        List<WhitelistEntry> entries = whitelistService.getEntriesForArticle(1L);
        
        assertEquals(2, entries.size(), "应有2个相关条目");
    }

    @Test
    @DisplayName("测试条目数量")
    void testSize() {
        assertEquals(0, whitelistService.size(), "初始应为0");
        
        whitelistService.addEntry(1L, 2L, "测试", "admin");
        assertEquals(1, whitelistService.size(), "添加后应为1");
        
        whitelistService.addEntry(3L, 4L, "测试", "admin");
        assertEquals(2, whitelistService.size(), "添加后应为2");
    }

    @Test
    @DisplayName("测试清空白名单")
    void testClear() {
        whitelistService.addEntry(1L, 2L, "测试", "admin");
        whitelistService.addEntry(3L, 4L, "测试", "admin");
        
        whitelistService.clear();
        
        assertEquals(0, whitelistService.size(), "清空后应为0");
        assertFalse(whitelistService.isWhitelisted(1L, 2L), "清空后不应在白名单中");
    }

    @Test
    @DisplayName("测试条目匹配")
    void testEntryMatches() {
        WhitelistEntry entry = new WhitelistEntry(1L, 2L, "测试", "admin");
        
        assertTrue(entry.matches(1L, 2L), "应匹配");
        assertTrue(entry.matches(2L, 1L), "顺序不影响匹配");
        assertFalse(entry.matches(1L, 3L), "不应匹配");
    }
}
