package controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.User;
import service.DemoDataService;
import util.AlertHelper;
import util.NavigationManager;

public class LoginController {

    @FXML private Button loginTab, signupTab;
    @FXML private VBox loginForm, signupForm;
    @FXML private TextField usernameField, nameField, emailField;
    @FXML private PasswordField passwordField, newPasswordField, confirmPasswordField;
    @FXML private RadioButton studentRadio, teacherRadio;
    @FXML private Label loginError, signupError, titleLabel;
    @FXML private ImageView mascotImage;

    private ToggleGroup roleGroup;

    @FXML
    public void initialize() {
        roleGroup = new ToggleGroup();
        studentRadio.setToggleGroup(roleGroup);
        teacherRadio.setToggleGroup(roleGroup);

        try {
            Image img = new Image(getClass().getResourceAsStream("/images/mascot_search.png"));
            mascotImage.setImage(img);
        } catch (Exception ignored) {}

        animateEntry();
    }

    @FXML
    private void switchToLogin() {
        titleLabel.setText("Welcome Back!");
        loginTab.getStyleClass().removeAll("tab-inactive");
        loginTab.getStyleClass().add("tab-active");
        signupTab.getStyleClass().removeAll("tab-active");
        signupTab.getStyleClass().add("tab-inactive");
        loginForm.setVisible(true); loginForm.setManaged(true);
        signupForm.setVisible(false); signupForm.setManaged(false);
        clearErrors();
        fadeIn(loginForm);
    }

    @FXML
    private void switchToSignup() {
        titleLabel.setText("Create Account");
        signupTab.getStyleClass().removeAll("tab-inactive");
        signupTab.getStyleClass().add("tab-active");
        loginTab.getStyleClass().removeAll("tab-active");
        loginTab.getStyleClass().add("tab-inactive");
        signupForm.setVisible(true); signupForm.setManaged(true);
        loginForm.setVisible(false); loginForm.setManaged(false);
        clearErrors();
        fadeIn(signupForm);
    }

    @FXML
    private void handleLogin() {
        clearErrors();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            AlertHelper.showError(loginError, "Username is required");
            return;
        }
        if (password.isEmpty()) {
            AlertHelper.showError(loginError, "Password is required");
            return;
        }

        // Try demo login with email
        String email = username.contains("@") ? username : username + "@univ-oran.dz";
        if (email.equals("bouchra@univ-oran.dz") || username.equalsIgnoreCase("bouchra")) {
            email = "bouchra@univ-oran.dz";
        }

        User user = DemoDataService.getInstance().login(email, password);
        if (user != null) {
            NavigationManager.getInstance().navigateTo("dashboard");
        } else {
            AlertHelper.showError(loginError, "Invalid username or password");
        }
    }

    @FXML
    private void handleSignup() {
        clearErrors();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();
        RadioButton selected = (RadioButton) roleGroup.getSelectedToggle();

        if (name.isEmpty()) { AlertHelper.showError(signupError, "Name is required"); return; }
        if (email.isEmpty()) { AlertHelper.showError(signupError, "Email is required"); return; }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            AlertHelper.showError(signupError, "Invalid email format"); return;
        }
        if (pass.length() < 8) { AlertHelper.showError(signupError, "Password must be at least 8 characters"); return; }
        if (!pass.equals(confirm)) { AlertHelper.showError(signupError, "Passwords do not match"); return; }
        if (selected == null) { AlertHelper.showError(signupError, "Please select a role"); return; }

        User.Role role = selected.getText().equals("Teacher") ? User.Role.TEACHER : User.Role.STUDENT;
        DemoDataService.getInstance().register(name, email, pass, role);
        NavigationManager.getInstance().navigateTo("dashboard");
    }

    private void clearErrors() {
        loginError.setVisible(false); loginError.setManaged(false);
        signupError.setVisible(false); signupError.setManaged(false);
    }

    private void fadeIn(VBox box) {
        FadeTransition ft = new FadeTransition(Duration.millis(250), box);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void animateEntry() {
        if (loginForm != null) {
            ScaleTransition st = new ScaleTransition(Duration.millis(400), loginForm);
            st.setFromX(0.95); st.setFromY(0.95); st.setToX(1); st.setToY(1);
            FadeTransition ft = new FadeTransition(Duration.millis(400), loginForm);
            ft.setFromValue(0); ft.setToValue(1);
            st.play(); ft.play();
        }
    }
}
