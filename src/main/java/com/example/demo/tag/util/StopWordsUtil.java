package com.example.demo.tag.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * 停用词表工具类
 * 用于过滤非标签类词汇，提高标签提取准确性
 */
public class StopWordsUtil {
    
    private static Set<String> stopWords = new HashSet<>();
    
    static {
        loadDefaultStopWords();
    }
    
    /**
     * 加载默认停用词表
     * 包含常见的形容词、副词、代词、数词、量词等
     */
    private static void loadDefaultStopWords() {
        String[] defaultStopWords = {
            "的", "了", "和", "是", "就", "都", "而", "及", "与", "着",
            "或", "一个", "没有", "我们", "你们", "他们", "它们", "这个", "那个", "哪些",
            "什么", "怎么", "如何", "为什么", "多少", "几个", "哪个", "哪里", "那里", "这里",
            "非常", "很", "太", "更", "最", "十分", "极其", "相当", "比较", "稍微",
            "已经", "正在", "将要", "曾经", "刚刚", "马上", "立刻", "一直", "总是", "从来",
            "可能", "应该", "必须", "需要", "能够", "可以", "应该", "得", "得",
            "这样", "那样", "怎样", "这么", "那么", "多么", "如此",
            "一些", "许多", "所有", "任何", "每个", "各个", "某种", "某些",
            "其他", "另外", "此外", "而且", "并且", "但是", "然而", "不过", "只是",
            "因为", "所以", "因此", "由于", "虽然", "即使", "如果", "假如", "只要",
            "一", "二", "三", "四", "五", "六", "七", "八", "九", "十",
            "百", "千", "万", "亿", "零", "壹", "贰", "叁", "肆", "伍",
            "陆", "柒", "捌", "玖", "拾", "佰", "仟",
            "个", "只", "条", "张", "件", "种", "类", "些", "点", "次",
            "回", "遍", "番", "趟", "下", "上", "前", "后", "左", "右",
            "大", "小", "多", "少", "高", "低", "长", "短", "快", "慢",
            "好", "坏", "新", "旧", "老", "少", "美", "丑", "真", "假",
            "对", "错", "是", "非", "有", "无", "生", "死", "存", "亡",
            "这", "那", "哪", "我", "你", "他", "她", "它", "自", "己",
            "人", "事", "物", "地", "方", "时", "候", "年", "月", "日",
            "说", "讲", "道", "问", "答", "想", "看", "听", "说", "做",
            "来", "去", "进", "出", "上", "下", "起", "落", "开", "关",
            "能", "会", "要", "得", "把", "被", "让", "给", "向", "从",
            "在", "于", "以", "为", "因", "由", "对", "跟", "同", "和"
        };
        
        for (String word : defaultStopWords) {
            stopWords.add(word);
        }
    }
    
    /**
     * 从资源文件加载停用词表
     * @param filename 资源文件名
     */
    public static void loadStopWordsFromFile(String filename) {
        try (InputStream inputStream = StopWordsUtil.class.getClassLoader().getResourceAsStream(filename)) {
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8)
                );
                String line;
                while ((line = reader.readLine()) != null) {
                    String word = line.trim();
                    if (!word.isEmpty() && !word.startsWith("#")) {
                        stopWords.add(word);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 判断是否为停用词
     * @param word 待判断的词
     * @return true表示是停用词，false表示不是
     */
    public static boolean isStopWord(String word) {
        return stopWords.contains(word);
    }
    
    /**
     * 添加停用词
     * @param word 停用词
     */
    public static void addStopWord(String word) {
        stopWords.add(word);
    }
    
    /**
     * 获取停用词表大小
     * @return 停用词数量
     */
    public static int getStopWordsCount() {
        return stopWords.size();
    }
}
