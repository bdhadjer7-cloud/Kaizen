package controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import service.DemoDataService;

public class ContactsContentController {

    @FXML private VBox onlineContacts, allContacts, messageArea;
    @FXML private TextField messageInput;
    @FXML private Label chatName;

    @FXML
    public void initialize() {
        loadContacts();
        loadMessages();
    }

    @FXML
    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (!text.isEmpty()) {
            messageArea.getChildren().add(createMessageBubble("SB", text, true));
            messageInput.clear();
        }
    }

    private void loadContacts() {
        DemoDataService demo = DemoDataService.getInstance();
        String[] names = demo.getContactNames();
        String[] initials = demo.getContactInitials();
        String[] colors = demo.getContactColors();
        boolean[] online = demo.getContactOnline();

        for (int i = 0; i < names.length; i++) {
            HBox row = createContactRow(names[i], initials[i], colors[i], online[i],
                    i == 0 ? "Did you finish the DFS?" : i == 1 ? "New course has been uploaded!" : "");
            if (online[i]) {
                onlineContacts.getChildren().add(row);
            }
            allContacts.getChildren().add(createContactRow(names[i], initials[i], colors[i], online[i],
                    i == 0 ? "Did you finish the DFS?" : ""));
        }
    }

    private HBox createContactRow(String name, String initials, String color, boolean online, String lastMsg) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 8 16; -fx-cursor: hand;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 8 16; -fx-cursor: hand; -fx-background-color: #F4F7FE;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-padding: 8 16; -fx-cursor: hand; -fx-background-color: transparent;"));

        StackPane avatar = new StackPane();
        avatar.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 50; -fx-min-width: 36; -fx-min-height: 36; -fx-max-width: 36; -fx-max-height: 36;");
        Label init = new Label(initials);
        init.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;");
        avatar.getChildren().add(init);

        VBox info = new VBox(1);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2B3674;");
        info.getChildren().add(nameLabel);
        if (!lastMsg.isEmpty()) {
            Label msg = new Label(lastMsg);
            msg.setStyle("-fx-font-size: 11; -fx-text-fill: #8F9BBA;");
            info.getChildren().add(msg);
        }
        HBox.setHgrow(info, Priority.ALWAYS);

        if (online) {
            Region dot = new Region();
            dot.setPrefSize(8, 8);
            dot.setStyle("-fx-background-color: #48BB78; -fx-background-radius: 50;");
            row.getChildren().addAll(avatar, info, dot);
        } else {
            row.getChildren().addAll(avatar, info);
        }

        row.setOnMouseClicked(e -> chatName.setText(name));

        return row;
    }

    private void loadMessages() {
        messageArea.getChildren().addAll(
                createMessageBubble("BH", "Hey! Did you finish the DFS assignment?", false),
                createMessageBubble("SB", "Yes! I just submitted it. The recursive approach worked well.", true),
                createMessageBubble("BH", "Nice! Can you help me with the BFS part? I'm stuck on the queue implementation.", false),
                createMessageBubble("SB", "Sure! Let's meet in the study room in 10 minutes?", true)
        );
    }

    private HBox createMessageBubble(String initials, String text, boolean sent) {
        HBox bubble = new HBox(8);
        bubble.setAlignment(sent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        bubble.setStyle("-fx-max-width: 500;");

        if (!sent) {
            StackPane avatar = new StackPane();
            avatar.setStyle("-fx-background-color: #4361EE; -fx-background-radius: 50; -fx-min-width: 30; -fx-min-height: 30; -fx-max-width: 30; -fx-max-height: 30;");
            Label init = new Label(initials);
            init.setStyle("-fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: bold;");
            avatar.getChildren().add(init);
            bubble.getChildren().add(avatar);
        }

        Label msg = new Label(text);
        msg.setWrapText(true);
        msg.setStyle("-fx-background-color: " + (sent ? "#4361EE" : "#F4F7FE")
                + "; -fx-text-fill: " + (sent ? "white" : "#2B3674")
                + "; -fx-padding: 10 16; -fx-background-radius: 14; -fx-font-size: 13;");
        bubble.getChildren().add(msg);

        return bubble;
    }
}
