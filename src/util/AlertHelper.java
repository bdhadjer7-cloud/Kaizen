package util;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class AlertHelper {

    public static void showError(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #E53E3E; -fx-font-size: 12px; -fx-font-weight: bold;");
        label.setVisible(true);
        label.setManaged(true);
        shake(label);
    }

    public static void showSuccess(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #48BB78; -fx-font-size: 12px; -fx-font-weight: bold;");
        label.setVisible(true);
        label.setManaged(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(300), label);
            fade.setToValue(0);
            fade.setOnFinished(ev -> { label.setVisible(false); label.setManaged(false); });
            fade.play();
        });
        pause.play();
    }

    public static void showToast(StackPane container, String message, boolean isError) {
        HBox toast = new HBox();
        toast.setAlignment(Pos.CENTER);
        toast.setStyle("-fx-background-color: " + (isError ? "#E53E3E" : "#48BB78") + ";"
                + "-fx-background-radius: 12; -fx-padding: 12 24;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);");
        Label lbl = new Label(message);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        toast.getChildren().add(lbl);
        StackPane.setAlignment(toast, Pos.TOP_CENTER);
        toast.setTranslateY(-50);
        container.getChildren().add(toast);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toast);
        slideIn.setFromY(-50);
        slideIn.setToY(20);
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> container.getChildren().remove(toast));
        slideIn.setOnFinished(e -> pause.play());
        pause.setOnFinished(e -> fadeOut.play());
        slideIn.play();
    }

    private static void shake(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }
}
