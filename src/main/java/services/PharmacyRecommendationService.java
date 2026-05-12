package services;

import entities.Pharmacy;

import java.text.Normalizer;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PharmacyRecommendationService {

    private final PharmacyServices pharmacyServices;
    private final Map<String, double[]> locationCoordinates = new HashMap<>();

    public PharmacyRecommendationService() {
        this.pharmacyServices = new PharmacyServices();
        initializeLocationCoordinates();
    }

    private void initializeLocationCoordinates() {
        locationCoordinates.put("tunis", new double[]{36.8065, 10.1815});
        locationCoordinates.put("tunis centre", new double[]{36.8008, 10.1800});
        locationCoordinates.put("avenue habib bourguiba tunis", new double[]{36.8008, 10.1800});
        locationCoordinates.put("rue de marseille tunis", new double[]{36.8000, 10.1840});

        locationCoordinates.put("ariana", new double[]{36.8665, 10.1647});
        locationCoordinates.put("ariana centre", new double[]{36.8665, 10.1647});
        locationCoordinates.put("ariana soghra", new double[]{36.8625, 10.1956});
        locationCoordinates.put("cite ennasr ariana", new double[]{36.8610, 10.1640});
        locationCoordinates.put("ennasr ariana", new double[]{36.8610, 10.1640});
        locationCoordinates.put("riadh andalous ariana", new double[]{36.8660, 10.1870});
        locationCoordinates.put("avenue hedi nouira ariana", new double[]{36.8620, 10.1680});

        locationCoordinates.put("sousse", new double[]{35.8256, 10.6370});
        locationCoordinates.put("sousse centre", new double[]{35.8256, 10.6370});
        locationCoordinates.put("rue de la republique sousse", new double[]{35.8256, 10.6370});

        locationCoordinates.put("sfax", new double[]{34.7406, 10.7603});
        locationCoordinates.put("sfax centre", new double[]{34.7406, 10.7603});
        locationCoordinates.put("route de tunis sfax", new double[]{34.7600, 10.7600});

        locationCoordinates.put("monastir", new double[]{35.7643, 10.8113});
        locationCoordinates.put("monastir centre", new double[]{35.7643, 10.8113});

        locationCoordinates.put("kairouan", new double[]{35.6781, 10.0963});
        locationCoordinates.put("kairouan centre", new double[]{35.6781, 10.0963});

        locationCoordinates.put("nabeul", new double[]{36.4513, 10.7357});
        locationCoordinates.put("bizerte", new double[]{37.2744, 9.8739});
        locationCoordinates.put("beja", new double[]{36.7333, 9.1833});
        locationCoordinates.put("gabes", new double[]{33.8815, 10.0982});
        locationCoordinates.put("medenine", new double[]{33.3549, 10.5055});
        locationCoordinates.put("gafsa", new double[]{34.4250, 8.7842});
    }

    public List<PharmacyRecommendation> recommendNearestPharmacies(String userLocation) {
        List<PharmacyRecommendation> recommendations = new ArrayList<>();

        if (userLocation == null || userLocation.trim().isEmpty()) {
            return recommendations;
        }

        try {
            List<Pharmacy> pharmacies = pharmacyServices.getAll();

            String cleanedUserLocation = cleanLocation(userLocation);
            String normalizedUserLocation = normalize(cleanedUserLocation);

            double[] userPoint = findBestCoordinates(normalizedUserLocation);
            String userCity = detectMainCity(normalizedUserLocation);

            if (userCity.isEmpty()) {
                userCity = detectMainCityFromCoordinatesKey(normalizedUserLocation);
            }

            for (Pharmacy pharmacy : pharmacies) {

                String pharmacyName = safe(pharmacy.getPharmacyName());
                String pharmacyAddress = safe(pharmacy.getAddress());
                String pharmacyStatus = safe(pharmacy.getPharmacyStatus());

                String pharmacyText = normalize(pharmacyName + " " + pharmacyAddress + " " + pharmacyStatus);

                String pharmacyCity = detectMainCity(pharmacyText);

                boolean sameCity = !userCity.isEmpty()
                        && !pharmacyCity.isEmpty()
                        && pharmacyCity.equals(userCity);

                boolean textMatch = containsUsefulWord(normalizedUserLocation, pharmacyText);

                if (!sameCity && !textMatch) {
                    continue;
                }

                double[] pharmacyPoint = findBestCoordinates(pharmacyText);

                if (pharmacyPoint == null && !pharmacyCity.isEmpty()) {
                    pharmacyPoint = locationCoordinates.get(pharmacyCity);
                }

                if (userPoint == null && !userCity.isEmpty()) {
                    userPoint = locationCoordinates.get(userCity);
                }

                double distanceKm;

                if (userPoint != null && pharmacyPoint != null) {
                    distanceKm = haversine(userPoint[0], userPoint[1], pharmacyPoint[0], pharmacyPoint[1]);
                } else if (sameCity) {
                    distanceKm = 1.0;
                } else {
                    continue;
                }

                PharmacyRecommendation recommendation = new PharmacyRecommendation();
                recommendation.setPharmacy(pharmacy);
                recommendation.setUserCity(userCity);
                recommendation.setPharmacyCity(pharmacyCity);
                recommendation.setEstimatedDistanceKm(distanceKm);
                recommendation.setFinalScore(calculateFinalScore(distanceKm, pharmacy));
                recommendation.setReason("");

                recommendations.add(recommendation);
            }

            recommendations.sort(Comparator.comparingDouble(PharmacyRecommendation::getEstimatedDistanceKm));

            if (recommendations.size() > 3) {
                return new ArrayList<>(recommendations.subList(0, 3));
            }

            return recommendations;

        } catch (Exception e) {
            e.printStackTrace();
            return recommendations;
        }
    }

    private boolean containsUsefulWord(String userLocation, String pharmacyText) {
        String[] words = userLocation.split(" ");

        for (String word : words) {
            if (word.length() >= 4 && pharmacyText.contains(word)) {
                return true;
            }
        }

        return false;
    }

    private double calculateFinalScore(double distanceKm, Pharmacy pharmacy) {
        double score = 100;

        score -= distanceKm * 8;

        String status = normalize(pharmacy.getPharmacyStatus());

        if (status.contains("garde") || status.contains("nuit")) {
            score += 15;
        }

        if (status.contains("jour") || status.contains("ouverte") || status.contains("ouvert")) {
            score += 8;
        }

        if (isOpenNow(pharmacy)) {
            score += 10;
        }

        return Math.max(score, 0);
    }

    private double[] findBestCoordinates(String text) {
        String normalized = normalize(text);

        String bestKey = "";
        double[] bestPoint = null;

        for (Map.Entry<String, double[]> entry : locationCoordinates.entrySet()) {
            String key = normalize(entry.getKey());

            if (normalized.contains(key)) {
                if (key.length() > bestKey.length()) {
                    bestKey = key;
                    bestPoint = entry.getValue();
                }
            }
        }

        return bestPoint;
    }

    private String detectMainCityFromCoordinatesKey(String text) {
        String normalized = normalize(text);

        for (String key : locationCoordinates.keySet()) {
            String normalizedKey = normalize(key);

            if (normalized.contains(normalizedKey)) {
                return detectMainCity(normalizedKey);
            }
        }

        return "";
    }

    private String detectMainCity(String text) {
        String normalized = normalize(text);

        String[] cities = {
                "tunis", "ariana", "sousse", "sfax", "monastir", "kairouan",
                "nabeul", "bizerte", "beja", "gabes", "medenine", "gafsa"
        };

        for (String city : cities) {
            if (normalized.contains(city)) {
                return city;
            }
        }

        return "";
    }

    public String extractLocationFromMessage(String message) {
        String msg = normalize(message);

        String[] connectors = {
                "je suis a",
                "je suis dans",
                "proche de",
                "pres de",
                "dans",
                "ville",
                "adresse"
        };

        for (String connector : connectors) {
            String normalizedConnector = normalize(connector);

            if (msg.startsWith(normalizedConnector + " ")) {
                String after = msg.substring(normalizedConnector.length()).trim();
                return cleanLocation(after);
            }

            String pattern = " " + normalizedConnector + " ";
            int index = msg.indexOf(pattern);

            if (index >= 0) {
                String after = msg.substring(index + pattern.length()).trim();
                return cleanLocation(after);
            }
        }

        return cleanLocation(msg);
    }

    private String cleanLocation(String location) {
        String result = normalize(location);

        result = result.replace("pharmacie", "");
        result = result.replace("ouverte", "");
        result = result.replace("ouvert", "");
        result = result.replace("proche", "");
        result = result.replace("pres", "");
        result = result.replace("plus", "");
        result = result.replace("recommandee", "");
        result = result.replace("recommande", "");
        result = result.replace("maintenant", "");
        result = result.trim();

        return result;
    }

    private boolean isOpenNow(Pharmacy pharmacy) {
        String status = normalize(pharmacy.getPharmacyStatus());

        if (status.contains("nuit") || status.contains("garde")) {
            return true;
        }

        String hours = normalize(pharmacy.getOpeningHours());

        if (hours.isEmpty()) {
            return status.contains("jour") || status.contains("ouverte") || status.contains("ouvert");
        }

        LocalTime now = LocalTime.now();

        try {
            Pattern pattern = Pattern.compile("(\\d{1,2})[:h](\\d{2})?\\s*[-a]\\s*(\\d{1,2})[:h](\\d{2})?");
            Matcher matcher = pattern.matcher(hours);

            if (matcher.find()) {
                int startHour = Integer.parseInt(matcher.group(1));
                int startMinute = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));

                int endHour = Integer.parseInt(matcher.group(3));
                int endMinute = matcher.group(4) == null ? 0 : Integer.parseInt(matcher.group(4));

                LocalTime start = LocalTime.of(startHour, startMinute);
                LocalTime end = LocalTime.of(endHour, endMinute);

                if (end.isAfter(start)) {
                    return !now.isBefore(start) && !now.isAfter(end);
                } else {
                    return now.isAfter(start) || now.isBefore(end);
                }
            }

        } catch (Exception ignored) {
        }

        return status.contains("jour") || status.contains("ouverte") || status.contains("ouvert");
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int earthRadiusKm = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        String result = value.trim().toLowerCase();
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = result.replaceAll("\\p{M}", "");
        result = result.replaceAll("[^a-z0-9\\s]", " ");
        result = result.replaceAll("\\s+", " ");

        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public static class PharmacyRecommendation {
        private Pharmacy pharmacy;
        private String userCity;
        private String pharmacyCity;
        private double estimatedDistanceKm;
        private double finalScore;
        private String reason;

        public Pharmacy getPharmacy() {
            return pharmacy;
        }

        public void setPharmacy(Pharmacy pharmacy) {
            this.pharmacy = pharmacy;
        }

        public String getUserCity() {
            return userCity;
        }

        public void setUserCity(String userCity) {
            this.userCity = userCity;
        }

        public String getPharmacyCity() {
            return pharmacyCity;
        }

        public void setPharmacyCity(String pharmacyCity) {
            this.pharmacyCity = pharmacyCity;
        }

        public double getEstimatedDistanceKm() {
            return estimatedDistanceKm;
        }

        public void setEstimatedDistanceKm(double estimatedDistanceKm) {
            this.estimatedDistanceKm = estimatedDistanceKm;
        }

        public double getFinalScore() {
            return finalScore;
        }

        public void setFinalScore(double finalScore) {
            this.finalScore = finalScore;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}