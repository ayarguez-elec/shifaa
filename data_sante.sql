-- ═══════════════════════════════════════════════════════════════════════════
-- SHIFAA – Données de démo : 10 Événements Santé + 20 Participants
-- Base de données : sante_db
-- ═══════════════════════════════════════════════════════════════════════════

-- ═════════════════════════════════════
-- ÉTAPE 1 : Mise à jour structure
-- ═════════════════════════════════════

-- Capacité maximale par défaut = 50
ALTER TABLE event MODIFY COLUMN capacite_max INT NOT NULL DEFAULT 50;

-- Colonne image_path si pas encore créée
ALTER TABLE event ADD COLUMN IF NOT EXISTS image_path VARCHAR(255) DEFAULT NULL;

-- Colonnes email dans reservation
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS email_date DATETIME DEFAULT NULL;

-- ═════════════════════════════════════
-- ÉTAPE 2 : 10 Événements Santé
-- ═════════════════════════════════════

INSERT INTO event (titre, type, description, date, time, lieu, status, image_path, capacite_max) VALUES

('Campagne Don de Sang',
 'Don de sang',
 'Participez à notre grande campagne de don de sang. Chaque don peut sauver jusqu''à 3 vies. Venez avec votre carte d''identité et un repas léger. L''équipe médicale sera présente pour vous accompagner tout au long du processus.',
 '2026-06-05', '08:00', 'Hôpital Charles Nicolle, Tunis',
 'Planifié', 'events_images/don_de_sang.png', 50),

('Conférence Internationale sur le Diabète',
 'Conférence médicale',
 'Conférence réunissant les meilleurs spécialistes en endocrinologie et diabétologie. Au programme : nouvelles thérapies, innovations numériques, nutrition et prévention. Destinée aux professionnels de santé et au grand public.',
 '2026-06-18', '09:00', 'Centre de Congrès Tunis, Lac 2',
 'Planifié', 'events_images/conference_medicale.png', 50),

('Journée Nationale de Vaccination',
 'Vaccination',
 'Journée de vaccination gratuite pour toute la famille. Vaccins disponibles : grippe saisonnière, méningite, hépatite B, tétanos. Aucune ordonnance requise. Venez accompagné de vos enfants pour les vaccins pédiatriques.',
 '2026-06-22', '08:30', 'Clinique Pasteur, Avenue de la Liberté, Tunis',
 'Planifié', 'events_images/vaccination.png', 50),

('Sensibilisation au Cancer du Sein',
 'Sensibilisation',
 'Campagne de sensibilisation au dépistage précoce du cancer du sein. Consultations de mammographie à tarif réduit. Conférences animées par des oncologues spécialistes. Distribution de matériel éducatif gratuit.',
 '2026-07-08', '09:00', 'CHU Farhat Hached, Sousse',
 'Planifié', 'events_images/sensibilisation.png', 50),

('Consultations Cardiologiques Gratuites',
 'Consultation',
 'Bénéficiez d''une consultation cardiologique gratuite assurée par une équipe de cardiologues bénévoles. Électrocardiogramme, mesure de la tension artérielle et bilan cardiaque complet inclus. Prise de rendez-vous sur place.',
 '2026-07-15', '08:00', 'Clinique Carthage, Tunis Nord',
 'Planifié', 'events_images/consultation.png', 50),

('Atelier Nutrition et Hygiène Alimentaire',
 'Atelier santé',
 'Atelier interactif animé par des diététiciens et nutritionnistes certifiés. Apprenez à composer des repas équilibrés, à lire les étiquettes alimentaires et à prévenir le surpoids et l''obésité. Dégustation d''aliments sains incluse.',
 '2026-07-22', '10:00', 'Institut Pasteur de Tunis, Bab Saadoun',
 'Planifié', 'events_images/atelier_sante.png', 50),

('Journée Mondiale de la Santé Mentale',
 'Sensibilisation',
 'Événement dédié à la santé mentale et au bien-être psychologique. Ateliers de gestion du stress et de l''anxiété, consultations avec des psychologues, groupes de parole et séances de méditation pleine conscience.',
 '2026-08-05', '09:00', 'Hôpital Razi, La Manouba',
 'Planifié', 'events_images/sensibilisation.png', 50),

