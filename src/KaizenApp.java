import controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.NavigationManager;

public class KaizenApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            NavigationManager nav = NavigationManager.getInstance();
            nav.setPrimaryStage(primaryStage);

            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/global.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

            nav.setMainScene(scene);

            primaryStage.setTitle("Kaizen - Continuous Improvement");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1100);
            primaryStage.setMinHeight(750);
            primaryStage.setWidth(1280);
            primaryStage.setHeight(820);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start Kaizen: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
