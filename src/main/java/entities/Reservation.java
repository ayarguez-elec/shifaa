package entities;

import java.time.LocalDateTime;

/**
 * Represents a booking request submitted to the Smart Booking Engine.
 * Status lifecycle: PENDING → CONFIRMED | REFUSED | CANCELLED
 */
public class Reservation {

    public enum Statut {
        EN_ATTENTE, CONFIRME, REFUSE, ANNULE, LISTE_D_ATTENTE, CHECKED_IN
    }

    private int           id;
    private int           eventId;
    private int           participantId;
    private int           nombrePlaces;   // multi-place support
    private Statut        statut;
    private LocalDateTime dateDemande;    // FIFO ordering key
    private String        motifRefus;     // if REFUSE, reason is logged

    // ── Full constructor (with id, from DB) ─────────────────────────────────
    public Reservation(int id, int eventId, int participantId,
                       int nombrePlaces, Statut statut,
                       LocalDateTime dateDemande, String motifRefus) {
        this.id            = id;
        this.eventId       = eventId;
        this.participantId = participantId;
        this.nombrePlaces  = nombrePlaces;
        this.statut        = statut;
        this.dateDemande   = dateDemande;
        this.motifRefus    = motifRefus;
    }

    // ── Constructor for new requests (no id yet) ────────────────────────────
    public Reservation(int eventId, int participantId, int nombrePlaces) {
        this(0, eventId, participantId, nombrePlaces,
             Statut.EN_ATTENTE, LocalDateTime.now(), null);
    }

    // ── Getters ─────────────────────────────────────────────────────────────
    public int           getId()            { return id; }
    public int           getEventId()       { return eventId; }
    public int           getParticipantId() { return participantId; }
    public int           getNombrePlaces()  { return nombrePlaces; }
    public Statut        getStatut()        { return statut; }
    public LocalDateTime getDateDemande()   { return dateDemande; }
    public String        getMotifRefus()    { return motifRefus; }

    // ── Setters ─────────────────────────────────────────────────────────────
    public void setId(int id)                    { this.id           = id; }
    public void setStatut(Statut s)              { this.statut       = s; }
    public void setMotifRefus(String motif)      { this.motifRefus   = motif; }
    public void setNombrePlaces(int n)           { this.nombrePlaces = n; }

    @Override
    public String toString() {
        return "Reservation{id=" + id + ", eventId=" + eventId +
               ", participantId=" + participantId + ", places=" + nombrePlaces +
               ", statut=" + statut + ", demande=" + dateDemande + "}";
    }
}
