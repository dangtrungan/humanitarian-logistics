package com.humanitarian.logistics.preprocessor;

import com.humanitarian.logistics.model.Post;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreprocessorPipeline {
    private final List<TextPreprocessor> preprocessors;
    private String name;

    public PreprocessorPipeline(String name) {
        this.name = name;
        this.preprocessors = new ArrayList<>();
    }

    public PreprocessorPipeline(String name, List<TextPreprocessor> preprocessors) {
        this.name = name;
        this.preprocessors = new ArrayList<>(preprocessors);
    }

    public void addPreprocessor(TextPreprocessor preprocessor) {
        this.preprocessors.add(preprocessor);
    }

    public void setPreprocessors(List<TextPreprocessor> preprocessors) {
        this.preprocessors.clear();
        this.preprocessors.addAll(preprocessors);
    }

    public Post execute(Post post) {
        Post processed = post;
        for (TextPreprocessor p : preprocessors) {
            processed = p.processPost(processed);
        }
        return processed;
    }

    public List<Post> executeAll(List<Post> posts) {
        List<Post> results = new ArrayList<>();
        for (Post post : posts) {
            results.add(execute(post));
        }
        return results;
    }

    public String getName() { return name; }

    public List<TextPreprocessor> getPreprocessors() { return new ArrayList<>(preprocessors); }

    public static PreprocessorPipeline createDefault() {
        PreprocessorPipeline pipeline = new PreprocessorPipeline("Default Pipeline");
        pipeline.addPreprocessor(new BasicTextPreprocessor());
        return pipeline;
    }

    public static PreprocessorPipeline createAdvanced() {
        PreprocessorPipeline pipeline = new PreprocessorPipeline("Advanced Pipeline");
        pipeline.addPreprocessor(new BasicTextPreprocessor());
        pipeline.addPreprocessor(new AdvancedTextPreprocessor());
        return pipeline;
    }
}
