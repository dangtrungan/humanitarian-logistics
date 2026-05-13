package com.humanitarian.logistics.analyzer;

import java.util.LinkedHashMap;
import java.util.Map;

public class AnalyzerFactory {
    private static AnalyzerFactory instance;
    private final Map<String, Analyzer> analyzers;
    private SentimentAnalyzer defaultSentimentAnalyzer;

    private AnalyzerFactory() {
        analyzers = new LinkedHashMap<>();
        this.defaultSentimentAnalyzer = new LocalSentimentAnalyzer();
        registerDefaults();
    }

    public static synchronized AnalyzerFactory getInstance() {
        if (instance == null) {
            instance = new AnalyzerFactory();
        }
        return instance;
    }

    private void registerDefaults() {
        registerAnalyzer("sentiment_timeline", new SentimentTimelineAnalyzer(defaultSentimentAnalyzer));
        registerAnalyzer("damage_analysis", new DamageAnalyzer(defaultSentimentAnalyzer));
        registerAnalyzer("relief_satisfaction", new ReliefSatisfactionAnalyzer(defaultSentimentAnalyzer));
        registerAnalyzer("relief_timeline", new ReliefTimelineAnalyzer(defaultSentimentAnalyzer));
    }

    public void registerAnalyzer(String name, Analyzer analyzer) {
        analyzers.put(name, analyzer);
    }

    public Analyzer getAnalyzer(String name) {
        Analyzer analyzer = analyzers.get(name);
        if (analyzer == null) {
            throw new IllegalArgumentException("Unknown analyzer: " + name);
        }
        return analyzer;
    }

    public Map<String, Analyzer> getAllAnalyzers() {
        return new LinkedHashMap<>(analyzers);
    }

    public void removeAnalyzer(String name) {
        analyzers.remove(name);
    }

    public void setSentimentAnalyzer(SentimentAnalyzer sentimentAnalyzer) {
        this.defaultSentimentAnalyzer = sentimentAnalyzer;
        replaceSentimentAnalyzer(sentimentAnalyzer);
    }

    public SentimentAnalyzer getDefaultSentimentAnalyzer() {
        return defaultSentimentAnalyzer;
    }

    private void replaceSentimentAnalyzer(SentimentAnalyzer newAnalyzer) {
        Map<String, Analyzer> updated = new LinkedHashMap<>();
        for (Map.Entry<String, Analyzer> entry : analyzers.entrySet()) {
            Analyzer a = entry.getValue();
            if (a instanceof SentimentTimelineAnalyzer) {
                updated.put(entry.getKey(), new SentimentTimelineAnalyzer(newAnalyzer));
            } else if (a instanceof DamageAnalyzer) {
                updated.put(entry.getKey(), new DamageAnalyzer(newAnalyzer));
            } else if (a instanceof ReliefSatisfactionAnalyzer) {
                updated.put(entry.getKey(), new ReliefSatisfactionAnalyzer(newAnalyzer));
            } else if (a instanceof ReliefTimelineAnalyzer) {
                updated.put(entry.getKey(), new ReliefTimelineAnalyzer(newAnalyzer));
            } else {
                updated.put(entry.getKey(), a);
            }
        }
        analyzers.clear();
        analyzers.putAll(updated);
    }
}
