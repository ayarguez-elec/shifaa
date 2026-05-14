package entities;

public class userDS {
    private int userId;
    private String firstName;
    private String lastName;
    private String userphoto;
    private String role;
    private String email;
    private String phoneNumber;
    private String speciality;
    private String imageSignature;

    public userDS() {}

    public userDS(int userId, String firstName, String lastName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public userDS(int userId, String firstName, String lastName, String userphoto, String role, String email) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userphoto = userphoto;
        this.role = role;
        this.email = email;
    }

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

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getUserphoto() {
        return userphoto;
    }

    public void setUserphoto(String userphoto) {
        this.userphoto = userphoto;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getImageSignature() {
        return imageSignature;
    }

    public void setImageSignature(String imageSignature) {
        this.imageSignature = imageSignature;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
