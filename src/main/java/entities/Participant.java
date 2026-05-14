package entities;

public class Participant {

    // ── Niveaux de priorité ───────────────────────────────────────────────────
    public enum PriorityLevel { LOW, MEDIUM, HIGH, VIP }

    // ── Badges de fidélité ────────────────────────────────────────────────────
    public enum Badge { Bronze, Silver, Gold }

    // ── Champs de base ────────────────────────────────────────────────────────
    private int    id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;

    // ── Champs fidélité (calculés / mis à jour par LoyaltyService) ────────────
    private int           loyaltyPoints;
    private PriorityLevel priorityLevel;
    private Badge         badge;
    private int           attendanceCount;
    private int           cancellationCount;

    // ── Constructeur complet (avec id + fidélité) ─────────────────────────────
    public Participant(int id, String nom, String prenom, String email, String telephone,
                       int loyaltyPoints, PriorityLevel priorityLevel, Badge badge,
                       int attendanceCount, int cancellationCount) {
        this.id                = id;
        this.nom               = nom;
        this.prenom            = prenom;
        this.email             = email;
        this.telephone         = telephone;
        this.loyaltyPoints     = loyaltyPoints;
        this.priorityLevel     = priorityLevel != null ? priorityLevel : PriorityLevel.LOW;
        this.badge             = badge != null ? badge : Badge.Bronze;
        this.attendanceCount   = attendanceCount;
        this.cancellationCount = cancellationCount;
    }

    // ── Constructeur retro-compatible (id seul, sans fidélité) ────────────────
    public Participant(int id, String nom, String prenom, String email, String telephone) {
        this(id, nom, prenom, email, telephone, 0, PriorityLevel.LOW, Badge.Bronze, 0, 0);
    }

    // ── Constructeur INSERT (sans id) ─────────────────────────────────────────
    public Participant(String nom, String prenom, String email, String telephone) {
        this(0, nom, prenom, email, telephone, 0, PriorityLevel.LOW, Badge.Bronze, 0, 0);
    }

    // ── Getters de base ───────────────────────────────────────────────────────
    public int    getId()        { return id; }
    public String getNom()       { return nom; }
    public String getPrenom()    { return prenom; }
    public String getEmail()     { return email; }
    public String getTelephone() { return telephone; }

    // ── Getters fidélité ──────────────────────────────────────────────────────
    public int           getLoyaltyPoints()     { return loyaltyPoints; }
    public PriorityLevel getPriorityLevel()     { return priorityLevel; }
    public Badge         getBadge()             { return badge; }
    public int           getAttendanceCount()   { return attendanceCount; }
    public int           getCancellationCount() { return cancellationCount; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setId(int id)                            { this.id                = id; }
    public void setNom(String nom)                       { this.nom               = nom; }
    public void setPrenom(String prenom)                 { this.prenom            = prenom; }
    public void setEmail(String email)                   { this.email             = email; }
    public void setTelephone(String tel)                 { this.telephone         = tel; }
    public void setLoyaltyPoints(int pts)                { this.loyaltyPoints     = pts; }
    public void setPriorityLevel(PriorityLevel p)        { this.priorityLevel     = p; }
    public void setBadge(Badge b)                        { this.badge             = b; }
    public void setAttendanceCount(int n)                { this.attendanceCount   = n; }
    public void setCancellationCount(int n)              { this.cancellationCount = n; }

    // ── Helpers d'affichage ───────────────────────────────────────────────────
    public String getBadgeEmoji() {
        switch (badge) {
            case Gold:   return "🥇";
            case Silver: return "🥈";
            default:     return "🥉";
        }
    }

    public String getPriorityEmoji() {
        switch (priorityLevel) {
            case VIP:    return "⭐";
            case HIGH:   return "🔺";
            case MEDIUM: return "🔹";
            default:     return "▫";
        }
    }

    public String getPriorityColor() {
        switch (priorityLevel) {
            case VIP:    return "#7c3aed";
            case HIGH:   return "#dc2626";
            case MEDIUM: return "#d97706";
            default:     return "#6b7280";
        }
    }

    public String getBadgeColor() {
        switch (badge) {
            case Gold:   return "#d97706";
            case Silver: return "#6b7280";
            default:     return "#b45309";
        }
    }

    @Override
    public String toString() {
        return "Participant{id=" + id + ", nom='" + nom + "', prenom='" + prenom +
               "', email='" + email + "', badge=" + badge +
               ", priority=" + priorityLevel + ", points=" + loyaltyPoints + "}";
    }
}
