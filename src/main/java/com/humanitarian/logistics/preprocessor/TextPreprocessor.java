package com.humanitarian.logistics.preprocessor;

import com.humanitarian.logistics.model.Post;

public interface TextPreprocessor {
    String getName();
    String process(String text);
    Post processPost(Post post);
    String getDescription();
}
