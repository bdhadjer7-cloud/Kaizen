package controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Course;
import service.DemoDataService;

import java.util.List;

public class CoursesContentController {

    @FXML private VBox courseList, classmatesList;
    @FXML private Label xpLabel;

    @FXML
    public void initialize() {
        loadCourses();
        loadClassmates();
    }

    private void loadCourses() {
        Object[][] courses = {
                {"Linear Algebra", "Vectors, matrices, eigenvalues and their real-world applications in computer science.",
                        new String[]{"Mathematics", "Intermediate", "In Progress"}, "Dr. Karim Bensaid", 0.65, "13 / 20", "#4361EE"},
                {"Web Development Fundamentals", "HTML, CSS, JavaScript and building modern responsive interfaces.",
                        new String[]{"Web Dev", "Beginner", "Completed"}, "Dr. Salem Farhi", 1.0, "20 / 20", "#2B3674"},
                {"Data Structures & Algorithms", "Trees, graphs, sorting and searching -- the foundations of CS problem solving.",
                        new String[]{"Computer Science", "Advanced", "In Progress"}, "Prof. Amina Hadj", 0.40, "8 / 20", "#48BB78"}
        };

        for (Object[] c : courses) {
            VBox card = new VBox(8);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 20;"
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
                    + "-fx-border-color: " + (((double)c[4] >= 1.0) ? "#4361EE" : "transparent") + "; -fx-border-radius: 16; -fx-border-width: 2;");

            HBox header = new HBox(12);
            header.setAlignment(Pos.CENTER_LEFT);

            StackPane icon = new StackPane();
            icon.setStyle("-fx-background-color: " + c[6] + "22; -fx-background-radius: 12; -fx-min-width: 48; -fx-min-height: 48;");
            Label iconText = new Label("\uD83D\uDCDA");
            iconText.setStyle("-fx-font-size: 22;");
            icon.getChildren().add(iconText);

            VBox info = new VBox(2);
            Label title = new Label((String) c[0]);
            title.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-text-fill: #2B3674;");
            Label desc = new Label((String) c[1]);
            desc.setStyle("-fx-font-size: 12; -fx-text-fill: #8F9BBA;");
            desc.setWrapText(true);
            info.getChildren().addAll(title, desc);
            HBox.setHgrow(info, Priority.ALWAYS);

            Button arrow = new Button("\u276F");
            arrow.setStyle("-fx-background-color: #4361EE; -fx-text-fill: white; -fx-background-radius: 50;"
                    + "-fx-min-width: 36; -fx-min-height: 36; -fx-font-size: 16; -fx-cursor: hand;");

            header.getChildren().addAll(icon, info, arrow);

            // Tags
            HBox tags = new HBox(8);
            for (String t : (String[]) c[2]) {
                Label tag = new Label(t);
                String color = t.contains("Progress") ? "#E3F2FD" : t.contains("Completed") ? "#E8F5E9" : "#F4F7FE";
                String textColor = t.contains("Progress") ? "#1565C0" : t.contains("Completed") ? "#2E7D32" : "#2B3674";
                tag.setStyle("-fx-background-color: " + color + "; -fx-text-fill: " + textColor
                        + "; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;");
                tags.getChildren().add(tag);
            }

            Label instructor = new Label("by " + c[3]);
            instructor.setStyle("-fx-font-size: 12; -fx-text-fill: #4361EE;");

            // Progress bar
            ProgressBar pb = new ProgressBar((double) c[4]);
            pb.setPrefWidth(Double.MAX_VALUE);
            pb.setStyle("-fx-accent: " + c[6] + ";");

            Label progressText = new Label(((int)((double)c[4]*100)) + "% complete . " + c[5] + " lessons"
                    + ((double)c[4] >= 1.0 ? " . +50 XP earned" : ""));
            progressText.setStyle("-fx-font-size: 11; -fx-text-fill: #8F9BBA;");

            card.getChildren().addAll(header, tags, instructor, pb, progressText);
            courseList.getChildren().add(card);
        }
    }

    private void loadClassmates() {
        DemoDataService demo = DemoDataService.getInstance();
        String[] names = demo.getContactNames();
        String[] initials = demo.getContactInitials();
        String[] colors = demo.getContactColors();
        boolean[] online = demo.getContactOnline();

        for (int i = 0; i < names.length; i++) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            StackPane avatar = new StackPane();
            avatar.setStyle("-fx-background-color: " + colors[i] + "; -fx-background-radius: 50; -fx-min-width: 32; -fx-min-height: 32; -fx-max-width: 32; -fx-max-height: 32;");
            Label init = new Label(initials[i]);
            init.setStyle("-fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold;");
            avatar.getChildren().add(init);
            Label name = new Label(names[i]);
            name.setStyle("-fx-font-size: 13; -fx-text-fill: #2B3674;");
            HBox.setHgrow(name, Priority.ALWAYS);
            Region dot = new Region();
            dot.setPrefSize(8, 8);
            dot.setStyle("-fx-background-color: " + (online[i] ? "#48BB78" : "#A0AEC0") + "; -fx-background-radius: 50;");
            row.getChildren().addAll(avatar, name, dot);
            classmatesList.getChildren().add(row);
        }
    }
}
