package utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Crée automatiquement les tables et colonnes manquantes au démarrage.
 * Aucune intervention manuelle nécessaire.
 */
public class DatabaseInitializer {

    public static void init() {
        Connection cnx = MyDataBase.getInstance().getConnection();
        try (Statement st = cnx.createStatement()) {

            // ── Colonnes fidélité sur participant ─────────────────────────────
            runSafe(st, "ALTER TABLE participant ADD COLUMN IF NOT EXISTS loyalty_points     INT  NOT NULL DEFAULT 0");
            runSafe(st, "ALTER TABLE participant ADD COLUMN IF NOT EXISTS attendance_count   INT  NOT NULL DEFAULT 0");
            runSafe(st, "ALTER TABLE participant ADD COLUMN IF NOT EXISTS cancellation_count INT  NOT NULL DEFAULT 0");
            runSafe(st, "ALTER TABLE participant ADD COLUMN IF NOT EXISTS last_activity      DATETIME DEFAULT NULL");

            // ENUM : essayer d'ajouter la colonne avec ENUM complet
            runSafe(st,
                "ALTER TABLE participant ADD COLUMN IF NOT EXISTS priority_level " +
                "ENUM('LOW','MEDIUM','HIGH','VIP') NOT NULL DEFAULT 'LOW'");
            runSafe(st,
                "ALTER TABLE participant ADD COLUMN IF NOT EXISTS badge " +
                "ENUM('Bronze','Silver','Gold') NOT NULL DEFAULT 'Bronze'");

            // ── Colonnes email/qr sur reservation ────────────────────────────
            runSafe(st, "ALTER TABLE reservation ADD COLUMN IF NOT EXISTS qr_code_path  VARCHAR(512) DEFAULT NULL");
            runSafe(st, "ALTER TABLE reservation ADD COLUMN IF NOT EXISTS email_sent    TINYINT(1)   NOT NULL DEFAULT 0");
            runSafe(st, "ALTER TABLE reservation ADD COLUMN IF NOT EXISTS email_date    DATETIME DEFAULT NULL");
            runSafe(st, "ALTER TABLE reservation ADD COLUMN IF NOT EXISTS date_checkin  DATETIME DEFAULT NULL");

            // ENUM reservation : ajouter LISTE_D_ATTENTE et CHECKED_IN si absent
            runSafe(st,
                "ALTER TABLE reservation MODIFY COLUMN statut " +
                "ENUM('EN_ATTENTE','CONFIRME','REFUSE','ANNULE','LISTE_D_ATTENTE','CHECKED_IN') " +
                "NOT NULL DEFAULT 'EN_ATTENTE'");

            // ── Table notifications participant ───────────────────────────────
            runSafe(st,
                "CREATE TABLE IF NOT EXISTS participant_notifications (" +
                "  id             INT AUTO_INCREMENT PRIMARY KEY," +
                "  participant_id INT          NOT NULL," +
                "  type           VARCHAR(50)  NOT NULL," +
                "  sujet          VARCHAR(255) NOT NULL," +
                "  message        TEXT         NOT NULL," +
                "  sent_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  is_read        TINYINT(1)   NOT NULL DEFAULT 0," +
                "  channel        VARCHAR(20)  NOT NULL DEFAULT 'EMAIL'," +
                "  CONSTRAINT fk_notif_part FOREIGN KEY (participant_id)" +
                "      REFERENCES participant(id) ON DELETE CASCADE," +
                "  INDEX idx_notif_part   (participant_id)," +
                "  INDEX idx_notif_unread (participant_id, is_read)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // ── image_path sur event ──────────────────────────────────────────
            runSafe(st, "ALTER TABLE event ADD COLUMN IF NOT EXISTS image_path VARCHAR(255) DEFAULT NULL");
            runSafe(st, "ALTER TABLE event ADD COLUMN IF NOT EXISTS capacite_max INT NOT NULL DEFAULT 50");

            // ── created_at sur reservation (pour trending) ────────────────────
            runSafe(st, "ALTER TABLE reservation ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");

            // ── Tables du système de recommandation ───────────────────────────
            runSafe(st,
                "CREATE TABLE IF NOT EXISTS recommendation_history (" +
                "  id             INT AUTO_INCREMENT PRIMARY KEY," +
                "  participant_id INT    NOT NULL," +
                "  event_id       INT    NOT NULL," +
                "  score          DOUBLE NOT NULL DEFAULT 0," +
                "  reason         VARCHAR(500)," +
                "  recommended_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  was_clicked    TINYINT(1) NOT NULL DEFAULT 0," +
                "  was_reserved   TINYINT(1) NOT NULL DEFAULT 0," +
                "  CONSTRAINT fk_rh_part  FOREIGN KEY (participant_id) REFERENCES participant(id) ON DELETE CASCADE," +
                "  CONSTRAINT fk_rh_event FOREIGN KEY (event_id)       REFERENCES event(id)       ON DELETE CASCADE," +
                "  INDEX idx_rh_part  (participant_id)," +
                "  INDEX idx_rh_event (event_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            runSafe(st,
                "CREATE TABLE IF NOT EXISTS participant_preferences (" +
                "  id                    INT AUTO_INCREMENT PRIMARY KEY," +
                "  participant_id        INT         NOT NULL UNIQUE," +
                "  favorite_category     VARCHAR(100)," +
                "  second_category       VARCHAR(100)," +
                "  interaction_count     INT         NOT NULL DEFAULT 0," +
                "  avg_participation_rate DOUBLE     NOT NULL DEFAULT 0," +
                "  last_computed         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  CONSTRAINT fk_pp_part FOREIGN KEY (participant_id) REFERENCES participant(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            runSafe(st,
                "CREATE TABLE IF NOT EXISTS event_popularity (" +
                "  id               INT AUTO_INCREMENT PRIMARY KEY," +
                "  event_id         INT    NOT NULL UNIQUE," +
                "  popularity_score DOUBLE NOT NULL DEFAULT 0," +
                "  click_count      INT    NOT NULL DEFAULT 0," +
                "  reservation_count INT   NOT NULL DEFAULT 0," +
                "  view_count       INT    NOT NULL DEFAULT 0," +
                "  last_updated     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  CONSTRAINT fk_ep_event FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            runSafe(st,
                "CREATE TABLE IF NOT EXISTS recommendation_statistics (" +
                "  id                   INT AUTO_INCREMENT PRIMARY KEY," +
                "  stat_date            DATE NOT NULL UNIQUE," +
                "  total_recommendations INT NOT NULL DEFAULT 0," +
                "  total_clicks         INT NOT NULL DEFAULT 0," +
                "  total_conversions    INT NOT NULL DEFAULT 0," +
                "  top_category         VARCHAR(100)," +
                "  avg_score            DOUBLE NOT NULL DEFAULT 0," +
                "  computed_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            System.out.println("✅ [DB] Schéma vérifié et à jour.");

        } catch (SQLException e) {
            System.err.println("❌ [DB] Initialisation : " + e.getMessage());
        }

        // Generate event images after schema is ready
        EventImageGenerator.generateAll();
    }

    private static void runSafe(Statement st, String sql) {
        try {
            st.execute(sql);
        } catch (SQLException e) {
            // Ignorer les erreurs bénignes (colonne déjà existante, etc.)
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Duplicate column") || msg.contains("already exists"))) {
                return; // Normal — colonne déjà présente
            }
            System.err.println("⚠️ [DB] " + e.getMessage());
        }
    }
}
