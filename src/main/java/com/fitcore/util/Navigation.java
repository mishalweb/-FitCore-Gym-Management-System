package com.fitcore.util;

import com.fitcore.controller.DashboardController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class Navigation {
    private Navigation() { }
    public static void login(Stage stage) throws IOException {
        show(stage, "/fxml/login.fxml", "FitCore — Login", 980, 660, null);
    }
    public static void dashboard(Stage stage, String adminName) throws IOException {
        show(stage, "/fxml/dashboard.fxml", "FitCore — Gym Management", 1320, 830, adminName);
    }
    private static void show(Stage stage, String resource, String title, int width, int height,
                             String adminName) throws IOException {
        FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(resource));
        Parent root = loader.load();
        if (adminName != null && loader.getController() instanceof DashboardController controller)
            controller.setAdminName(adminName);
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(Navigation.class.getResource("/css/dark.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setMinWidth(920);
        stage.setMinHeight(620);
        stage.show();
    }
}
