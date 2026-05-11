package services;
import entities.Prescription;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Prescriptionserv implements iCrud<Prescription> {
    
    private Connection getConnection() throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("❌ Connexion à la base de données perdue.");
        }
        return connection;
    }

    @Override
    public void add(Prescription p) throws SQLException {
        String sql = "INSERT INTO prescription (prescriptionDate, prescription, treatmentDuration, doctorId, patientId, idfile) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(p.getPrescriptionDate()));
            pst.setString(2, p.getPrescription());
            pst.setString(3, p.getTreatmentDuration());
            pst.setInt(4, p.getDoctorId());
            pst.setInt(5, p.getPatientId());
            pst.setInt(6, p.getIdfile());
            pst.executeUpdate();
        }
    }

    @Override
    public void deleteById(int prescriptionId) throws SQLException {
        String sql = "DELETE FROM prescription WHERE prescriptionId = ?";
        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, prescriptionId);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Prescription> getAll() throws SQLException {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.* FROM prescription p JOIN user u ON p.patientId = u.userId WHERE u.role = 'patient'";
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Prescription p = new Prescription();
                p.setPrescriptionId(rs.getInt("prescriptionId"));
                p.setPrescriptionDate(rs.getDate("prescriptionDate").toLocalDate());
                p.setPrescription(rs.getString("prescription"));
                p.setTreatmentDuration(rs.getString("treatmentDuration"));
                p.setDoctorId(rs.getInt("doctorId"));
                p.setPatientId(rs.getInt("patientId"));
                p.setIdfile(rs.getInt("idfile"));
                list.add(p);
            }
        }
        return list;
    }

    @Override
    public void update(Prescription p) throws SQLException {
        String sql = "UPDATE prescription SET prescriptionDate=?, prescription=?, treatmentDuration=?, doctorId=?, patientId=?, idfile=? WHERE prescriptionId=?";
        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(p.getPrescriptionDate()));
            pst.setString(2, p.getPrescription());
            pst.setString(3, p.getTreatmentDuration());
            pst.setInt(4, p.getDoctorId());
            pst.setInt(5, p.getPatientId());
            pst.setInt(6, p.getIdfile());
            pst.setInt(7, p.getPrescriptionId());
            pst.executeUpdate();
        }
    }

    public List<Prescription> getByPatientId(int patientId) throws SQLException {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.* FROM prescription p JOIN user u ON p.patientId = u.userId WHERE p.patientId = ? AND u.role = 'patient' ORDER BY p.prescriptionDate DESC";
        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, patientId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Prescription p = new Prescription();
                p.setPrescriptionId(rs.getInt("prescriptionId"));
                p.setPrescriptionDate(rs.getDate("prescriptionDate").toLocalDate());
                p.setPrescription(rs.getString("prescription"));
                p.setTreatmentDuration(rs.getString("treatmentDuration"));
                p.setDoctorId(rs.getInt("doctorId"));
                p.setPatientId(rs.getInt("patientId"));
                p.setIdfile(rs.getInt("idfile"));
                list.add(p);
            }
        }
        return list;
    }
}
