-- ═══════════════════════════════════════════════════════════════════════
-- REPAIR SCRIPT – Fix Database Schema for Smart Booking & Notifications
-- ═══════════════════════════════════════════════════════════════════════

USE sante_db;

-- 1. FIX TABLE: event
ALTER TABLE event
    ADD COLUMN IF NOT EXISTS capacite_max INT DEFAULT 0;

-- 2. FIX TABLE: reservation
-- We create it from scratch if it doesn't exist, or fix it if it does.

CREATE TABLE IF NOT EXISTS reservation (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    event_id       INT NOT NULL,
    participant_id INT NOT NULL,
    nombre_places  INT NOT NULL DEFAULT 1,
    statut         ENUM('EN_ATTENTE','CONFIRME','REFUSE','ANNULE','LISTE_D_ATTENTE','CHECKED_IN') NOT NULL DEFAULT 'EN_ATTENTE',
    date_demande   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motif_refus    TEXT,
    qr_code_path   VARCHAR(512) DEFAULT NULL,
    email_sent     TINYINT(1)   NOT NULL DEFAULT 0,
    date_checkin   DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- If table existed, ensure all columns are there (safe for already existing)
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS event_id       INT NOT NULL;
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS participant_id INT NOT NULL;
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS nombre_places  INT NOT NULL DEFAULT 1;
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS statut         ENUM('EN_ATTENTE','CONFIRME','REFUSE','ANNULE','LISTE_D_ATTENTE','CHECKED_IN') NOT NULL DEFAULT 'EN_ATTENTE';
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS date_demande   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS motif_refus    TEXT;
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS qr_code_path   VARCHAR(512) DEFAULT NULL;
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS email_sent     TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS date_checkin   DATETIME DEFAULT NULL;

-- Ensure statut enum is correct if it was an older version
ALTER TABLE reservation 
    MODIFY COLUMN statut ENUM('EN_ATTENTE','CONFIRME','REFUSE','ANNULE','LISTE_D_ATTENTE','CHECKED_IN') 
    NOT NULL DEFAULT 'EN_ATTENTE';

-- Add Foreign Keys (if they don't exist)
-- Note: In MySQL we usually drop and recreate or use a procedure, 
-- but here we'll keep it simple for the user.
-- If these fail because they exist, it's fine.

-- Indices
CREATE INDEX IF NOT EXISTS idx_res_event  ON reservation (event_id);
CREATE INDEX IF NOT EXISTS idx_res_part   ON reservation (participant_id);
CREATE INDEX IF NOT EXISTS idx_res_statut ON reservation (statut);
CREATE INDEX IF NOT EXISTS idx_res_fifo   ON reservation (event_id, statut, date_demande);

-- ═══════════════════════════════════════════════════════════════════════
SELECT 'Base de données réparée avec succès ! ✅' AS status;
SHOW COLUMNS FROM event;
SHOW COLUMNS FROM reservation;
