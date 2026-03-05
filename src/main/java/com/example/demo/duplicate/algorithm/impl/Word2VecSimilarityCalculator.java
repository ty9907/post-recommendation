package com.example.demo.duplicate.algorithm.impl;

import com.example.demo.duplicate.algorithm.SimilarityCalculator;
import com.example.demo.duplicate.model.Article;
import com.example.demo.duplicate.model.SimilarityResult;
import com.example.demo.duplicate.util.TextPreprocessor;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Word2Vec 相似度计算器
 * 
 * 使用词向量（Word Embeddings）计算文本的语义相似度。
 * Word2Vec 能够捕捉词语之间的语义关系，适合处理同义词、近义词等语义相似的情况。
 * 
 * 特点：
 * 1. 语义理解：能够识别语义相似但词汇不同的文本
 * 2. 词向量表示：将文本转换为高维向量进行相似度计算
 * 3. 模型支持：支持训练新模型或使用简单的词频向量
 * 
 * @author ty9907
 * @version 1.0
 * @since 2026-03-06
 */
public class Word2VecSimilarityCalculator implements SimilarityCalculator {

    private static final Logger logger = LoggerFactory.getLogger(Word2VecSimilarityCalculator.class);

    private static final String CALCULATOR_NAME = "Word2Vec";

    private static final int DEFAULT_VECTOR_SIZE = 100;

    private static final int DEFAULT_WINDOW_SIZE = 5;

    private static final int DEFAULT_MIN_WORD_FREQUENCY = 5;

    private Word2Vec word2Vec;

    private final TextPreprocessor textPreprocessor;

    private boolean modelLoaded;

    /**
     * 默认构造器 - 不使用预训练模型
     * 将使用简单的词频向量计算
     */
    public Word2VecSimilarityCalculator() {
        this.textPreprocessor = new TextPreprocessor();
        this.modelLoaded = false;
        logger.info("Word2Vec 计算器初始化（无预训练模型），将使用词频向量");
    }

    /**
     * 构造器 - 从语料库训练模型
     * 
     * @param corpusFilePath 语料库文件路径
     * @param vectorSize 向量维度
     * @param windowSize 窗口大小
     */
    public Word2VecSimilarityCalculator(String corpusFilePath, int vectorSize, int windowSize) {
        this.textPreprocessor = new TextPreprocessor();
        this.modelLoaded = trainModel(corpusFilePath, vectorSize, windowSize);
        
        if (modelLoaded) {
            logger.info("Word2Vec 模型训练成功，向量维度：{}，窗口大小：{}", vectorSize, windowSize);
        } else {
            logger.warn("Word2Vec 模型训练失败，将使用简单的词频向量");
        }
    }

