package com.fitcore.util;
import javafx.scene.control.Alert;
public final class Alerts {
    private Alerts() { }
    public static void error(String message) { show(Alert.AlertType.ERROR, "Error", message); }
    public static void info(String message) { show(Alert.AlertType.INFORMATION, "Success", message); }
    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
