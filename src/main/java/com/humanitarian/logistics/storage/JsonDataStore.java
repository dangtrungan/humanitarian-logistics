package com.humanitarian.logistics.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.humanitarian.logistics.model.Post;
import com.humanitarian.logistics.model.AnalysisResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JsonDataStore implements DataStore {
    private final String outputDir;
    private final Gson gson;

    public JsonDataStore(String outputDir) {
        this.outputDir = outputDir;
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
        try { Files.createDirectories(Paths.get(outputDir)); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public JsonDataStore() {
        this("data/output");
    }

    @Override
    public String getName() { return "JSON Data Store"; }

    @Override
    public boolean isAvailable() { return true; }

    @Override
    public void savePosts(List<Post> posts) {
        String filePath = outputDir + "/posts_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            gson.toJson(serializePosts(posts), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAnalysisResult(AnalysisResult result) {
        String filePath = outputDir + "/analysis_" + result.getAnalysisType() + "_"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            gson.toJson(result, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> loadPosts() {
        return new ArrayList<>();
    }

    @Override
    public List<AnalysisResult> loadResults() {
        return new ArrayList<>();
    }

    private List<Map<String, Object>> serializePosts(List<Post> posts) {
        List<Map<String, Object>> serialized = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Post p : posts) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("source", p.getSource());
            m.put("content", p.getContent());
            m.put("timestamp", p.getTimestamp() != null ? p.getTimestamp().format(fmt) : "");
            m.put("authorName", p.getAuthorName());
            m.put("likeCount", p.getLikeCount());
            m.put("language", p.getLanguage());
            serialized.add(m);
        }
        return serialized;
    }

    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            out.value(value != null ? value.format(FMT) : null);
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            String str = in.nextString();
            return str != null ? LocalDateTime.parse(str, FMT) : null;
        }
    }
}
