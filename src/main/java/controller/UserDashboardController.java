package controller;

import javafx.fxml.FXML;
import tests.MainFx;

public class UserDashboardController {

    @FXML
    public void initialize() {
        System.out.println("User dashboard chargé");
    }

    @FXML
    public void goDashboard() {
        MainFx.changeScene("/userdashboard.fxml");
    }

    @FXML
    public void goProducts() {
        MainFx.changeScene("/userproducts.fxml");
    }

    @FXML
    public void goPharmacies() {
        MainFx.changeScene("/userpharmacies.fxml");
    }
}