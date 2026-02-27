package com.example.demo.tag.impl;

import com.example.demo.tag.TagExtractor;
import com.example.demo.tag.model.Tag;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IKAnalyzer标签提取器
 * 使用IKAnalyzer分词器进行中文分词，适合中文文章的标签提取
 */
public class IKAnalyzerTagExtractor implements TagExtractor {
    
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

        // 使用IKAnalyzer分词
        Map<String, Integer> wordCount = new HashMap<>();
        try {
            StringReader reader = new StringReader(content);
            // 创建IK分词器，true表示使用智能分词模式
            IKSegmenter segmenter = new IKSegmenter(reader, true);
            Lexeme lexeme;
            // 遍历分词结果
            while ((lexeme = segmenter.next()) != null) {
                String word = lexeme.getLexemeText().trim().toLowerCase();
                // 过滤太短的词（长度小于2的词）
                if (!word.isEmpty() && word.length() >= 2) {
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        return "IKAnalyzerTagExtractor";
    }
}
