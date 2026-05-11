package entities;

import java.time.LocalDate;

public class CalendarEvent {

    public enum EventType {
        reunion, consultation, rappel, autre
    }

    private int id;
    private int userId;          // admin qui a créé l'événement
    private LocalDate date;
    private String time;         // "HH:mm"
    private String title;
    private String description;
    private EventType type;
    private String color;        // couleur hex du point

    public CalendarEvent() {}

    public CalendarEvent(int userId, LocalDate date, String time,
                         String title, String description, EventType type, String color) {
        this.userId      = userId;
        this.date        = date;
        this.time        = time;
        this.title       = title;
        this.description = description;
        this.type        = type;
        this.color       = color;
    }

    // ── Getters / Setters ──────────────────────────────────────────────
    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public int getUserId()                { return userId; }
    public void setUserId(int userId)     { this.userId = userId; }

    public LocalDate getDate()            { return date; }
    public void setDate(LocalDate date)   { this.date = date; }

    public String getTime()               { return time; }
    public void setTime(String time)      { this.time = time; }

    public String getTitle()              { return title; }
    public void setTitle(String title)    { this.title = title; }

    public String getDescription()                    { return description; }
    public void setDescription(String description)    { this.description = description; }

    public EventType getType()                        { return type; }
    public void setType(EventType type)               { this.type = type; }

    public String getColor()              { return color; }
    public void setColor(String color)    { this.color = color; }
}
