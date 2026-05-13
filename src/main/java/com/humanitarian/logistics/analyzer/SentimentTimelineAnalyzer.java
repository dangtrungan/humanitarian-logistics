package com.humanitarian.logistics.analyzer;

import com.humanitarian.logistics.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SentimentTimelineAnalyzer implements Analyzer {
    private final SentimentAnalyzer sentimentAnalyzer;

    public SentimentTimelineAnalyzer(SentimentAnalyzer sentimentAnalyzer) {
        this.sentimentAnalyzer = sentimentAnalyzer;
    }

    @Override
    public String getName() { return "Sentiment Timeline Analysis (Bài toán 1)"; }

    @Override
    public String getDescription() {
        return "Theo dõi sự thay đổi tâm lý công chúng theo thời gian (positive/negative counts per day)";
    }

    @Override
    public boolean isAvailable() { return sentimentAnalyzer.isAvailable(); }

    @Override
    public AnalysisResult analyze(List<Post> posts, AnalysisConfig config) {
        AnalysisResult result = new AnalysisResult(getName(), "sentiment_timeline");

        List<SentimentResult> sentiments = sentimentAnalyzer.analyzeBatch(posts);

        Map<LocalDate, Map<String, Integer>> dailyCounts = new TreeMap<>();

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            SentimentResult sentiment = sentiments.get(i);

            LocalDate date = post.getTimestamp().toLocalDate();
            dailyCounts.putIfAbsent(date, new HashMap<>());
            Map<String, Integer> counts = dailyCounts.get(date);

            String label = sentiment.getLabel();
            counts.put(label, counts.getOrDefault(label, 0) + 1);
            counts.put("total", counts.getOrDefault("total", 0) + 1);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Map.Entry<LocalDate, Map<String, Integer>> entry : dailyCounts.entrySet()) {
            String dateStr = entry.getKey().format(fmt);
            Map<String, Integer> counts = entry.getValue();
            result.addChartData(new AnalysisResult.ChartDataPoint(dateStr,
                counts.getOrDefault("POSITIVE", 0), "POSITIVE", "Tích cực"));
            result.addChartData(new AnalysisResult.ChartDataPoint(dateStr,
                counts.getOrDefault("NEGATIVE", 0), "NEGATIVE", "Tiêu cực"));
            result.addChartData(new AnalysisResult.ChartDataPoint(dateStr,
                counts.getOrDefault("NEUTRAL", 0), "NEUTRAL", "Trung tính"));
        }

        long totalPositive = sentiments.stream().filter(SentimentResult::isPositive).count();
        long totalNegative = sentiments.stream().filter(SentimentResult::isNegative).count();
        long totalNeutral = sentiments.stream().filter(SentimentResult::isNeutral).count();

        result.addData("totalPosts", posts.size());
        result.addData("totalPositive", totalPositive);
        result.addData("totalNegative", totalNegative);
        result.addData("totalNeutral", totalNeutral);
        result.addData("positiveRatio", posts.isEmpty() ? 0 : (double) totalPositive / posts.size());
        result.addData("negativeRatio", posts.isEmpty() ? 0 : (double) totalNegative / posts.size());

        result.setSummary(String.format(
            "Phân tích %d bài đăng từ %s đến %s. Tích cực: %d (%.1f%%), Tiêu cực: %d (%.1f%%)",
            posts.size(), config.getStartDate(), config.getEndDate(),
            totalPositive, totalPositive * 100.0 / Math.max(1, posts.size()),
            totalNegative, totalNegative * 100.0 / Math.max(1, posts.size())));

        return result;
    }
}
