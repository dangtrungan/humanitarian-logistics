package com.humanitarian.logistics.model;

public class SentimentResult {
    private String postId;
    private double positiveScore;
    private double negativeScore;
    private double neutralScore;
    private String label;
    private double confidence;

    public SentimentResult() {}

    public SentimentResult(String postId, double positiveScore, double negativeScore, double neutralScore, String label) {
        this.postId = postId;
        this.positiveScore = positiveScore;
        this.negativeScore = negativeScore;
        this.neutralScore = neutralScore;
        this.label = label;
        this.confidence = Math.max(positiveScore, Math.max(negativeScore, neutralScore));
    }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public double getPositiveScore() { return positiveScore; }
    public void setPositiveScore(double positiveScore) { this.positiveScore = positiveScore; }

    public double getNegativeScore() { return negativeScore; }
    public void setNegativeScore(double negativeScore) { this.negativeScore = negativeScore; }

    public double getNeutralScore() { return neutralScore; }
    public void setNeutralScore(double neutralScore) { this.neutralScore = neutralScore; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public boolean isPositive() { return "POSITIVE".equals(label); }
    public boolean isNegative() { return "NEGATIVE".equals(label); }
    public boolean isNeutral() { return "NEUTRAL".equals(label); }
}
