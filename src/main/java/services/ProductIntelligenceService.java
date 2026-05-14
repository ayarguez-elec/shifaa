package services;

import entities.Pharmacy;
import entities.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductIntelligenceService {

    private final ProductServices productServices;
    private final PharmacyServices pharmacyServices;

    public ProductIntelligenceService() {
        this.productServices = new ProductServices();
        this.pharmacyServices = new PharmacyServices();
    }

    public int calculateAvailabilityScore(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return 0;
        }

        int totalQuantity = 0;
        int availablePharmacies = 0;

        for (Product product : products) {
            int quantity = product.getAvailableQuantity();
            totalQuantity += quantity;

            if (quantity > 0) {
                availablePharmacies++;
            }
        }

        int quantityScore = calculateQuantityScore(totalQuantity);
        int pharmacyScore = calculatePharmacyScore(availablePharmacies);

        int finalScore = (quantityScore * 70 + pharmacyScore * 30) / 100;

        return Math.min(finalScore, 100);
    }

    private int calculateQuantityScore(int totalQuantity) {
        if (totalQuantity <= 0) {
            return 0;
        }

        if (totalQuantity <= 5) {
            return 25;
        }

        if (totalQuantity <= 15) {
            return 50;
        }

        if (totalQuantity <= 30) {
            return 75;
        }

        return 100;
    }

    private int calculatePharmacyScore(int availablePharmacies) {
        if (availablePharmacies <= 0) {
            return 0;
        }

        if (availablePharmacies == 1) {
            return 30;
        }

        if (availablePharmacies == 2) {
            return 60;
        }

        return 100;
    }

    public String getAvailabilityLabel(int score) {
        if (score <= 0) {
            return "Rupture";
        }

        if (score < 40) {
            return "Stock faible";
        }

        if (score < 75) {
            return "Disponible";
        }

        return "Très disponible";
    }

    public int getTotalQuantity(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return 0;
        }

        int total = 0;

        for (Product product : products) {
            total += product.getAvailableQuantity();
        }

        return total;
    }

    public int getAvailablePharmacyCount(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return 0;
        }

        int count = 0;

        for (Product product : products) {
            if (product.getAvailableQuantity() > 0) {
                count++;
            }
        }

        return count;
    }

    public Pharmacy recommendBestPharmacy(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return null;
        }

        try {
            List<Pharmacy> pharmacies = pharmacyServices.getAll();

            Product bestProduct = null;
            int bestScore = -1;

            for (Product product : products) {
                Pharmacy pharmacy = findPharmacyById(pharmacies, product.getPharmacyId());

                if (pharmacy == null) {
                    continue;
                }

                int score = calculatePharmacyRecommendationScore(product, pharmacy);

                if (score > bestScore) {
                    bestScore = score;
                    bestProduct = product;
                }
            }

            if (bestProduct == null) {
                return null;
            }

            return findPharmacyById(pharmacies, bestProduct.getPharmacyId());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Product getBestProductAvailability(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return null;
        }

        Product bestProduct = null;
        int bestQuantity = -1;

        for (Product product : products) {
            if (product.getAvailableQuantity() > bestQuantity) {
                bestQuantity = product.getAvailableQuantity();
                bestProduct = product;
            }
        }

        return bestProduct;
    }

    private int calculatePharmacyRecommendationScore(Product product, Pharmacy pharmacy) {
        int score = 0;

        int quantity = product.getAvailableQuantity();

        if (quantity > 30) {
            score += 50;
        } else if (quantity > 10) {
            score += 35;
        } else if (quantity > 0) {
            score += 20;
        }

        String status = safe(pharmacy.getPharmacyStatus()).toLowerCase();

        if (status.contains("garde") || status.contains("nuit")) {
            score += 25;
        } else if (status.contains("jour")) {
            score += 15;
        }

        String address = safe(pharmacy.getAddress());

        if (!address.isEmpty()) {
            score += 15;
        }

        String phone = safe(pharmacy.getPhone());

        if (!phone.isEmpty()) {
            score += 10;
        }

        return score;
    }

    public String buildRecommendationReason(Product product, Pharmacy pharmacy) {
        if (product == null || pharmacy == null) {
            return "Aucune recommandation disponible.";
        }

        StringBuilder reason = new StringBuilder();

        reason.append("Raison : ");

        if (product.getAvailableQuantity() > 10) {
            reason.append("stock suffisant");
        } else if (product.getAvailableQuantity() > 0) {
            reason.append("produit disponible mais quantité limitée");
        } else {
            reason.append("produit non disponible");
        }

        if (pharmacy.getAddress() != null && !pharmacy.getAddress().trim().isEmpty()) {
            reason.append(" + adresse disponible pour la carte");
        }

        if (pharmacy.getPharmacyStatus() != null && !pharmacy.getPharmacyStatus().trim().isEmpty()) {
            reason.append(" + statut pharmacie : ").append(pharmacy.getPharmacyStatus());
        }

        return reason.toString();
    }

    public List<Product> getLowStockProducts() {
        List<Product> result = new ArrayList<>();

        try {
            List<Product> products = productServices.getAll();

            for (Product product : products) {
                if (product.getAvailableQuantity() > 0 && product.getAvailableQuantity() <= 10) {
                    result.add(product);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<Product> getOutOfStockProducts() {
        List<Product> result = new ArrayList<>();

        try {
            List<Product> products = productServices.getAll();

            for (Product product : products) {
                if (product.getAvailableQuantity() <= 0) {
                    result.add(product);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public int countLowStockProducts() {
        return getLowStockProducts().size();
    }

    public int countOutOfStockProducts() {
        return getOutOfStockProducts().size();
    }

    private Pharmacy findPharmacyById(List<Pharmacy> pharmacies, int pharmacyId) {
        for (Pharmacy pharmacy : pharmacies) {
            if (pharmacy.getPharmacyId() == pharmacyId) {
                return pharmacy;
            }
        }

        return null;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
