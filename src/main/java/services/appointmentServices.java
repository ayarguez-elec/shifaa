package services;

import entities.appointment;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class appointmentServices implements IServiceCrud<appointment>
{
private Connection con = MyDataBase.getInstance().getConnection();

public appointmentServices() {
    if (con != null) {
        try {
            Statement st = con.createStatement();

            try {
                st.executeUpdate("ALTER TABLE appointment ADD COLUMN status VARCHAR(20) DEFAULT 'EN_ATTENTE'");
            } catch (SQLException ignored) {}
            try {
                st.executeUpdate("ALTER TABLE appointment ADD COLUMN meetingLink VARCHAR(255)");
            } catch (SQLException ignored) {}
            try {
                st.executeUpdate("ALTER TABLE appointment ADD COLUMN availabilityId INT DEFAULT NULL");
            } catch (SQLException ignored) {}

            // Auto-seed doctors if tables are empty
            String[] tables = {"user", "users"};
            for (String table : tables) {
                try {
                    // Ensure columns exist
                    String[] cols = {"specialite", "prenom", "nom", "specialty", "firstName", "lastName", "email", "adresse", "address"};
                    for (String col : cols) {
                        try { st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + col + " VARCHAR(255)"); } catch (SQLException ignored) {}
                    }


                    // Check if we already have doctors
                    ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE role IN ('medecin', 'doctor', 'DOCTOR')");
                    if (rs.next() && rs.getInt(1) == 0) {
                        String[][] doctors = {
                            {"Cardiologue", "Ahmed", "Mansour", "medecin"},
                            {"Pédiatre", "Sami", "Ben Ali", "medecin"},
                            {"Généraliste", "Lina", "Gharbi", "medecin"},
                            {"Dermatologue", "Yassine", "Zaid", "medecin"},
                            {"Neurologue", "Omar", "Khelil", "medecin"},
                            {"Ophtalmologue", "Hiba", "Tlili", "medecin"},
                            {"Gynécologue", "Sara", "Amira", "medecin"},
                            {"Orthopédiste", "Karim", "Zaki", "medecin"},
                            {"Psychiatre", "Leila", "Masmoudi", "medecin"},
                            {"Dentiste", "Firas", "Jallouli", "medecin"},
                            {"Urologue", "Hedi", "Belhaj", "medecin"},
                            {"Radiologue", "Nizar", "Chaker", "medecin"},
                            {"Endocrinologue", "Mouna", "Feki", "medecin"},
                            {"Gastro-entérologue", "Riadh", "Hammami", "medecin"}
                        };
                        String insertSql = "INSERT INTO " + table + " (specialite, specialty, prenom, firstName, nom, lastName, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement pst = con.prepareStatement(insertSql);
                        for (String[] d : doctors) {
                            pst.setString(1, d[0]); pst.setString(2, d[0]);
                            pst.setString(3, d[1]); pst.setString(4, d[1]);
                            pst.setString(5, d[2]); pst.setString(6, d[2]);
                            pst.setString(7, d[3]);
                            pst.executeUpdate();
                        }
                        System.out.println("✓ Sample doctors seeded into table " + table);
                    }

                    // Check if we already have patients
                    rs = st.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE role = 'patient'");
                    if (rs.next() && rs.getInt(1) == 0) {
                        String[][] patients = {
                            {"Aya", "Rguez", "aya.rguez@email.com"},
                            {"Youssef", "Kallel", "youssef.kallel@email.com"},
                            {"Amine", "Trabelsi", "amine.trabelsi@email.com"},
                            {"Sonia", "Masmoudi", "sonia.masmoudi@email.com"}
                        };
                        String insertSql = "INSERT INTO " + table + " (prenom, firstName, nom, lastName, email, role) VALUES (?, ?, ?, ?, ?, 'patient')";
                        PreparedStatement pst = con.prepareStatement(insertSql);
                        for (String[] p : patients) {
                            pst.setString(1, p[0]); pst.setString(2, p[0]);
                            pst.setString(3, p[1]); pst.setString(4, p[1]);
                            pst.setString(5, p[2]);
                            pst.executeUpdate();
                        }
                        System.out.println("✓ Sample patients seeded into table " + table);
                    }

                } catch (SQLException e) {
                    // Table might not exist, ignore
                }
            }
        } catch (SQLException e) {
            System.err.println("DB Initialization error: " + e.getMessage());
        }
    }
}


