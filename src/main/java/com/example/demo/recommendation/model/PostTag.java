package com.example.demo.recommendation.model;

/**
 * 帖子标签数据模型类
 * 用于存储帖子的标签信息
 */
public class PostTag {
    private String name;        // 标签名称
    private double weight;      // 标签权重，表示标签在帖子中的重要程度
    private Long postId;        // 帖子ID

    /**
     * 默认构造器
     */
    public PostTag() {
    }

    /**
     * 带参数的构造器
     * @param name 标签名称
     * @param weight 标签权重
     * @param postId 帖子ID
     */
    public PostTag(String name, double weight, Long postId) {
        this.name = name;
        this.weight = weight;
        this.postId = postId;
    }

    /**
     * 获取标签名称
     * @return 标签名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置标签名称
     * @param name 标签名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取标签权重
     * @return 标签权重
     */
    public double getWeight() {
        return weight;
    }

    /**
     * 设置标签权重
     * @param weight 标签权重
     */
    public void setWeight(double weight) {
        this.weight = weight;
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

    @Override
    public String toString() {
        return "PostTag{" +
                "name='" + name + '\'' +
                ", weight=" + weight +
                ", postId=" + postId +
                '}';
    }
}
