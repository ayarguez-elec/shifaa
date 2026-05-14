package entities;

import java.time.LocalDate;

public class event {

    private int       id;
    private String    titre;
    private String    type;
    private String    description;
    private LocalDate date;
    private String    time;
    private String    lieu;
    private String    status;
    private String    imagePath; // Path to event image (optional)
    private int       capaciteMax;

    // ── Constructeur complet (avec id) ────────────────────────────────────────
    public event(int id, String titre, String type, String description,
                 LocalDate date, String time, String lieu, String status, String imagePath, int capaciteMax) {
        this.id          = id;
        this.titre       = titre;
        this.type        = type;
        this.description = description;
        this.date        = date;
        this.time        = time;
        this.lieu        = lieu;
        this.status      = status;
        this.imagePath   = imagePath;
        this.capaciteMax = capaciteMax;
    }
    
    // For backwards compatibility before DB migration
    public event(int id, String titre, String type, String description,
                 LocalDate date, String time, String lieu, String status) {
        this(id, titre, type, description, date, time, lieu, status, null, 0);
    }

    // ── Constructeur sans id (INSERT auto-increment) ──────────────────────────
    public event(String titre, String type, String description,
                 LocalDate date, String time, String lieu, String status, String imagePath, int capaciteMax) {
        this(0, titre, type, description, date, time, lieu, status, imagePath, capaciteMax);
    }
    
    public event(String titre, String type, String description,
                 LocalDate date, String time, String lieu, String status) {
        this(0, titre, type, description, date, time, lieu, status, null, 0);
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int       getId()          { return id; }
    public String    getTitre()       { return titre; }
    public String    getType()        { return type; }
    public String    getDescription() { return description; }
    public LocalDate getDate()        { return date; }
    public String    getTime()        { return time; }
    public String    getLieu()        { return lieu; }
    public String    getStatus()      { return status; }
    public String    getImagePath()   { return imagePath; }
    public int       getCapaciteMax() { return capaciteMax; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setId(int id)                   { this.id          = id; }
    public void setTitre(String titre)          { this.titre       = titre; }
    public void setType(String type)            { this.type        = type; }
    public void setDescription(String desc)     { this.description = desc; }
    public void setDate(LocalDate date)         { this.date        = date; }
    public void setTime(String time)            { this.time        = time; }
    public void setLieu(String lieu)            { this.lieu        = lieu; }
    public void setStatus(String status)        { this.status      = status; }
    public void setImagePath(String imagePath)  { this.imagePath   = imagePath; }
    public void setCapaciteMax(int cap)         { this.capaciteMax = cap; }

    @Override
    public String toString() {
        return "event{id=" + id + ", titre='" + titre + "', type='" + type +
                "', date=" + date + ", lieu='" + lieu + "', status='" + status + 
                "', capMax=" + capaciteMax + ", imagePath='" + imagePath + "'}";
    }
}