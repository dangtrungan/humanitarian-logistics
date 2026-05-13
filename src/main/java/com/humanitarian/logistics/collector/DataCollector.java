package com.humanitarian.logistics.collector;

import com.humanitarian.logistics.model.Post;

import java.util.List;

public interface DataCollector {
    String getSourceName();
    List<Post> collect(String keyword, int limit);
    List<Post> collectWithHashtag(String hashtag, int limit);
    List<Post> collectByDateRange(String keyword, String startDate, String endDate, int limit);
    boolean isAvailable();
    void authenticate(String apiKey, String apiSecret);
    String getStatus();
}
