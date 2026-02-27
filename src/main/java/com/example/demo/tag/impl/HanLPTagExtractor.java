package com.example.demo.tag.impl;

import com.example.demo.tag.TagExtractor;
import com.example.demo.tag.model.Tag;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HanLP标签提取器
 * 使用HanLP分词器进行中文分词，功能强大，支持词性过滤
 */
public class HanLPTagExtractor implements TagExtractor {
    
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

        // 使用HanLP分词
        Map<String, Integer> wordCount = new HashMap<>();
        List<Term> terms = HanLP.segment(content);
        
        for (Term term : terms) {
            String word = term.word.trim().toLowerCase();
            String nature = term.nature.toString();
            
            // 过滤太短的词和虚词
            // u: 助词, c: 连词, p: 介词, d: 副词
            if (!word.isEmpty() && word.length() >= 2 && 
                !nature.startsWith("u") && !nature.startsWith("c") && 
                !nature.startsWith("p") && !nature.startsWith("d")) {
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
        return "HanLPTagExtractor";
    }
}