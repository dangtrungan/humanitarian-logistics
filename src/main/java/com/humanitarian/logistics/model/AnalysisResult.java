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

    public AnalysisResult(String analysisName, String analysisType) {
        this.analysisName = analysisName;
        this.analysisType = analysisType;
        this.data = new HashMap<>();
        this.chartData = new ArrayList<>();
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
