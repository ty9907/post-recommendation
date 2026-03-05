package com.example.demo.duplicate.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 白名单条目
 * 
 * 用于存储被排除在抄袭检测之外的文章对。
 * 当两篇文章被加入白名单后，系统将不再将它们标记为重复。
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-06
 */
public class WhitelistEntry {

    private Long id;

    private Long articleId1;

    private Long articleId2;

    private String reason;

    private String addedBy;

    private LocalDateTime createTime;

    private LocalDateTime expireTime;

    private boolean active;

    public WhitelistEntry() {
        this.createTime = LocalDateTime.now();
        this.active = true;
    }

    public WhitelistEntry(Long articleId1, Long articleId2, String reason, String addedBy) {
        this();
        this.articleId1 = Math.min(articleId1, articleId2);
        this.articleId2 = Math.max(articleId1, articleId2);
        this.reason = reason;
        this.addedBy = addedBy;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isActive() {
        if (!active) {
            return false;
        }
        if (expireTime != null && LocalDateTime.now().isAfter(expireTime)) {
            return false;
        }
        return true;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean matches(Long id1, Long id2) {
        Long minId = Math.min(id1, id2);
        Long maxId = Math.max(id1, id2);
        return Objects.equals(this.articleId1, minId) && Objects.equals(this.articleId2, maxId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhitelistEntry that = (WhitelistEntry) o;
        return Objects.equals(articleId1, that.articleId1) &&
               Objects.equals(articleId2, that.articleId2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleId1, articleId2);
    }

    @Override
    public String toString() {
        return "WhitelistEntry{" +
                "id=" + id +
                ", articleId1=" + articleId1 +
                ", articleId2=" + articleId2 +
                ", reason='" + reason + '\'' +
                ", addedBy='" + addedBy + '\'' +
                ", active=" + active +
                '}';
    }
}
