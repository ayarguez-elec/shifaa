/* =========================================
   SHIFAA - Mock Data
   src/data/mockData.js
   ========================================= */

// ---- State Management ---- //
const ShifaaDB = {

  // ---- EVENTS ---- //
  events: [
    {
      id: 1,
      titre: "Conférence Internationale de Cardiologie",
      type: "Conférence",
      date: "2025-06-15",
      heure: "09:00",
      lieu: "Hôpital La Rabta, Tunis",
      capacite: 120,
      inscrits: 87,
      statut: "Actif",
      description: "Conférence annuelle réunissant des spécialistes du monde entier sur les avancées en cardiologie interventionnelle et prévention cardiovasculaire.",
      organisateur: "Dr. Amira Ben Trabelsi"
    },
    {
      id: 2,
      titre: "Formation Premiers Secours & RCP",
      type: "Formation",
      date: "2025-07-02",
      heure: "08:30",
      lieu: "Centre Médical Ibn Sina, Tunis",
      capacite: 40,
      inscrits: 32,
      statut: "Actif",
      description: "Formation pratique certifiante aux gestes de premiers secours, réanimation cardio-pulmonaire et utilisation du défibrillateur.",
      organisateur: "Sarah Miller"
    },
    {
      id: 3,
      titre: "Séminaire Santé Mentale & Bien-être",
      type: "Séminaire",
      date: "2025-05-20",
      heure: "10:00",
      lieu: "Clinique Hannibal, Tunis",
      capacite: 80,
      inscrits: 80,
      statut: "Terminé",
      description: "Séminaire pluridisciplinaire sur la prise en charge des troubles mentaux, la prévention du burnout et les nouvelles thérapies cognitivo-comportementales.",
      organisateur: "Dr. James Wilson"
    },
    {
      id: 4,
      titre: "Atelier Nutrition & Diabète",
      type: "Atelier",
      date: "2025-08-10",
      heure: "14:00",
      lieu: "CHU Mongi Slim, La Marsa",
      capacite: 30,
      inscrits: 12,
      statut: "Actif",
      description: "Atelier pratique animé par des diététiciens et endocrinologues sur la nutrition adaptée pour les patients diabétiques de type 2.",
      organisateur: "Dr. Robert Williams"
    },
    {
      id: 5,
      titre: "Forum Chirurgie Robotique 2025",
      type: "Conférence",
      date: "2025-04-18",
      heure: "09:00",
      lieu: "Palais des Congrès, Tunis",
      capacite: 200,
      inscrits: 198,
      statut: "Terminé",
      description: "Grand forum national sur les nouvelles techniques de chirurgie robotique, laparoscopie avancée et perspectives d'IA en bloc opératoire.",
      organisateur: "John Doe"
    },
    {
      id: 6,
      titre: "Formation Hygiène Hospitalière",
      type: "Formation",
      date: "2025-09-05",
      heure: "08:00",
      lieu: "Hôpital Charles Nicolle, Tunis",
      capacite: 60,
      inscrits: 5,
      statut: "Actif",
      description: "Formation obligatoire sur les protocoles d'hygiène, de stérilisation et de prévention des infections nosocomiales en milieu hospitalier.",
      organisateur: "Emma Thompson"
    }
  ],

  // ---- PARTICIPANTS ---- //
  participants: [
    { id: 1,  prenom: "Ahmed",    nom: "Ben Ali",      email: "ahmed.benali@hopital.tn",         tel: "+216 71 234 567", role: "Docteur",    statut: "Actif",    eventId: 1, age: 45, adresse: "Tunis" },
    { id: 2,  prenom: "Fatma",    nom: "Trabelsi",     email: "fatma.trabelsi@gmail.com",         tel: "+216 98 765 432", role: "Patient",    statut: "Actif",    eventId: 1, age: 34, adresse: "Sfax" },
    { id: 3,  prenom: "Sami",     nom: "Karray",       email: "sami.karray@pharmacie.tn",         tel: "+216 52 111 222", role: "Pharmacien", statut: "Actif",    eventId: 2, age: 39, adresse: "Sousse" },
    { id: 4,  prenom: "Leila",    nom: "Mansour",      email: "leila.mansour@clinique.tn",        tel: "+216 55 333 444", role: "Assistant",  statut: "Suspendu", eventId: 3, age: 28, adresse: "Monastir" },
    { id: 5,  prenom: "Omar",     nom: "Hamdi",        email: "omar.hamdi@chu.tn",                tel: "+216 97 555 666", role: "Docteur",    statut: "Actif",    eventId: 4, age: 52, adresse: "La Marsa" },
    { id: 6,  prenom: "Rania",    nom: "Chebbi",       email: "rania.chebbi@gmail.com",           tel: "+216 22 777 888", role: "Patient",    statut: "Actif",    eventId: 2, age: 27, adresse: "Ariana" },
    { id: 7,  prenom: "Khaled",   nom: "Jebali",       email: "khaled.jebali@medecin.tn",        tel: "+216 71 888 999", role: "Docteur",    statut: "Actif",    eventId: 5, age: 48, adresse: "Tunis" },
    { id: 8,  prenom: "Maryam",   nom: "Sellami",      email: "maryam.sellami@patient.tn",       tel: "+216 50 123 456", role: "Patient",    statut: "Actif",    eventId: 3, age: 31, adresse: "Bizerte" },
    { id: 9,  prenom: "Nizar",    nom: "Bouaziz",      email: "nizar.bouaziz@pharma.tn",         tel: "+216 73 321 654", role: "Pharmacien", statut: "Actif",    eventId: 6, age: 36, adresse: "Nabeul" },
    { id: 10, prenom: "Yasmine",  nom: "Gharbi",       email: "yasmine.gharbi@assistant.tn",     tel: "+216 94 456 789", role: "Assistant",  statut: "Actif",    eventId: 1, age: 25, adresse: "Ben Arous" },
  ],

  // ---- Counters for new IDs ---- //
  nextEventId: 7,
  nextParticipantId: 11,

  // ---- Helpers ---- //
  getEvent(id)       { return this.events.find(e => e.id === id); },
  getParticipant(id) { return this.participants.find(p => p.id === id); },

  getParticipantsByEvent(eventId) {
    return this.participants.filter(p => p.eventId === eventId);
  },

  addEvent(data) {
    const ev = { id: this.nextEventId++, inscrits: 0, ...data };
    this.events.push(ev);
    return ev;
  },

  updateEvent(id, data) {
    const idx = this.events.findIndex(e => e.id === id);
    if (idx !== -1) this.events[idx] = { ...this.events[idx], ...data };
    return this.events[idx];
  },

  deleteEvent(id) {
    this.events = this.events.filter(e => e.id !== id);
    // also remove linked participants
    this.participants = this.participants.filter(p => p.eventId !== id);
  },

  addParticipant(data) {
    const pt = { id: this.nextParticipantId++, ...data };
    this.participants.push(pt);
    // increment event inscrits
    const ev = this.getEvent(data.eventId);
    if (ev) ev.inscrits = Math.min(ev.inscrits + 1, ev.capacite);
    return pt;
  },

  updateParticipant(id, data) {
    const idx = this.participants.findIndex(p => p.id === id);
    if (idx !== -1) this.participants[idx] = { ...this.participants[idx], ...data };
    return this.participants[idx];
  },

  deleteParticipant(id) {
    const pt = this.getParticipant(id);
    if (pt) {
      const ev = this.getEvent(pt.eventId);
      if (ev) ev.inscrits = Math.max(0, ev.inscrits - 1);
    }
    this.participants = this.participants.filter(p => p.id !== id);
  }
};
