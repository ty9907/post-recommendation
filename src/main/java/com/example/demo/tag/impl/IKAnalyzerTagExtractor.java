package com.example.demo.tag.impl;

import com.example.demo.tag.TagExtractor;
import com.example.demo.tag.config.TagExtractionConfig;
import com.example.demo.tag.model.Tag;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * IKAnalyzer标签提取器
 * 
 * 该类使用IKAnalyzer分词器进行中文分词，适合中文文章的标签提取。
 * IKAnalyzer是一个开源的中文分词器，具有较好的分词效果和性能。
 * 
 * 核心特性：
 * 1. 使用IKAnalyzer分词器进行中文分词，支持智能分词模式
 * 2. 停用词过滤：排除常见停用词，提高标签质量
 * 3. 频率过滤：根据词语出现频率过滤低频词汇
 * 4. 自适应过滤：根据内容长度动态调整频率阈值
 * 5. 异常处理：处理分词过程中的异常情况
 * 
 * 与HanLP提取器的区别：
 * - IKAnalyzer不支持词性标注，因此无法进行词性过滤
 * - IKAnalyzer分词速度较快，适合大规模文本处理
 * - IKAnalyzer对中文分词效果较好，但对英文支持较弱
 * 
 * 使用示例：
 * <pre>
 * // 使用默认配置
 * IKAnalyzerTagExtractor extractor = new IKAnalyzerTagExtractor();
 * List<Tag> tags = extractor.extractTags("文章内容");
 * 
 * // 使用自定义配置
 * TagExtractionConfig config = TagExtractionConfig.lenientConfig();
 * IKAnalyzerTagExtractor extractor = new IKAnalyzerTagExtractor(config);
 * List<Tag> tags = extractor.extractTags("文章内容", 15);
 * </pre>
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-04
 */
public class IKAnalyzerTagExtractor implements TagExtractor {
    
