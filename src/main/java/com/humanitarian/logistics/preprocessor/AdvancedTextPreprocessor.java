package com.humanitarian.logistics.preprocessor;

import com.humanitarian.logistics.model.Post;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public class AdvancedTextPreprocessor implements TextPreprocessor {
    private final Set<String> stopwords;
    private static final Pattern VIETNAMESE_NORMALIZE = Pattern.compile("[\\p{InCombiningDiacriticalMarks}]");

    public AdvancedTextPreprocessor() {
        this.stopwords = loadStopwords();
    }

    @Override
    public String getName() { return "Advanced Preprocessor (Vietnamese-aware)"; }

    @Override
    public String getDescription() {
        return "Removes URLs, mentions, hashtags, stopwords, accents normalization, lemmatization hints";
    }

    @Override
    public String process(String text) {
        if (text == null || text.isEmpty()) return "";
        BasicTextPreprocessor basic = new BasicTextPreprocessor();
        text = basic.process(text);
        text = normalizeVietnameseAccents(text);
        text = removeStopwords(text);
        text = removeShortWords(text);
        return text.trim();
    }

    @Override
    public Post processPost(Post post) {
        String cleaned = process(post.getContent());
        post.setContent(cleaned);
        return post;
    }

    private String normalizeVietnameseAccents(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return VIETNAMESE_NORMALIZE.matcher(normalized).replaceAll("");
    }

    private String removeStopwords(String text) {
        List<String> words = new ArrayList<>(Arrays.asList(text.split("\\s+")));
        words.removeIf(w -> stopwords.contains(w.toLowerCase()));
        return String.join(" ", words);
    }

    private String removeShortWords(String text) {
        List<String> words = new ArrayList<>(Arrays.asList(text.split("\\s+")));
        words.removeIf(w -> w.length() <= 2);
        return String.join(" ", words);
    }

    private Set<String> loadStopwords() {
        Set<String> sw = new HashSet<>(Arrays.asList(
            "và", "là", "của", "có", "được", "các", "những", "đã", "đang", "sẽ",
            "để", "với", "trong", "cho", "về", "như", "khi", "từ", "trên", "tại",
            "này", "một", "hai", "ba", "bốn", "năm", "cũng", "rất", "thì", "bởi",
            "ra", "vào", "ở", "lên", "xuống", "qua", "lại", "nên", "mà", "cả",
            "do", "hay", "hoặc", "đây", "đó", "ấy", "nào", "sao", "vậy", "thế",
            "tôi", "bạn", "chúng", "ta", "mình", "người", "gì", "ấy", "nhưng",
            "vì", "nếu", "tuy", "song", "đến", "phải", "chưa", "vẫn", "cứ", "hãy",
            "đừng", "xin", "mong", "kính"
        ));
        return sw;
    }
}
