package com.example.demo.duplicate.algorithm;

import com.example.demo.duplicate.algorithm.impl.CosineSimilarityCalculator;
import com.example.demo.duplicate.algorithm.impl.EditDistanceSimilarityCalculator;
import com.example.demo.duplicate.algorithm.impl.HybridSimilarityCalculator;
import com.example.demo.duplicate.algorithm.impl.SimHashSimilarityCalculator;
import com.example.demo.duplicate.algorithm.impl.TFIDFSimilarityCalculator;
import com.example.demo.duplicate.algorithm.impl.Word2VecSimilarityCalculator;

/**
 * 相似度计算器工厂类
 * 
 * 提供静态方法根据算法类型获取对应的相似度计算器实例。
 * 支持的算法类型：TFIDF, COSINE, EDIT_DISTANCE, SIMHASH, WORD2VEC
 * 
 * 使用示例：
 * <pre>
 * // 获取默认计算器（TF-IDF）
 * SimilarityCalculator calculator = SimilarityCalculatorFactory.getCalculator();
 * 
 * // 获取指定类型的计算器
 * SimilarityCalculator tfidfCalculator = SimilarityCalculatorFactory.getCalculator("TFIDF");
 * SimilarityCalculator cosineCalculator = SimilarityCalculatorFactory.getCalculator("COSINE");
 * SimilarityCalculator word2vecCalculator = SimilarityCalculatorFactory.getCalculator("WORD2VEC");
 * </pre>
 * 
 * 算法特点：
 * - TFIDF: 基于TF-IDF和余弦相似度，适合长文本，考虑词的重要性
 * - COSINE: 基于词频向量的余弦相似度，计算简单，效率高
 * - EDIT_DISTANCE: 基于编辑距离，适合检测抄袭和改写
 * - SIMHASH: 基于SimHash指纹，适合大规模文档快速去重
 * - WORD2VEC: 基于词向量的语义相似度，能够理解同义词和语义关系
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-05
 */
public final class SimilarityCalculatorFactory {

    public static final String TFIDF = "TFIDF";

    public static final String COSINE = "COSINE";

    public static final String EDIT_DISTANCE = "EDIT_DISTANCE";

    public static final String SIMHASH = "SIMHASH";

    public static final String WORD2VEC = "WORD2VEC";

    public static final String HYBRID = "HYBRID";

    public static final String COMBINED = "COMBINED";

    private static final String DEFAULT_ALGORITHM = TFIDF;

    private static final TFIDFSimilarityCalculator TFIDF_INSTANCE = new TFIDFSimilarityCalculator();

    private static final CosineSimilarityCalculator COSINE_INSTANCE = new CosineSimilarityCalculator();

    private static final EditDistanceSimilarityCalculator EDIT_DISTANCE_INSTANCE = new EditDistanceSimilarityCalculator();

    private static final SimHashSimilarityCalculator SIMHASH_INSTANCE = new SimHashSimilarityCalculator();

    private static final HybridSimilarityCalculator HYBRID_INSTANCE = new HybridSimilarityCalculator();

    private static Word2VecSimilarityCalculator WORD2VEC_INSTANCE;

    private SimilarityCalculatorFactory() {
    }

    /**
     * 获取默认的相似度计算器（TF-IDF）
     * 
     * @return TF-IDF相似度计算器实例
     */
    public static SimilarityCalculator getCalculator() {
        return getCalculator(DEFAULT_ALGORITHM);
    }

    /**
     * 根据算法类型获取对应的相似度计算器
     * 
     * @param algorithmType 算法类型（TFIDF, COSINE, EDIT_DISTANCE, SIMHASH, WORD2VEC）
     * @return 对应的相似度计算器实例
     * @throws IllegalArgumentException 如果算法类型不支持
     */
    public static SimilarityCalculator getCalculator(String algorithmType) {
        if (algorithmType == null || algorithmType.isEmpty()) {
            return TFIDF_INSTANCE;
        }

        switch (algorithmType.toUpperCase()) {
            case TFIDF:
                return TFIDF_INSTANCE;
            case COSINE:
                return COSINE_INSTANCE;
            case EDIT_DISTANCE:
                return EDIT_DISTANCE_INSTANCE;
            case SIMHASH:
                return SIMHASH_INSTANCE;
            case HYBRID:
            case COMBINED:
                return HYBRID_INSTANCE;
            case WORD2VEC:
                return getWord2VecInstance();
            default:
                throw new IllegalArgumentException(
                        "不支持的算法类型: " + algorithmType + 
                        "。支持的类型: TFIDF, COSINE, EDIT_DISTANCE, SIMHASH, HYBRID, COMBINED, WORD2VEC"
                );
        }
    }

