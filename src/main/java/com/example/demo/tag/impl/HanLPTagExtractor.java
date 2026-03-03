package com.example.demo.tag.impl;

import com.example.demo.tag.TagExtractor;
import com.example.demo.tag.config.TagExtractionConfig;
import com.example.demo.tag.model.Tag;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HanLP标签提取器
 * 
 * 该类使用HanLP分词器进行中文分词，是功能最强大的标签提取器实现。
 * 支持词性过滤、停用词过滤和频率过滤，能够准确识别名词、动词等核心词汇。
 * 
 * 核心特性：
 * 1. 使用HanLP分词器进行中文分词，支持多种词性标注
 * 2. 词性过滤：只保留名词、动词、形容词等核心词性
 * 3. 停用词过滤：排除常见停用词，提高标签质量
 * 4. 频率过滤：根据词语出现频率过滤低频词汇
 * 5. 自适应过滤：根据内容长度动态调整频率阈值
 * 
 * 使用示例：
 * <pre>
 * // 使用默认配置
 * HanLPTagExtractor extractor = new HanLPTagExtractor();
 * List<Tag> tags = extractor.extractTags("文章内容");
 * 
 * // 使用自定义配置
 * TagExtractionConfig config = TagExtractionConfig.strictConfig();
 * HanLPTagExtractor extractor = new HanLPTagExtractor(config);
 * List<Tag> tags = extractor.extractTags("文章内容", 20);
 * </pre>
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-04
 */
public class HanLPTagExtractor implements TagExtractor {
    
    /**
     * 允许的词性集合
     * 包含名词、动词、形容词等核心词性，用于过滤非核心词汇
     * 
     * 词性说明：
     * - n: 名词
     * - nr: 人名
     * - ns: 地名
     * - nt: 机构团体
     * - nz: 其他专名
     * - ng: 名词语素
     * - nl: 名词性惯用语
     * - ni: 机构名
     * - nm: 数词名
     * - v: 动词
     * - vg: 动词语素
     * - vi: 不及物动词
     * - vn: 名动词
     * - vd: 副动词
     * - a: 形容词
     * - ad: 副形词
     * - an: 名形词
     * - j: 简称
     * - l: 习用语
     * - q: 量词
     * - b: 区别词
     */
    private static final Set<String> ALLOWED_NATURES = new HashSet<>(Arrays.asList(
        "n", "nr", "ns", "nt", "nz", "ng", "nl", "ni", "nm",
        "v", "vg", "vi", "vn", "vd",
        "a", "ad", "an",
        "j", "l", "q", "b"
    ));
    
    /**
     * 停用词集合
     * 包含常见的中文停用词，用于过滤无意义的词汇
     */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "的", "了", "是", "在", "我", "有", "和", "就", "不", "人", "都", "一", "一个",
        "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好",
        "自己", "这", "那", "什么", "他", "她", "它", "们", "这个", "那个", "这些", "那些",
        "可以", "因为", "所以", "但是", "如果", "虽然", "而且", "或者", "还是", "以及",
        "这样", "那样", "怎样", "如何", "为什么", "哪", "哪里", "哪个", "哪些"
    ));
    
    /**
     * 标签提取配置对象
     * 用于配置频率过滤和自适应过滤参数
     */
    private TagExtractionConfig config;
    
    /**
     * 默认构造器
     * 使用默认配置创建HanLP标签提取器
     */
    public HanLPTagExtractor() {
        this.config = TagExtractionConfig.defaultConfig();
    }
    
    /**
     * 带配置参数的构造器
     * 允许使用自定义配置创建HanLP标签提取器
     * 
     * @param config 标签提取配置对象
     */
    public HanLPTagExtractor(TagExtractionConfig config) {
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
     * 4. 分词处理：使用HanLP进行分词
     * 5. 词频统计：统计每个词语的出现频率
     * 6. 过滤处理：应用词性过滤、停用词过滤和频率过滤
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

        // 使用HanLP进行分词，统计词频
        Map<String, Integer> wordCount = new HashMap<>();
        List<Term> terms = HanLP.segment(content);
        
        // 遍历分词结果，进行过滤和统计
        for (Term term : terms) {
            String word = term.word.trim().toLowerCase();
            String nature = term.nature.toString();
            
            // 应用过滤规则：非空、长度>=2、非停用词、允许的词性
            if (!word.isEmpty() && 
                word.length() >= 2 && 
                !STOP_WORDS.contains(word) &&
                isAllowedNature(nature)) {
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
        return "HanLPTagExtractor";
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
    
    /**
     * 判断词性是否允许
     * 
     * 判断逻辑：
     * 1. 如果词性为空，返回false
     * 2. 如果词性在允许列表中，返回true
     * 3. 如果词性以'n'开头（名词类），且不是特殊名词，返回true
     * 4. 其他情况返回false
     * 
     * @param nature 词性标注
     * @return true表示允许，false表示不允许
     */
    private boolean isAllowedNature(String nature) {
        // 空词性检查
        if (nature == null || nature.isEmpty()) {
            return false;
        }
        
        // 检查是否在允许列表中
        if (ALLOWED_NATURES.contains(nature)) {
            return true;
        }
        
        // 检查是否为名词类词性（排除特殊名词）
        if (nature.startsWith("n") && !nature.equals("nrfg") && !nature.equals("nrt")) {
            return true;
        }
        
        return false;
    }
}
