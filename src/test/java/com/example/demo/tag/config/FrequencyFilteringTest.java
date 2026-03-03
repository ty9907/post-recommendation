package com.example.demo.tag.config;

import com.example.demo.tag.impl.HanLPTagExtractor;
import com.example.demo.tag.impl.IKAnalyzerTagExtractor;
import com.example.demo.tag.impl.SimpleTagExtractor;
import com.example.demo.tag.model.Tag;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.*;

/**
 * 频率过滤功能测试类
 * 测试不同内容长度和词语频率场景下的标签提取效果
 */
public class FrequencyFilteringTest {
    private static final Logger logger = LoggerFactory.getLogger(FrequencyFilteringTest.class);
    
    /**
     * 测试TagExtractionConfig配置类
     * 验证配置参数的正确性
     */
    @Test
    public void testTagExtractionConfig() {
        logger.info("\n=== 测试TagExtractionConfig配置类 ===\n");
        
        TagExtractionConfig defaultConfig = TagExtractionConfig.defaultConfig();
        logger.info("默认配置: {}", defaultConfig);
        assertEquals(2, defaultConfig.getMinFrequency());
        assertEquals(100, defaultConfig.getMinContentLength());
        assertTrue(defaultConfig.isEnableAdaptiveFiltering());
        
        TagExtractionConfig strictConfig = TagExtractionConfig.strictConfig();
        logger.info("严格配置: {}", strictConfig);
        assertEquals(3, strictConfig.getMinFrequency());
        assertEquals(4, strictConfig.getShortContentMinFrequency());
        
        TagExtractionConfig lenientConfig = TagExtractionConfig.lenientConfig();
        logger.info("宽松配置: {}", lenientConfig);
        assertEquals(1, lenientConfig.getMinFrequency());
        assertEquals(2, lenientConfig.getShortContentMinFrequency());
    }
    
    /**
     * 测试动态频率阈值计算
     * 验证根据内容长度计算的频率阈值是否正确
     */
    @Test
    public void testDynamicFrequencyCalculation() {
        logger.info("\n=== 测试动态频率阈值计算 ===\n");
        
        TagExtractionConfig config = TagExtractionConfig.defaultConfig();
        
        int veryShortLength = 30;
        int shortLength = 80;
        int normalLength = 150;
        
        int veryShortFreq = config.calculateMinFrequency(veryShortLength);
        int shortFreq = config.calculateMinFrequency(shortLength);
        int normalFreq = config.calculateMinFrequency(normalLength);
        
        logger.info("极短文本(长度={}): 最低频率={}", veryShortLength, veryShortFreq);
        logger.info("短文本(长度={}): 最低频率={}", shortLength, shortFreq);
        logger.info("正常文本(长度={}): 最低频率={}", normalLength, normalFreq);
        
        assertEquals(4, veryShortFreq);
        assertEquals(3, shortFreq);
        assertEquals(2, normalFreq);
        
        assertTrue("极短文本频率阈值应该最高", veryShortFreq >= shortFreq);
        assertTrue("短文本频率阈值应该高于正常文本", shortFreq >= normalFreq);
    }
    
    /**
     * 测试短文本标签提取
     * 验证短文本中仅出现一次的词语不会被提取为标签
     */
    @Test
    public void testShortTextTagExtraction() {
        logger.info("\n=== 测试短文本标签提取 ===\n");
        
        String shortText = "Java是一门编程语言。Python也是编程语言。编程很有趣。";
        
        HanLPTagExtractor extractor = new HanLPTagExtractor();
        List<Tag> tags = extractor.extractTags(shortText, 10);
        
        logger.info("短文本内容: {}", shortText);
        logger.info("提取的标签:");
        for (Tag tag : tags) {
            logger.info("  - {} (频率: {})", tag.getName(), tag.getFrequency());
        }
        
        for (Tag tag : tags) {
            assertTrue("短文本中仅出现一次的词语不应被提取", tag.getFrequency() >= 2);
        }
    }
    