    /**
     * 获取 Word2Vec 计算器实例（懒加载）
     * 
     * @return Word2Vec 相似度计算器实例
     */
    private static synchronized SimilarityCalculator getWord2VecInstance() {
        if (WORD2VEC_INSTANCE == null) {
            WORD2VEC_INSTANCE = new Word2VecSimilarityCalculator();
        }
        return WORD2VEC_INSTANCE;
    }

    /**
     * 获取带预训练模型的 Word2Vec 计算器
     * 
     * @param corpusFilePath 语料库文件路径
     * @return Word2Vec 相似度计算器实例
     */
    public static SimilarityCalculator getWord2VecCalculator(String corpusFilePath) {
        return new Word2VecSimilarityCalculator(corpusFilePath, DEFAULT_VECTOR_SIZE, DEFAULT_WINDOW_SIZE);
    }

    private static final int DEFAULT_VECTOR_SIZE = 100;
    private static final int DEFAULT_WINDOW_SIZE = 5;

    /**
     * 获取从语料库训练的 Word2Vec 计算器
     * 
     * @param corpusFilePath 语料库文件路径
     * @param vectorSize 向量维度
     * @param windowSize 窗口大小
     * @return Word2Vec 相似度计算器实例
     */
    public static SimilarityCalculator getWord2VecCalculator(String corpusFilePath, int vectorSize, int windowSize) {
        return new Word2VecSimilarityCalculator(corpusFilePath, vectorSize, windowSize);
    }

    /**
     * 检查算法类型是否支持
     * 
     * @param algorithmType 算法类型
     * @return true表示支持，false表示不支持
     */
    public static boolean isSupported(String algorithmType) {
        if (algorithmType == null || algorithmType.isEmpty()) {
            return false;
        }

        switch (algorithmType.toUpperCase()) {
            case TFIDF:
            case COSINE:
            case EDIT_DISTANCE:
            case SIMHASH:
            case HYBRID:
            case COMBINED:
            case WORD2VEC:
                return true;
            default:
                return false;
        }
    }

    /**
     * 获取默认算法类型
     * 
     * @return 默认算法类型（TFIDF）
     */
    public static String getDefaultAlgorithm() {
        return DEFAULT_ALGORITHM;
    }

    /**
     * 获取所有支持的算法类型
     * 
     * @return 算法类型数组
     */
    public static String[] getSupportedAlgorithms() {
        return new String[]{TFIDF, COSINE, EDIT_DISTANCE, SIMHASH, HYBRID, COMBINED, WORD2VEC};
    }

    /**
     * 获取算法描述
     * 
     * @param algorithmType 算法类型
     * @return 算法描述
     */
    public static String getAlgorithmDescription(String algorithmType) {
        if (algorithmType == null) {
            return "未知算法";
        }

        switch (algorithmType.toUpperCase()) {
            case TFIDF:
                return "TF-IDF相似度：基于词频-逆文档频率的相似度计算，适合长文本，考虑词的重要性";
            case COSINE:
                return "余弦相似度：基于词频向量的余弦相似度计算，计算简单，效率高";
            case EDIT_DISTANCE:
                return "编辑距离相似度：基于Levenshtein距离的相似度计算，适合检测抄袭和改写";
            case SIMHASH:
                return "SimHash相似度：基于SimHash指纹的相似度计算，适合大规模文档快速去重";
            case HYBRID:
            case COMBINED:
                return "混合相似度：融合标签与文本相似度，适合作为精确检测阶段的综合算法";
            case WORD2VEC:
                return "Word2Vec相似度：基于词向量的语义相似度计算，能够理解同义词和语义关系";
            default:
                return "未知算法: " + algorithmType;
        }
    }
}
