package entities;
import java.time.LocalDate;
public class Prescription {
    // Attributs
    private int prescriptionId;
    private LocalDate prescriptionDate;
    private String prescription;
    private String treatmentDuration;
    private int doctorId;
    private int patientId;
    private int idfile;

    // ─── Constructeurs ───────────────────────────────────────────

    public Prescription() {
    }

    public Prescription(LocalDate prescriptionDate, String prescription,
                        String treatmentDuration, int doctorId, int patientId, int idfile) {
        this.prescriptionDate = prescriptionDate;
        this.prescription = prescription;
        this.treatmentDuration = treatmentDuration;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.idfile = idfile;
    }

    public Prescription(int prescriptionId, LocalDate prescriptionDate, String prescription,
                        String treatmentDuration, int doctorId, int patientId, int idfile) {
        this.prescriptionId = prescriptionId;
        this.prescriptionDate = prescriptionDate;
        this.prescription = prescription;
        this.treatmentDuration = treatmentDuration;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.idfile = idfile;
    }

    // ─── Getters & Setters ───────────────────────────────────────

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public String getTreatmentDuration() {
        return treatmentDuration;
    }

    public void setTreatmentDuration(String treatmentDuration) {
        this.treatmentDuration = treatmentDuration;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getIdfile() {
        return idfile;
    }

    public void setIdfile(int idfile) {
        this.idfile = idfile;
    }

    // ─── toString ────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Prescription{" +
                "prescriptionId=" + prescriptionId +
                ", prescriptionDate=" + prescriptionDate +
                ", prescription='" + prescription + '\'' +
                ", treatmentDuration='" + treatmentDuration + '\'' +
                ", doctorId=" + doctorId +
                ", patientId=" + patientId +
                ", idfile=" + idfile +
                '}';
    }
}
