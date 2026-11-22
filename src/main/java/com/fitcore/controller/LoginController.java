package com.fitcore.controller;

import com.fitcore.service.AuthService;
import com.fitcore.util.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label feedbackLabel;

    @FXML private void login() {
        try {
            String name = new AuthService().authenticate(emailField.getText(), passwordField.getText());
            if (name == null) {
                feedbackLabel.setText("Invalid email or password.");
                return;
            }
            Stage stage = (Stage) emailField.getScene().getWindow();
            Navigation.dashboard(stage, name);
        } catch (Exception e) {
            feedbackLabel.setText("Cannot sign in: " + e.getMessage());
        }
    }
}
