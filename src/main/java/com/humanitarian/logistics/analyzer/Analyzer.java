package com.humanitarian.logistics.analyzer;

import com.humanitarian.logistics.model.AnalysisResult;
import com.humanitarian.logistics.model.Post;
import com.humanitarian.logistics.model.AnalysisConfig;

import java.util.List;

public interface Analyzer {
    String getName();
    String getDescription();
    AnalysisResult analyze(List<Post> posts, AnalysisConfig config);
    boolean isAvailable();
}
