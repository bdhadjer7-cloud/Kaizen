package controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class PomodoroContentController {

    @FXML private Label timerLabel, modeLabel;
    @FXML private Button startBtn;
    @FXML private VBox sessionList, taskList;
    @FXML private HBox streakDots;

    private Timeline timer;
    private int seconds = 25 * 60;
    private boolean running = false;

    @FXML
    public void initialize() {
        loadSessions();
        loadTasks();
        loadStreakDots();
        updateTimerDisplay();
    }

    @FXML
    private void handleStartPause() {
        if (running) {
            timer.stop();
            running = false;
            startBtn.setText("\u25B6  Start");
        } else {
            running = true;
            startBtn.setText("\u23F8  Pause");
            timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                seconds--;
                updateTimerDisplay();
                if (seconds <= 0) {
                    timer.stop();
                    running = false;
                    startBtn.setText("\u25B6  Start");
                    modeLabel.setText("Session complete!");
                }
            }));
            timer.setCycleCount(Timeline.INDEFINITE);
            timer.play();
        }
    }

    @FXML private void handleReset() {
        if (timer != null) timer.stop();
        running = false;
        seconds = 25 * 60;
        startBtn.setText("\u25B6  Start");
        modeLabel.setText("Focus Session");
        updateTimerDisplay();
    }

    @FXML private void setFocus25() { setTime(25, "Focus Session"); }
    @FXML private void setBreak5()  { setTime(5, "Short Break"); }
    @FXML private void setBreak15() { setTime(15, "Long Break"); }
    @FXML private void setFocus50() { setTime(50, "Deep Focus Session"); }

    private void setTime(int minutes, String mode) {
        if (timer != null) timer.stop();
        running = false;
        seconds = minutes * 60;
        modeLabel.setText(mode);
        startBtn.setText("\u25B6  Start");
        updateTimerDisplay();
    }

    private void updateTimerDisplay() {
        int m = seconds / 60;
        int s = seconds % 60;
        timerLabel.setText(String.format("%02d:%02d", m, s));
    }

    @FXML
    private void addTask() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Task");
        dialog.setHeaderText(null);
        dialog.setContentText("Task:");
        dialog.showAndWait().ifPresent(task -> {
            if (!task.isBlank()) {
                HBox row = createTaskRow(task, false);
                taskList.getChildren().add(row);
            }
        });
    }

    private void loadSessions() {
        Object[][] sessions = {
                {"Linear Algebra", "25 min", "Today . 14:00"},
                {"Data Structures", "50 min", "Today . 10:30"},
                {"Break", "5 min", "Today . 10:25"}
        };
        for (Object[] s : sessions) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 8; -fx-background-color: #F4F7FE; -fx-background-radius: 10;");
            Label name = new Label((String) s[0]);
            name.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2B3674;");
            HBox.setHgrow(name, Priority.ALWAYS);
            Label dur = new Label((String) s[1]);
            dur.setStyle("-fx-font-size: 12; -fx-text-fill: #4361EE; -fx-font-weight: bold;");
            Label time = new Label((String) s[2]);
            time.setStyle("-fx-font-size: 11; -fx-text-fill: #8F9BBA;");
            row.getChildren().addAll(name, dur, time);
            sessionList.getChildren().add(row);
        }
    }

    private void loadTasks() {
        String[] tasks = {"Review eigenvalues notes", "Solve 5 practice problems", "Watch graph traversal video"};
        boolean[] done = {true, false, false};
        for (int i = 0; i < tasks.length; i++) {
            taskList.getChildren().add(createTaskRow(tasks[i], done[i]));
        }
    }

    private HBox createTaskRow(String task, boolean completed) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        CheckBox cb = new CheckBox();
        cb.setSelected(completed);
        Label lbl = new Label(task);
        lbl.setStyle("-fx-font-size: 13; -fx-text-fill: " + (completed ? "#A0AEC0" : "#2B3674") + ";");
        cb.setOnAction(e -> lbl.setStyle("-fx-font-size: 13; -fx-text-fill: " + (cb.isSelected() ? "#A0AEC0" : "#2B3674") + ";"));
        row.getChildren().addAll(cb, lbl);
        return row;
    }

    private void loadStreakDots() {
        String[] days = {"M", "T", "W", "T", "F", "S", "S"};
        for (int i = 0; i < 7; i++) {
            VBox dot = new VBox(2);
            dot.setAlignment(Pos.CENTER);
            StackPane circle = new StackPane();
            circle.setStyle("-fx-background-color: " + (i < 5 ? "#4361EE" : "#E0E5F2")
                    + "; -fx-background-radius: 50; -fx-min-width: 24; -fx-min-height: 24;");
            Label d = new Label(days[i]);
            d.setStyle("-fx-font-size: 9; -fx-text-fill: " + (i < 5 ? "white" : "#8F9BBA") + "; -fx-font-weight: bold;");
            circle.getChildren().add(d);
            dot.getChildren().add(circle);
            streakDots.getChildren().add(dot);
        }
    }
}
