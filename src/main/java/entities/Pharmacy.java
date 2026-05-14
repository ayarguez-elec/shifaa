package entities;

public class Pharmacy {

    private int pharmacyId;
    private String pharmacyName;
    private String address;
    private String phone;
    private String email;
    private String openingHours;
    private String pharmacyStatus;
    private String image;

    public Pharmacy() {
    }

    public Pharmacy(int pharmacyId, String pharmacyName, String address, String phone, String email,
                    String openingHours, String pharmacyStatus, String image) {
        this.pharmacyId = pharmacyId;
        this.pharmacyName = pharmacyName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.openingHours = openingHours;
        this.pharmacyStatus = pharmacyStatus;
        this.image = image;
    }

    public Pharmacy(String pharmacyName, String address, String phone, String email,
                    String openingHours, String pharmacyStatus, String image) {
        this.pharmacyName = pharmacyName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.openingHours = openingHours;
        this.pharmacyStatus = pharmacyStatus;
        this.image = image;
    }

    public int getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(int pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getPharmacyStatus() {
        return pharmacyStatus;
    }

    public void setPharmacyStatus(String pharmacyStatus) {
        this.pharmacyStatus = pharmacyStatus;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
