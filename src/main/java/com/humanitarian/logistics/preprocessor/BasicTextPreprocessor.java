package com.humanitarian.logistics.preprocessor;

import com.humanitarian.logistics.model.Post;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class BasicTextPreprocessor implements TextPreprocessor {
    @Override
    public String getName() { return "Basic Preprocessor"; }

    @Override
    public String getDescription() {
        return "Removes URLs, mentions, hashtags, extra whitespace, and special characters";
    }

    @Override
    public String process(String text) {
        if (text == null || text.isEmpty()) return "";
        text = removeUrls(text);
        text = removeMentions(text);
        text = removeHashtags(text);
        text = removeExtraWhitespace(text);
        text = removeSpecialChars(text);
        return text.trim().toLowerCase();
    }

    @Override
    public Post processPost(Post post) {
        String cleaned = process(post.getContent());
        post.setContent(cleaned);
        return post;
    }

    protected String removeUrls(String text) {
        return text.replaceAll("https?://\\S+\\s?", "").trim();
    }

    protected String removeMentions(String text) {
        return text.replaceAll("@\\w+", "").trim();
    }

    protected String removeHashtags(String text) {
        return text.replaceAll("#\\w+", "").trim();
    }

    protected String removeExtraWhitespace(String text) {
        return text.replaceAll("\\s+", " ");
    }

    protected String removeSpecialChars(String text) {
        return text.replaceAll("[^\\p{L}\\p{N}\\s]", " ");
    }
}
