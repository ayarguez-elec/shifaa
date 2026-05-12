package services;

import entities.Participant;
import entities.Reservation;
import entities.Reservation.Statut;
import entities.event;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  NotificationService — API 2 : Orchestrateur QR + Email           ║
 * ╠══════════════════════════════════════════════════════════════════════╣
 * ║  Triggered automatically after each booking decision.             ║
 * ║  CONFIRME → generate QR code → send confirmation email            ║
 * ║  REFUSE   → send refusal email (no QR code)                       ║
 * ║  All operations run asynchronously to not block the caller.       ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
public class NotificationService {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final QrCodeService    qrService    = new QrCodeService();
    private final EmailService     emailService = new EmailService();
    private final eventServ        eServ        = new eventServ();
    private final Participantserv  pServ        = new Participantserv();
    private final Connection       cnx          = MyDataBase.getInstance().getConnection();

    // Single background thread — notifications must not slow down the booking response
    private static final ExecutorService EXECUTOR =
        Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Notification-Thread");
            t.setDaemon(true);
            return t;
        });

    // ── Main entry point (called by ReservationServ) ──────────────────────────

    /**
     * Processes notifications for a reservation asynchronously.
     * Returns immediately; the actual work runs in a background thread.
     */
    public void notifierAsync(Reservation res) {
        EXECUTOR.submit(() -> {
            try {
                notifier(res);
            } catch (Exception e) {
                System.err.println("❌ [Notification] Erreur inattendue : " + e.getMessage());
            }
        });
    }

    // ── Synchronous notification (for direct use / testing) ───────────────────
    public void notifier(Reservation res) {
        event       ev   = eServ.getById(res.getEventId());
        Participant part = pServ.getById(res.getParticipantId());

        if (ev == null || part == null) {
            System.err.println("❌ [Notification] Événement ou participant introuvable.");
            return;
        }

        String partNom   = part.getNom() + " " + part.getPrenom();
        String partEmail = part.getEmail();
        String eventDate = ev.getDate() != null ? ev.getDate().toString() : "Date à confirmer";
        String eventLieu = ev.getLieu() != null ? ev.getLieu() : "—";
        String demande   = res.getDateDemande() != null ? res.getDateDemande().format(FMT) : "—";

        if (res.getStatut() == Statut.CONFIRME) {
            handleConfirmed(res, ev, partNom, partEmail, eventDate, eventLieu, demande);
        } else if (res.getStatut() == Statut.REFUSE) {
            handleRefused(res, ev, partNom, partEmail);
        }
    }

    // ── CONFIRMED: QR Code + Email ────────────────────────────────────────────
    private void handleConfirmed(Reservation res, event ev, String partNom,
                                  String partEmail, String eventDate,
                                  String eventLieu, String demande) {
        String qrPath = null;

        // 1. Generate QR code
        try {
            qrPath = qrService.generateQrCode(
                res.getId(), ev.getId(), ev.getTitre(),
                partNom, partEmail, res.getNombrePlaces(), demande);

            // 2. Persist QR code path in DB
            saveQrPath(res.getId(), qrPath);

        } catch (Exception e) {
            System.err.println("❌ [QR] Génération échouée : " + e.getMessage());
        }

        // 3. Send confirmation email (even if QR failed — email still sent without attachment)
        boolean sent = emailService.sendConfirmation(
            partEmail, partNom, ev.getTitre(), eventDate,
            eventLieu, res.getNombrePlaces(), res.getId(), qrPath);

        markEmailSent(res.getId(), sent);
        System.out.printf("📬 [Notification] Confirmation → %s | QR: %s | Email: %s%n",
            partEmail, qrPath != null ? "✅" : "❌", sent ? "✅" : "❌");
    }

    // ── REFUSED: Email only ───────────────────────────────────────────────────
    private void handleRefused(Reservation res, event ev, String partNom, String partEmail) {
        String motif = res.getMotifRefus() != null ? res.getMotifRefus() : "Capacité atteinte.";
        boolean sent = emailService.sendRefus(partEmail, partNom, ev.getTitre(), motif);
        markEmailSent(res.getId(), sent);
        System.out.printf("📬 [Notification] Refus → %s | Email: %s%n",
            partEmail, sent ? "✅" : "❌");
    }

    // ── DB helpers ────────────────────────────────────────────────────────────

    private void saveQrPath(int reservationId, String path) {
        String sql = "UPDATE reservation SET qr_code_path=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, path);
            ps.setInt(2, reservationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ [Notification] saveQrPath : " + e.getMessage());
        }
    }

    private void markEmailSent(int reservationId, boolean sent) {
        // Try with email_date first; fall back silently if column doesn't exist yet
        String sqlFull    = "UPDATE reservation SET email_sent=?, email_date=? WHERE id=?";
        String sqlCompact = "UPDATE reservation SET email_sent=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sqlFull)) {
            ps.setBoolean(1, sent);
            ps.setTimestamp(2, sent ? Timestamp.valueOf(LocalDateTime.now()) : null);
            ps.setInt(3, reservationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("email_date")) {
                // Column not yet added — run migration_reservation.sql to fix
                try (PreparedStatement ps2 = cnx.prepareStatement(sqlCompact)) {
                    ps2.setBoolean(1, sent);
                    ps2.setInt(2, reservationId);
                    ps2.executeUpdate();
                    System.out.println("⚠️ [Notification] email_date manquant — exécutez migration_reservation.sql");
                } catch (SQLException ex2) {
                    System.err.println("❌ [Notification] markEmailSent : " + ex2.getMessage());
                }
            } else {
                System.err.println("❌ [Notification] markEmailSent : " + e.getMessage());
            }
        }
    }

    // ── Resend notification for an existing reservation ───────────────────────
    public void resendNotification(Reservation res) {
        notifier(res); // synchronous for manual resend
    }
}
