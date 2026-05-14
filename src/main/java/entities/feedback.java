package entities;

import java.sql.Timestamp;

public class feedback {

    // Enum for targetType matching database ENUM
    public enum TargetType {
        doctor, pharmacy, application
    }

    // Fields matching table columns
    private int feedbackId;          // AUTO_INCREMENT
    private int userId;              // foreign key to user table
    private TargetType targetType;
    private int targetId;
    private int rating;              // integer rating (e.g., 1-5)
    private String comment;
    private Timestamp creationDate;
    private boolean isAnonyme;       // tinyint(1) -> boolean
    private String avatarUrl;

    // Constructors
    public feedback() {}

    public feedback(int userId, TargetType targetType, int targetId, int rating,
                    String comment, Timestamp creationDate, boolean isAnonyme, String avatarUrl) {
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.rating = rating;
        this.comment = comment;
        this.creationDate = creationDate;
        this.isAnonyme = isAnonyme;
        this.avatarUrl = avatarUrl;
    }

    public feedback(int feedbackId, int userId, TargetType targetType, int targetId,
                    int rating, String comment, Timestamp creationDate, boolean isAnonyme, String avatarUrl) {
        this.feedbackId = feedbackId;
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.rating = rating;
        this.comment = comment;
        this.creationDate = creationDate;
        this.isAnonyme = isAnonyme;
        this.avatarUrl = avatarUrl;
    }

    // Getters and setters
    public int getFeedbackId() { return feedbackId; }
    public void setFeedbackId(int feedbackId) { this.feedbackId = feedbackId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }

    public int getTargetId() { return targetId; }
    public void setTargetId(int targetId) { this.targetId = targetId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Timestamp getCreationDate() { return creationDate; }
    public void setCreationDate(Timestamp creationDate) { this.creationDate = creationDate; }

    public boolean isAnonyme() { return isAnonyme; }
    public void setAnonyme(boolean anonyme) { isAnonyme = anonyme; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    @Override
    public String toString() {
        return "feedback{" +
                "feedbackId=" + feedbackId +
                ", userId=" + userId +
                ", targetType=" + targetType +
                ", targetId=" + targetId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", creationDate=" + creationDate +
                ", isAnonyme=" + isAnonyme +
                '}';
    }
}