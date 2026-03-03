package com.example.demo.tag.util;

import com.example.demo.tag.impl.HanLPTagExtractor;
import com.example.demo.tag.impl.IKAnalyzerTagExtractor;
import com.example.demo.tag.impl.SimpleTagExtractor;
import com.example.demo.tag.model.Tag;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 标签提取优化效果测试类
 * 测试优化前后的标签提取质量对比
 */
public class TagExtractionOptimizationTest {
    private static final Logger logger = LoggerFactory.getLogger(TagExtractionOptimizationTest.class);
    
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
     * 测试HanLP标签提取器的优化效果
     * 对比优化前后的标签质量
     */
    @Test
    public void testHanLPOptimization() {
        logger.info("\n=== HanLP标签提取器优化效果测试 ===\n");
        
        HanLPTagExtractor extractor = new HanLPTagExtractor();
        String content = loadArticleContent("article-java.txt");
        
        List<String> expectedTags = Arrays.asList(
            "java", "编程", "语言", "面向对象", "跨平台", "虚拟机", "类", "对象",
            "继承", "多态", "封装", "开发", "应用", "技术", "特性", "系统"
        );
        
        List<Tag> extractedTags = extractor.extractTags(content, 15);
        
        logger.info("提取的标签：");
        for (int i = 0; i < extractedTags.size(); i++) {
            Tag tag = extractedTags.get(i);
            logger.info("  {}. {} (权重: {}, 频率: {})", 
                (i + 1), tag.getName(), 
                String.format("%.4f", tag.getWeight()), 
                tag.getFrequency());
        }
        
        TagQualityEvaluator.EvaluationResult result = 
            TagQualityEvaluator.evaluate(extractedTags, expectedTags);
        
        logger.info("\n{}", result.toString());
    }
    
    /**
     * 测试IKAnalyzer标签提取器的优化效果
     * 对比优化前后的标签质量
     */
    @Test
    public void testIKAnalyzerOptimization() {
        logger.info("\n=== IKAnalyzer标签提取器优化效果测试 ===\n");
        
        IKAnalyzerTagExtractor extractor = new IKAnalyzerTagExtractor();
        String content = loadArticleContent("article-java.txt");
        
        List<String> expectedTags = Arrays.asList(
            "java", "编程", "语言", "面向对象", "跨平台", "虚拟机", "类", "对象",
            "继承", "多态", "封装", "开发", "应用", "技术", "特性", "系统"
        );
        
        List<Tag> extractedTags = extractor.extractTags(content, 15);
        
        logger.info("提取的标签：");
        for (int i = 0; i < extractedTags.size(); i++) {
            Tag tag = extractedTags.get(i);
            logger.info("  {}. {} (权重: {}, 频率: {})", 
                (i + 1), tag.getName(), 
                String.format("%.4f", tag.getWeight()), 
                tag.getFrequency());
        }
        
        TagQualityEvaluator.EvaluationResult result = 
            TagQualityEvaluator.evaluate(extractedTags, expectedTags);
        
        logger.info("\n{}", result.toString());
    }
    
    /**
     * 测试Simple标签提取器的优化效果
     * 对比优化前后的标签质量
     */
    @Test
    public void testSimpleOptimization() {
        logger.info("\n=== Simple标签提取器优化效果测试 ===\n");
        
        SimpleTagExtractor extractor = new SimpleTagExtractor();
        String content = loadArticleContent("article-java.txt");
        
        List<String> expectedTags = Arrays.asList(
            "java", "编程", "语言", "面向对象", "跨平台", "虚拟机", "类", "对象",
            "继承", "多态", "封装", "开发", "应用", "技术", "特性", "系统"
        );
        
        List<Tag> extractedTags = extractor.extractTags(content, 15);
        
        logger.info("提取的标签：");
        for (int i = 0; i < extractedTags.size(); i++) {
            Tag tag = extractedTags.get(i);
            logger.info("  {}. {} (权重: {}, 频率: {})", 
                (i + 1), tag.getName(), 
                String.format("%.4f", tag.getWeight()), 
                tag.getFrequency());
        }
        
        TagQualityEvaluator.EvaluationResult result = 
            TagQualityEvaluator.evaluate(extractedTags, expectedTags);
        
        logger.info("\n{}", result.toString());
    }
    
