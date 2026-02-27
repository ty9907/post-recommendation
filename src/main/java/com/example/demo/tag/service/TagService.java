package com.example.demo.tag.service;

import com.example.demo.tag.TagExtractor;
import com.example.demo.tag.impl.HanLPTagExtractor;
import com.example.demo.tag.impl.IKAnalyzerTagExtractor;
import com.example.demo.tag.impl.SimpleTagExtractor;
import com.example.demo.tag.model.Tag;
import java.util.List;

/**
 * 标签提取服务类
 * 提供统一的标签提取接口，支持多种分词器
 */
public class TagService {
    private TagExtractor tagExtractor;

    /**
     * 默认构造器，使用HanLP分词器
     */
    public TagService() {
        this.tagExtractor = new HanLPTagExtractor();
    }

    /**
     * 带参数构造器，可指定使用的分词器
     * @param extractorType 分词器类型："simple"、"ik"或"hanlp"
     */
    public TagService(String extractorType) {
        switch (extractorType.toLowerCase()) {
            case "simple":
                this.tagExtractor = new SimpleTagExtractor();
                break;
            case "ik":
                this.tagExtractor = new IKAnalyzerTagExtractor();
                break;
            case "hanlp":
            default:
                this.tagExtractor = new HanLPTagExtractor();
                break;
        }
    }

    /**
     * 手动设置分词器
     * @param tagExtractor 标签提取器实例
     */
    public void setTagExtractor(TagExtractor tagExtractor) {
        this.tagExtractor = tagExtractor;
    }

    /**
     * 提取文章标签
     * @param content 文章内容
     * @return 标签列表
     */
    public List<Tag> extractTags(String content) {
        return tagExtractor.extractTags(content);
    }

    /**
     * 提取指定数量的文章标签
     * @param content 文章内容
     * @param limit 标签数量限制
     * @return 标签列表
     */
    public List<Tag> extractTags(String content, int limit) {
        return tagExtractor.extractTags(content, limit);
    }

    /**
     * 获取当前使用的提取器名称
     * @return 提取器名称
     */
    public String getExtractorName() {
        return tagExtractor.getName();
    }
}