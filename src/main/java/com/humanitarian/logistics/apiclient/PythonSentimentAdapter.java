package com.humanitarian.logistics.apiclient;

import com.humanitarian.logistics.analyzer.SentimentAnalyzer;
import com.humanitarian.logistics.model.SentimentResult;
import com.humanitarian.logistics.model.Post;

import java.util.List;

public class PythonSentimentAdapter implements SentimentAnalyzer {
    private final SentimentApiClient apiClient;
    private boolean useFallback;

    public PythonSentimentAdapter(SentimentApiClient apiClient) {
        this.apiClient = apiClient;
        this.useFallback = false;
    }

    public PythonSentimentAdapter(String baseUrl) {
        this(new SentimentApiClient(baseUrl));
    }

    public PythonSentimentAdapter() {
        this(new SentimentApiClient());
    }

    @Override
    public String getName() {
        if (useFallback) return "Python API (fallback to local)";
        return "Python API Sentiment Model";
    }

    @Override
    public boolean isAvailable() {
        if (useFallback) return true;
        boolean available = apiClient.isAvailable();
        if (!available) {
            System.out.println("Python API not available, useFallback=true");
        }
        return available;
    }

    @Override
    public SentimentResult analyze(Post post) {
        if (!useFallback && apiClient.isAvailable()) {
            try {
                return apiClient.analyze(post);
            } catch (Exception e) {
                useFallback = true;
            }
        }
        return fallbackAnalyze(post);
    }

    @Override
    public List<SentimentResult> analyzeBatch(List<Post> posts) {
        if (!useFallback && apiClient.isAvailable()) {
            try {
                return apiClient.analyzeBatch(posts);
            } catch (Exception e) {
                useFallback = true;
            }
        }
        return posts.stream().map(this::fallbackAnalyze).toList();
    }

    private SentimentResult fallbackAnalyze(Post post) {
        return new SentimentResult(post.getId(), 0.33, 0.33, 0.34, "NEUTRAL");
    }

    public void setUseFallback(boolean useFallback) { this.useFallback = useFallback; }
    public boolean isUsingFallback() { return useFallback; }
}
