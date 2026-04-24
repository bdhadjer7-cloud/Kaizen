package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import model.Course;
import model.User;
import service.DemoDataService;

import java.util.List;
import java.util.Random;

public class HomeContentController {

    @FXML private Label profileName, profileMeta, profileBio, xpLabel, studyTimeLabel, streakLabel;
    @FXML private Label profileInitials, xpSidebar, rankLabel;
    @FXML private Label overviewTab, historyTab;
    @FXML private VBox overviewPane, historyPane, recentCoursesBox, historyList;
    @FXML private HBox badgesBox;
    @FXML private FlowPane calendarHeatmap, badgeGrid;
    @FXML private ImageView mascotSmall;

    @FXML
    public void initialize() {
        User user = DemoDataService.getInstance().getCurrentUser();
        if (user != null) {
            profileName.setText(user.getName());
            profileInitials.setText(user.getInitials());
            profileMeta.setText("@" + user.getMeta("username", String.class)
                    + " . " + user.getMeta("university", String.class));
            profileBio.setText(user.getBio());
            Integer xp = user.getMeta("totalXp", Integer.class);
            xpLabel.setText(String.format("%,d", xp != null ? xp : 0));
            xpSidebar.setText(String.format("%,d", xp != null ? xp : 0));
            Integer streak = user.getMeta("dayStreak", Integer.class);
            streakLabel.setText(String.valueOf(streak != null ? streak : 0));
        }

        loadBadges();
        loadRecentCourses();
        loadCalendarHeatmap();
        loadStudyHistory();
        loadBadgeGrid();

        try {
            mascotSmall.setImage(new Image(getClass().getResourceAsStream("/images/mascot_search.png")));
        } catch (Exception ignored) {}
    }

    @FXML
    private void showOverview() {
        overviewPane.setVisible(true); overviewPane.setManaged(true);
        historyPane.setVisible(false); historyPane.setManaged(false);
        overviewTab.getStyleClass().add("tab-active-label");
        historyTab.getStyleClass().remove("tab-active-label");
    }

    @FXML
    private void showHistory() {
        historyPane.setVisible(true); historyPane.setManaged(true);
        overviewPane.setVisible(false); overviewPane.setManaged(false);
        historyTab.getStyleClass().add("tab-active-label");
        overviewTab.getStyleClass().remove("tab-active-label");
    }

    private void loadBadges() {
        String[] badges = {"First Flame", "Focus Master", "Bookworm"};
        String[] colors = {"#E8F5E9", "#E8F5E9", "#FFF8E1"};
        for (int i = 0; i < badges.length; i++) {
            VBox badge = new VBox(4);
            badge.setAlignment(javafx.geometry.Pos.CENTER);
            badge.setStyle("-fx-background-color: " + colors[i] + "; -fx-background-radius: 12; -fx-padding: 8; -fx-min-width: 70;");
            Label icon = new Label("\uD83C\uDFC5");
            icon.setStyle("-fx-font-size: 24;");
            Label name = new Label(badges[i]);
            name.setStyle("-fx-font-size: 10; -fx-text-fill: #4A5568;");
            badge.getChildren().addAll(icon, name);
            badgesBox.getChildren().add(badge);
        }
        // Locked badge
        VBox locked = new VBox(4);
        locked.setAlignment(javafx.geometry.Pos.CENTER);
        locked.setStyle("-fx-background-color: #F0F0F0; -fx-background-radius: 12; -fx-padding: 8; -fx-min-width: 70;");
        Label lockIcon = new Label("\uD83D\uDD12");
        lockIcon.setStyle("-fx-font-size: 24;");
        Label lockText = new Label("Locked");
        lockText.setStyle("-fx-font-size: 10; -fx-text-fill: #E07B4C;");
        locked.getChildren().addAll(lockIcon, lockText);
        badgesBox.getChildren().add(locked);
    }

