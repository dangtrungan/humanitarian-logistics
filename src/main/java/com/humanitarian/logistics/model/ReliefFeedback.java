package com.humanitarian.logistics.model;

public class ReliefFeedback {
    private String postId;
    private String reliefCategory;
    private double positiveScore;
    private double negativeScore;
    private String sentimentLabel;
    private String specificNeed;

    public ReliefFeedback() {}

    public ReliefFeedback(String postId, String reliefCategory, double positiveScore, double negativeScore) {
        this.postId = postId;
        this.reliefCategory = reliefCategory;
        this.positiveScore = positiveScore;
        this.negativeScore = negativeScore;
        this.sentimentLabel = positiveScore > negativeScore ? "POSITIVE" : "NEGATIVE";
    }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getReliefCategory() { return reliefCategory; }
    public void setReliefCategory(String reliefCategory) { this.reliefCategory = reliefCategory; }

    public double getPositiveScore() { return positiveScore; }
    public void setPositiveScore(double positiveScore) { this.positiveScore = positiveScore; }

    public double getNegativeScore() { return negativeScore; }
    public void setNegativeScore(double negativeScore) { this.negativeScore = negativeScore; }

    public String getSentimentLabel() { return sentimentLabel; }
    public void setSentimentLabel(String sentimentLabel) { this.sentimentLabel = sentimentLabel; }

    public String getSpecificNeed() { return specificNeed; }
    public void setSpecificNeed(String specificNeed) { this.specificNeed = specificNeed; }

    public boolean isSatisfied() { return "POSITIVE".equals(sentimentLabel); }
}
