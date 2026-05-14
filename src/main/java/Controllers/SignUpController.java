package Controllers;

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
            showStyledAlert("Champs manquants",
                    "Veuillez remplir tous les champs.", "WARNING");
            return;
        }
        if (password.length() < 6) {
            showStyledAlert(
                    "Mot de passe faible",
                    "Le mot de passe doit contenir au moins 6 caractères.",
                    "WARNING"
            );
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showStyledAlert(
                    "Email invalide",
                    "Veuillez entrer une adresse email valide.",
                    "WARNING"
            );
            return;
        }
        if (!password.equals(confirmPassword)) {
            showStyledAlert("Mots de passe",
                    "Les mots de passe ne correspondent pas.", "WARNING");
            return;
        }

        if (!agreeTerms.isSelected()) {
            showStyledAlert("Conditions",
                    "Veuillez accepter les conditions d'utilisation.", "WARNING");
            return;
        }

        try {
            if (userService.findByEmail(email) != null) {
                showStyledAlert("Email existant",
                        "Cet email est déjà utilisé.", "WARNING");
                return;
            }

            User newUser = new User(nom, prenom, email, PasswordUtil.hash(password), "JOUEUR");
            userService.add(newUser);

            showStyledAlert("Succès",
                    "Compte créé avec succès !", "SUCCESS");
            handleGoLogin(event);
        } catch (Exception e) {
            e.printStackTrace();
            showStyledAlert("Erreur",
                    "Une erreur est survenue lors de l'inscription.", "ERROR");
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
    private void showStyledAlert(String title, String message, String type) {
        Stage alertStage = new Stage();
        alertStage.setTitle(title);
        alertStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        // Icon
        String icon = switch (type) {
            case "SUCCESS" -> "✓";
            case "ERROR"   -> "✕";
            case "WARNING" -> "⚠";
            default        -> "ℹ";
        };
        String iconColor = switch (type) {
            case "SUCCESS" -> "#22d98a";
            case "ERROR"   -> "#ff4d6d";
            case "WARNING" -> "#ffb347";
            default        -> "#4d78ff";
        };
        String borderColor = switch (type) {
            case "SUCCESS" -> "rgba(34,217,138,0.2)";
            case "ERROR"   -> "rgba(255,77,109,0.2)";
            case "WARNING" -> "rgba(255,179,71,0.2)";
            default        -> "rgba(77,120,255,0.2)";
        };

        // Icon circle
        javafx.scene.layout.StackPane iconPane =
                new javafx.scene.layout.StackPane();
        iconPane.setPrefSize(60, 60);
        iconPane.setStyle(
                "-fx-background-color: " + iconColor + "22;" +
                        "-fx-border-color: " + iconColor + ";" +
                        "-fx-border-radius: 50;" +
                        "-fx-background-radius: 50;"
        );
        Label iconLabel = new Label(icon);
        iconLabel.setStyle(
                "-fx-text-fill: " + iconColor + ";" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;"
        );
        iconPane.getChildren().add(iconLabel);

        // Title label
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;"
        );

        // Message label
        Label msgLabel = new Label(message);
        msgLabel.setStyle(
                "-fx-text-fill: #9aa3c7;" +
                        "-fx-font-size: 13px;"
        );
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(280);
        msgLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // OK Button
        Button okBtn = new Button("OK");
        okBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #4d78ff, #7c4dff);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10 40;" +
                        "-fx-cursor: hand;"
        );
        okBtn.setOnAction(ev -> alertStage.close());

        // Layout
        javafx.scene.layout.VBox content =
                new javafx.scene.layout.VBox(16);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setPadding(new javafx.geometry.Insets(32));
        content.setStyle(
                "-fx-background-color: #111524;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-radius: 16;" +
                        "-fx-background-radius: 16;"
        );
        content.getChildren().addAll(iconPane, titleLabel, msgLabel, okBtn);

        // Orb decorations
        javafx.scene.shape.Circle orb1 = new javafx.scene.shape.Circle(80);
        orb1.setFill(javafx.scene.paint.Color.web("#4d78ff", 0.08));
        orb1.setTranslateX(-120);
        orb1.setTranslateY(-80);

        javafx.scene.shape.Circle orb2 = new javafx.scene.shape.Circle(60);
        orb2.setFill(javafx.scene.paint.Color.web("#7c4dff", 0.06));
        orb2.setTranslateX(120);
        orb2.setTranslateY(80);

        javafx.scene.layout.StackPane root =
                new javafx.scene.layout.StackPane();
        root.getChildren().addAll(orb1, orb2, content);
        root.setStyle("-fx-background-color: #111524;" +
                "-fx-background-radius: 16;");

        Scene scene = new Scene(root, 360, 260);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        alertStage.setScene(scene);
        alertStage.show();
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