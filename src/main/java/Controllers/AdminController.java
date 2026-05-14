package Controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Joueur;
import models.Tournoi;
import services.AdminService;
import services.JoueurService;
import services.TournoiService;
import services.UserService;
import utils.Session;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    // ─── Pages ───────────────────────────────────────────────────
    @FXML private ScrollPane pageDashboard;
    @FXML private ScrollPane pageUsers;
    @FXML private ScrollPane pagePlayers;
    @FXML private ScrollPane pageMatches;
    @FXML private ScrollPane pageTeams;
    @FXML private ScrollPane pageTournaments;
    @FXML private ScrollPane pageModeration;
    @FXML private ScrollPane pageSettings;

    // ─── Nav ─────────────────────────────────────────────────────
    @FXML private HBox navDashboard;
    @FXML private HBox navUsers;
    @FXML private HBox navPlayers;
    @FXML private HBox navMatches;
    @FXML private HBox navTeams;
    @FXML private HBox navTournaments;
    @FXML private HBox navReports;
    @FXML private HBox navModeration;
    @FXML private HBox navSettings;

    // ─── Topbar ──────────────────────────────────────────────────
    @FXML private Label     pageTitle;
    @FXML private Label     pageSubtitle;
    @FXML private TextField searchField;
    @FXML private Button    actionBtn;
    @FXML private Label     statusLabel;
    @FXML private Label     clockLabel;
    @FXML private Label     userCountLabel;

    // ─── Dashboard ───────────────────────────────────────────────
    @FXML private BarChart<String, Number> registrationsChart;
    @FXML private VBox activityFeed;
    @FXML private VBox topPlayersList;
    @FXML private VBox flaggedList;
    @FXML private VBox liveMatchesList;

    // ─── Dashboard stat labels ────────────────────────────────────
    @FXML private Label totalUsersLabel;
    @FXML private Label totalTournamentsLabel;
    @FXML private Label liveNowLabel;

    // ─── Users Table ─────────────────────────────────────────────
    @FXML private TableView<UserService.UserRow>            usersTable;
    @FXML private TableColumn<UserService.UserRow, Integer> colUserId;
    @FXML private TableColumn<UserService.UserRow, String>  colUserName;
    @FXML private TableColumn<UserService.UserRow, String>  colUserEmail;
    @FXML private TableColumn<UserService.UserRow, String>  colUserRole;
    @FXML private TableColumn<UserService.UserRow, Void>    colUserActions;
    @FXML private ComboBox<String> filterRole;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterGame;

    // ─── Settings ────────────────────────────────────────────────
    @FXML private TextField settingPlatformName;
    @FXML private TextField settingSeasonName;
    @FXML private TextField settingMaxTeams;
    @FXML private CheckBox  settingMaintenance;
    @FXML private CheckBox  settingRegistrations;
    // Ajoute ces lignes dans la section des @FXML (vers ligne 50-70)

    // === PLAYERS TABLE ===
    @FXML private TableView<Joueur> playersTable;
    @FXML private TableColumn<Joueur, Integer> colRank;
    @FXML private TableColumn<Joueur, String> colHandle;
    @FXML private TableColumn<Joueur, String> colGame;
    @FXML private TableColumn<Joueur, String> colTeam;
    @FXML private TableColumn<Joueur, Double> colKda;
    @FXML private TableColumn<Joueur, String> colWinRate;
    @FXML private TableColumn<Joueur, Integer> colMatches;
    @FXML private TableColumn<Joueur, Double> colRating;
    @FXML private TableColumn<Joueur, String> colPlayerStatus;
    @FXML private TableColumn<Joueur, Void> colPlayerActions;
    @FXML private ComboBox<String> filterPlayerGame;

    // === TOURNAMENTS TABLE ===
    @FXML private TableView<Tournoi> tournamentsTable;
    @FXML private TableColumn<Tournoi, Integer> colTournId;
    @FXML private TableColumn<Tournoi, String> colTournName;
    @FXML private TableColumn<Tournoi, String> colTournGame;
    @FXML private TableColumn<Tournoi, Integer> colTournTeams;
    @FXML private TableColumn<Tournoi, String> colTournPrize;
    @FXML private TableColumn<Tournoi, String> colTournDate;
    @FXML private TableColumn<Tournoi, String> colTournStatus;
    @FXML private TableColumn<Tournoi, Void> colTournActions;
    // ─── Data ────────────────────────────────────────────────────
    private final ObservableList<UserService.UserRow> allUsers =
            FXCollections.observableArrayList();

    private final AdminService adminService = new AdminService();
    private HBox currentNav;

    // ═══════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ═══════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentNav = navDashboard;
        loadUsersFromDB();
        setupPlayersTable();      // ← AJOUTE
        setupTournamentsTable();  // ← AJOUTE
        setupComboBoxes();
        setupUsersTable();
        populateDashboardStats();
        populateDashboardCharts();
        startClock();
    }

    // ═══════════════════════════════════════════════════════════════
    //  LOAD FROM DATABASE
    // ═══════════════════════════════════════════════════════════════
    private void loadUsersFromDB() {
        allUsers.clear();
        allUsers.addAll(adminService.getAllUsers());
    }
    // Dans initialize(), après loadUsersFromDB()
    private void setupPlayersTable() {
        try {
            JoueurService joueurService = new JoueurService();
            List<Joueur> joueurs = joueurService.getAll();
            ObservableList<Joueur> playerList = FXCollections.observableArrayList(joueurs);
            playersTable.setItems(playerList);

            colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
            colHandle.setCellValueFactory(new PropertyValueFactory<>("pseudo"));
            colGame.setCellValueFactory(new PropertyValueFactory<>("game"));
            colTeam.setCellValueFactory(new PropertyValueFactory<>("equipeId"));
            colKda.setCellValueFactory(new PropertyValueFactory<>("kda"));
            colWinRate.setCellValueFactory(new PropertyValueFactory<>("winRate"));
            colMatches.setCellValueFactory(new PropertyValueFactory<>("matches"));
            colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
            colPlayerStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Actions pour les joueurs (Modifier, Supprimer, Bannir)
            colPlayerActions.setCellFactory(col -> new TableCell<>() {
                private final Button editBtn = makeSmallBtn("Edit", "#4d78ff");
                private final Button banBtn = makeSmallBtn("Ban", "#ff4d6d");
                private final Button deleteBtn = makeSmallBtn("Delete", "#6b7394");
                private final HBox box = new HBox(6, editBtn, banBtn, deleteBtn);
                {
                    box.setAlignment(Pos.CENTER_LEFT);
                    editBtn.setOnAction(e -> {
                        Joueur j = getTableView().getItems().get(getIndex());
                        editPlayer(j);
                    });
                    banBtn.setOnAction(e -> {
                        Joueur j = getTableView().getItems().get(getIndex());
                        j.setStatus("BANNED");
                        try {
                            joueurService.update(j);
                            setupPlayersTable();
                            setStatus("Banned: " + j.getPseudo());
                        } catch (Exception ex) { ex.printStackTrace(); }
                    });
                    deleteBtn.setOnAction(e -> {
                        Joueur j = getTableView().getItems().get(getIndex());
                        try {
                            joueurService.delete(j.getId());
                            setupPlayersTable();
                            setStatus("Deleted: " + j.getPseudo());
                        } catch (Exception ex) { ex.printStackTrace(); }
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : box);
                }
            });

            // Filtre par jeu
            if (filterPlayerGame != null) {
                filterPlayerGame.getItems().addAll("All Games", "Valorant", "CS2", "League of Legends");
                filterPlayerGame.setOnAction(e -> filterPlayersByGame());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTournamentsTable() {
        try {
            TournoiService tournoiService = new TournoiService();
            List<Tournoi> tournois = tournoiService.getAll();
            ObservableList<Tournoi> tournamentList = FXCollections.observableArrayList(tournois);
            tournamentsTable.setItems(tournamentList);

            colTournId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colTournName.setCellValueFactory(new PropertyValueFactory<>("nom"));
            colTournGame.setCellValueFactory(new PropertyValueFactory<>("jeu"));
            colTournTeams.setCellValueFactory(new PropertyValueFactory<>("maxEquipe"));
            colTournPrize.setCellValueFactory(new PropertyValueFactory<>("prizePool"));
            colTournDate.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
            colTournStatus.setCellValueFactory(new PropertyValueFactory<>("statut"));

            // Actions pour les tournois
            colTournActions.setCellFactory(col -> new TableCell<>() {
                private final Button editBtn = makeSmallBtn("Edit", "#4d78ff");
                private final Button deleteBtn = makeSmallBtn("Delete", "#ff4d6d");
                private final Button forceBtn = makeSmallBtn("Force Close", "#ffb347");
                private final HBox box = new HBox(6, editBtn, forceBtn, deleteBtn);
                {
                    box.setAlignment(Pos.CENTER_LEFT);
                    editBtn.setOnAction(e -> {
                        Tournoi t = getTableView().getItems().get(getIndex());
                        editTournament(t);
                    });
                    forceBtn.setOnAction(e -> {
                        Tournoi t = getTableView().getItems().get(getIndex());
                        try {
                            t.setStatut("COMPLETED");
                            tournoiService.update(t);
                            setupTournamentsTable();
                            setStatus("Tournament closed: " + t.getNom());
                        } catch (Exception ex) { ex.printStackTrace(); }
                    });
                    deleteBtn.setOnAction(e -> {
                        Tournoi t = getTableView().getItems().get(getIndex());
                        try {
                            tournoiService.delete(t.getId());
                            setupTournamentsTable();
                            setStatus("Deleted: " + t.getNom());
                        } catch (Exception ex) { ex.printStackTrace(); }
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : box);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterPlayersByGame() {
        String selectedGame = filterPlayerGame.getValue();
        if (selectedGame == null || "All Games".equals(selectedGame)) {
            setupPlayersTable();
            return;
        }
        try {
            JoueurService joueurService = new JoueurService();
            List<Joueur> filtered = joueurService.getAll().stream()
                    .filter(j -> selectedGame.equals(j.getGame()))
                    .toList();
            playersTable.setItems(FXCollections.observableArrayList(filtered));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void editPlayer(Joueur j) {
        Dialog<Joueur> dialog = new Dialog<>();
        dialog.setTitle("Edit Player");
        dialog.setHeaderText("Editing: " + j.getPseudo());

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField pseudoField = new TextField(j.getPseudo());
        TextField gameField = new TextField(j.getGame());
        TextField kdaField = new TextField(String.valueOf(j.getKda()));
        TextField winRateField = new TextField(j.getWinRate());
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("ACTIVE", "BANNED", "INACTIVE");
        statusCombo.setValue(j.getStatus());

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 10;");
        content.getChildren().addAll(
                new Label("Pseudo:"), pseudoField,
                new Label("Game:"), gameField,
                new Label("KDA:"), kdaField,
                new Label("Win Rate:"), winRateField,
                new Label("Status:"), statusCombo
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                j.setPseudo(pseudoField.getText());
                j.setGame(gameField.getText());
                j.setKda(Double.parseDouble(kdaField.getText()));
                j.setWinRate(winRateField.getText());
                j.setStatus(statusCombo.getValue());
                return j;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                JoueurService joueurService = new JoueurService();
                joueurService.update(result);
                setupPlayersTable();
                setStatus("Player updated: " + result.getPseudo());
            } catch (Exception e) {
                showInfo("Error", "Update failed: " + e.getMessage());
            }
        });
    }

    private void editTournament(Tournoi t) {
        Dialog<Tournoi> dialog = new Dialog<>();
        dialog.setTitle("Edit Tournament");
        dialog.setHeaderText("Editing: " + t.getNom());

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField nomField = new TextField(t.getNom());
        TextField jeuField = new TextField(t.getJeu());
        TextField maxEquipeField = new TextField(String.valueOf(t.getMaxEquipe()));
        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("PENDING", "ONGOING", "COMPLETED", "CANCELLED");
        statutCombo.setValue(t.getStatut());

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 10;");
        content.getChildren().addAll(
                new Label("Name:"), nomField,
                new Label("Game:"), jeuField,
                new Label("Max Teams:"), maxEquipeField,
                new Label("Status:"), statutCombo
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                t.setNom(nomField.getText());
                t.setJeu(jeuField.getText());
                t.setMaxEquipe(Integer.parseInt(maxEquipeField.getText()));
                t.setStatut(statutCombo.getValue());
                return t;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                TournoiService tournoiService = new TournoiService();
                tournoiService.update(result);
                setupTournamentsTable();
                setStatus("Tournament updated: " + result.getNom());
            } catch (Exception e) {
                showInfo("Error", "Update failed: " + e.getMessage());
            }
        });
    }
    // ═══════════════════════════════════════════════════════════════
    //  DASHBOARD STATS FROM DB
    // ═══════════════════════════════════════════════════════════════
    private void populateDashboardStats() {
        int users = adminService.countUsers();
        int tournaments = adminService.countTournaments();
        int live = adminService.countLive();

        if (totalUsersLabel != null)
            totalUsersLabel.setText(users >= 1000
                    ? (users / 1000) + "K+" : String.valueOf(users));
        if (totalTournamentsLabel != null)
            totalTournamentsLabel.setText(tournaments >= 1000
                    ? (tournaments / 1000) + "K+" : String.valueOf(tournaments));
        if (liveNowLabel != null)
            liveNowLabel.setText(String.valueOf(live));
        if (userCountLabel != null)
            userCountLabel.setText(users + " users");
    }

    // ═══════════════════════════════════════════════════════════════
    //  DASHBOARD CHARTS (static demo data)
    // ═══════════════════════════════════════════════════════════════
    private void populateDashboardCharts() {
        if (registrationsChart != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Registrations");
            String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
            int[] vals = {420, 380, 510, 470, 620, 780, 540};
            for (int i = 0; i < days.length; i++)
                series.getData().add(new XYChart.Data<>(days[i], vals[i]));
            registrationsChart.getData().add(series);
            registrationsChart.setLegendVisible(false);
        }

        if (activityFeed != null) {
            List<String[]> activities = List.of(
                    new String[]{"#22d98a", "New user registered",    "2m ago"},
                    new String[]{"#4d78ff", "Tournament created",     "8m ago"},
                    new String[]{"#ff3b5c", "Report filed: Cheating", "15m ago"},
                    new String[]{"#ffb347", "Match scheduled",        "32m ago"},
                    new String[]{"#22d98a", "Team verified",          "1h ago"}
            );
            for (String[] a : activities) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(9, 0, 9, 0));
                row.setStyle("-fx-border-color:rgba(255,255,255,0.04);" +
                        "-fx-border-width:0 0 1 0;");
                Rectangle dot = new Rectangle(6, 6);
                dot.setArcWidth(6); dot.setArcHeight(6);
                dot.setFill(Color.web(a[0]));
                Label msg = new Label(a[1]);
                msg.setStyle("-fx-text-fill:#e8eaf6; -fx-font-size:12;");
                HBox.setHgrow(msg, Priority.ALWAYS);
                Label time = new Label(a[2]);
                time.setStyle("-fx-text-fill:#6b7394; -fx-font-size:11;");
                row.getChildren().addAll(dot, msg, time);
                activityFeed.getChildren().add(row);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  CLOCK
    // ═══════════════════════════════════════════════════════════════
    private void startClock() {
        if (clockLabel == null) return;
        DateTimeFormatter fmt =
                DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm:ss");
        Timeline clock = new Timeline(
                new KeyFrame(Duration.seconds(1),
                        e -> clockLabel.setText(
                                LocalDateTime.now().format(fmt)))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        clockLabel.setText(LocalDateTime.now().format(fmt));
    }

    // ═══════════════════════════════════════════════════════════════
    //  COMBO BOXES
    // ═══════════════════════════════════════════════════════════════
    private void setupComboBoxes() {
        if (filterRole != null)
            filterRole.setItems(FXCollections.observableArrayList(
                    "All Roles", "admin", "JOUEUR", "BANNI"));
        if (filterStatus != null)
            filterStatus.setItems(FXCollections.observableArrayList(
                    "All Status", "Active", "Inactive", "Banned"));
        if (filterGame != null)
            filterGame.setItems(FXCollections.observableArrayList(
                    "All Games", "Valorant", "CS2", "LoL"));

        if (filterRole != null) filterRole.getSelectionModel().selectFirst();
        if (filterStatus != null) filterStatus.getSelectionModel().selectFirst();
        if (filterGame != null) filterGame.getSelectionModel().selectFirst();
    }

    // ═══════════════════════════════════════════════════════════════
    //  USERS TABLE — FROM DATABASE
    // ═══════════════════════════════════════════════════════════════
    private void setupUsersTable() {
        if (usersTable == null) return;

        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        colUserRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                setGraphic(makeBadge(item)); setText(null);
            }
        });

        colUserActions.setCellFactory(col -> new TableCell<>() {
            private final Button banBtn    = makeSmallBtn("Ban",    "#ff4d6d");
            private final Button deleteBtn = makeSmallBtn("Delete", "#6b7394");
            private final HBox   box       = new HBox(6, banBtn, deleteBtn);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                banBtn.setOnAction(e -> {
                    UserService.UserRow u =
                            getTableView().getItems().get(getIndex());
                    adminService.banUser(u.getId());
                    loadUsersFromDB();
                    usersTable.setItems(allUsers);
                    setStatus("Banned: " + u.getFullName());
                });
                deleteBtn.setOnAction(e -> {
                    UserService.UserRow u =
                            getTableView().getItems().get(getIndex());
                    adminService.deleteUser(u.getId());
                    loadUsersFromDB();
                    usersTable.setItems(allUsers);
                    setStatus("Deleted: " + u.getFullName());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        usersTable.setItems(allUsers);
        if (userCountLabel != null)
            userCountLabel.setText(allUsers.size() + " users");
    }

    // ═══════════════════════════════════════════════════════════════
    //  NAVIGATION HANDLERS
    // ═══════════════════════════════════════════════════════════════
    @FXML private void onNavDashboard() {
        showPage("Dashboard", "Platform overview",
                pageDashboard, navDashboard, "Refresh");
    }
    @FXML private void onNavUsers() {
        showPage("Users", "Manage all registered accounts",
                pageUsers, navUsers, "+ New User");
    }
    @FXML private void onNavPlayers() {
        showPage("Players", "Player rankings",
                pagePlayers, navPlayers, "+ Invite Player");
    }
    @FXML private void onNavMatches() {
        showPage("Matches", "Live & scheduled matches",
                pageMatches, navMatches, "+ Schedule Match");
    }
    @FXML private void onNavTeams() {
        showPage("Teams", "Active teams",
                pageTeams, navTeams, "+ Create Team");
    }
    @FXML private void onNavTournaments() {
        showPage("Tournaments", "All tournaments",
                pageTournaments, navTournaments, "+ Host Tournament");
    }
    @FXML private void onNavReports() {
        showPage("Moderation", "Pending reports",
                pageModeration, navReports, "Review All");
    }
    @FXML private void onNavModeration() {
        showPage("Moderation", "Pending reports",
                pageModeration, navModeration, "Review All");
    }
    @FXML private void onNavSettings() {
        showPage("Settings", "Platform configuration",
                pageSettings, navSettings, "Save Changes");
    }

    private void showPage(String title, String subtitle,
                          ScrollPane page, HBox nav, String btnText) {
        List.of(pageDashboard, pageUsers, pagePlayers, pageMatches,
                        pageTeams, pageTournaments, pageModeration, pageSettings)
                .forEach(p -> {
                    if (p != null) {
                        p.setVisible(false);
                        p.setManaged(false);
                    }
                });

        if (page != null) {
            page.setVisible(true);
            page.setManaged(true);
        }
        if (pageTitle != null) pageTitle.setText(title);
        if (pageSubtitle != null) pageSubtitle.setText(subtitle);
        if (actionBtn != null) actionBtn.setText(btnText);

        if (currentNav != null)
            currentNav.setStyle("-fx-padding:9 10; -fx-background-radius:8;" +
                    "-fx-background-color:transparent; -fx-cursor:hand;");

        if (nav != null)
            nav.setStyle("-fx-padding:9 10; -fx-background-radius:8;" +
                    "-fx-background-color:rgba(77,120,255,0.15);" +
                    "-fx-border-color:rgba(77,120,255,0.2);" +
                    "-fx-border-radius:8; -fx-cursor:hand;");
        currentNav = nav;
        setStatus("Viewing: " + title);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HOVER HANDLERS
    // ═══════════════════════════════════════════════════════════════
    @FXML private void onNavHoverEnter(MouseEvent e) {
        HBox nav = (HBox) e.getSource();
        if (nav != currentNav)
            nav.setStyle("-fx-padding:9 10; -fx-background-radius:8;" +
                    "-fx-background-color:#161a2e; -fx-cursor:hand;");
    }

    @FXML private void onNavHoverExit(MouseEvent e) {
        HBox nav = (HBox) e.getSource();
        if (nav != currentNav)
            nav.setStyle("-fx-padding:9 10; -fx-background-radius:8;" +
                    "-fx-background-color:transparent; -fx-cursor:hand;");
    }

    // ═══════════════════════════════════════════════════════════════
    //  SEARCH & FILTER
    // ═══════════════════════════════════════════════════════════════
    @FXML private void onSearch() {
        if (searchField == null || usersTable == null) return;
        String q = searchField.getText().toLowerCase().trim();
        FilteredList<UserService.UserRow> f = new FilteredList<>(allUsers,
                u -> q.isEmpty()
                        || u.getFullName().toLowerCase().contains(q)
                        || u.getEmail().toLowerCase().contains(q));
        usersTable.setItems(f);
        if (userCountLabel != null)
            userCountLabel.setText(f.size() + " users");
    }

    @FXML private void onFilterChange() {
        if (usersTable == null) return;
        String role = filterRole != null && filterRole.getValue() != null
                ? filterRole.getValue() : "All Roles";

        FilteredList<UserService.UserRow> filtered =
                new FilteredList<>(allUsers,
                        u -> "All Roles".equals(role)
                                || role.equals(u.getRole()));
        usersTable.setItems(filtered);
        if (userCountLabel != null)
            userCountLabel.setText(filtered.size() + " users");
    }

    // ═══════════════════════════════════════════════════════════════
    //  ACTION BUTTON
    // ═══════════════════════════════════════════════════════════════
    @FXML private void onActionBtn() {
        if (actionBtn == null) return;
        if ("Refresh".equals(actionBtn.getText())) {
            loadUsersFromDB();
            populateDashboardStats();
            setStatus("Dashboard refreshed ✓");
        } else {
            showInfo("Action", actionBtn.getText() + " — coming soon.");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  QUICK ACTIONS
    // ═══════════════════════════════════════════════════════════════
    @FXML private void onCreateTournament() { showInfo("Create Tournament", "Coming soon."); }
    @FXML private void onBanUser()          { showInfo("Ban User", "Select a user from the Users table."); }
    @FXML private void onAnnouncement()     { showInfo("Announcement", "Broadcast a message."); }
    @FXML private void onExportBackup()     { showInfo("Export Backup", "Database backup initiated."); }
    @FXML private void onRefreshCache()     { loadUsersFromDB(); populateDashboardStats(); setStatus("Cache refreshed ✓"); }
    @FXML private void onScheduleMatch()    { showInfo("Schedule Match", "Coming soon."); }
    @FXML private void onCreateTeam()       { showInfo("Create Team", "Coming soon."); }
    @FXML private void onExportUsers()      { showInfo("Export", "User data export initiated."); }

    // ═══════════════════════════════════════════════════════════════
    //  LOGOUT
    // ═══════════════════════════════════════════════════════════════
    @FXML private void onLogout() {
        try {
            Session.getInstance().clear();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) pageTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  SETTINGS
    // ═══════════════════════════════════════════════════════════════
    @FXML private void onSaveSettings() {
        setStatus("Settings saved ✓");
        showInfo("Settings", "Platform settings saved successfully.");
    }

    @FXML private void onResetSettings() {
        if (settingPlatformName != null) settingPlatformName.setText("Cartix");
        if (settingSeasonName != null)   settingSeasonName.setText("Season 2025");
        if (settingMaxTeams != null)     settingMaxTeams.setText("32");
        if (settingMaintenance != null)  settingMaintenance.setSelected(false);
        if (settingRegistrations != null) settingRegistrations.setSelected(true);
        setStatus("Settings reset to defaults ✓");
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════
    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private Label makeBadge(String text) {
        Label lbl = new Label(text);
        String style = switch (text.toLowerCase()) {
            case "admin"  -> "-fx-text-fill:#ff4d6d; -fx-background-color:rgba(255,77,109,0.12);";
            case "joueur" -> "-fx-text-fill:#22d98a; -fx-background-color:rgba(34,217,138,0.12);";
            case "banni"  -> "-fx-text-fill:#9aa3c7; -fx-background-color:rgba(154,163,199,0.12);";
            default       -> "-fx-text-fill:#e8eaf6; -fx-background-color:rgba(255,255,255,0.08);";
        };
        lbl.setStyle(style + " -fx-padding:2 8; -fx-background-radius:10;" +
                "-fx-font-size:10; -fx-font-weight:bold;");
        return lbl;
    }

    private Button makeSmallBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setFocusTraversable(false);
        // Ajoute un style plus visible
        btn.setStyle(
                "-fx-background-color:" + color + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:12;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:8;" +
                        "-fx-padding:4 12;" +
                        "-fx-cursor:hand;" +
                        "-fx-border-width:0;"
        );
        // Force l'affichage du texte
        btn.setText(text);
        return btn;
    }

    private void styleTable(TableView<?> table) {
        if (table == null) return;
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(42);
    }
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}