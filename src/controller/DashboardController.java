package controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import model.User;
import service.DemoDataService;
import util.NavigationManager;

import java.io.IOException;

public class DashboardController {

    @FXML private StackPane rootStack, contentPane;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox sidebar, contentArea, studyMenu, communityMenu;
    @FXML private Label pageTitle, avatarLabel;
    @FXML private Button navHome, navStudy, navCourses, navNotes, navResources,
            navLessons, navPomodoro, navCommunity, navFireFeed,
            navContacts, navRooms, navSettings, navLogout;

    private Button activeNavButton;
    private boolean studyExpanded = true;
    private boolean communityExpanded = true;

    @FXML
    public void initialize() {
        User user = DemoDataService.getInstance().getCurrentUser();
        if (user != null) {
            avatarLabel.setText(user.getInitials());
        }
        loadContent("home_content");
        setActiveNav(navHome);
        pageTitle.setText("Home");
    }

    // Navigation methods
    @FXML private void navHome()     { loadPage("home_content", "Home", navHome); }
    @FXML private void navCourses()  { loadPage("courses_content", "My Courses", navCourses); }
    @FXML private void navNotes()    { loadPage("notes_content", "Notes", navNotes); }
    @FXML private void navResources(){ loadPage("resources_content", "Resources", navResources); }
    @FXML private void navLessons()  { loadPage("lessons_content", "My Lessons", navLessons); }
    @FXML private void navPomodoro() { loadPage("pomodoro_content", "Pomodoro", navPomodoro); }
    @FXML private void navFireFeed() { loadPage("firefeed_content", "Fire feed", navFireFeed); }
    @FXML private void navContacts() { loadPage("contacts_content", "Contacts", navContacts); }
    @FXML private void navRooms()    { loadPage("rooms_content", "Rooms", navRooms); }
    @FXML private void navSettings() { loadPage("settings_content", "Settings", navSettings); }

    @FXML
    private void toggleStudy() {
        studyExpanded = !studyExpanded;
        studyMenu.setVisible(studyExpanded);
        studyMenu.setManaged(studyExpanded);
        navStudy.setText(studyExpanded ? "  \u2610  Study  \u25BE" : "  \u2610  Study  \u25B8");
    }

    @FXML
    private void toggleCommunity() {
        communityExpanded = !communityExpanded;
        communityMenu.setVisible(communityExpanded);
        communityMenu.setManaged(communityExpanded);
        navCommunity.setText(communityExpanded ? "  \u2609  Community  \u25BE" : "  \u2609  Community  \u25B8");
    }

    @FXML
    private void handleLogout() {
        DemoDataService.getInstance().setCurrentUser(null);
        NavigationManager.getInstance().navigateTo("login");
    }

    private void loadPage(String fxmlName, String title, Button navBtn) {
        pageTitle.setText(title);
        setActiveNav(navBtn);
        loadContent(fxmlName);
    }

    private void loadContent(String fxmlName) {
        try {
            Node content = FXMLLoader.load(getClass().getResource("/view/" + fxmlName + ".fxml"));
            contentPane.getChildren().clear();
            contentPane.getChildren().add(content);
            scrollPane.setVvalue(0);

            FadeTransition ft = new FadeTransition(Duration.millis(200), content);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } catch (IOException e) {
            System.err.println("Failed to load content: " + fxmlName);
            e.printStackTrace();
            Label err = new Label("Page not found: " + fxmlName);
            err.setStyle("-fx-font-size: 18; -fx-text-fill: #E53E3E;");
            contentPane.getChildren().clear();
            contentPane.getChildren().add(err);
        }
    }

    private void setActiveNav(Button btn) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("sidebar-active");
        }
        activeNavButton = btn;
        if (btn != null) {
            btn.getStyleClass().add("sidebar-active");
        }
    }
}
