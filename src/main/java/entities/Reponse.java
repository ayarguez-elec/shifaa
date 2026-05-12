package entities;

import java.sql.Timestamp;

public class Reponse {

    private int responseId;
    private String content;
    private Timestamp responseDate;
    private int adminId;
    private int reclamationId;

    public Reponse() {
    }

    public Reponse(int responseId, String content, Timestamp responseDate, int adminId, int reclamationId) {
        this.responseId = responseId;
        this.content = content;
        this.responseDate = responseDate;
        this.adminId = adminId;
        this.reclamationId = reclamationId;
    }

    public Reponse(String content, Timestamp responseDate, int adminId, int reclamationId) {
        this.content = content;
        this.responseDate = responseDate;
        this.adminId = adminId;
        this.reclamationId = reclamationId;
    }

    public int getResponseId() {
        return responseId;
    }

    public void setResponseId(int responseId) {
        this.responseId = responseId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(Timestamp responseDate) {
        this.responseDate = responseDate;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public int getReclamationId() {
        return reclamationId;
    }

    public void setReclamationId(int reclamationId) {
        this.reclamationId = reclamationId;
    }

    @Override
    public String toString() {
        return "Reponse{" +
                "responseId=" + responseId +
                ", content=\'" + content + "\'" +
                ", responseDate=" + responseDate +
                ", adminId=" + adminId +
                ", reclamationId=" + reclamationId +
                '}';
    }
}