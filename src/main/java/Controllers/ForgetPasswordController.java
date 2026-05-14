package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.EmailService;
import services.UserService;
import utils.MyDatabase;
import utils.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ForgetPasswordController {

    @FXML private VBox stepEmail;
    @FXML private VBox stepCode;
    @FXML private VBox stepNewPassword;

    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label codeSubLabel;

    private String generatedCode;
    private String userEmail;

    private final EmailService emailService = new EmailService();
    private final UserService userService = new UserService();

    // ================= ALERT =================
    private void showStyledAlert(String title, String message, String type) {
        Stage alertStage = new Stage();
        alertStage.setTitle(title);
        alertStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        String icon = switch (type) {
            case "SUCCESS" -> "✓";
            case "ERROR" -> "✕";
            case "WARNING" -> "⚠";
            default -> "ℹ";
        };

        String iconColor = switch (type) {
            case "SUCCESS" -> "#22d98a";
            case "ERROR" -> "#ff4d6d";
            case "WARNING" -> "#ffb347";
            default -> "#4d78ff";
        };

        String borderColor = switch (type) {
            case "SUCCESS" -> "rgba(34,217,138,0.2)";
            case "ERROR" -> "rgba(255,77,109,0.2)";
            case "WARNING" -> "rgba(255,179,71,0.2)";
            default -> "rgba(77,120,255,0.2)";
        };

        StackPane iconPane = new StackPane();
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

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-text-fill: #9aa3c7; -fx-font-size: 13px;");
        msgLabel.setWrapText(true);

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> alertStage.close());

        VBox content = new VBox(15, iconPane, titleLabel, msgLabel, okBtn);
        content.setStyle(
                "-fx-background-color: #111524;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-radius: 16;"
        );

        StackPane root = new StackPane(content);
        Scene scene = new Scene(root, 360, 260);

        alertStage.setScene(scene);
        alertStage.show();
    }

    // ================= SEND CODE =================
    @FXML
    private void handleSendCode(ActionEvent event) {

        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showStyledAlert("Champ vide", "Veuillez entrer votre email.", "WARNING");
            return;
        }

        if (userService.findByEmail(email) == null) {
            showStyledAlert("Email introuvable", "Aucun compte trouvé avec cet email.", "ERROR");
            return;
        }

        new Thread(() -> {
            try {
                generatedCode = emailService.sendResetCode(email);
                userEmail = email;

                javafx.application.Platform.runLater(() -> {
                    codeSubLabel.setText("We sent a code to " + email);
                    showStep(stepCode);

                    showStyledAlert(
                            "Code envoyé",
                            "Un code de réinitialisation a été envoyé à " + email,
                            "SUCCESS"
                    );
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        showStyledAlert("Erreur",
                                "Impossible d'envoyer l'email : " + e.getMessage(),
                                "ERROR")
                );
            }
        }).start();
    }

    // ================= VERIFY CODE =================
    @FXML
    private void handleVerifyCode(ActionEvent event) {

        String enteredCode = codeField.getText().trim();

        if (enteredCode.isEmpty()) {
            showStyledAlert("Champ vide", "Veuillez entrer le code.", "WARNING");
            return;
        }

        if (enteredCode.equals(generatedCode)) {
            showStep(stepNewPassword);
        } else {
            showStyledAlert("Code incorrect",
                    "Le code entré est incorrect. Veuillez réessayer.",
                    "ERROR");
        }
    }

    // ================= RESET PASSWORD =================
    @FXML
    private void handleResetPassword(ActionEvent event) {

        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showStyledAlert("Champs vides", "Veuillez remplir tous les champs.", "WARNING");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showStyledAlert("Erreur", "Les mots de passe ne correspondent pas.", "ERROR");
            return;
        }

        if (newPassword.length() < 6) {
            showStyledAlert("Mot de passe faible",
                    "Le mot de passe doit contenir au moins 6 caractères.",
                    "WARNING");
            return;
        }

        try {
            Connection connection = MyDatabase.getInstance().getConnection();
            String query = "UPDATE user SET mdp = ? WHERE email = ?";
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, PasswordUtil.hash(newPassword));
            ps.setString(2, userEmail);

            ps.executeUpdate();

            showStyledAlert("Succès",
                    "Mot de passe réinitialisé avec succès !",
                    "SUCCESS");

            handleBackToLogin(event);

        } catch (Exception e) {
            showStyledAlert("Erreur",
                    "Impossible de réinitialiser le mot de passe.",
                    "ERROR");
        }
    }

    // ================= NAVIGATION =================
    @FXML
    private void handleBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            showStyledAlert("Erreur",
                    "Impossible de charger la page de connexion.",
                    "ERROR");
        }
    }

    @FXML
    private void handleBackToEmail(ActionEvent event) {
        showStep(stepEmail);
    }

    // ================= STEP SWITCH =================
    private void showStep(VBox step) {

        stepEmail.setVisible(false);
        stepEmail.setManaged(false);

        stepCode.setVisible(false);
        stepCode.setManaged(false);

        stepNewPassword.setVisible(false);
        stepNewPassword.setManaged(false);

        step.setVisible(true);
        step.setManaged(true);
    }
}