public boolean doctorExists(int doctorId) {
    String[][] candidates = {
        {"user",   "id"},
        {"users",  "id"},
        {"doctor", "id"},
        {"doctor", "doctorId"},
        {"user",   "userId"},
        {"users",  "userId"}
    };
    for (String[] c : candidates) {
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT COUNT(*) FROM `" + c[0] + "` WHERE `" + c[1] + "` = ?");
            pst.setInt(1, doctorId);
            ResultSet rs = pst.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return true;
        } catch (SQLException ignored) {}
    }
    return false;
}

public boolean patientExists(int patientId) {
    String[][] candidates = {
        {"user",    "id"},
        {"users",   "id"},
        {"patient", "id"},
        {"patient", "patientId"},
        {"user",    "userId"},
        {"users",   "userId"}
    };
    for (String[] c : candidates) {
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT COUNT(*) FROM `" + c[0] + "` WHERE `" + c[1] + "` = ?");
            pst.setInt(1, patientId);
            ResultSet rs = pst.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return true;
        } catch (SQLException ignored) {}
    }
    return false;
}

@Override
public void add(appointment appointment) throws SQLException {
    if (con == null) throw new SQLException("Connexion DB non disponible.");

    // Validation FK avant insertion
    if (!doctorExists(appointment.getDoctorId())) {
        throw new SQLException("Le médecin ID " + appointment.getDoctorId() + " n'existe pas dans la base.");
    }
    if (!patientExists(appointment.getPatientId())) {
        throw new SQLException("Le patient ID " + appointment.getPatientId() + " n'existe pas dans la base.");
    }

    String sql = "INSERT INTO appointment (appointmentDate, appointmentTime, reason, type, paymentMethod, patientId, doctorId, status, meetingLink, availabilityId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement pst = con.prepareStatement(sql);
    pst.setDate(1, appointment.getAppointmentDate());
    pst.setTime(2, appointment.getAppointmentTime());
    pst.setString(3, appointment.getReason());
    pst.setString(4, appointment.getType());
    pst.setString(5, appointment.getPaymentMethodAsString());
    pst.setInt(6, appointment.getPatientId());
    pst.setInt(7, appointment.getDoctorId());
    pst.setString(8, appointment.getStatus() != null ? appointment.getStatus() : "EN_ATTENTE");
    pst.setString(9, appointment.getMeetingLink());
    if (appointment.getAvailabilityId() > 0) {
        pst.setInt(10, appointment.getAvailabilityId());
    } else {
        pst.setNull(10, java.sql.Types.INTEGER);
    }
    pst.executeUpdate();
    System.out.println("Rendez-vous ajouté avec succès !");
}

@Override
public void delete(appointment appointment) throws SQLException {
    String sql = "DELETE FROM appointment WHERE appointmentId = ?";
    PreparedStatement pst = con.prepareStatement(sql);
    pst.setInt(1, appointment.getAppointmentId());
    pst.executeUpdate();
    System.out.println("Rendez-vous supprimé !");
}

@Override
public java.util.List<appointment> getAll() throws SQLException {
    java.util.List<appointment> appointments = new ArrayList<>();
    if (con == null) return appointments;
    String sql = "SELECT * FROM appointment";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);
    while (rs.next()) {
        appointment a = new appointment();
        a.setAppointmentId(rs.getInt("appointmentId"));
        a.setAppointmentDate(rs.getDate("appointmentDate"));
        a.setAppointmentTime(rs.getTime("appointmentTime"));
        a.setReason(rs.getString("reason"));
        a.setType(rs.getString("type"));
        a.setPaymentMethodFromString(rs.getString("paymentMethod"));
        a.setPatientId(rs.getInt("patientId"));
        a.setDoctorId(rs.getInt("doctorId"));
        a.setStatus(rs.getString("status"));
        try { a.setMeetingLink(rs.getString("meetingLink")); } catch (Exception ignored) {}
        try { a.setAvailabilityId(rs.getInt("availabilityId")); } catch (Exception ignored) {}
        appointments.add(a);
    }
    return appointments;
}

