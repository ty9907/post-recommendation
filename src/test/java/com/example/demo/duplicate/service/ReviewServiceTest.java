package com.example.demo.duplicate.service;

import com.example.demo.duplicate.model.SuspiciousCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReviewService 测试类
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-06
 */
@DisplayName("可疑案例审核服务测试")
class ReviewServiceTest {

    private ReviewService reviewService;
    private WhitelistService whitelistService;

    @BeforeEach
    void setUp() {
        whitelistService = new WhitelistService();
        reviewService = new ReviewService(whitelistService);
    }

    @Test
    @DisplayName("测试创建可疑案例")
    void testCreateCase() {
        SuspiciousCase suspiciousCase = reviewService.createCase(1L, 2L, 0.85, "SimHash");
        
        assertNotNull(suspiciousCase, "案例不应为空");
        assertNotNull(suspiciousCase.getId(), "ID不应为空");
        assertEquals(1L, suspiciousCase.getArticleId1(), "文章ID1应匹配");
        assertEquals(2L, suspiciousCase.getArticleId2(), "文章ID2应匹配");
        assertEquals(0.85, suspiciousCase.getSimilarity(), 0.001, "相似度应匹配");
        assertEquals("SimHash", suspiciousCase.getAlgorithm(), "算法应匹配");
        assertEquals(SuspiciousCase.CaseStatus.PENDING, suspiciousCase.getStatus(), "状态应为待审核");
    }

    @Test
    @DisplayName("测试创建重复案例 - 返回已存在案例")
    void testCreateCase_Duplicate() {
        SuspiciousCase case1 = reviewService.createCase(1L, 2L, 0.85, "SimHash");
        SuspiciousCase case2 = reviewService.createCase(1L, 2L, 0.90, "TFIDF");
        
        assertEquals(case1.getId(), case2.getId(), "应返回相同案例");
    }

    @Test
    @DisplayName("测试相似度决定优先级 - 关键")
    void testPriority_Critical() {
        SuspiciousCase suspiciousCase = reviewService.createCase(1L, 2L, 0.96, "SimHash");
        
        assertEquals(SuspiciousCase.CasePriority.CRITICAL, suspiciousCase.getPriority(), "相似度>=0.95应为关键优先级");
    }

    @Test
    @DisplayName("测试相似度决定优先级 - 高")
    void testPriority_High() {
        SuspiciousCase suspiciousCase = reviewService.createCase(1L, 2L, 0.88, "SimHash");
        
        assertEquals(SuspiciousCase.CasePriority.HIGH, suspiciousCase.getPriority(), "相似度>=0.85应为高优先级");
    }

    @Test
    @DisplayName("测试相似度决定优先级 - 中")
    void testPriority_Medium() {
        SuspiciousCase suspiciousCase = reviewService.createCase(1L, 2L, 0.78, "SimHash");
        
        assertEquals(SuspiciousCase.CasePriority.MEDIUM, suspiciousCase.getPriority(), "相似度>=0.75应为中优先级");
    }

    @Test
    @DisplayName("测试相似度决定优先级 - 低")
    void testPriority_Low() {
        SuspiciousCase suspiciousCase = reviewService.createCase(1L, 2L, 0.65, "SimHash");
        
        assertEquals(SuspiciousCase.CasePriority.LOW, suspiciousCase.getPriority(), "相似度<0.75应为低优先级");
    }

    @Test
    @DisplayName("测试标记案例")
    void testMarkCase() {
        SuspiciousCase suspiciousCase = reviewService.createCase(1L, 2L, 0.85, "SimHash");
        
        boolean result = reviewService.markCase(suspiciousCase.getId(), "admin", SuspiciousCase.CasePriority.HIGH);
        
        assertTrue(result, "应成功标记");
        assertEquals("admin", suspiciousCase.getMarkedBy(), "标记人应匹配");
        assertEquals(SuspiciousCase.CasePriority.HIGH, suspiciousCase.getPriority(), "优先级应更新");
    }

    @Test
    @DisplayName("测试开始审核")
    void testStartReview() {
        SuspiciousCase suspiciousCase = reviewService.createCase(1L, 2L, 0.85, "SimHash");
        
        boolean result = reviewService.startReview(suspiciousCase.getId(), "reviewer");
        
        assertTrue(result, "应成功开始审核");
        assertEquals(SuspiciousCase.CaseStatus.IN_REVIEW, suspiciousCase.getStatus(), "状态应为审核中");
    }

    @Test
    @DisplayName("测试确认抄袭")
    void testConfirmPlagiarism() {
        SuspiciousCase suspiciousCase = reviewService.createCase(1L, 2L, 0.85, "SimHash");
        
        boolean result = reviewService.confirmPlagiarism(suspiciousCase.getId(), "reviewer", "确认抄袭");
        
        assertTrue(result, "应成功确认");
        assertEquals(SuspiciousCase.CaseStatus.CONFIRMED, suspiciousCase.getStatus(), "状态应为已确认");
        assertEquals("reviewer", suspiciousCase.getReviewedBy(), "审核人应匹配");
        assertNotNull(suspiciousCase.getReviewTime(), "审核时间不应为空");
    }

    @Test
    @DisplayName("测试标记为误报 - 不加入白名单")
    void testMarkAsFalsePositive_NoWhitelist() {
        SuspiciousCase suspiciousCase = reviewService.createCase(1L, 2L, 0.85, "SimHash");
        
        boolean result = reviewService.markAsFalsePositive(suspiciousCase.getId(), "reviewer", "误报", false);
        
        assertTrue(result, "应成功标记");
        assertEquals(SuspiciousCase.CaseStatus.FALSE_POSITIVE, suspiciousCase.getStatus(), "状态应为误报");
        assertFalse(whitelistService.isWhitelisted(1L, 2L), "不应加入白名单");
    }