    /**
     * 停用词集合
     * 包含常见的中文停用词和量词、形容词等非核心词汇，用于过滤无意义的词汇
     * 
     * 停用词类型：
     * 1. 功能词：的、了、是、在等
     * 2. 代词：我、你、他、她等
     * 3. 连词：和、与、或、但等
     * 4. 量词：个、些、次、位等
     * 5. 形容词：非常、特别、十分等
     */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "的", "了", "是", "在", "我", "有", "和", "就", "不", "人", "都", "一", "一个",
        "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好",
        "自己", "这", "那", "什么", "他", "她", "它", "们", "这个", "那个", "这些", "那些",
        "可以", "因为", "所以", "但是", "如果", "虽然", "而且", "或者", "还是", "以及",
        "这样", "那样", "怎样", "如何", "为什么", "哪", "哪里", "哪个", "哪些",
        "之", "与", "及", "等", "中", "来", "把", "被", "让", "给", "向", "从", "对",
        "个", "些", "次", "位", "只", "条", "件", "种", "样", "块", "片", "张", "本",
        "非常", "特别", "十分", "比较", "更", "最", "太", "真", "好", "多", "少",
        "大", "小", "高", "低", "长", "短", "快", "慢", "新", "旧", "好", "坏"
    ));
    
    /**
     * 标签提取配置对象
     * 用于配置频率过滤和自适应过滤参数
     */
    private TagExtractionConfig config;
    
    /**
     * 默认构造器
     * 使用默认配置创建IKAnalyzer标签提取器
     */
    public IKAnalyzerTagExtractor() {
        this.config = TagExtractionConfig.defaultConfig();
    }
    
    /**
     * 带配置参数的构造器
     * 允许使用自定义配置创建IKAnalyzer标签提取器
     * 
     * @param config 标签提取配置对象
     */
    public IKAnalyzerTagExtractor(TagExtractionConfig config) {
        this.config = config != null ? config : TagExtractionConfig.defaultConfig();
    }
    
    /**
     * 从文章内容中提取标签（默认提取10个）
     * 
     * @param content 文章内容
     * @return 标签列表，按权重排序
     */
    @Override
    public List<Tag> extractTags(String content) {
        return extractTags(content, 10);
    }

    /**
     * 从文章内容中提取指定数量的标签
     * 
     * 处理流程：
     * 1. 空内容检查：如果内容为空或null，返回空列表
     * 2. 计算频率阈值：根据内容长度计算最低频率阈值
     * 3. 极短文本处理：如果配置了返回空标签集，则返回空列表
     * 4. 分词处理：使用IKAnalyzer进行智能分词
     * 5. 词频统计：统计每个词语的出现频率
     * 6. 过滤处理：应用停用词过滤和频率过滤
     * 7. 权重计算：计算每个词语的权重（词频/总词数）
     * 8. 排序输出：按权重降序排序并限制数量
     * 9. 自适应调整：如果标签数量不足，自动降低频率阈值
     * 
     * @param content 文章内容
     * @param limit 标签数量限制
     * @return 标签列表，按权重排序
     */
    @Override
    public List<Tag> extractTags(String content, int limit) {
        // 空内容检查
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }

        // 计算内容长度和频率阈值
        int contentLength = content.length();
        int minFrequency = config.calculateMinFrequency(contentLength);
        
        // 极短文本处理
        if (config.isReturnEmptyOnShortContent() && contentLength < config.getVeryShortContentThreshold()) {
            return new ArrayList<>();
        }

        // 使用IKAnalyzer进行分词，统计词频
        Map<String, Integer> wordCount = new HashMap<>();
        try {
            // 创建IKAnalyzer分词器，使用智能分词模式
            StringReader reader = new StringReader(content);
            IKSegmenter segmenter = new IKSegmenter(reader, true);
            Lexeme lexeme;
            
            // 遍历分词结果，进行过滤和统计
            while ((lexeme = segmenter.next()) != null) {
                String word = lexeme.getLexemeText().trim().toLowerCase();
                
                // 应用过滤规则：非空、长度>=2、非停用词
                if (!word.isEmpty() && 
                    word.length() >= 2 && 
                    !STOP_WORDS.contains(word)) {
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        } catch (Exception e) {
            // 异常处理：打印异常堆栈
            e.printStackTrace();
        }

        // 创建标签列表，应用频率过滤
        List<Tag> tags = new ArrayList<>();
        int totalWords = wordCount.values().stream().mapToInt(Integer::intValue).sum();
        
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            String word = entry.getKey();
            int frequency = entry.getValue();
            
            // 只保留频率达到阈值的词语
            if (frequency >= minFrequency) {
                // 权重计算：词频 / 总词数
                double weight = (double) frequency / totalWords;
                tags.add(new Tag(word, weight, frequency));
            }
        }

        // 按权重降序排序并限制数量
        tags = tags.stream()
                .sorted((t1, t2) -> Double.compare(t2.getWeight(), t1.getWeight()))
                .limit(limit)
                .collect(Collectors.toList());
        
        // 自适应调整：如果标签数量不足，降低频率阈值重新提取
        if (tags.size() < config.getMinTagsRequired() && minFrequency > 1) {
            int adjustedMinFrequency = Math.max(1, minFrequency - 1);
            tags = new ArrayList<>();
            
            // 使用降低后的频率阈值重新提取标签
            for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                String word = entry.getKey();
                int frequency = entry.getValue();
                
                if (frequency >= adjustedMinFrequency) {
                    double weight = (double) frequency / totalWords;
                    tags.add(new Tag(word, weight, frequency));
                }
            }
            
            // 重新排序并限制数量
            tags = tags.stream()
                    .sorted((t1, t2) -> Double.compare(t2.getWeight(), t1.getWeight()))
                    .limit(limit)
                    .collect(Collectors.toList());
        }
        
        return tags;
    }

    /**
     * 获取提取器名称
     * @return 提取器名称
     */
    @Override
    public String getName() {
        return "IKAnalyzerTagExtractor";
    }
    
    /**
     * 获取当前配置
     * @return 标签提取配置对象
     */
    public TagExtractionConfig getConfig() {
        return config;
    }
    
    /**
     * 设置配置
     * @param config 标签提取配置对象
     */
    public void setConfig(TagExtractionConfig config) {
        this.config = config != null ? config : TagExtractionConfig.defaultConfig();
    }
}
