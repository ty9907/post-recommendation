package com.example.demo.recommendation.model;

/**
 * 用户标签数据模型类
 * 用于存储用户的标签偏好信息
 */
public class UserTag {
    private String name;        // 标签名称
    private double weight;      // 标签权重，表示用户对该标签的偏好程度
    private String type;        // 标签类型

    /**
     * 默认构造器
     */
    public UserTag() {
    }

    /**
     * 带参数的构造器
     * @param name 标签名称
     * @param weight 标签权重
     * @param type 标签类型
     */
    public UserTag(String name, double weight, String type) {
        this.name = name;
        this.weight = weight;
        this.type = type;
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
     * 获取标签类型
     * @return 标签类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置标签类型
     * @param type 标签类型
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "UserTag{" +
                "name='" + name + '\'' +
                ", weight=" + weight +
                ", type='" + type + '\'' +
                '}';
    }
}