    @Test
    @DisplayName("测试标记为误报 - 加入白名单")
    void testMarkAsFalsePositive_WithWhitelist() {
        SuspiciousCase suspiciousCase = reviewService.createCase(1L, 2L, 0.85, "SimHash");
        
        boolean result = reviewService.markAsFalsePositive(suspiciousCase.getId(), "reviewer", "误报", true);
        
        assertTrue(result, "应成功标记");
        assertEquals(SuspiciousCase.CaseStatus.FALSE_POSITIVE, suspiciousCase.getStatus(), "状态应为误报");
        assertTrue(whitelistService.isWhitelisted(1L, 2L), "应加入白名单");
    }

    @Test
    @DisplayName("测试驳回案例")
    void testDismissCase() {
        SuspiciousCase suspiciousCase = reviewService.createCase(1L, 2L, 0.85, "SimHash");
        
        boolean result = reviewService.dismissCase(suspiciousCase.getId(), "reviewer", "驳回");
        
        assertTrue(result, "应成功驳回");
        assertEquals(SuspiciousCase.CaseStatus.DISMISSED, suspiciousCase.getStatus(), "状态应为已驳回");
    }

    @Test
    @DisplayName("测试查找案例")
    void testFindCase() {
        reviewService.createCase(1L, 2L, 0.85, "SimHash");
        
        Optional<SuspiciousCase> found = reviewService.findCase(1L, 2L);
        
        assertTrue(found.isPresent(), "应找到案例");
    }

    @Test
    @DisplayName("测试获取待审核案例")
    void testGetPendingCases() {
        reviewService.createCase(1L, 2L, 0.85, "SimHash");
        reviewService.createCase(3L, 4L, 0.75, "SimHash");
        SuspiciousCase confirmed = reviewService.createCase(5L, 6L, 0.95, "SimHash");
        reviewService.confirmPlagiarism(confirmed.getId(), "reviewer", "确认");
        
        List<SuspiciousCase> pending = reviewService.getPendingCases();
        
        assertEquals(2, pending.size(), "应有2个待审核案例");
    }

    @Test
    @DisplayName("测试获取高优先级待审核案例")
    void testGetHighPriorityPendingCases() {
        reviewService.createCase(1L, 2L, 0.65, "SimHash");
        reviewService.createCase(3L, 4L, 0.88, "SimHash");
        reviewService.createCase(5L, 6L, 0.96, "SimHash");
        
        List<SuspiciousCase> highPriority = reviewService.getHighPriorityPendingCases();
        
        assertEquals(2, highPriority.size(), "应有2个高优先级案例");
    }

    @Test
    @DisplayName("测试获取与文章相关的案例")
    void testGetCasesForArticle() {
        reviewService.createCase(1L, 2L, 0.85, "SimHash");
        reviewService.createCase(1L, 3L, 0.75, "SimHash");
        reviewService.createCase(4L, 5L, 0.95, "SimHash");
        
        List<SuspiciousCase> cases = reviewService.getCasesForArticle(1L);
        
        assertEquals(2, cases.size(), "应有2个相关案例");
    }

    @Test
    @DisplayName("测试获取统计信息")
    void testGetStatistics() {
        reviewService.createCase(1L, 2L, 0.85, "SimHash");
        reviewService.createCase(3L, 4L, 0.75, "SimHash");
        SuspiciousCase confirmed = reviewService.createCase(5L, 6L, 0.95, "SimHash");
        reviewService.confirmPlagiarism(confirmed.getId(), "reviewer", "确认");
        
        Map<String, Object> stats = reviewService.getStatistics();
        
        assertEquals(3, stats.get("total"), "总数应为3");
        assertEquals(2, stats.get("pending"), "待审核应为2");
        assertEquals(1, stats.get("confirmed"), "已确认应为1");
    }

    @Test
    @DisplayName("测试删除案例")
    void testDeleteCase() {
        SuspiciousCase suspiciousCase = reviewService.createCase(1L, 2L, 0.85, "SimHash");
        
        boolean result = reviewService.deleteCase(suspiciousCase.getId());
        
        assertTrue(result, "应成功删除");
        assertFalse(reviewService.findCase(1L, 2L).isPresent(), "删除后不应找到案例");
    }

    @Test
    @DisplayName("测试清空所有案例")
    void testClear() {
        reviewService.createCase(1L, 2L, 0.85, "SimHash");
        reviewService.createCase(3L, 4L, 0.75, "SimHash");
        
        reviewService.clear();
        
        assertEquals(0, reviewService.getStatistics().get("total"), "清空后应为0");
    }

    @Test
    @DisplayName("测试案例匹配")
    void testCaseMatches() {
        SuspiciousCase suspiciousCase = new SuspiciousCase(1L, 2L, 0.85, "SimHash");
        
        assertTrue(suspiciousCase.matches(1L, 2L), "应匹配");
        assertTrue(suspiciousCase.matches(2L, 1L), "顺序不影响匹配");
        assertFalse(suspiciousCase.matches(1L, 3L), "不应匹配");
    }

    @Test
    @DisplayName("测试案例是否需要审核")
    void testCaseNeedsReview() {
        SuspiciousCase suspiciousCase = reviewService.createCase(1L, 2L, 0.85, "SimHash");
        
        assertTrue(suspiciousCase.needsReview(), "待审核案例需要审核");
        
        reviewService.startReview(suspiciousCase.getId(), "reviewer");
        assertTrue(suspiciousCase.needsReview(), "审核中案例需要审核");
        
        reviewService.confirmPlagiarism(suspiciousCase.getId(), "reviewer", "确认");
        assertFalse(suspiciousCase.needsReview(), "已确认案例不需要审核");
    }
}
