# 🩺 SHIFAA – Plateforme de Santé Intégrée

<div align="center">

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-20-blue?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)

> **SHIFAA** (الشفاء — *la guérison*) est une solution desktop avancée conçue pour transformer la gestion des services de santé.  
> Elle connecte de manière sécurisée **patients**, **médecins**, **pharmaciens** et **administrateurs** au sein d'un écosystème digital unifié.

</div>

---

## 📋 Table des Matières

- [🔭 Aperçu](#-aperçu)
- [✨ Fonctionnalités Principales](#-fonctionnalités-principales)
- [🛠️ Stack Technique](#️-stack-technique)
- [🏗️ Architecture du Projet](#-architecture-du-projet)
- [🚀 Installation et Configuration](#-installation-et-configuration)
- [📁 Structure du Projet](#-structure-du-projet)
- [💡 Points Forts](#-points-forts-du-projet)
- [🤝 Contributeurs](#-contributeurs)

---

## 🔭 Aperçu

SHIFAA est une application de bureau **tout-en-un** qui numérise et automatise les processus clés d'un établissement de santé. Grâce à une architecture **MVC** rigoureuse et une couche service dédiée, la plateforme garantit :

- 🔒 Une **sécurité biométrique** par reconnaissance faciale (Face ID).
- 🧑‍💼 Une **gestion multi-rôles** avec des tableaux de bord interactifs.
- 📄 Une **génération automatique** de documents médicaux (PDF, Excel, QR Code).
- 📲 Des **notifications en temps réel** par Email et SMS.
- 🤖 Un **assistant intelligent** intégré pour l'aide au diagnostic.

---

## ✨ Fonctionnalités Principales

### 🔐 Sécurité & Authentification

| Fonctionnalité | Description |
|---|---|
| **Face ID Login** | Authentification biométrique par reconnaissance faciale via webcam (intégration Python/OpenCV). Sécurité renforcée contre l'usurpation. |
| **Gestion des Rôles** | Dashboards personnalisés pour Administrateurs, Médecins, Patients et Pharmaciens. |

### 👨‍⚕️ Espace Médecin & Santé
- 🗓️ **Gestion des rendez-vous** : Suivi des consultations et gestion dynamique des disponibilités.
- 📝 **Ordonnances Digitales** : Génération de prescriptions au format **PDF** via `iText`.
- 🗂️ **Dossiers Médicaux** : Historique complet, fiches de soins et suivi évolutif.
- 🤖 **Assistant IA** : Intégration de l'API Grok pour des recommandations de santé intelligentes.

### 👤 Espace Patient & Fidélité
- 📅 **Smart Booking** : Réservation simplifiée de rendez-vous selon les spécialités.
- 🎁 **Loyalty Program** : Programme de fidélité avec système de points pour les patients actifs.
- ⭐ **Feedback** : Système de notation des prestations et gestion des réclamations.
- 🎪 **Événements** : Inscription aux campagnes de sensibilisation avec validation par **QR Code**.

### 💊 Espace Pharmacien
- 📦 **Gestion des Stocks** : Inventaire en temps réel et alertes de stock bas.
- 🛒 **Catalogue** : Consultation rapide des médicaments et traitements disponibles.

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
│  IA / Vision     │  Python 3.x + OpenCV + Face Recognition  │
├──────────────────┼──────────────────────────────────────────┤
│  Bibliothèques   │  iText       → Génération PDF            │
│  Clés            │  ZXing       → Génération QR Code        │
│                  │  Apache POI  → Export Excel              │
│                  │  Twilio      → Notifications SMS         │
│                  │  Jakarta Mail→ Notifications Email       │
│                  │  Gson/Grok   → Intelligence Artificielle │
└──────────────────┴──────────────────────────────────────────┘
```

---

## 🏗️ Architecture du Projet

Le projet repose sur le pattern **MVC (Modèle – Vue – Contrôleur)**, optimisé par une couche **Services** pour une séparation nette des responsabilités.

- **Frontend** : Interfaces riches développées en FXML avec un stylisme CSS médical moderne.
- **Backend** : Services robustes gérant la persistance et la logique métier complexe.
- **Biométrie (Module Python)** :
  ```
  Webcam ──▶ OpenCV (Capture) ──▶ Face Recognition ──▶ Java (Validation Session)
  ```

---

## 🚀 Installation et Configuration

### Prérequis
- **JDK 17** ou supérieur.
- **Apache Maven 3.8+**.
- **MySQL 8.0+**.
- **Python 3.x** avec `opencv-python`, `face-recognition` et `numpy`.

### ⚙️ Étapes d'Installation

1. **Cloner le dépôt**
   ```bash
   git clone https://github.com/ayarguez-elec/shifaa.git
   cd shifaa
   ```

2. **Base de Données**
   Créez la base `sante_db` et importez les scripts dans cet ordre :
   ```sql
   SOURCE data_sante.sql;
   SOURCE REPAIR_DB.sql;
   ```
   *Note : Configurez vos accès dans `src/main/java/utils/MyDataBase.java`.*

3. **Dépendances Python**
   ```bash
   pip install opencv-python face_recognition numpy
   ```

4. **Build & Run**
   ```bash
   mvn clean install
   mvn javafx:run
   ```

---

## 📁 Structure du Projet

```
shifaa/
├── src/main/java/
│   ├── entities/      # 📦 Modèles de données (User, Appointment, Prescription...)
│   ├── services/      # ⚙️  Logique métier et CRUD Database
│   ├── Controler/     # 🎮 Contrôleurs JavaFX (Logique UI)
│   ├── utils/         # 🛠️  Utilitaires (DB Connection, Mailer, FaceAuthHelper)
│   └── tests/         # 🚀 Main.java - Point d'entrée
├── src/main/resources/
│   ├── fxml/          # 🖼️  Vues (interfaces utilisateur)
│   └── css/           # 🎨 Thèmes et styles modernes
├── python/            # 🤖 Scripts de reconnaissance faciale (face_reco.py)
├── pom.xml            # Configuration Maven
└── README.md
```

---

## 💡 Points Forts du Projet

| 🏆 Innovation | Détail |
|---|---|
| **Sécurité Biométrique** | Le Face ID offre une couche de protection critique pour les données médicales sensibles. |
| **Interopérabilité** | Flux de données complet : de la consultation (PDF) à la pharmacie, avec export Excel pour l'admin. |
| **Expérience Utilisateur** | Design JavaFX soigné, offrant la réactivité d'une application native. |
| **Communication Omnicanale** | Notifications SMS et Email intégrées pour un suivi patient optimal. |

---

## 🤝 Contributeurs

Ce projet a été développé avec passion pour moderniser le secteur de la santé.

| Nom | Rôle |
|---|---|
| **[Votre Nom]** | Lead Developer / Architect |

---

<div align="center">

**Fait avec ❤️ pour améliorer les soins de santé**
*SHIFAA — الشفاء — La guérison*

</div>