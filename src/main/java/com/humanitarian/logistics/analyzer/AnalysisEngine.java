package com.humanitarian.logistics.analyzer;

import com.humanitarian.logistics.collector.DataCollector;
import com.humanitarian.logistics.collector.DataCollectorFactory;
import com.humanitarian.logistics.config.AppConfig;
import com.humanitarian.logistics.export.ChartGenerator;
import com.humanitarian.logistics.export.ReportExporter;
import com.humanitarian.logistics.model.*;
import com.humanitarian.logistics.preprocessor.PreprocessorPipeline;
import com.humanitarian.logistics.storage.DataStore;
import com.humanitarian.logistics.storage.CsvDataStore;
import com.humanitarian.logistics.storage.JsonDataStore;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

public class AnalysisEngine {
    private final AppConfig appConfig;
    private final DataCollectorFactory collectorFactory;
    private final AnalyzerFactory analyzerFactory;
    private final List<DataStore> dataStores;
    private final ChartGenerator chartGenerator;
    private final ReportExporter reportExporter;
    private PreprocessorPipeline preprocessor;
    private List<Post> collectedPosts;
    private List<AnalysisResult> results;
    private List<String> logs;

    public AnalysisEngine() {
        this.appConfig = AppConfig.getInstance();
        this.collectorFactory = DataCollectorFactory.getInstance();
        this.analyzerFactory = AnalyzerFactory.getInstance();
        this.dataStores = new ArrayList<>();
        this.chartGenerator = new ChartGenerator();
        this.reportExporter = new ReportExporter();
        this.collectedPosts = new ArrayList<>();
        this.results = new ArrayList<>();
        this.logs = new ArrayList<>();

        dataStores.add(new CsvDataStore());
        dataStores.add(new JsonDataStore());
    }

    public void setPreprocessor(PreprocessorPipeline preprocessor) {
        this.preprocessor = preprocessor;
    }

    public void setSentimentAnalyzer(SentimentAnalyzer analyzer) {
        this.analyzerFactory.setSentimentAnalyzer(analyzer);
    }

    public void collectData() {
        AnalysisConfig config = appConfig.getAnalysisConfig();
        logs.clear();
        collectedPosts.clear();
        addLog("Starting data collection for: " + config.getDisasterName());

        for (String source : config.getDataSources()) {
            try {
                DataCollector collector = collectorFactory.getCollector(source);
                collector.authenticate(appConfig.getApiKey(source), "");
                addLog("  Collecting from " + collector.getSourceName() + "...");

                List<Post> sourcePosts = new ArrayList<>();
                for (String keyword : config.getKeywords()) {
                    sourcePosts.addAll(collector.collectByDateRange(
                        keyword, config.getStartDate(), config.getEndDate(), 20));
                }
                for (String hashtag : config.getHashtags()) {
                    sourcePosts.addAll(collector.collectWithHashtag(hashtag, 20));
                }

                Map<String, Post> uniquePosts = new LinkedHashMap<>();
                for (Post p : sourcePosts) {
                    uniquePosts.put(p.getId(), p);
                }

                collectedPosts.addAll(uniquePosts.values());
                addLog("    Collected " + uniquePosts.size() + " posts from " + collector.getSourceName());
            } catch (Exception e) {
                addLog("    ERROR collecting from " + source + ": " + e.getMessage());
            }
        }

        addLog("Total posts collected: " + collectedPosts.size());
    }

    public void preprocessData() {
        addLog("Preprocessing " + collectedPosts.size() + " posts...");
        if (preprocessor != null) {
            collectedPosts = preprocessor.executeAll(collectedPosts);
            addLog("  Pipeline: " + preprocessor.getName());
        } else {
            PreprocessorPipeline defaultPipeline = PreprocessorPipeline.createDefault();
            collectedPosts = defaultPipeline.executeAll(collectedPosts);
            addLog("  Pipeline: " + defaultPipeline.getName());
        }
        addLog("Preprocessing complete.");
    }

