package com.example.demo.recommendation.dimension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DimensionManager测试类
 * 测试维度管理器的各种功能
 */
public class DimensionManagerTest {

    private DimensionManager dimensionManager;

    @BeforeEach
    public void setUp() {
        dimensionManager = new DimensionManager();
    }

    /**
     * 创建模拟的维度实例
     * @param name 维度名称
     * @return 模拟的维度实例
     */
    private RecommendationDimension createMockDimension(String name) {
        RecommendationDimension dimension = Mockito.mock(RecommendationDimension.class);
        when(dimension.getName()).thenReturn(name);
        return dimension;
    }

    /**
     * 测试注册维度
     * 验证维度能够正确注册到管理器
     */
    @Test
    public void testRegisterDimension() {
        RecommendationDimension dimension = createMockDimension("TEST_DIMENSION");

        dimensionManager.registerDimension("TEST_DIMENSION", dimension, 0.5);

        assertTrue(dimensionManager.containsDimension("TEST_DIMENSION"));
        assertEquals(1, dimensionManager.getDimensionCount());
        assertEquals(0.5, dimensionManager.getDimensionWeight("TEST_DIMENSION"), 0.001);
    }

    /**
     * 测试注册维度 - 空名称
     * 验证空名称抛出异常
     */
    @Test
    public void testRegisterDimensionWithEmptyName() {
        RecommendationDimension dimension = createMockDimension("TEST");

        assertThrows(IllegalArgumentException.class, () -> {
            dimensionManager.registerDimension("", dimension, 0.5);
        });
    }

    /**
     * 测试注册维度 - null名称
     * 验证null名称抛出异常
     */
    @Test
    public void testRegisterDimensionWithNullName() {
        RecommendationDimension dimension = createMockDimension("TEST");

        assertThrows(IllegalArgumentException.class, () -> {
            dimensionManager.registerDimension(null, dimension, 0.5);
        });
    }

    /**
     * 测试注册维度 - null维度实例
     * 验证null维度实例抛出异常
     */
    @Test
    public void testRegisterDimensionWithNullDimension() {
        assertThrows(IllegalArgumentException.class, () -> {
            dimensionManager.registerDimension("TEST", null, 0.5);
        });
    }

    /**
     * 测试注册维度 - 负权重
     * 验证负权重抛出异常
     */
    @Test
    public void testRegisterDimensionWithNegativeWeight() {
        RecommendationDimension dimension = createMockDimension("TEST");

        assertThrows(IllegalArgumentException.class, () -> {
            dimensionManager.registerDimension("TEST", dimension, -0.5);
        });
    }

    /**
     * 测试移除维度
     * 验证维度能够正确从管理器移除
     */
    @Test
    public void testRemoveDimension() {
        RecommendationDimension dimension = createMockDimension("TEST_DIMENSION");
        dimensionManager.registerDimension("TEST_DIMENSION", dimension, 0.5);

        RecommendationDimension removed = dimensionManager.removeDimension("TEST_DIMENSION");

        assertNotNull(removed);
        assertEquals(dimension, removed);
        assertFalse(dimensionManager.containsDimension("TEST_DIMENSION"));
        assertEquals(0, dimensionManager.getDimensionCount());
    }

    /**
     * 测试移除维度 - 不存在的维度
     * 验证移除不存在的维度返回null
     */
    @Test
    public void testRemoveDimensionNotExist() {
        RecommendationDimension removed = dimensionManager.removeDimension("NOT_EXIST");

        assertNull(removed);
    }

    /**
     * 测试设置维度权重
     * 验证权重能够正确设置
     */
    @Test
    public void testSetDimensionWeight() {
        RecommendationDimension dimension = createMockDimension("TEST_DIMENSION");
        dimensionManager.registerDimension("TEST_DIMENSION", dimension, 0.5);

        dimensionManager.setDimensionWeight("TEST_DIMENSION", 0.8);

        assertEquals(0.8, dimensionManager.getDimensionWeight("TEST_DIMENSION"), 0.001);
    }

