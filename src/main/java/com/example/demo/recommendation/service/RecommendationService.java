package com.example.demo.recommendation.service;

import com.example.demo.recommendation.model.RecommendationRequest;
import com.example.demo.recommendation.model.RecommendationResult;

import java.util.List;
import java.util.Map;

/**
 * 推荐服务接口
 * 定义推荐系统的核心功能规范
 */
public interface RecommendationService {

    /**
     * 单次推荐
     * 根据推荐请求计算推荐结果
     * @param request 推荐请求
     * @return 推荐结果列表，按总分降序排列
     */
    List<RecommendationResult> recommend(RecommendationRequest request);

    /**
     * 批量推荐
     * 并行处理多个推荐请求
     * @param requests 请求映射，键为请求ID，值为推荐请求
     * @return 结果映射，键为请求ID，值为推荐结果列表
     */
    Map<String, List<RecommendationResult>> batchRecommend(Map<String, RecommendationRequest> requests);

    /**
     * 动态更新维度权重
     * @param weights 维度权重映射，键为维度名称，值为权重值
     */
    void updateDimensionWeights(Map<String, Double> weights);
}
