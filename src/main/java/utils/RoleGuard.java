package utils;

import entities.user;
import entities.user.Role;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Centralized access-control utility.
 * Equivalent to Spring Security @PreAuthorize / SecurityConfig route protection.
 *
 * Three-layer defence strategy:
 *   Layer 1 – Navigation guard  : Main.java blocks loading admin FXMLs
 *   Layer 2 – Controller guard  : initialize() + action handlers check here
 *   Layer 3 – Service guard     : eventServ rejects unauthorized DB operations
 */
public final class RoleGuard {

    private RoleGuard() {}

    // ── Role checks ───────────────────────────────────────────────────────────

    public static user getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }

    public static Role getCurrentRole() {
        user u = getCurrentUser();
        return (u != null) ? u.getRole() : null;
    }

    /** True if the logged-in user has the admin role. */
    public static boolean isAdmin() {
        return getCurrentRole() == Role.admin;
    }

    /** True if the logged-in user has the patient role. */
    public static boolean isPatient() {
        return getCurrentRole() == Role.patient;
    }

    /** True if the logged-in user has ANY of the given roles. */
    public static boolean hasAnyRole(Role... roles) {
        Role current = getCurrentRole();
        if (current == null) return false;
        for (Role r : roles) {
            if (current == r) return true;
        }
        return false;
    }

    // ── Enforcement helpers ───────────────────────────────────────────────────

    /**
     * Shows a 403 alert and returns true if the current user is NOT admin.
     * Usage in action handlers: if (RoleGuard.denyIfNotAdmin()) return;
     */
    public static boolean denyIfNotAdmin() {
        if (isAdmin()) return false;
        show403Alert("Cette action est réservée aux administrateurs.");
        return true;
    }

    /**
     * Shows a 403 alert and returns true if the current user is NOT in the allowed roles.
     */
    public static boolean denyIfNotRole(Role... allowed) {
        if (hasAnyRole(allowed)) return false;
        show403Alert("Accès refusé. Vous n'avez pas les permissions nécessaires.");
        return true;
    }

    /**
     * Redirects to patient events view if the current user is not admin.
     * Must be called inside Platform.runLater when used from initialize().
     * Returns true if access was denied (caller should stop initialization).
     */
    public static boolean redirectIfNotAdmin(Runnable redirectAction) {
        if (isAdmin()) return false;
        show403Alert("Accès non autorisé. Redirection en cours…");
        if (redirectAction != null) {
            Platform.runLater(redirectAction);
        }
        return true;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static void show403Alert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR,
            "⛔ " + message,
            ButtonType.OK);
        alert.setTitle("Accès refusé — 403");
        alert.setHeaderText("Accès non autorisé");
        alert.showAndWait();
    }
}
