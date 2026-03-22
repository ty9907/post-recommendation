package com.example.demo.duplicate.risk;

import com.example.demo.duplicate.config.DuplicateCheckConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("风险评估器测试")
class RiskAssessorTest {

    @Test
    @DisplayName("应按阈值返回高风险")
    void shouldReturnHighRisk() {
        RiskAssessor riskAssessor = new RiskAssessor();
        assertEquals(RiskLevel.HIGH, riskAssessor.assess(0.9, DuplicateCheckConfig.defaultConfig()));
    }

    @Test
    @DisplayName("应按阈值返回中风险")
    void shouldReturnMediumRisk() {
        RiskAssessor riskAssessor = new RiskAssessor();
        assertEquals(RiskLevel.MEDIUM, riskAssessor.assess(0.5, DuplicateCheckConfig.defaultConfig()));
    }

    @Test
    @DisplayName("应按阈值返回低风险")
    void shouldReturnLowRisk() {
        RiskAssessor riskAssessor = new RiskAssessor();
        assertEquals(RiskLevel.LOW, riskAssessor.assess(0.2, DuplicateCheckConfig.defaultConfig()));
    }
}
