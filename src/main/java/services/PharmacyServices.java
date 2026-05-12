package services;

import entities.Pharmacy;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PharmacyServices {

    private final Connection connection;

    public PharmacyServices() {
        connection = MyDataBase.getInstance().getConnection();
    }

    public void add(Pharmacy pharmacy) {
        String sql = "INSERT INTO pharmacy (pharmacyName, address, phone, email, openingHours, pharmacyStatus, image) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, pharmacy.getPharmacyName());
            ps.setString(2, pharmacy.getAddress());
            ps.setString(3, pharmacy.getPhone());
            ps.setString(4, pharmacy.getEmail());
            ps.setString(5, pharmacy.getOpeningHours());
            ps.setString(6, pharmacy.getPharmacyStatus());
            ps.setString(7, pharmacy.getImage());

            ps.executeUpdate();
            System.out.println("Pharmacie ajoutée avec succès");

        } catch (SQLException e) {
            System.out.println("Erreur ajout pharmacie : " + e.getMessage());
        }
    }

    public void update(Pharmacy pharmacy) {
        String sql = "UPDATE pharmacy SET pharmacyName = ?, address = ?, phone = ?, email = ?, " +
                "openingHours = ?, pharmacyStatus = ?, image = ? WHERE pharmacyId = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, pharmacy.getPharmacyName());
            ps.setString(2, pharmacy.getAddress());
            ps.setString(3, pharmacy.getPhone());
            ps.setString(4, pharmacy.getEmail());
            ps.setString(5, pharmacy.getOpeningHours());
            ps.setString(6, pharmacy.getPharmacyStatus());
            ps.setString(7, pharmacy.getImage());
            ps.setInt(8, pharmacy.getPharmacyId());

            ps.executeUpdate();
            System.out.println("Pharmacie modifiée avec succès");

        } catch (SQLException e) {
            System.out.println("Erreur modification pharmacie : " + e.getMessage());
        }
    }

    public void delete(Pharmacy pharmacy) {
        String sql = "DELETE FROM pharmacy WHERE pharmacyId = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, pharmacy.getPharmacyId());
            ps.executeUpdate();

            System.out.println("Pharmacie supprimée avec succès");

        } catch (SQLException e) {
            System.out.println("Erreur suppression pharmacie : " + e.getMessage());
        }
    }

    public List<Pharmacy> getAll() {
        List<Pharmacy> pharmacies = new ArrayList<>();

        String sql = "SELECT * FROM pharmacy";

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                Pharmacy pharmacy = new Pharmacy();

                pharmacy.setPharmacyId(rs.getInt("pharmacyId"));
                pharmacy.setPharmacyName(rs.getString("pharmacyName"));
                pharmacy.setAddress(rs.getString("address"));
                pharmacy.setPhone(rs.getString("phone"));
                pharmacy.setEmail(rs.getString("email"));
                pharmacy.setOpeningHours(rs.getString("openingHours"));
                pharmacy.setPharmacyStatus(rs.getString("pharmacyStatus"));

                try {
                    pharmacy.setImage(rs.getString("image"));
                } catch (Exception e) {
                    pharmacy.setImage("");
                }

                pharmacies.add(pharmacy);
            }

        } catch (SQLException e) {
            System.out.println("Erreur affichage pharmacies : " + e.getMessage());
        }

        return pharmacies;
    }
}