package controller;

import entities.Product;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import services.ProductServices;
import tests.MainFx;

import java.io.File;
import java.text.Normalizer;
import java.util.*;

public class AdminProductController {

    @FXML private FlowPane productContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private Label totalLabel;
    @FXML private Label pageLabel;

    private ProductServices productServices;

    private List<Product> products = new ArrayList<>();
    private List<ProductGroup> groupedProducts = new ArrayList<>();
    private List<ProductGroup> displayedGroups = new ArrayList<>();

    private int currentPage = 0;
    private final int itemsPerPage = 6;

    @FXML
    public void initialize() {
        productServices = new ProductServices();

        setupCategoryCombo();
        loadProducts();

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> filterProducts());
        }

        if (categoryCombo != null) {
            categoryCombo.valueProperty().addListener((obs, oldValue, newValue) -> filterProducts());
        }
    }

    private void setupCategoryCombo() {
        if (categoryCombo != null) {
            categoryCombo.setItems(FXCollections.observableArrayList("Toutes les catégories"));
            categoryCombo.setValue("Toutes les catégories");
            categoryCombo.setVisibleRowCount(6);
        }
    }

    private void loadProducts() {
        try {
            products = productServices.getAll();

            groupedProducts = groupProducts(products);
            displayedGroups = new ArrayList<>(groupedProducts);

            loadCategories();

            currentPage = 0;
            showCurrentPage();

        } catch (Exception e) {
            e.printStackTrace();

            if (totalLabel != null) {
                totalLabel.setText("Erreur : impossible de charger les produits.");
            }
        }
    }

    private void loadCategories() {
        if (categoryCombo == null) {
            return;
        }

        Set<String> categories = new TreeSet<>();
        categories.add("Toutes les catégories");

        for (ProductGroup group : groupedProducts) {
            String category = safe(group.mainProduct.getCategory());

            if (!category.isEmpty()) {
                categories.add(category);
            }
        }

        categoryCombo.setItems(FXCollections.observableArrayList(categories));
        categoryCombo.setValue("Toutes les catégories");
    }

    private List<ProductGroup> groupProducts(List<Product> productList) {
        Map<String, ProductGroup> map = new LinkedHashMap<>();

        for (Product product : productList) {
            String key = normalizeText(product.getProductName()) + "|" + normalizeText(product.getDosage());

            if (!map.containsKey(key)) {
                map.put(key, new ProductGroup(product));
            }

            map.get(key).items.add(product);
        }

        return new ArrayList<>(map.values());
    }

    private void filterProducts() {
        currentPage = 0;

        String keyword = searchField == null ? "" : normalizeText(searchField.getText());
        String selectedCategory = categoryCombo == null ? "Toutes les catégories" : categoryCombo.getValue();

        List<ProductGroup> filtered = new ArrayList<>();

        for (ProductGroup group : groupedProducts) {
            Product product = group.mainProduct;

            String name = normalizeText(product.getProductName());
            String description = normalizeText(product.getDescription());
            String dosage = normalizeText(product.getDosage());
            String category = normalizeText(product.getCategory());
            String laboratory = normalizeText(product.getLaboratory());
            String therapeuticClass = normalizeText(product.getTherapeuticClass());

            boolean matchKeyword = keyword.isEmpty()
                    || name.contains(keyword)
                    || description.contains(keyword)
                    || dosage.contains(keyword)
                    || category.contains(keyword)
                    || laboratory.contains(keyword)
                    || therapeuticClass.contains(keyword);

            boolean matchCategory = selectedCategory == null
                    || selectedCategory.equals("Toutes les catégories")
                    || category.equals(normalizeText(selectedCategory));

            if (matchKeyword && matchCategory) {
                filtered.add(group);
            }
        }

        displayedGroups = filtered;
        showCurrentPage();
    }

    private void showCurrentPage() {
        if (productContainer == null) {
            return;
        }

        productContainer.getChildren().clear();

        productContainer.setHgap(24);
        productContainer.setVgap(24);
        productContainer.setPadding(new Insets(20, 0, 95, 0));

        if (displayedGroups == null || displayedGroups.isEmpty()) {
            VBox emptyBox = new VBox();
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setSpacing(8);
            emptyBox.setPrefWidth(900);
            emptyBox.setPrefHeight(250);

            Label title = new Label("Aucun produit trouvé.");
            title.setStyle(
                    "-fx-text-fill: #0F172A;" +
                            "-fx-font-size: 20px;" +
                            "-fx-font-weight: bold;"
            );

            Label text = new Label("Essayez un autre filtre.");
            text.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px;");

            emptyBox.getChildren().addAll(title, text);
            productContainer.getChildren().add(emptyBox);

            if (totalLabel != null) {
                totalLabel.setText("Total : 0 produit");
            }

            if (pageLabel != null) {
                pageLabel.setText("Page 0 / 0");
            }

            return;
        }

        int totalItems = displayedGroups.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

        if (currentPage < 0) {
            currentPage = 0;
        }

        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }

        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, totalItems);

        int index = 0;

        for (ProductGroup group : displayedGroups.subList(start, end)) {
            VBox card = createProductCard(group);
            productContainer.getChildren().add(card);
            playCardEntrance(card, index);
            index++;
        }

        if (totalLabel != null) {
            totalLabel.setText("Total : " + totalItems + " produits");
        }

        if (pageLabel != null) {
            pageLabel.setText("Page " + (currentPage + 1) + " / " + totalPages);
        }
    }

    private VBox createProductCard(ProductGroup group) {
        Product product = group.mainProduct;

        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(18));
        card.setPrefWidth(300);
        card.setMinWidth(300);
        card.setMaxWidth(300);
        card.setMinHeight(520);
        card.setAlignment(Pos.TOP_LEFT);

        card.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-radius: 20;"
        );

        DropShadow normalShadow = new DropShadow();
        normalShadow.setColor(Color.web("#000000", 0.06));
        normalShadow.setRadius(24);
        normalShadow.setOffsetY(8);
        card.setEffect(normalShadow);

        StackPane imageFrame = createImageFrame(product);

        Label name = new Label(safe(product.getProductName()));
        name.setWrapText(true);
        name.setMaxWidth(Double.MAX_VALUE);
        name.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #0F172A;"
        );

        Label description = new Label(safe(product.getDescription()));
        description.setWrapText(true);
        description.setMaxHeight(42);
        description.setStyle(
                "-fx-text-fill: #64748B;" +
                        "-fx-font-size: 13px;" +
                        "-fx-line-spacing: 4;"
        );

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(26);
        detailsGrid.setVgap(8);
        detailsGrid.setMaxWidth(Double.MAX_VALUE);

        addInfo(detailsGrid, "Dosage", safe(product.getDosage()), 0, 0);
        addInfo(detailsGrid, "Prix", formatPrice(product.getPrice()) + " DT", 0, 1);
        addInfo(detailsGrid, "Catégorie", safe(product.getCategory()), 1, 0);
        addInfo(detailsGrid, "Quantité", String.valueOf(product.getAvailableQuantity()), 1, 1);
        addInfo(detailsGrid, "Laboratoire", safe(product.getLaboratory()), 2, 0);
        addInfo(detailsGrid, "Classe", safe(product.getTherapeuticClass()), 2, 1);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button editButton = new Button("Modifier");
        editButton.setMaxWidth(Double.MAX_VALUE);
        editButton.setPrefHeight(44);
        editButton.setStyle(
                "-fx-background-color: #0F172A;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
        );
        editButton.setOnAction(event -> openUpdateProduct(product));

        Button deleteButton = new Button("Supprimer tout");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setPrefHeight(40);
        deleteButton.setStyle(
                "-fx-background-color: #FEE2E2;" +
                        "-fx-text-fill: #B91C1C;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
        );
        deleteButton.setOnAction(event -> deleteProductGroup(group));

        setupButtonHover(editButton);
        setupButtonHover(deleteButton);
        setupCardHover(card);

        card.getChildren().addAll(
                imageFrame,
                name,
                description,
                detailsGrid,
                spacer,
                editButton,
                deleteButton
        );

        return card;
    }

    private void addInfo(GridPane grid, String titleText, String valueText, int row, int col) {
        VBox box = new VBox();
        box.setSpacing(2);
        box.setPrefWidth(110);

        Label title = new Label(titleText);
        title.setStyle(
                "-fx-text-fill: #0F172A;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        Label value = new Label(valueText == null || valueText.trim().isEmpty() ? "-" : valueText);
        value.setWrapText(true);
        value.setStyle(
                "-fx-text-fill: #0F172A;" +
                        "-fx-font-size: 12px;"
        );

        box.getChildren().addAll(title, value);
        grid.add(box, col, row);
    }

    private StackPane createImageFrame(Product product) {
        StackPane imageFrame = new StackPane();
        imageFrame.setPrefWidth(260);
        imageFrame.setPrefHeight(145);
        imageFrame.setStyle(
                "-fx-background-color: #EFF6FF;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #DBEAFE;" +
                        "-fx-border-radius: 16;"
        );

        ImageView imageView = new ImageView();
        imageView.setFitWidth(230);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        if (loadProductImage(product, imageView)) {
            imageFrame.getChildren().add(imageView);
        } else {
            Label icon = new Label("💊");
            icon.setStyle(
                    "-fx-font-size: 38px;" +
                            "-fx-text-fill: #94A3B8;"
            );
            imageFrame.getChildren().add(icon);
        }

        return imageFrame;
    }

    private boolean loadProductImage(Product product, ImageView imageView) {
        try {
            String imagePath = product.getImage();

            if (imagePath != null && !imagePath.trim().isEmpty()) {
                File file = new File(imagePath);

                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                    return true;
                }
            }

        } catch (Exception e) {
            System.out.println("Erreur chargement image produit : " + e.getMessage());
        }

        return false;
    }

    private void setupCardHover(VBox card) {
        card.setOnMouseEntered(event -> {
            TranslateTransition translate = new TranslateTransition(Duration.millis(300), card);
            translate.setToY(-4);

            DropShadow hoverShadow = new DropShadow();
            hoverShadow.setColor(Color.web("#000000", 0.10));
            hoverShadow.setRadius(32);
            hoverShadow.setOffsetY(12);
            card.setEffect(hoverShadow);

            translate.play();
        });

        card.setOnMouseExited(event -> {
            TranslateTransition translate = new TranslateTransition(Duration.millis(300), card);
            translate.setToY(0);

            DropShadow normalShadow = new DropShadow();
            normalShadow.setColor(Color.web("#000000", 0.06));
            normalShadow.setRadius(24);
            normalShadow.setOffsetY(8);
            card.setEffect(normalShadow);

            translate.play();
        });
    }

    private void setupButtonHover(Button button) {
        button.setOnMouseEntered(event -> {
            TranslateTransition translate = new TranslateTransition(Duration.millis(200), button);
            translate.setToY(-2);
            translate.play();
        });

        button.setOnMouseExited(event -> {
            TranslateTransition translate = new TranslateTransition(Duration.millis(200), button);
            translate.setToY(0);
            translate.play();
        });
    }

    private void playCardEntrance(Node node, int index) {
        node.setOpacity(0);
        node.setTranslateY(20);

        FadeTransition fade = new FadeTransition(Duration.millis(350), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(index * 80));

        TranslateTransition translate = new TranslateTransition(Duration.millis(350), node);
        translate.setFromY(20);
        translate.setToY(0);
        translate.setDelay(Duration.millis(index * 80));

        new ParallelTransition(fade, translate).play();
    }

    private void openUpdateProduct(Product product) {
        if (product == null) {
            return;
        }

        AdminUpdateProductController.setSelectedProduct(product);
        MainFx.changeScene("/adminupdateproduct.fxml");
    }

    private void deleteProductGroup(ProductGroup group) {
        if (group == null || group.items == null || group.items.isEmpty()) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le produit");
        confirm.setContentText("Voulez-vous vraiment supprimer ce produit ?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (Product product : group.items) {
                    productServices.delete(product);
                }

                loadProducts();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit supprimé avec succès.");

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le produit.");
            }
        }
    }

    @FXML
    public void resetFilters() {
        if (searchField != null) {
            searchField.clear();
        }

        if (categoryCombo != null) {
            categoryCombo.setValue("Toutes les catégories");
        }

        displayedGroups = new ArrayList<>(groupedProducts);
        currentPage = 0;
        showCurrentPage();
    }

    @FXML
    public void nextPage() {
        int totalPages = (int) Math.ceil((double) displayedGroups.size() / itemsPerPage);

        if (currentPage < totalPages - 1) {
            currentPage++;
            showCurrentPage();
        }
    }

    @FXML
    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            showCurrentPage();
        }
    }

    @FXML
    public void goDashboard() {
        MainFx.changeScene("/admindashboard.fxml");
    }

    @FXML
    public void goProducts() {
        MainFx.changeScene("/adminproducts.fxml");
    }

    @FXML
    public void goPharmacies() {
        MainFx.changeScene("/adminpharmacies.fxml");
    }

    @FXML
    public void goAddProduct() {
        MainFx.changeScene("/adminaddproduct.fxml");
    }

    private String formatPrice(double price) {
        return String.format("%.1f", price);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        String result = value.trim().toLowerCase();
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = result.replaceAll("\\p{M}", "");
        result = result.replaceAll("\\s+", " ");

        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class ProductGroup {
        Product mainProduct;
        List<Product> items = new ArrayList<>();

        ProductGroup(Product mainProduct) {
            this.mainProduct = mainProduct;
        }
    }
}