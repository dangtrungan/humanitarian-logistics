package com.humanitarian.logistics;

import com.humanitarian.logistics.analyzer.AnalysisEngine;
import com.humanitarian.logistics.analyzer.AnalyzerFactory;
import com.humanitarian.logistics.analyzer.LocalSentimentAnalyzer;
import com.humanitarian.logistics.apiclient.PythonSentimentAdapter;
import com.humanitarian.logistics.apiclient.SentimentApiClient;
import com.humanitarian.logistics.config.AppConfig;
import com.humanitarian.logistics.ui.MainFrame;

import javax.swing.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        AppConfig config = AppConfig.getInstance();
        config.setApiEndpoint("sentiment", "http://localhost:5000");

        config.getAnalysisConfig().setSentimentModel("local");

        for (String arg : args) {
            if (arg.startsWith("--config=")) {
                String configFile = arg.substring("--config=".length());
                try {
                    config.loadFromJson(configFile);
                    System.out.println("Loaded config from: " + configFile);
                } catch (Exception e) {
                    System.err.println("Warning: Could not load config: " + e.getMessage());
                }
            }
            if (arg.equals("--use-python-api")) {
                SentimentApiClient apiClient = new SentimentApiClient("http://localhost:5000");
                if (apiClient.isAvailable()) {
                    AnalyzerFactory.getInstance().setSentimentAnalyzer(new PythonSentimentAdapter(apiClient));
                    config.getAnalysisConfig().setSentimentModel("python_api");
                    System.out.println("Using Python API for sentiment analysis.");
                } else {
                    System.out.println("Python API not available. Using local analyzer.");
                }
            }
            if (arg.equals("--batch")) {
                runBatchMode(config);
                return;
            }
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    private static void runBatchMode(AppConfig config) {
        System.out.println("=== Humanitarian Logistics Analyzer - Batch Mode ===");
        AnalysisEngine engine = new AnalysisEngine();
        engine.setPreprocessor(com.humanitarian.logistics.preprocessor.PreprocessorPipeline.createDefault());
        engine.runFullPipeline();

        System.out.println("\n=== RESULTS ===");
        engine.getResults().forEach(r -> {
            System.out.println("\n--- " + r.getAnalysisName() + " ---");
            System.out.println(r.getSummary());
        });
        System.out.println("\nOutput saved to data/ directory.");
    }
}
