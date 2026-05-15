package com.humanitarian.logistics.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class AnalysisResult {
    private String analysisName;
    private String analysisType;
    private Map<String, Object> data;
    private List<ChartDataPoint> chartData;
    private String summary;
    private String narrativeSummary;
    private List<String> insights;
    private List<String> conclusions;
    private List<String> recommendations;

    public AnalysisResult(String analysisName, String analysisType) {
        this.analysisName = analysisName;
        this.analysisType = analysisType;
        this.data = new HashMap<>();
        this.chartData = new ArrayList<>();
        this.insights = new ArrayList<>();
        this.conclusions = new ArrayList<>();
        this.recommendations = new ArrayList<>();
    }

    public String getAnalysisName() { return analysisName; }
    public void setAnalysisName(String analysisName) { this.analysisName = analysisName; }

    public String getAnalysisType() { return analysisType; }
    public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    public void addData(String key, Object value) { this.data.put(key, value); }

    public List<ChartDataPoint> getChartData() { return chartData; }
    public void setChartData(List<ChartDataPoint> chartData) { this.chartData = chartData; }
    public void addChartData(ChartDataPoint point) { this.chartData.add(point); }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getNarrativeSummary() { return narrativeSummary; }
    public void setNarrativeSummary(String narrativeSummary) { this.narrativeSummary = narrativeSummary; }

    public List<String> getInsights() { return insights; }
    public void setInsights(List<String> insights) { this.insights = insights; }
    public void addInsight(String insight) { this.insights.add(insight); }

    public List<String> getConclusions() { return conclusions; }
    public void setConclusions(List<String> conclusions) { this.conclusions = conclusions; }
    public void addConclusion(String conclusion) { this.conclusions.add(conclusion); }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    public void addRecommendation(String recommendation) { this.recommendations.add(recommendation); }

    public static class ChartDataPoint {
        private String label;
        private double value;
        private String category;
        private String series;

        public ChartDataPoint(String label, double value) {
            this.label = label;
            this.value = value;
        }

        public ChartDataPoint(String label, double value, String category) {
            this.label = label;
            this.value = value;
            this.category = category;
        }

        public ChartDataPoint(String label, double value, String category, String series) {
            this.label = label;
            this.value = value;
            this.category = category;
            this.series = series;
        }

        public String getLabel() { return label; }
        public double getValue() { return value; }
        public String getCategory() { return category; }
        public String getSeries() { return series; }
    }
}
