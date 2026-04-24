package controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class RoomsContentController {

    @FXML private FlowPane roomGrid;

    @FXML
    public void initialize() {
        loadRooms();
    }

    @FXML
    private void createRoom() {
        TextInputDialog dialog = new TextInputDialog("My Study Room");
        dialog.setTitle("Create Room");
        dialog.setHeaderText("Enter room name:");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                roomGrid.getChildren().add(0, createRoomCard(name, "Just created", 1, 50, true,
                        new String[]{"New"}, "#4361EE"));
            }
        });
    }

    private void loadRooms() {
        roomGrid.getChildren().addAll(
                createRoomCard("med students", "Medical school study group — anatomy, biochemistry, and more.",
                        23, 50, true, new String[]{"Med", "Biology", "Exam Prep"}, "#E53E3E"),
                createRoomCard("IT & Tech", "Web development, algorithms, and system design practice.",
                        45, 100, true, new String[]{"IT", "Programming", "Web Dev"}, "#4361EE"),
                createRoomCard("focus & achieve", "Silent study room. Pomodoro timer synced for all members.",
                        120, 200, true, new String[]{"Focus", "Pomodoro", "Silent"}, "#7C5CFC"),
                createRoomCard("Math Help", "Need help with calculus or linear algebra? Join us!",
                        8, 30, false, new String[]{"Math", "Tutoring"}, "#48BB78"),
                createRoomCard("DSA Grind", "Daily competitive programming practice and mock interviews.",
                        15, 40, true, new String[]{"DSA", "Interview Prep"}, "#E07B4C"),
                createRoomCard("Physics Lab", "Discussion group for physics experiments and theory.",
                        5, 25, false, new String[]{"Physics", "Science"}, "#2B3674")
        );
    }

    private VBox createRoomCard(String name, String desc, int members, int max,
                                boolean live, String[] tags, String color) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 16;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
                + "-fx-min-width: 240; -fx-pref-width: 260;");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane icon = new StackPane();
        icon.setStyle("-fx-background-color: " + color + "22; -fx-background-radius: 12; -fx-min-width: 44; -fx-min-height: 44;");
        Label iconText = new Label(name.substring(0, 1).toUpperCase());
        iconText.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        icon.getChildren().add(iconText);
        VBox nameBox = new VBox();
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2B3674;");
        HBox liveBox = new HBox(4);
        liveBox.setAlignment(Pos.CENTER_LEFT);
        if (live) {
            Region dot = new Region();
            dot.setPrefSize(8, 8);
            dot.setStyle("-fx-background-color: #48BB78; -fx-background-radius: 50;");
            Label liveLabel = new Label("Live");
            liveLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #48BB78; -fx-font-weight: bold;");
            liveBox.getChildren().addAll(dot, liveLabel);
        }
        nameBox.getChildren().add(nameLabel);
        if (live) nameBox.getChildren().add(liveBox);
        header.getChildren().addAll(icon, nameBox);

        Label descLabel = new Label(desc);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #8F9BBA;");

        FlowPane tagPane = new FlowPane(6, 4);
        for (String t : tags) {
            Label tag = new Label(t);
            tag.setStyle("-fx-background-color: #F4F7FE; -fx-text-fill: " + color
                    + "; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10; -fx-font-weight: bold;");
            tagPane.getChildren().add(tag);
        }

        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label count = new Label(members + "/" + max + " members");
        count.setStyle("-fx-font-size: 11; -fx-text-fill: #8F9BBA;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button joinBtn = new Button("Join");
        joinBtn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 6 16; -fx-font-size: 12; -fx-font-weight: bold; -fx-cursor: hand;");
        footer.getChildren().addAll(count, spacer, joinBtn);

        card.getChildren().addAll(header, descLabel, tagPane, footer);
        return card;
    }
}
