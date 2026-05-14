package controllers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import models.User;
import services.UserService;
import utils.PasswordUtil;

public class SignUpController {

    @FXML private TextField usernameField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button showBtn;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisible;
    @FXML private Button showConfirmBtn;
    @FXML private CheckBox agreeTerms;

    private boolean passwordShown = false;
    private boolean confirmPasswordShown = false;
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordVisible.textProperty().bindBidirectional(confirmPasswordField.textProperty());
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        String nom = usernameField.getText().trim();
        String prenom = (prenomField != null) ? prenomField.getText().trim() : "";
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (nom.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Mots de passe", "Les mots de passe ne correspondent pas.");
            return;
        }

        if (!agreeTerms.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Conditions", "Veuillez accepter les conditions d'utilisation.");
            return;
        }

        try {
            if (userService.findByEmail(email) != null) {
                showAlert(Alert.AlertType.WARNING, "Email existant", "Cet email est déjà utilisé.");
                return;
            }

            User newUser = new User(nom, prenom, email, PasswordUtil.hash(password), "JOUEUR");
            userService.add(newUser);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Compte créé avec succès !");
            handleGoLogin(event);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'inscription.");
        }
    }

    @FXML
    private void handleGoLogin(ActionEvent event) {
        navigateTo("/login.fxml", event);
    }

    @FXML
    private void togglePassword(ActionEvent event) {
        passwordShown = !passwordShown;
        if (passwordShown) {
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            showBtn.setText("Cacher");
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            showBtn.setText("Voir");
        }
    }

    @FXML
    private void toggleConfirmPassword(ActionEvent event) {
        confirmPasswordShown = !confirmPasswordShown;
        if (confirmPasswordShown) {
            confirmPasswordVisible.setVisible(true);
            confirmPasswordVisible.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            showConfirmBtn.setText("Cacher");
        } else {
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmPasswordVisible.setVisible(false);
            confirmPasswordVisible.setManaged(false);
            showConfirmBtn.setText("Voir");
        }
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page : " + fxmlPath);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}