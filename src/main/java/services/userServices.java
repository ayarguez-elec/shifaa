package services;

import entities.user;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class userServices implements ICrud<user> {

    private Connection getCon() {
        return MyDataBase.getInstance().getConnection();
    }

    public userServices() {
        checkAndAddColumns();
    }

    private void checkAndAddColumns() {
        Connection conn = getCon();
        if (conn == null) {
            System.err.println("Impossible de vérifier les colonnes : la connexion est nulle.");
            return;
        }
        try {
            DatabaseMetaData meta = conn.getMetaData();
            
            // Check avatar_url
            ResultSet rs = meta.getColumns(null, null, "user", "avatar_url");
            if (!rs.next()) {
                conn.createStatement().execute("ALTER TABLE `user` ADD COLUMN `avatar_url` LONGTEXT DEFAULT NULL");
                System.out.println("Colonne avatar_url ajoutée en LONGTEXT.");
            } else {
                // S'assure que la colonne existante est assez large (au cas où elle était en VARCHAR)
                conn.createStatement().execute("ALTER TABLE `user` MODIFY COLUMN `avatar_url` LONGTEXT DEFAULT NULL");
            }
            
            // Check professional_document
            // Check professional_document
            rs = meta.getColumns(null, null, "user", "professional_document");
            if (!rs.next()) {
                conn.createStatement().execute("ALTER TABLE `user` ADD COLUMN `professional_document` LONGTEXT DEFAULT NULL");
                System.out.println("Colonne professional_document ajoutée avec succès.");
            }
            
            // Check registration_date
            rs = meta.getColumns(null, null, "user", "registration_date");
            if (!rs.next()) {
                conn.createStatement().execute("ALTER TABLE `user` ADD COLUMN `registration_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                System.out.println("Colonne registration_date ajoutée avec succès.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification/ajout des colonnes : " + e.getMessage());
        }
    }

    @Override
    public void add(user user) throws SQLException {
        Connection conn = getCon();
        if (conn == null) {
            System.err.println("Échec de l'ajout : connexion DB nulle.");
            return;
        }
        String sql = "INSERT INTO `user` (`firstName`, `lastName`, `email`, `password`, `address`, `role`, `age`, `gender`, `phone`, `specialty`, `orderNumber`, `consultationFee`, `pharmacyName`, `licenceNumber`, `userPhoto`, `avatar_url`, `professional_document`, `accountStatus`, `doctorId`, `registration_date`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, user.getFirstName());
        pst.setString(2, user.getLastName());
        pst.setString(3, user.getEmail());
        pst.setString(4, user.getPassword());
        pst.setString(5, user.getAddress());
        pst.setString(6, user.getRole().name());
        pst.setInt(7, user.getAge());
        pst.setString(8, user.getGender().name());
        pst.setString(9, user.getPhone() != null ? user.getPhone() : "");
        pst.setString(10, user.getSpecialty() != null ? user.getSpecialty() : "");
        pst.setString(11, user.getOrderNumber() != null ? user.getOrderNumber() : "");
        pst.setFloat(12, user.getConsultationFee());
        pst.setString(13, user.getPharmacyName() != null ? user.getPharmacyName() : "");
        pst.setString(14, user.getLicenceNumber() != null ? user.getLicenceNumber() : "");
        pst.setString(15, user.getUserPhoto() != null ? user.getUserPhoto() : "");
        pst.setString(16, user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        pst.setString(17, user.getProfessionalDocument() != null ? user.getProfessionalDocument() : "");
        pst.setString(18, user.getAccountStatus().name());
        pst.setInt(19, user.getDoctorId());
        pst.setTimestamp(20, new java.sql.Timestamp(System.currentTimeMillis()));
        pst.executeUpdate();
        System.out.println("Utilisateur ajouté avec succès !");
    }

    @Override
    public void delete(user user) throws SQLException {
        Connection conn = getCon();
        if (conn == null) return;
        String sql = "DELETE FROM `user` WHERE `userId` = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, user.getUserId());
        pst.executeUpdate();
        System.out.println("Utilisateur supprimé !");
    }

    @Override
    public List<user> getAll() throws SQLException {
        List<user> users = new ArrayList<>();
        Connection conn = getCon();
        if (conn == null) return users;
        String sql = "SELECT * FROM `user`";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            user u = new user();
            u.setUserId(rs.getInt("userId"));
            u.setFirstName(rs.getString("firstName"));
            u.setLastName(rs.getString("lastName"));
            u.setEmail(rs.getString("email"));
            u.setPassword(rs.getString("password"));
            u.setAddress(rs.getString("address"));
            String roleStr = rs.getString("role");
            if (roleStr != null) {
                try {
                    u.setRole(user.Role.valueOf(roleStr.toLowerCase().trim()));
                } catch (IllegalArgumentException e) {
                    System.err.println("Role inconnu dans la DB: " + roleStr);
                    u.setRole(user.Role.patient); // Fallback
                }
            }
            u.setAge(rs.getInt("age"));
            u.setGender(user.Gender.valueOf(rs.getString("gender")));
            u.setPhone(rs.getString("phone"));
            u.setSpecialty(rs.getString("specialty"));
            u.setOrderNumber(rs.getString("orderNumber"));
            u.setConsultationFee(rs.getFloat("consultationFee"));
            u.setPharmacyName(rs.getString("pharmacyName"));
            u.setLicenceNumber(rs.getString("licenceNumber"));
            u.setUserPhoto(rs.getString("userPhoto"));
            
            try {
                u.setAvatarUrl(rs.getString("avatar_url"));
            } catch (SQLException e) {
                u.setAvatarUrl(null);
            }

            try {
                u.setProfessionalDocument(rs.getString("professional_document"));
            } catch (SQLException e) {
                u.setProfessionalDocument(null);
            }

            String statusStr = rs.getString("accountStatus");
            if (statusStr != null) {
                try {
                    u.setAccountStatus(user.AccountStatus.valueOf(statusStr.toLowerCase().trim()));
                } catch (IllegalArgumentException e) {
                    u.setAccountStatus(user.AccountStatus.active);
                }
            }
            
            try {
                u.setDoctorId(rs.getInt("doctorId"));
            } catch (SQLException e) {
                u.setDoctorId(0);
            }
            
            try {
                u.setRegistrationDate(rs.getTimestamp("registration_date"));
            } catch (SQLException e) {
                u.setRegistrationDate(null);
            }
            
            users.add(u);
        }
        return users;
    }

    @Override
    public void update(user user) throws SQLException {
        Connection conn = getCon();
        if (conn == null) return;
        String sql = "UPDATE `user` SET `firstName`=?, `lastName`=?, `email`=?, `password`=?, `address`=?, `role`=?, `age`=?, `gender`=?, `phone`=?, `specialty`=?, `orderNumber`=?, `consultationFee`=?, `pharmacyName`=?, `licenceNumber`=?, `userPhoto`=?, `avatar_url`=?, `professional_document`=?, `accountStatus`=?, `doctorId`=? WHERE `userId`=?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, user.getFirstName());
        pst.setString(2, user.getLastName());
        pst.setString(3, user.getEmail());
        pst.setString(4, user.getPassword());
        pst.setString(5, user.getAddress());
        pst.setString(6, user.getRole().name());
        pst.setInt(7, user.getAge());
        pst.setString(8, user.getGender().name());
        pst.setString(9, user.getPhone() != null ? user.getPhone() : "");
        pst.setString(10, user.getSpecialty() != null ? user.getSpecialty() : "");
        pst.setString(11, user.getOrderNumber() != null ? user.getOrderNumber() : "");
        pst.setFloat(12, user.getConsultationFee());
        pst.setString(13, user.getPharmacyName() != null ? user.getPharmacyName() : "");
        pst.setString(14, user.getLicenceNumber() != null ? user.getLicenceNumber() : "");
        pst.setString(15, user.getUserPhoto() != null ? user.getUserPhoto() : "");
        pst.setString(16, user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        pst.setString(17, user.getProfessionalDocument() != null ? user.getProfessionalDocument() : "");
        pst.setString(18, user.getAccountStatus().name());
        pst.setInt(19, user.getDoctorId());
        pst.setInt(20, user.getUserId());
        pst.executeUpdate();
        System.out.println("Utilisateur mis à jour !");
    }
}