@Override
public void update(appointment appointment) throws SQLException {
    if (con == null) throw new SQLException("Connexion DB non disponible.");

    if (!doctorExists(appointment.getDoctorId())) {
        throw new SQLException("Le médecin ID " + appointment.getDoctorId() + " n'existe pas dans la base.");
    }
    if (!patientExists(appointment.getPatientId())) {
        throw new SQLException("Le patient ID " + appointment.getPatientId() + " n'existe pas dans la base.");
    }

    String sql = "UPDATE appointment SET appointmentDate=?, appointmentTime=?, reason=?, type=?, paymentMethod=?, patientId=?, doctorId=?, status=?, meetingLink=?, availabilityId=? WHERE appointmentId=?";
    PreparedStatement pst = con.prepareStatement(sql);
    pst.setDate(1, appointment.getAppointmentDate());
    pst.setTime(2, appointment.getAppointmentTime());
    pst.setString(3, appointment.getReason());
    pst.setString(4, appointment.getType());
    pst.setString(5, appointment.getPaymentMethodAsString());
    pst.setInt(6, appointment.getPatientId());
    pst.setInt(7, appointment.getDoctorId());
    pst.setString(8, appointment.getStatus());
    pst.setString(9, appointment.getMeetingLink());
    if (appointment.getAvailabilityId() > 0) {
        pst.setInt(10, appointment.getAvailabilityId());
    } else {
        pst.setNull(10, java.sql.Types.INTEGER);
    }
    pst.setInt(11, appointment.getAppointmentId());
    pst.executeUpdate();
    
    // === LOGIQUE METIER : Si le RDV est CONFIRME, marquer la dispo comme occupée ===
    if ("CONFIRME".equals(appointment.getStatus()) && appointment.getAvailabilityId() > 0) {
        markLinkedAvailabilityOccupied(appointment.getAvailabilityId());
    } else if ("CONFIRME".equals(appointment.getStatus()) && appointment.getAvailabilityId() == 0) {
        // Tenter de trouver la dispo correspondante automatiquement
        int foundId = findAvailabilityId(appointment.getDoctorId(), appointment.getAppointmentDate(), appointment.getAppointmentTime());
        if (foundId > 0) {
            markLinkedAvailabilityOccupied(foundId);
            appointment.setAvailabilityId(foundId);
        }
    }
    System.out.println("Rendez-vous mis à jour !");
}

// Additional useful methods specific to appointments

public java.util.List<appointment> getAppointmentsByPatient(int patientId) throws SQLException {
    java.util.List<appointment> appointments = new ArrayList<>();
    if (con == null) return appointments;
    String sql = "SELECT * FROM appointment WHERE patientId = ?";
    PreparedStatement pst = con.prepareStatement(sql);
    pst.setInt(1, patientId);
    ResultSet rs = pst.executeQuery();
    while (rs.next()) {
        appointment a = new appointment();
        a.setAppointmentId(rs.getInt("appointmentId"));
        a.setAppointmentDate(rs.getDate("appointmentDate"));
        a.setAppointmentTime(rs.getTime("appointmentTime"));
        a.setReason(rs.getString("reason"));
        a.setType(rs.getString("type"));
        a.setPaymentMethodFromString(rs.getString("paymentMethod"));
        a.setPatientId(rs.getInt("patientId"));
        a.setDoctorId(rs.getInt("doctorId"));
        a.setStatus(rs.getString("status"));
        try { a.setMeetingLink(rs.getString("meetingLink")); } catch (Exception ignored) {}
        try { a.setAvailabilityId(rs.getInt("availabilityId")); } catch (Exception ignored) {}
        appointments.add(a);
    }
    return appointments;
}

public java.util.List<appointment> getAppointmentsByDoctor(int doctorId) throws SQLException {
    java.util.List<appointment> appointments = new ArrayList<>();
    if (con == null) return appointments;
    String sql = "SELECT * FROM appointment WHERE doctorId = ?";
    PreparedStatement pst = con.prepareStatement(sql);
    pst.setInt(1, doctorId);
    ResultSet rs = pst.executeQuery();
    while (rs.next()) {
        appointment a = new appointment();
        a.setAppointmentId(rs.getInt("appointmentId"));
        a.setAppointmentDate(rs.getDate("appointmentDate"));
        a.setAppointmentTime(rs.getTime("appointmentTime"));
        a.setReason(rs.getString("reason"));
        a.setType(rs.getString("type"));
        a.setPaymentMethodFromString(rs.getString("paymentMethod"));
        a.setPatientId(rs.getInt("patientId"));
        a.setDoctorId(rs.getInt("doctorId"));
        a.setStatus(rs.getString("status"));
        try { a.setMeetingLink(rs.getString("meetingLink")); } catch (Exception ignored) {}
        try { a.setAvailabilityId(rs.getInt("availabilityId")); } catch (Exception ignored) {}
        appointments.add(a);
    }
    return appointments;
}

