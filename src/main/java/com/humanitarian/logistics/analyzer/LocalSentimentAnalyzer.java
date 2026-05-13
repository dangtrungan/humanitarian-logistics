package com.humanitarian.logistics.analyzer;

import com.humanitarian.logistics.model.SentimentResult;
import com.humanitarian.logistics.model.Post;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

public class LocalSentimentAnalyzer implements SentimentAnalyzer {
    private final Map<String, Double> positiveWords;
    private final Map<String, Double> negativeWords;

    public LocalSentimentAnalyzer() {
        positiveWords = loadPositiveWords();
        negativeWords = loadNegativeWords();
        addNormalizedKeys(positiveWords);
        addNormalizedKeys(negativeWords);
    }

    @Override
    public String getName() { return "Local Sentiment Analyzer (Keyword-based)"; }

    @Override
    public boolean isAvailable() { return true; }

    @Override
    public SentimentResult analyze(Post post) {
        String text = normalizeText(post.getContent());
        double positiveScore = calculateScore(text, positiveWords);
        double negativeScore = calculateScore(text, negativeWords);

        double total = positiveScore + negativeScore;
        double neutralScore = Math.max(0, 1.0 - total);

        if (total == 0) {
            return new SentimentResult(post.getId(), 0, 0, 1.0, "NEUTRAL");
        }

        double pos = positiveScore / total;
        double neg = negativeScore / total;

        String label;
        if (pos > neg + 0.1) label = "POSITIVE";
        else if (neg > pos + 0.1) label = "NEGATIVE";
        else label = "NEUTRAL";

        return new SentimentResult(post.getId(), pos, neg, Math.min(neutralScore, 1.0), label);
    }

    @Override
    public List<SentimentResult> analyzeBatch(List<Post> posts) {
        return posts.stream().map(this::analyze).collect(Collectors.toList());
    }

    private double calculateScore(String text, Map<String, Double> wordMap) {
        double score = 0;
        for (Map.Entry<String, Double> entry : wordMap.entrySet()) {
            if (text.contains(entry.getKey())) {
                score += entry.getValue();
            }
        }
        return Math.min(score, 1.0);
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        String lower = text.toLowerCase();
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }

    private void addNormalizedKeys(Map<String, Double> map) {
        Map<String, Double> normalized = new HashMap<>();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            String nk = normalizeText(entry.getKey());
            if (!nk.equals(entry.getKey())) {
                normalized.put(nk, entry.getValue());
            }
        }
        map.putAll(normalized);
    }

    private Map<String, Double> loadPositiveWords() {
        Map<String, Double> words = new HashMap<>();
        words.put("cảm ơn", 0.8); words.put("tuyệt vời", 0.9);
        words.put("đoàn kết", 0.7); words.put("giúp đỡ", 0.6);
        words.put("kịp thời", 0.7); words.put("khâm phục", 0.8);
        words.put("hảo tâm", 0.7); words.put("khẩn trương", 0.5);
        words.put("khen ngợi", 0.8); words.put("hỗ trợ", 0.5);
        words.put("hy vọng", 0.6); words.put("phục hồi", 0.5);
        words.put("tốt", 0.6); words.put("hiệu quả", 0.7);
        words.put("thành công", 0.8); words.put("hài lòng", 0.7);
        words.put("an toàn", 0.6); words.put("vui mừng", 0.8);
        words.put("biết ơn", 0.9); words.put("tích cực", 0.6);
        words.put("ổn định", 0.5); words.put("đủ", 0.4);
        words.put("good", 0.6); words.put("thank", 0.8);
        words.put("great", 0.8); words.put("help", 0.5);
        words.put("hope", 0.6); words.put("grateful", 0.8);
        return words;
    }

    private Map<String, Double> loadNegativeWords() {
        Map<String, Double> words = new HashMap<>();
        words.put("sập", 0.8); words.put("phá hủy", 0.9);
        words.put("thiệt hại", 0.7); words.put("ngập", 0.6);
        words.put("sạt lở", 0.8); words.put("tê liệt", 0.7);
        words.put("khó khăn", 0.6); words.put("chết", 0.9);
        words.put("tử vong", 0.9); words.put("thương", 0.7);
        words.put("mất tích", 0.8); words.put("chậm", 0.5);
        words.put("không công bằng", 0.7); words.put("thiếu", 0.6);
        words.put("nghiêm trọng", 0.6); words.put("lo lắng", 0.6);
        words.put("sợ", 0.7); words.put("bức xúc", 0.8);
        words.put("thất vọng", 0.8); words.put("khủng khiếp", 0.9);
        words.put("cô lập", 0.6); words.put("mất", 0.5);
        words.put("nguy hiểm", 0.7); words.put("đau", 0.7);
        words.put("bad", 0.6); words.put("terrible", 0.8);
        words.put("awful", 0.8); words.put("failure", 0.7);
        words.put("damage", 0.6); words.put("destroy", 0.8);
        return words;
    }
}
