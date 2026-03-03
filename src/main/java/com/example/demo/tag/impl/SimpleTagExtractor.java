package com.example.demo.tag.impl;

import com.example.demo.tag.TagExtractor;
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
 * 基于正则表达式进行分词，适用于简单的标签提取场景
 */
public class SimpleTagExtractor implements TagExtractor {
    
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
     * 从文章内容中提取标签（默认提取10个）
     * @param content 文章内容
     * @return 标签列表，按权重排序
     */
    @Override
    public List<Tag> extractTags(String content) {
        return extractTags(content, 10);
    }

    /**
     * 从文章内容中提取指定数量的标签
     * @param content 文章内容
     * @param limit 标签数量限制
     * @return 标签列表，按权重排序
     */
    @Override
    public List<Tag> extractTags(String content, int limit) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }

        // 简单分词：按空格和标点符号分割
        String[] words = content.split("[\\s,.;:!?()\\[\\]{}，。；：！？（）【】｛｝]");

        // 统计词频
        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            word = word.trim().toLowerCase();
            // 停用词过滤：排除常见停用词
            // 长度过滤：排除过短的词
            if (!word.isEmpty() && 
                word.length() >= 2 && 
                !STOP_WORDS.contains(word)) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }

        // 计算权重并创建标签
        List<Tag> tags = new ArrayList<>();
        int totalWords = wordCount.values().stream().mapToInt(Integer::intValue).sum();
        
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            String word = entry.getKey();
            int frequency = entry.getValue();
            // 权重计算：词频 / 总词数
            double weight = (double) frequency / totalWords;
            tags.add(new Tag(word, weight, frequency));
        }

        // 按权重降序排序并限制数量
        return tags.stream()
                .sorted((t1, t2) -> Double.compare(t2.getWeight(), t1.getWeight()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取提取器名称
     * @return 提取器名称
     */
    @Override
    public String getName() {
        return "SimpleTagExtractor";
    }
}
