package com.humanitarian.logistics.storage;

import com.humanitarian.logistics.model.Post;
import com.humanitarian.logistics.model.AnalysisResult;

import java.util.List;

public interface DataStore {
    String getName();
    void savePosts(List<Post> posts);
    void saveAnalysisResult(AnalysisResult result);
    List<Post> loadPosts();
    List<AnalysisResult> loadResults();
    boolean isAvailable();
}