('Campagne de Dépistage de l''Hypertension',
 'Dépistage',
 'Dépistage gratuit de l''hypertension artérielle et du diabète. Mesure de la glycémie à jeun, bilan lipidique et calcul de l''IMC. Des médecins spécialistes seront disponibles pour les consultations de suivi.',
 '2026-08-19', '07:30', 'Centre de Santé de l''Ariana, Avenue Mongi Slim',
 'Planifié', 'events_images/depistage.png', 50),

('Campagne Ophtalmologique Gratuite',
 'Consultation',
 'Examen ophtalmologique complet gratuit pour tous les âges. Tests de vision, dépistage du glaucome, examen du fond de l''œil. Lunettes correctrices disponibles à tarif social pour les cas détectés. Priorité aux enfants scolarisés.',
 '2026-09-03', '08:00', 'Clinique Taoufik, Rue de Rome, Tunis',
 'Planifié', 'events_images/consultation.png', 50),

('Séminaire Gestes de Premiers Secours',
 'Premiers secours',
 'Apprenez les gestes qui sauvent ! Formation pratique aux premiers secours animée par des paramédicaux certifiés Croissant-Rouge. Au programme : massage cardiaque, défibrillateur, Heimlich, brûlures, fractures. Attestation délivrée à la fin.',
 '2026-09-17', '09:00', 'Siège du Croissant-Rouge Tunisien, Avenue Mohammed V, Tunis',
 'Planifié', 'events_images/premiers_secours.png', 50);

-- ═════════════════════════════════════
-- ÉTAPE 3 : 20 Participants réalistes
-- ═════════════════════════════════════

INSERT INTO participant (nom, prenom, email, telephone) VALUES
('Ben Ali',      'Ahmed',     'ahmed.benali@gmail.com',       '+216 22 100 001'),
('Trabelsi',     'Fatma',     'fatma.trabelsi@gmail.com',     '+216 22 100 002'),
('Mansour',      'Mohamed',   'med.mansour@gmail.com',        '+216 22 100 003'),
('Chaabane',     'Ines',      'ines.chaabane@gmail.com',      '+216 22 100 004'),
('Jebali',       'Khalil',    'khalil.jebali@gmail.com',      '+216 22 100 005'),
('Sfaxi',        'Mariam',    'mariam.sfaxi@gmail.com',       '+216 22 100 006'),
('Bouazizi',     'Yassine',   'yassine.bouazizi@gmail.com',   '+216 22 100 007'),
('Hamdi',        'Sonia',     'sonia.hamdi@gmail.com',        '+216 22 100 008'),
('Khelifi',      'Omar',      'omar.khelifi@gmail.com',       '+216 22 100 009'),
('Rejeb',        'Nour',      'nour.rejeb@gmail.com',         '+216 22 100 010'),
('Dridi',        'Tarek',     'tarek.dridi@gmail.com',        '+216 22 100 011'),
('Zouari',       'Amira',     'amira.zouari@gmail.com',       '+216 22 100 012'),
('Aouini',       'Bassem',    'bassem.aouini@gmail.com',      '+216 22 100 013'),
('Oueslati',     'Leila',     'leila.oueslati@gmail.com',     '+216 22 100 014'),
('Maaloul',      'Bilel',     'bilel.maaloul@gmail.com',      '+216 22 100 015'),
('Hamzaoui',     'Rania',     'rania.hamzaoui@gmail.com',     '+216 22 100 016'),
('Baccouche',    'Adel',      'adel.baccouche@gmail.com',     '+216 22 100 017'),
('Gharbi',       'Nesrine',   'nesrine.gharbi@gmail.com',     '+216 22 100 018'),
('Saidi',        'Mokhtar',   'mokhtar.saidi@gmail.com',      '+216 22 100 019'),
('Achour',       'Yosra',     'yosra.achour@gmail.com',       '+216 22 100 020');

-- ═════════════════════════════════════
-- VÉRIFICATION
-- ═════════════════════════════════════
SELECT 'Événements insérés' AS info, COUNT(*) AS nb FROM event;
SELECT 'Participants insérés' AS info, COUNT(*) AS nb FROM participant;
