-- ═══════════════════════════════════════════════════════════════════════
-- SHIFAA – Migration : Système de Fidélité & Gestion Intelligente
-- Exécuter dans : sante_db
-- ═══════════════════════════════════════════════════════════════════════

-- ─────────────────────────────────────────────────────────────────────
-- 1. Colonnes fidélité sur la table participant
-- ─────────────────────────────────────────────────────────────────────
ALTER TABLE participant
    ADD COLUMN IF NOT EXISTS loyalty_points      INT         NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS priority_level      ENUM('LOW','MEDIUM','HIGH','VIP') NOT NULL DEFAULT 'LOW',
    ADD COLUMN IF NOT EXISTS badge               ENUM('Bronze','Silver','Gold')    NOT NULL DEFAULT 'Bronze',
    ADD COLUMN IF NOT EXISTS attendance_count    INT         NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS cancellation_count  INT         NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_activity       DATETIME    DEFAULT NULL;

-- ─────────────────────────────────────────────────────────────────────
-- 2. Table historique des notifications participant
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS participant_notifications (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    participant_id  INT          NOT NULL,
    type            VARCHAR(50)  NOT NULL COMMENT 'CONFIRMATION, REFUS, WAITLIST_PROMO, REMINDER',
    sujet           VARCHAR(255) NOT NULL,
    message         TEXT         NOT NULL,
    sent_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read         TINYINT(1)   NOT NULL DEFAULT 0,
    channel         VARCHAR(20)  NOT NULL DEFAULT 'EMAIL' COMMENT 'EMAIL, IN_APP',
    CONSTRAINT fk_notif_part FOREIGN KEY (participant_id)
        REFERENCES participant(id) ON DELETE CASCADE,
    INDEX idx_notif_part (participant_id),
    INDEX idx_notif_unread (participant_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─────────────────────────────────────────────────────────────────────
-- 3. Vue matérialisée (view) : profil complet participant avec stats
-- ─────────────────────────────────────────────────────────────────────
CREATE OR REPLACE VIEW v_participant_profile AS
SELECT
    p.id,
    p.nom,
    p.prenom,
    p.email,
    p.telephone,
    p.loyalty_points,
    p.priority_level,
    p.badge,
    p.attendance_count,
    p.cancellation_count,
    p.last_activity,
    -- Réservations calculées en temps réel
    COALESCE(stats.total_resa,     0) AS total_reservations,
    COALESCE(stats.nb_confirme,    0) AS nb_confirme,
    COALESCE(stats.nb_refuse,      0) AS nb_refuse,
    COALESCE(stats.nb_annule,      0) AS nb_annule,
    COALESCE(stats.nb_attente,     0) AS nb_attente,
    COALESCE(stats.nb_checkin,     0) AS nb_checkin,
    COALESCE(stats.total_places,   0) AS total_places_reservees,
    -- Taux de présence
    CASE
        WHEN COALESCE(stats.nb_confirme, 0) = 0 THEN 0
        ELSE ROUND(COALESCE(stats.nb_checkin, 0) * 100.0 / stats.nb_confirme, 1)
    END AS taux_presence
FROM participant p
LEFT JOIN (
    SELECT
        participant_id,
        COUNT(*)                                                  AS total_resa,
        SUM(statut IN ('CONFIRME','CHECKED_IN'))                  AS nb_confirme,
        SUM(statut = 'REFUSE')                                    AS nb_refuse,
        SUM(statut = 'ANNULE')                                    AS nb_annule,
        SUM(statut = 'LISTE_D_ATTENTE')                           AS nb_attente,
        SUM(statut = 'CHECKED_IN')                                AS nb_checkin,
        SUM(nombre_places)                                        AS total_places
    FROM reservation
    GROUP BY participant_id
) stats ON p.id = stats.participant_id;

-- ─────────────────────────────────────────────────────────────────────
-- 4. Recalculer les scores fidélité pour les participants existants
-- ─────────────────────────────────────────────────────────────────────
UPDATE participant p
JOIN (
    SELECT
        participant_id,
        GREATEST(0,
            SUM(CASE WHEN statut IN ('CONFIRME','CHECKED_IN') THEN 10 ELSE 0 END) +
            SUM(CASE WHEN statut = 'CHECKED_IN'               THEN 20 ELSE 0 END) -
            SUM(CASE WHEN statut = 'ANNULE'                   THEN  5 ELSE 0 END)
        ) AS score
    FROM reservation
    GROUP BY participant_id
) r ON p.id = r.participant_id
SET p.loyalty_points = r.score,
    p.badge = CASE
        WHEN r.score >= 150 THEN 'Gold'
        WHEN r.score >= 50  THEN 'Silver'
        ELSE 'Bronze'
    END,
    p.priority_level = CASE
        WHEN r.score >= 150 THEN 'VIP'
        WHEN r.score >= 100 THEN 'HIGH'
        WHEN r.score >= 30  THEN 'MEDIUM'
        ELSE 'LOW'
    END;

-- ─────────────────────────────────────────────────────────────────────
-- 5. Mettre à jour attendance_count et cancellation_count
-- ─────────────────────────────────────────────────────────────────────
UPDATE participant p
JOIN (
    SELECT
        participant_id,
        SUM(statut = 'CHECKED_IN') AS nb_att,
        SUM(statut = 'ANNULE')     AS nb_ann
    FROM reservation
    GROUP BY participant_id
) r ON p.id = r.participant_id
SET p.attendance_count   = r.nb_att,
    p.cancellation_count = r.nb_ann;

-- ─────────────────────────────────────────────────────────────────────
-- 6. Vérification
-- ─────────────────────────────────────────────────────────────────────
SELECT 'Migration fidélité terminée ✅' AS status;
SELECT priority_level, COUNT(*) AS nb FROM participant GROUP BY priority_level;
SELECT badge, COUNT(*) AS nb FROM participant GROUP BY badge;