public appointment getAppointmentById(int appointmentId) throws SQLException {
    if (con == null) return null;
    String sql = "SELECT * FROM appointment WHERE appointmentId = ?";
    PreparedStatement pst = con.prepareStatement(sql);
    pst.setInt(1, appointmentId);
    ResultSet rs = pst.executeQuery();
    if (rs.next()) {
        appointment a = new appointment();
        a.setAppointmentId(rs.getInt("appointmentId"));
        a.setAppointmentDate(rs.getDate("appointmentDate"));
        a.setAppointmentTime(rs.getTime("appointmentTime"));
        a.setReason(rs.getString("reason"));
        a.setType(rs.getString("type"));
        a.setPaymentMethodFromString(rs.getString("paymentMethod"));
        a.setPatientId(rs.getInt("patientId"));
        a.setDoctorId(rs.getInt("doctorId"));
        a.setStatus(rs.getString("status"));
        try { a.setMeetingLink(rs.getString("meetingLink")); } catch (Exception ignored) {}
        try { a.setAvailabilityId(rs.getInt("availabilityId")); } catch (Exception ignored) {}
        return a;
    }
    return null;
}

public java.util.List<appointment> getAppointmentsByDate(Date date) throws SQLException {
    java.util.List<appointment> appointments = new ArrayList<>();
    if (con == null) return appointments;
    String sql = "SELECT * FROM appointment WHERE appointmentDate = ?";
    PreparedStatement pst = con.prepareStatement(sql);
    pst.setDate(1, date);
    ResultSet rs = pst.executeQuery();
    while (rs.next()) {
        appointment a = new appointment();
        a.setAppointmentId(rs.getInt("appointmentId"));
        a.setAppointmentDate(rs.getDate("appointmentDate"));
        a.setAppointmentTime(rs.getTime("appointmentTime"));
        a.setReason(rs.getString("reason"));
        a.setType(rs.getString("type"));
        a.setPaymentMethodFromString(rs.getString("paymentMethod"));
        a.setPatientId(rs.getInt("patientId"));
        a.setDoctorId(rs.getInt("doctorId"));
        a.setStatus(rs.getString("status"));
        try { a.setMeetingLink(rs.getString("meetingLink")); } catch (Exception ignored) {}
        appointments.add(a);
    }
    return appointments;
}

    /**
     * Retourne une Map {Spécialité -> Liste de Médecins (ID — Nom)}
     */
    public java.util.Map<String, List<String>> getDoctorsBySpecialty() {
        java.util.Map<String, List<String>> map = new java.util.TreeMap<>();
        if (con == null) return map;
        
        String[] queries = {
            "SELECT specialty, userId, firstName, lastName FROM user WHERE role = 'doctor'",
            "SELECT specialty, userId, firstName, lastName FROM users WHERE role = 'doctor'",
            "SELECT specialite, id, prenom, nom FROM user WHERE role IN ('medecin', 'doctor', 'DOCTOR')",
            "SELECT specialite, id, prenom, nom FROM users WHERE role IN ('medecin', 'doctor', 'DOCTOR')",
            "SELECT specialty, id, firstName, lastName FROM user WHERE role IN ('medecin', 'doctor', 'DOCTOR')",
            "SELECT specialty, id, firstName, lastName FROM users WHERE role IN ('medecin', 'doctor', 'DOCTOR')",
            "SELECT specialite, id, nom FROM doctor",
            "SELECT specialty, id, name FROM doctor",
            "SELECT 'Généraliste', id, nom, prenom FROM user", // Last resort: any user
            "SELECT 'Généraliste', id, nom, prenom FROM users"
        };

        for (String sql : queries) {
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                
                int count = 0;
                while (rs.next()) {
                    count++;
                    String spec = rs.getString(1);
                    if (spec == null || spec.isBlank()) spec = "Généraliste";
                    
                    int id = rs.getInt(2);
                    String name = "Inconnu";
                    try {
                        name = rs.getString(3);
                        if (sql.contains("prenom") || sql.contains("firstName")) {
                            name = rs.getString(3) + " " + rs.getString(4);
                        }
                    } catch (Exception e) {}
                    
                    String doctorInfo = "Dr. " + name + " (ID: " + id + ")";
                    map.computeIfAbsent(spec, k -> new ArrayList<>()).add(doctorInfo);
                }
                if (count > 0) {
                    System.out.println("✓ Success with query: " + sql + " (" + count + " found)");
                    return map;
                }
            } catch (SQLException e) {
                // System.out.println("X Failed query: " + sql + " - " + e.getMessage());
            }
        }
        System.out.println("! No doctors found in any table with any column variation.");
        return map;
    }

    public java.util.List<String> getPatientsList() {
        java.util.List<String> list = new ArrayList<>();
        if (con == null) return list;
        
        String[] queries = {
            "SELECT userId, firstName, lastName FROM user WHERE role = 'patient'",
            "SELECT userId, firstName, lastName FROM users WHERE role = 'patient'",
            "SELECT id, firstName, lastName FROM user WHERE role = 'patient'",
            "SELECT id, firstName, lastName FROM users WHERE role = 'patient'",
            "SELECT id, prenom, nom FROM user WHERE role = 'patient'",
            "SELECT id, nom, prenom FROM patient",
            "SELECT userId, firstName, lastName FROM user",
            "SELECT id, firstName, lastName FROM users"
        };


        for (String sql : queries) {
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String p1 = rs.getString(2);
                    String p2 = rs.getString(3);
                    
                    String fullName = (p1 != null ? p1 : "") + " " + (p2 != null ? p2 : "");
                    fullName = fullName.trim();
                    
                    if (fullName.isEmpty() || fullName.equalsIgnoreCase("null null")) {
                        fullName = "Utilisateur #" + id;
                    }
                    
                    String entry = fullName + " (ID: " + id + ")";
                    if (!list.contains(entry)) {
                        list.add(entry);
                    }
                }
                if (!list.isEmpty()) {
                    System.out.println("✓ " + list.size() + " patients chargés via: " + sql);
                    return list;
                }
            } catch (SQLException ignored) {}
        }
        System.err.println("! Attention: Aucun patient trouvé dans la base de données.");


        return list;
    }