    /**
     * 从语料库训练模型
     * 
     * @param corpusFilePath 语料库文件路径
     * @param vectorSize 向量维度
     * @param windowSize 窗口大小
     * @return 是否训练成功
     */
    private boolean trainModel(String corpusFilePath, int vectorSize, int windowSize) {
        try {
            File corpusFile = new File(corpusFilePath);
            if (!corpusFile.exists()) {
                logger.error("语料库文件不存在：{}", corpusFilePath);
                return false;
            }
            
            SentenceIterator iter = new BasicLineIterator(corpusFile);
            TokenizerFactory t = new DefaultTokenizerFactory();
            
            word2Vec = new Word2Vec.Builder()
                    .minWordFrequency(DEFAULT_MIN_WORD_FREQUENCY)
                    .iterations(1)
                    .layerSize(vectorSize)
                    .seed(42)
                    .windowSize(windowSize)
                    .iterate(iter)
                    .tokenizerFactory(t)
                    .build();
            
            word2Vec.fit();
            return true;
        } catch (Exception e) {
            logger.error("训练 Word2Vec 模型失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 计算两篇文章的相似度
     * 
     * @param article1 文章1
     * @param article2 文章2
     * @return 相似度值（0-1）
     */
    @Override
    public double calculateSimilarity(Article article1, Article article2) {
        if (article1 == null || article2 == null) {
            return 0.0;
        }
        
        String text1 = getArticleText(article1);
        String text2 = getArticleText(article2);
        
        if (text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }
        
        if (modelLoaded && word2Vec != null) {
            return calculateWord2VecSimilarity(text1, text2);
        } else {
            return calculateSimpleSimilarity(text1, text2);
        }
    }

    /**
     * 使用 Word2Vec 计算相似度
     * 
     * @param text1 文本1
     * @param text2 文本2
     * @return 相似度值
     */
    private double calculateWord2VecSimilarity(String text1, String text2) {
        try {
            List<String> tokens1 = textPreprocessor.preprocessAndTokenize(text1);
            List<String> tokens2 = textPreprocessor.preprocessAndTokenize(text2);
            
            INDArray vector1 = getTextVector(tokens1);
            INDArray vector2 = getTextVector(tokens2);
            
            if (vector1 == null || vector2 == null) {
                return 0.0;
            }
            
            double cosineSimilarity = Transforms.cosineSim(vector1, vector2);
            return Math.max(0.0, Math.min(1.0, (cosineSimilarity + 1.0) / 2.0));
        } catch (Exception e) {
            logger.error("计算 Word2Vec 相似度失败：{}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * 获取文本的向量表示
     * 
     * @param tokens 分词列表
     * @return 文本向量
     */
    private INDArray getTextVector(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }
        
        List<INDArray> wordVectors = new ArrayList<>();
        
        for (String token : tokens) {
            if (word2Vec.hasWord(token)) {
                wordVectors.add(word2Vec.getWordVectorMatrix(token));
            }
        }
        
        if (wordVectors.isEmpty()) {
            return null;
        }
        
        INDArray sum = Nd4j.zeros(wordVectors.get(0).shape());
        for (INDArray vec : wordVectors) {
            sum.addi(vec);
        }
        
        return sum.div(wordVectors.size());
    }

    /**
     * 使用简单的词频向量计算相似度（无模型时的备选方案）
     * 
     * @param text1 文本1
     * @param text2 文本2
     * @return 相似度值
     */
    private double calculateSimpleSimilarity(String text1, String text2) {
        List<String> tokens1 = textPreprocessor.preprocessAndTokenize(text1);
        List<String> tokens2 = textPreprocessor.preprocessAndTokenize(text2);
        
        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0;
        }
        
        Map<String, Double> vec1 = buildTfVector(tokens1);
        Map<String, Double> vec2 = buildTfVector(tokens2);
        
        return calculateCosineSimilarity(vec1, vec2);
    }

    /**
     * 构建 TF 向量
     */
    private Map<String, Double> buildTfVector(List<String> tokens) {
        Map<String, Double> tf = new HashMap<>();
        for (String token : tokens) {
            tf.merge(token, 1.0, Double::sum);
        }
        double total = tokens.size();
        tf.replaceAll((k, v) -> v / total);
        return tf;
    }

    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(Map<String, Double> vec1, Map<String, Double> vec2) {
        Set<String> allWords = new HashSet<>(vec1.keySet());
        allWords.addAll(vec2.keySet());
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (String word : allWords) {
            double v1 = vec1.getOrDefault(word, 0.0);
            double v2 = vec2.getOrDefault(word, 0.0);
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }
        
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 获取文章文本
     */
    private String getArticleText(Article article) {
        StringBuilder sb = new StringBuilder();
        
        if (article.getTitle() != null) {
            sb.append(article.getTitle()).append(" ");
        }
        
        if (article.getContent() != null) {
            sb.append(article.getContent());
        }
        
        return sb.toString().trim();
    }

    /**
     * 批量计算相似度
     */
    @Override
    public List<SimilarityResult> calculateSimilarities(Article article, List<Article> articles) {
        List<SimilarityResult> results = new ArrayList<>();
        
        if (article == null || articles == null || articles.isEmpty()) {
            return results;
        }
        
        for (Article other : articles) {
            if (other != null && !Objects.equals(article.getId(), other.getId())) {
                double similarity = calculateSimilarity(article, other);
                
                SimilarityResult result = new SimilarityResult();
                result.setArticleId(article.getId());
                result.setComparedArticleId(other.getId());
                result.setSimilarity(similarity);
                result.setAlgorithm(CALCULATOR_NAME);
                result.setCheckTime(LocalDateTime.now());
                
                results.add(result);
            }
        }
        
        return results;
    }

    /**
     * 获取计算器名称
     */
    @Override
    public String getName() {
        return CALCULATOR_NAME;
    }

    /**
     * 检查模型是否已加载
     */
    public boolean isModelLoaded() {
        return modelLoaded;
    }

    /**
     * 获取词汇表大小
     */
    public int getVocabSize() {
        if (word2Vec != null) {
            return word2Vec.getVocab().numWords();
        }
        return 0;
    }

    /**
     * 获取词向量维度
     */
    public int getVectorSize() {
        if (word2Vec != null) {
            return word2Vec.getLayerSize();
        }
        return 0;
    }

    /**
     * 查找相似词
     */
    public List<String> findSimilarWords(String word, int topN) {
        if (word2Vec == null || !word2Vec.hasWord(word)) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(word2Vec.wordsNearest(word, topN));
    }
}
