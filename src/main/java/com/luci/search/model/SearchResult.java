package com.luci.search.model;

public class SearchResult {
    private String title;
    private String summary;
    private String imageUrl;

    public SearchResult(String title, String summary, String imageUrl) {
        this.title = title;
        this.summary = summary;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getImageUrl() { return imageUrl; }

    public void setTitle(String title) { this.title = title; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
