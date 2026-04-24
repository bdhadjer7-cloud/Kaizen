package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class NotesContentController {

    @FXML private VBox noteList;
    @FXML private Label noteTitle;
    @FXML private TextArea noteEditor;

    @FXML
    public void initialize() {
        loadNotes();
    }

    private void loadNotes() {
        Object[][] notes = {
                {"Eigenvalues Formula", "Linear Algebra", "Today . 14:23", true},
                {"Graph Traversal", "Data Structures", "Today . 10:30", false},
                {"Matrix Ops Cheatsheet", "Linear Algebra", "Yesterday", false},
                {"Big-O Summary", "Data Structures", "2 days ago", false},
                {"CSS Grid Notes", "Web Development", "Last week", false}
        };

        for (Object[] n : notes) {
            VBox card = new VBox(2);
            boolean active = (boolean) n[3];
            card.setStyle("-fx-padding: 10 16; -fx-cursor: hand;"
                    + "-fx-background-color: " + (active ? "#E8ECF8" : "transparent") + ";");
            Label title = new Label((String) n[0]);
            title.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2B3674;");
            Label course = new Label((String) n[1]);
            course.setStyle("-fx-font-size: 11; -fx-text-fill: #4361EE;");
            Label time = new Label((String) n[2]);
            time.setStyle("-fx-font-size: 10; -fx-text-fill: #8F9BBA;");
            card.getChildren().addAll(title, course, time);

            card.setOnMouseClicked(e -> {
                noteTitle.setText((String) n[0]);
                noteList.getChildren().forEach(c ->
                        c.setStyle("-fx-padding: 10 16; -fx-cursor: hand; -fx-background-color: transparent;"));
                card.setStyle("-fx-padding: 10 16; -fx-cursor: hand; -fx-background-color: #E8ECF8;");
            });

            noteList.getChildren().add(card);
        }
    }
}
