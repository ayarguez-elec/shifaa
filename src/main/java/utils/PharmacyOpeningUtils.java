package utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PharmacyOpeningUtils {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static PharmacyStatus calculateStatus(String openingHours) {
        if (openingHours == null || openingHours.trim().isEmpty()) {
            return new PharmacyStatus(
                    false,
                    "Horaires non renseignés",
                    "Non défini",
                    "Service non défini",
                    "status-closed"
            );
        }

        try {
            String cleanHours = openingHours.trim().replace(" ", "");

            if (!cleanHours.contains("-")) {
                return new PharmacyStatus(
                        false,
                        "Horaires invalides",
                        "Format attendu : 08:00-20:00",
                        "Service non défini",
                        "status-closed"
                );
            }

            String[] parts = cleanHours.split("-");

            if (parts.length != 2) {
                return new PharmacyStatus(
                        false,
                        "Horaires invalides",
                        "Format attendu : 08:00-20:00",
                        "Service non défini",
                        "status-closed"
                );
            }

            LocalTime openTime = LocalTime.parse(normalizeHour(parts[0]), TIME_FORMATTER);
            LocalTime closeTime = LocalTime.parse(normalizeHour(parts[1]), TIME_FORMATTER);
            LocalTime now = LocalTime.now();

            boolean isNightService = closeTime.isBefore(openTime);
            boolean isOpen;

            if (isNightService) {
                isOpen = !now.isBefore(openTime) || now.isBefore(closeTime);
            } else {
                isOpen = !now.isBefore(openTime) && now.isBefore(closeTime);
            }

            String serviceType = isNightService ? "Service : Nuit" : "Service : Jour";

            if (isOpen) {
                return new PharmacyStatus(
                        true,
                        "Ouverte maintenant",
                        "Ferme à " + closeTime.format(TIME_FORMATTER),
                        serviceType,
                        isNightService ? "status-duty" : "status-active"
                );
            } else {
                return new PharmacyStatus(
                        false,
                        "Fermée maintenant",
                        "Ouvre à " + openTime.format(TIME_FORMATTER),
                        serviceType,
                        "status-closed"
                );
            }

        } catch (Exception e) {
            return new PharmacyStatus(
                    false,
                    "Horaires invalides",
                    "Format attendu : 08:00-20:00",
                    "Service non défini",
                    "status-closed"
            );
        }
    }

    private static String normalizeHour(String value) {
        String hour = value.trim();

        if (hour.matches("\\d{1}:\\d{2}")) {
            hour = "0" + hour;
        }

        return hour;
    }

    public static class PharmacyStatus {
        private final boolean open;
        private final String mainStatus;
        private final String detailStatus;
        private final String serviceType;
        private final String styleClass;

        public PharmacyStatus(boolean open, String mainStatus, String detailStatus, String serviceType, String styleClass) {
            this.open = open;
            this.mainStatus = mainStatus;
            this.detailStatus = detailStatus;
            this.serviceType = serviceType;
            this.styleClass = styleClass;
        }

        public boolean isOpen() {
            return open;
        }

        public String getMainStatus() {
            return mainStatus;
        }

        public String getDetailStatus() {
            return detailStatus;
        }

        public String getServiceType() {
            return serviceType;
        }

        public String getStyleClass() {
            return styleClass;
        }
    }
}