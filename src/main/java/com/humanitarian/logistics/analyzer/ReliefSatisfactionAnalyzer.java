package com.humanitarian.logistics.analyzer;

import com.humanitarian.logistics.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class ReliefSatisfactionAnalyzer implements Analyzer {
    private final SentimentAnalyzer sentimentAnalyzer;

    public ReliefSatisfactionAnalyzer(SentimentAnalyzer sentimentAnalyzer) {
        this.sentimentAnalyzer = sentimentAnalyzer;
    }

    @Override
    public String getName() { return "Relief Satisfaction Analysis (Bài toán 3)"; }

    @Override
    public String getDescription() {
        return "Xác định mức độ hài lòng/không hài lòng của công chúng với các loại hàng cứu trợ";
    }

    @Override
    public boolean isAvailable() { return sentimentAnalyzer.isAvailable(); }

    @Override
    public AnalysisResult analyze(List<Post> posts, AnalysisConfig config) {
        AnalysisResult result = new AnalysisResult(getName(), "relief_satisfaction");
        Map<String, List<String>> reliefCategories = config.getReliefCategories();
        Map<String, ReliefStats> statsMap = new LinkedHashMap<>();

        for (String category : reliefCategories.keySet()) {
            statsMap.put(category, new ReliefStats());
        }

        List<SentimentResult> sentiments = sentimentAnalyzer.analyzeBatch(posts);

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            SentimentResult sentiment = sentiments.get(i);
            String text = post.getContent().toLowerCase();

            for (Map.Entry<String, List<String>> entry : reliefCategories.entrySet()) {
                String category = entry.getKey();
                List<String> keywords = entry.getValue();
                boolean matched = keywords.stream().anyMatch(kw -> text.contains(kw.toLowerCase()));
                if (matched) {
                    ReliefStats stats = statsMap.get(category);
                    stats.total++;
                    if (sentiment.isPositive()) stats.positive++;
                    else if (sentiment.isNegative()) stats.negative++;
                    else stats.neutral++;
                }
            }
        }

        for (Map.Entry<String, ReliefStats> entry : statsMap.entrySet()) {
            ReliefStats stats = entry.getValue();
            result.addChartData(new AnalysisResult.ChartDataPoint(
                entry.getKey(), stats.positive, "Hài lòng", "Tích cực"));
            result.addChartData(new AnalysisResult.ChartDataPoint(
                entry.getKey(), stats.negative, "Không hài lòng", "Tiêu cực"));
            result.addChartData(new AnalysisResult.ChartDataPoint(
                entry.getKey(), stats.neutral, "Trung tính", "Trung tính"));
        }

        result.addData("reliefStats", statsMap);

        String mostSatisfied = statsMap.entrySet().stream()
            .filter(e -> e.getValue().total > 0)
            .max(Comparator.comparingDouble(e -> (double) e.getValue().positive / Math.max(1, e.getValue().total)))
            .map(Map.Entry::getKey).orElse("N/A");

        String mostDissatisfied = statsMap.entrySet().stream()
            .filter(e -> e.getValue().total > 0)
            .max(Comparator.comparingDouble(e -> (double) e.getValue().negative / Math.max(1, e.getValue().total)))
            .map(Map.Entry::getKey).orElse("N/A");

        result.addData("mostSatisfied", mostSatisfied);
        result.addData("mostDissatisfied", mostDissatisfied);

        ReliefStats satisfiedStats = statsMap.get(mostSatisfied);
        ReliefStats dissatisfiedStats = statsMap.get(mostDissatisfied);
        result.setSummary(String.format(
            "Hài lòng nhất: '%s' (%.0f%% tích cực). Không hài lòng nhất: '%s' (%.0f%% tiêu cực). %s",
            mostSatisfied,
            satisfiedStats != null ? getPositiveRate(satisfiedStats) : 0,
            mostDissatisfied,
            dissatisfiedStats != null ? getNegativeRate(dissatisfiedStats) : 0,
            buildRecommendation(mostSatisfied, mostDissatisfied)));

        return result;
    }

    private double getPositiveRate(ReliefStats s) {
        return s.total > 0 ? s.positive * 100.0 / s.total : 0;
    }

    private double getNegativeRate(ReliefStats s) {
        return s.total > 0 ? s.negative * 100.0 / s.total : 0;
    }

    private String buildRecommendation(String satisfied, String dissatisfied) {
        if ("Nhà ở".equals(dissatisfied) || "Giao thông".equals(dissatisfied)) {
            return "Cần ưu tiên nguồn lực cho " + dissatisfied + ".";
        }
        return "Tiếp tục duy trì hiệu quả ở lĩnh vực " + satisfied + ".";
    }

    public static class ReliefStats {
        int total = 0;
        int positive = 0;
        int negative = 0;
        int neutral = 0;
    }
}
