package com.humanitarian.logistics.apiclient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.humanitarian.logistics.model.SentimentResult;
import com.humanitarian.logistics.model.Post;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class SentimentApiClient {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;
    private boolean available;

    public SentimentApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.gson = new Gson();
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.available = checkHealth();
    }

    public SentimentApiClient() {
        this("http://localhost:5000");
    }

    public boolean checkHealth() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/health"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            available = response.statusCode() == 200;
            return available;
        } catch (Exception e) {
            available = false;
            return false;
        }
    }

    public boolean isAvailable() { return available; }

    public SentimentResult analyze(Post post) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", post.getContent());

        try {
            String json = gson.toJson(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/sentiment/analyze"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
                var resultsArray = responseJson.getAsJsonArray("results");
                if (resultsArray != null && resultsArray.size() > 0) {
                    JsonObject result = resultsArray.get(0).getAsJsonObject();
                    return new SentimentResult(
                        post.getId(),
                        result.get("positive").getAsDouble(),
                        result.get("negative").getAsDouble(),
                        result.get("neutral").getAsDouble(),
                        result.get("label").getAsString()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("API call failed: " + e.getMessage());
        }
        return fallbackAnalysis(post);
    }

    public List<SentimentResult> analyzeBatch(List<Post> posts) {
        List<Map<String, String>> postList = posts.stream()
            .map(p -> {
                Map<String, String> m = new HashMap<>();
                m.put("id", p.getId());
                m.put("content", p.getContent());
                return m;
            })
            .collect(Collectors.toList());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("posts", postList);

        try {
            String json = gson.toJson(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/sentiment/analyze-posts"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
                var resultsArray = responseJson.getAsJsonArray("results");
                if (resultsArray != null) {
                    List<SentimentResult> results = new ArrayList<>();
                    for (int i = 0; i < resultsArray.size(); i++) {
                        JsonObject r = resultsArray.get(i).getAsJsonObject();
                        results.add(new SentimentResult(
                            r.get("postId").getAsString(),
                            r.get("positive").getAsDouble(),
                            r.get("negative").getAsDouble(),
                            r.get("neutral").getAsDouble(),
                            r.get("label").getAsString()
                        ));
                    }
                    return results;
                }
            }
        } catch (Exception e) {
            System.err.println("Batch API call failed: " + e.getMessage());
        }
        return posts.stream().map(this::fallbackAnalysis).collect(Collectors.toList());
    }

    private SentimentResult fallbackAnalysis(Post post) {
        return new SentimentResult(post.getId(), 0.33, 0.33, 0.34, "NEUTRAL");
    }
}
