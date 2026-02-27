package com.example.demo.tag.service;

import com.example.demo.tag.service.TagService;
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
 * TagService测试类
 * 测试标签提取服务的各种功能
 */
public class TagServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(TagServiceTest.class);
    
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
     * 测试默认构造器
     * 验证默认使用HanLP分词器
     */
    @Test
    public void testDefaultConstructor() {
        TagService service = new TagService();
        assertNotNull(service);
        assertEquals("HanLPTagExtractor", service.getExtractorName());
    }
    
    /**
     * 测试使用simple类型构造器
     * 验证正确使用SimpleTagExtractor
     */
    @Test
    public void testConstructorWithSimpleType() {
        TagService service = new TagService("simple");
        assertNotNull(service);
        assertEquals("SimpleTagExtractor", service.getExtractorName());
    }
    
    /**
     * 测试使用ik类型构造器
     * 验证正确使用IKAnalyzerTagExtractor
     */
    @Test
    public void testConstructorWithIKType() {
        TagService service = new TagService("ik");
        assertNotNull(service);
        assertEquals("IKAnalyzerTagExtractor", service.getExtractorName());
    }
    
    /**
     * 测试使用hanlp类型构造器
     * 验证正确使用HanLPTagExtractor
     */
    @Test
    public void testConstructorWithHanLPType() {
        TagService service = new TagService("hanlp");
        assertNotNull(service);
        assertEquals("HanLPTagExtractor", service.getExtractorName());
    }
    
    /**
     * 测试标签提取功能
     * 从资源文件加载文章内容并提取标签
     */
    @Test
    public void testExtractTags() {
        TagService service = new TagService("simple");
        String content = loadArticleContent("article-java.txt");
        
        List<Tag> tags = service.extractTags(content);
        
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        
        logger.info("=== 提取的标签 ===");
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            logger.info("{}. {} (权重: {}, 频率: {})", 
                    (i + 1), tag.getName(), String.format("%.4f", tag.getWeight()), tag.getFrequency());
        }
    }
    
    /**
     * 测试限制数量的标签提取功能
     * 验证提取的标签数量不超过限制值
     */
    @Test
    public void testExtractTagsWithLimit() {
        TagService service = new TagService("simple");
        String content = loadArticleContent("article-java.txt");
        
        List<Tag> tags = service.extractTags(content, 5);
        
        assertNotNull(tags);
        assertTrue(tags.size() <= 5);
        
        logger.info("=== 提取的标签 (限制5个) ===");
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
        TagService service = new TagService();
        List<Tag> tags = service.extractTags("");
        
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }
    
    /**
     * 测试null内容的标签提取
     * 验证返回空列表
     */
    @Test
    public void testExtractTagsWithNullContent() {
        TagService service = new TagService();
        List<Tag> tags = service.extractTags(null);
        
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }
}
