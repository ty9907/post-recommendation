package com.example.demo.duplicate.risk;

import com.example.demo.duplicate.config.DuplicateCheckConfig;
import com.example.demo.duplicate.model.SimilarityResult;

import java.util.List;

/**
 * 风险评估器。
 */
public class RiskAssessor {

    /**
     * 根据最高相似度评估风险等级。
     *
     * @param similarity 相似度
     * @param config 检测配置
     * @return 风险等级
     */
    public RiskLevel assess(double similarity, DuplicateCheckConfig config) {
        DuplicateCheckConfig useConfig = config != null ? config : DuplicateCheckConfig.defaultConfig();
        if (similarity >= useConfig.getHighRiskThreshold()) {
            return RiskLevel.HIGH;
        }
        if (similarity >= useConfig.getMediumRiskThreshold()) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    /**
     * 从相似度结果列表中评估风险等级。
     *
     * @param results 相似度结果
     * @param config 检测配置
     * @return 风险等级
     */
    public RiskLevel assess(List<SimilarityResult> results, DuplicateCheckConfig config) {
        if (results == null || results.isEmpty()) {
            return RiskLevel.LOW;
        }
        double maxSimilarity = results.stream()
                .mapToDouble(SimilarityResult::getSimilarity)
                .max()
                .orElse(0.0);
        return assess(maxSimilarity, config);
    }

    /**
     * 获取风险等级说明。
     *
     * @param riskLevel 风险等级
     * @return 说明文本
     */
    public String describe(RiskLevel riskLevel) {
        if (riskLevel == null) {
            return "未评估";
        }
        return switch (riskLevel) {
            case HIGH -> "高风险：建议阻断发布并进入人工审核";
            case MEDIUM -> "中风险：建议允许发布但加入待审核队列";
            case LOW -> "低风险：建议正常发布并异步复检";
        };
    }
}
