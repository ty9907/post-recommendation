package com.example.demo.tag.model;

/**
 * 标签数据模型类
 * 用于存储文章标签的名称、权重和出现频率
 */
public class Tag {
    private String name;        // 标签名称
    private double weight;      // 标签权重，表示标签在文章中的重要程度
    private int frequency;      // 出现频率，表示标签在文章中出现的次数

    /**
     * 默认构造器
     */
    public Tag() {
    }

    /**
     * 带参数的构造器
     * @param name 标签名称
     * @param weight 标签权重
     * @param frequency 出现频率
     */
    public Tag(String name, double weight, int frequency) {
        this.name = name;
        this.weight = weight;
        this.frequency = frequency;
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
     * 获取标签出现频率
     * @return 出现频率
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * 设置标签出现频率
     * @param frequency 出现频率
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "name='" + name + '\'' +
                ", weight=" + weight +
                ", frequency=" + frequency +
                '}';
    }
}