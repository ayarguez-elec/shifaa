package services;

import entities.Reservation;
import entities.Reservation.Statut;
import entities.event;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║         SMART BOOKING ENGINE  –  Métier 2                       ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  Rules:                                                         ║
 * ║  1. FIFO – requests processed in chronological order            ║
 * ║  2. Multi-place – one user can reserve N seats at once          ║
 * ║  3. Capacity guard – refuses if total places exceed capaciteMax ║
 * ║  4. Fully automated – no human intervention required            ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class ReservationServ {

    private final Connection         cnx          = MyDataBase.getInstance().getConnection();
    private final eventServ          eServ        = new eventServ();
    private final NotificationService notifService = new NotificationService();

    // ─────────────────────────────────────────────────────────────────────────
    // CORE SMART-BOOKING LOGIC
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Main entry point.
     * Submits a reservation request. 
     * Rules: 
     * - If space available -> CONFIRME
     * - If no space -> LISTE_D_ATTENTE (Waitlist)
     */
    public Reservation soumettreDemande(int eventId, int participantId, int nombrePlaces) {
        Reservation res = new Reservation(eventId, participantId, nombrePlaces);

        if (nombrePlaces <= 0) {
            res.setStatut(Statut.REFUSE);
            res.setMotifRefus("Le nombre de places demandé doit être positif.");
            persister(res);
            notifService.notifierAsync(res);
            return res;
        }

        event ev = eServ.getById(eventId);
        if (ev == null) {
            res.setStatut(Statut.REFUSE);
            res.setMotifRefus("Événement introuvable.");
            persister(res);
            notifService.notifierAsync(res);
            return res;
        }
        if ("Annulé".equalsIgnoreCase(ev.getStatus()) || "Terminé".equalsIgnoreCase(ev.getStatus())) {
            res.setStatut(Statut.REFUSE);
            res.setMotifRefus("L'événement n'accepte plus d'inscriptions.");
            persister(res);
            notifService.notifierAsync(res);
            return res;
        }

        if (hasActiveReservation(eventId, participantId)) {
            res.setStatut(Statut.REFUSE);
            res.setMotifRefus("Déjà inscrit ou en liste d'attente.");
            persister(res);
            notifService.notifierAsync(res);
            return res;
        }

        // Capacity check
        int cap = ev.getCapaciteMax();
        int pris = getPlacesConfirmees(eventId);
        int restantes = cap - pris;

        if (cap > 0 && nombrePlaces > restantes) {
            // ── WAITLIST (Liste d'attente) ──────────────────────────────────
            res.setStatut(Statut.LISTE_D_ATTENTE);
            res.setMotifRefus("Événement complet. Vous avez été placé en liste d'attente.");
            persister(res);
            notifService.notifierAsync(res);
            System.out.println("⏳ [Waitlist] Participant " + participantId + " ajouté à la liste d'attente (event " + eventId + ")");
        } else {
            // ── CONFIRMED ───────────────────────────────────────────────────
            res.setStatut(Statut.CONFIRME);
            persister(res);
            notifService.notifierAsync(res);
            System.out.println("✅ [SmartBooking] Réservation CONFIRMÉE : participant " + participantId);
        }

        return res;
    }

    /**
     * Cancel a reservation and AUTOMATICALLY release places to the next in waitlist.
     */
    public boolean annulerReservation(int reservationId) {
        // Find eventId before update
        int eventId = -1;
        String findSql = "SELECT event_id, statut FROM reservation WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(findSql)) {
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                eventId = rs.getInt("event_id");
                String oldStatut = rs.getString("statut");
                if ("ANNULE".equals(oldStatut) || "REFUSE".equals(oldStatut)) return false;
            }
        } catch (SQLException e) { return false; }

        String sql = "UPDATE reservation SET statut='ANNULE' WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("↩️ [SmartBooking] Réservation #" + reservationId + " annulée.");
                // ── LIBÉRATION AUTOMATIQUE ──
                traiterListeDAttente(eventId);
                return true;
            }
        } catch (SQLException ex) { System.err.println(ex.getMessage()); }
        return false;
    }

    /**
     * SECURE CHECK-IN: Marks a participant as present.
     */
    public boolean checkIn(int reservationId) {
        String sql = "UPDATE reservation SET statut='CHECKED_IN', date_checkin=NOW() WHERE id=? AND statut='CONFIRME'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("🎯 [Check-In] Réservation #" + reservationId + " validée à l'entrée.");
                return true;
            }
        } catch (SQLException ex) { System.err.println(ex.getMessage()); }
        return false;
    }

    /**
     * PROCESS WAITLIST (FIFO): Promotes participants if space becomes available.
     */
    public void traiterListeDAttente(int eventId) {
        if (eventId <= 0) return;
        
        event ev = eServ.getById(eventId);
        if (ev == null || ev.getCapaciteMax() <= 0) return;

        // Get waitlist ordered by date (FIFO)
        List<Reservation> waitlist = query("SELECT * FROM reservation WHERE event_id=? AND statut='LISTE_D_ATTENTE' ORDER BY date_demande ASC", eventId);
        
        for (Reservation res : waitlist) {
            int restantes = getPlacesRestantes(eventId);
            if (res.getNombrePlaces() <= restantes) {
                // PROMOTE TO CONFIRMED
                updateStatus(res.getId(), Statut.CONFIRME);
                res.setStatut(Statut.CONFIRME);
                notifService.notifierAsync(res); // Send new QR + Confirmation email
                System.out.println("✨ [Waitlist] Promotion : #" + res.getId() + " est maintenant CONFIRMÉ !");
            } else {
                break; // Not enough space for the next one, stop (strict FIFO)
            }
        }
    }

    private void updateStatus(int id, Statut s) {
        String sql = "UPDATE reservation SET statut=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, s.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println(e.getMessage()); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QUERIES
    // ─────────────────────────────────────────────────────────────────────────

    /** All reservations for an event, ordered by request date (FIFO). */
    public List<Reservation> getByEvent(int eventId) {
        return query("SELECT * FROM reservation WHERE event_id=? ORDER BY date_demande ASC", eventId);
    }

    /** All reservations by a specific participant. */
    public List<Reservation> getByParticipant(int participantId) {
        return query("SELECT * FROM reservation WHERE participant_id=? ORDER BY date_demande DESC", participantId);
    }

    /** All reservations (admin view), FIFO order. */
    public List<Reservation> getAll() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation ORDER BY date_demande ASC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException ex) {
            System.err.println("❌ getAll reservations : " + ex.getMessage());
        }
        return list;
    }

    /** Sum of confirmed places for an event (used for capacity guard). */
    public int getPlacesConfirmees(int eventId) {
        String sql = "SELECT COALESCE(SUM(nombre_places),0) FROM reservation WHERE event_id=? AND statut='CONFIRME'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            System.err.println("❌ getPlacesConfirmees : " + ex.getMessage());
        }
        return 0;
    }

    /** Places restantes = capaciteMax − places confirmées. */
    public int getPlacesRestantes(int eventId) {
        event ev = eServ.getById(eventId);
        if (ev == null || ev.getCapaciteMax() <= 0) return Integer.MAX_VALUE;
        return ev.getCapaciteMax() - getPlacesConfirmees(eventId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private boolean hasActiveReservation(int eventId, int participantId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE event_id=? AND participant_id=? AND statut IN ('CONFIRME', 'LISTE_D_ATTENTE', 'CHECKED_IN')";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, participantId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException ex) {
            System.err.println("❌ hasActiveReservation : " + ex.getMessage());
        }
        return false;
    }

    private void persister(Reservation res) {
        String sql = "INSERT INTO reservation (event_id, participant_id, nombre_places, statut, date_demande, motif_refus) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, res.getEventId());
            ps.setInt(2, res.getParticipantId());
            ps.setInt(3, res.getNombrePlaces());
            ps.setString(4, res.getStatut().name());
            ps.setTimestamp(5, Timestamp.valueOf(
                res.getDateDemande() != null ? res.getDateDemande() : LocalDateTime.now()));
            ps.setString(6, res.getMotifRefus());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) res.setId(keys.getInt(1));
        } catch (SQLException ex) {
            System.err.println("❌ persister reservation : " + ex.getMessage());
        }
    }

    private List<Reservation> query(String sql, int param) {
        List<Reservation> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException ex) {
            System.err.println("❌ query reservation : " + ex.getMessage());
        }
        return list;
    }

    private Reservation map(ResultSet rs) throws SQLException {
        String statutStr = rs.getString("statut");
        Statut s;
        try { s = Statut.valueOf(statutStr); } catch (Exception e) { s = Statut.EN_ATTENTE; }

        Timestamp ts = rs.getTimestamp("date_demande");
        LocalDateTime dateDemande = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        return new Reservation(
            rs.getInt("id"),
            rs.getInt("event_id"),
            rs.getInt("participant_id"),
            rs.getInt("nombre_places"),
            s,
            dateDemande,
            rs.getString("motif_refus")
        );
    }
}
