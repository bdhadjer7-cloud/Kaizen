package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.User;
import service.DemoDataService;
import util.AlertHelper;

public class SettingsContentController {

    @FXML private VBox profilePane, securityPane, notifPane, studyPane, appearPane;
    @FXML private Button profileTab, securityTab, notifTab, studyTab, appearTab;
    @FXML private TextField nameField, usernameField, emailField, universityField;
    @FXML private TextArea bioField;
    @FXML private PasswordField currentPassField, newPassField, confirmPassField;
    @FXML private Label profileMsg, securityMsg, avatarInitial;

    private Button activeTab;

    @FXML
    public void initialize() {
        activeTab = profileTab;
        User user = DemoDataService.getInstance().getCurrentUser();
        if (user != null) {
            nameField.setText(user.getName());
            String un = user.getMeta("username", String.class);
            usernameField.setText(un != null ? un : "");
            emailField.setText(user.getEmail());
            String uni = user.getMeta("university", String.class);
            universityField.setText(uni != null ? uni : "");
            bioField.setText(user.getBio() != null ? user.getBio() : "");
            avatarInitial.setText(user.getInitials());
        }
    }

    @FXML private void showProfile()       { switchSection(profilePane, profileTab); }
    @FXML private void showSecurity()      { switchSection(securityPane, securityTab); }
    @FXML private void showNotifications() { switchSection(notifPane, notifTab); }
    @FXML private void showStudyPrefs()    { switchSection(studyPane, studyTab); }
    @FXML private void showAppearance()    { switchSection(appearPane, appearTab); }

    @FXML
    private void saveProfile() {
        User user = DemoDataService.getInstance().getCurrentUser();
        if (user != null) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                AlertHelper.showError(profileMsg, "Name cannot be empty");
                return;
            }
            user.setName(name);
            user.setEmail(emailField.getText().trim());
            user.setBio(bioField.getText().trim());
            user.setMeta("university", universityField.getText().trim());
            user.setMeta("username", usernameField.getText().trim());
            AlertHelper.showSuccess(profileMsg, "Profile saved successfully!");
        }
    }

    @FXML
    private void updatePassword() {
        String current = currentPassField.getText();
        String newPass = newPassField.getText();
        String confirm = confirmPassField.getText();

        if (current.isEmpty()) {
            AlertHelper.showError(securityMsg, "Current password required");
            return;
        }
        if (newPass.length() < 8) {
            AlertHelper.showError(securityMsg, "Password must be at least 8 characters");
            return;
        }
        if (!newPass.equals(confirm)) {
            AlertHelper.showError(securityMsg, "Passwords do not match");
            return;
        }
        AlertHelper.showSuccess(securityMsg, "Password updated successfully!");
        currentPassField.clear();
        newPassField.clear();
        confirmPassField.clear();
    }

    private void switchSection(VBox pane, Button tab) {
        profilePane.setVisible(false); profilePane.setManaged(false);
        securityPane.setVisible(false); securityPane.setManaged(false);
        notifPane.setVisible(false); notifPane.setManaged(false);
        studyPane.setVisible(false); studyPane.setManaged(false);
        appearPane.setVisible(false); appearPane.setManaged(false);

        pane.setVisible(true);
        pane.setManaged(true);

        if (activeTab != null) activeTab.getStyleClass().remove("sidebar-active");
        activeTab = tab;
        tab.getStyleClass().add("sidebar-active");
    }
}
