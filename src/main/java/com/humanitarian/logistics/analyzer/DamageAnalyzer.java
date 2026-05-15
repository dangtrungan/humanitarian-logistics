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
    public String getName() { return "Damage Type Analysis"; }

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
            result.setNarrativeSummary(
                "Không phát hiện đề cập nào về thiệt hại trong các bài đăng thu thập được. " +
                "Điều này có thể do dữ liệu chưa đầy đủ hoặc các bài đăng tập trung vào " +
                "khía cạnh khác của thảm họa.");
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

            List<String> categoriesWithData = categoryTotals.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> e.getKey() + " (" + e.getValue() + " lượt)")
                .collect(Collectors.toList());

            StringBuilder narrative = new StringBuilder();
            narrative.append(String.format(
                "Báo cáo phân tích thiệt hại từ Bão Yagi dựa trên %d lượt đề cập " +
                "trong các bài đăng mạng xã hội. ",
                totalMentions));
            narrative.append(String.format(
                "Loại thiệt hại được nhắc đến nhiều nhất là '%s'. ", mostMentioned));
            narrative.append("Các loại thiệt hại được ghi nhận bao gồm: ");
            narrative.append(String.join("; ", categoriesWithData));
            narrative.append(". ");

            int maxVal = categoryTotals.values().stream().mapToInt(Integer::intValue).max().orElse(0);
            double concentration = maxVal * 100.0 / totalMentions;
            if (concentration > 50) {
                narrative.append(String.format(
                    "Đáng chú ý, '%s' chiếm tới %.0f%% tổng số lượt đề cập, " +
                    "cho thấy đây là vấn đề cấp bách nhất cần ưu tiên xử lý.",
                    mostMentioned, concentration));
            }

            result.setNarrativeSummary(narrative.toString());

            result.addInsight(String.format(
                "'%s' là loại thiệt hại được quan tâm nhất với %d lượt đề cập, " +
                "chiếm %.0f%% tổng số.",
                mostMentioned, maxVal, concentration));
            for (Map.Entry<String, Integer> e : categoryTotals.entrySet()) {
                if (e.getValue() > 0 && !e.getKey().equals(mostMentioned)) {
                    double pct = e.getValue() * 100.0 / totalMentions;
                    result.addInsight(String.format(
                        "'%s' được nhắc đến %d lần (%.0f%%), phản ánh mối quan tâm " +
                        "đáng kể từ cộng đồng.",
                        e.getKey(), e.getValue(), pct));
                    break;
                }
            }

            result.addConclusion(String.format(
                "Thiệt hại từ Bão Yagi tập trung chủ yếu vào lĩnh vực '%s', " +
                "phản ánh đúng thực tế thiệt hại nặng nề mà cơn bão đã gây ra. " +
                "Sự phân bố đề cập giữa các loại thiệt hại cho thấy mối quan tâm " +
                "đa dạng của cộng đồng đối với các khía cạnh khác nhau của thảm họa.",
                mostMentioned));

            result.addRecommendation(String.format(
                "Ưu tiên nguồn lực khắc phục '%s' - lĩnh vực được cộng đồng " +
                "quan tâm nhất, đồng thời không xem nhẹ các loại thiệt hại khác.",
                mostMentioned));
            result.addRecommendation(
                "Triển khai khảo sát thực địa để đối chiếu và bổ sung thông tin " +
                "từ mạng xã hội nhằm có cái nhìn toàn diện về thiệt hại.");
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
