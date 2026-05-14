package Controllers;

import com.google.api.services.oauth2.model.Userinfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.User;
import services.CallbackServer;
import services.DiscordAuthService;
import services.GoogleAuthService;
import services.UserService;
import utils.MyDatabase;
import utils.PasswordUtil;
import utils.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    private final UserService userService = new UserService();

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
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
        loadStats();
    }

    private void loadStats() {
        try {
            Connection connection = MyDatabase.getInstance().getConnection();

            PreparedStatement ps1 = connection.prepareStatement("SELECT COUNT(*) FROM user WHERE role = 'JOUEUR'");
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next() && playersCountLabel != null) {
                playersCountLabel.setText(String.valueOf(rs1.getInt(1)));
            }

            PreparedStatement ps2 = connection.prepareStatement("SELECT COUNT(*) FROM tournoi");
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next() && tournamentsCountLabel != null) {
                tournamentsCountLabel.setText(String.valueOf(rs2.getInt(1)));
            }

            PreparedStatement ps3 = connection.prepareStatement("SELECT COUNT(*) FROM tournoi WHERE statut = 'ONGOING'");
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next() && liveCountLabel != null) {
                liveCountLabel.setText(String.valueOf(rs3.getInt(1)));
            }

        } catch (Exception e) {
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
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            showBtn.setText("Show");
        }
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showStyledAlert("Champs manquants",
                    "Veuillez entrer votre email et votre mot de passe.",
                    "WARNING");
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

                    if ("admin".equalsIgnoreCase(loggedUser.getRole())) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin.fxml"));
                        Parent root = loader.load();
                        stage.setScene(new Scene(root, 1440, 960));
                    } else {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
                        Parent root = loader.load();

                        DashboardController dc = loader.getController();
                        dc.setUser(loggedUser);

                        stage.setScene(new Scene(root, 1440, 960));
                    }

                    stage.show();

                } else {
                    showStyledAlert("Connexion échouée",
                            "Mot de passe incorrect. Veuillez réessayer.",
                            "ERROR");
                }

            } else {
                showStyledAlert("Connexion échouée",
                        "Aucun compte trouvé avec cette adresse email.",
                        "ERROR");
            }

        } catch (Exception e) {
            showStyledAlert("Erreur de connexion",
                    "Impossible de se connecter à la base de données : " + e.getMessage(),
                    "ERROR");
        }
    }

    // ================= GOOGLE LOGIN =================
    @FXML
    private void handleGoogle(ActionEvent e) {
        new Thread(() -> {
            try {
                GoogleAuthService googleAuth = new GoogleAuthService();
                Userinfo userInfo = googleAuth.authenticate();

                String email = userInfo.getEmail();
                String name = userInfo.getName() != null ? userInfo.getName() : email;
                String[] parts = name.split(" ", 2);
                String prenom = parts[0];
                String nom = parts.length > 1 ? parts[1] : "";

                User user = userService.findByEmail(email);
                if (user == null) {
                    User newUser = new User(nom, prenom, email,
                            PasswordUtil.hash("GOOGLE_AUTH_" + email), "JOUEUR");
                    userService.add(newUser);
                    user = userService.findByEmail(email);
                }

                final User loggedUser = user;
                Session.getInstance().setCurrentUser(loggedUser);

                javafx.application.Platform.runLater(() -> {
                    try {
                        Stage stage = (Stage) emailField.getScene().getWindow();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
                        Parent root = loader.load();

                        DashboardController dc = loader.getController();
                        dc.setUser(loggedUser);

                        stage.setScene(new Scene(root, 1440, 960));
                        stage.show();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> showStyledAlert(
                        "Google Login",
                        "Échec de l'authentification Google : " + ex.getMessage(),
                        "ERROR"
                ));
            }
        }).start();
    }

    // ================= DISCORD LOGIN =================
    @FXML
    private void handleDiscord(ActionEvent e) {
        new Thread(() -> {
            try {
                CallbackServer.reset();
                CallbackServer.start();

                DiscordAuthService discord = new DiscordAuthService();

                javafx.application.Platform.runLater(() -> {
                    try {
                        java.awt.Desktop.getDesktop().browse(
                                new java.net.URI(discord.getAuthUrl())
                        );
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                String code = null;
                int timeout = 0;

                while ((code = CallbackServer.getCode()) == null && timeout < 120) {
                    Thread.sleep(1000);
                    timeout++;
                }

                if (code == null) {
                    javafx.application.Platform.runLater(() ->
                            showStyledAlert("Connexion Discord",
                                    "Délai de connexion dépassé.",
                                    "ERROR")
                    );
                    return;
                }

                String token = discord.getAccessToken(code);
                org.json.JSONObject userInfo = discord.getUserInfo(token);

                String email = userInfo.optString("email", "");
                String username = userInfo.getString("username");
                String id = userInfo.getString("id");

                if (email.isEmpty()) email = id + "@discord.com";

                User user = userService.findByEmail(email);
                if (user == null) {
                    User newUser = new User("", username, email,
                            PasswordUtil.hash("DISCORD_AUTH_" + email), "JOUEUR");
                    userService.add(newUser);
                    user = userService.findByEmail(email);
                }

                final User loggedUser = user;
                Session.getInstance().setCurrentUser(loggedUser);

                javafx.application.Platform.runLater(() -> {
                    try {
                        Stage stage = (Stage) emailField.getScene().getWindow();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
                        Parent root = loader.load();

                        DashboardController dc = loader.getController();
                        dc.setUser(loggedUser);

                        stage.setScene(new Scene(root, 1440, 960));
                        stage.show();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

            } catch (Exception ex) {
                javafx.application.Platform.runLater(() ->
                        showStyledAlert("Discord Login",
                                "Échec de l'authentification Discord : " + ex.getMessage(),
                                "ERROR")
                );
            }
        }).start();
    }

    // ================= FORGOT / SIGNUP =================
    @FXML
    private void handleForgot(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forgot_password.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ex) {
            showStyledAlert("Erreur", "Impossible de charger la page.", "ERROR");
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
            showStyledAlert("Erreur",
                    "Impossible de charger la page d'inscription.",
                    "ERROR");
        }
    }

    // ================= ALERT =================
    private void showStyledAlert(String title, String message, String type) {
        Stage alertStage = new Stage();
        alertStage.setTitle(title);
        alertStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        Label label = new Label(message);
        Button ok = new Button("OK");
        ok.setOnAction(e -> alertStage.close());

        VBox box = new VBox(label, ok);
        Scene scene = new Scene(box, 300, 150);

        alertStage.setScene(scene);
        alertStage.show();
    }
}