    public void runAnalysis() {
        results.clear();
        AnalysisConfig config = appConfig.getAnalysisConfig();
        addLog("Running " + config.getSelectedAnalyzers().size() + " analyzers...");

        for (String analyzerName : config.getSelectedAnalyzers()) {
            try {
                Analyzer analyzer = analyzerFactory.getAnalyzer(analyzerName);
                addLog("  Running: " + analyzer.getName());
                AnalysisResult result = analyzer.analyze(collectedPosts, config);
                results.add(result);
                addLog("    Complete: " + result.getSummary());
            } catch (Exception e) {
                addLog("    ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        }

        addLog("Analysis complete. " + results.size() + " results generated.");
    }

    private void cleanOldOutputs() {
        File chartsDir = new File("data/charts");
        if (chartsDir.exists()) {
            File[] oldCharts = chartsDir.listFiles((dir, name) -> name.endsWith(".png"));
            if (oldCharts != null) {
                for (File f : oldCharts) f.delete();
                addLog("Cleaned " + oldCharts.length + " old chart images.");
            }
        }
        File reportsDir = new File("data/reports");
        if (reportsDir.exists()) {
            File[] oldReports = reportsDir.listFiles((dir, name) ->
                name.endsWith(".html") || name.endsWith(".txt"));
            if (oldReports != null) {
                for (File f : oldReports) f.delete();
                addLog("Cleaned " + oldReports.length + " old reports.");
            }
        }
    }

    public void saveResults() {
        cleanOldOutputs();
        addLog("Saving results...");
        for (DataStore store : dataStores) {
            store.savePosts(collectedPosts);
            for (AnalysisResult result : results) {
                store.saveAnalysisResult(result);
            }
        }

        String disasterName = appConfig.getAnalysisConfig().getDisasterName();

        for (AnalysisResult result : results) {
            List<String> chartPaths = new ArrayList<>();

            try {
                chartPaths.add(chartGenerator.saveChartToImage(
                    chartGenerator.createBarChart(result, result.getAnalysisName()), 800, 500,
                    result.getAnalysisType() + "_bar"));

                chartPaths.add(chartGenerator.saveChartToImage(
                    chartGenerator.createPieChart(result, result.getAnalysisName() + " - Phân bổ"), 600, 500,
                    result.getAnalysisType() + "_pie"));
            } catch (Exception e) {
                addLog("    Chart generation error: " + e.getMessage());
            }

            String type = result.getAnalysisType();
            if ("sentiment_timeline".equals(type) || "relief_timeline".equals(type)) {
                try {
                    chartPaths.add(chartGenerator.saveChartToImage(
                        chartGenerator.createLineChart(result, result.getAnalysisName() + " - Xu hướng"), 800, 500,
                        result.getAnalysisType() + "_line"));
                } catch (Exception e) {
                    addLog("    Line chart generation error: " + e.getMessage());
                }
            }

            chartPaths.removeIf(Objects::isNull);

            reportExporter.exportTextReport(result, chartPaths);
            reportExporter.exportHtmlReport(result, chartPaths, disasterName);
        }
        addLog("Results saved to data/output/, data/charts/, data/reports/");
    }

    public void runFullPipeline() {
        addLog("=== FULL PIPELINE START ===");
        collectData();
        if (collectedPosts.isEmpty()) {
            addLog("No data collected. Using sample data.");
            collectedPosts = com.humanitarian.logistics.collector.MockDataCollector.generateYagiSampleData();
            addLog("Generated " + collectedPosts.size() + " sample posts.");
        }
        preprocessData();
        runAnalysis();
        saveResults();
        addLog("=== FULL PIPELINE COMPLETE ===");
    }

    public List<AnalysisResult> getResults() { return results; }
    public List<Post> getCollectedPosts() { return collectedPosts; }
    public List<String> getLogs() { return new ArrayList<>(logs); }
    public AnalysisResult getResultByType(String type) {
        return results.stream().filter(r -> r.getAnalysisType().equals(type)).findFirst().orElse(null);
    }

    private void addLog(String message) {
        logs.add("[" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message);
    }
}
