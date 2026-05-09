package services;

import entities.user;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class userServices implements ICrud<user> {

    private Connection con = MyDataBase.getInstance().getConnection();

    @Override
    public void add(user user) throws SQLException {
        String sql = "INSERT INTO `user` (`firstName`, `lastName`, `email`, `password`, `address`, `role`, `age`, `gender`, `phone`, `specialty`, `orderNumber`, `consultationFee`, `pharmacyName`, `licenceNumber`, `userPhoto`, `accountStatus`, `doctorId`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, user.getFirstName());
        pst.setString(2, user.getLastName());
        pst.setString(3, user.getEmail());
        pst.setString(4, user.getPassword());
        pst.setString(5, user.getAddress());
        pst.setString(6, user.getRole().name());
        pst.setInt(7, user.getAge());
        pst.setString(8, user.getGender().name());
        pst.setString(9, user.getPhone());
        pst.setString(10, user.getSpecialty());
        pst.setString(11, user.getOrderNumber());
        pst.setFloat(12, user.getConsultationFee());
        pst.setString(13, user.getPharmacyName());
        pst.setString(14, user.getLicenceNumber());
        pst.setString(15, user.getUserPhoto());
        pst.setString(16, user.getAccountStatus().name());
        pst.setInt(17, user.getDoctorId());
        pst.executeUpdate();
        System.out.println("Utilisateur ajouté avec succès !");
    }

    @Override
    public void delete(user user) throws SQLException {
        String sql = "DELETE FROM `user` WHERE `userId` = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, user.getUserId());
        pst.executeUpdate();
        System.out.println("Utilisateur supprimé !");
    }

    @Override
    public List<user> getAll() throws SQLException {
        List<user> users = new ArrayList<>();
        String sql = "SELECT * FROM `user`";
        Statement st = con.createStatement();
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
                // Column might not exist yet
                u.setDoctorId(0);
            }
            
            users.add(u);
        }
        return users;
    }

    @Override
    public void update(user user) throws SQLException {
        String sql = "UPDATE `user` SET `firstName`=?, `lastName`=?, `email`=?, `password`=?, `address`=?, `role`=?, `age`=?, `gender`=?, `phone`=?, `specialty`=?, `orderNumber`=?, `consultationFee`=?, `pharmacyName`=?, `licenceNumber`=?, `userPhoto`=?, `accountStatus`=?, `doctorId`=? WHERE `userId`=?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, user.getFirstName());
        pst.setString(2, user.getLastName());
        pst.setString(3, user.getEmail());
        pst.setString(4, user.getPassword());
        pst.setString(5, user.getAddress());
        pst.setString(6, user.getRole().name());
        pst.setInt(7, user.getAge());
        pst.setString(8, user.getGender().name());
        pst.setString(9, user.getPhone());
        pst.setString(10, user.getSpecialty());
        pst.setString(11, user.getOrderNumber());
        pst.setFloat(12, user.getConsultationFee());
        pst.setString(13, user.getPharmacyName());
        pst.setString(14, user.getLicenceNumber());
        pst.setString(15, user.getUserPhoto());
        pst.setString(16, user.getAccountStatus().name());
        pst.setInt(17, user.getDoctorId());
        pst.setInt(18, user.getUserId());
        pst.executeUpdate();
        System.out.println("Utilisateur mis à jour !");
    }
}