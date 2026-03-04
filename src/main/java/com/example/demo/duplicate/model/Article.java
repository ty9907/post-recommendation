package com.example.demo.duplicate.model;

import com.example.demo.tag.model.Tag;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 文章数据模型类
 * 用于存储文章的基本信息和标签列表
 */
public class Article {
    private Long id;                    // 文章ID
    private String title;               // 文章标题
    private String content;             // 文章内容
    private List<Tag> tags;             // 文章标签列表
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间

    /**
     * 默认构造器
     */
    public Article() {
        this.tags = new ArrayList<>();
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 带参数的构造器
     * @param id 文章ID
     * @param title 文章标题
     * @param content 文章内容
     */
    public Article(Long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.tags = new ArrayList<>();
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 获取文章ID
     * @return 文章ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置文章ID
     * @param id 文章ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取文章标题
     * @return 文章标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置文章标题
     * @param title 文章标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取文章内容
     * @return 文章内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置文章内容
     * @param content 文章内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取文章标签列表
     * @return 标签列表
     */
    public List<Tag> getTags() {
        return tags;
    }

    /**
     * 设置文章标签列表
     * @param tags 标签列表
     */
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    /**
     * 获取创建时间
     * @return 创建时间
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     * @param createTime 创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取更新时间
     * @return 更新时间
     */
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更新时间
     * @param updateTime 更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + (content != null && content.length() > 50 ? content.substring(0, 50) + "..." : content) + '\'' +
                ", tags=" + tags +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
