package com.example.demo.recommendation.model;

import java.util.List;

/**
 * 浏览历史数据模型类
 * 用于存储用户的帖子浏览历史记录
 */
public class BrowseHistory {
    private Long postId;                // 帖子ID
    private Long browseTime;            // 浏览时间（时间戳）
    private List<PostTag> tags;         // 帖子标签列表

    /**
     * 默认构造器
     */
    public BrowseHistory() {
    }

    /**
     * 带参数的构造器
     * @param postId 帖子ID
     * @param browseTime 浏览时间
     * @param tags 帖子标签列表
     */
    public BrowseHistory(Long postId, Long browseTime, List<PostTag> tags) {
        this.postId = postId;
        this.browseTime = browseTime;
        this.tags = tags;
    }

    /**
     * 获取帖子ID
     * @return 帖子ID
     */
    public Long getPostId() {
        return postId;
    }

    /**
     * 设置帖子ID
     * @param postId 帖子ID
     */
    public void setPostId(Long postId) {
        this.postId = postId;
    }

    /**
     * 获取浏览时间
     * @return 浏览时间
     */
    public Long getBrowseTime() {
        return browseTime;
    }

    /**
     * 设置浏览时间
     * @param browseTime 浏览时间
     */
    public void setBrowseTime(Long browseTime) {
        this.browseTime = browseTime;
    }

    /**
     * 获取帖子标签列表
     * @return 帖子标签列表
     */
    public List<PostTag> getTags() {
        return tags;
    }

    /**
     * 设置帖子标签列表
     * @param tags 帖子标签列表
     */
    public void setTags(List<PostTag> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "BrowseHistory{" +
                "postId=" + postId +
                ", browseTime=" + browseTime +
                ", tags=" + tags +
                '}';
    }
}
