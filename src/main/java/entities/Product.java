package entities;

import java.sql.Date;

public class Product {
    private int productId;
    private String productName;
    private String description;
    private String dosage;
    private String form;
    private String laboratory;
    private String therapeuticClass;
    private double price;
    private String image;
    private String category;
    private int availableQuantity;
    private String availabilityStatus;
    private Date lastUpdateDate;
    private int pharmacyId;

    public Product() {
    }

    public Product(int productId, String productName, String description, String dosage,
                   String form, String laboratory, String therapeuticClass,
                   double price, String image, String category,
                   int availableQuantity, String availabilityStatus, Date lastUpdateDate,
                   int pharmacyId) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.dosage = dosage;
        this.form = form;
        this.laboratory = laboratory;
        this.therapeuticClass = therapeuticClass;
        this.price = price;
        this.image = image;
        this.category = category;
        this.availableQuantity = availableQuantity;
        this.availabilityStatus = availabilityStatus;
        this.lastUpdateDate = lastUpdateDate;
        this.pharmacyId = pharmacyId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getLaboratory() {
        return laboratory;
    }

    public void setLaboratory(String laboratory) {
        this.laboratory = laboratory;
    }

    public String getTherapeuticClass() {
        return therapeuticClass;
    }

    public void setTherapeuticClass(String therapeuticClass) {
        this.therapeuticClass = therapeuticClass;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public int getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(int pharmacyId) {
        this.pharmacyId = pharmacyId;
    }
}