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
    public String getName() { return "Sentiment Timeline Analysis"; }

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

        double posPct = totalPositive * 100.0 / Math.max(1, posts.size());
        double negPct = totalNegative * 100.0 / Math.max(1, posts.size());
        double neutPct = totalNeutral * 100.0 / Math.max(1, posts.size());

        result.setSummary(String.format(
            "Phân tích %d bài đăng từ %s đến %s. Tích cực: %d (%.1f%%), Tiêu cực: %d (%.1f%%)",
            posts.size(), config.getStartDate(), config.getEndDate(),
            totalPositive, posPct, totalNegative, negPct));

        String overall;
        if (posPct > negPct + 10) {
            overall = "tích cực, với tỷ lệ cảm xúc tích cực cao hơn đáng kể so với tiêu cực";
        } else if (negPct > posPct + 10) {
            overall = "tiêu cực, với nhiều lo ngại và phản ánh từ cộng đồng";
        } else {
            overall = "trung hòa, với cả phản hồi tích cực và tiêu cực đan xen";
        }

        result.setNarrativeSummary(String.format(
            "Báo cáo phân tích tâm lý công chúng về Bão Yagi dựa trên %d bài đăng trên mạng xã hội " +
            "trong giai đoạn từ %s đến %s. Kết quả cho thấy có %d bài đăng mang tâm lý tích cực " +
            "(chiếm %.1f%%), %d bài đăng tiêu cực (%.1f%%), và %d bài đăng trung tính (%.1f%%). " +
            "Nhìn chung, xu hướng dư luận trong giai đoạn này là %s.",
            posts.size(), config.getStartDate(), config.getEndDate(),
            totalPositive, posPct, totalNegative, negPct, totalNeutral, neutPct, overall));

        result.addInsight(String.format(
            "Tỷ lệ bài đăng tích cực (%.1f%%) cao hơn tỷ lệ tiêu cực (%.1f%%), " +
            "cho thấy cộng đồng có sự lạc quan nhất định trong bối cảnh thiên tai.",
            posPct, negPct));
        result.addInsight(String.format(
            "Có %d bài đăng mang sắc thái trung tính (%.1f%%), phản ánh một bộ phận " +
            "người dùng chia sẻ thông tin mà không bày tỏ cảm xúc rõ ràng.",
            totalNeutral, neutPct));

        result.addConclusion(String.format(
            "Dư luận về Bão Yagi trên mạng xã hội nghiêng về %s. Sự chênh lệch " +
            "%.1f điểm phần trăm giữa tỷ lệ tích cực và tiêu cực cho thấy cộng đồng " +
            "đã có những phản ứng đa chiều trước tình hình thiệt hại và công tác cứu trợ.",
            posPct > negPct ? "phía tích cực" : "phía tiêu cực",
            Math.abs(posPct - negPct)));

        result.addRecommendation(
            "Tiếp tục theo dõi diễn biến tâm lý công chúng hàng ngày để kịp thời điều chỉnh " +
            "chiến lược truyền thông và cung cấp thông tin chính xác đến người dân.");
        result.addRecommendation(
            "Tăng cường các chiến dịch truyền thông tích cực nhằm duy trì tinh thần lạc quan " +
            "và khuyến khích sự tham gia của cộng đồng vào công tác khắc phục hậu quả.");
        if (negPct > 30) {
            result.addRecommendation(
                "Cần có biện pháp xử lý các thông tin tiêu cực và lo ngại của người dân, " +
                "đặc biệt là về công tác cứu trợ và khắc phục thiệt hại.");
        }

        return result;
    }
}
