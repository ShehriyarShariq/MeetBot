package com.shehriyar.experienceadvisor.model;

public class ExpReview {

    private String reviewerThumbnailUrl, reviewer, message;
    private float rating;

    public ExpReview(String reviewerThumbnailUrl, String reviewer, String message, float rating) {
        this.reviewerThumbnailUrl = reviewerThumbnailUrl;
        this.reviewer = reviewer;
        this.message = message;
        this.rating = rating;
    }

    public String getReviewerThumbnailUrl() {
        return reviewerThumbnailUrl;
    }

    public String getReviewer() {
        return reviewer;
    }

    public String getMessage() {
        return message;
    }

    public float getRating() {
        return rating;
    }
}
