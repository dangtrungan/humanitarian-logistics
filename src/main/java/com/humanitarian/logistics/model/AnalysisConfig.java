package com.humanitarian.logistics.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class AnalysisConfig {
    private String disasterName;
    private String disasterType;
    private String startDate;
    private String endDate;
    private List<String> keywords;
    private List<String> hashtags;
    private List<String> dataSources;
    private List<String> selectedAnalyzers;
    private Map<String, List<String>> damageCategories;
    private Map<String, List<String>> reliefCategories;
    private String sentimentModel;
    private String preprocessingPipeline;
    private String language;

    public AnalysisConfig() {
        this.keywords = new ArrayList<>();
        this.hashtags = new ArrayList<>();
        this.dataSources = new ArrayList<>();
        this.selectedAnalyzers = new ArrayList<>();
        this.damageCategories = new HashMap<>();
        this.reliefCategories = new HashMap<>();
        this.preprocessingPipeline = "basic";
        this.language = "vi";
    }

    public String getDisasterName() { return disasterName; }
    public void setDisasterName(String disasterName) { this.disasterName = disasterName; }

    public String getDisasterType() { return disasterType; }
    public void setDisasterType(String disasterType) { this.disasterType = disasterType; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public List<String> getHashtags() { return hashtags; }
    public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

    public List<String> getDataSources() { return dataSources; }
    public void setDataSources(List<String> dataSources) { this.dataSources = dataSources; }

    public List<String> getSelectedAnalyzers() { return selectedAnalyzers; }
    public void setSelectedAnalyzers(List<String> selectedAnalyzers) { this.selectedAnalyzers = selectedAnalyzers; }

    public Map<String, List<String>> getDamageCategories() { return damageCategories; }
    public void setDamageCategories(Map<String, List<String>> damageCategories) { this.damageCategories = damageCategories; }

    public Map<String, List<String>> getReliefCategories() { return reliefCategories; }
    public void setReliefCategories(Map<String, List<String>> reliefCategories) { this.reliefCategories = reliefCategories; }

    public String getSentimentModel() { return sentimentModel; }
    public void setSentimentModel(String sentimentModel) { this.sentimentModel = sentimentModel; }

    public String getPreprocessingPipeline() { return preprocessingPipeline; }
    public void setPreprocessingPipeline(String preprocessingPipeline) { this.preprocessingPipeline = preprocessingPipeline; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
