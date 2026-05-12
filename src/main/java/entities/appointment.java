package entities;
import java.sql.Date;
import java.sql.Time;
public class appointment {
    public enum PaymentMethod {
        espece, carte_bancaire
    }

    // Fields matching the table columns
    private int appointmentId;           // AUTO_INCREMENT
    private Date appointmentDate;        // date type
    private Time appointmentTime;        // time type
    private String reason;               // varchar(100)
    private String type;                 // varchar(50)
    private PaymentMethod paymentMethod; // enum('espèce', 'carte bancaire')
    private int patientId;               // int(11)
    private int doctorId;                // int(11)
    private String status;               // varchar(20) - EN_ATTENTE, CONFIRME, REFUSE
    private String meetingLink;          // varchar(255)
    private int availabilityId;          // FK vers availability (0 si non lié)

    // Default constructor
    public appointment() {
    }

    // Constructor without appointmentId (for new records, auto-generated)
    public appointment(Date appointmentDate, Time appointmentTime, String reason,
                       String type, PaymentMethod paymentMethod, int patientId, int doctorId, String status) {
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.type = type;
        this.paymentMethod = paymentMethod;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.status = status;
    }

    // Constructor with appointmentId (for existing records)
    public appointment(int appointmentId, Date appointmentDate, Time appointmentTime,
                       String reason, String type, PaymentMethod paymentMethod,
                       int patientId, int doctorId, String status) {
        this.appointmentId = appointmentId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.type = type;
        this.paymentMethod = paymentMethod;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.status = status;
    }

    // Getters and Setters
    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Time getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(Time appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public int getAvailabilityId() {
        return availabilityId;
    }

    public void setAvailabilityId(int availabilityId) {
        this.availabilityId = availabilityId;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId=" + appointmentId +
                ", appointmentDate=" + appointmentDate +
                ", appointmentTime=" + appointmentTime +
                ", reason='" + reason + '\'' +
                ", type='" + type + '\'' +
                ", paymentMethod=" + paymentMethod +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", status='" + status + '\'' +
                ", meetingLink='" + meetingLink + '\'' +
                '}';
    }

    // Helper method to get payment method as string for database insertion
    public String getPaymentMethodAsString() {
        if (paymentMethod == PaymentMethod.espece) {
            return "espèce";
        } else {
            return "carte bancaire";
        }
    }

    // Helper method to set payment method from database string value
    public void setPaymentMethodFromString(String paymentMethodStr) {
        if ("espèce".equals(paymentMethodStr)) {
            this.paymentMethod = PaymentMethod.espece;
        } else if ("carte bancaire".equals(paymentMethodStr)) {
            this.paymentMethod = PaymentMethod.carte_bancaire;
        }
    }

}
