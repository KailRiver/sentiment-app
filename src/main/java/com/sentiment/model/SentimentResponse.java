package com.sentiment.model;

public class SentimentResponse {
    private String sentiment;
    private double confidence;
    private String text;

    public SentimentResponse() {}

    public SentimentResponse(String sentiment, double confidence, String text) {
        this.sentiment = sentiment;
        this.confidence = confidence;
        this.text = text;
    }

    // Getters and Setters
    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}