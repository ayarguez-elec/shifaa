package services;

import entities.Product;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PharmacyStockService {

    private final Connection connection;

    public PharmacyStockService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    public List<Product> getAllProductsForStock() {
        List<Product> products = new ArrayList<>();

        String sql = """
                SELECT *
                FROM product
                ORDER BY productName ASC
                """;

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                products.add(mapProduct(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur chargement produits : " + e.getMessage());
            e.printStackTrace();
        }

        return products;
    }

    public List<Product> getProductsByPharmacy(int pharmacyId) {
        List<Product> products = new ArrayList<>();

        String sql = """
                SELECT *
                FROM product
                WHERE pharmacyId = ?
                ORDER BY productName ASC
                """;

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, pharmacyId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                products.add(mapProduct(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur produits pharmacie : " + e.getMessage());
            e.printStackTrace();
        }

        return products;
    }

    public void updateProductStock(int productId, int pharmacyId, int quantity, String status) {
        String sql = """
                UPDATE product
                SET pharmacyId = ?, availableQuantity = ?, availabilityStatus = ?
                WHERE productId = ?
                """;

        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setInt(1, pharmacyId);
            ps.setInt(2, quantity);
            ps.setString(3, status);
            ps.setInt(4, productId);

            ps.executeUpdate();

            System.out.println("Stock produit mis à jour avec succès");

        } catch (SQLException e) {
            System.out.println("Erreur modification stock produit : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product product = new Product();

        product.setProductId(rs.getInt("productId"));
        product.setProductName(rs.getString("productName"));
        product.setDescription(rs.getString("description"));
        product.setDosage(rs.getString("dosage"));
        product.setForm(rs.getString("form"));
        product.setLaboratory(rs.getString("laboratory"));
        product.setTherapeuticClass(rs.getString("therapeuticClass"));
        product.setPrice(rs.getDouble("price"));
        product.setImage(rs.getString("image"));
        product.setCategory(rs.getString("category"));

        try {
            product.setPharmacyId(rs.getInt("pharmacyId"));
        } catch (Exception ignored) {
            product.setPharmacyId(0);
        }

        try {
            product.setAvailableQuantity(rs.getInt("availableQuantity"));
        } catch (Exception ignored) {
            product.setAvailableQuantity(0);
        }

        try {
            String status = rs.getString("availabilityStatus");
            product.setAvailabilityStatus(status == null || status.trim().isEmpty() ? "Rupture" : status);
        } catch (Exception ignored) {
            product.setAvailabilityStatus("Rupture");
        }

        return product;
    }
}