public boolean isDoctorAvailable(int doctorId, Date date, Time time) throws SQLException {
    if (con == null) return false;
    String sql = "SELECT COUNT(*) FROM appointment WHERE doctorId = ? AND appointmentDate = ? AND appointmentTime = ?";
    PreparedStatement pst = con.prepareStatement(sql);
    pst.setInt(1, doctorId);
    pst.setDate(2, date);
    pst.setTime(3, time);
    ResultSet rs = pst.executeQuery();
    if (rs.next()) {
        return rs.getInt(1) == 0;
    }
    return true;
}

public String getUserFullName(int id) {
    if (con == null) return "Inconnu";
    
    // Define possible table/column combinations to try
    String[][] searchPaths = {
        {"user", "firstName", "lastName", "userId"},
        {"users", "firstName", "lastName", "userId"},
        {"user", "firstName", "lastName", "id"},
        {"users", "firstName", "lastName", "id"},
        {"user", "prenom", "nom", "id"},
        {"users", "prenom", "nom", "id"},
        {"doctor", "nom", "prenom", "id"},
        {"patient", "nom", "prenom", "id"}
    };


    for (String[] path : searchPaths) {
        String table = path[0];
        String col1 = path[1];
        String col2 = path[2];
        String idCol = path[3];
        
        String sql;
        if (col2.isEmpty()) {
            sql = "SELECT " + col1 + " FROM " + table + " WHERE " + idCol + " = ?";
        } else {
            sql = "SELECT " + col1 + ", " + col2 + " FROM " + table + " WHERE " + idCol + " = ?";
        }

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String fName = rs.getString(1);
                    String lName = col2.isEmpty() ? "" : rs.getString(2);
                    
                    StringBuilder full = new StringBuilder();
                    if (fName != null && !fName.isBlank() && !fName.equalsIgnoreCase("null")) full.append(fName.trim());
                    if (lName != null && !lName.isBlank() && !lName.equalsIgnoreCase("null")) {
                        if (full.length() > 0) full.append(" ");
                        full.append(lName.trim());
                    }
                    
                    String result = full.toString().trim();
                    if (!result.isEmpty()) return result;
                }
            }
        } catch (SQLException ignored) {
        }
    }
    
    return "Inconnu (ID: " + id + ")";
}

