package controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LessonsContentController {

    @FXML private VBox chapterList;
    @FXML private TextArea noteArea;

    @FXML
    public void initialize() {
        loadChapters();
    }

    private void loadChapters() {
        String[][] chapters = {
                {"CHAPTER 1", ""},
                {"", "Vectors & Spaces"},
                {"", "Vector Operations"},
                {"CHAPTER 2", ""},
                {"", "Matrix Operations"},
                {"", "Determinants"},
                {"CHAPTER 3", ""},
                {"", "Eigenvalues & Eigenvectors"},
                {"", "Characteristic Polynomial"},
                {"", "Diagonalization"},
                {"CHAPTER 4", ""},
                {"", "Linear Transformations"},
                {"", "Orthogonality"}
        };

        int lessonNum = 0;
        for (String[] ch : chapters) {
            if (!ch[0].isEmpty()) {
                Label header = new Label(ch[0]);
                header.setStyle("-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: #8F9BBA; -fx-padding: 12 16 4 16;");
                chapterList.getChildren().add(header);
            } else {
                lessonNum++;
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 4 16; -fx-cursor: hand;");

                boolean completed = lessonNum <= 4;
                boolean active = lessonNum == 5;

                Label icon;
                if (completed) {
                    icon = new Label("\u2714");
                    icon.setStyle("-fx-text-fill: #48BB78; -fx-font-size: 14;");
                } else if (active) {
                    StackPane circle = new StackPane();
                    circle.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 50; -fx-min-width: 22; -fx-min-height: 22;");
                    Label num = new Label(String.valueOf(lessonNum));
                    num.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
                    circle.getChildren().add(num);
                    row.getChildren().add(circle);
                    Label name = new Label(ch[1]);
                    name.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2B3674;"
                            + "-fx-background-color: #E8F5E9; -fx-background-radius: 8; -fx-padding: 4 8;");
                    row.getChildren().add(name);
                    chapterList.getChildren().add(row);
                    continue;
                } else {
                    icon = new Label(String.valueOf(lessonNum));
                    icon.setStyle("-fx-text-fill: #8F9BBA; -fx-font-size: 12; -fx-min-width: 20; -fx-alignment: center;");
                }

                Label name = new Label(ch[1]);
                name.setStyle("-fx-font-size: 13; -fx-text-fill: " + (completed ? "#48BB78" : "#8F9BBA") + ";");

                row.getChildren().addAll(icon, name);
                chapterList.getChildren().add(row);
            }
        }
    }
}
