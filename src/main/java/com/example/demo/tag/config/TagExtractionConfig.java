package com.example.demo.tag.config;

/**
 * 标签提取配置类
 * 
 * 该类用于配置标签提取器的各项参数，主要包括频率过滤机制和内容长度阈值。
 * 通过该配置类，可以灵活调整标签提取的严格程度，适应不同场景的需求。
 * 
 * 核心功能：
 * 1. 频率过滤：根据词语出现频率过滤低频词汇，提高标签质量
 * 2. 自适应过滤：根据内容长度动态调整频率阈值
 * 3. 异常处理：处理极短文本和空内容等特殊情况
 * 
 * 使用示例：
 * <pre>
 * // 使用默认配置
 * TagExtractionConfig config = TagExtractionConfig.defaultConfig();
 * 
 * // 使用严格配置（高质量标签提取）
 * TagExtractionConfig config = TagExtractionConfig.strictConfig();
 * 
 * // 使用宽松配置（短文本标签提取）
 * TagExtractionConfig config = TagExtractionConfig.lenientConfig();
 * 
 * // 自定义配置
 * TagExtractionConfig config = new TagExtractionConfig();
 * config.setMinFrequency(3);
 * config.setEnableAdaptiveFiltering(true);
 * </pre>
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-04
 */
public class TagExtractionConfig {
    
    /**
     * 正常文本的最低频率阈值
     * 默认值：2，表示词语至少出现2次才会被提取为标签
     */
    private int minFrequency = 2;
    
    /**
     * 正常文本的最小长度阈值（字符数）
     * 默认值：100，超过此长度的文本使用minFrequency
     */
    private int minContentLength = 100;
    
    /**
     * 短文本的最低频率阈值
     * 默认值：3，用于长度在veryShortContentThreshold到minContentLength之间的文本
     */
    private int shortContentMinFrequency = 3;
    
    /**
     * 极短文本的最低频率阈值
     * 默认值：4，用于长度小于veryShortContentThreshold的文本
     */
    private int veryShortContentMinFrequency = 4;
    
    /**
     * 极短文本的长度阈值（字符数）
     * 默认值：50，小于此长度的文本使用veryShortContentMinFrequency
     */
    private int veryShortContentThreshold = 50;
    
    /**
     * 是否启用自适应频率过滤
     * 默认值：true，启用后会根据内容长度动态调整频率阈值
     */
    private boolean enableAdaptiveFiltering = true;
    
    /**
     * 是否在极短文本情况下返回空标签集
     * 默认值：false，不返回空标签集
     */
    private boolean returnEmptyOnShortContent = false;
    
    /**
     * 最少需要提取的标签数量
     * 默认值：1，当提取的标签数量少于此值时，会自动降低频率阈值
     */
    private int minTagsRequired = 1;
    
    /**
     * 默认构造器
     * 使用默认配置参数创建配置实例
     */
    public TagExtractionConfig() {
    }
    
    /**
     * 带参数的构造器
     * 允许设置正常文本的最低频率阈值和最小长度阈值
     * 
     * @param minFrequency 正常文本的最低频率阈值
     * @param minContentLength 正常文本的最小长度阈值
     */
    public TagExtractionConfig(int minFrequency, int minContentLength) {
        this.minFrequency = minFrequency;
        this.minContentLength = minContentLength;
    }
    
    /**
     * 获取正常文本的最低频率阈值
     * @return 最低频率阈值
     */
    public int getMinFrequency() {
        return minFrequency;
    }
    
    /**
     * 设置正常文本的最低频率阈值
     * 会自动确保最小值为1，避免过滤所有词汇
     * 
     * @param minFrequency 最低频率阈值
     */
    public void setMinFrequency(int minFrequency) {
        this.minFrequency = Math.max(1, minFrequency);
    }
    
