# 🩺 SHIFAA – Plateforme de Santé Intégrée

<div align="center">

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-20-blue?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)

> **SHIFAA** (الشفاء — *la guérison*) est une solution desktop avancée conçue pour transformer la gestion des services de santé.  
> Elle connecte de manière sécurisée **patients**, **médecins**, **pharmaciens** , **assistant** et **administrateurs** au sein d'un écosystème digital unifié.

</div>

---

## 📋 Table des Matières

- [Aperçu](#-aperçu)
- [Modules Fonctionnels](#-modules-fonctionnels)
- [Fonctionnalités](#-fonctionnalités-principales)
- [Stack Technique](#️-stack-technique)
- [Architecture](#-architecture-du-projet)
- [Installation](#-installation-et-configuration)
- [Structure du Projet](#-structure-du-projet)
- [Captures d'Écran](#-captures-décran)
- [Contributeurs](#-contributeurs)

---

## 🔭 Aperçu

SHIFAA est une application de bureau **tout-en-un** qui numérise et automatise les processus clés d'un établissement de santé. Grâce à une architecture **MVC** rigoureuse et une couche service dédiée, la plateforme garantit :

- 🔒 Une **sécurité biométrique** par reconnaissance faciale (Face ID)
- 🧑‍💼 Une **gestion multi-rôles** avec des tableaux de bord dédiés
- 📄 Une **génération automatique** de documents médicaux (PDF, Excel, QR Code)
- 📲 Des **notifications en temps réel** par Email et SMS

---

## 🗂️ Modules Fonctionnels

SHIFAA est structurée autour de **6 modules métier** indépendants et interconnectés, couvrant l'intégralité du parcours de santé numérique :

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          MODULES SHIFAA                                 │
├───┬────────────────────────────────┬────────────────────────────────────┤
│ 1 │ 👤 Gestion des Utilisateurs    │ Identité numérique & Face ID       │
├───┼────────────────────────────────┼────────────────────────────────────┤
│ 2 │ 📅 Gestion des RDV             │ Flux patients & notifications      │
├───┼────────────────────────────────┼────────────────────────────────────┤
│ 3 │ 💊 Gestion Pharmacie & Produits│ Stocks & approvisionnement         │
├───┼────────────────────────────────┼────────────────────────────────────┤
│ 4 │ 🗂️ Dossier Médical & Prescription│ Parcours soins & PDF             │
├───┼────────────────────────────────┼────────────────────────────────────┤
│ 5 │ 🎪 Gestion Événements          │ Promotion santé & QR Code          │
├───┼────────────────────────────────┼────────────────────────────────────┤
│ 6 │ 📣 Gestion Réclamations        │ Qualité de service & feedback      │
└───┴────────────────────────────────┴────────────────────────────────────┘
```

---

### 👤 Module 1 — Gestion des Utilisateurs
> *Identité numérique & sécurité Face ID*

Chaque utilisateur dispose d'une **identité numérique unique** protégée par authentification biométrique. Le module gère l'ensemble du cycle de vie des comptes et contrôle les droits d'accès selon le rôle.

- 🔐 **Face ID** : Connexion par reconnaissance faciale via webcam (Python/OpenCV) — élimine les risques liés aux mots de passe faibles.
- 🛡️ **RBAC** (Role-Based Access Control) : Chaque rôle accède uniquement aux fonctionnalités qui lui sont autorisées.
- 📋 **Audit des accès** : Traçabilité des connexions et des actions sensibles.

---

### 📅 Module 2 — Gestion des RDV
> *Flux patients & agendas médecins (avec notifications Email/SMS)*

Gère l'intégralité du flux patient, de la prise de rendez-vous à la consultation, avec confirmation et rappel automatique.

- 🗓️ **Agenda médecin** : Visualisation et gestion des plages de disponibilité.
- ⚡ **Smart Booking** : Réservation en temps réel selon les créneaux libres.
- 📧 **Email & SMS** : Confirmations et rappels automatiques (Jakarta Mail + Twilio) pour réduire les absences.
- ❌ **Gestion des annulations** : Libération automatique des créneaux et notification du médecin.

---

### 💊 Module 3 — Gestion Pharmacie & Produits
> *Chaîne d'approvisionnement & suivi des stocks*

Digitalise la chaîne d'approvisionnement pharmaceutique pour assurer la disponibilité des médicaments à tout moment.

- 📦 **Inventaire en temps réel** : Suivi des quantités, dates de péremption et seuils d'alerte.
- 🛒 **Catalogue produits** : Fiche détaillée par médicament (posologie, contre-indications, fournisseur).
- ⚠️ **Alertes de rupture** : Notifications automatiques en cas de stock critique.
- 📊 **Rapports de stock** : Export Excel des mouvements d'inventaire.

---

### 🗂️ Module 4 — Dossier Médical & Prescription
> *Digitalisation du parcours de soins (PDF)*

Assure la digitalisation complète du parcours de soins, du diagnostic à l'ordonnance, pour une prise en charge continue et sécurisée.

- 📁 **Dossier patient complet** : Historique médical, antécédents, allergies, résultats d'examens.
- 📝 **Ordonnances digitales** : Génération automatique de prescriptions au format **PDF** (iText), horodatées.
- 🔗 **Liaison RDV → Dossier** : Chaque consultation enrichit automatiquement le dossier du patient.
- 🔒 **Confidentialité** : Accès restreint au médecin traitant et au patient concerné.

---

### 🎪 Module 5 — Gestion Événements & Participants
> *Promotion de la santé & suivi par QR Code*

Permet l'organisation de campagnes de promotion de la santé (dépistages, conférences, ateliers) avec un suivi précis des participants.

- 📅 **Création d'événements** : Titre, description, lieu, date et capacité maximale.
- 🎟️ **Inscription en ligne** : Réservation de place directement depuis l'espace patient.
- 📲 **QR Code unique** : Généré à l'inscription (ZXing) et scanné à l'entrée pour valider la présence.
- 📊 **Suivi des participants** : Liste des inscrits, taux de présence, export de rapports.

---

### 📣 Module 6 — Gestion Réclamations & Réponses
> *Qualité de service & feedback*

Donne aux patients un canal officiel pour exprimer leur satisfaction ou signaler un problème, et garantit un suivi rigoureux des retours.

- ⭐ **Système de notation** : Évaluation post-consultation (qualité d'accueil, délais, soins).
- 📩 **Formulaire de réclamation** : Soumission structurée avec catégorie et description.
- 🔄 **Suivi de traitement** : Statut mis à jour en temps réel (Reçue → En cours → Résolue).
- 💬 **Réponse officielle** : L'administrateur répond directement depuis son tableau de bord.

---

## ✨ Fonctionnalités Principales

### 🔐 Sécurité & Authentification

| Fonctionnalité | Description |
|---|---|
| **Face ID Login** | Authentification biométrique par reconnaissance faciale via webcam (intégration Python/OpenCV). Réduit les risques d'usurpation d'identité. |
| **Gestion des Rôles** | Accès restreint et personnalisé selon le profil : Administrateur, Médecin, Patient, Pharmacien. |

---

### 🏥 Espace Administrateur

- 📊 **Tableau de bord global** : Vue d'ensemble des statistiques de la plateforme.
- 👥 **Gestion des utilisateurs** : Création, modification et suppression des comptes.
- 📈 **Export de rapports** : Génération de rapports d'activité au format **Excel** (Apache POI).

---

### 👨‍⚕️ Espace Médecin

- 🗓️ **Gestion des rendez-vous** : Suivi des consultations et gestion des plages de disponibilité en temps réel.
- 📝 **Ordonnances Digitales** : Génération automatique de prescriptions médicales au format **PDF** (iText).
- 🗂️ **Dossiers Patients** : Accès à l'historique médical complet et sécurisé de chaque patient.

---

### 👤 Espace Patient

- 📅 **Smart Booking** : Réservation intelligente de rendez-vous selon les disponibilités des médecins.
- 🎁 **Programme de Fidélité** : Système de points et participation à des événements de santé.
- ⭐ **Feedback & Réclamations** : Système de notation et de suivi de la satisfaction patient.
- 📲 **Notifications** : Rappels de rendez-vous automatiques par **Email** et **SMS**.

---

### 🧑‍💼 Espace Assistant

L'Assistant est un **opérateur humain dédié à la coordination des rendez-vous**. Il joue le rôle d'intermédiaire entre les patients et les médecins, garantissant un flux de consultations fluide et organisé.

- ✅ **Validation des demandes** : Acceptation ou refus des demandes de rendez-vous soumises par les patients, avec motif de refus si nécessaire.
- ✏️ **Modification des rendez-vous** : Mise à jour des détails d'un rendez-vous (date, heure, médecin, motif de consultation).
- 🗑️ **Suppression** : Annulation d'un rendez-vous avec notification automatique au patient (Email/SMS).
- 🕐 **Gestion des disponibilités** : Ajout et mise à jour des créneaux des médecins — marquage **Occupé** ou **Libre** en temps réel pour refléter fidèlement l'agenda.
- 📋 **Vue centralisée** : Tableau de bord dédié listant toutes les demandes en attente, confirmées et annulées pour une gestion rapide et efficace.

---

### 💊 Espace Pharmacien

- 📦 **Gestion des Stocks** : Suivi en temps réel de l'inventaire des médicaments.
- 🛒 **Catalogue Produits** : Consultation des produits disponibles avec détails des traitements.
- ⚠️ **Alertes de Stock** : Notifications automatiques en cas de rupture ou de stock bas.

---

### 📢 Événements & Communication

- 🎪 **Événements de Santé** : Organisation et gestion d'événements médicaux et campagnes de sensibilisation.
- 📲 **QR Codes** : Génération de codes QR (ZXing) pour la validation des réservations d'événements.
- 📧 **Emails Automatisés** : Confirmations et rappels via **Jakarta Mail**.
- 💬 **SMS** : Notifications critiques via l'API **Twilio**.

---

## 🛠️ Stack Technique

```
┌─────────────────────────────────────────────────────────────┐
│                         SHIFAA STACK                        │
├──────────────────┬──────────────────────────────────────────┤
│  Backend         │  Java 17                                 │
│  Frontend        │  JavaFX 20 + FXML + CSS3                 │
│  Base de données │  MySQL 8.0                               │
│  Build Tool      │  Apache Maven                            │
│  IA / Vision     │  Python 3.x + OpenCV                    │
├──────────────────┼──────────────────────────────────────────┤
│  Bibliothèques   │  iText       → Génération PDF            │
│                  │  ZXing       → Génération QR Code        │
│                  │  Apache POI  → Export Excel              │
│                  │  Twilio      → Notifications SMS         │
│                  │  Jakarta Mail→ Notifications Email       │
└──────────────────┴──────────────────────────────────────────┘
```

---

## 🏗️ Architecture du Projet

SHIFAA suit le pattern **MVC (Modèle – Vue – Contrôleur)** enrichi d'une couche **Services** pour isoler la logique métier :

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Vue (FXML) │────▶│  Contrôleur  │────▶│   Services   │
│   + CSS      │     │  (JavaFX)    │     │  (Métier)    │
└──────────────┘     └──────────────┘     └──────┬───────┘
                                                  │
                                         ┌────────▼───────┐
                                         │   Entités      │
                                         │   (Modèles)    │
                                         └────────┬───────┘
                                                  │
                                         ┌────────▼───────┐
                                         │   MySQL DB     │
                                         │  (sante_db)    │
                                         └────────────────┘
```

**Module IA (Python)**
```
Webcam ──▶ OpenCV (capture) ──▶ Modèle de reconnaissance ──▶ Java (résultat auth)
```

---

## 🚀 Installation et Configuration

### Prérequis

- ☑️ **JDK 17** ou supérieur 
- ☑️ **Apache Maven 3.8+**
- ☑️ **MySQL Server 8.0+** 
- ☑️ **Python 3.x** avec les librairies `opencv-python` et `face_recognition`

---

### ⚙️ Étapes d'Installation

**1. Cloner le dépôt**

```bash
git clone https://github.com/ayarguez-elec/shifaa.git
cd shifaa
```

**2. Configurer la Base de Données**

```sql
-- Dans votre client MySQL :
CREATE DATABASE sante_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sante_db;

-- Importer les scripts fournis :
SOURCE data_sante.sql;
SOURCE REPAIR_DB.sql;
```

Mettez à jour les identifiants de connexion dans le fichier de configuration :

```java
// src/main/java/utils/DataSource.java (ou équivalent)
private static final String URL  = "jdbc:mysql://localhost:3306/sante_db";
private static final String USER = "votre_utilisateur";
private static final String PASS = "votre_mot_de_passe";
```

**3. Installer les dépendances Python**

```bash
pip install opencv-python face_recognition numpy
```

**4. Installer les dépendances Java (Maven)**

```bash
mvn clean install
```

**5. Lancer l'application**

```bash
mvn javafx:run
# ou directement via votre IDE (IntelliJ IDEA recommandé)
# Classe principale : tests.Main
```

---

## 📁 Structure du Projet

```
shifaa/
│
├── src/main/java/
│   ├── entities/            # 📦 Modèles de données (Patient, Médecin, RDV…)
│   ├── services/            # ⚙️  Logique métier & accès BDD (CRUD)
│   ├── Controler/           # 🎮 Contrôleurs JavaFX
│   └── tests/
│       └── Main.java        # 🚀 Point d'entrée de l'application
│
├── src/main/resources/
│   ├── fxml/                # 🖼️  Vues FXML (interfaces utilisateur)
│   ├── css/                 # 🎨 Feuilles de style (thème moderne)
│   └── images/              # 🖼️  Ressources graphiques
│
├── python/                  # 🤖 Scripts de reconnaissance faciale
│   ├── face_auth.py         # Module d'authentification
│   └── known_faces/         # Base de données des visages enregistrés
│
├── sql/
│   ├── data_sante.sql       # Schéma & données initiales
│   └── REPAIR_DB.sql        # Script de réparation/migration
│
├── pom.xml                  # Configuration Maven & dépendances
└── README.md
```

---

## 💡 Points Forts du Projet

| 🏆 Innovation | Détail |
|---|---|
| **Sécurité Biométrique** | Le Face ID élève le niveau de sécurité au-delà des mots de passe classiques, essentiel pour la protection des données de santé (conformité RGPD-ready). |
| **Interopérabilité** | Génération de formats standards (PDF, Excel, QR Code) facilitant l'échange d'informations entre tous les acteurs de la santé. |
| **UX Moderne** | Interface desktop pensée pour être intuitive et visuellement soignée grâce à un CSS personnalisé sous JavaFX. |
| **Automatisation** | Réduction de la charge administrative grâce aux notifications automatiques (SMS/Email) et à la génération automatique de documents. |

---

## 🤝 Contributeurs

> Projet réalisé dans le cadre d'un projet académique / professionnel.
 *prospera* 

---



<div align="center">

**Fait avec ❤️ pour améliorer les soins de santé**

*SHIFAA — الشفاء — La guérison*

</div>
