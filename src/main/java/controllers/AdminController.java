package controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
        import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
        import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
        import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

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
    @FXML private Label    pageTitle;
    @FXML private Label    pageSubtitle;
    @FXML private TextField searchField;
    @FXML private Button   actionBtn;
    @FXML private Label    statusLabel;
    @FXML private Label    clockLabel;
    @FXML private Label    userCountLabel;

    // ─── Dashboard ───────────────────────────────────────────────
    @FXML private BarChart<String, Number> registrationsChart;
    @FXML private VBox activityFeed;
    @FXML private VBox topPlayersList;
    @FXML private VBox flaggedList;
    @FXML private VBox liveMatchesList;

    // ─── Users Table ─────────────────────────────────────────────
    @FXML private TableView<UserModel>          usersTable;
    @FXML private TableColumn<UserModel, Integer> colUserId;
    @FXML private TableColumn<UserModel, String>  colUserName;
    @FXML private TableColumn<UserModel, String>  colUserEmail;
    @FXML private TableColumn<UserModel, String>  colUserRole;
    @FXML private TableColumn<UserModel, String>  colUserGame;
    @FXML private TableColumn<UserModel, String>  colUserJoined;
    @FXML private TableColumn<UserModel, String>  colUserStatus;
    @FXML private TableColumn<UserModel, Void>    colUserActions;
    @FXML private ComboBox<String> filterRole;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterGame;

    // ─── Players Table ───────────────────────────────────────────
    @FXML private TableView<PlayerModel>           playersTable;
    @FXML private TableColumn<PlayerModel, Integer> colRank;
    @FXML private TableColumn<PlayerModel, String>  colHandle;
    @FXML private TableColumn<PlayerModel, String>  colGame;
    @FXML private TableColumn<PlayerModel, String>  colTeam;
    @FXML private TableColumn<PlayerModel, Double>  colKda;
    @FXML private TableColumn<PlayerModel, String>  colWinRate;
    @FXML private TableColumn<PlayerModel, Integer> colMatches;
    @FXML private TableColumn<PlayerModel, Double>  colRating;
    @FXML private TableColumn<PlayerModel, String>  colPlayerStatus;
    @FXML private TableColumn<PlayerModel, Void>    colPlayerActions;
    @FXML private ComboBox<String> filterPlayerGame;

    // ─── Matches Table ───────────────────────────────────────────
    @FXML private TableView<MatchModel>          matchesTable;
    @FXML private TableColumn<MatchModel, Integer> colMatchId;
    @FXML private TableColumn<MatchModel, String>  colMatchGame;
    @FXML private TableColumn<MatchModel, String>  colTeam1;
    @FXML private TableColumn<MatchModel, String>  colScore;
    @FXML private TableColumn<MatchModel, String>  colTeam2;
    @FXML private TableColumn<MatchModel, String>  colMatchDate;
    @FXML private TableColumn<MatchModel, String>  colMatchStatus;
    @FXML private TableColumn<MatchModel, Void>    colMatchActions;

    // ─── Teams Table ─────────────────────────────────────────────
    @FXML private TableView<TeamModel>           teamsTable;
    @FXML private TableColumn<TeamModel, Integer> colTeamRank;
    @FXML private TableColumn<TeamModel, String>  colTeamName;
    @FXML private TableColumn<TeamModel, String>  colTeamGame;
    @FXML private TableColumn<TeamModel, Integer> colTeamPlayers;
    @FXML private TableColumn<TeamModel, Integer> colTeamWins;
    @FXML private TableColumn<TeamModel, String>  colTeamWinRate;
    @FXML private TableColumn<TeamModel, String>  colTeamPrize;
    @FXML private TableColumn<TeamModel, String>  colTeamStatus;
    @FXML private TableColumn<TeamModel, Void>    colTeamActions;

    // ─── Tournaments Table ───────────────────────────────────────
    @FXML private TableView<TournamentModel>           tournamentsTable;
    @FXML private TableColumn<TournamentModel, Integer> colTournId;
    @FXML private TableColumn<TournamentModel, String>  colTournName;
    @FXML private TableColumn<TournamentModel, String>  colTournGame;
    @FXML private TableColumn<TournamentModel, Integer> colTournTeams;
    @FXML private TableColumn<TournamentModel, String>  colTournPrize;
    @FXML private TableColumn<TournamentModel, String>  colTournDate;
    @FXML private TableColumn<TournamentModel, String>  colTournStatus;
    @FXML private TableColumn<TournamentModel, Void>    colTournActions;

    // ─── Moderation Table ────────────────────────────────────────
    @FXML private TableView<ReportModel>          moderationTable;
    @FXML private TableColumn<ReportModel, Integer> colRepId;
    @FXML private TableColumn<ReportModel, String>  colRepType;
    @FXML private TableColumn<ReportModel, String>  colRepTarget;
    @FXML private TableColumn<ReportModel, String>  colRepReporter;
    @FXML private TableColumn<ReportModel, String>  colRepReason;
    @FXML private TableColumn<ReportModel, String>  colRepDate;
    @FXML private TableColumn<ReportModel, String>  colRepSeverity;
    @FXML private TableColumn<ReportModel, Void>    colRepActions;

    // ─── Settings ────────────────────────────────────────────────
    @FXML private TextField settingPlatformName;
    @FXML private TextField settingSeasonName;
    @FXML private TextField settingMaxTeams;
    @FXML private CheckBox  settingMaintenance;
    @FXML private CheckBox  settingRegistrations;

    // ─── Data ────────────────────────────────────────────────────
    private final ObservableList<UserModel>       allUsers       = FXCollections.observableArrayList();
    private final ObservableList<PlayerModel>     allPlayers     = FXCollections.observableArrayList();
    private final ObservableList<MatchModel>      allMatches     = FXCollections.observableArrayList();
    private final ObservableList<TeamModel>       allTeams       = FXCollections.observableArrayList();
    private final ObservableList<TournamentModel> allTournaments = FXCollections.observableArrayList();
    private final ObservableList<ReportModel>     allReports     = FXCollections.observableArrayList();

    private HBox currentNav;

    // ═══════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ═══════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentNav = navDashboard;
        seedData();
        setupComboBoxes();
        setupUsersTable();
        setupPlayersTable();
        setupMatchesTable();
        setupTeamsTable();
        setupTournamentsTable();
        setupModerationTable();
        populateDashboard();
        startClock();
    }

    // ═══════════════════════════════════════════════════════════════
    //  CLOCK
    // ═══════════════════════════════════════════════════════════════
    private void startClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm:ss");
        Timeline clock = new Timeline(
                new KeyFrame(Duration.seconds(1),
                        e -> clockLabel.setText(LocalDateTime.now().format(fmt)))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        clockLabel.setText(LocalDateTime.now().format(fmt));
    }

    // ═══════════════════════════════════════════════════════════════
    //  SEED DATA
    // ═══════════════════════════════════════════════════════════════
    private void seedData() {
        allUsers.addAll(
                new UserModel(1,  "ShadowRift",  "iheb@cartix.gg",      "Pro",   "Valorant", "Jan 12 2024", "Active"),
                new UserModel(2,  "NightTrace",  "alex@cartix.gg",      "Pro",   "CS2",      "Feb 5 2024",  "Active"),
                new UserModel(3,  "PhantomX",    "sami@cartix.gg",      "Pro",   "LoL",      "Mar 3 2024",  "Active"),
                new UserModel(4,  "VoidZero",    "lucas@cartix.gg",     "Pro",   "Valorant", "Apr 1 2024",  "Active"),
                new UserModel(5,  "CrimsonFox",  "omar@cartix.gg",      "Pro",   "CS2",      "Apr 14 2024", "Active"),
                new UserModel(6,  "BladeStorm",  "chen@cartix.gg",      "Pro",   "Valorant", "May 2 2024",  "Inactive"),
                new UserModel(7,  "ArcLight",    "yuki@cartix.gg",      "User",  "LoL",      "May 18 2024", "Active"),
                new UserModel(8,  "NeonPulse",   "sara@cartix.gg",      "User",  "CS2",      "Jun 7 2024",  "Active"),
                new UserModel(9,  "GhostFrame",  "dmitri@cartix.gg",    "User",  "Valorant", "Jun 22 2024", "Banned"),
                new UserModel(10, "FrostByte",   "mohammed@cartix.gg",  "User",  "LoL",      "Jul 3 2024",  "Active")
        );

        allPlayers.addAll(
                new PlayerModel(1,  "ShadowRift",  "Iheb Tarhouni",  "Valorant", "Team Nexus",   4.8, "72%", 312, 98.2, "Active"),
                new PlayerModel(2,  "NightTrace",  "Alex Morel",     "CS2",      "Iron Wolves",  4.5, "68%", 289, 96.5, "Active"),
                new PlayerModel(3,  "PhantomX",    "Sami Belkaid",   "LoL",      "Red Grid",     4.2, "65%", 256, 94.1, "Active"),
                new PlayerModel(4,  "VoidZero",    "Lucas Ferreira", "Valorant", "Dark Knights", 3.9, "63%", 241, 91.8, "Active"),
                new PlayerModel(5,  "CrimsonFox",  "Omar Benali",    "CS2",      "Storm Pulse",  3.7, "60%", 218, 89.3, "Active"),
                new PlayerModel(6,  "BladeStorm",  "Chen Wei",       "Valorant", "Team Nexus",   3.5, "58%", 198, 87.0, "Inactive"),
                new PlayerModel(7,  "ArcLight",    "Yuki Tanaka",    "LoL",      "Iron Wolves",  3.4, "57%", 187, 85.2, "Active"),
                new PlayerModel(8,  "NeonPulse",   "Sara Dupont",    "CS2",      "Red Grid",     3.2, "55%", 174, 82.7, "Active"),
                new PlayerModel(9,  "GhostFrame",  "Dmitri Volkov",  "Valorant", "Storm Pulse",  3.0, "53%", 161, 80.1, "Inactive"),
                new PlayerModel(10, "FrostByte",   "Mohammed A.",    "LoL",      "Dark Knights", 2.9, "51%", 148, 77.4, "Active")
        );

        allMatches.addAll(
                new MatchModel(1, "Valorant", "Team Nexus",  "11 — 9", "Iron Wolves",  "Live · Round 18",  "Live"),
                new MatchModel(2, "CS2",      "Fnatic",      "8 — 13", "NAVI",         "Live · Half Time", "Live"),
                new MatchModel(3, "Valorant", "Paper Rex",   "vs",     "ZETA Div",     "Today · 8:00 PM",  "Upcoming"),
                new MatchModel(4, "CS2",      "Team Liquid", "vs",     "G2 Esports",   "Today · 9:30 PM",  "Upcoming"),
                new MatchModel(5, "LoL",      "T1",          "vs",     "Cloud9",       "Tmrw · 6:00 PM",   "Upcoming"),
                new MatchModel(6, "Valorant", "Sentinels",   "13—11",  "100 Thieves",  "Apr 5 · Ended",    "Completed"),
                new MatchModel(7, "CS2",      "Astralis",    "16—12",  "Vitality",     "Apr 3 · Ended",    "Completed"),
                new MatchModel(8, "LoL",      "G2",          "3 — 1",  "Fnatic",       "Apr 1 · Ended",    "Completed")
        );

        allTeams.addAll(
                new TeamModel(1, "Team Nexus",   "Valorant", 8, 142, 110, "78%", "$420K", "Active"),
                new TeamModel(2, "Iron Wolves",  "CS2",      7, 130,  97, "74%", "$380K", "Active"),
                new TeamModel(3, "Red Grid",     "LoL",      6, 118,  84, "71%", "$310K", "Active"),
                new TeamModel(4, "Storm Pulse",  "CS2",      8, 105,  72, "68%", "$240K", "Active"),
                new TeamModel(5, "Dark Knights", "Valorant", 7,  98,  61, "62%", "$175K", "Recruiting"),
                new TeamModel(6, "Phantom FC",   "LoL",      5,  84,  49, "58%", "$120K", "Active"),
                new TeamModel(7, "NightSquad",   "Valorant", 6,  71,  39, "55%", "$90K",  "Inactive")
        );

        allTournaments.addAll(
                new TournamentModel(1, "VCT Champions 2025", "Valorant", 32, "$1,000,000", "Apr 1 2025",  "Live"),
                new TournamentModel(2, "IEM Cologne 2025",   "CS2",      16, "$500,000",   "Apr 12 2025", "Upcoming"),
                new TournamentModel(3, "MSI 2025",           "LoL",      24, "$800,000",   "Apr 20 2025", "Upcoming"),
                new TournamentModel(4, "ESL Pro League S20", "CS2",      16, "$750,000",   "Mar 28 2025", "Completed"),
                new TournamentModel(5, "Nexus Cup Spring",   "Valorant",  8, "$10,000",    "Apr 15 2025", "Upcoming"),
                new TournamentModel(6, "Valorant Open 2025", "Valorant", 64, "$50,000",    "May 1 2025",  "Upcoming")
        );

        allReports.addAll(
                new ReportModel(1, "Player",     "GhostFrame", "NightTrace", "Cheating / Aimbot",       "Apr 7 2025", "High"),
                new ReportModel(2, "Player",     "BladeStorm", "CrimsonFox", "Harassment in chat",      "Apr 6 2025", "Medium"),
                new ReportModel(3, "Tournament", "Nexus Cup",  "VoidZero",   "Unfair bracket seeding",  "Apr 5 2025", "Low"),
                new ReportModel(4, "Match",      "Match #4",   "NeonPulse",  "Score manipulation",      "Apr 4 2025", "High"),
                new ReportModel(5, "Player",     "FrostByte",  "ShadowRift", "Hate speech",             "Apr 3 2025", "High")
        );
    }

    // ═══════════════════════════════════════════════════════════════
    //  COMBO BOXES
    // ═══════════════════════════════════════════════════════════════
    private void setupComboBoxes() {
        filterRole.setItems(FXCollections.observableArrayList("All Roles","Admin","Pro","User"));
        filterStatus.setItems(FXCollections.observableArrayList("All Status","Active","Inactive","Banned"));
        filterGame.setItems(FXCollections.observableArrayList("All Games","Valorant","CS2","LoL"));
        filterPlayerGame.setItems(FXCollections.observableArrayList("All Games","Valorant","CS2","LoL"));
        filterRole.getSelectionModel().selectFirst();
        filterStatus.getSelectionModel().selectFirst();
        filterGame.getSelectionModel().selectFirst();
        filterPlayerGame.getSelectionModel().selectFirst();
    }

    // ═══════════════════════════════════════════════════════════════
    //  USERS TABLE
    // ═══════════════════════════════════════════════════════════════
    private void setupUsersTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserName.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colUserJoined.setCellValueFactory(new PropertyValueFactory<>("joined"));
        colUserStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colUserStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                setGraphic(makeBadge(item)); setText(null);
            }
        });

        colUserActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = makeSmallBtn("Edit", "#4d78ff");
            private final Button banBtn  = makeSmallBtn("Ban",  "#ff4d6d");
            private final HBox   box     = new HBox(6, editBtn, banBtn);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                editBtn.setOnAction(e -> onEditUser(getTableView().getItems().get(getIndex())));
                banBtn.setOnAction(e  -> onBanUserRow(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        styleTable(usersTable);
        FilteredList<UserModel> filtered = new FilteredList<>(allUsers, p -> true);
        usersTable.setItems(filtered);
        if (userCountLabel != null) {
            userCountLabel.setText(allUsers.size() + " users");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  PLAYERS TABLE
    // ═══════════════════════════════════════════════════════════════
    private void setupPlayersTable() {
        colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colHandle.setCellValueFactory(new PropertyValueFactory<>("handle"));
        colGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colTeam.setCellValueFactory(new PropertyValueFactory<>("team"));
        colKda.setCellValueFactory(new PropertyValueFactory<>("kda"));
        colWinRate.setCellValueFactory(new PropertyValueFactory<>("winRate"));
        colMatches.setCellValueFactory(new PropertyValueFactory<>("matches"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
        colPlayerStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colPlayerStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                setGraphic(makeBadge(item)); setText(null);
            }
        });

        colRating.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String color = item >= 95 ? "#22d98a" : item >= 85 ? "#4d78ff" : "#6b7394";
                Label lbl = new Label(String.valueOf(item));
                lbl.setStyle("-fx-text-fill:" + color + "; -fx-font-weight:bold;" +
                        "-fx-background-color:" + color + "22; -fx-padding:2 8; -fx-background-radius:10;");
                setGraphic(lbl); setText(null);
            }
        });

        colPlayerActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn    = makeSmallBtn("View",    "#4d78ff");
            private final Button suspendBtn = makeSmallBtn("Suspend", "#ffb347");
            private final HBox   box        = new HBox(6, viewBtn, suspendBtn);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                viewBtn.setOnAction(e    -> onViewPlayer(getTableView().getItems().get(getIndex())));
                suspendBtn.setOnAction(e -> onSuspendPlayer(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        styleTable(playersTable);
        playersTable.setItems(allPlayers);
    }

    // ═══════════════════════════════════════════════════════════════
    //  MATCHES TABLE
    // ═══════════════════════════════════════════════════════════════
    private void setupMatchesTable() {
        colMatchId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMatchGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colTeam1.setCellValueFactory(new PropertyValueFactory<>("team1"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colTeam2.setCellValueFactory(new PropertyValueFactory<>("team2"));
        colMatchDate.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        colMatchStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colMatchStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                setGraphic(makeBadge(item)); setText(null);
            }
        });

        colMatchActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn   = makeSmallBtn("View",   "#4d78ff");
            private final Button cancelBtn = makeSmallBtn("Cancel", "#ff4d6d");
            private final HBox   box       = new HBox(6, viewBtn, cancelBtn);
            { box.setAlignment(Pos.CENTER_LEFT); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        styleTable(matchesTable);
        matchesTable.setItems(allMatches);
    }

    // ═══════════════════════════════════════════════════════════════
    //  TEAMS TABLE
    // ═══════════════════════════════════════════════════════════════
    private void setupTeamsTable() {
        colTeamRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colTeamName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTeamGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colTeamPlayers.setCellValueFactory(new PropertyValueFactory<>("players"));
        colTeamWins.setCellValueFactory(new PropertyValueFactory<>("wins"));
        colTeamWinRate.setCellValueFactory(new PropertyValueFactory<>("winRate"));
        colTeamPrize.setCellValueFactory(new PropertyValueFactory<>("prize"));
        colTeamStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colTeamStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                setGraphic(makeBadge(item)); setText(null);
            }
        });

        colTeamActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn     = makeSmallBtn("View",     "#4d78ff");
            private final Button dissolveBtn = makeSmallBtn("Dissolve", "#ff4d6d");
            private final HBox   box         = new HBox(6, viewBtn, dissolveBtn);
            { box.setAlignment(Pos.CENTER_LEFT); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        styleTable(teamsTable);
        teamsTable.setItems(allTeams);
    }

    // ═══════════════════════════════════════════════════════════════
    //  TOURNAMENTS TABLE
    // ═══════════════════════════════════════════════════════════════
    private void setupTournamentsTable() {
        colTournId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTournName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTournGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colTournTeams.setCellValueFactory(new PropertyValueFactory<>("teams"));
        colTournPrize.setCellValueFactory(new PropertyValueFactory<>("prize"));
        colTournDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colTournStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colTournStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                setGraphic(makeBadge(item)); setText(null);
            }
        });

        colTournActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn   = makeSmallBtn("View",   "#4d78ff");
            private final Button editBtn   = makeSmallBtn("Edit",   "#22d98a");
            private final Button cancelBtn = makeSmallBtn("Cancel", "#ff4d6d");
            private final HBox   box       = new HBox(4, viewBtn, editBtn, cancelBtn);
            { box.setAlignment(Pos.CENTER_LEFT); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        styleTable(tournamentsTable);
        tournamentsTable.setItems(allTournaments);
    }

    // ═══════════════════════════════════════════════════════════════
    //  MODERATION TABLE
    // ═══════════════════════════════════════════════════════════════
    private void setupModerationTable() {
        colRepId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRepType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colRepTarget.setCellValueFactory(new PropertyValueFactory<>("target"));
        colRepReporter.setCellValueFactory(new PropertyValueFactory<>("reporter"));
        colRepReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colRepDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colRepSeverity.setCellValueFactory(new PropertyValueFactory<>("severity"));

        colRepSeverity.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String color = "High".equals(item) ? "#ff4d6d" : "Medium".equals(item) ? "#ffb347" : "#22d98a";
                String bg    = "High".equals(item) ? "rgba(255,77,109,0.12)"
                        : "Medium".equals(item) ? "rgba(255,179,71,0.12)"
                        : "rgba(34,217,138,0.1)";
                Label lbl = new Label(item);
                lbl.setStyle("-fx-text-fill:" + color + "; -fx-background-color:" + bg +
                        "; -fx-padding:2 8; -fx-background-radius:10; -fx-font-weight:bold; -fx-font-size:10;");
                setGraphic(lbl); setText(null);
            }
        });

        colRepActions.setCellFactory(col -> new TableCell<>() {
            private final Button resolveBtn = makeSmallBtn("Resolve", "#22d98a");
            private final Button banBtn     = makeSmallBtn("Ban",     "#ff4d6d");
            private final Button dismissBtn = makeSmallBtn("Dismiss", "#6b7394");
            private final HBox   box        = new HBox(4, resolveBtn, banBtn, dismissBtn);
            { box.setAlignment(Pos.CENTER_LEFT); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        styleTable(moderationTable);
        moderationTable.setItems(allReports);
    }

    // ═══════════════════════════════════════════════════════════════
    //  DASHBOARD POPULATION
    // ═══════════════════════════════════════════════════════════════
    private void populateDashboard() {
        // ── Bar Chart ──
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Registrations");
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        int[]    vals = {420, 380, 510, 470, 620, 780, 540};
        for (int i = 0; i < days.length; i++)
            series.getData().add(new XYChart.Data<>(days[i], vals[i]));
        registrationsChart.getData().add(series);
        registrationsChart.setLegendVisible(false);
        registrationsChart.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-plot-background-color:transparent;" +
                        "-fx-horizontal-grid-lines-visible:false;"
        );

        // ── Activity Feed ──
        List<String[]> activities = List.of(
                new String[]{"#22d98a", "New user registered: ShadowRift",      "2m ago"},
                new String[]{"#4d78ff", "Tournament created: Nexus Cup Spring",  "8m ago"},
                new String[]{"#ff3b5c", "Report filed: GhostFrame — Cheating",   "15m ago"},
                new String[]{"#ffb347", "Match scheduled: Paper Rex vs ZETA",    "32m ago"},
                new String[]{"#22d98a", "Team verified: Storm Pulse",            "1h ago"},
                new String[]{"#ff4d6d", "User banned: GhostFrame",               "2h ago"}
        );
        for (String[] a : activities) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(9, 0, 9, 0));
            row.setStyle("-fx-border-color:rgba(255,255,255,0.04); -fx-border-width:0 0 1 0;");
            Rectangle dot = new Rectangle(6, 6);
            dot.setArcWidth(6); dot.setArcHeight(6);
            dot.setFill(Color.web(a[0]));
            Label msg  = new Label(a[1]);
            msg.setStyle("-fx-text-fill:#e8eaf6; -fx-font-size:12;");
            HBox.setHgrow(msg, Priority.ALWAYS);
            Label time = new Label(a[2]);
            time.setStyle("-fx-text-fill:#6b7394; -fx-font-size:11;");
            row.getChildren().addAll(dot, msg, time);
            activityFeed.getChildren().add(row);
        }

        // ── Top Players ──
        for (int i = 0; i < 5; i++) {
            PlayerModel p = allPlayers.get(i);
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 0, 8, 0));
            row.setStyle("-fx-border-color:rgba(255,255,255,0.04); -fx-border-width:0 0 1 0;");
            Label rank   = new Label(String.valueOf(p.getRank()));
            rank.setStyle("-fx-text-fill:#4a5070; -fx-font-weight:bold; -fx-font-size:14; -fx-min-width:20;");
            Label name   = new Label(p.getHandle());
            name.setStyle("-fx-text-fill:#e8eaf6; -fx-font-weight:bold; -fx-font-size:12;");
            HBox.setHgrow(name, Priority.ALWAYS);
            Label game   = new Label(p.getGame());
            game.setStyle("-fx-text-fill:#6b7394; -fx-font-size:11; -fx-background-color:#1c2038; -fx-padding:2 8; -fx-background-radius:10;");
            String ratingColor = p.getRating() >= 95 ? "#22d98a" : "#4d78ff";
            Label ratingLbl = new Label(String.valueOf(p.getRating()));
            ratingLbl.setStyle("-fx-text-fill:" + ratingColor + "; -fx-font-weight:bold; -fx-font-size:12;");
            row.getChildren().addAll(rank, name, game, ratingLbl);
            topPlayersList.getChildren().add(row);
        }

        // ── Flagged Reports ──
        for (ReportModel r : allReports) {
            VBox card = new VBox(6);
            card.setPadding(new Insets(10));
            card.setStyle("-fx-background-color:#161a2e; -fx-background-radius:8;" +
                    "-fx-border-color:rgba(255,77,109,0.15); -fx-border-radius:8;");
            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);
            String sevColor = "High".equals(r.getSeverity()) ? "#ff4d6d"
                    : "Medium".equals(r.getSeverity()) ? "#ffb347" : "#22d98a";
            Label sev  = new Label("● " + r.getSeverity());
            sev.setStyle("-fx-text-fill:" + sevColor + "; -fx-font-size:10; -fx-font-weight:bold;");
            Label type = new Label(r.getType());
            type.setStyle("-fx-text-fill:#6b7394; -fx-font-size:10;");
            Region sp  = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Label date = new Label(r.getDate());
            date.setStyle("-fx-text-fill:#4a5070; -fx-font-size:10;");
            header.getChildren().addAll(sev, type, sp, date);
            Label reason = new Label(r.getReason() + " → " + r.getTarget());
            reason.setStyle("-fx-text-fill:#e8eaf6; -fx-font-size:11;");
            HBox btns    = new HBox(6);
            btns.getChildren().addAll(makeSmallBtn("Resolve","#22d98a"), makeSmallBtn("Ban","#ff4d6d"));
            card.getChildren().addAll(header, reason, btns);
            flaggedList.getChildren().add(card);
        }

        // ── Live Matches ──
        for (MatchModel m : allMatches) {
            if (!"Live".equals(m.getStatus())) continue;
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12, 16, 12, 16));
            row.setStyle("-fx-background-color:#161a2e; -fx-background-radius:10;" +
                    "-fx-border-color:rgba(255,59,92,0.15); -fx-border-radius:10;");
            Label game  = new Label(m.getGame());
            game.setStyle("-fx-text-fill:#e8eaf6; -fx-background-color:#1c2038; -fx-padding:3 8; -fx-background-radius:8; -fx-font-size:11;");
            Label t1    = new Label(m.getTeam1());
            t1.setStyle("-fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:14;");
            Label score = new Label(m.getScore());
            score.setStyle("-fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:18;" +
                    "-fx-background-color:#1c2038; -fx-padding:4 16; -fx-background-radius:8;");
            Label t2    = new Label(m.getTeam2());
            t2.setStyle("-fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:14;");
            Region sp   = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            row.getChildren().addAll(game, t1, score, t2, sp, makeSmallBtn("Watch Live","#4d78ff"));
            liveMatchesList.getChildren().add(row);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  NAVIGATION HANDLERS
    // ═══════════════════════════════════════════════════════════════
    @FXML private void onNavDashboard()   { showPage("Dashboard",   "Platform overview · Season 2025",    pageDashboard,   navDashboard,   "+ New Entry"); }
    @FXML private void onNavUsers()       { showPage("Users",       "Manage all registered accounts",     pageUsers,       navUsers,       "+ New User"); }
    @FXML private void onNavPlayers()     { showPage("Players",     "Player rankings · Season 2025",      pagePlayers,     navPlayers,     "+ Invite Player"); }
    @FXML private void onNavMatches()     { showPage("Matches",     "Live & scheduled · Season 2025",     pageMatches,     navMatches,     "+ Schedule Match"); }
    @FXML private void onNavTeams()       { showPage("Teams",       "486 active teams · Season 2025",     pageTeams,       navTeams,       "+ Create Team"); }
    @FXML private void onNavTournaments() { showPage("Tournaments", "4,800 tournaments hosted",           pageTournaments, navTournaments, "+ Host Tournament"); }
    @FXML private void onNavReports()     { showPage("Moderation",  "Pending reports & flagged content",  pageModeration,  navReports,     "Review All"); }
    @FXML private void onNavModeration()  { showPage("Moderation",  "Pending reports & flagged content",  pageModeration,  navModeration,  "Review All"); }
    @FXML private void onNavSettings()    { showPage("Settings",    "Platform configuration",             pageSettings,    navSettings,    "Save Changes"); }

    private void showPage(String title, String subtitle, ScrollPane page, HBox nav, String btnText) {
        List.of(pageDashboard, pageUsers, pagePlayers, pageMatches,
                        pageTeams, pageTournaments, pageModeration, pageSettings)
                .forEach(p -> { p.setVisible(false); p.setManaged(false); });

        page.setVisible(true);
        page.setManaged(true);
        pageTitle.setText(title);
        pageSubtitle.setText(subtitle);
        actionBtn.setText(btnText);

        if (currentNav != null)
            currentNav.setStyle("-fx-padding:9 10; -fx-background-radius:8; -fx-background-color:transparent; -fx-cursor:hand;");

        nav.setStyle("-fx-padding:9 10; -fx-background-radius:8; " +
                "-fx-background-color:rgba(77,120,255,0.15); " +
                "-fx-border-color:rgba(77,120,255,0.2); -fx-border-radius:8; -fx-cursor:hand;");
        currentNav = nav;
        setStatus("Viewing: " + title);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HOVER HANDLERS
    // ═══════════════════════════════════════════════════════════════
    @FXML private void onNavHoverEnter(MouseEvent e) {
        HBox nav = (HBox) e.getSource();
        if (nav != currentNav)
            nav.setStyle("-fx-padding:9 10; -fx-background-radius:8; -fx-background-color:#161a2e; -fx-cursor:hand;");
    }
    @FXML private void onNavHoverExit(MouseEvent e) {
        HBox nav = (HBox) e.getSource();
        if (nav != currentNav)
            nav.setStyle("-fx-padding:9 10; -fx-background-radius:8; -fx-background-color:transparent; -fx-cursor:hand;");
    }

    // ═══════════════════════════════════════════════════════════════
    //  SEARCH & FILTER
    // ═══════════════════════════════════════════════════════════════
    @FXML private void onSearch() {
        String q = searchField.getText().toLowerCase().trim();
        FilteredList<UserModel> f = new FilteredList<>(allUsers,
                u -> q.isEmpty() || u.getUsername().toLowerCase().contains(q)
                        || u.getEmail().toLowerCase().contains(q));
        usersTable.setItems(f);
        if (userCountLabel != null) userCountLabel.setText(f.size() + " users");
    }

    @FXML private void onFilterChange() {
        String role = filterRole != null && filterRole.getValue() != null
                ? filterRole.getValue()
                : "All Roles";
        String status = filterStatus != null && filterStatus.getValue() != null
                ? filterStatus.getValue()
                : "All Status";
        String game = filterGame != null && filterGame.getValue() != null
                ? filterGame.getValue()
                : "All Games";
        String playerGame = filterPlayerGame != null && filterPlayerGame.getValue() != null
                ? filterPlayerGame.getValue()
                : "All Games";

        FilteredList<UserModel> filteredUsers = new FilteredList<>(allUsers,
                u -> ("All Roles".equals(role) || role.equals(u.getRole()))
                        && ("All Status".equals(status) || status.equals(u.getStatus()))
                        && ("All Games".equals(game) || game.equals(u.getGame())));
        usersTable.setItems(filteredUsers);
        if (userCountLabel != null) {
            userCountLabel.setText(filteredUsers.size() + " users");
        }

        FilteredList<PlayerModel> filteredPlayers = new FilteredList<>(allPlayers,
                p -> "All Games".equals(playerGame) || playerGame.equals(p.getGame()));
        playersTable.setItems(filteredPlayers);
    }

    // ═══════════════════════════════════════════════════════════════
    //  ACTION BUTTON
    // ═══════════════════════════════════════════════════════════════
    @FXML private void onActionBtn() {
        setStatus("Action: " + actionBtn.getText());
        showInfo("Action", actionBtn.getText() + " — coming soon.");
    }

    // ═══════════════════════════════════════════════════════════════
    //  QUICK ACTIONS
    // ═══════════════════════════════════════════════════════════════
    @FXML private void onCreateTournament() { setStatus("Create Tournament wizard opened"); showInfo("Create Tournament","Tournament wizard — coming soon."); }
    @FXML private void onBanUser()          { setStatus("Ban User dialog opened");          showInfo("Ban User","Select a user to ban from the Users table."); }
    @FXML private void onAnnouncement()     { setStatus("Announcement composer opened");    showInfo("Announcement","Broadcast a platform-wide message."); }
    @FXML private void onExportBackup()     { setStatus("Exporting database backup…");      showInfo("Export Backup","Database backup initiated."); }
    @FXML private void onRefreshCache()     { setStatus("Cache refreshed ✓");               showInfo("Cache","Platform cache has been cleared."); }
    @FXML private void onScheduleMatch()    { setStatus("Schedule Match form opened");      showInfo("Schedule Match","Match scheduling form — coming soon."); }
    @FXML private void onCreateTeam()       { setStatus("Create Team form opened");         showInfo("Create Team","Team creation form — coming soon."); }
    @FXML private void onExportUsers()      { setStatus("Exporting user CSV…");             showInfo("Export","User data export initiated."); }

    // ═══════════════════════════════════════════════════════════════
    //  ROW-LEVEL ACTIONS
    // ═══════════════════════════════════════════════════════════════
    private void onEditUser(UserModel u)      { showInfo("Edit User","Editing: " + u.getUsername()); }
    private void onBanUserRow(UserModel u)    { u.setStatus("Banned"); usersTable.refresh(); setStatus("Banned: " + u.getUsername()); }
    private void onViewPlayer(PlayerModel p)  { showInfo("Player Profile","Viewing: " + p.getHandle() + " · Rating: " + p.getRating()); }
    private void onSuspendPlayer(PlayerModel p) { p.setStatus("Inactive"); playersTable.refresh(); setStatus("Suspended: " + p.getHandle()); }

    // ═══════════════════════════════════════════════════════════════
    //  SETTINGS
    // ═══════════════════════════════════════════════════════════════
    @FXML private void onSaveSettings()  { setStatus("Settings saved ✓"); showInfo("Settings","Platform settings saved successfully."); }
    @FXML private void onResetSettings() {
        settingPlatformName.setText("Cartix");
        settingSeasonName.setText("Season 2025");
        settingMaxTeams.setText("32");
        settingMaintenance.setSelected(false);
        settingRegistrations.setSelected(true);
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
        alert.getDialogPane().setStyle(
                "-fx-background-color:#111524; -fx-border-color:rgba(77,120,255,0.3); -fx-border-radius:12;"
        );
        alert.showAndWait();
    }

    private Label makeBadge(String text) {
        Label lbl = new Label(text);
        String style = switch (text) {
            case "Active" ->
                    "-fx-text-fill:#22d98a; -fx-background-color:rgba(34,217,138,0.12);";
            case "Inactive" ->
                    "-fx-text-fill:#9aa3c7; -fx-background-color:rgba(154,163,199,0.12);";
            case "Banned" ->
                    "-fx-text-fill:#ff4d6d; -fx-background-color:rgba(255,77,109,0.12);";
            case "Recruiting" ->
                    "-fx-text-fill:#ffb347; -fx-background-color:rgba(255,179,71,0.12);";
            case "Live" ->
                    "-fx-text-fill:#ff4d6d; -fx-background-color:rgba(255,77,109,0.12);";
            case "Upcoming" ->
                    "-fx-text-fill:#4d78ff; -fx-background-color:rgba(77,120,255,0.12);";
            case "Completed" ->
                    "-fx-text-fill:#9aa3c7; -fx-background-color:rgba(154,163,199,0.12);";
            case "High" ->
                    "-fx-text-fill:#ff4d6d; -fx-background-color:rgba(255,77,109,0.12);";
            case "Medium" ->
                    "-fx-text-fill:#ffb347; -fx-background-color:rgba(255,179,71,0.12);";
            case "Low" ->
                    "-fx-text-fill:#22d98a; -fx-background-color:rgba(34,217,138,0.12);";
            default ->
                    "-fx-text-fill:#e8eaf6; -fx-background-color:rgba(255,255,255,0.08);";
        };
        lbl.setStyle(style + " -fx-padding:2 8; -fx-background-radius:10; -fx-font-size:10; -fx-font-weight:bold;");
        return lbl;
    }

    private Button makeSmallBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setFocusTraversable(false);
        btn.setStyle(
                "-fx-background-color:" + color + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:11;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:8;" +
                        "-fx-padding:4 10;" +
                        "-fx-cursor:hand;" +
                        "-fx-border-width:0;"
        );
        return btn;
    }

    private void styleTable(TableView<?> table) {
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(42);
    }

    public static final class UserModel {
        public final int id;
        public final String username;
        public final String email;
        public final String role;
        public final String game;
        public final String joined;
        public String status;

        private UserModel(int id, String username, String email, String role, String game, String joined, String status) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.role = role;
            this.game = game;
            this.joined = joined;
            this.status = status;
        }

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getGame() { return game; }
        public String getJoined() { return joined; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static final class PlayerModel {
        public final int rank;
        public final String handle;
        public final String fullName;
        public final String game;
        public final String team;
        public final double kda;
        public final String winRate;
        public final int matches;
        public final double rating;
        public String status;

        private PlayerModel(int rank, String handle, String fullName, String game, String team,
                            double kda, String winRate, int matches, double rating, String status) {
            this.rank = rank;
            this.handle = handle;
            this.fullName = fullName;
            this.game = game;
            this.team = team;
            this.kda = kda;
            this.winRate = winRate;
            this.matches = matches;
            this.rating = rating;
            this.status = status;
        }

        public int getRank() { return rank; }
        public String getHandle() { return handle; }
        public String getFullName() { return fullName; }
        public String getGame() { return game; }
        public String getTeam() { return team; }
        public double getKda() { return kda; }
        public String getWinRate() { return winRate; }
        public int getMatches() { return matches; }
        public double getRating() { return rating; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static final class MatchModel {
        public final int id;
        public final String game;
        public final String team1;
        public final String score;
        public final String team2;
        public final String dateTime;
        public final String status;

        private MatchModel(int id, String game, String team1, String score, String team2, String dateTime, String status) {
            this.id = id;
            this.game = game;
            this.team1 = team1;
            this.score = score;
            this.team2 = team2;
            this.dateTime = dateTime;
            this.status = status;
        }

        public int getId() { return id; }
        public String getGame() { return game; }
        public String getTeam1() { return team1; }
        public String getScore() { return score; }
        public String getTeam2() { return team2; }
        public String getDateTime() { return dateTime; }
        public String getStatus() { return status; }
    }

    public static final class TeamModel {
        public final int rank;
        public final String name;
        public final String game;
        public final int players;
        public final int matches;
        public final int wins;
        public final String winRate;
        public final String prize;
        public final String status;

        private TeamModel(int rank, String name, String game, int players, int matches, int wins,
                          String winRate, String prize, String status) {
            this.rank = rank;
            this.name = name;
            this.game = game;
            this.players = players;
            this.matches = matches;
            this.wins = wins;
            this.winRate = winRate;
            this.prize = prize;
            this.status = status;
        }

        public int getRank() { return rank; }
        public String getName() { return name; }
        public String getGame() { return game; }
        public int getPlayers() { return players; }
        public int getMatches() { return matches; }
        public int getWins() { return wins; }
        public String getWinRate() { return winRate; }
        public String getPrize() { return prize; }
        public String getStatus() { return status; }
    }

    public static final class TournamentModel {
        public final int id;
        public final String name;
        public final String game;
        public final int teams;
        public final String prize;
        public final String startDate;
        public final String status;

        private TournamentModel(int id, String name, String game, int teams, String prize, String startDate, String status) {
            this.id = id;
            this.name = name;
            this.game = game;
            this.teams = teams;
            this.prize = prize;
            this.startDate = startDate;
            this.status = status;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getGame() { return game; }
        public int getTeams() { return teams; }
        public String getPrize() { return prize; }
        public String getStartDate() { return startDate; }
        public String getStatus() { return status; }
    }

    public static final class ReportModel {
        public final int id;
        public final String type;
        public final String target;
        public final String reporter;
        public final String reason;
        public final String date;
        public final String severity;

        private ReportModel(int id, String type, String target, String reporter, String reason, String date, String severity) {
            this.id = id;
            this.type = type;
            this.target = target;
            this.reporter = reporter;
            this.reason = reason;
            this.date = date;
            this.severity = severity;
        }

        public int getId() { return id; }
        public String getType() { return type; }
        public String getTarget() { return target; }
        public String getReporter() { return reporter; }
        public String getReason() { return reason; }
        public String getDate() { return date; }
        public String getSeverity() { return severity; }
    }
}
