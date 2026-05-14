package entities;

public class MedicationReminder {
    private int id;
    private int userId;
    private String medName;
    private String dosage;
    private String time; // Format "HH:mm"
    private boolean taken;

    public MedicationReminder() {}

    public MedicationReminder(int userId, String medName, String dosage, String time, boolean taken) {
        this.userId = userId;
        this.medName = medName;
        this.dosage = dosage;
        this.time = time;
        this.taken = taken;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getMedName() { return medName; }
    public void setMedName(String medName) { this.medName = medName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public boolean isTaken() { return taken; }
    public void setTaken(boolean taken) { this.taken = taken; }
}
