package com.example.demo.duplicate.service;

import com.example.demo.duplicate.model.SuspiciousCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 可疑案例审核服务
 * 
 * 管理可疑抄袭案例的人工审核流程。
 * 
 * 功能：
 * 1. 创建/更新可疑案例
 * 2. 审核案例（确认/误报/驳回）
 * 3. 查询待审核案例
 * 4. 优先级管理
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-06
 */
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final Map<Long, SuspiciousCase> cases = new ConcurrentHashMap<>();

    private final Map<SuspiciousCase.CaseStatus, Set<Long>> statusIndex = new ConcurrentHashMap<>();

    private final Map<SuspiciousCase.CasePriority, Set<Long>> priorityIndex = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(1);

    private final WhitelistService whitelistService;

    public ReviewService() {
        this(null);
    }

    public ReviewService(WhitelistService whitelistService) {
        this.whitelistService = whitelistService;
        initIndexes();
        logger.info("可疑案例审核服务初始化完成");
    }

    private void initIndexes() {
        for (SuspiciousCase.CaseStatus status : SuspiciousCase.CaseStatus.values()) {
            statusIndex.put(status, ConcurrentHashMap.newKeySet());
        }
        for (SuspiciousCase.CasePriority priority : SuspiciousCase.CasePriority.values()) {
            priorityIndex.put(priority, ConcurrentHashMap.newKeySet());
        }
    }

    /**
     * 创建可疑案例
     * 
     * @param articleId1 文章ID1
     * @param articleId2 文章ID2
     * @param similarity 相似度
     * @param algorithm 算法名称
     * @return 可疑案例
     */
    public SuspiciousCase createCase(Long articleId1, Long articleId2, double similarity, String algorithm) {
        Optional<SuspiciousCase> existingCase = findCase(articleId1, articleId2);
        if (existingCase.isPresent()) {
            logger.debug("案例已存在：文章 {} 和 {}", articleId1, articleId2);
            return existingCase.get();
        }

        SuspiciousCase suspiciousCase = new SuspiciousCase(articleId1, articleId2, similarity, algorithm);
        Long id = generateId();
        suspiciousCase.setId(id);
        
        SuspiciousCase.CasePriority priority = determinePriority(similarity);
        suspiciousCase.setPriority(priority);
        
        cases.put(id, suspiciousCase);
        statusIndex.get(suspiciousCase.getStatus()).add(id);
        priorityIndex.get(priority).add(id);
        
        logger.info("创建可疑案例：ID={}，文章 {} 和 {}，相似度={}，优先级={}", 
                id, articleId1, articleId2, similarity, priority);
        
        return suspiciousCase;
    }

    /**
     * 标记案例
     * 
     * @param id 案例ID
     * @param markedBy 标记人
     * @param priority 优先级
     * @return 是否成功
     */
    public boolean markCase(Long id, String markedBy, SuspiciousCase.CasePriority priority) {
        SuspiciousCase suspiciousCase = cases.get(id);
        if (suspiciousCase == null) {
            return false;
        }
        
        SuspiciousCase.CasePriority oldPriority = suspiciousCase.getPriority();
        suspiciousCase.setMarkedBy(markedBy);
        suspiciousCase.setPriority(priority);
        
        if (!oldPriority.equals(priority)) {
            priorityIndex.get(oldPriority).remove(id);
            priorityIndex.get(priority).add(id);
        }
        
        logger.info("标记案例：ID={}，标记人={}，优先级={}", id, markedBy, priority);
        return true;
    }

    /**
     * 开始审核
     * 
     * @param id 案例ID
     * @param reviewer 审核人
     * @return 是否成功
     */
    public boolean startReview(Long id, String reviewer) {
        SuspiciousCase suspiciousCase = cases.get(id);
        if (suspiciousCase == null || !suspiciousCase.needsReview()) {
            return false;
        }
        
        updateStatusIndex(id, suspiciousCase.getStatus(), SuspiciousCase.CaseStatus.IN_REVIEW);
        suspiciousCase.setStatus(SuspiciousCase.CaseStatus.IN_REVIEW);
        
        logger.info("开始审核案例：ID={}，审核人={}", id, reviewer);
        return true;
    }

    /**
     * 确认为抄袭
     * 
     * @param id 案例ID
     * @param reviewer 审核人
     * @param note 备注
     * @return 是否成功
     */
    public boolean confirmPlagiarism(Long id, String reviewer, String note) {
        return reviewCase(id, SuspiciousCase.CaseStatus.CONFIRMED, reviewer, note);
    }

    /**
     * 标记为误报
     * 
     * @param id 案例ID
     * @param reviewer 审核人
     * @param note 备注
     * @param addToWhitelist 是否加入白名单
     * @return 是否成功
     */
    public boolean markAsFalsePositive(Long id, String reviewer, String note, boolean addToWhitelist) {
        boolean success = reviewCase(id, SuspiciousCase.CaseStatus.FALSE_POSITIVE, reviewer, note);
        
        if (success && addToWhitelist && whitelistService != null) {
            SuspiciousCase suspiciousCase = cases.get(id);
            whitelistService.addEntry(
                suspiciousCase.getArticleId1(),
                suspiciousCase.getArticleId2(),
                "误报自动加入白名单: " + note,
                reviewer
            );
            logger.info("已将文章对加入白名单：{} 和 {}", 
                    suspiciousCase.getArticleId1(), suspiciousCase.getArticleId2());
        }
        
        return success;
    }

    /**
     * 驳回案例
     * 
     * @param id 案例ID
     * @param reviewer 审核人
     * @param note 备注
     * @return 是否成功
     */
    public boolean dismissCase(Long id, String reviewer, String note) {
        return reviewCase(id, SuspiciousCase.CaseStatus.DISMISSED, reviewer, note);
    }

    /**
     * 审核案例
     * 
     * @param id 案例ID
     * @param status 新状态
     * @param reviewer 审核人
     * @param note 备注
     * @return 是否成功
     */
    private boolean reviewCase(Long id, SuspiciousCase.CaseStatus status, String reviewer, String note) {
        SuspiciousCase suspiciousCase = cases.get(id);
        if (suspiciousCase == null) {
            return false;
        }
        
        SuspiciousCase.CaseStatus oldStatus = suspiciousCase.getStatus();
        suspiciousCase.review(status, reviewer, note);
        
        updateStatusIndex(id, oldStatus, status);
        
        logger.info("审核案例：ID={}，状态={}，审核人={}", id, status, reviewer);
        return true;
    }

    /**
     * 查找案例
     * 
     * @param articleId1 文章ID1
     * @param articleId2 文章ID2
     * @return 可疑案例
     */
    public Optional<SuspiciousCase> findCase(Long articleId1, Long articleId2) {
        return cases.values().stream()
                .filter(c -> c.matches(articleId1, articleId2))
                .findFirst();
    }

    /**
     * 获取案例
     * 
     * @param id 案例ID
     * @return 可疑案例
     */
    public Optional<SuspiciousCase> getCase(Long id) {
        return Optional.ofNullable(cases.get(id));
    }

    /**
     * 获取待审核案例
     * 
     * @return 案例列表
     */
    public List<SuspiciousCase> getPendingCases() {
        return getCasesByStatus(SuspiciousCase.CaseStatus.PENDING);
    }

    /**
     * 获取审核中案例
     * 
     * @return 案例列表
     */
    public List<SuspiciousCase> getInReviewCases() {
        return getCasesByStatus(SuspiciousCase.CaseStatus.IN_REVIEW);
    }

    /**
     * 获取已确认案例
     * 
     * @return 案例列表
     */
    public List<SuspiciousCase> getConfirmedCases() {
        return getCasesByStatus(SuspiciousCase.CaseStatus.CONFIRMED);
    }

    /**
     * 获取误报案例
     * 
     * @return 案例列表
     */
    public List<SuspiciousCase> getFalsePositiveCases() {
        return getCasesByStatus(SuspiciousCase.CaseStatus.FALSE_POSITIVE);
    }

    /**
     * 按状态获取案例
     * 
     * @param status 状态
     * @return 案例列表
     */
    public List<SuspiciousCase> getCasesByStatus(SuspiciousCase.CaseStatus status) {
        Set<Long> ids = statusIndex.get(status);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        
        return ids.stream()
                .map(cases::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(SuspiciousCase::getSimilarity).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 按优先级获取案例
     * 
     * @param priority 优先级
     * @return 案例列表
     */
    public List<SuspiciousCase> getCasesByPriority(SuspiciousCase.CasePriority priority) {
        Set<Long> ids = priorityIndex.get(priority);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        
        return ids.stream()
                .map(cases::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(SuspiciousCase::getCreateTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 获取与指定文章相关的所有案例
     * 
     * @param articleId 文章ID
     * @return 案例列表
     */
    public List<SuspiciousCase> getCasesForArticle(Long articleId) {
        return cases.values().stream()
                .filter(c -> c.getArticleId1().equals(articleId) || c.getArticleId2().equals(articleId))
                .collect(Collectors.toList());
    }

    /**
     * 获取高优先级待审核案例
     * 
     * @return 案例列表
     */
    public List<SuspiciousCase> getHighPriorityPendingCases() {
        return getPendingCases().stream()
                .filter(c -> c.getPriority() == SuspiciousCase.CasePriority.HIGH || 
                            c.getPriority() == SuspiciousCase.CasePriority.CRITICAL)
                .collect(Collectors.toList());
    }

    /**
     * 获取统计信息
     * 
     * @return 统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total", cases.size());
        stats.put("pending", statusIndex.get(SuspiciousCase.CaseStatus.PENDING).size());
        stats.put("inReview", statusIndex.get(SuspiciousCase.CaseStatus.IN_REVIEW).size());
        stats.put("confirmed", statusIndex.get(SuspiciousCase.CaseStatus.CONFIRMED).size());
        stats.put("falsePositive", statusIndex.get(SuspiciousCase.CaseStatus.FALSE_POSITIVE).size());
        stats.put("dismissed", statusIndex.get(SuspiciousCase.CaseStatus.DISMISSED).size());
        
        stats.put("critical", priorityIndex.get(SuspiciousCase.CasePriority.CRITICAL).size());
        stats.put("high", priorityIndex.get(SuspiciousCase.CasePriority.HIGH).size());
        stats.put("medium", priorityIndex.get(SuspiciousCase.CasePriority.MEDIUM).size());
        stats.put("low", priorityIndex.get(SuspiciousCase.CasePriority.LOW).size());
        
        return stats;
    }

    /**
     * 删除案例
     * 
     * @param id 案例ID
     * @return 是否成功
     */
    public boolean deleteCase(Long id) {
        SuspiciousCase suspiciousCase = cases.remove(id);
        if (suspiciousCase != null) {
            statusIndex.get(suspiciousCase.getStatus()).remove(id);
            priorityIndex.get(suspiciousCase.getPriority()).remove(id);
            logger.info("删除案例：ID={}", id);
            return true;
        }
        return false;
    }

    /**
     * 清空所有案例
     */
    public void clear() {
        cases.clear();
        initIndexes();
        logger.info("清空所有案例");
    }

    /**
     * 更新状态索引
     */
    private void updateStatusIndex(Long id, SuspiciousCase.CaseStatus oldStatus, SuspiciousCase.CaseStatus newStatus) {
        if (!oldStatus.equals(newStatus)) {
            statusIndex.get(oldStatus).remove(id);
            statusIndex.get(newStatus).add(id);
        }
    }

    /**
     * 根据相似度确定优先级
     */
    private SuspiciousCase.CasePriority determinePriority(double similarity) {
        if (similarity >= 0.95) {
            return SuspiciousCase.CasePriority.CRITICAL;
        } else if (similarity >= 0.85) {
            return SuspiciousCase.CasePriority.HIGH;
        } else if (similarity >= 0.75) {
            return SuspiciousCase.CasePriority.MEDIUM;
        } else {
            return SuspiciousCase.CasePriority.LOW;
        }
    }

    /**
     * 生成唯一ID
     */
    private Long generateId() {
        return idGenerator.getAndIncrement();
    }
}
