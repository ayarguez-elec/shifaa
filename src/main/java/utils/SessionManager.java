package utils;

public class SessionManager {

    private static int connectedPharmacyId = 1;

    public static int getConnectedPharmacyId() {
        return connectedPharmacyId;
    }

    public static void setConnectedPharmacyId(int pharmacyId) {
        connectedPharmacyId = pharmacyId;
    }

    public static void clearSession() {
        connectedPharmacyId = 0;
    }
}