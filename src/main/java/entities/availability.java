package entities;

import java.sql.Date;
import java.sql.Time;

public class availability {

    public enum StatusRDV {
        libre, occupe
    }

    // Fields matching the table columns
    private int availabilityId;      // AUTO_INCREMENT
    private Date date;               // date type
    private Time time;               // time type
    private StatusRDV statusRDV;     // enum('libre', 'occupe')
    private int doctorId;            // int(11)

    // ✅ Default constructor
    public availability() {
    }

    // ✅ Constructor without availabilityId (for new records)
    public availability(Date date, Time time, StatusRDV statusRDV, int doctorId) {
        this.date = date;
        this.time = time;
        this.statusRDV = statusRDV;
        this.doctorId = doctorId;
    }

    // ✅ Constructor with availabilityId (for existing records)
    public availability(int availabilityId, Date date, Time time,
                        StatusRDV statusRDV, int doctorId) {
        this.availabilityId = availabilityId;
        this.date = date;
        this.time = time;
        this.statusRDV = statusRDV;
        this.doctorId = doctorId;
    }

    // ==================== Getters & Setters ====================

    public int getAvailabilityId() {
        return availabilityId;
    }

    public void setAvailabilityId(int availabilityId) {
        this.availabilityId = availabilityId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public StatusRDV getStatusRDV() {
        return statusRDV;
    }

    public void setStatusRDV(StatusRDV statusRDV) {
        this.statusRDV = statusRDV;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    // ==================== Helper Methods ====================

    /**
     * Retourne "libre" ou "occupe" (sans accent) pour la base de données
     * Car la colonne MySQL est enum('libre', 'occupe')
     */
    public String getStatusRDVAsString() {
        if (statusRDV == null) return "";
        if (statusRDV == StatusRDV.occupe) return "occupe";
        return "libre";
    }

    /**
     * Charge le statut depuis une String (ComboBox ou DB).
     * Accepte : "occupé", "occupe", "occupé", "Occupé", etc.
     */
    public void setStatusRDVFromString(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            this.statusRDV = StatusRDV.libre;
            return;
        }
        String normalized = statusStr.trim().toLowerCase();
        if (normalized.equals("libre")) {
            this.statusRDV = StatusRDV.libre;
        } else if (normalized.startsWith("occup")) {
            // Accepte "occupé", "occupe", "occupé", etc.
            this.statusRDV = StatusRDV.occupe;
        } else {
            this.statusRDV = StatusRDV.libre;
        }
    }

    // ==================== toString ====================

    @Override
    public String toString() {
        return "Availability{" +
                "availabilityId=" + availabilityId +
                ", date=" + date +
                ", time=" + time +
                ", statusRDV=" + (statusRDV != null ? statusRDV.name() : "null") +
                ", doctorId=" + doctorId +
                '}';
    }
}