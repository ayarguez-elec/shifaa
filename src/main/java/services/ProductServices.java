package services;

import entities.Product;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductServices {

    private final Connection cnx;

    public ProductServices() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public void add(Product product) {
        String req = """
                INSERT INTO product
                (productName, description, dosage, form, laboratory, therapeuticClass, price, image, category,
                 availableQuantity, availabilityStatus, lastUpdateDate, pharmacyId)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try {
            PreparedStatement ps = cnx.prepareStatement(req);

            ps.setString(1, product.getProductName());
            ps.setString(2, product.getDescription());
            ps.setString(3, product.getDosage());
            ps.setString(4, product.getForm());
            ps.setString(5, product.getLaboratory());
            ps.setString(6, product.getTherapeuticClass());
            ps.setDouble(7, product.getPrice());
            ps.setString(8, product.getImage());
            ps.setString(9, product.getCategory());
            ps.setInt(10, product.getAvailableQuantity());
            ps.setString(11, product.getAvailabilityStatus());

            if (product.getLastUpdateDate() != null) {
                ps.setDate(12, product.getLastUpdateDate());
            } else {
                ps.setDate(12, new Date(System.currentTimeMillis()));
            }

            ps.setInt(13, product.getPharmacyId());

            ps.executeUpdate();
            System.out.println("Produit ajouté avec succès.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Product product) {
        String req = """
                UPDATE product
                SET productName = ?,
                    description = ?,
                    dosage = ?,
                    form = ?,
                    laboratory = ?,
                    therapeuticClass = ?,
                    price = ?,
                    image = ?,
                    category = ?,
                    availableQuantity = ?,
                    availabilityStatus = ?,
                    lastUpdateDate = ?,
                    pharmacyId = ?
                WHERE productId = ?
                """;

        try {
            PreparedStatement ps = cnx.prepareStatement(req);

            ps.setString(1, product.getProductName());
            ps.setString(2, product.getDescription());
            ps.setString(3, product.getDosage());
            ps.setString(4, product.getForm());
            ps.setString(5, product.getLaboratory());
            ps.setString(6, product.getTherapeuticClass());
            ps.setDouble(7, product.getPrice());
            ps.setString(8, product.getImage());
            ps.setString(9, product.getCategory());
            ps.setInt(10, product.getAvailableQuantity());
            ps.setString(11, product.getAvailabilityStatus());

            if (product.getLastUpdateDate() != null) {
                ps.setDate(12, product.getLastUpdateDate());
            } else {
                ps.setDate(12, new Date(System.currentTimeMillis()));
            }

            ps.setInt(13, product.getPharmacyId());
            ps.setInt(14, product.getProductId());

            ps.executeUpdate();
            System.out.println("Produit modifié avec succès.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Product product) {
        String req = "DELETE FROM product WHERE productId = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, product.getProductId());
            ps.executeUpdate();
            System.out.println("Produit supprimé avec succès.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Product> getAll() {
        List<Product> products = new ArrayList<>();

        String req = "SELECT * FROM product ORDER BY productId DESC";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                products.add(mapProduct(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public Product getById(int productId) {
        String req = "SELECT * FROM product WHERE productId = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, productId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapProduct(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Product> getSimilarProducts(Product product) {
        List<Product> similarProducts = new ArrayList<>();

        if (product == null) {
            return similarProducts;
        }

        String req = """
                SELECT * FROM product
                WHERE productId <> ?
                AND LOWER(productName) <> LOWER(?)
                AND (
                    LOWER(category) = LOWER(?)
                    OR LOWER(therapeuticClass) = LOWER(?)
                )
                AND availableQuantity > 0
                ORDER BY availableQuantity DESC
                LIMIT 5
                """;

        try {
            PreparedStatement ps = cnx.prepareStatement(req);

            ps.setInt(1, product.getProductId());
            ps.setString(2, safe(product.getProductName()));
            ps.setString(3, safe(product.getCategory()));
            ps.setString(4, safe(product.getTherapeuticClass()));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                similarProducts.add(mapProduct(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return similarProducts;
    }

    public boolean productExistsInPharmacy(String productName, String dosage, int pharmacyId) {
        String req = """
                SELECT COUNT(*) AS total
                FROM product
                WHERE LOWER(productName) = LOWER(?)
                AND LOWER(dosage) = LOWER(?)
                AND pharmacyId = ?
                """;

        try {
            PreparedStatement ps = cnx.prepareStatement(req);

            ps.setString(1, safe(productName));
            ps.setString(2, safe(dosage));
            ps.setInt(3, pharmacyId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("total") > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<Product> getProductsByPharmacy(int pharmacyId) {
        List<Product> products = new ArrayList<>();

        String req = "SELECT * FROM product WHERE pharmacyId = ? ORDER BY productName ASC";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, pharmacyId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                products.add(mapProduct(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public List<Product> getLowStockProducts() {
        List<Product> products = new ArrayList<>();

        String req = """
                SELECT * FROM product
                WHERE availableQuantity > 0
                AND availableQuantity <= 10
                ORDER BY availableQuantity ASC
                """;

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                products.add(mapProduct(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public List<Product> getOutOfStockProducts() {
        List<Product> products = new ArrayList<>();

        String req = """
                SELECT * FROM product
                WHERE availableQuantity = 0
                OR LOWER(availabilityStatus) = 'rupture'
                OR LOWER(availabilityStatus) = 'rupture de stock'
                OR LOWER(availabilityStatus) = 'indisponible'
                ORDER BY productName ASC
                """;

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                products.add(mapProduct(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
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
        product.setAvailableQuantity(rs.getInt("availableQuantity"));
        product.setAvailabilityStatus(rs.getString("availabilityStatus"));
        product.setLastUpdateDate(rs.getDate("lastUpdateDate"));
        product.setPharmacyId(rs.getInt("pharmacyId"));

        return product;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}