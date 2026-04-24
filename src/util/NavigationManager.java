package util;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class NavigationManager {

    private static NavigationManager instance;
    private Stage primaryStage;
    private Scene mainScene;

    private NavigationManager() {}

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setMainScene(Scene scene) {
        this.mainScene = scene;
    }

    public Scene getMainScene() {
        return mainScene;
    }

    public void navigateTo(String viewName) {
        try {
            String fxmlPath = "/view/" + viewName + ".fxml";
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

            if (mainScene == null) {
                mainScene = new Scene(root);
                primaryStage.setScene(mainScene);
            } else {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(120), mainScene.getRoot());
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    mainScene.setRoot(root);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(180), root);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                fadeOut.play();
            }

            applyCss(viewName);
        } catch (IOException e) {
            System.err.println("Navigation error loading " + viewName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyCss(String viewName) {
        if (mainScene == null) return;
        mainScene.getStylesheets().clear();

        var globalCss = getClass().getResource("/css/global.css");
        if (globalCss != null) {
            mainScene.getStylesheets().add(globalCss.toExternalForm());
        }

        var viewCss = getClass().getResource("/css/" + viewName + ".css");
        if (viewCss != null) {
            mainScene.getStylesheets().add(viewCss.toExternalForm());
        }

        // Dashboard sub-pages share dashboard.css
        var dashCss = getClass().getResource("/css/dashboard.css");
        if (dashCss != null && !viewName.equals("login") && !viewName.equals("welcome")) {
            mainScene.getStylesheets().add(dashCss.toExternalForm());
        }
    }
}
