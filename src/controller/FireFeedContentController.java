package controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class FireFeedContentController {

    @FXML private TextField composeField;
    @FXML private VBox postList, contributorsList;
    @FXML private FlowPane tagCloud;

    @FXML
    public void initialize() {
        loadPosts();
        loadContributors();
        loadTags();
    }

    @FXML
    private void handlePost() {
        String text = composeField.getText().trim();
        if (!text.isEmpty()) {
            postList.getChildren().add(0, createPost("SB", "Saffih Bouchra", text,
                    new String[]{"New"}, "Just now", 0, 0));
            composeField.clear();
        }
    }

    private void loadPosts() {
        postList.getChildren().add(createPost("AK", "Amira Khoualdia",
                "Can someone clarify the difference between eigenvalues and singular values? I keep mixing them up when studying PCA. Are they ever the same?",
                new String[]{"Question"}, "Today . 14:23", 12, 3));

        postList.getChildren().add(createPost("FZ", "Fatima Zahra",
                "Quiz Champion earned!\nScored 100% on Data Structures Quiz 2 -- +50 XP",
                new String[]{"Achievement"}, "Today . 13:00", 12, 5));

        postList.getChildren().add(createPost("BH", "Bensaid Hadjer",
                "Just finished a 50-minute deep focus session on graph algorithms. The Pomodoro technique really helps maintain concentration!",
                new String[]{"Study Tip"}, "Yesterday . 22:15", 8, 2));
    }

    private VBox createPost(String initials, String name, String content,
                            String[] tags, String time, int likes, int comments) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 16;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = new StackPane();
        avatar.setStyle("-fx-background-color: #4361EE; -fx-background-radius: 50; -fx-min-width: 36; -fx-min-height: 36; -fx-max-width: 36; -fx-max-height: 36;");
        Label init = new Label(initials);
        init.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13;");
        avatar.getChildren().add(init);
        VBox nameBox = new VBox();
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2B3674;");
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #8F9BBA;");
        nameBox.getChildren().addAll(nameLabel, timeLabel);
        header.getChildren().addAll(avatar, nameBox);

        Label text = new Label(content);
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 13; -fx-text-fill: #4A5568;");

        HBox tagBox = new HBox(6);
        for (String t : tags) {
            Label tag = new Label(t);
            String color = t.equals("Question") ? "#E3F2FD" : t.equals("Achievement") ? "#E8F5E9" : "#F3E8FF";
            String tColor = t.equals("Question") ? "#1565C0" : t.equals("Achievement") ? "#2E7D32" : "#6B21A8";
            tag.setStyle("-fx-background-color: " + color + "; -fx-text-fill: " + tColor
                    + "; -fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;");
            tagBox.getChildren().add(tag);
        }

        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER_LEFT);
        Label likeBtn = new Label("\uD83D\uDD25 " + likes);
        likeBtn.setStyle("-fx-font-size: 12; -fx-text-fill: #8F9BBA; -fx-cursor: hand;");
        Label commentBtn = new Label("\uD83D\uDCAC " + comments);
        commentBtn.setStyle("-fx-font-size: 12; -fx-text-fill: #8F9BBA; -fx-cursor: hand;");
        Label shareBtn = new Label("\u21AA Share");
        shareBtn.setStyle("-fx-font-size: 12; -fx-text-fill: #8F9BBA; -fx-cursor: hand;");
        actions.getChildren().addAll(likeBtn, commentBtn, shareBtn);

        card.getChildren().addAll(header, text, tagBox, actions);
        return card;
    }

    private void loadContributors() {
        String[][] contributors = {
                {"AK", "Amira K.", "#E07B4C", "2,400 XP"},
                {"BH", "Bensaid Hadjer", "#4361EE", "2,100 XP"},
                {"FZ", "Fatima Z.", "#7C5CFC", "1,800 XP"}
        };
        for (String[] c : contributors) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            StackPane avatar = new StackPane();
            avatar.setStyle("-fx-background-color: " + c[2] + "; -fx-background-radius: 50; -fx-min-width: 28; -fx-min-height: 28; -fx-max-width: 28; -fx-max-height: 28;");
            Label init = new Label(c[0]);
            init.setStyle("-fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: bold;");
            avatar.getChildren().add(init);
            Label name = new Label(c[1]);
            name.setStyle("-fx-font-size: 12; -fx-text-fill: #2B3674;");
            HBox.setHgrow(name, Priority.ALWAYS);
            Label xp = new Label(c[3]);
            xp.setStyle("-fx-font-size: 11; -fx-text-fill: #4361EE; -fx-font-weight: bold;");
            row.getChildren().addAll(avatar, name, xp);
            contributorsList.getChildren().add(row);
        }
    }

    private void loadTags() {
        String[] tags = {"Linear Algebra", "DSA", "Study Tips", "Web Dev", "Algorithms", "Physics", "Math", "AI"};
        for (String t : tags) {
            Label tag = new Label(t);
            tag.setStyle("-fx-background-color: #F4F7FE; -fx-text-fill: #4361EE; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-cursor: hand;");
            tagCloud.getChildren().add(tag);
        }
    }
}