public java.util.Map<Integer, String> getAllUserNames() {
    java.util.Map<Integer, String> names = new java.util.HashMap<>();
    if (con == null) return names;
    
    String[] queries = {
        "SELECT userId, firstName, lastName FROM user",
        "SELECT id, firstName, lastName FROM users",
        "SELECT userId, firstName, lastName FROM users",
        "SELECT id, prenom, nom FROM user"
    };


    for (String sql : queries) {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt(1);
                String fName = rs.getString(2);
                String lName = rs.getString(3);
                names.putIfAbsent(id, (fName != null ? fName : "") + " " + (lName != null ? lName : ""));
            }
        } catch (SQLException ignored) {}
    }
    return names;
}
    public String getUserEmail(int id) {
        if (con == null) return "ysmn1422022@gmail.com";
        String[] tables = {"user", "users"};
        for (String table : tables) {
            try {
                // Try id first then userId
                String[] idCols = {"id", "userId"};
                for (String idCol : idCols) {
                    try (PreparedStatement pst = con.prepareStatement("SELECT email FROM " + table + " WHERE " + idCol + " = ?")) {
                        pst.setInt(1, id);
                        try (ResultSet rs = pst.executeQuery()) {
                            if (rs.next()) {
                                String email = rs.getString("email");
                                if (email != null && email.contains("@") && !email.contains("email.com")) {
                                    return email;
                                }
                            }
                        }
                    } catch (SQLException ignored) {}
                }
            } catch (Exception ignored) {}
        }
        return "ysmn1422022@gmail.com"; // Fallback pour que vous receviez l'email de test
    }

    public String[] getAssistantInfo() {
        if (con == null) return null;
        String[] queries = {
            "SELECT firstName, lastName, role FROM user WHERE role LIKE '%assistant%' LIMIT 1",
            "SELECT firstName, lastName, role FROM users WHERE role LIKE '%assistant%' LIMIT 1"
        };
        for (String sql : queries) {
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    return new String[]{rs.getString(1) + " " + rs.getString(2), rs.getString(3)};
                }
            } catch (SQLException ignored) {}
        }
        return null;
    }

    public String getUserAddress(int id) {
        if (con == null) return "Adresse non spécifiée";
        String[] tables = {"user", "users", "doctor"};
        for (String table : tables) {
            try {
                String[] idCols = {"id", "userId"};
                for (String idCol : idCols) {
                    try (PreparedStatement pst = con.prepareStatement("SELECT adresse, address FROM " + table + " WHERE " + idCol + " = ?")) {
                        pst.setInt(1, id);
                        try (ResultSet rs = pst.executeQuery()) {
                            if (rs.next()) {
                                String addr = rs.getString("adresse");
                                if (addr != null && !addr.trim().isEmpty() && !addr.equalsIgnoreCase("null")) return addr.trim();
                                String addrEn = rs.getString("address");
                                if (addrEn != null && !addrEn.trim().isEmpty() && !addrEn.equalsIgnoreCase("null")) return addrEn.trim();
                            }
                        }
                    } catch (SQLException ignored) {}
                }
            } catch (Exception ignored) {}
        }
        return "Adresse non spécifiée";
    }

    // ============================================================
    //  LIAISON RDV ↔ DISPONIBILITÉ
    // ============================================================

    /**
     * Marque la disponibilité comme occupée lorsqu'un RDV est confirmé.
     */
    private void markLinkedAvailabilityOccupied(int availabilityId) {
        try {
            PreparedStatement pst = con.prepareStatement(
                "UPDATE availability SET statusRDV = 'occupe' WHERE availabilityId = ?");
            pst.setInt(1, availabilityId);
            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("\u2713 Disponibilité #" + availabilityId + " marquée comme OCCUPÉE.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour disponibilité : " + e.getMessage());
        }
    }

    /**
     * Cherche une disponibilité libre correspondant au médecin, date et heure du RDV.
     * Utilisé comme fallback quand availabilityId n'est pas renseigné.
     */
    public int findAvailabilityId(int doctorId, java.sql.Date date, java.sql.Time time) {
        if (con == null) return 0;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT availabilityId FROM availability WHERE doctorId=? AND date=? AND time=? AND statusRDV='libre' LIMIT 1");
            pst.setInt(1, doctorId);
            pst.setDate(2, date);
            pst.setTime(3, time);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("findAvailabilityId error: " + e.getMessage());
        }
        return 0;
    }
}

