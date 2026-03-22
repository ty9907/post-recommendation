package com.example.demo.duplicate.util;

import com.example.demo.duplicate.preprocess.DefaultRichTextPreprocessor;
import com.example.demo.duplicate.preprocess.PreprocessedTextCache;
import com.example.demo.duplicate.preprocess.RichTextPreprocessor;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 文本预处理工具类
 * 
 * 提供文本清洗、分词、去停用词等功能，用于文本相似度分析前的预处理。
 * 支持HTML标签去除、特殊字符过滤、HanLP中文分词等操作。
 * 
 * 核心功能：
 * 1. 文本清洗：去除HTML标签、特殊字符
 * 2. 中文分词：使用HanLP进行高质量中文分词
 * 3. 停用词过滤：过滤常见的中文停用词
 * 4. 文本预处理：一键完成所有预处理步骤
 * 
 * 使用示例：
 * <pre>
 * TextPreprocessor preprocessor = new TextPreprocessor();
 * 
 * // 完整预处理
 * String cleaned = preprocessor.preprocess("&lt;p&gt;文章内容&lt;/p&gt;");
 * 
 * // 分词
 * List&lt;String&gt; tokens = preprocessor.tokenize("Java是一门编程语言");
 * 
 * // 去停用词
 * List&lt;String&gt; filtered = preprocessor.removeStopWords(tokens);
 * </pre>
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
public class TextPreprocessor {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "的", "了", "是", "在", "我", "有", "和", "就", "不", "人", "都", "一", "一个",
        "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好",
        "自己", "这", "那", "什么", "他", "她", "它", "们", "这个", "那个", "这些", "那些",
        "可以", "因为", "所以", "但是", "如果", "虽然", "而且", "或者", "还是", "以及",
        "这样", "那样", "怎样", "如何", "为什么", "哪", "哪里", "哪个", "哪些",
        "之", "与", "及", "等", "中", "来", "把", "被", "让", "给", "向", "从", "对",
        "个", "些", "次", "位", "只", "条", "件", "种", "样", "块", "片", "张", "本",
        "非常", "特别", "十分", "比较", "更", "最", "太", "真", "好", "多", "少",
        "大", "小", "高", "低", "长", "短", "快", "慢", "新", "旧", "好", "坏",
        "能", "得", "地", "得", "啊", "吧", "吗", "呢", "嘛", "哦", "哈", "呀",
        "就是", "可能", "应该", "需要", "能够", "已经", "正在", "将要", "曾经",
        "刚刚", "马上", "立刻", "一直", "总是", "从来", "开始", "继续", "结束",
        "一下", "一点", "一些", "所有", "任何", "每个", "各个", "某种", "某些",
        "其他", "另外", "此外", "而且", "并且", "但是", "然而", "不过", "只是",
        "因为", "所以", "因此", "由于", "虽然", "即使", "如果", "假如", "只要",
        "一", "二", "三", "四", "五", "六", "七", "八", "九", "十",
        "百", "千", "万", "亿", "零", "壹", "贰", "叁", "肆", "伍",
        "陆", "柒", "捌", "玖", "拾", "佰", "仟"
    ));

    private final RichTextPreprocessor richTextPreprocessor;
    private final PreprocessedTextCache preprocessedTextCache;

    public TextPreprocessor() {
        this.richTextPreprocessor = new DefaultRichTextPreprocessor();
        this.preprocessedTextCache = new PreprocessedTextCache();
    }

    /**
     * 预处理文本
     * 
     * 完整的文本预处理流程：
     * 1. 去除HTML标签
     * 2. 去除HTML实体
     * 3. 去除控制字符
     * 4. 去除特殊字符
     * 5. 规范化空白字符
     * 
     * @param text 原始文本
     * @return 预处理后的文本
     */
    public String preprocess(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return preprocessedTextCache.getOrCompute(text, () -> richTextPreprocessor.preprocess(text));
    }

    /**
     * 去除HTML标签
     * 
     * 移除所有形如 &lt;...&gt; 的HTML标签
     * 例如：&lt;p&gt;内容&lt;/p&gt; → 内容
     * 
     * @param text 包含HTML标签的文本
     * @return 去除HTML标签后的文本
     */
    public String removeHtmlTags(String text) {
        return richTextPreprocessor.stripTags(text);
    }

    /**
     * 去除HTML实体
     * 
     * 移除HTML实体字符，如 &amp;nbsp;, &amp;lt;, &amp;gt;, &amp;#160; 等
     * 
     * @param text 包含HTML实体的文本
     * @return 去除HTML实体后的文本
     */
    public String removeHtmlEntities(String text) {
        return richTextPreprocessor.decodeEntities(text);
    }

    /**
     * 去除特殊字符
     * 
     * 移除非中文、非英文、非数字的特殊字符
     * 保留中文汉字、英文字母、数字
     * 
     * @param text 包含特殊字符的文本
     * @return 去除特殊字符后的文本
     */
    public String removeSpecialChars(String text) {
        return richTextPreprocessor.normalize(text);
    }

    /**
     * 去除控制字符
     * 
     * 移除ASCII控制字符（0x00-0x1F, 0x7F）
     * 
     * @param text 包含控制字符的文本
     * @return 去除控制字符后的文本
     */
    public String removeControlChars(String text) {
        return richTextPreprocessor.normalize(text);
    }

    /**
     * 规范化空白字符
     * 
     * 将多个连续空白字符合并为单个空格
     * 
     * @param text 包含多余空白字符的文本
     * @return 规范化后的文本
     */
    public String normalizeWhitespace(String text) {
        return richTextPreprocessor.normalize(text);
    }

    /**
     * 使用HanLP进行中文分词
     * 
     * 对文本进行分词处理，返回词语列表
     * 自动过滤空词和单字词
     * 
     * @param text 待分词的文本
     * @return 分词结果列表
     */
    public List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> tokens = new ArrayList<>();
        List<Term> terms = HanLP.segment(text);
        
        for (Term term : terms) {
            String word = term.word.trim();
            if (!word.isEmpty() && word.length() >= 2) {
                tokens.add(word.toLowerCase());
            }
        }
        
        return tokens;
    }

    /**
     * 去除停用词
     * 
     * 从词语列表中过滤掉停用词
     * 停用词包括：功能词、代词、连词、量词、形容词等
     * 
     * @param tokens 词语列表
     * @return 过滤后的词语列表
     */
    public List<String> removeStopWords(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> filtered = new ArrayList<>();
        for (String token : tokens) {
            if (!STOP_WORDS.contains(token)) {
                filtered.add(token);
            }
        }
        
        return filtered;
    }

    /**
     * 预处理并分词
     * 
     * 完整的文本处理流程：
     * 1. 预处理文本（去HTML、特殊字符）
     * 2. 分词
     * 3. 去停用词
     * 
     * @param text 原始文本
     * @return 处理后的词语列表
     */
    public List<String> preprocessAndTokenize(String text) {
        String cleaned = preprocess(text);
        List<String> tokens = tokenize(cleaned);
        return removeStopWords(tokens);
    }

    /**
     * 判断是否为停用词
     * 
     * @param word 待判断的词语
     * @return true表示是停用词，false表示不是
     */
    public boolean isStopWord(String word) {
        return word != null && STOP_WORDS.contains(word);
    }

    /**
     * 添加自定义停用词
     * 
     * @param word 停用词
     */
    public void addStopWord(String word) {
        if (word != null && !word.isEmpty()) {
            STOP_WORDS.add(word);
        }
    }

    /**
     * 获取停用词集合大小
     * 
     * @return 停用词数量
     */
    public int getStopWordsCount() {
        return STOP_WORDS.size();
    }
}
