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
    public String getName() { return "Relief Satisfaction Analysis"; }

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
        double satRate = satisfiedStats != null ? getPositiveRate(satisfiedStats) : 0;
        double disRate = dissatisfiedStats != null ? getNegativeRate(dissatisfiedStats) : 0;

        result.setSummary(String.format(
            "Hài lòng nhất: '%s' (%.0f%% tích cực). Không hài lòng nhất: '%s' (%.0f%% tiêu cực). %s",
            mostSatisfied, satRate,
            mostDissatisfied, disRate,
            buildRecommendation(mostSatisfied, mostDissatisfied)));

        int totalMentions = statsMap.values().stream().mapToInt(s -> s.total).sum();
        long totalPos = statsMap.values().stream().mapToInt(s -> s.positive).sum();
        long totalNeg = statsMap.values().stream().mapToInt(s -> s.negative).sum();

        StringBuilder narrative = new StringBuilder();
        narrative.append(String.format(
            "Báo cáo đánh giá mức độ hài lòng của người dân đối với các loại hàng cứu trợ " +
            "trong bão Bão Yagi, dựa trên %d lượt đề cập trên mạng xã hội. ", totalMentions));
        narrative.append(String.format(
            "Nhìn chung, có %d phản hồi tích cực và %d phản hồi tiêu cực. ", totalPos, totalNeg));

        if (satRate >= 60) {
            narrative.append(String.format(
                "Lĩnh vực được hài lòng nhất là '%s' với %.0f%% phản hồi tích cực, " +
                "cho thấy công tác cứu trợ ở lĩnh vực này đáp ứng tốt nhu cầu người dân. ",
                mostSatisfied, satRate));
        } else {
            narrative.append(String.format(
                "Ngay cả lĩnh vực được đánh giá cao nhất là '%s' cũng chỉ đạt %.0f%% " +
                "phản hồi tích cực, cho thấy còn nhiều thách thức trong công tác cứu trợ. ",
                mostSatisfied, satRate));
        }

        if (disRate >= 40) {
            narrative.append(String.format(
                "Đáng lo ngại, '%s' có tới %.0f%% phản hồi tiêu cực, " +
                "phản ánh sự bất bình đáng kể từ cộng đồng cần được giải quyết.",
                mostDissatisfied, disRate));
        }

        result.setNarrativeSummary(narrative.toString());

        for (Map.Entry<String, ReliefStats> e : statsMap.entrySet()) {
            if (e.getValue().total > 0) {
                double pos = getPositiveRate(e.getValue());
                double neg = getNegativeRate(e.getValue());
                if (pos > 50) {
                    result.addInsight(String.format(
                        "'%s': %d lượt đề cập với %.0f%% tích cực - đây là lĩnh vực " +
                        "được đánh giá cao, cho thấy sự hài lòng của người dân.",
                        e.getKey(), e.getValue().total, pos));
                } else if (neg > 50) {
                    result.addInsight(String.format(
                        "'%s': %d lượt đề cập với %.0f%% tiêu cực - đây là lĩnh vực " +
                        "cần được cải thiện khẩn cấp.",
                        e.getKey(), e.getValue().total, neg));
                } else {
                    result.addInsight(String.format(
                        "'%s': %d lượt đề cập với phản hồi trái chiều " +
                        "(%.0f%% tích cực, %.0f%% tiêu cực).",
                        e.getKey(), e.getValue().total, pos, neg));
                }
            }
        }

        if (totalPos < totalNeg) {
            result.addConclusion(String.format(
                "Tổng thể, mức độ hài lòng với các hàng cứu trợ còn thấp " +
                "(%d tích cực so với %d tiêu cực). Công tác cứu trợ cần được " +
                "cải thiện toàn diện để đáp ứng kỳ vọng của người dân.",
                totalPos, totalNeg));
        } else {
            result.addConclusion(String.format(
                "Tổng thể, mức độ hài lòng với các hàng cứu trợ ở mức khả quan " +
                "(%d tích cực so với %d tiêu cực). Tuy nhiên vẫn cần tiếp tục " +
                "cải thiện ở những lĩnh vực còn tồn tại.",
                totalPos, totalNeg));
        }

        result.addRecommendation(
            "Tập trung nguồn lực cải thiện các lĩnh vực có tỷ lệ phản hồi tiêu cực cao, " +
            "đặc biệt là các vấn đề về nhà ở tạm thời và giao thông tiếp cận.");
        result.addRecommendation(
            "Duy trì và nhân rộng các mô hình cứu trợ hiệu quả ở những lĩnh vực " +
            "được người dân đánh giá cao.");

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
