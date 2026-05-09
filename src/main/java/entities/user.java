package entities;
public class user
{
    // Enum definitions matching the database ENUM types
    public enum Role {
        patient, doctor, pharmacist, assistant , admin
    }

    public enum Gender {
        F, M
    }

    public enum AccountStatus {
        pending, active, suspended, blocked
    }

    // Fields matching the table columns
    private int userId;                 // AUTO_INCREMENT
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String address;
    private Role role;
    private int age;
    private Gender gender;
    private String phone;
    private String specialty;           // for doctors
    private String orderNumber;
    private float consultationFee;      // for doctors
    private String pharmacyName;        // for pharmacists
    private String licenceNumber;       // for pharmacists
    private String userPhoto;
    private int doctorId;               // for assistants (links to a doctor)
    private AccountStatus accountStatus;

    // Default constructor
    public user() {
    }

    // Constructor without userId (for new records, auto-generated)
    public user(String firstName, String lastName, String email, String password,
                String address, Role role, int age, Gender gender, String phone,
                String specialty, String orderNumber, float consultationFee,
                String pharmacyName, String licenceNumber, String userPhoto, int doctorId,
                AccountStatus accountStatus) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.address = address;
        this.role = role;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.specialty = specialty;
        this.orderNumber = orderNumber;
        this.consultationFee = consultationFee;
        this.pharmacyName = pharmacyName;
        this.licenceNumber = licenceNumber;
        this.userPhoto = userPhoto;
        this.doctorId = doctorId;
        this.accountStatus = accountStatus;
    }

    // Constructor with userId (for existing records)
    public user(int userId, String firstName, String lastName, String email, String password,
                String address, Role role, int age, Gender gender, String phone,
                String specialty, String orderNumber, float consultationFee,
                String pharmacyName, String licenceNumber, String userPhoto, int doctorId,
                AccountStatus accountStatus) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.address = address;
        this.role = role;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.specialty = specialty;
        this.orderNumber = orderNumber;
        this.consultationFee = consultationFee;
        this.pharmacyName = pharmacyName;
        this.licenceNumber = licenceNumber;
        this.userPhoto = userPhoto;
        this.doctorId = doctorId;
        this.accountStatus = accountStatus;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public float getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(float consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", role=" + role +
                ", age=" + age +
                ", gender=" + gender +
                ", phone='" + phone + '\'' +
                ", specialty='" + specialty + '\'' +
                ", orderNumber='" + orderNumber + '\'' +
                ", consultationFee=" + consultationFee +
                ", pharmacyName='" + pharmacyName + '\'' +
                ", licenceNumber='" + licenceNumber + '\'' +
                ", userPhoto='" + userPhoto + '\'' +
                ", doctorId=" + doctorId +
                ", accountStatus=" + accountStatus +
                '}';
    }
}