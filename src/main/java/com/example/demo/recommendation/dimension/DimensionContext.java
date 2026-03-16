package com.example.demo.recommendation.dimension;

import com.example.demo.recommendation.model.BrowseHistory;
import com.example.demo.recommendation.model.PostTag;
import com.example.demo.recommendation.model.UserTag;

import java.util.ArrayList;
import java.util.List;

/**
 * 维度上下文数据模型类
 * 用于存储维度计算所需的上下文信息
 */
public class DimensionContext {
    private List<UserTag> userTags;             // 用户标签列表
    private List<PostTag> postTags;             // 帖子标签列表
    private List<BrowseHistory> browseHistory;  // 浏览历史列表
    private Long candidatePostId;               // 候选帖子ID
    private List<PostTag> candidatePostTags;    // 候选帖子标签

    /**
     * 默认构造器
     */
    public DimensionContext() {
        this.userTags = new ArrayList<>();
        this.postTags = new ArrayList<>();
        this.browseHistory = new ArrayList<>();
        this.candidatePostTags = new ArrayList<>();
    }

    /**
     * 带参数的构造器
     * @param userTags 用户标签列表
     * @param postTags 帖子标签列表
     * @param browseHistory 浏览历史列表
     * @param candidatePostId 候选帖子ID
     * @param candidatePostTags 候选帖子标签
     */
    public DimensionContext(List<UserTag> userTags, List<PostTag> postTags,
                            List<BrowseHistory> browseHistory, Long candidatePostId,
                            List<PostTag> candidatePostTags) {
        this.userTags = userTags != null ? userTags : new ArrayList<>();
        this.postTags = postTags != null ? postTags : new ArrayList<>();
        this.browseHistory = browseHistory != null ? browseHistory : new ArrayList<>();
        this.candidatePostId = candidatePostId;
        this.candidatePostTags = candidatePostTags != null ? candidatePostTags : new ArrayList<>();
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
        this.userTags = userTags != null ? userTags : new ArrayList<>();
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
        this.postTags = postTags != null ? postTags : new ArrayList<>();
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
        this.browseHistory = browseHistory != null ? browseHistory : new ArrayList<>();
    }

    /**
     * 获取候选帖子ID
     * @return 候选帖子ID
     */
    public Long getCandidatePostId() {
        return candidatePostId;
    }

    /**
     * 设置候选帖子ID
     * @param candidatePostId 候选帖子ID
     */
    public void setCandidatePostId(Long candidatePostId) {
        this.candidatePostId = candidatePostId;
    }

    /**
     * 获取候选帖子标签
     * @return 候选帖子标签
     */
    public List<PostTag> getCandidatePostTags() {
        return candidatePostTags;
    }

    /**
     * 设置候选帖子标签
     * @param candidatePostTags 候选帖子标签
     */
    public void setCandidatePostTags(List<PostTag> candidatePostTags) {
        this.candidatePostTags = candidatePostTags != null ? candidatePostTags : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "DimensionContext{" +
                "userTags=" + userTags +
                ", postTags=" + postTags +
                ", browseHistory=" + browseHistory +
                ", candidatePostId=" + candidatePostId +
                ", candidatePostTags=" + candidatePostTags +
                '}';
    }

    /**
     * 创建Builder实例
     * @return Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder模式实现
     */
    public static class Builder {
        private List<UserTag> userTags = new ArrayList<>();
        private List<PostTag> postTags = new ArrayList<>();
        private List<BrowseHistory> browseHistory = new ArrayList<>();
        private Long candidatePostId;
        private List<PostTag> candidatePostTags = new ArrayList<>();

        /**
         * 设置用户标签列表
         * @param userTags 用户标签列表
         * @return Builder实例
         */
        public Builder userTags(List<UserTag> userTags) {
            this.userTags = userTags != null ? userTags : new ArrayList<>();
            return this;
        }

        /**
         * 设置帖子标签列表
         * @param postTags 帖子标签列表
         * @return Builder实例
         */
        public Builder postTags(List<PostTag> postTags) {
            this.postTags = postTags != null ? postTags : new ArrayList<>();
            return this;
        }

        /**
         * 设置浏览历史列表
         * @param browseHistory 浏览历史列表
         * @return Builder实例
         */
        public Builder browseHistory(List<BrowseHistory> browseHistory) {
            this.browseHistory = browseHistory != null ? browseHistory : new ArrayList<>();
            return this;
        }

        /**
         * 设置候选帖子ID
         * @param candidatePostId 候选帖子ID
         * @return Builder实例
         */
        public Builder candidatePostId(Long candidatePostId) {
            this.candidatePostId = candidatePostId;
            return this;
        }

        /**
         * 设置候选帖子标签
         * @param candidatePostTags 候选帖子标签
         * @return Builder实例
         */
        public Builder candidatePostTags(List<PostTag> candidatePostTags) {
            this.candidatePostTags = candidatePostTags != null ? candidatePostTags : new ArrayList<>();
            return this;
        }

        /**
         * 构建DimensionContext实例
         * @return DimensionContext实例
         */
        public DimensionContext build() {
            return new DimensionContext(userTags, postTags, browseHistory, candidatePostId, candidatePostTags);
        }
    }
}
