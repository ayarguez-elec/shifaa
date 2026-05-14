package entities;

public enum PatientStatus {
    STABLE("Stable"),
    CRITIQUE("Critique"),
    EN_TRAITEMENT("En traitement"),
    RETABLI("Rétabli"),
    EN_OBSERVATION("En observation");

    private final String displayName;

    PatientStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}