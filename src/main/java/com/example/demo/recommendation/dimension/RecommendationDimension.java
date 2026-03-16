package com.example.demo.recommendation.dimension;

import com.example.demo.recommendation.model.RecommendationConfig;

/**
 * 推荐维度接口
 * 定义推荐系统中各个维度的计算规范
 */
public interface RecommendationDimension {

    /**
     * 获取维度名称
     * @return 维度名称
     */
    String getName();

    /**
     * 计算推荐分数
     * @param context 维度上下文，包含计算所需的各种数据
     * @return 维度计算结果
     */
    DimensionResult calculate(DimensionContext context);

    /**
     * 初始化维度
     * @param config 推荐配置
     */
    void initialize(RecommendationConfig config);
}
