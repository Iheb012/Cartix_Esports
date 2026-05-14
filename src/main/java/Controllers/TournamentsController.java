package Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.ExternalTournament;
import models.Joueur;
import models.Tournoi;
import models.User;
import services.*;
import utils.Session;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class TournamentsController {

    @FXML private VBox tournamentsBox;
    @FXML private VBox myTourneysBox;
    @FXML private VBox bracketDetailBox;
    @FXML private Label bracketTitle;
    @FXML private VBox bracketContent;
    @FXML private ScrollPane bracketScrollPane;
    @FXML private PasswordField hostPassword;
    @FXML private TextField hostName;
    @FXML private TextField hostPrize;
    @FXML private ComboBox<String> hostGame;
    @FXML private ComboBox<String> hostFormat;
    @FXML private ComboBox<Integer> hostSize;

    // External Tournaments
    @FXML private TableView<ExternalTournament> externalTable;
    @FXML private TableColumn<ExternalTournament, String> colSource;
    @FXML private TableColumn<ExternalTournament, String> colExtName;
    @FXML private TableColumn<ExternalTournament, String> colExtGame;
    @FXML private TableColumn<ExternalTournament, String> colExtDate;
    @FXML private TableColumn<ExternalTournament, String> colExtPrize;
    @FXML private TableColumn<ExternalTournament, String> colExtLocation;
    @FXML private TableColumn<ExternalTournament, Void> colExtLink;
    @FXML private ComboBox<String> extGameSelector;

    private TournoiService tournoiService;
    private JoueurService joueurService;

    private int currentUserId;
    private List<Tournoi> allTournois;
    private Tournoi currentSelectedTournament;
    private PandaScoreApiService pandaScoreService;
    @FXML
    public void initialize() {
        tournoiService = new TournoiService();
        joueurService = new JoueurService();
        pandaScoreService = new PandaScoreApiService();

        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getId();
        } else {
            currentUserId = 1;
        }

        hostGame.getItems().addAll("Valorant", "CS2", "League of Legends", "Rocket League", "Dota2", "Fortnite", "Apex");
        hostFormat.getItems().addAll("Single Elimination", "Double Elimination", "Round Robin");
        hostSize.getItems().addAll(4, 8, 16, 32, 64);

        if (extGameSelector != null) {
            extGameSelector.setValue("valorant");
            extGameSelector.setOnAction(e -> loadExternalTournaments());
        }

        setupExternalTable();
        loadTournaments();
        loadExternalTournaments();
    }

    private void setupExternalTable() {
        colSource.setCellValueFactory(new PropertyValueFactory<>("source"));
        colExtName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colExtGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colExtDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colExtPrize.setCellValueFactory(new PropertyValueFactory<>("prizePool"));
        colExtLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

        colExtLink.setCellFactory(col -> new TableCell<>() {
            private final Button linkBtn = new Button("Open");
            {
                linkBtn.setStyle("-fx-background-color: #388FFF; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 8;");
                linkBtn.setOnAction(e -> {
                    ExternalTournament t = getTableView().getItems().get(getIndex());
                    String url = t.getUrl();
                    if (url != null && !url.isEmpty() && !url.equals("#") && url.startsWith("http")) {
                        try {
                            // Ouvre dans le navigateur par défaut
                            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
                        } catch (Exception ex) {
                            System.err.println("Erreur: " + ex.getMessage());
                            showAlert("Erreur", "Impossible d'ouvrir: " + url);
                        }
                    } else {
                        showAlert("Info", "Lien non disponible");
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : linkBtn);
            }
        });
    }

    @FXML
    public void loadExternalTournaments() {
        if (extGameSelector == null) return;
        String game = extGameSelector.getValue();
        if (game == null) game = "valorant";

        String finalGame = game;

        new Thread(() -> {
            List<ExternalTournament> tournaments = pandaScoreService.getUpcomingTournaments(finalGame);
            ObservableList<ExternalTournament> finalList = FXCollections.observableArrayList(tournaments);
            Platform.runLater(() -> externalTable.setItems(finalList));
        }).start();
    }

    private void loadTournaments() {
        try {
            allTournois = tournoiService.getAll();
            tournamentsBox.getChildren().clear();
            myTourneysBox.getChildren().clear();

            for (Tournoi t : allTournois) {
                if (t.getOrganisateurId() == currentUserId) {
                    myTourneysBox.getChildren().add(createTournamentCard(t, true));
                } else {
                    tournamentsBox.getChildren().add(createTournamentCard(t, false));
                }
            }
            System.out.println("✅ " + allTournois.size() + " tournois chargés");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createTournamentCard(Tournoi t, boolean isMine) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #1C1C29; -fx-background-radius: 12; -fx-padding: 12; -fx-border-color: #2E2E44; -fx-border-radius: 12;");
        card.setSpacing(8);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        Label nameLabel = new Label(t.getNom());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label gameLabel = new Label(t.getJeu());
        gameLabel.setStyle("-fx-text-fill: #388FFF; -fx-font-size: 10px; -fx-background-color: #0F0F17; -fx-background-radius: 4; -fx-padding: 2 8;");

        header.getChildren().addAll(nameLabel, gameLabel);

        Label detailsLabel = new Label("📅 " + t.getDateDebut() + " → " + t.getDateFin() + " | 👥 " + t.getNbInscrits() + "/" + t.getMaxEquipe() + " équipes");
        detailsLabel.setStyle("-fx-text-fill: #949499; -fx-font-size: 11px;");

        Label statusLabel = new Label(t.getStatut());
        String statusColor;
        switch (t.getStatut().toUpperCase()) {
            case "LIVE": case "ONGOING": statusColor = "#ff4d6d"; break;
            case "COMPLETED": statusColor = "#22d98a"; break;
            default: statusColor = "#ffb347";
        }
        statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 10px; -fx-font-weight: bold;");

        HBox buttonsBox = new HBox();
        buttonsBox.setAlignment(Pos.CENTER_LEFT);
        buttonsBox.setSpacing(8);

        Button viewBtn = new Button("Voir détails");
        viewBtn.setStyle("-fx-background-color: #388FFF; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 12;");
        viewBtn.setOnAction(e -> showTournamentDetails(t));
        buttonsBox.getChildren().add(viewBtn);

        if (!isMine) {
            Button joinBtn = new Button("Rejoindre");
            joinBtn.setStyle("-fx-background-color: #2DFF9F; -fx-text-fill: #0F0F17; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 12;");
            joinBtn.setOnAction(e -> joinTournament(t));
            buttonsBox.getChildren().add(joinBtn);
        }

        if (isMine) {
            Button editBtn = new Button("Modifier");
            editBtn.setStyle("-fx-background-color: #FFB830; -fx-text-fill: #0F0F17; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 12;");
            editBtn.setOnAction(e -> editTournament(t));

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.setStyle("-fx-background-color: #FF4D6A; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 12;");
            deleteBtn.setOnAction(e -> deleteTournament(t));

            buttonsBox.getChildren().addAll(editBtn, deleteBtn);
        }

        card.getChildren().addAll(header, detailsLabel, statusLabel, buttonsBox);
        return card;
    }

    @FXML
    public void onCreateTournament() {
        try {
            String nom = hostName.getText();
            String jeu = hostGame.getValue();
            Integer maxEquipe = hostSize.getValue();

            if (nom.isEmpty() || jeu == null || maxEquipe == null) {
                showAlert("Champs manquants", "Veuillez remplir le nom, le jeu et le nombre d'équipes");
                return;
            }

            Tournoi t = new Tournoi();
            t.setNom(nom);
            t.setJeu(jeu);
            t.setDateDebut(Date.valueOf(LocalDate.now()));
            t.setDateFin(Date.valueOf(LocalDate.now().plusDays(7)));
            t.setMaxEquipe(maxEquipe);
            t.setStatut("PENDING");
            t.setOrganisateurId(currentUserId);
            if (hostPassword != null && !hostPassword.getText().isEmpty()) {
                t.setMotDePasse(hostPassword.getText());
            }

            tournoiService.add(t);

            hostName.clear();
            hostGame.setValue(null);
            hostSize.setValue(null);
            hostPrize.clear();
            if (hostPassword != null) hostPassword.clear();

            loadTournaments();
            showAlert("Succès", "Tournoi créé avec succès !");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Création échouée: " + e.getMessage());
        }
    }

    private void showTournamentDetails(Tournoi t) {
        currentSelectedTournament = t;
        bracketTitle.setText("🏆 " + t.getNom() + " - Bracket");
        displayBracket(t);
        bracketDetailBox.setVisible(true);
        bracketDetailBox.setManaged(true);
        bracketScrollPane.setVvalue(1.0);
    }

    @FXML
    private void hideBracketDetail() {
        bracketDetailBox.setVisible(false);
        bracketDetailBox.setManaged(false);
        bracketContent.getChildren().clear();
        currentSelectedTournament = null;
    }

    private void displayBracket(Tournoi t) {
        bracketContent.getChildren().clear();
        VBox bracketContainer = new VBox(15);
        bracketContainer.setAlignment(Pos.TOP_CENTER);

        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setStyle("-fx-background-color: #1C1C29; -fx-background-radius: 10; -fx-padding: 10;");
        Label teamsLabel = new Label("👥 " + t.getNbInscrits() + " / " + t.getMaxEquipe() + " joueurs");
        teamsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        Label statusLabel = new Label("📌 " + t.getStatut());
        statusLabel.setStyle("-fx-text-fill: #FFB830; -fx-font-size: 12px;");
        infoBox.getChildren().addAll(teamsLabel, statusLabel);
        bracketContainer.getChildren().add(infoBox);

        try {
            List<Joueur> inscrits = tournoiService.getJoueursInscrits(t.getId());
            if (inscrits.isEmpty()) {
                Label emptyLabel = new Label("Aucun joueur inscrit pour le moment.");
                emptyLabel.setStyle("-fx-text-fill: #949499; -fx-font-size: 12px;");
                bracketContainer.getChildren().add(emptyLabel);
            } else {
                Label inscritsTitle = new Label("📋 Joueurs inscrits (" + inscrits.size() + ")");
                inscritsTitle.setStyle("-fx-text-fill: #388FFF; -fx-font-size: 14px; -fx-font-weight: bold;");
                bracketContainer.getChildren().add(inscritsTitle);
                VBox playersList = new VBox(5);
                playersList.setStyle("-fx-padding: 10;");
                for (Joueur j : inscrits) {
                    HBox playerRow = new HBox(10);
                    playerRow.setAlignment(Pos.CENTER_LEFT);
                    playerRow.setStyle("-fx-background-color: #1C1C29; -fx-background-radius: 6; -fx-padding: 6 10;");
                    Label pseudoLabel = new Label("🎮 " + j.getPseudo());
                    pseudoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                    Label gameLabel = new Label(j.getGame());
                    gameLabel.setStyle("-fx-text-fill: #388FFF; -fx-font-size: 10px;");
                    playerRow.getChildren().addAll(pseudoLabel, gameLabel);
                    playersList.getChildren().add(playerRow);
                }
                bracketContainer.getChildren().add(playersList);
                if (inscrits.size() >= 4) {
                    HBox roundsBox = new HBox(40);
                    roundsBox.setAlignment(Pos.CENTER);
                    roundsBox.setStyle("-fx-padding: 20 0 0 0;");
                    VBox quarterFinal = createRoundBox("Quarts de finale", 4);
                    VBox semiFinal = createRoundBox("Demi-finales", 2);
                    VBox finale = createRoundBox("Finale", 1);
                    roundsBox.getChildren().addAll(quarterFinal, semiFinal, finale);
                    bracketContainer.getChildren().add(roundsBox);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Erreur chargement: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #FF4D6A; -fx-font-size: 12px;");
            bracketContainer.getChildren().add(errorLabel);
        }
        bracketContent.getChildren().add(bracketContainer);
    }

    private VBox createRoundBox(String title, int matchCount) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.TOP_CENTER);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #388FFF; -fx-font-size: 14px; -fx-font-weight: bold;");
        box.getChildren().add(titleLabel);
        for (int i = 0; i < matchCount; i++) {
            VBox matchCard = new VBox(5);
            matchCard.setStyle("-fx-background-color: #1C1C29; -fx-background-radius: 8; -fx-padding: 8; -fx-min-width: 160;");
            matchCard.setAlignment(Pos.CENTER);
            Label matchLabel = new Label("Match " + (i + 1));
            matchLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
            matchCard.getChildren().add(matchLabel);
            box.getChildren().add(matchCard);
        }
        return box;
    }

    private void editTournament(Tournoi t) {
        Dialog<Tournoi> dialog = new Dialog<>();
        dialog.setTitle("Modifier le tournoi");
        dialog.setHeaderText("Modifier: " + t.getNom());
        ButtonType saveButtonType = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        TextField nomField = new TextField(t.getNom());
        TextField jeuField = new TextField(t.getJeu());
        VBox content = new VBox(10);
        content.getChildren().addAll(new Label("Nom:"), nomField, new Label("Jeu:"), jeuField);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                t.setNom(nomField.getText());
                t.setJeu(jeuField.getText());
                return t;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(result -> {
            try {
                tournoiService.update(result);
                loadTournaments();
                showAlert("Succès", "Tournoi modifié !");
            } catch (Exception e) {
                showAlert("Erreur", "Modification échouée");
            }
        });
    }

    private void deleteTournament(Tournoi t) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer le tournoi '" + t.getNom() + "' ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    tournoiService.delete(t.getId());
                    loadTournaments();
                    showAlert("Succès", "Tournoi supprimé !");
                } catch (Exception e) {
                    showAlert("Erreur", "Suppression échouée");
                }
            }
        });
    }

    private void joinTournament(Tournoi t) {
        if (t.getNbInscrits() >= t.getMaxEquipe()) {
            showAlert("Tournoi complet", "Désolé, ce tournoi n'a plus de places disponibles.");
            return;
        }
        if (t.getMotDePasse() != null && !t.getMotDePasse().isEmpty()) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Mot de passe requis");
            dialog.setHeaderText("Tournoi protégé par mot de passe");
            dialog.setContentText("Entrez le mot de passe pour rejoindre:");
            dialog.showAndWait().ifPresent(password -> {
                if (password.equals(t.getMotDePasse())) {
                    registerForTournament(t);
                } else {
                    showAlert("Erreur", "Mot de passe incorrect");
                }
            });
        } else {
            registerForTournament(t);
        }
    }

    private void registerForTournament(Tournoi t) {
        try {
            User currentUser = Session.getInstance().getCurrentUser();
            if (currentUser == null) {
                showAlert("Erreur", "Vous devez être connecté");
                return;
            }
            Joueur joueur = joueurService.getByUserId(currentUser.getId());
            if (joueur == null) {
                showAlert("Erreur", "Aucun profil joueur trouvé pour cet utilisateur");
                return;
            }
            tournoiService.inscrireJoueur(t.getId(), joueur.getId());
            loadTournaments();
            showAlert("Succès", joueur.getPseudo() + " a rejoint le tournoi " + t.getNom());
            if (currentSelectedTournament != null && currentSelectedTournament.getId() == t.getId()) {
                displayBracket(t);
            }
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML public void filterAll() { loadTournaments(); }

    @FXML public void filterLive() {
        try {
            tournamentsBox.getChildren().clear();
            myTourneysBox.getChildren().clear();
            for (Tournoi t : tournoiService.getAll()) {
                if (t.getStatut().equals("ONGOING") || t.getStatut().equals("LIVE")) {
                    if (t.getOrganisateurId() == currentUserId) {
                        myTourneysBox.getChildren().add(createTournamentCard(t, true));
                    } else {
                        tournamentsBox.getChildren().add(createTournamentCard(t, false));
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void filterUpcoming() {
        try {
            tournamentsBox.getChildren().clear();
            myTourneysBox.getChildren().clear();
            for (Tournoi t : tournoiService.getAll()) {
                if (t.getStatut().equals("PENDING")) {
                    if (t.getOrganisateurId() == currentUserId) {
                        myTourneysBox.getChildren().add(createTournamentCard(t, true));
                    } else {
                        tournamentsBox.getChildren().add(createTournamentCard(t, false));
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void filterMine() {
        try {
            tournamentsBox.getChildren().clear();
            myTourneysBox.getChildren().clear();
            for (Tournoi t : tournoiService.getAll()) {
                if (t.getOrganisateurId() == currentUserId) {
                    myTourneysBox.getChildren().add(createTournamentCard(t, true));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void navigate(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxml));
            Stage stage = (Stage) tournamentsBox.getScene().getWindow();
            stage.setScene(new Scene(root, 1440, 960));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void navDashboard()   { navigate("dashboard.fxml"); }
    @FXML public void navPlayers()     { navigate("players.fxml"); }
    @FXML public void navMatches()     { navigate("matches.fxml"); }
    @FXML public void navTeams()       { navigate("teams.fxml"); }
    @FXML public void navTournaments() { navigate("tournaments.fxml"); }
    @FXML public void navAnalytics()   { navigate("analytics.fxml"); }
    @FXML public void navProduits()    { navigate("produit.fxml"); }
    @FXML public void navSponsors()    { navigate("sponsor.fxml"); }
}