package com.example.demo.duplicate.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 可疑案例
 * 
 * 用于存储被标记为可疑的抄袭案例，需要人工审核。
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-06
 */
public class SuspiciousCase {

    private Long id;

    private Long articleId1;

    private Long articleId2;

    private double similarity;

    private String algorithm;

    private CaseStatus status;

    private CasePriority priority;

    private String markedBy;

    private LocalDateTime createTime;

    private LocalDateTime reviewTime;

    private String reviewedBy;

    private String reviewNote;

    public enum CaseStatus {
        PENDING,
        IN_REVIEW,
        CONFIRMED,
        FALSE_POSITIVE,
        DISMISSED
    }

    public enum CasePriority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public SuspiciousCase() {
        this.createTime = LocalDateTime.now();
        this.status = CaseStatus.PENDING;
        this.priority = CasePriority.MEDIUM;
    }

    public SuspiciousCase(Long articleId1, Long articleId2, double similarity, String algorithm) {
        this();
        this.articleId1 = Math.min(articleId1, articleId2);
        this.articleId2 = Math.max(articleId1, articleId2);
        this.similarity = similarity;
        this.algorithm = algorithm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getArticleId1() {
        return articleId1;
    }

    public void setArticleId1(Long articleId1) {
        this.articleId1 = articleId1;
    }

    public Long getArticleId2() {
        return articleId2;
    }

    public void setArticleId2(Long articleId2) {
        this.articleId2 = articleId2;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus status) {
        this.status = status;
    }

    public CasePriority getPriority() {
        return priority;
    }

    public void setPriority(CasePriority priority) {
        this.priority = priority;
    }

    public String getMarkedBy() {
        return markedBy;
    }

    public void setMarkedBy(String markedBy) {
        this.markedBy = markedBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public boolean matches(Long id1, Long id2) {
        Long minId = Math.min(id1, id2);
        Long maxId = Math.max(id1, id2);
        return Objects.equals(this.articleId1, minId) && Objects.equals(this.articleId2, maxId);
    }

    public void review(CaseStatus status, String reviewedBy, String reviewNote) {
        this.status = status;
        this.reviewedBy = reviewedBy;
        this.reviewNote = reviewNote;
        this.reviewTime = LocalDateTime.now();
    }

    public boolean needsReview() {
        return status == CaseStatus.PENDING || status == CaseStatus.IN_REVIEW;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuspiciousCase that = (SuspiciousCase) o;
        return Objects.equals(articleId1, that.articleId1) &&
               Objects.equals(articleId2, that.articleId2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleId1, articleId2);
    }

    @Override
    public String toString() {
        return "SuspiciousCase{" +
                "id=" + id +
                ", articleId1=" + articleId1 +
                ", articleId2=" + articleId2 +
                ", similarity=" + similarity +
                ", status=" + status +
                ", priority=" + priority +
                '}';
    }
}
