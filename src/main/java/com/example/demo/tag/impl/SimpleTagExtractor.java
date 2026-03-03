package com.example.demo.tag.impl;

import com.example.demo.tag.TagExtractor;
import com.example.demo.tag.config.TagExtractionConfig;
import com.example.demo.tag.model.Tag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 简单标签提取器
 * 
 * 该类基于正则表达式进行分词，是最简单的标签提取器实现。
 * 不依赖第三方分词库，适用于简单的标签提取场景。
 * 
 * 核心特性：
 * 1. 使用正则表达式进行分词，无需第三方依赖
 * 2. 停用词过滤：排除常见停用词，提高标签质量
 * 3. 频率过滤：根据词语出现频率过滤低频词汇
 * 4. 自适应过滤：根据内容长度动态调整频率阈值
 * 5. 轻量级：适合对性能要求较高的场景
 * 
 * 与其他提取器的区别：
 * - 不依赖第三方分词库，部署简单
 * - 分词效果不如HanLP和IKAnalyzer，适合简单场景
 * - 性能较好，适合大规模文本处理
 * - 支持中英文混合文本
 * 
 * 使用示例：
 * <pre>
 * // 使用默认配置
 * SimpleTagExtractor extractor = new SimpleTagExtractor();
 * List<Tag> tags = extractor.extractTags("文章内容");
 * 
 * // 使用自定义配置
 * TagExtractionConfig config = TagExtractionConfig.lenientConfig();
 * SimpleTagExtractor extractor = new SimpleTagExtractor(config);
 * List<Tag> tags = extractor.extractTags("文章内容", 15);
 * </pre>
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-04
 */
public class SimpleTagExtractor implements TagExtractor {
    
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
     * 使用默认配置创建简单标签提取器
     */
    public SimpleTagExtractor() {
        this.config = TagExtractionConfig.defaultConfig();
    }
    
    /**
     * 带配置参数的构造器
     * 允许使用自定义配置创建简单标签提取器
     * 
     * @param config 标签提取配置对象
     */
    public SimpleTagExtractor(TagExtractionConfig config) {
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
     * 4. 分词处理：使用正则表达式按空格和标点符号分割
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

        // 简单分词：按空格和标点符号分割（支持中英文标点）
        String[] words = content.split("[\\s,.;:!?()\\[\\]{}，。；：！？（）【】｛｝]");

        // 统计词频
        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            word = word.trim().toLowerCase();
            
            // 应用过滤规则：非空、长度>=2、非停用词
            if (!word.isEmpty() && 
                word.length() >= 2 && 
                !STOP_WORDS.contains(word)) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
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
        return "SimpleTagExtractor";
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
