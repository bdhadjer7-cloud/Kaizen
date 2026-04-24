package controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ResourcesContentController {

    @FXML private FlowPane resourceGrid;

    @FXML
    public void initialize() {
        loadResources();
    }

    private void loadResources() {
        Object[][] resources = {
                {"Fiche TD TL", "PDF", "Linear Algebra", "\u2B50 4.5", "12 saves", "#E53E3E"},
                {"MinMax-- Agent", "AI Tool", "Data Structures", "\u2B50 4.8", "24 saves", "#7C5CFC"},
                {"HTML/CSS Cheatsheet", "PDF", "Web Development", "\u2B50 4.2", "18 saves", "#4361EE"},
                {"Graph Visualizer", "Link", "Data Structures", "\u2B50 4.0", "8 saves", "#48BB78"},
                {"Khan Academy: LA", "Video", "Linear Algebra", "\u2B50 4.7", "31 saves", "#E07B4C"},
                {"Sorting Animations", "Link", "Data Structures", "\u2B50 4.3", "15 saves", "#2B3674"}
        };

        for (Object[] r : resources) {
            VBox card = new VBox(8);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 16;"
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
                    + "-fx-min-width: 220; -fx-pref-width: 240;");

            HBox top = new HBox(8);
            top.setAlignment(Pos.CENTER_LEFT);
            StackPane icon = new StackPane();
            icon.setStyle("-fx-background-color: " + r[5] + "22; -fx-background-radius: 10; -fx-min-width: 40; -fx-min-height: 40;");
            Label iconText = new Label(getTypeIcon((String) r[1]));
            iconText.setStyle("-fx-font-size: 18;");
            icon.getChildren().add(iconText);
            Label type = new Label((String) r[1]);
            type.setStyle("-fx-background-color: #F4F7FE; -fx-text-fill: " + r[5]
                    + "; -fx-padding: 2 8; -fx-background-radius: 8; -fx-font-size: 10; -fx-font-weight: bold;");
            top.getChildren().addAll(icon, type);

            Label title = new Label((String) r[0]);
            title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2B3674;");

            Label course = new Label((String) r[2]);
            course.setStyle("-fx-font-size: 11; -fx-text-fill: #4361EE;");

            HBox bottom = new HBox(12);
            Label rating = new Label((String) r[3]);
            rating.setStyle("-fx-font-size: 12; -fx-text-fill: #ECC94B;");
            Label saves = new Label((String) r[4]);
            saves.setStyle("-fx-font-size: 12; -fx-text-fill: #8F9BBA;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button saveBtn = new Button("\u2661");
            saveBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16; -fx-cursor: hand; -fx-text-fill: #E53E3E;");
            bottom.getChildren().addAll(rating, saves, spacer, saveBtn);

            card.getChildren().addAll(top, title, course, bottom);
            resourceGrid.getChildren().add(card);
        }
    }

    private String getTypeIcon(String type) {
        return switch (type) {
            case "PDF" -> "\uD83D\uDCC4";
            case "AI Tool" -> "\uD83E\uDD16";
            case "Link" -> "\uD83D\uDD17";
            case "Video" -> "\uD83C\uDFA5";
            default -> "\uD83D\uDCC1";
        };
    }
}