    /**
     * 测试设置维度权重 - 负权重
     * 验证负权重抛出异常
     */
    @Test
    public void testSetDimensionWeightNegative() {
        RecommendationDimension dimension = createMockDimension("TEST_DIMENSION");
        dimensionManager.registerDimension("TEST_DIMENSION", dimension, 0.5);

        assertThrows(IllegalArgumentException.class, () -> {
            dimensionManager.setDimensionWeight("TEST_DIMENSION", -0.5);
        });
    }

    /**
     * 测试设置维度权重 - 不存在的维度
     * 验证设置不存在维度的权重抛出异常
     */
    @Test
    public void testSetDimensionWeightNotExist() {
        assertThrows(IllegalArgumentException.class, () -> {
            dimensionManager.setDimensionWeight("NOT_EXIST", 0.5);
        });
    }

    /**
     * 测试权重归一化
     * 验证权重归一化后总和为1
     */
    @Test
    public void testNormalizeWeights() {
        RecommendationDimension dimension1 = createMockDimension("DIM1");
        RecommendationDimension dimension2 = createMockDimension("DIM2");
        RecommendationDimension dimension3 = createMockDimension("DIM3");

        dimensionManager.registerDimension("DIM1", dimension1, 2.0);
        dimensionManager.registerDimension("DIM2", dimension2, 3.0);
        dimensionManager.registerDimension("DIM3", dimension3, 5.0);

        dimensionManager.normalizeWeights();

        double weight1 = dimensionManager.getDimensionWeight("DIM1");
        double weight2 = dimensionManager.getDimensionWeight("DIM2");
        double weight3 = dimensionManager.getDimensionWeight("DIM3");

        double totalWeight = weight1 + weight2 + weight3;

        assertEquals(1.0, totalWeight, 0.001);
        assertEquals(0.2, weight1, 0.001);
        assertEquals(0.3, weight2, 0.001);
        assertEquals(0.5, weight3, 0.001);
    }

    /**
     * 测试权重归一化 - 禁用的维度不参与
     * 验证禁用的维度不参与归一化
     */
    @Test
    public void testNormalizeWeightsWithDisabledDimension() {
        RecommendationDimension dimension1 = createMockDimension("DIM1");
        RecommendationDimension dimension2 = createMockDimension("DIM2");

        dimensionManager.registerDimension("DIM1", dimension1, 1.0);
        dimensionManager.registerDimension("DIM2", dimension2, 1.0);

        dimensionManager.setDimensionEnabled("DIM2", false);
        dimensionManager.normalizeWeights();

        double weight1 = dimensionManager.getDimensionWeight("DIM1");
        double weight2 = dimensionManager.getDimensionWeight("DIM2");

        assertEquals(1.0, weight1, 0.001);
        assertEquals(1.0, weight2, 0.001);
    }

    /**
     * 测试动态权重更新
     * 验证动态更新权重后归一化正确
     */
    @Test
    public void testDynamicWeightUpdate() {
        RecommendationDimension dimension1 = createMockDimension("DIM1");
        RecommendationDimension dimension2 = createMockDimension("DIM2");

        dimensionManager.registerDimension("DIM1", dimension1, 0.5);
        dimensionManager.registerDimension("DIM2", dimension2, 0.5);

        dimensionManager.normalizeWeights();

        assertEquals(0.5, dimensionManager.getDimensionWeight("DIM1"), 0.001);
        assertEquals(0.5, dimensionManager.getDimensionWeight("DIM2"), 0.001);

        dimensionManager.setDimensionWeight("DIM1", 0.8);
        dimensionManager.setDimensionWeight("DIM2", 0.2);
        dimensionManager.normalizeWeights();

        assertEquals(0.8, dimensionManager.getDimensionWeight("DIM1"), 0.001);
        assertEquals(0.2, dimensionManager.getDimensionWeight("DIM2"), 0.001);
    }

    /**
     * 测试获取维度实例
     */
    @Test
    public void testGetDimension() {
        RecommendationDimension dimension = createMockDimension("TEST_DIMENSION");
        dimensionManager.registerDimension("TEST_DIMENSION", dimension, 0.5);

        RecommendationDimension retrieved = dimensionManager.getDimension("TEST_DIMENSION");

        assertNotNull(retrieved);
        assertEquals(dimension, retrieved);
    }

