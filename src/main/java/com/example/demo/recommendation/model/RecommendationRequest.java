package com.example.demo.recommendation.model;

import java.util.List;
import java.util.Map;

/**
 * 推荐请求数据模型类
 * 用于封装推荐请求的所有参数
 */
public class RecommendationRequest {
    private List<UserTag> userTags;                 // 用户标签列表
    private List<PostTag> postTags;                 // 帖子标签列表
    private List<BrowseHistory> browseHistory;      // 浏览历史列表
    private RecommendationConfig config;            // 配置参数

    /**
     * 默认构造器
     */
    public RecommendationRequest() {
    }

    /**
     * 带参数的构造器
     * @param userTags 用户标签列表
     * @param postTags 帖子标签列表
     * @param browseHistory 浏览历史列表
     * @param config 配置参数
     */
    public RecommendationRequest(List<UserTag> userTags, List<PostTag> postTags,
                                 List<BrowseHistory> browseHistory, RecommendationConfig config) {
        this.userTags = userTags;
        this.postTags = postTags;
        this.browseHistory = browseHistory;
        this.config = config;
    }

    /**
     * 获取用户标签列表
     * @return 用户标签列表
     */
    public List<UserTag> getUserTags() {
        return userTags;
    }

    /**
     * 设置用户标签列表
     * @param userTags 用户标签列表
     */
    public void setUserTags(List<UserTag> userTags) {
        this.userTags = userTags;
    }

    /**
     * 获取帖子标签列表
     * @return 帖子标签列表
     */
    public List<PostTag> getPostTags() {
        return postTags;
    }

    /**
     * 设置帖子标签列表
     * @param postTags 帖子标签列表
     */
    public void setPostTags(List<PostTag> postTags) {
        this.postTags = postTags;
    }

    /**
     * 获取浏览历史列表
     * @return 浏览历史列表
     */
    public List<BrowseHistory> getBrowseHistory() {
        return browseHistory;
    }

    /**
     * 设置浏览历史列表
     * @param browseHistory 浏览历史列表
     */
    public void setBrowseHistory(List<BrowseHistory> browseHistory) {
        this.browseHistory = browseHistory;
    }

    /**
     * 获取配置参数
     * @return 配置参数
     */
    public RecommendationConfig getConfig() {
        return config;
    }

    /**
     * 设置配置参数
     * @param config 配置参数
     */
    public void setConfig(RecommendationConfig config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "RecommendationRequest{" +
                "userTags=" + userTags +
                ", postTags=" + postTags +
                ", browseHistory=" + browseHistory +
                ", config=" + config +
                '}';
    }
}
