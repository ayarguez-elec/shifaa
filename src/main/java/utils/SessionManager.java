package utils;

import entities.user;

public class SessionManager {

    private static SessionManager instance;
    private user currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(user u) {
        this.currentUser = u;
        if (u != null) {
            tests.Main.currentUserId   = u.getUserId();
            tests.Main.currentUserRole = u.getRole().name();
        }
    }

    public void logout() {
        this.currentUser = null;
        tests.Main.currentUserId   = -1;
        tests.Main.currentUserRole = "";
    }

    public user getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public int getCurrentUserId() {
        if (currentUser != null) return currentUser.getUserId();
        return tests.Main.currentUserId;
    }

    public String getCurrentUserRole() {
        if (currentUser != null) return currentUser.getRole().name();
        return tests.Main.currentUserRole;
    }

    private static int connectedPharmacyId = 1;

    public static int getConnectedPharmacyId() {
        return connectedPharmacyId;
    }

    public static void setConnectedPharmacyId(int pharmacyId) {
        connectedPharmacyId = pharmacyId;
    }
}
