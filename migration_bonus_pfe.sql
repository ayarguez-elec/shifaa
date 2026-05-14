-- ═══════════════════════════════════════════════════════════════════════
-- Migration SQL – Fonctionnalités Avancées (PFE Bonus)
-- ═══════════════════════════════════════════════════════════════════════

-- 1. Mise à jour des statuts ENUM pour inclure la liste d'attente et le check-in
-- Note: MySQL ne supporte pas ADD VALUE sur les ENUM existants de façon simple, 
-- on re-définit donc la colonne.
ALTER TABLE reservation 
    MODIFY COLUMN statut ENUM('EN_ATTENTE','CONFIRME','REFUSE','ANNULE','LISTE_D_ATTENTE','CHECKED_IN') 
    NOT NULL DEFAULT 'EN_ATTENTE';

-- 2. Colonne pour la date de check-in
ALTER TABLE reservation
    ADD COLUMN IF NOT EXISTS date_checkin DATETIME DEFAULT NULL;

-- 3. Index pour optimiser la file d'attente (FIFO par événement)
CREATE INDEX IF NOT EXISTS idx_res_waitlist ON reservation (event_id, statut, date_demande);

-- ═══════════════════════════════════════════════════════════════════════
SELECT 'Migration Bonus PFE terminée ✅' AS status;