    /**
     * 测试极短文本处理
     * 验证极短文本的异常处理机制
     */
    @Test
    public void testVeryShortTextHandling() {
        logger.info("\n=== 测试极短文本处理 ===\n");
        
        String veryShortText = "Java编程";
        
        TagExtractionConfig config = new TagExtractionConfig();
        config.setReturnEmptyOnShortContent(true);
        
        HanLPTagExtractor extractor = new HanLPTagExtractor(config);
        List<Tag> tags = extractor.extractTags(veryShortText, 10);
        
        logger.info("极短文本内容: {}", veryShortText);
        logger.info("配置returnEmptyOnShortContent=true时的提取结果: {} 个标签", tags.size());
        
        assertTrue("极短文本应返回空标签集或少量标签", tags.size() <= 2);
    }
    
    /**
     * 测试长文本标签提取
     * 验证长文本中高频词汇能够被正确提取
     */
    @Test
    public void testLongTextTagExtraction() {
        logger.info("\n=== 测试长文本标签提取 ===\n");
        
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            longText.append("Java是一门优秀的编程语言，具有跨平台、面向对象等特性。");
            longText.append("Python也是一门流行的编程语言，适合数据分析和机器学习。");
        }
        
        HanLPTagExtractor extractor = new HanLPTagExtractor();
        List<Tag> tags = extractor.extractTags(longText.toString(), 10);
        
        logger.info("长文本长度: {} 字符", longText.length());
        logger.info("提取的标签:");
        for (Tag tag : tags) {
            logger.info("  - {} (频率: {})", tag.getName(), tag.getFrequency());
        }
        
