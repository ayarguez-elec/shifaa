package services;

import entities.CalendarEvent;
import entities.CalendarEvent.EventType;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarEventService {

    private Connection getCon() {
        return MyDataBase.getInstance().getConnection();
    }

    public CalendarEventService() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS `calendar_event` (" +
                "`id`          INT AUTO_INCREMENT PRIMARY KEY, " +
                "`userId`      INT NOT NULL, " +
                "`date`        DATE NOT NULL, " +
                "`time`        VARCHAR(5) NOT NULL, " +
                "`title`       VARCHAR(255) NOT NULL, " +
                "`description` TEXT, " +
                "`type`        ENUM('reunion','consultation','rappel','autre') DEFAULT 'autre', " +
                "`color`       VARCHAR(10) DEFAULT '#4A6CF7', " +
                "FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE" +
                ")";
        try {
            getCon().createStatement().execute(sql);
        } catch (SQLException e) {
            System.err.println("Erreur création table calendar_event : " + e.getMessage());
        }
    }

    /** Ajoute un événement et retourne son id généré. */
    public int add(CalendarEvent ev) throws SQLException {
        String sql = "INSERT INTO `calendar_event` (`userId`,`date`,`time`,`title`,`description`,`type`,`color`) " +
                     "VALUES (?,?,?,?,?,?,?)";
        PreparedStatement pst = getCon().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pst.setInt(1, ev.getUserId());
        pst.setDate(2, Date.valueOf(ev.getDate()));
        pst.setString(3, ev.getTime());
        pst.setString(4, ev.getTitle());
        pst.setString(5, ev.getDescription());
        pst.setString(6, ev.getType().name());
        pst.setString(7, ev.getColor());
        pst.executeUpdate();
        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) { ev.setId(rs.getInt(1)); }
        return ev.getId();
    }

    /** Supprime un événement par son id. */
    public void delete(int id) throws SQLException {
        PreparedStatement pst = getCon().prepareStatement(
                "DELETE FROM `calendar_event` WHERE `id` = ?");
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    /** Retourne tous les événements d'un utilisateur pour une date donnée, triés par heure. */
    public List<CalendarEvent> getByUserAndDate(int userId, LocalDate date) throws SQLException {
        List<CalendarEvent> list = new ArrayList<>();
        PreparedStatement pst = getCon().prepareStatement(
                "SELECT * FROM `calendar_event` WHERE `userId`=? AND `date`=? ORDER BY `time` ASC");
        pst.setInt(1, userId);
        pst.setDate(2, Date.valueOf(date));
        ResultSet rs = pst.executeQuery();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    /** Retourne toutes les dates qui ont au moins un événement pour un utilisateur (pour le mois courant). */
    public List<LocalDate> getDatesWithEvents(int userId) throws SQLException {
        List<LocalDate> dates = new ArrayList<>();
        PreparedStatement pst = getCon().prepareStatement(
                "SELECT DISTINCT `date` FROM `calendar_event` WHERE `userId`=?");
        pst.setInt(1, userId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) dates.add(rs.getDate("date").toLocalDate());
        return dates;
    }

    public CalendarEvent getNextEventForUser(int userId) throws SQLException {
        String sql = "SELECT * FROM `calendar_event` WHERE `userId` = ? AND `date` >= ? ORDER BY `date` ASC, `time` ASC LIMIT 1";
        try (PreparedStatement pst = getCon().prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.setDate(2, Date.valueOf(LocalDate.now()));
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        }
        return null;
    }

    private CalendarEvent map(ResultSet rs) throws SQLException {
        CalendarEvent ev = new CalendarEvent();
        ev.setId(rs.getInt("id"));
        ev.setUserId(rs.getInt("userId"));
        ev.setDate(rs.getDate("date").toLocalDate());
        ev.setTime(rs.getString("time"));
        ev.setTitle(rs.getString("title"));
        ev.setDescription(rs.getString("description"));
        try { ev.setType(EventType.valueOf(rs.getString("type"))); }
        catch (Exception e) { ev.setType(EventType.autre); }
        ev.setColor(rs.getString("color"));
        return ev;
    }
}
