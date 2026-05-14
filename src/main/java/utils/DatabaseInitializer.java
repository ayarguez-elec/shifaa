package utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void init() {
        Connection cnx = MyDataBase.getInstance().getConnection();
        if (cnx == null) {
            System.err.println("Connexion nulle");
            return;
        }
        try (Statement st = cnx.createStatement()) {

            runSafe(st, "CREATE TABLE IF NOT EXISTS `event` (`id` INT NOT NULL AUTO_INCREMENT,`titre` VARCHAR(100) NOT NULL,`type` VARCHAR(100) NOT NULL,`description` TEXT NOT NULL,`date` DATE DEFAULT NULL,`time` VARCHAR(20) DEFAULT NULL,`lieu` VARCHAR(100) DEFAULT NULL,`status` VARCHAR(50) NOT NULL DEFAULT 'Planifie',`image_path` VARCHAR(255) DEFAULT NULL,`capacite_max` INT NOT NULL DEFAULT 50, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            runSafe(st, "CREATE TABLE IF NOT EXISTS `participant` (`id` INT NOT NULL AUTO_INCREMENT,`nom` VARCHAR(100) NOT NULL,`prenom` VARCHAR(100) NOT NULL,`email` VARCHAR(255) NOT NULL UNIQUE,`telephone` VARCHAR(20) DEFAULT NULL,`loyalty_points` INT NOT NULL DEFAULT 0,`priority_level` ENUM('LOW','MEDIUM','HIGH','VIP') NOT NULL DEFAULT 'LOW',`badge` ENUM('Bronze','Silver','Gold') NOT NULL DEFAULT 'Bronze',`attendance_count` INT NOT NULL DEFAULT 0,`cancellation_count` INT NOT NULL DEFAULT 0,`last_activity` DATETIME DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            runSafe(st, "CREATE TABLE IF NOT EXISTS `event_participant` (`event_id` INT NOT NULL,`participant_id` INT NOT NULL, PRIMARY KEY (`event_id`,`participant_id`), CONSTRAINT `fk_ep_event` FOREIGN KEY (`event_id`) REFERENCES `event`(`id`) ON DELETE CASCADE, CONSTRAINT `fk_ep_participant` FOREIGN KEY (`participant_id`) REFERENCES `participant`(`id`) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            runSafe(st, "CREATE TABLE IF NOT EXISTS `reservation` (`id` INT NOT NULL AUTO_INCREMENT,`event_id` INT NOT NULL,`participant_id` INT NOT NULL,`nombre_places` INT NOT NULL DEFAULT 1,`statut` ENUM('EN_ATTENTE','CONFIRME','REFUSE','ANNULE','LISTE_D_ATTENTE','CHECKED_IN') NOT NULL DEFAULT 'EN_ATTENTE',`date_demande` DATETIME DEFAULT CURRENT_TIMESTAMP,`motif_refus` TEXT DEFAULT NULL,`qr_code_path` VARCHAR(512) DEFAULT NULL,`email_sent` TINYINT(1) NOT NULL DEFAULT 0,`email_date` DATETIME DEFAULT NULL,`date_checkin` DATETIME DEFAULT NULL,`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`), CONSTRAINT `fk_res_event` FOREIGN KEY (`event_id`) REFERENCES `event`(`id`) ON DELETE CASCADE, CONSTRAINT `fk_res_part` FOREIGN KEY (`participant_id`) REFERENCES `participant`(`id`) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            runSafe(st, "CREATE TABLE IF NOT EXISTS `participant_notifications` (`id` INT NOT NULL AUTO_INCREMENT,`participant_id` INT NOT NULL,`type` VARCHAR(50) NOT NULL,`sujet` VARCHAR(255) NOT NULL,`message` TEXT NOT NULL,`sent_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,`is_read` TINYINT(1) NOT NULL DEFAULT 0,`channel` VARCHAR(20) NOT NULL DEFAULT 'EMAIL', PRIMARY KEY (`id`), CONSTRAINT `fk_pn_part` FOREIGN KEY (`participant_id`) REFERENCES `participant`(`id`) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            runSafe(st, "CREATE TABLE IF NOT EXISTS `recommendation_history` (`id` INT NOT NULL AUTO_INCREMENT,`participant_id` INT NOT NULL,`event_id` INT NOT NULL,`score` DOUBLE NOT NULL DEFAULT 0,`reason` VARCHAR(500) DEFAULT NULL,`recommended_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,`was_clicked` TINYINT(1) NOT NULL DEFAULT 0,`was_reserved` TINYINT(1) NOT NULL DEFAULT 0, PRIMARY KEY (`id`), CONSTRAINT `fk_rh_part` FOREIGN KEY (`participant_id`) REFERENCES `participant`(`id`) ON DELETE CASCADE, CONSTRAINT `fk_rh_event` FOREIGN KEY (`event_id`) REFERENCES `event`(`id`) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            runSafe(st, "CREATE TABLE IF NOT EXISTS `participant_preferences` (`id` INT NOT NULL AUTO_INCREMENT,`participant_id` INT NOT NULL UNIQUE,`favorite_category` VARCHAR(100) DEFAULT NULL,`second_category` VARCHAR(100) DEFAULT NULL,`interaction_count` INT NOT NULL DEFAULT 0,`avg_participation_rate` DOUBLE NOT NULL DEFAULT 0,`last_computed` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`), CONSTRAINT `fk_pp_part` FOREIGN KEY (`participant_id`) REFERENCES `participant`(`id`) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            runSafe(st, "CREATE TABLE IF NOT EXISTS `event_popularity` (`id` INT NOT NULL AUTO_INCREMENT,`event_id` INT NOT NULL UNIQUE,`popularity_score` DOUBLE NOT NULL DEFAULT 0,`click_count` INT NOT NULL DEFAULT 0,`reservation_count` INT NOT NULL DEFAULT 0,`view_count` INT NOT NULL DEFAULT 0,`last_updated` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`), CONSTRAINT `fk_epop_event` FOREIGN KEY (`event_id`) REFERENCES `event`(`id`) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            runSafe(st, "CREATE TABLE IF NOT EXISTS `recommendation_statistics` (`id` INT NOT NULL AUTO_INCREMENT,`stat_date` DATE NOT NULL UNIQUE,`total_recommendations` INT NOT NULL DEFAULT 0,`total_clicks` INT NOT NULL DEFAULT 0,`total_conversions` INT NOT NULL DEFAULT 0,`top_category` VARCHAR(100) DEFAULT NULL,`avg_score` DOUBLE NOT NULL DEFAULT 0,`computed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            runSafe(st, "CREATE TABLE IF NOT EXISTS `reclamation_reponse` (`responseId` INT NOT NULL AUTO_INCREMENT,`reclamationId` INT NOT NULL,`content` TEXT NOT NULL,`responseDate` DATETIME DEFAULT CURRENT_TIMESTAMP,`adminId` INT NOT NULL, PRIMARY KEY (`responseId`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            runSafe(st, "CREATE TABLE IF NOT EXISTS `escalation` (`escalationId` INT NOT NULL AUTO_INCREMENT,`reclamationId` INT NOT NULL,`userId` INT NOT NULL,`reason` TEXT NOT NULL,`status` VARCHAR(50) DEFAULT 'En attente',`createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,`resolvedAt` DATETIME DEFAULT NULL, PRIMARY KEY (`escalationId`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            runSafe(st, "CREATE TABLE IF NOT EXISTS `reclamation_notification` (`notificationId` INT NOT NULL AUTO_INCREMENT,`reclamationId` INT NOT NULL,`userId` INT NOT NULL,`message` TEXT NOT NULL,`createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,`isRead` BOOLEAN DEFAULT FALSE, PRIMARY KEY (`notificationId`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            runSafe(st, "ALTER TABLE `participant` ADD COLUMN IF NOT EXISTS `loyalty_points` INT NOT NULL DEFAULT 0");
            runSafe(st, "ALTER TABLE `participant` ADD COLUMN IF NOT EXISTS `attendance_count` INT NOT NULL DEFAULT 0");
            runSafe(st, "ALTER TABLE `participant` ADD COLUMN IF NOT EXISTS `cancellation_count` INT NOT NULL DEFAULT 0");
            runSafe(st, "ALTER TABLE `participant` ADD COLUMN IF NOT EXISTS `last_activity` DATETIME DEFAULT NULL");
            runSafe(st, "ALTER TABLE `participant` ADD COLUMN IF NOT EXISTS `priority_level` ENUM('LOW','MEDIUM','HIGH','VIP') NOT NULL DEFAULT 'LOW'");
            runSafe(st, "ALTER TABLE `participant` ADD COLUMN IF NOT EXISTS `badge` ENUM('Bronze','Silver','Gold') NOT NULL DEFAULT 'Bronze'");
            runSafe(st, "ALTER TABLE `reservation` ADD COLUMN IF NOT EXISTS `qr_code_path` VARCHAR(512) DEFAULT NULL");
            runSafe(st, "ALTER TABLE `reservation` ADD COLUMN IF NOT EXISTS `email_sent` TINYINT(1) NOT NULL DEFAULT 0");
            runSafe(st, "ALTER TABLE `reservation` ADD COLUMN IF NOT EXISTS `email_date` DATETIME DEFAULT NULL");
            runSafe(st, "ALTER TABLE `reservation` ADD COLUMN IF NOT EXISTS `date_checkin` DATETIME DEFAULT NULL");
            runSafe(st, "ALTER TABLE `reservation` ADD COLUMN IF NOT EXISTS `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
            runSafe(st, "ALTER TABLE `event` ADD COLUMN IF NOT EXISTS `image_path` VARCHAR(255) DEFAULT NULL");
            runSafe(st, "ALTER TABLE `event` ADD COLUMN IF NOT EXISTS `capacite_max` INT NOT NULL DEFAULT 50");
            
            // Fix reclamation & escalation schemas
            runSafe(st, "ALTER TABLE `reclamation` ADD COLUMN `isEscalated` BOOLEAN DEFAULT FALSE");
            runSafe(st, "ALTER TABLE `escalation` ADD COLUMN `userId` INT NOT NULL DEFAULT 0");
            runSafe(st, "ALTER TABLE `escalation` ADD COLUMN `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP");
            runSafe(st, "ALTER TABLE `escalation` ADD COLUMN `resolvedAt` DATETIME DEFAULT NULL");


            System.out.println("Schema shefaa verifie et a jour.");

        } catch (SQLException e) {
            System.err.println("DB-Init erreur: " + e.getMessage());
        }
        EventImageGenerator.generateAll();
    }

    private static void runSafe(Statement st, String sql) {
        try {
            st.execute(sql);
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Duplicate column") || msg.contains("already exists"))) return;
            System.err.println("DB-Init warning: " + e.getMessage());
        }
    }
}