    private void loadRecentCourses() {
        Object[][] data = {
                {"Linear Algebra", 0.65, "#4361EE"},
                {"Data Structures", 0.40, "#2B3674"},
                {"Biology 201", 0.90, "#48BB78"}
        };
        for (Object[] c : data) {
            HBox row = new HBox(12);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label name = new Label((String) c[0]);
            name.setStyle("-fx-font-size: 14; -fx-text-fill: #2B3674; -fx-font-weight: bold;");
            ProgressBar pb = new ProgressBar((double) c[1]);
            pb.setPrefWidth(400);
            pb.setStyle("-fx-accent: " + c[2] + ";");
            HBox.setHgrow(pb, Priority.ALWAYS);
            Label pct = new Label(((int)((double)c[1] * 100)) + "%");
            pct.setStyle("-fx-font-size: 13; -fx-text-fill: #8F9BBA;");
            row.getChildren().addAll(name, pb, pct);
            recentCoursesBox.getChildren().add(row);
        }
    }

    private void loadCalendarHeatmap() {
        Random rand = new Random(42);
        String[] shades = {"#E0E5F2", "#B0BDD4", "#6B7DB3", "#3B5998", "#2B3674"};
        for (int i = 0; i < 80; i++) {
            Region cell = new Region();
            cell.setPrefSize(16, 16);
            cell.setStyle("-fx-background-color: " + shades[rand.nextInt(shades.length)]
                    + "; -fx-background-radius: 3;");
            calendarHeatmap.getChildren().add(cell);
        }
    }

    private void loadStudyHistory() {
        Object[][] items = {
                {"Completed Lesson 5 - Eigenvalues", "Today . 14:23 . Linear Algebra", "+20XP", "#C8E6C9"},
                {"Pomodoro session - 25 min focus", "Today . 13:00 . Data Structures", "+15 XP", "#FFE0B2"},
                {"Earned Focus Master badge", "Yesterday . 23:11", "+25 XP", "#BBDEFB"}
        };
        for (Object[] item : items) {
            HBox row = new HBox(12);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Region dot = new Region();
            dot.setPrefSize(12, 12);
            dot.setStyle("-fx-background-color: " + item[3] + "; -fx-background-radius: 3;");
            VBox text = new VBox(2);
            Label title = new Label((String) item[0]);
            title.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2B3674;");
            Label sub = new Label((String) item[1]);
            sub.setStyle("-fx-font-size: 11; -fx-text-fill: #8F9BBA;");
            text.getChildren().addAll(title, sub);
            HBox.setHgrow(text, Priority.ALWAYS);
            Label xp = new Label((String) item[2]);
            xp.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #48BB78;");
            row.getChildren().addAll(dot, text, xp);
            historyList.getChildren().add(row);
        }
    }

    private void loadBadgeGrid() {
        String[] names = {"Top Learner", "Kenja III", "Quiz Champion", "Night Owl"};
        String[] descs = {"Rank #1 on the\nweekly leaderboard", "Reach III Kenja\nrank (6,000 XP)", "Score 100% on 3\nquizzes in a row", "Study after\nmidnight 5 times"};
        for (int i = 0; i < names.length; i++) {
            VBox badge = new VBox(4);
            badge.setAlignment(javafx.geometry.Pos.CENTER);
            badge.setStyle("-fx-background-color: #F4F7FE; -fx-background-radius: 12; -fx-padding: 12; -fx-min-width: 90;");
            Label icon = new Label("\uD83C\uDFC6");
            icon.setStyle("-fx-font-size: 28;");
            Label name = new Label(names[i]);
            name.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #2B3674;");
            Label desc = new Label(descs[i]);
            desc.setStyle("-fx-font-size: 9; -fx-text-fill: #8F9BBA;");
            desc.setWrapText(true);
            Label locked = new Label("Locked");
            locked.setStyle("-fx-font-size: 10; -fx-text-fill: #A0AEC0;");
            badge.getChildren().addAll(icon, name, desc, locked);
            badgeGrid.getChildren().add(badge);
        }
    }
}
