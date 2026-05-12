package utils;

import entities.Pharmacy;
import entities.Product;
import services.PharmacyServices;
import services.ProductServices;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ProductServices ps = new ProductServices();
        PharmacyServices phs = new PharmacyServices();

        /*
        // =========================
        // TEST ADD PRODUCT
        // =========================
        Product p = new Product();
        p.setProductName("Doliprane 1000");
        p.setDescription("Medicament antalgique et antipiretique");
        p.setDosage("1000mg");
        p.setForm("Comprime");
        p.setLaboratory("Sanofi");
        p.setTherapeuticClass("Analgesic");
        p.setPrice(4.950);
        p.setImage("doliprane1000.jpg");
        p.setCategory("Medicine");
        p.setAvailableQuantity(20);
        p.setAvailabilityStatus("Available");
        p.setLastUpdateDate(Date.valueOf("2026-04-29"));

        try {
            ps.add(p);
            System.out.println("Produit ajoute avec succes !");
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        */

        /*
        // =========================
        // TEST ADD PHARMACY
        // =========================
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setPharmacyName("Pharmacie Centrale");
        pharmacy.setAddress("Tunis");
        pharmacy.setPhone("22123456");
        pharmacy.setEmail("pharma@gmail.com");
        pharmacy.setOpeningHours("08:00-20:00");
        pharmacy.setPharmacyStatus("Active");

        try {
            phs.add(pharmacy);
            System.out.println("Pharmacy ajoutee avec succes !");
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        */

        /*
        // =========================
        // TEST UPDATE PRODUCT
        // =========================
        Product productUpdate = new Product();
        productUpdate.setProductId(1);
        productUpdate.setProductName("Doliprane 500");
        productUpdate.setDescription("Nouvelle description");
        productUpdate.setDosage("500mg");
        productUpdate.setForm("Comprime");
        productUpdate.setLaboratory("Sanofi");
        productUpdate.setTherapeuticClass("Analgesic");
        productUpdate.setPrice(5.500);
        productUpdate.setImage("doliprane_modifie.jpg");
        productUpdate.setCategory("Medicine");
        productUpdate.setAvailableQuantity(50);
        productUpdate.setAvailabilityStatus("Available");
        productUpdate.setLastUpdateDate(Date.valueOf("2026-04-29"));

        try {
            ps.update(productUpdate);
            System.out.println("Produit modifie avec succes !");
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        */

        /*
        // =========================
        // TEST UPDATE PHARMACY
        // =========================
        Pharmacy pharmacyUpdate = new Pharmacy();
        pharmacyUpdate.setPharmacyId(1);
        pharmacyUpdate.setPharmacyName("Pharmacie Modifiee");
        pharmacyUpdate.setAddress("Sfax");
        pharmacyUpdate.setPhone("99888777");
        pharmacyUpdate.setEmail("newpharma@gmail.com");
        pharmacyUpdate.setOpeningHours("09:00-22:00");
        pharmacyUpdate.setPharmacyStatus("Active");

        try {
            phs.update(pharmacyUpdate);
            System.out.println("Pharmacy modifiee avec succes !");
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        */

        /*
        // =========================
        // TEST DELETE PRODUCT
        // =========================
        Product productDelete = new Product();
        productDelete.setProductId(1);

        try {
            ps.delete(productDelete);
            System.out.println("Produit supprime avec succes !");
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        */

        /*
        // =========================
        // TEST DELETE PHARMACY
        // =========================
        Pharmacy pharmacyDelete = new Pharmacy();
        pharmacyDelete.setPharmacyId(1);

        try {
            phs.delete(pharmacyDelete);
            System.out.println("Pharmacy supprimee avec succes !");
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        */

        /*
        // =========================
        // TEST GET ALL PRODUCTS
        // =========================
        try {
            List<Product> products = ps.getAll();
            System.out.println("Liste des produits :");
            for (Product prod : products) {
                System.out.println(prod);
            }
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        */

        /*
        // =========================
        // TEST GET ALL PHARMACIES
        // =========================
        try {
            List<Pharmacy> pharmacies = phs.getAll();
            System.out.println("Liste des pharmacies :");
            for (Pharmacy pharmacy : pharmacies) {
                System.out.println(pharmacy);
            }
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        */
    }
}