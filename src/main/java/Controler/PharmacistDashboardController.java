package Controler;

import entities.Pharmacy;
import entities.Product;
import entities.user;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import services.PharmacyServices;
import services.PharmacyStockService;
import services.userServices;
import tests.Main;
import utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard controller for the Pharmacist role.
 * Shows stock KPIs, low-stock alerts, category breakdown, and recent updates.
 */
public class PharmacistDashboardController implements Initializable {

    private final PharmacyStockService stockSvc = new PharmacyStockService();
    private final PharmacyServices pharmacySvc = new PharmacyServices();
    private final userServices userSvc = new userServices();

    // ── Hero ──────────────────────────────────────────────────────
    @FXML private Label dateLabel;
    @FXML private Label greetingLabel;
    @FXML private Label pharmacyNameLabel;
    @FXML private Label pharmacyStatusLabel;
    @FXML private Label pharmacyHoursLabel;

    // ── KPI Cards ─────────────────────────────────────────────────
    @FXML private Label totalProductsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label outOfStockLabel;
    @FXML private Label availableLabel;

    // ── Dynamic containers ────────────────────────────────────────
    @FXML private VBox lowStockContainer;
    @FXML private VBox recentUpdatesContainer;

    private int pharmacyId = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dateLabel.setText(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.FRENCH)));

        loadPharmacistInfo();
        refreshAll();
    }

    // ── Resolve the pharmacist's pharmacy ────────────────────────
    private void loadPharmacistInfo() {
        // First try SessionManager (set when pharmacist logs in)
        int sessionPharmacyId = SessionManager.getConnectedPharmacyId();

        try {
            int currentUserId = Main.getCurrentUserId();
            String pharmacistName = "Pharmacien";

            for (user u : userSvc.getAll()) {
                if (u.getUserId() == currentUserId) {
                    pharmacistName = u.getFirstName() + " " + u.getLastName();
                    break;
                }
            }
            greetingLabel.setText("Bonjour, " + pharmacistName + " ! 👋");

            // Find pharmacy: prefer session, then match by name
            if (sessionPharmacyId > 0) {
                this.pharmacyId = sessionPharmacyId;
            } else {
                // Try to find a pharmacy linked to this user by pharmacyName field
                String pharmacyName = null;
                for (user u : userSvc.getAll()) {
                    if (u.getUserId() == currentUserId) {
                        pharmacyName = u.getPharmacyName();
                        break;
                    }
                }
                List<Pharmacy> all = pharmacySvc.getAll();
                if (pharmacyName != null && !pharmacyName.isEmpty()) {
                    final String pName = pharmacyName;
                    Optional<Pharmacy> match = all.stream()
                            .filter(p -> p.getPharmacyName() != null &&
                                         p.getPharmacyName().equalsIgnoreCase(pName))
                            .findFirst();
                    if (match.isPresent()) {
                        this.pharmacyId = match.get().getPharmacyId();
                    }
                }
                // Fallback: use first pharmacy
                if (this.pharmacyId == 0 && !all.isEmpty()) {
                    this.pharmacyId = all.get(0).getPharmacyId();
                }
            }

            // Load pharmacy info card
            if (this.pharmacyId > 0) {
                for (Pharmacy p : pharmacySvc.getAll()) {
                    if (p.getPharmacyId() == this.pharmacyId) {
                        pharmacyNameLabel.setText(p.getPharmacyName() != null ? p.getPharmacyName() : "—");
                        String status = p.getPharmacyStatus();
                        pharmacyStatusLabel.setText(status != null ? status : "—");
                        pharmacyStatusLabel.setStyle(
                                "Ouvert".equalsIgnoreCase(status)
                                ? "-fx-text-fill: #276749; -fx-font-weight: bold; -fx-font-size: 12px;"
                                : "-fx-text-fill: #C53030; -fx-font-weight: bold; -fx-font-size: 12px;");
                        pharmacyHoursLabel.setText(
                                p.getOpeningHours() != null ? "🕐 " + p.getOpeningHours() : "");
                        break;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            greetingLabel.setText("Bonjour ! 👋");
        }
    }

    private void refreshAll() {
        List<Product> products = (pharmacyId > 0)
                ? stockSvc.getProductsByPharmacy(pharmacyId)
                : stockSvc.getAllProductsForStock();

        long total      = products.size();
        long available  = products.stream()
                .filter(p -> "Disponible".equalsIgnoreCase(p.getAvailabilityStatus())).count();
        long lowStock   = products.stream()
                .filter(p -> "Stock faible".equalsIgnoreCase(p.getAvailabilityStatus())).count();
        long outOfStock = products.stream()
                .filter(p -> "Rupture".equalsIgnoreCase(p.getAvailabilityStatus())).count();

        totalProductsLabel.setText(String.valueOf(total));
        availableLabel.setText(String.valueOf(available));
        lowStockLabel.setText(String.valueOf(lowStock));
        outOfStockLabel.setText(String.valueOf(outOfStock));

        loadLowStockAlerts(products);
        loadRecentUpdates(products);
    }

    // ── Low stock / rupture alert list ────────────────────────────
    private void loadLowStockAlerts(List<Product> products) {
        lowStockContainer.getChildren().clear();

        List<Product> alerts = products.stream()
                .filter(p -> "Stock faible".equalsIgnoreCase(p.getAvailabilityStatus())
                          || "Rupture".equalsIgnoreCase(p.getAvailabilityStatus()))
                .sorted(Comparator.comparingInt(Product::getAvailableQuantity))
                .limit(6)
                .collect(Collectors.toList());

        if (alerts.isEmpty()) {
            Label ok = new Label("✅ Tous les produits sont bien approvisionnés");
            ok.setStyle("-fx-text-fill: #68D391; -fx-font-size: 13px; -fx-padding: 10 0 0 0;");
            lowStockContainer.getChildren().add(ok);
            return;
        }

        for (Product p : alerts) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 12, 10, 12));

            boolean isRupture = "Rupture".equalsIgnoreCase(p.getAvailabilityStatus());
            String borderColor = isRupture ? "#FC8181" : "#F6AD55";
            String bgColor     = isRupture ? "#FFF5F5" : "#FFFAF0";
            row.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; " +
                         "-fx-border-color: " + borderColor + "; -fx-border-radius: 10; " +
                         "-fx-border-width: 0 0 0 4;");

            // Icon
            Label icon = new Label(isRupture ? "🚫" : "⚠️");
            icon.setStyle("-fx-font-size: 18px;");

            // Info
            VBox info = new VBox(3);
            Label nameLabel = new Label(p.getProductName() != null ? p.getProductName() : "—");
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2D3748;");
            Label qtyLabel = new Label("Quantité : " + p.getAvailableQuantity()
                    + "  •  " + (p.getTherapeuticClass() != null ? p.getTherapeuticClass() : p.getCategory()));
            qtyLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
            info.getChildren().addAll(nameLabel, qtyLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Status badge
            Label badge = new Label(isRupture ? "Rupture" : "Stock faible");
            badge.setStyle(isRupture
                    ? "-fx-background-color: #FED7D7; -fx-text-fill: #C53030; -fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;"
                    : "-fx-background-color: #FEFCBF; -fx-text-fill: #744210; -fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");

            Button btnRestock = new Button("📦 Réappro.");
            btnRestock.setStyle("-fx-background-color: #4A6CF7; -fx-text-fill: white; " +
                                "-fx-font-size: 11px; -fx-background-radius: 7; -fx-cursor: hand; -fx-padding: 5 10;");
            btnRestock.setOnAction(e -> Main.showPharmacyAddStock());

            row.getChildren().addAll(icon, info, spacer, badge, btnRestock);
            lowStockContainer.getChildren().add(row);
        }
    }

    // ── Recent product updates ────────────────────────────────────
    private void loadRecentUpdates(List<Product> products) {
        recentUpdatesContainer.getChildren().clear();

        // Sort by lastUpdateDate desc (nulls last), take top 5
        List<Product> recent = products.stream()
                .sorted((a, b) -> {
                    if (a.getLastUpdateDate() == null && b.getLastUpdateDate() == null) return 0;
                    if (a.getLastUpdateDate() == null) return 1;
                    if (b.getLastUpdateDate() == null) return -1;
                    return b.getLastUpdateDate().compareTo(a.getLastUpdateDate());
                })
                .limit(5)
                .collect(Collectors.toList());

        if (recent.isEmpty()) {
            Label empty = new Label("Aucun produit enregistré");
            empty.setStyle("-fx-text-fill: #A0AEC0; -fx-font-size: 13px; -fx-padding: 10 0 0 0;");
            recentUpdatesContainer.getChildren().add(empty);
            return;
        }

        for (Product p : recent) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 0, 8, 0));
            row.setStyle("-fx-border-color: transparent transparent #EDF2F7 transparent; -fx-border-width: 0 0 1 0;");

            Label icon = new Label("💊");
            icon.setStyle("-fx-font-size: 16px; -fx-text-fill: #A0AEC0;");

            VBox details = new VBox(2);
            Label name = new Label(p.getProductName() != null ? p.getProductName() : "—");
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2D3748;");
            String dateStr = p.getLastUpdateDate() != null
                    ? p.getLastUpdateDate().toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "Date inconnue";
            Label sub = new Label("Qté: " + p.getAvailableQuantity() + "  •  " + dateStr);
            sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
            details.getChildren().addAll(name, sub);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label priceLbl = new Label(String.format("%.2f DT", p.getPrice()));
            priceLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #4A6CF7;");

            row.getChildren().addAll(icon, details, spacer, priceLbl);
            recentUpdatesContainer.getChildren().add(row);
        }
    }

    // ── Navigation ────────────────────────────────────────────────
    @FXML private void goToStock()    { Main.showPharmacyStock(); }
    @FXML private void goToAddStock() { Main.showPharmacyAddStock(); }
    @FXML private void goToEvents()   { Main.showPharmacistEvents(); }
}
