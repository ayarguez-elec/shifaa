package services;

import entities.Pharmacy;
import services.PharmacyRecommendationService.PharmacyRecommendation;

import java.util.ArrayList;
import java.util.List;

public class AssistantProximiteService {

    private final PharmacyRecommendationService recommendationService;

    private String lastUserLocation;
    private List<PharmacyRecommendation> lastRecommendations;

    public AssistantProximiteService() {
        this.recommendationService = new PharmacyRecommendationService();
        this.lastUserLocation = "";
        this.lastRecommendations = new ArrayList<>();
    }

    public String rechercherPharmaciesProches(String userMessage) {
        lastUserLocation = "";
        lastRecommendations = new ArrayList<>();

        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Veuillez saisir votre emplacement.";
        }

        String location = recommendationService.extractLocationFromMessage(userMessage);

        if (location == null || location.trim().isEmpty()) {
            return "Veuillez saisir votre emplacement. Exemple : Ariana.";
        }

        List<PharmacyRecommendation> recommendations =
                recommendationService.recommendNearestPharmacies(location);

        if (recommendations == null || recommendations.isEmpty()) {
            lastUserLocation = location;
            return "Aucune pharmacie proche trouvée pour : " + location;
        }

        lastUserLocation = location;
        lastRecommendations = recommendations;

        return "Résultats trouvés.";
    }

    public String getLastUserLocation() {
        return lastUserLocation;
    }

    public List<PharmacyRecommendation> getLastRecommendations() {
        return lastRecommendations;
    }
}
