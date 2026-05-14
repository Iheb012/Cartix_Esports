package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import models.User;
import utils.MyDatabase;
import utils.PasswordUtil;
import utils.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button showBtn;
    @FXML private CheckBox rememberMe;

    @FXML private Label playersCountLabel;
    @FXML private Label tournamentsCountLabel;
    @FXML private Label liveCountLabel;

    private boolean passwordShown = false;

    @FXML
    public void initialize() {
        passwordVisible.textProperty()
                .bindBidirectional(passwordField.textProperty());
        loadStats();
    }

    private void loadStats() {
        try {
            Connection connection = MyDatabase.getInstance().getConnection();

            // Count players
            PreparedStatement ps1 = connection.prepareStatement(
                    "SELECT COUNT(*) FROM user WHERE role = 'JOUEUR'");
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                int count = rs1.getInt(1);
                playersCountLabel.setText(count >= 1000
                        ? (count / 1000) + "K+" : String.valueOf(count));
            }

            // Count tournaments
            PreparedStatement ps2 = connection.prepareStatement(
                    "SELECT COUNT(*) FROM tournament");
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                int count = rs2.getInt(1);
                tournamentsCountLabel.setText(count >= 1000
                        ? (count / 1000) + "K+" : String.valueOf(count));
            }

            // Count live tournaments
            PreparedStatement ps3 = connection.prepareStatement(
                    "SELECT COUNT(*) FROM tournament WHERE status = 'LIVE'");
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) {
                liveCountLabel.setText(String.valueOf(rs3.getInt(1)));
            }

        } catch (Exception e) {
            // Keep default values if DB query fails
            e.printStackTrace();
        }
    }

    @FXML
    private void togglePassword(ActionEvent event) {
        passwordShown = !passwordShown;
        if (passwordShown) {
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            showBtn.setText("Hide");
            passwordVisible.requestFocus();
            passwordVisible.positionCaret(passwordVisible.getText().length());
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            showBtn.setText("Show");
            passwordField.requestFocus();
        }
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    "Missing Fields", "Please enter your email and password.");
            return;
        }

        try {
            Connection connection = MyDatabase.getInstance().getConnection();
            String query = "SELECT * FROM user WHERE email = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("mdp");

                if (PasswordUtil.verify(password, storedHash)) {

                    User loggedUser = new User(
                            rs.getInt("Id"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("mdp"),
                            rs.getString("role")
                    );
                    Session.getInstance().setCurrentUser(loggedUser);

                    Stage stage = (Stage) emailField.getScene().getWindow();
                    String role = loggedUser.getRole();

                    if ("admin".equalsIgnoreCase(role)) {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/admin.fxml"));
                        Parent root = loader.load();
                        stage.setScene(new Scene(root, 1440, 960));
                    } else {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/dashboard.fxml"));
                        Parent root = loader.load();
                        DashboardController dc = loader.getController();
                        dc.setUser(loggedUser);
                        stage.setScene(new Scene(root, 1440, 960));
                    }

                    stage.show();

                } else {
                    showAlert(Alert.AlertType.ERROR,
                            "Login Failed", "Incorrect password. Please try again.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR,
                        "Login Failed", "No account found with this email address.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,
                    "Connection Error", "Could not connect to database: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreate(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signup.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page d'inscription.");
        }
    }

    @FXML
    private void handleGoogle(ActionEvent e) {
        showAlert(Alert.AlertType.INFORMATION, "Google OAuth", "Google login coming soon!");
    }

    @FXML
    private void handleDiscord(ActionEvent e) {
        showAlert(Alert.AlertType.INFORMATION, "Discord OAuth", "Discord login coming soon!");
    }

    @FXML
    private void handleForgot(ActionEvent e) {
        showAlert(Alert.AlertType.INFORMATION, "Forgot Password", "Password reset coming soon!");
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}