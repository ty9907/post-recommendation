package com.example.demo.tag.util;

import com.example.demo.tag.model.Tag;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 标签质量评估工具类
 * 用于评估标签提取的准确率、召回率、F1分数等关键指标
 */
public class TagQualityEvaluator {
    
    /**
     * 评估结果类
     * 包含准确率、召回率、F1分数等评估指标
     */
    public static class EvaluationResult {
        private double precision;
        private double recall;
        private double f1Score;
        private int truePositives;
        private int falsePositives;
        private int falseNegatives;
        private int totalExtracted;
        private int totalExpected;
        
        public EvaluationResult(double precision, double recall, double f1Score,
                              int truePositives, int falsePositives, int falseNegatives,
                              int totalExtracted, int totalExpected) {
            this.precision = precision;
            this.recall = recall;
            this.f1Score = f1Score;
            this.truePositives = truePositives;
            this.falsePositives = falsePositives;
            this.falseNegatives = falseNegatives;
            this.totalExtracted = totalExtracted;
            this.totalExpected = totalExpected;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== 标签质量评估结果 ===\n");
            sb.append(String.format("准确率 (Precision): %.2f%%\n", precision * 100));
            sb.append(String.format("召回率 (Recall): %.2f%%\n", recall * 100));
            sb.append(String.format("F1分数 (F1-Score): %.2f%%\n", f1Score * 100));
            sb.append(String.format("真正例 (True Positives): %d\n", truePositives));
            sb.append(String.format("假正例 (False Positives): %d\n", falsePositives));
            sb.append(String.format("假负例 (False Negatives): %d\n", falseNegatives));
            sb.append(String.format("提取标签总数: %d\n", totalExtracted));
            sb.append(String.format("期望标签总数: %d\n", totalExpected));
            return sb.toString();
        }
        
        public double getPrecision() { return precision; }
        public double getRecall() { return recall; }
        public double getF1Score() { return f1Score; }
        public int getTruePositives() { return truePositives; }
        public int getFalsePositives() { return falsePositives; }
        public int getFalseNegatives() { return falseNegatives; }
        public int getTotalExtracted() { return totalExtracted; }
        public int getTotalExpected() { return totalExpected; }
    }
    
    /**
     * 评估标签提取质量
     * @param extractedTags 提取的标签列表
     * @param expectedTags 期望的标签列表（人工标注的正确标签）
     * @return 评估结果
     */
    public static EvaluationResult evaluate(List<Tag> extractedTags, List<String> expectedTags) {
        Set<String> extractedSet = new HashSet<>();
        for (Tag tag : extractedTags) {
            extractedSet.add(tag.getName().toLowerCase());
        }
        
        Set<String> expectedSet = new HashSet<>();
        for (String tag : expectedTags) {
            expectedSet.add(tag.toLowerCase());
        }
        
        Set<String> intersection = new HashSet<>(extractedSet);
        intersection.retainAll(expectedSet);
        
        int truePositives = intersection.size();
        int falsePositives = extractedSet.size() - truePositives;
        int falseNegatives = expectedSet.size() - truePositives;
        
        double precision = extractedSet.size() > 0 ? (double) truePositives / extractedSet.size() : 0.0;
        double recall = expectedSet.size() > 0 ? (double) truePositives / expectedSet.size() : 0.0;
        double f1Score = (precision + recall) > 0 ? 2 * precision * recall / (precision + recall) : 0.0;
        
        return new EvaluationResult(
            precision, recall, f1Score,
            truePositives, falsePositives, falseNegatives,
            extractedSet.size(), expectedSet.size()
        );
    }
    
    /**
     * 评估标签提取质量（字符串列表版本）
     * @param extractedTags 提取的标签名称列表
     * @param expectedTags 期望的标签列表
     * @return 评估结果
     */
    public static EvaluationResult evaluateStrings(List<String> extractedTags, List<String> expectedTags) {
        Set<String> extractedSet = new HashSet<>();
        for (String tag : extractedTags) {
            extractedSet.add(tag.toLowerCase());
        }
        
        Set<String> expectedSet = new HashSet<>();
        for (String tag : expectedTags) {
            expectedSet.add(tag.toLowerCase());
        }
        
        Set<String> intersection = new HashSet<>(extractedSet);
        intersection.retainAll(expectedSet);
        
        int truePositives = intersection.size();
        int falsePositives = extractedSet.size() - truePositives;
        int falseNegatives = expectedSet.size() - truePositives;
        
        double precision = extractedSet.size() > 0 ? (double) truePositives / extractedSet.size() : 0.0;
        double recall = expectedSet.size() > 0 ? (double) truePositives / expectedSet.size() : 0.0;
        double f1Score = (precision + recall) > 0 ? 2 * precision * recall / (precision + recall) : 0.0;
        
        return new EvaluationResult(
            precision, recall, f1Score,
            truePositives, falsePositives, falseNegatives,
            extractedSet.size(), expectedSet.size()
        );
    }
    
    /**
     * 对比两个标签提取器的性能
     * @param extractor1Name 提取器1名称
     * @param result1 提取器1的评估结果
     * @param extractor2Name 提取器2名称
     * @param result2 提取器2的评估结果
     * @return 对比报告
     */
    public static String compareResults(String extractor1Name, EvaluationResult result1,
                                       String extractor2Name, EvaluationResult result2) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== 标签提取器性能对比 ===\n\n");
        sb.append(String.format("%-25s %s\n", "指标", extractor1Name + " vs " + extractor2Name));
        sb.append(String.format("%-25s %.2f%% vs %.2f%% (提升: %.2f%%)\n", 
            "准确率", 
            result1.getPrecision() * 100, 
            result2.getPrecision() * 100,
            (result2.getPrecision() - result1.getPrecision()) * 100));
        sb.append(String.format("%-25s %.2f%% vs %.2f%% (提升: %.2f%%)\n", 
            "召回率", 
            result1.getRecall() * 100, 
            result2.getRecall() * 100,
            (result2.getRecall() - result1.getRecall()) * 100));
        sb.append(String.format("%-25s %.2f%% vs %.2f%% (提升: %.2f%%)\n", 
            "F1分数", 
            result1.getF1Score() * 100, 
            result2.getF1Score() * 100,
            (result2.getF1Score() - result1.getF1Score()) * 100));
        sb.append(String.format("%-25s %d vs %d\n", "真正例", result1.getTruePositives(), result2.getTruePositives()));
        sb.append(String.format("%-25s %d vs %d\n", "假正例", result1.getFalsePositives(), result2.getFalsePositives()));
        sb.append(String.format("%-25s %d vs %d\n", "假负例", result1.getFalseNegatives(), result2.getFalseNegatives()));
        return sb.toString();
    }
}
