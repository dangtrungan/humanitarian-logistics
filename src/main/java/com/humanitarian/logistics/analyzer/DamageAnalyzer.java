package com.humanitarian.logistics.analyzer;

import com.humanitarian.logistics.model.*;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

public class DamageAnalyzer implements Analyzer {
    private final SentimentAnalyzer sentimentAnalyzer;

    public DamageAnalyzer(SentimentAnalyzer sentimentAnalyzer) {
        this.sentimentAnalyzer = sentimentAnalyzer;
    }

    @Override
    public String getName() { return "Damage Type Analysis (Bài toán 2)"; }

    @Override
    public String getDescription() {
        return "Xác định mức độ và loại thiệt hại phổ biến nhất từ các bài đăng mạng xã hội";
    }

    @Override
    public boolean isAvailable() { return sentimentAnalyzer.isAvailable(); }

    @Override
    public AnalysisResult analyze(List<Post> posts, AnalysisConfig config) {
        AnalysisResult result = new AnalysisResult(getName(), "damage_analysis");
        Map<String, Map<String, Integer>> damageCounts = new LinkedHashMap<>();
        Map<String, List<String>> damageCategories = config.getDamageCategories();
        Map<String, List<String>> normalizedCategories = normalizeKeys(damageCategories);

        for (String category : damageCategories.keySet()) {
            damageCounts.put(category, new HashMap<>());
        }

        for (Post post : posts) {
            String text = removeAccents(post.getContent().toLowerCase());
            for (Map.Entry<String, List<String>> entry : normalizedCategories.entrySet()) {
                String category = entry.getKey();
                List<String> subCategories = entry.getValue();
                for (String sub : subCategories) {
                    if (text.contains(sub)) {
                        Map<String, Integer> counts = damageCounts.get(category);
                        counts.put(sub, counts.getOrDefault(sub, 0) + 1);
                        counts.put("total", counts.getOrDefault("total", 0) + 1);
                        break;
                    }
                }
            }
        }

        for (Map.Entry<String, Map<String, Integer>> entry : damageCounts.entrySet()) {
            result.addChartData(new AnalysisResult.ChartDataPoint(
                entry.getKey(), entry.getValue().getOrDefault("total", 0), "damage"));
        }

        Map<String, Integer> categoryTotals = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Integer>> entry : damageCounts.entrySet()) {
            categoryTotals.put(entry.getKey(), entry.getValue().getOrDefault("total", 0));
        }

        String mostMentioned = categoryTotals.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");

        result.addData("categoryTotals", categoryTotals);
        result.addData("mostMentioned", mostMentioned);
        result.addData("damageDetails", damageCounts);
        int totalMentions = categoryTotals.values().stream().mapToInt(Integer::intValue).sum();
        result.addData("totalMentions", totalMentions);

        if (totalMentions == 0) {
            result.setSummary("Không tìm thấy đề cập thiệt hại nào trong dữ liệu thu thập được.");
        } else {
            result.setSummary(String.format(
                "Thiệt hại được đề cập nhiều nhất: '%s'. " +
                "Tổng số lượt đề cập thiệt hại: %d. " +
                "Phân bố: %s",
                mostMentioned,
                totalMentions,
                categoryTotals.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .map(e -> e.getKey() + "(" + e.getValue() + ")")
                    .collect(Collectors.joining(", "))));
        }

        return result;
    }

    private String removeAccents(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }

    private Map<String, List<String>> normalizeKeys(Map<String, List<String>> categories) {
        Map<String, List<String>> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
            normalized.put(entry.getKey(), entry.getValue().stream()
                .map(s -> removeAccents(s.toLowerCase()))
                .collect(Collectors.toList()));
        }
        return normalized;
    }
}
