package com.example.demo.tag.impl;

import com.example.demo.tag.TagExtractor;
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
 * 使用HanLP分词器进行中文分词，功能强大，支持词性过滤
 */
public class HanLPTagExtractor implements TagExtractor {
    
    private static final Set<String> ALLOWED_NATURES = new HashSet<>(Arrays.asList(
        "n",    // 名词
        "nr",   // 人名
        "ns",   // 地名
        "nt",   // 机构名
        "nz",   // 其他专名
        "ng",   // 名词语素
        "nl",   // 名词性惯用语
        "ni",   // 机构专名
        "nm",   // 数词名
        "v",    // 动词
        "vg",   // 动词语素
        "vi",   // 不及物动词
        "vn",   // 名动词
        "vd",   // 副动词
        "a",    // 形容词（保留部分形容词作为标签）
        "ad",   // 副形词
        "an",   // 名形词
        "j",    // 简称
        "l",    // 习用语
        "q",    // 量词（保留部分量词）
        "b"     // 区别词
    ));
    
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "的", "了", "是", "在", "我", "有", "和", "就", "不", "人", "都", "一", "一个",
        "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好",
        "自己", "这", "那", "什么", "他", "她", "它", "们", "这个", "那个", "这些", "那些",
        "可以", "因为", "所以", "但是", "如果", "虽然", "而且", "或者", "还是", "以及",
        "这样", "那样", "怎样", "如何", "为什么", "哪", "哪里", "哪个", "哪些"
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

        // 使用HanLP分词
        Map<String, Integer> wordCount = new HashMap<>();
        List<Term> terms = HanLP.segment(content);
        
        for (Term term : terms) {
            String word = term.word.trim().toLowerCase();
            String nature = term.nature.toString();
            
            // 词性过滤：只保留允许的词性
            // 停用词过滤：排除常见停用词
            // 长度过滤：排除过短的词
            if (!word.isEmpty() && 
                word.length() >= 2 && 
                !STOP_WORDS.contains(word) &&
                isAllowedNature(nature)) {
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
    
    /**
     * 判断词性是否允许作为标签
     * @param nature 词性标注
     * @return true表示允许，false表示不允许
     */
    private boolean isAllowedNature(String nature) {
        if (nature == null || nature.isEmpty()) {
            return false;
        }
        
        // 精确匹配允许的词性
        if (ALLOWED_NATURES.contains(nature)) {
            return true;
        }
        
        // 对于以n开头的词性（名词类），大部分都允许
        if (nature.startsWith("n") && !nature.equals("nrfg") && !nature.equals("nrt")) {
            return true;
        }
        
        return false;
    }
}