    /**
     * 获取正常文本的最小长度阈值
     * @return 最小长度阈值（字符数）
     */
    public int getMinContentLength() {
        return minContentLength;
    }
    
    /**
     * 设置正常文本的最小长度阈值
     * 会自动确保最小值为10，避免阈值过小
     * 
     * @param minContentLength 最小长度阈值（字符数）
     */
    public void setMinContentLength(int minContentLength) {
        this.minContentLength = Math.max(10, minContentLength);
    }
    
    /**
     * 获取短文本的最低频率阈值
     * @return 短文本的最低频率阈值
     */
    public int getShortContentMinFrequency() {
        return shortContentMinFrequency;
    }
    
    /**
     * 设置短文本的最低频率阈值
     * 会自动确保最小值为2
     * 
     * @param shortContentMinFrequency 短文本的最低频率阈值
     */
    public void setShortContentMinFrequency(int shortContentMinFrequency) {
        this.shortContentMinFrequency = Math.max(2, shortContentMinFrequency);
    }
    
    /**
     * 获取极短文本的最低频率阈值
     * @return 极短文本的最低频率阈值
     */
    public int getVeryShortContentMinFrequency() {
        return veryShortContentMinFrequency;
    }
    
    /**
     * 设置极短文本的最低频率阈值
     * 会自动确保最小值为3
     * 
     * @param veryShortContentMinFrequency 极短文本的最低频率阈值
     */
    public void setVeryShortContentMinFrequency(int veryShortContentMinFrequency) {
        this.veryShortContentMinFrequency = Math.max(3, veryShortContentMinFrequency);
    }
    
    /**
     * 获取极短文本的长度阈值
     * @return 极短文本的长度阈值（字符数）
     */
    public int getVeryShortContentThreshold() {
        return veryShortContentThreshold;
    }
    
    /**
     * 设置极短文本的长度阈值
     * 会自动确保最小值为10
     * 
     * @param veryShortContentThreshold 极短文本的长度阈值（字符数）
     */
    public void setVeryShortContentThreshold(int veryShortContentThreshold) {
        this.veryShortContentThreshold = Math.max(10, veryShortContentThreshold);
    }
    
    /**
     * 判断是否启用自适应频率过滤
     * @return true表示启用，false表示禁用
     */
    public boolean isEnableAdaptiveFiltering() {
        return enableAdaptiveFiltering;
    }
    
    /**
     * 设置是否启用自适应频率过滤
     * 启用后会根据内容长度动态调整频率阈值
     * 
     * @param enableAdaptiveFiltering true表示启用，false表示禁用
     */
    public void setEnableAdaptiveFiltering(boolean enableAdaptiveFiltering) {
        this.enableAdaptiveFiltering = enableAdaptiveFiltering;
    }
    
    /**
     * 判断是否在极短文本情况下返回空标签集
     * @return true表示返回空标签集，false表示正常处理
     */
    public boolean isReturnEmptyOnShortContent() {
        return returnEmptyOnShortContent;
    }
    
    /**
     * 设置是否在极短文本情况下返回空标签集
     * 
     * @param returnEmptyOnShortContent true表示返回空标签集，false表示正常处理
     */
    public void setReturnEmptyOnShortContent(boolean returnEmptyOnShortContent) {
        this.returnEmptyOnShortContent = returnEmptyOnShortContent;
    }
    
    /**
     * 获取最少需要提取的标签数量
     * @return 最少标签数量
     */
    public int getMinTagsRequired() {
        return minTagsRequired;
    }
    
    /**
     * 设置最少需要提取的标签数量
     * 当提取的标签数量少于此值时，会自动降低频率阈值
     * 会自动确保最小值为0
     * 
     * @param minTagsRequired 最少标签数量
     */
    public void setMinTagsRequired(int minTagsRequired) {
        this.minTagsRequired = Math.max(0, minTagsRequired);
    }
    
