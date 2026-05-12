-- ═══════════════════════════════════════════════════════════════════════
-- Migration SQL – Réservation + Notifications (QR Code & Email)
-- À exécuter dans votre base de données : sante_db
-- ═══════════════════════════════════════════════════════════════════════

-- 1. Capacité max sur la table event (si pas déjà fait depuis Métier 1)
ALTER TABLE event
    ADD COLUMN IF NOT EXISTS capacite_max INT DEFAULT 0;

-- 2. Table des réservations (avec colonnes de notification)
CREATE TABLE IF NOT EXISTS reservation (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    event_id       INT NOT NULL,
    participant_id INT NOT NULL,
    nombre_places  INT NOT NULL DEFAULT 1,
    statut         ENUM('EN_ATTENTE','CONFIRME','REFUSE','ANNULE') NOT NULL DEFAULT 'EN_ATTENTE',
    date_demande   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motif_refus    TEXT,
    qr_code_path   VARCHAR(512) DEFAULT NULL,
    email_sent     TINYINT(1)   NOT NULL DEFAULT 0,
    CONSTRAINT fk_res_event FOREIGN KEY (event_id)       REFERENCES event(id)       ON DELETE CASCADE,
    CONSTRAINT fk_res_part  FOREIGN KEY (participant_id) REFERENCES participant(id) ON DELETE CASCADE,
    INDEX idx_res_event  (event_id),
    INDEX idx_res_part   (participant_id),
    INDEX idx_res_statut (statut),
    INDEX idx_res_fifo   (event_id, statut, date_demande)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Si la table existe déjà sans les colonnes notification, les ajouter :
ALTER TABLE reservation
    ADD COLUMN IF NOT EXISTS qr_code_path VARCHAR(512)  DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS email_sent   TINYINT(1)    NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS email_date   DATETIME      DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS date_checkin DATETIME      DEFAULT NULL;

-- 4. Mettre à jour l'ENUM pour inclure liste d'attente et check-in
ALTER TABLE reservation
    MODIFY COLUMN statut ENUM('EN_ATTENTE','CONFIRME','REFUSE','ANNULE','LISTE_D_ATTENTE','CHECKED_IN')
    NOT NULL DEFAULT 'EN_ATTENTE';

-- 5. Index pour la file d'attente FIFO et les historiques email
CREATE INDEX IF NOT EXISTS idx_res_waitlist ON reservation (event_id, statut, date_demande);
CREATE INDEX IF NOT EXISTS idx_res_email    ON reservation (email_sent, email_date);

-- ═══════════════════════════════════════════════════════════════════════
SELECT 'Migration terminée ✅' AS status;
DESCRIBE reservation;
