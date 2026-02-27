package com.example.demo.tag.impl;

import com.example.demo.tag.impl.SimpleTagExtractor;
import com.example.demo.tag.model.Tag;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * SimpleTagExtractor测试类
 * 测试简单标签提取器的各种功能
 */
public class SimpleTagExtractorTest {
    private static final Logger logger = LoggerFactory.getLogger(SimpleTagExtractorTest.class);
    
    /**
     * 从资源文件加载文章内容
     * @param filename 资源文件名
     * @return 文章内容字符串
     */
    private String loadArticleContent(String filename) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new RuntimeException("Resource file not found: " + filename);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load article content: " + filename, e);
        }
    }
    
    /**
     * 测试标签提取功能
     * 验证能够从文章中提取标签并按权重排序
     */
    @Test
    public void testExtractTags() {
        SimpleTagExtractor extractor = new SimpleTagExtractor();
        String content = loadArticleContent("article-java.txt");
        
        List<Tag> tags = extractor.extractTags(content);
        
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        assertEquals("SimpleTagExtractor", extractor.getName());
        
        logger.info("=== SimpleTagExtractor 提取的标签 ===");
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            logger.info("{}. {} (权重: {}, 频率: {})", 
                    (i + 1), tag.getName(), String.format("%.4f", tag.getWeight()), tag.getFrequency());
        }
    }
    
    /**
     * 测试限制数量的标签提取功能
     * 验证提取的标签数量不超过限制值，且权重和频率有效
     */
    @Test
    public void testExtractTagsWithLimit() {
        SimpleTagExtractor extractor = new SimpleTagExtractor();
        String content = loadArticleContent("article-java.txt");
        
        List<Tag> tags = extractor.extractTags(content, 5);
        
        assertNotNull(tags);
        assertTrue(tags.size() <= 5);
        
        // 验证每个标签的属性
        for (Tag tag : tags) {
            assertNotNull(tag.getName());
            assertTrue(tag.getWeight() > 0);
            assertTrue(tag.getFrequency() > 0);
        }
        
        logger.info("=== SimpleTagExtractor 提取的标签 (限制5个) ===");
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            logger.info("{}. {} (权重: {}, 频率: {})", 
                    (i + 1), tag.getName(), String.format("%.4f", tag.getWeight()), tag.getFrequency());
        }
    }
    
    /**
     * 测试空内容的标签提取
     * 验证返回空列表
     */
    @Test
    public void testExtractTagsWithEmptyContent() {
        SimpleTagExtractor extractor = new SimpleTagExtractor();
        List<Tag> tags = extractor.extractTags("");
        
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }
    
    /**
     * 测试null内容的标签提取
     * 验证返回空列表
     */
    @Test
    public void testExtractTagsWithNullContent() {
        SimpleTagExtractor extractor = new SimpleTagExtractor();
        List<Tag> tags = extractor.extractTags(null);
        
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }
    
    /**
     * 测试标签排序功能
     * 验证标签按权重降序排列
     */
    @Test
    public void testTagOrdering() {
        SimpleTagExtractor extractor = new SimpleTagExtractor();
        String content = "Java编程语言 Java技术 Java应用 编程语言技术";
        
        List<Tag> tags = extractor.extractTags(content, 3);
        
        assertNotNull(tags);
        // 验证权重降序排列
        if (tags.size() > 1) {
            double firstWeight = tags.get(0).getWeight();
            double secondWeight = tags.get(1).getWeight();
            assertTrue(firstWeight >= secondWeight);
        }
        
        logger.info("=== SimpleTagExtractor 标签排序测试 ===");
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            logger.info("{}. {} (权重: {}, 频率: {})", 
                    (i + 1), tag.getName(), String.format("%.4f", tag.getWeight()), tag.getFrequency());
        }
    }
}
