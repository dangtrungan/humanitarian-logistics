package com.humanitarian.logistics.analyzer;

import com.humanitarian.logistics.model.SentimentResult;
import com.humanitarian.logistics.model.Post;

import java.util.List;

public interface SentimentAnalyzer {
    String getName();
    SentimentResult analyze(Post post);
    List<SentimentResult> analyzeBatch(List<Post> posts);
    boolean isAvailable();
}
