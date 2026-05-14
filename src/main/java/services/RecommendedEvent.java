package services;

import entities.event;

public class RecommendedEvent {

    private final event  event;
    private final double score;
    private final String reason;

    public RecommendedEvent(event event, double score, String reason) {
        this.event  = event;
        this.score  = score;
        this.reason = reason;
    }

    public event  getEvent()  { return event; }
    public double getScore()  { return score; }
    public String getReason() { return reason; }
}
