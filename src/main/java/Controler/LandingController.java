package Controler;

import javafx.fxml.FXML;
import tests.Main;

public class LandingController {

    @FXML
    private void goToLogin() {
        Main.showLoginPage();
    }

    @FXML
    private void goToSignUp() {
        Main.showSignUpPage();
    }
}
