package controller;

import javafx.fxml.FXML;
import tests.MainFx;

public class AdminDashboardController {

    @FXML
    public void initialize() {
        System.out.println("Admin dashboard chargé");
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

    @FXML
    public void goAddPharmacy() {
        MainFx.changeScene("/adminaddpharmacy.fxml");
    }

    @FXML
    public void goUserDashboard() {
        MainFx.changeScene("/userdashboard.fxml");
    }
}