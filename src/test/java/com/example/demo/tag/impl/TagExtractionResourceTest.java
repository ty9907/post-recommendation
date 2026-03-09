package com.example.demo.tag.impl;

import com.example.demo.tag.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 标签提取资源文件测试类
 * 测试从新增的资源文件中提取标签的功能
 */
@DisplayName("标签提取资源文件测试")
public class TagExtractionResourceTest {

    private static final Logger logger = LoggerFactory.getLogger(TagExtractionResourceTest.class);
    
    private HanLPTagExtractor hanlpExtractor;
    private IKAnalyzerTagExtractor ikExtractor;
    private SimpleTagExtractor simpleExtractor;

    @BeforeEach
    void setUp() {
        hanlpExtractor = new HanLPTagExtractor();
        ikExtractor = new IKAnalyzerTagExtractor();
        simpleExtractor = new SimpleTagExtractor();
    }

    private String loadResourceContent(String resourcePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Resource file not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + resourcePath, e);
        }
    }

    @Test
    @DisplayName("测试Spring Boot文章标签提取 - HanLP")
    void testSpringBootArticle_HanLP() {
        String content = loadResourceContent("tag-extraction/spring-boot-guide.txt");
        
        List<Tag> tags = hanlpExtractor.extractTags(content, 10);
        
        assertNotNull(tags, "标签列表不应为空");
        assertFalse(tags.isEmpty(), "应提取到标签");
        assertTrue(tags.size() <= 10, "标签数量不应超过限制");
        
        boolean hasSpringBootTag = tags.stream()
                .anyMatch(tag -> tag.getName().toLowerCase().contains("spring") || 
                        tag.getName().toLowerCase().contains("boot"));
        assertTrue(hasSpringBootTag, "应包含Spring Boot相关标签");
        
        logger.info("=== Spring Boot文章标签提取 (HanLP) ===");
        tags.forEach(tag -> logger.info("{} (权重: {}, 频率: {})", 
                tag.getName(), String.format("%.4f", tag.getWeight()), tag.getFrequency()));
    }

    @Test
    @DisplayName("测试Spring Boot文章标签提取 - IKAnalyzer")
    void testSpringBootArticle_IKAnalyzer() {
        String content = loadResourceContent("tag-extraction/spring-boot-guide.txt");
        
        List<Tag> tags = ikExtractor.extractTags(content, 10);
        
        assertNotNull(tags, "标签列表不应为空");
        assertFalse(tags.isEmpty(), "应提取到标签");
        
        logger.info("=== Spring Boot文章标签提取 (IKAnalyzer) ===");
        tags.forEach(tag -> logger.info("{} (权重: {}, 频率: {})", 
                tag.getName(), String.format("%.4f", tag.getWeight()), tag.getFrequency()));
    }

    @Test
    @DisplayName("测试微服务架构文章标签提取 - HanLP")
    void testMicroserviceArticle_HanLP() {
        String content = loadResourceContent("tag-extraction/microservice-architecture.txt");
        
        List<Tag> tags = hanlpExtractor.extractTags(content, 15);
        
        assertNotNull(tags, "标签列表不应为空");
        assertFalse(tags.isEmpty(), "应提取到标签");
        
        boolean hasMicroserviceTag = tags.stream()
                .anyMatch(tag -> tag.getName().contains("微服务") || 
                        tag.getName().contains("服务") ||
                        tag.getName().contains("架构"));
        assertTrue(hasMicroserviceTag, "应包含微服务相关标签");
        
        logger.info("=== 微服务架构文章标签提取 (HanLP) ===");
        tags.forEach(tag -> logger.info("{} (权重: {}, 频率: {})", 
                tag.getName(), String.format("%.4f", tag.getWeight()), tag.getFrequency()));
    }

    @Test
    @DisplayName("测试微服务架构文章标签提取 - IKAnalyzer")
    void testMicroserviceArticle_IKAnalyzer() {
        String content = loadResourceContent("tag-extraction/microservice-architecture.txt");
        
        List<Tag> tags = ikExtractor.extractTags(content, 15);
        
        assertNotNull(tags, "标签列表不应为空");
        assertFalse(tags.isEmpty(), "应提取到标签");
        
        logger.info("=== 微服务架构文章标签提取 (IKAnalyzer) ===");
        tags.forEach(tag -> logger.info("{} (权重: {}, 频率: {})", 
                tag.getName(), String.format("%.4f", tag.getWeight()), tag.getFrequency()));
    }

    @Test
    @DisplayName("测试数据库优化文章标签提取 - HanLP")
    void testDatabaseOptimizationArticle_HanLP() {
        String content = loadResourceContent("tag-extraction/database-optimization.txt");
        
        List<Tag> tags = hanlpExtractor.extractTags(content, 12);
        
        assertNotNull(tags, "标签列表不应为空");
        assertFalse(tags.isEmpty(), "应提取到标签");
        
        boolean hasDatabaseTag = tags.stream()
                .anyMatch(tag -> tag.getName().contains("数据库") || 
                        tag.getName().contains("索引") ||
                        tag.getName().contains("SQL") ||
                        tag.getName().contains("查询"));
        assertTrue(hasDatabaseTag, "应包含数据库相关标签");
        
        logger.info("=== 数据库优化文章标签提取 (HanLP) ===");
        tags.forEach(tag -> logger.info("{} (权重: {}, 频率: {})", 
                tag.getName(), String.format("%.4f", tag.getWeight()), tag.getFrequency()));
    }

    @Test
    @DisplayName("测试数据库优化文章标签提取 - IKAnalyzer")
    void testDatabaseOptimizationArticle_IKAnalyzer() {
        String content = loadResourceContent("tag-extraction/database-optimization.txt");
        
        List<Tag> tags = ikExtractor.extractTags(content, 12);
        
        assertNotNull(tags, "标签列表不应为空");
        assertFalse(tags.isEmpty(), "应提取到标签");
        
        logger.info("=== 数据库优化文章标签提取 (IKAnalyzer) ===");
        tags.forEach(tag -> logger.info("{} (权重: {}, 频率: {})", 
                tag.getName(), String.format("%.4f", tag.getWeight()), tag.getFrequency()));
    }

    @Test
    @DisplayName("测试不同提取器结果对比")
    void testCompareExtractors() {
        String content = loadResourceContent("tag-extraction/spring-boot-guide.txt");
        
        List<Tag> hanlpTags = hanlpExtractor.extractTags(content, 10);
        List<Tag> ikTags = ikExtractor.extractTags(content, 10);
        List<Tag> simpleTags = simpleExtractor.extractTags(content, 10);
        
        assertNotNull(hanlpTags, "HanLP标签不应为空");
        assertNotNull(ikTags, "IKAnalyzer标签不应为空");
        assertNotNull(simpleTags, "Simple标签不应为空");
        
        logger.info("=== 提取器结果对比 ===");
        logger.info("HanLP提取标签数: {}", hanlpTags.size());
        logger.info("IKAnalyzer提取标签数: {}", ikTags.size());
        logger.info("Simple提取标签数: {}", simpleTags.size());
        
        assertFalse(hanlpTags.isEmpty() && ikTags.isEmpty() && simpleTags.isEmpty(), 
                "至少有一个提取器应提取到标签");
    }

    @Test
    @DisplayName("测试标签权重排序")
    void testTagWeightOrdering() {
        String content = loadResourceContent("tag-extraction/microservice-architecture.txt");
        
        List<Tag> tags = hanlpExtractor.extractTags(content, 20);
        
        for (int i = 0; i < tags.size() - 1; i++) {
            assertTrue(tags.get(i).getWeight() >= tags.get(i + 1).getWeight(),
                    "标签应按权重降序排列");
        }
        
        logger.info("=== 标签权重排序验证 ===");
        for (int i = 0; i < Math.min(5, tags.size()); i++) {
            Tag tag = tags.get(i);
            logger.info("{}. {} (权重: {})", 
                    (i + 1), tag.getName(), String.format("%.4f", tag.getWeight()));
        }
    }

    @Test
    @DisplayName("测试标签频率有效性")
    void testTagFrequencyValidity() {
        String content = loadResourceContent("tag-extraction/database-optimization.txt");
        
        List<Tag> tags = hanlpExtractor.extractTags(content, 10);
        
        for (Tag tag : tags) {
            assertTrue(tag.getFrequency() > 0, "标签频率应大于0");
            assertTrue(tag.getFrequency() <= content.length(), 
                    "标签频率不应超过内容长度");
        }
        
        logger.info("=== 标签频率验证 ===");
        tags.forEach(tag -> logger.info("{}: 频率={}", tag.getName(), tag.getFrequency()));
    }

    @Test
    @DisplayName("测试长文本标签提取性能")
    void testLongTextExtractionPerformance() {
        String content = loadResourceContent("tag-extraction/spring-boot-guide.txt");
        
        long startTime = System.currentTimeMillis();
        List<Tag> tags = hanlpExtractor.extractTags(content, 15);
        long endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        
        assertNotNull(tags, "标签列表不应为空");
        assertTrue(duration < 5000, "标签提取应在5秒内完成");
        
        logger.info("=== 长文本标签提取性能 ===");
        logger.info("文本长度: {} 字符", content.length());
        logger.info("提取时间: {} ms", duration);
        logger.info("提取标签数: {}", tags.size());
    }

    @Test
    @DisplayName("测试所有标签提取资源文件")
    void testAllTagExtractionResources() {
        String[] resourceFiles = {
            "tag-extraction/spring-boot-guide.txt",
            "tag-extraction/microservice-architecture.txt",
            "tag-extraction/database-optimization.txt"
        };
        
        for (String resourceFile : resourceFiles) {
            String content = loadResourceContent(resourceFile);
            List<Tag> tags = hanlpExtractor.extractTags(content, 10);
            
            assertNotNull(tags, resourceFile + " 标签列表不应为空");
            assertFalse(tags.isEmpty(), resourceFile + " 应提取到标签");
            
            logger.info("=== {} ===", resourceFile);
            logger.info("内容长度: {} 字符", content.length());
            logger.info("提取标签数: {}", tags.size());
            if (!tags.isEmpty()) {
                logger.info("Top 3 标签: {}, {}, {}", 
                        tags.get(0).getName(),
                        tags.size() > 1 ? tags.get(1).getName() : "N/A",
                        tags.size() > 2 ? tags.get(2).getName() : "N/A");
            }
        }
    }
}