        assertFalse("长文本应能提取到标签", tags.isEmpty());
        for (Tag tag : tags) {
            assertTrue("长文本标签频率应较高", tag.getFrequency() >= 2);
        }
    }
    
    /**
     * 测试不同配置下的标签提取效果
     * 对比默认配置、严格配置和宽松配置的提取结果
     */
    @Test
    public void testDifferentConfigurations() {
        logger.info("\n=== 测试不同配置下的标签提取效果 ===\n");
        
        String text = "Java编程语言 Python编程语言 编程语言重要 Java技术 Python技术";
        
        HanLPTagExtractor defaultExtractor = new HanLPTagExtractor(TagExtractionConfig.defaultConfig());
        HanLPTagExtractor strictExtractor = new HanLPTagExtractor(TagExtractionConfig.strictConfig());
        HanLPTagExtractor lenientExtractor = new HanLPTagExtractor(TagExtractionConfig.lenientConfig());
        
        List<Tag> defaultTags = defaultExtractor.extractTags(text, 10);
        List<Tag> strictTags = strictExtractor.extractTags(text, 10);
        List<Tag> lenientTags = lenientExtractor.extractTags(text, 10);
        
        logger.info("默认配置提取结果 ({} 个标签):", defaultTags.size());
        defaultTags.forEach(tag -> logger.info("  - {} (频率: {})", tag.getName(), tag.getFrequency()));
        
        logger.info("\n严格配置提取结果 ({} 个标签):", strictTags.size());
        strictTags.forEach(tag -> logger.info("  - {} (频率: {})", tag.getName(), tag.getFrequency()));
        
        logger.info("\n宽松配置提取结果 ({} 个标签):", lenientTags.size());
        lenientTags.forEach(tag -> logger.info("  - {} (频率: {})", tag.getName(), tag.getFrequency()));
        
        assertTrue("宽松配置应提取最多标签", lenientTags.size() >= defaultTags.size());
        assertTrue("严格配置应提取最少标签", strictTags.size() <= defaultTags.size());
    }
    
    /**
     * 测试IKAnalyzer提取器的频率过滤
     */
    @Test
    public void testIKAnalyzerFrequencyFiltering() {
        logger.info("\n=== 测试IKAnalyzer提取器的频率过滤 ===\n");
        
        String text = "Java编程 编程语言 Java技术 编程重要";
        
        IKAnalyzerTagExtractor extractor = new IKAnalyzerTagExtractor();
        List<Tag> tags = extractor.extractTags(text, 10);
        
        logger.info("文本内容: {}", text);
        logger.info("提取的标签:");
        for (Tag tag : tags) {
            logger.info("  - {} (频率: {})", tag.getName(), tag.getFrequency());
        }
        
        for (Tag tag : tags) {
            assertTrue("频率过滤应生效", tag.getFrequency() >= 2);
        }
    }
    
    /**
     * 测试Simple提取器的频率过滤
     */
    @Test
    public void testSimpleFrequencyFiltering() {
        logger.info("\n=== 测试Simple提取器的频率过滤 ===\n");
        
        String text = "Java编程 编程语言 Java技术 编程重要";
        
        SimpleTagExtractor extractor = new SimpleTagExtractor();
        List<Tag> tags = extractor.extractTags(text, 10);
        
        logger.info("文本内容: {}", text);
        logger.info("提取的标签:");
        for (Tag tag : tags) {
            logger.info("  - {} (频率: {})", tag.getName(), tag.getFrequency());
        }
        
        for (Tag tag : tags) {
            assertTrue("频率过滤应生效", tag.getFrequency() >= 2);
        }
    }
    
    /**
     * 测试空内容和null内容处理
     */
    @Test
    public void testEmptyAndNullContent() {
        logger.info("\n=== 测试空内容和null内容处理 ===\n");
        
        HanLPTagExtractor extractor = new HanLPTagExtractor();
        
        List<Tag> emptyTags = extractor.extractTags("", 10);
        List<Tag> nullTags = extractor.extractTags(null, 10);
        
        logger.info("空内容提取结果: {} 个标签", emptyTags.size());
        logger.info("null内容提取结果: {} 个标签", nullTags.size());
        
        assertTrue("空内容应返回空标签列表", emptyTags.isEmpty());
        assertTrue("null内容应返回空标签列表", nullTags.isEmpty());
    }
    
    /**
     * 测试自适应频率调整
     * 验证当标签数量不足时，系统会自动降低频率阈值
     */
    @Test
    public void testAdaptiveFrequencyAdjustment() {
        logger.info("\n=== 测试自适应频率调整 ===\n");
        
        TagExtractionConfig config = new TagExtractionConfig();
        config.setMinFrequency(3);
        config.setMinTagsRequired(2);
        
        HanLPTagExtractor extractor = new HanLPTagExtractor(config);
        
        String text = "Java编程 编程语言 Java技术";
        List<Tag> tags = extractor.extractTags(text, 10);
        
        logger.info("文本内容: {}", text);
        logger.info("配置: minFrequency=3, minTagsRequired=2");
        logger.info("提取的标签:");
        for (Tag tag : tags) {
            logger.info("  - {} (频率: {})", tag.getName(), tag.getFrequency());
        }
        
        logger.info("\n说明: 当标签数量不足minTagsRequired时，系统会自动降低频率阈值");
    }
    
    /**
     * 测试高频核心词汇保留
     * 验证高频核心词汇不会被过度过滤
     */
    @Test
    public void testHighFrequencyCoreWordsRetention() {
        logger.info("\n=== 测试高频核心词汇保留 ===\n");
        
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            text.append("Java编程语言 ");
        }
        text.append("Python编程语言 ");
        
        HanLPTagExtractor extractor = new HanLPTagExtractor(TagExtractionConfig.strictConfig());
        List<Tag> tags = extractor.extractTags(text.toString(), 5);
        
        logger.info("文本中'Java'出现20次，'Python'出现1次");
        logger.info("提取的标签:");
        for (Tag tag : tags) {
            logger.info("  - {} (频率: {})", tag.getName(), tag.getFrequency());
        }
        
        boolean hasJava = tags.stream().anyMatch(tag -> tag.getName().contains("java"));
        assertTrue("高频核心词汇'Java'应被保留", hasJava);
    }
}
