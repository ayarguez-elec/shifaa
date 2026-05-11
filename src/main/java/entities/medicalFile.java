package entities;

import java.time.LocalDate;

public class medicalFile {
    private int idfile;
    private int patientId;
    private LocalDate creationdate;
    private LocalDate lastvisit;
    private String generalhistory;
    private String antecedents;
    private String allergies;
    private String followup;
    private String patientstatus;

    public medicalFile() {}

    public int getIdfile() {
        return idfile;
    }

    public void setIdfile(int idfile) {
        this.idfile = idfile;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public LocalDate getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(LocalDate creationdate) {
        this.creationdate = creationdate;
    }

    public LocalDate getLastvisit() {
        return lastvisit;
    }

    public void setLastvisit(LocalDate lastvisit) {
        this.lastvisit = lastvisit;
    }

    public String getGeneralhistory() {
        return generalhistory;
    }

    public void setGeneralhistory(String generalhistory) {
        this.generalhistory = generalhistory;
    }

    public String getAntecedents() {
        return antecedents;
    }

    public void setAntecedents(String antecedents) {
        this.antecedents = antecedents;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getFollowup() {
        return followup;
    }

    public void setFollowup(String followup) {
        this.followup = followup;
    }

    public String getPatientstatus() {
        return patientstatus;
    }

    public void setPatientstatus(String patientstatus) {
        this.patientstatus = patientstatus;
    }

    @Override
    public String toString() {
        return "Dossier #" + idfile;
    }
}