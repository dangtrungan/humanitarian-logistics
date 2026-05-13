package com.humanitarian.logistics.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

public class Post {
    private String id;
    private String source;
    private String authorId;
    private String authorName;
    private String content;
    private LocalDateTime timestamp;
    private String language;
    private int likeCount;
    private int shareCount;
    private int commentCount;
    private String url;
    private Map<String, Object> metadata;

    public Post() {
        this.metadata = new HashMap<>();
    }

    public Post(String id, String source, String content, LocalDateTime timestamp) {
        this();
        this.id = id;
        this.source = source;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getShareCount() { return shareCount; }
    public void setShareCount(int shareCount) { this.shareCount = shareCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public void addMetadata(String key, Object value) { this.metadata.put(key, value); }

    @Override
    public String toString() {
        return "Post{" + "id='" + id + '\'' + ", source='" + source + '\'' +
               ", timestamp=" + timestamp + ", content='" +
               (content != null ? content.substring(0, Math.min(50, content.length())) : "") + "...'}";
    }
}