    /**
     * 测试获取维度实例 - 不存在
     */
    @Test
    public void testGetDimensionNotExist() {
        RecommendationDimension retrieved = dimensionManager.getDimension("NOT_EXIST");

        assertNull(retrieved);
    }

    /**
     * 测试获取所有维度名称
     */
    @Test
    public void testGetDimensionNames() {
        RecommendationDimension dimension1 = createMockDimension("DIM1");
        RecommendationDimension dimension2 = createMockDimension("DIM2");

        dimensionManager.registerDimension("DIM1", dimension1, 0.5);
        dimensionManager.registerDimension("DIM2", dimension2, 0.5);

        assertEquals(2, dimensionManager.getDimensionNames().size());
        assertTrue(dimensionManager.getDimensionNames().contains("DIM1"));
        assertTrue(dimensionManager.getDimensionNames().contains("DIM2"));
    }

    /**
     * 测试获取所有维度实例
     */
    @Test
    public void testGetDimensions() {
        RecommendationDimension dimension1 = createMockDimension("DIM1");
        RecommendationDimension dimension2 = createMockDimension("DIM2");

        dimensionManager.registerDimension("DIM1", dimension1, 0.5);
        dimensionManager.registerDimension("DIM2", dimension2, 0.5);

        assertEquals(2, dimensionManager.getDimensions().size());
    }

    /**
     * 测试维度启用状态
     */
    @Test
    public void testDimensionEnabled() {
        RecommendationDimension dimension = createMockDimension("TEST_DIMENSION");
        dimensionManager.registerDimension("TEST_DIMENSION", dimension, 0.5);

        assertTrue(dimensionManager.isDimensionEnabled("TEST_DIMENSION"));

        dimensionManager.setDimensionEnabled("TEST_DIMENSION", false);

        assertFalse(dimensionManager.isDimensionEnabled("TEST_DIMENSION"));
    }

    /**
     * 测试获取权重配置
     */
    @Test
    public void testGetWeightConfig() {
        RecommendationDimension dimension = createMockDimension("TEST_DIMENSION");
        dimensionManager.registerDimension("TEST_DIMENSION", dimension, 0.5);

        DimensionWeightConfig config = dimensionManager.getWeightConfig("TEST_DIMENSION");

        assertNotNull(config);
        assertEquals("TEST_DIMENSION", config.getDimensionName());
        assertEquals(0.5, config.getWeight(), 0.001);
        assertTrue(config.isEnabled());
    }

    /**
     * 测试获取所有权重配置
     */
    @Test
    public void testGetAllWeightConfigs() {
        RecommendationDimension dimension1 = createMockDimension("DIM1");
        RecommendationDimension dimension2 = createMockDimension("DIM2");

        dimensionManager.registerDimension("DIM1", dimension1, 0.5);
        dimensionManager.registerDimension("DIM2", dimension2, 0.5);

        assertEquals(2, dimensionManager.getAllWeightConfigs().size());
    }

    /**
     * 测试清空所有维度
     */
    @Test
    public void testClear() {
        RecommendationDimension dimension1 = createMockDimension("DIM1");
        RecommendationDimension dimension2 = createMockDimension("DIM2");

        dimensionManager.registerDimension("DIM1", dimension1, 0.5);
        dimensionManager.registerDimension("DIM2", dimension2, 0.5);

        dimensionManager.clear();

        assertEquals(0, dimensionManager.getDimensionCount());
        assertFalse(dimensionManager.containsDimension("DIM1"));
        assertFalse(dimensionManager.containsDimension("DIM2"));
    }

    /**
     * 测试获取不存在的维度权重
     */
    @Test
    public void testGetDimensionWeightNotExist() {
        double weight = dimensionManager.getDimensionWeight("NOT_EXIST");

        assertEquals(0.0, weight, 0.001);
    }

    /**
     * 测试不存在的维度启用状态
     */
    @Test
    public void testIsDimensionEnabledNotExist() {
        assertFalse(dimensionManager.isDimensionEnabled("NOT_EXIST"));
    }
}
