package com.humanitarian.logistics.storage;

import com.humanitarian.logistics.model.Post;
import com.humanitarian.logistics.model.AnalysisResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CsvDataStore implements DataStore {
    private final String outputDir;

    public CsvDataStore(String outputDir) {
        this.outputDir = outputDir;
        try { Files.createDirectories(Paths.get(outputDir)); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public CsvDataStore() {
        this("data/output");
    }

    @Override
    public String getName() { return "CSV Data Store"; }

    @Override
    public boolean isAvailable() { return true; }

    @Override
    public void savePosts(List<Post> posts) {
        String filePath = outputDir + "/posts_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.println("id,source,authorId,authorName,timestamp,content,likeCount,shareCount,commentCount,language");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (Post p : posts) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%d,\"%s\"%n",
                    escapeCsv(p.getId()), escapeCsv(p.getSource()),
                    escapeCsv(p.getAuthorId()), escapeCsv(p.getAuthorName()),
                    p.getTimestamp() != null ? p.getTimestamp().format(fmt) : "",
                    escapeCsv(p.getContent()),
                    p.getLikeCount(), p.getShareCount(), p.getCommentCount(),
                    escapeCsv(p.getLanguage()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAnalysisResult(AnalysisResult result) {
        String filePath = outputDir + "/analysis_" + result.getAnalysisType() + "_"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.println("label,value,category,series");
            for (AnalysisResult.ChartDataPoint point : result.getChartData()) {
                writer.printf("\"%s\",%f,\"%s\",\"%s\"%n",
                    escapeCsv(point.getLabel()), point.getValue(),
                    escapeCsv(point.getCategory() != null ? point.getCategory() : ""),
                    escapeCsv(point.getSeries() != null ? point.getSeries() : ""));
            }
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

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