    /**
     * 测试多个提取器的性能对比
     * 对比HanLP、IKAnalyzer和Simple三种提取器的效果
     */
    @Test
    public void testExtractorComparison() {
        logger.info("\n=== 多提取器性能对比测试 ===\n");
        
        String content = loadArticleContent("article-ai.txt");
        
        List<String> expectedTags = Arrays.asList(
            "人工智能", "机器学习", "深度学习", "神经网络", "算法", "数据",
            "模型", "训练", "应用", "技术", "计算机", "智能", "学习", "系统"
        );
        
        HanLPTagExtractor hanlpExtractor = new HanLPTagExtractor();
        IKAnalyzerTagExtractor ikExtractor = new IKAnalyzerTagExtractor();
        SimpleTagExtractor simpleExtractor = new SimpleTagExtractor();
        
        List<Tag> hanlpTags = hanlpExtractor.extractTags(content, 15);
        List<Tag> ikTags = ikExtractor.extractTags(content, 15);
        List<Tag> simpleTags = simpleExtractor.extractTags(content, 15);
        
        TagQualityEvaluator.EvaluationResult hanlpResult = 
            TagQualityEvaluator.evaluate(hanlpTags, expectedTags);
        TagQualityEvaluator.EvaluationResult ikResult = 
            TagQualityEvaluator.evaluate(ikTags, expectedTags);
        TagQualityEvaluator.EvaluationResult simpleResult = 
            TagQualityEvaluator.evaluate(simpleTags, expectedTags);
        
        logger.info("HanLP提取器结果：\n{}", hanlpResult.toString());
        logger.info("IKAnalyzer提取器结果：\n{}", ikResult.toString());
        logger.info("Simple提取器结果：\n{}", simpleResult.toString());
        
        logger.info("\n{}", TagQualityEvaluator.compareResults(
            "Simple", simpleResult, "HanLP", hanlpResult));
        logger.info("\n{}", TagQualityEvaluator.compareResults(
            "Simple", simpleResult, "IKAnalyzer", ikResult));
    }
    
    /**
     * 测试词性过滤效果
     * 验证形容词、量词等非核心词汇是否被正确过滤
     */
    @Test
    public void testPartOfSpeechFiltering() {
        logger.info("\n=== 词性过滤效果测试 ===\n");
        
        String testContent = "这个美丽的城市有很多高楼大厦，三个五个人都在快乐地工作。" +
            "Java是一门优秀的编程语言，具有强大的功能和广泛的应用。";
        
        HanLPTagExtractor extractor = new HanLPTagExtractor();
        List<Tag> tags = extractor.extractTags(testContent, 20);
        
        logger.info("提取的标签：");
        for (Tag tag : tags) {
            logger.info("  - {}", tag.getName());
        }
        
        List<String> shouldNotContain = Arrays.asList(
            "美丽", "很多", "三个", "五个", "快乐", "优秀", "强大", "广泛"
        );
        
        boolean hasUnwantedTags = false;
        for (Tag tag : tags) {
            if (shouldNotContain.contains(tag.getName())) {
                logger.warn("发现不应出现的标签: {}", tag.getName());
                hasUnwantedTags = true;
            }
        }
        
        if (!hasUnwantedTags) {
            logger.info("✓ 词性过滤效果良好，未发现形容词、量词等非核心词汇");
        } else {
            logger.warn("✗ 词性过滤需要进一步优化");
        }
    }
    
    /**
     * 测试停用词过滤效果
     * 验证常见停用词是否被正确过滤
     */
    @Test
    public void testStopWordsFiltering() {
        logger.info("\n=== 停用词过滤效果测试 ===\n");
        
        String testContent = "这是一个非常好的技术，我们可以使用它来开发应用程序。" +
            "那些人们都在学习这个技术，因为它很有用。";
        
        HanLPTagExtractor hanlpExtractor = new HanLPTagExtractor();
        IKAnalyzerTagExtractor ikExtractor = new IKAnalyzerTagExtractor();
        SimpleTagExtractor simpleExtractor = new SimpleTagExtractor();
        
        List<Tag> hanlpTags = hanlpExtractor.extractTags(testContent, 20);
        List<Tag> ikTags = ikExtractor.extractTags(testContent, 20);
        List<Tag> simpleTags = simpleExtractor.extractTags(testContent, 20);
        
        List<String> stopWords = Arrays.asList(
            "这", "这有", "那", "那些", "的", "是", "在", "我", "我们", "可以",
            "因为", "所以", "但是", "而且", "或者", "非常", "很", "都", "来"
        );
        
        logger.info("HanLP提取结果：");
        checkStopWords(hanlpTags, stopWords);
        
        logger.info("\nIKAnalyzer提取结果：");
        checkStopWords(ikTags, stopWords);
        
        logger.info("\nSimple提取结果：");
        checkStopWords(simpleTags, stopWords);
    }
    
    /**
     * 检查标签列表中是否包含停用词
     */
    private void checkStopWords(List<Tag> tags, List<String> stopWords) {
        boolean hasStopWords = false;
        for (Tag tag : tags) {
            if (stopWords.contains(tag.getName())) {
                logger.warn("  发现停用词: {}", tag.getName());
                hasStopWords = true;
            }
        }
        
        if (!hasStopWords) {
            logger.info("  ✓ 未发现停用词");
        } else {
            logger.warn("  ✗ 发现停用词，需要优化过滤规则");
        }
    }
}
