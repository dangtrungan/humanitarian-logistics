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
    public String getName() { return "Relief Timeline Sentiment Analysis"; }

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

        int grandTotal = summaries.values().stream().mapToInt(s -> s.total).sum();
        int grandPos = summaries.values().stream().mapToInt(s -> s.positive).sum();
        int grandNeg = summaries.values().stream().mapToInt(s -> s.negative).sum();

        StringBuilder narrative = new StringBuilder();
        narrative.append(String.format(
            "Báo cáo theo dõi diễn biến tâm lý công chúng đối với %d loại hàng cứu trợ " +
            "trong suốt thời gian xảy ra Bão Yagi, dựa trên %d lượt đề cập trên mạng xã hội. ",
            reliefCategories.size(), grandTotal));
        narrative.append(String.format(
            "Tổng quan, có %d phản hồi tích cực và %d phản hồi tiêu cực. ",
            grandPos, grandNeg));

        List<String> positiveCats = new ArrayList<>();
        List<String> negativeCats = new ArrayList<>();
        List<String> neutralCats = new ArrayList<>();

        for (Map.Entry<String, ReliefSummary> e : summaries.entrySet()) {
            if (e.getValue().total == 0) continue;
            double posRatio = (double) e.getValue().positive / e.getValue().total;
            if (posRatio >= 0.6) {
                positiveCats.add(e.getKey());
            } else if (posRatio <= 0.3) {
                negativeCats.add(e.getKey());
            } else {
                neutralCats.add(e.getKey());
            }
        }

        if (!positiveCats.isEmpty()) {
            narrative.append("Lĩnh vực được đánh giá tích cực: ");
            narrative.append(String.join(", ", positiveCats));
            narrative.append(". ");
        }
        if (!negativeCats.isEmpty()) {
            narrative.append("Lĩnh vực còn nhiều bất cập: ");
            narrative.append(String.join(", ", negativeCats));
            narrative.append(". ");
        }
        if (!neutralCats.isEmpty()) {
            narrative.append("Lĩnh vực có phản hồi trung tính: ");
            narrative.append(String.join(", ", neutralCats));
            narrative.append(". ");
        }

        String trend;
        if (grandPos > grandNeg * 2) {
            trend = "tích cực, với tỷ lệ hài lòng vượt trội so với không hài lòng";
        } else if (grandNeg > grandPos * 2) {
            trend = "đáng lo ngại, với lượng phản hồi tiêu cực áp đảo";
        } else {
            trend = "trung hòa, với các ý kiến trái chiều đan xen";
        }
        narrative.append(String.format("Nhìn chung, xu hướng dư luận về hàng cứu trợ là %s.", trend));

        result.setNarrativeSummary(narrative.toString());

        for (Map.Entry<String, ReliefSummary> e : summaries.entrySet()) {
            if (e.getValue().total == 0) continue;
            double posRatio = (double) e.getValue().positive / e.getValue().total;
            double negRatio = (double) e.getValue().negative / e.getValue().total;
            String insight;
            if (posRatio >= 0.8) {
                insight = String.format(
                    "'%s' có tỷ lệ hài lòng rất cao (%.0f%%), " +
                    "cho thấy công tác cứu trợ ở lĩnh vực này rất hiệu quả.",
                    e.getKey(), posRatio * 100);
            } else if (negRatio >= 0.6) {
                insight = String.format(
                    "'%s' có tỷ lệ không hài lòng cao (%.0f%%), " +
                    "cần được ưu tiên cải thiện ngay.",
                    e.getKey(), negRatio * 100);
            } else {
                insight = String.format(
                    "'%s' có %d lượt đề cập (%.0f%% tích cực, %.0f%% tiêu cực), " +
                    "phản ánh nhu cầu và mức độ quan tâm nhất định từ cộng đồng.",
                    e.getKey(), e.getValue().total, posRatio * 100, negRatio * 100);
            }
            result.addInsight(insight);
        }

        if (grandPos > grandNeg) {
            result.addConclusion(String.format(
                "Nhìn chung, người dân có phản hồi tích cực hơn là tiêu cực về các hàng cứu trợ " +
                "(%d so với %d). Điều này cho thấy công tác cứu trợ bước đầu đáp ứng được " +
                "nhu cầu cơ bản của người dân vùng bão.",
                grandPos, grandNeg));
        } else {
            result.addConclusion(String.format(
                "Tỷ lệ phản hồi tiêu cực (%d) cao hơn tích cực (%d) cho thấy công tác cứu trợ " +
                "còn nhiều bất cập và chưa đáp ứng kịp thời nhu cầu của người dân.",
                grandNeg, grandPos));
        }

        result.addRecommendation(
            "Theo dõi sát sao diễn biến tâm lý theo từng loại hàng cứu trợ để có điều chỉnh " +
            "kịp thời trong phân bổ nguồn lực.");
        result.addRecommendation(
            "Tăng cường truyền thông về tiến độ cứu trợ ở các lĩnh vực có phản hồi tiêu cực, " +
            "đồng thời công bố minh bạch kế hoạch khắc phục.");
        if (!negativeCats.isEmpty()) {
            result.addRecommendation(String.format(
                "Cần đặc biệt quan tâm cải thiện các lĩnh vực %s, " +
                "nơi người dân đang có nhiều bức xúc và lo ngại nhất.",
                String.join(", ", negativeCats)));
        }

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
