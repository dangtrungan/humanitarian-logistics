package com.humanitarian.logistics.model;

public class DamageReport {
    private String postId;
    private String damageCategory;
    private String subCategory;
    private double confidence;
    private String severity;
    private String location;

    public DamageReport() {}

    public DamageReport(String postId, String damageCategory, double confidence) {
        this.postId = postId;
        this.damageCategory = damageCategory;
        this.confidence = confidence;
    }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getDamageCategory() { return damageCategory; }
    public void setDamageCategory(String damageCategory) { this.damageCategory = damageCategory; }

    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