    /**
     * 根据内容长度计算动态频率阈值
     * 
     * 该方法实现了自适应频率过滤的核心逻辑：
     * 1. 如果禁用自适应过滤，返回固定的minFrequency
     * 2. 如果内容长度小于极短文本阈值，使用veryShortContentMinFrequency
     * 3. 如果内容长度在极短文本阈值和正常文本阈值之间，使用shortContentMinFrequency
     * 4. 如果内容长度大于等于正常文本阈值，使用minFrequency
     * 
     * @param contentLength 内容长度（字符数）
     * @return 计算得出的最低频率阈值
     */
    public int calculateMinFrequency(int contentLength) {
        // 如果禁用自适应过滤，返回固定阈值
        if (!enableAdaptiveFiltering) {
            return minFrequency;
        }
        
        // 根据内容长度选择不同的频率阈值
        if (contentLength < veryShortContentThreshold) {
            // 极短文本：使用最高的频率阈值
            return veryShortContentMinFrequency;
        } else if (contentLength < minContentLength) {
            // 短文本：使用中等频率阈值
            return shortContentMinFrequency;
        } else {
            // 正常文本：使用标准频率阈值
            return minFrequency;
        }
    }
    
    /**
     * 创建默认配置实例
     * 
     * 默认配置参数：
     * - minFrequency: 2
     * - minContentLength: 100
     * - shortContentMinFrequency: 3
     * - veryShortContentMinFrequency: 4
     * - veryShortContentThreshold: 50
     * - enableAdaptiveFiltering: true
     * 
     * @return 默认配置实例
     */
    public static TagExtractionConfig defaultConfig() {
        return new TagExtractionConfig();
    }
    
    /**
     * 创建严格配置实例（适用于高质量标签提取）
     * 
     * 严格配置参数：
     * - minFrequency: 3
     * - shortContentMinFrequency: 4
     * - veryShortContentMinFrequency: 5
     * 
     * 适用场景：
     * - 需要高质量标签的场景
     * - 内容较长的文章
     * - 对标签准确性要求较高的应用
     * 
     * @return 严格配置实例
     */
    public static TagExtractionConfig strictConfig() {
        TagExtractionConfig config = new TagExtractionConfig();
        config.setMinFrequency(3);
        config.setShortContentMinFrequency(4);
        config.setVeryShortContentMinFrequency(5);
        return config;
    }
    
    /**
     * 创建宽松配置实例（适用于短文本标签提取）
     * 
     * 宽松配置参数：
     * - minFrequency: 1
     * - shortContentMinFrequency: 2
     * - veryShortContentMinFrequency: 2
     * - enableAdaptiveFiltering: true
     * 
     * 适用场景：
     * - 短文本标签提取
     * - 需要提取更多标签的场景
     * - 对标签召回率要求较高的应用
     * 
     * @return 宽松配置实例
     */
    public static TagExtractionConfig lenientConfig() {
        TagExtractionConfig config = new TagExtractionConfig();
        config.setMinFrequency(1);
        config.setShortContentMinFrequency(2);
        config.setVeryShortContentMinFrequency(2);
        config.setEnableAdaptiveFiltering(true);
        return config;
    }
    
    /**
     * 返回配置对象的字符串表示
     * 包含所有配置参数的详细信息
     * 
     * @return 配置对象的字符串表示
     */
    @Override
    public String toString() {
        return "TagExtractionConfig{" +
                "minFrequency=" + minFrequency +
                ", minContentLength=" + minContentLength +
                ", shortContentMinFrequency=" + shortContentMinFrequency +
                ", veryShortContentMinFrequency=" + veryShortContentMinFrequency +
                ", veryShortContentThreshold=" + veryShortContentThreshold +
                ", enableAdaptiveFiltering=" + enableAdaptiveFiltering +
                ", returnEmptyOnShortContent=" + returnEmptyOnShortContent +
                ", minTagsRequired=" + minTagsRequired +
                '}';
    }
}
