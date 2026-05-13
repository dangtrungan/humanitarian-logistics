package com.humanitarian.logistics.analyzer;

import com.humanitarian.logistics.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReliefTimelineAnalyzer implements Analyzer {
    private final SentimentAnalyzer sentimentAnalyzer;

    public ReliefTimelineAnalyzer(SentimentAnalyzer sentimentAnalyzer) {
        this.sentimentAnalyzer = sentimentAnalyzer;
    }

    @Override
    public String getName() { return "Relief Timeline Sentiment Analysis (Bài toán 4)"; }

    @Override
    public String getDescription() {
        return "Theo dõi tâm lý theo thời gian cho từng loại hàng cứu trợ (tiền mặt, y tế, nhà ở, thực phẩm, giao thông)";
    }

    @Override
    public boolean isAvailable() { return sentimentAnalyzer.isAvailable(); }

    @Override
    public AnalysisResult analyze(List<Post> posts, AnalysisConfig config) {
        AnalysisResult result = new AnalysisResult(getName(), "relief_timeline");
        Map<String, List<String>> reliefCategories = config.getReliefCategories();
        List<SentimentResult> sentiments = sentimentAnalyzer.analyzeBatch(posts);

        Map<String, Map<LocalDate, ReliefDailyStats>> timelineStats = new LinkedHashMap<>();

        for (String category : reliefCategories.keySet()) {
            timelineStats.put(category, new TreeMap<>());
        }

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            SentimentResult sentiment = sentiments.get(i);
            String text = post.getContent().toLowerCase();
            LocalDate date = post.getTimestamp().toLocalDate();

            for (Map.Entry<String, List<String>> entry : reliefCategories.entrySet()) {
                String category = entry.getKey();
                List<String> keywords = entry.getValue();
                boolean matched = keywords.stream().anyMatch(kw -> text.contains(kw.toLowerCase()));
                if (matched) {
                    Map<LocalDate, ReliefDailyStats> dailyMap = timelineStats.get(category);
                    dailyMap.putIfAbsent(date, new ReliefDailyStats());
                    ReliefDailyStats daily = dailyMap.get(date);
                    daily.total++;
                    if (sentiment.isPositive()) daily.positive++;
                    else if (sentiment.isNegative()) daily.negative++;
                }
            }
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Map.Entry<String, Map<LocalDate, ReliefDailyStats>> catEntry : timelineStats.entrySet()) {
            String category = catEntry.getKey();
            Map<LocalDate, ReliefDailyStats> dailyMap = catEntry.getValue();
            for (Map.Entry<LocalDate, ReliefDailyStats> dayEntry : dailyMap.entrySet()) {
                String dateStr = dayEntry.getKey().format(fmt);
                ReliefDailyStats stats = dayEntry.getValue();
                result.addChartData(new AnalysisResult.ChartDataPoint(
                    dateStr, stats.positive, category + "_pos", category + " (Tích cực)"));
                result.addChartData(new AnalysisResult.ChartDataPoint(
                    dateStr, stats.negative, category + "_neg", category + " (Tiêu cực)"));
            }
        }

        Map<String, ReliefSummary> summaries = new LinkedHashMap<>();
        for (Map.Entry<String, Map<LocalDate, ReliefDailyStats>> catEntry : timelineStats.entrySet()) {
            String category = catEntry.getKey();
            Map<LocalDate, ReliefDailyStats> dailyMap = catEntry.getValue();
            int total = dailyMap.values().stream().mapToInt(s -> s.total).sum();
            int pos = dailyMap.values().stream().mapToInt(s -> s.positive).sum();
            int neg = dailyMap.values().stream().mapToInt(s -> s.negative).sum();
            summaries.put(category, new ReliefSummary(total, pos, neg));
        }

        result.addData("reliefSummaries", summaries);

        List<String> trendDescriptions = buildTrendDescriptions(summaries);
        result.addData("trendDescriptions", trendDescriptions);

        result.setSummary(String.format(
            "Phân tích tâm lý theo thời gian cho %d loại hàng cứu trợ. %s",
            reliefCategories.size(), String.join(" ", trendDescriptions)));

        return result;
    }

    private List<String> buildTrendDescriptions(Map<String, ReliefSummary> summaries) {
        List<String> descs = new ArrayList<>();
        for (Map.Entry<String, ReliefSummary> entry : summaries.entrySet()) {
            ReliefSummary s = entry.getValue();
            if (s.total == 0) continue;
            double posRatio = (double) s.positive / s.total;
            if (posRatio >= 0.6) {
                descs.add(entry.getKey() + ": Tích cực (" + (int)(posRatio*100) + "%).");
            } else if (posRatio <= 0.3) {
                descs.add(entry.getKey() + ": Tiêu cực (" + (int)((1-posRatio)*100) + "%).");
            } else {
                descs.add(entry.getKey() + ": Trung tính.");
            }
        }
        return descs;
    }

    static class ReliefDailyStats {
        int total = 0;
        int positive = 0;
        int negative = 0;
    }

    static class ReliefSummary {
        int total;
        int positive;
        int negative;

        ReliefSummary(int total, int positive, int negative) {
            this.total = total;
            this.positive = positive;
            this.negative = negative;
        }
    }
}
