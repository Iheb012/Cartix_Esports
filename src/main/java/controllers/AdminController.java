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
import models.Equipe;
import models.Match;
import services.EquipeService;
import services.MatchService;
import services.ResultatMatchService;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminController implements Initializable {

    // ─── Pages ────────────────────────────────────────────────────
    @FXML private ScrollPane pageDashboard;
    @FXML private ScrollPane pageUsers;
    @FXML private ScrollPane pagePlayers;
    @FXML private ScrollPane pageMatches;
    @FXML private ScrollPane pageTeams;
    @FXML private ScrollPane pageTournaments;
    @FXML private ScrollPane pageModeration;
    @FXML private ScrollPane pageSettings;

    // ─── Nav ──────────────────────────────────────────────────────
    @FXML private HBox navDashboard;
    @FXML private HBox navUsers;
    @FXML private HBox navPlayers;
    @FXML private HBox navMatches;
    @FXML private HBox navTeams;
    @FXML private HBox navTournaments;
    @FXML private HBox navReports;
    @FXML private HBox navModeration;
    @FXML private HBox navSettings;

    // ─── Topbar ───────────────────────────────────────────────────
    @FXML private Label     pageTitle;
    @FXML private Label     pageSubtitle;
    @FXML private TextField searchField;
    @FXML private Button    actionBtn;
    @FXML private Label     statusLabel;
    @FXML private Label     clockLabel;
    @FXML private Label     userCountLabel;

    // ─── Dashboard ────────────────────────────────────────────────
    @FXML private BarChart<String, Number> registrationsChart;
    @FXML private VBox activityFeed;
    @FXML private VBox topPlayersList;
    @FXML private VBox flaggedList;
    @FXML private VBox liveMatchesList;

    // ─── Users Table ──────────────────────────────────────────────
    @FXML private TableView<UserModel>           usersTable;
    @FXML private TableColumn<UserModel,Integer> colUserId;
    @FXML private TableColumn<UserModel,String>  colUserName;
    @FXML private TableColumn<UserModel,String>  colUserEmail;
    @FXML private TableColumn<UserModel,String>  colUserRole;
    @FXML private TableColumn<UserModel,String>  colUserGame;
    @FXML private TableColumn<UserModel,String>  colUserJoined;
    @FXML private TableColumn<UserModel,String>  colUserStatus;
    @FXML private TableColumn<UserModel,Void>    colUserActions;
    @FXML private ComboBox<String> filterRole;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterGame;

    // ─── Players Table ────────────────────────────────────────────
    @FXML private TableView<PlayerModel>            playersTable;
    @FXML private TableColumn<PlayerModel,Integer>  colRank;
    @FXML private TableColumn<PlayerModel,String>   colHandle;
    @FXML private TableColumn<PlayerModel,String>   colGame;
    @FXML private TableColumn<PlayerModel,String>   colTeam;
    @FXML private TableColumn<PlayerModel,Double>   colKda;
    @FXML private TableColumn<PlayerModel,String>   colWinRate;
    @FXML private TableColumn<PlayerModel,Integer>  colMatches;
    @FXML private TableColumn<PlayerModel,Double>   colRating;
    @FXML private TableColumn<PlayerModel,String>   colPlayerStatus;
    @FXML private TableColumn<PlayerModel,Void>     colPlayerActions;
    @FXML private ComboBox<String> filterPlayerGame;

    // ─── Matches Page (real DB + reports) ─────────────────────────
    @FXML private TableView<MatchAdminModel>           matchesTable;
    @FXML private TableColumn<MatchAdminModel,Integer> colMatchId;
    @FXML private TableColumn<MatchAdminModel,String>  colMatchGame;
    @FXML private TableColumn<MatchAdminModel,String>  colTeam1;
    @FXML private TableColumn<MatchAdminModel,String>  colScore;
    @FXML private TableColumn<MatchAdminModel,String>  colTeam2;
    @FXML private TableColumn<MatchAdminModel,String>  colMatchDate;
    @FXML private TableColumn<MatchAdminModel,String>  colMatchStatus;
    @FXML private TableColumn<MatchAdminModel,Void>    colMatchActions;

    // Match page extra widgets (injected dynamically)
    private VBox  matchReportPanel;
    private Label matchStatTotal;
    private Label matchStatLive;
    private Label matchStatUpcoming;
    private Label matchStatDone;
    private Label matchStatCancelled;
    private ComboBox<String> matchStatusFilter;
    private ComboBox<String> matchGameFilter;
    private TextField        matchSearch;

    // ─── Teams Page (real DB + reports) ───────────────────────────
    @FXML private TableView<TeamAdminModel>           teamsTable;
    @FXML private TableColumn<TeamAdminModel,Integer> colTeamRank;
    @FXML private TableColumn<TeamAdminModel,String>  colTeamName;
    @FXML private TableColumn<TeamAdminModel,String>  colTeamGame;
    @FXML private TableColumn<TeamAdminModel,Integer> colTeamPlayers;
    @FXML private TableColumn<TeamAdminModel,Integer> colTeamWins;
    @FXML private TableColumn<TeamAdminModel,String>  colTeamWinRate;
    @FXML private TableColumn<TeamAdminModel,String>  colTeamPrize;
    @FXML private TableColumn<TeamAdminModel,String>  colTeamStatus;
    @FXML private TableColumn<TeamAdminModel,Void>    colTeamActions;

    // Team page extra widgets
    private VBox  teamReportPanel;
    private Label teamStatTotal;
    private Label teamStatActive;
    private Label teamStatRecruiting;
    private Label teamStatInactive;
    private TextField        teamSearch;
    private ComboBox<String> teamGameFilter;

    // ─── Tournaments Table ────────────────────────────────────────
    @FXML private TableView<TournamentModel>            tournamentsTable;
    @FXML private TableColumn<TournamentModel,Integer>  colTournId;
    @FXML private TableColumn<TournamentModel,String>   colTournName;
    @FXML private TableColumn<TournamentModel,String>   colTournGame;
    @FXML private TableColumn<TournamentModel,Integer>  colTournTeams;
    @FXML private TableColumn<TournamentModel,String>   colTournPrize;
    @FXML private TableColumn<TournamentModel,String>   colTournDate;
    @FXML private TableColumn<TournamentModel,String>   colTournStatus;
    @FXML private TableColumn<TournamentModel,Void>     colTournActions;

    // ─── Moderation Table ─────────────────────────────────────────
    @FXML private TableView<ReportModel>           moderationTable;
    @FXML private TableColumn<ReportModel,Integer> colRepId;
    @FXML private TableColumn<ReportModel,String>  colRepType;
    @FXML private TableColumn<ReportModel,String>  colRepTarget;
    @FXML private TableColumn<ReportModel,String>  colRepReporter;
    @FXML private TableColumn<ReportModel,String>  colRepReason;
    @FXML private TableColumn<ReportModel,String>  colRepDate;
    @FXML private TableColumn<ReportModel,String>  colRepSeverity;
    @FXML private TableColumn<ReportModel,Void>    colRepActions;

    // ─── Settings ─────────────────────────────────────────────────
    @FXML private TextField settingPlatformName;
    @FXML private TextField settingSeasonName;
    @FXML private TextField settingMaxTeams;
    @FXML private CheckBox  settingMaintenance;
    @FXML private CheckBox  settingRegistrations;

    // ─── Observable data ──────────────────────────────────────────
    private final ObservableList<UserModel>       allUsers       = FXCollections.observableArrayList();
    private final ObservableList<PlayerModel>     allPlayers     = FXCollections.observableArrayList();
    private final ObservableList<MatchAdminModel> allMatchModels = FXCollections.observableArrayList();
    private final ObservableList<TeamAdminModel>  allTeamModels  = FXCollections.observableArrayList();
    private final ObservableList<TournamentModel> allTournaments = FXCollections.observableArrayList();
    private final ObservableList<ReportModel>     allReports     = FXCollections.observableArrayList();

    // ─── Real DB services ─────────────────────────────────────────
    private final MatchService         matchService    = new MatchService();
    private final EquipeService        equipeService   = new EquipeService();
    private final ResultatMatchService resultatService = new ResultatMatchService();

    private HBox currentNav;

    // ═══════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ═══════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentNav = navDashboard;
        seedStaticData();
        loadRealMatchData();
        loadRealTeamData();
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
    //  LOAD REAL MATCH DATA FROM DB
    // ═══════════════════════════════════════════════════════════════
    private void loadRealMatchData() {
        allMatchModels.clear();
        try {
            List<Match>  matches = matchService.getAll();
            List<Equipe> equipes = equipeService.getAll();

            // Build ID→name map
            java.util.Map<Integer,String> teamNames = new java.util.HashMap<>();
            for (Equipe e : equipes) teamNames.put(e.getId(), e.getNom());

            int rank = 1;
            for (Match m : matches) {
                String t1    = teamNames.getOrDefault(m.getEquipe1Id(), "Team " + m.getEquipe1Id());
                String t2    = teamNames.getOrDefault(m.getEquipe2Id(), "Team " + m.getEquipe2Id());
                String score = "—";
                try {
                    var res = resultatService.getByMatchId(m.getId());
                    if (res != null) score = res.getScoreEquipe1() + " — " + res.getScoreEquipe2();
                } catch (Exception ignored) {}

                String statut = m.getStatut() != null ? m.getStatut() : "planifie";
                String adminStatus = switch (statut.toLowerCase()) {
                    case "en_cours"  -> "Live";
                    case "planifie"  -> "Upcoming";
                    case "termine"   -> "Completed";
                    case "annule"    -> "Cancelled";
                    default          -> statut;
                };
                String date = m.getDateMatch() != null ? m.getDateMatch().toString() : "—";
                String game = m.getJeu() != null ? m.getJeu() : "—";

                allMatchModels.add(new MatchAdminModel(
                        m.getId(), game, t1, score, t2, date, adminStatus));
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not load matches: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  LOAD REAL TEAM DATA FROM DB
    // ═══════════════════════════════════════════════════════════════
    private void loadRealTeamData() {
        allTeamModels.clear();
        try {
            List<Equipe> equipes = equipeService.getAll();
            List<Match>  matches = matchService.getAll();

            int rank = 1;
            for (Equipe e : equipes) {
                // Count matches and wins
                long played = matches.stream()
                        .filter(m -> m.getEquipe1Id() == e.getId() || m.getEquipe2Id() == e.getId())
                        .count();
                long wins = 0;
                for (Match m : matches) {
                    if (m.getEquipe1Id() != e.getId() && m.getEquipe2Id() != e.getId()) continue;
                    try {
                        var res = resultatService.getByMatchId(m.getId());
                        if (res != null && res.getGagnantId() == e.getId()) wins++;
                    } catch (Exception ignored) {}
                }
                String winRate = played > 0 ? String.format("%.0f%%", wins * 100.0 / played) : "0%";
                String game    = e.getJeu()   != null ? e.getJeu()   : "—";
                String status  = "Active";

                allTeamModels.add(new TeamAdminModel(
                        rank++, e.getId(), e.getNom(), game,
                        e.getNbMembres(), (int) played, (int) wins,
                        winRate, "—", status));
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not load teams: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  MATCHES TABLE — with moderation + filter + stats + report panel
    // ═══════════════════════════════════════════════════════════════
    private void setupMatchesTable() {
        // ── Inject stats + filters above the table ────────────────
        if (pageMatches != null && pageMatches.getContent() instanceof VBox vbox) {
            injectMatchHeader(vbox);
        }

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

        // Score column with color
        colScore.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;" +
                        "-fx-background-color: #1c2038; -fx-padding: 2 10; -fx-background-radius: 7;");
                setGraphic(lbl); setText(null);
            }
        });

        colMatchActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn    = makeSmallBtn("View",    "#4d78ff");
            private final Button reportBtn  = makeSmallBtn("Report",  "#ffb347");
            private final Button cancelBtn  = makeSmallBtn("Cancel",  "#ff4d6d");
            private final HBox   box        = new HBox(5, viewBtn, reportBtn, cancelBtn);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                viewBtn.setOnAction(e -> onViewMatch(getTableView().getItems().get(getIndex())));
                reportBtn.setOnAction(e -> onReportMatch(getTableView().getItems().get(getIndex())));
                cancelBtn.setOnAction(e -> onCancelMatch(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        styleTable(matchesTable);
        matchesTable.setItems(allMatchModels);
    }

    private void injectMatchHeader(VBox container) {
        // ── Stats row ─────────────────────────────────────────────
        HBox statsRow = new HBox(12);
        statsRow.setPadding(new Insets(0, 0, 14, 0));

        matchStatTotal     = new Label("0");
        matchStatLive      = new Label("0");
        matchStatUpcoming  = new Label("0");
        matchStatDone      = new Label("0");
        matchStatCancelled = new Label("0");

        statsRow.getChildren().addAll(
                makeAdminStatCard("Total",     matchStatTotal,     "#4d78ff"),
                makeAdminStatCard("🔴 Live",   matchStatLive,      "#ff4d6d"),
                makeAdminStatCard("🟡 Upcoming", matchStatUpcoming, "#ffb347"),
                makeAdminStatCard("✅ Done",   matchStatDone,      "#22d98a"),
                makeAdminStatCard("❌ Cancelled", matchStatCancelled,"#9aa3c7")
        );

        // ── Filter row ────────────────────────────────────────────
        HBox filterRow = new HBox(10);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        filterRow.setPadding(new Insets(0, 0, 12, 0));

        matchSearch = new TextField();
        matchSearch.setPromptText("🔍  Search teams, game...");
        matchSearch.setPrefWidth(220);
        matchSearch.setPrefHeight(32);
        matchSearch.setStyle("-fx-background-color: #1c2038; -fx-text-fill: white;" +
                "-fx-background-radius: 8; -fx-border-color: #2e3460;" +
                "-fx-border-radius: 8; -fx-padding: 0 10;");
        matchSearch.textProperty().addListener((o, a, n) -> applyMatchFilters());

        matchStatusFilter = new ComboBox<>();
        matchStatusFilter.setItems(FXCollections.observableArrayList(
                "All Status", "Live", "Upcoming", "Completed", "Cancelled"));
        matchStatusFilter.setValue("All Status");
        matchStatusFilter.setPrefHeight(32);
        matchStatusFilter.setStyle("-fx-background-color: #1c2038; -fx-background-radius: 8;");
        matchStatusFilter.setOnAction(e -> applyMatchFilters());

        matchGameFilter = new ComboBox<>();
        matchGameFilter.setItems(FXCollections.observableArrayList(
                "All Games", "Valorant", "CS2", "LoL", "Dota 2", "R6 Siege", "—"));
        matchGameFilter.setValue("All Games");
        matchGameFilter.setPrefHeight(32);
        matchGameFilter.setStyle("-fx-background-color: #1c2038; -fx-background-radius: 8;");
        matchGameFilter.setOnAction(e -> applyMatchFilters());

        Button exportBtn = makeSmallBtn("📥 Export CSV", "#22d98a");
        exportBtn.setOnAction(e -> exportMatchesCSV());

        Button refreshBtn = makeSmallBtn("🔄 Refresh", "#4d78ff");
        refreshBtn.setOnAction(e -> { loadRealMatchData(); applyMatchFilters(); refreshMatchStats(); });

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        filterRow.getChildren().addAll(matchSearch, matchStatusFilter, matchGameFilter, sp, exportBtn, refreshBtn);

        // ── Report summary panel ──────────────────────────────────
        matchReportPanel = buildMatchReportPanel();

        container.getChildren().addAll(0, List.of(statsRow, filterRow));
        container.getChildren().add(matchReportPanel);

        refreshMatchStats();
    }

    private void refreshMatchStats() {
        if (matchStatTotal == null) return;
        matchStatTotal.setText(String.valueOf(allMatchModels.size()));
        matchStatLive.setText(String.valueOf(allMatchModels.stream().filter(m -> "Live".equals(m.getStatus())).count()));
        matchStatUpcoming.setText(String.valueOf(allMatchModels.stream().filter(m -> "Upcoming".equals(m.getStatus())).count()));
        matchStatDone.setText(String.valueOf(allMatchModels.stream().filter(m -> "Completed".equals(m.getStatus())).count()));
        matchStatCancelled.setText(String.valueOf(allMatchModels.stream().filter(m -> "Cancelled".equals(m.getStatus())).count()));
    }

    private VBox buildMatchReportPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(14, 0, 14, 0));

        Label hdr = new Label("📊  Match Report Summary");
        hdr.setStyle("-fx-text-fill: #4d78ff; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Distribution bar
        HBox distRow = new HBox(6);
        distRow.setAlignment(Pos.CENTER_LEFT);

        // Win-rate distribution chart (top 5 teams by wins)
        VBox chartBox = new VBox(6);
        chartBox.setStyle("-fx-background-color: #111524; -fx-background-radius: 10; -fx-padding: 12;");
        Label chartHdr = new Label("⚔  Teams by Match Count");
        chartHdr.setStyle("-fx-text-fill: #9aa3c7; -fx-font-size: 11px; -fx-font-weight: bold;");
        chartBox.getChildren().add(chartHdr);

        List<TeamAdminModel> top5 = allTeamModels.stream()
                .sorted((a, b) -> b.getMatchCount() - a.getMatchCount())
                .limit(5).collect(Collectors.toList());
        int maxMatches = top5.stream().mapToInt(TeamAdminModel::getMatchCount).max().orElse(1);
        for (TeamAdminModel t : top5) {
            HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
            Label name = new Label(t.getName());
            name.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-min-width: 100;");
            double pct = maxMatches > 0 ? (double) t.getMatchCount() / maxMatches : 0;
            ProgressBar pb = new ProgressBar(pct);
            pb.setPrefWidth(180); pb.setPrefHeight(8);
            pb.setStyle("-fx-accent: #4d78ff;");
            Label cnt = new Label(t.getMatchCount() + " matches");
            cnt.setStyle("-fx-text-fill: #6b7394; -fx-font-size: 10px;");
            row.getChildren().addAll(name, pb, cnt);
            chartBox.getChildren().add(row);
        }

        // Recent flagged matches
        VBox flaggedBox = new VBox(6);
        flaggedBox.setStyle("-fx-background-color: #111524; -fx-background-radius: 10; -fx-padding: 12;");
        Label flaggedHdr = new Label("🚩  Flagged Matches");
        flaggedHdr.setStyle("-fx-text-fill: #ff4d6d; -fx-font-size: 11px; -fx-font-weight: bold;");
        flaggedBox.getChildren().add(flaggedHdr);

        // Show cancelled + any matches flagged in reports
        allMatchModels.stream()
                .filter(m -> "Cancelled".equals(m.getStatus()))
                .limit(4)
                .forEach(m -> {
                    HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
                    row.setStyle("-fx-background-color: rgba(255,77,109,0.06);" +
                            "-fx-background-radius: 7; -fx-padding: 6 8;");
                    Label badge = makeBadge("Cancelled");
                    Label info = new Label("Match #" + m.getId() + "  " + m.getTeam1() + " vs " + m.getTeam2());
                    info.setStyle("-fx-text-fill: #9aa3c7; -fx-font-size: 10px;");
                    HBox.setHgrow(info, Priority.ALWAYS);
                    Button resolve = makeSmallBtn("Review", "#4d78ff");
                    resolve.setOnAction(e -> onViewMatch(m));
                    row.getChildren().addAll(badge, info, resolve);
                    flaggedBox.getChildren().add(row);
                });

        if (flaggedBox.getChildren().size() == 1) {
            Label none = new Label("No flagged matches.");
            none.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");
            flaggedBox.getChildren().add(none);
        }

        HBox.setHgrow(chartBox,  Priority.ALWAYS);
        HBox.setHgrow(flaggedBox, Priority.ALWAYS);
        distRow.getChildren().addAll(chartBox, flaggedBox);
        panel.getChildren().addAll(hdr, distRow);
        return panel;
    }

    private void applyMatchFilters() {
        String q      = matchSearch      != null ? matchSearch.getText().toLowerCase().trim() : "";
        String status = matchStatusFilter != null ? matchStatusFilter.getValue() : "All Status";
        String game   = matchGameFilter   != null ? matchGameFilter.getValue()   : "All Games";

        FilteredList<MatchAdminModel> f = new FilteredList<>(allMatchModels, m ->
                ("All Status".equals(status) || status.equals(m.getStatus())) &&
                        ("All Games".equals(game)   || game.equals(m.getGame()))       &&
                        (q.isEmpty() ||
                                m.getTeam1().toLowerCase().contains(q) ||
                                m.getTeam2().toLowerCase().contains(q) ||
                                m.getGame().toLowerCase().contains(q)  ||
                                String.valueOf(m.getId()).contains(q))
        );
        matchesTable.setItems(f);
    }

    private void exportMatchesCSV() {
        StringBuilder sb = new StringBuilder("ID,Game,Team1,Score,Team2,Date,Status\n");
        for (MatchAdminModel m : matchesTable.getItems())
            sb.append(m.getId()).append(",").append(m.getGame()).append(",")
                    .append(m.getTeam1()).append(",").append(m.getScore()).append(",")
                    .append(m.getTeam2()).append(",").append(m.getDateTime()).append(",")
                    .append(m.getStatus()).append("\n");
        try {
            java.nio.file.Files.writeString(
                    java.nio.file.Path.of(System.getProperty("user.home"), "cartix_matches_export.csv"), sb);
            setStatus("✅ Exported to ~/cartix_matches_export.csv");
            showInfo("Export", "Matches exported to your home folder as cartix_matches_export.csv");
        } catch (Exception e) { setStatus("❌ Export failed: " + e.getMessage()); }
    }

    // ═══════════════════════════════════════════════════════════════
    //  TEAMS TABLE — real DB + moderation + filter + report panel
    // ═══════════════════════════════════════════════════════════════
    private void setupTeamsTable() {
        if (pageTeams != null && pageTeams.getContent() instanceof VBox vbox) {
            injectTeamHeader(vbox);
        }

        colTeamRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colTeamName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTeamGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colTeamPlayers.setCellValueFactory(new PropertyValueFactory<>("players"));
        colTeamWins.setCellValueFactory(new PropertyValueFactory<>("wins"));
        colTeamWinRate.setCellValueFactory(new PropertyValueFactory<>("winRate"));
        colTeamPrize.setCellValueFactory(new PropertyValueFactory<>("prize"));
        colTeamStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Win rate colored column
        colTeamWinRate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                int pct = 0;
                try { pct = Integer.parseInt(item.replace("%", "")); } catch (Exception ignored) {}
                String color = pct >= 65 ? "#22d98a" : pct >= 45 ? "#ffb347" : "#ff4d6d";
                Label lbl = new Label(item);
                lbl.setStyle("-fx-text-fill:" + color + "; -fx-font-weight:bold;" +
                        "-fx-background-color:" + color + "22; -fx-padding:2 8;" +
                        "-fx-background-radius:10;");
                setGraphic(lbl); setText(null);
            }
        });

        colTeamStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                setGraphic(makeBadge(item)); setText(null);
            }
        });

        colTeamActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn     = makeSmallBtn("View",     "#4d78ff");
            private final Button reportBtn   = makeSmallBtn("Report",   "#ffb347");
            private final Button dissolveBtn = makeSmallBtn("Dissolve", "#ff4d6d");
            private final HBox   box         = new HBox(5, viewBtn, reportBtn, dissolveBtn);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                viewBtn.setOnAction(e     -> onViewTeam(getTableView().getItems().get(getIndex())));
                reportBtn.setOnAction(e   -> onReportTeam(getTableView().getItems().get(getIndex())));
                dissolveBtn.setOnAction(e -> onDissolveTeam(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        styleTable(teamsTable);
        teamsTable.setItems(allTeamModels);
    }

    private void injectTeamHeader(VBox container) {
        // ── Stats row ─────────────────────────────────────────────
        HBox statsRow = new HBox(12);
        statsRow.setPadding(new Insets(0, 0, 14, 0));

        teamStatTotal      = new Label("0");
        teamStatActive     = new Label("0");
        teamStatRecruiting = new Label("0");
        teamStatInactive   = new Label("0");

        statsRow.getChildren().addAll(
                makeAdminStatCard("Total Teams",  teamStatTotal,      "#4d78ff"),
                makeAdminStatCard("✅ Active",    teamStatActive,     "#22d98a"),
                makeAdminStatCard("📢 Recruiting", teamStatRecruiting,"#ffb347"),
                makeAdminStatCard("💤 Inactive",  teamStatInactive,   "#9aa3c7")
        );

        // ── Filter row ────────────────────────────────────────────
        HBox filterRow = new HBox(10);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        filterRow.setPadding(new Insets(0, 0, 12, 0));

        teamSearch = new TextField();
        teamSearch.setPromptText("🔍  Search team name...");
        teamSearch.setPrefWidth(220); teamSearch.setPrefHeight(32);
        teamSearch.setStyle("-fx-background-color: #1c2038; -fx-text-fill: white;" +
                "-fx-background-radius: 8; -fx-border-color: #2e3460;" +
                "-fx-border-radius: 8; -fx-padding: 0 10;");
        teamSearch.textProperty().addListener((o, a, n) -> applyTeamFilters());

        teamGameFilter = new ComboBox<>();
        teamGameFilter.setItems(FXCollections.observableArrayList(
                "All Games", "Valorant", "CS2", "LoL", "Dota 2", "R6 Siege", "—"));
        teamGameFilter.setValue("All Games");
        teamGameFilter.setPrefHeight(32);
        teamGameFilter.setStyle("-fx-background-color: #1c2038; -fx-background-radius: 8;");
        teamGameFilter.setOnAction(e -> applyTeamFilters());

        Button exportBtn  = makeSmallBtn("📥 Export CSV", "#22d98a");
        exportBtn.setOnAction(e -> exportTeamsCSV());
        Button refreshBtn = makeSmallBtn("🔄 Refresh", "#4d78ff");
        refreshBtn.setOnAction(e -> { loadRealTeamData(); applyTeamFilters(); refreshTeamStats(); });

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        filterRow.getChildren().addAll(teamSearch, teamGameFilter, sp, exportBtn, refreshBtn);

        teamReportPanel = buildTeamReportPanel();

        container.getChildren().addAll(0, List.of(statsRow, filterRow));
        container.getChildren().add(teamReportPanel);

        refreshTeamStats();
    }

    private void refreshTeamStats() {
        if (teamStatTotal == null) return;
        teamStatTotal.setText(String.valueOf(allTeamModels.size()));
        teamStatActive.setText(String.valueOf(allTeamModels.stream().filter(t -> "Active".equals(t.getStatus())).count()));
        teamStatRecruiting.setText(String.valueOf(allTeamModels.stream().filter(t -> "Recruiting".equals(t.getStatus())).count()));
        teamStatInactive.setText(String.valueOf(allTeamModels.stream().filter(t -> "Inactive".equals(t.getStatus())).count()));
    }

    private VBox buildTeamReportPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(14, 0, 14, 0));

        Label hdr = new Label("📊  Team Report Summary");
        hdr.setStyle("-fx-text-fill: #22d98a; -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox row = new HBox(12);

        // Top teams by win rate
        VBox winBox = new VBox(6);
        winBox.setStyle("-fx-background-color: #111524; -fx-background-radius: 10; -fx-padding: 12;");
        Label winHdr = new Label("🏆  Top Teams by Win Rate");
        winHdr.setStyle("-fx-text-fill: #9aa3c7; -fx-font-size: 11px; -fx-font-weight: bold;");
        winBox.getChildren().add(winHdr);

        allTeamModels.stream()
                .sorted((a, b) -> {
                    int wa = parseWinPct(a.getWinRate()), wb = parseWinPct(b.getWinRate());
                    return wb - wa;
                })
                .limit(5)
                .forEach(t -> {
                    HBox r = new HBox(8); r.setAlignment(Pos.CENTER_LEFT);
                    Label nm = new Label(t.getName());
                    nm.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-min-width: 100;");
                    double pct = parseWinPct(t.getWinRate()) / 100.0;
                    ProgressBar pb = new ProgressBar(pct); pb.setPrefWidth(160); pb.setPrefHeight(8);
                    String clr = pct >= 0.65 ? "#22d98a" : pct >= 0.45 ? "#ffb347" : "#ff4d6d";
                    pb.setStyle("-fx-accent: " + clr + ";");
                    Label wr = new Label(t.getWinRate());
                    wr.setStyle("-fx-text-fill:" + clr + "; -fx-font-size:10px; -fx-font-weight:bold;");
                    r.getChildren().addAll(nm, pb, wr);
                    winBox.getChildren().add(r);
                });

        // Team size distribution
        VBox sizeBox = new VBox(6);
        sizeBox.setStyle("-fx-background-color: #111524; -fx-background-radius: 10; -fx-padding: 12;");
        Label sizeHdr = new Label("👥  Team Size Distribution");
        sizeHdr.setStyle("-fx-text-fill: #9aa3c7; -fx-font-size: 11px; -fx-font-weight: bold;");
        sizeBox.getChildren().add(sizeHdr);

        allTeamModels.stream().limit(6).forEach(t -> {
            HBox r = new HBox(8); r.setAlignment(Pos.CENTER_LEFT);
            Label nm = new Label(t.getName());
            nm.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-min-width: 100;");
            Label cnt = new Label(t.getPlayers() + " members");
            cnt.setStyle("-fx-text-fill: #4d78ff; -fx-font-size: 10px; -fx-font-weight: bold;" +
                    "-fx-background-color: rgba(77,120,255,0.1); -fx-padding: 2 6; -fx-background-radius: 6;");
            r.getChildren().addAll(nm, cnt);
            sizeBox.getChildren().add(r);
        });

        HBox.setHgrow(winBox,  Priority.ALWAYS);
        HBox.setHgrow(sizeBox, Priority.ALWAYS);
        row.getChildren().addAll(winBox, sizeBox);
        panel.getChildren().addAll(hdr, row);
        return panel;
    }

    private void applyTeamFilters() {
        String q    = teamSearch     != null ? teamSearch.getText().toLowerCase().trim() : "";
        String game = teamGameFilter != null ? teamGameFilter.getValue() : "All Games";

        FilteredList<TeamAdminModel> f = new FilteredList<>(allTeamModels, t ->
                ("All Games".equals(game) || game.equals(t.getGame())) &&
                        (q.isEmpty() || t.getName().toLowerCase().contains(q)  ||
                                t.getGame().toLowerCase().contains(q))
        );
        teamsTable.setItems(f);
    }

    private void exportTeamsCSV() {
        StringBuilder sb = new StringBuilder("Rank,Name,Game,Players,Matches,Wins,WinRate,Status\n");
        for (TeamAdminModel t : teamsTable.getItems())
            sb.append(t.getRank()).append(",").append(t.getName()).append(",")
                    .append(t.getGame()).append(",").append(t.getPlayers()).append(",")
                    .append(t.getMatchCount()).append(",").append(t.getWins()).append(",")
                    .append(t.getWinRate()).append(",").append(t.getStatus()).append("\n");
        try {
            java.nio.file.Files.writeString(
                    java.nio.file.Path.of(System.getProperty("user.home"), "cartix_teams_export.csv"), sb);
            setStatus("✅ Exported to ~/cartix_teams_export.csv");
            showInfo("Export", "Teams exported to your home folder as cartix_teams_export.csv");
        } catch (Exception e) { setStatus("❌ Export failed: " + e.getMessage()); }
    }

    // ═══════════════════════════════════════════════════════════════
    //  MATCH ROW ACTIONS
    // ═══════════════════════════════════════════════════════════════
    private void onViewMatch(MatchAdminModel m) {
        showInfo("Match #" + m.getId(),
                "Game: " + m.getGame() +
                        "\nTeams: " + m.getTeam1() + " vs " + m.getTeam2() +
                        "\nScore: " + m.getScore() +
                        "\nDate: " + m.getDateTime() +
                        "\nStatus: " + m.getStatus());
    }

    private void onReportMatch(MatchAdminModel m) {
        allReports.add(new ReportModel(
                allReports.size() + 1, "Match",
                "Match #" + m.getId() + " (" + m.getTeam1() + " vs " + m.getTeam2() + ")",
                "Admin", "Flagged for review",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM d yyyy")),
                "Medium"
        ));
        setStatus("🚩 Match #" + m.getId() + " reported.");
        showInfo("Reported", "Match #" + m.getId() + " has been added to moderation queue.");
    }

    private void onCancelMatch(MatchAdminModel m) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Match"); confirm.setHeaderText(null);
        confirm.setContentText("Cancel match #" + m.getId() + " (" + m.getTeam1() + " vs " + m.getTeam2() + ")?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    models.Match dbMatch = matchService.getById(m.getId());
                    if (dbMatch != null) {
                        dbMatch.setStatut("annule");
                        matchService.update(dbMatch);
                    }
                    loadRealMatchData();
                    applyMatchFilters();
                    refreshMatchStats();
                    setStatus("❌ Match #" + m.getId() + " cancelled.");
                } catch (Exception e) { setStatus("❌ Error: " + e.getMessage()); }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  TEAM ROW ACTIONS
    // ═══════════════════════════════════════════════════════════════
    private void onViewTeam(TeamAdminModel t) {
        showInfo("Team: " + t.getName(),
                "Game: " + t.getGame() +
                        "\nMembers: " + t.getPlayers() +
                        "\nMatches played: " + t.getMatchCount() +
                        "\nWins: " + t.getWins() +
                        "\nWin Rate: " + t.getWinRate() +
                        "\nStatus: " + t.getStatus());
    }

    private void onReportTeam(TeamAdminModel t) {
        allReports.add(new ReportModel(
                allReports.size() + 1, "Team",
                t.getName(), "Admin", "Flagged for review",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM d yyyy")),
                "Medium"
        ));
        setStatus("🚩 Team " + t.getName() + " reported.");
        showInfo("Reported", "Team \"" + t.getName() + "\" has been added to moderation queue.");
    }

    private void onDissolveTeam(TeamAdminModel t) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Dissolve Team"); confirm.setHeaderText(null);
        confirm.setContentText("Dissolve team \"" + t.getName() + "\"? This will delete it from the DB.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    equipeService.delete(t.getDbId());
                    loadRealTeamData();
                    applyTeamFilters();
                    refreshTeamStats();
                    setStatus("🗑 Team \"" + t.getName() + "\" dissolved.");
                } catch (Exception e) { setStatus("❌ Error: " + e.getMessage()); }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPER: stat card
    // ═══════════════════════════════════════════════════════════════
    private VBox makeAdminStatCard(String label, Label valueLabel, String color) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle("-fx-background-color: #111524; -fx-background-radius: 10;" +
                "-fx-border-color: " + color + "33; -fx-border-radius: 10; -fx-border-width: 1;");
        card.setPrefWidth(140);

        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #6b7394; -fx-font-size: 11px;");
        card.getChildren().addAll(valueLabel, lbl);
        return card;
    }

    private int parseWinPct(String wr) {
        try { return Integer.parseInt(wr.replace("%", "").trim()); }
        catch (Exception e) { return 0; }
    }

    // ═══════════════════════════════════════════════════════════════
    //  ALL ORIGINAL SETUP METHODS (unchanged)
    // ═══════════════════════════════════════════════════════════════
    private void startClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm:ss");
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> clockLabel.setText(LocalDateTime.now().format(fmt))));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        clockLabel.setText(LocalDateTime.now().format(fmt));
    }

    private void seedStaticData() {
        allUsers.addAll(
                new UserModel(1,"ShadowRift","iheb@cartix.gg","Pro","Valorant","Jan 12 2024","Active"),
                new UserModel(2,"NightTrace","alex@cartix.gg","Pro","CS2","Feb 5 2024","Active"),
                new UserModel(3,"PhantomX","sami@cartix.gg","Pro","LoL","Mar 3 2024","Active"),
                new UserModel(4,"VoidZero","lucas@cartix.gg","Pro","Valorant","Apr 1 2024","Active"),
                new UserModel(5,"CrimsonFox","omar@cartix.gg","Pro","CS2","Apr 14 2024","Active"),
                new UserModel(6,"BladeStorm","chen@cartix.gg","Pro","Valorant","May 2 2024","Inactive"),
                new UserModel(7,"ArcLight","yuki@cartix.gg","User","LoL","May 18 2024","Active"),
                new UserModel(8,"NeonPulse","sara@cartix.gg","User","CS2","Jun 7 2024","Active"),
                new UserModel(9,"GhostFrame","dmitri@cartix.gg","User","Valorant","Jun 22 2024","Banned"),
                new UserModel(10,"FrostByte","mohammed@cartix.gg","User","LoL","Jul 3 2024","Active")
        );
        allPlayers.addAll(
                new PlayerModel(1,"ShadowRift","Iheb Tarhouni","Valorant","Team Nexus",4.8,"72%",312,98.2,"Active"),
                new PlayerModel(2,"NightTrace","Alex Morel","CS2","Iron Wolves",4.5,"68%",289,96.5,"Active"),
                new PlayerModel(3,"PhantomX","Sami Belkaid","LoL","Red Grid",4.2,"65%",256,94.1,"Active"),
                new PlayerModel(4,"VoidZero","Lucas Ferreira","Valorant","Dark Knights",3.9,"63%",241,91.8,"Active"),
                new PlayerModel(5,"CrimsonFox","Omar Benali","CS2","Storm Pulse",3.7,"60%",218,89.3,"Active"),
                new PlayerModel(6,"BladeStorm","Chen Wei","Valorant","Team Nexus",3.5,"58%",198,87.0,"Inactive"),
                new PlayerModel(7,"ArcLight","Yuki Tanaka","LoL","Iron Wolves",3.4,"57%",187,85.2,"Active"),
                new PlayerModel(8,"NeonPulse","Sara Dupont","CS2","Red Grid",3.2,"55%",174,82.7,"Active"),
                new PlayerModel(9,"GhostFrame","Dmitri Volkov","Valorant","Storm Pulse",3.0,"53%",161,80.1,"Inactive"),
                new PlayerModel(10,"FrostByte","Mohammed A.","LoL","Dark Knights",2.9,"51%",148,77.4,"Active")
        );
        allTournaments.addAll(
                new TournamentModel(1,"VCT Champions 2025","Valorant",32,"$1,000,000","Apr 1 2025","Live"),
                new TournamentModel(2,"IEM Cologne 2025","CS2",16,"$500,000","Apr 12 2025","Upcoming"),
                new TournamentModel(3,"MSI 2025","LoL",24,"$800,000","Apr 20 2025","Upcoming"),
                new TournamentModel(4,"ESL Pro League S20","CS2",16,"$750,000","Mar 28 2025","Completed"),
                new TournamentModel(5,"Nexus Cup Spring","Valorant",8,"$10,000","Apr 15 2025","Upcoming"),
                new TournamentModel(6,"Valorant Open 2025","Valorant",64,"$50,000","May 1 2025","Upcoming")
        );
        allReports.addAll(
                new ReportModel(1,"Player","GhostFrame","NightTrace","Cheating / Aimbot","Apr 7 2025","High"),
                new ReportModel(2,"Player","BladeStorm","CrimsonFox","Harassment in chat","Apr 6 2025","Medium"),
                new ReportModel(3,"Tournament","Nexus Cup","VoidZero","Unfair bracket seeding","Apr 5 2025","Low"),
                new ReportModel(4,"Match","Match #4","NeonPulse","Score manipulation","Apr 4 2025","High"),
                new ReportModel(5,"Player","FrostByte","ShadowRift","Hate speech","Apr 3 2025","High")
        );
    }

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
            private final Button editBtn = makeSmallBtn("Edit","#4d78ff");
            private final Button banBtn  = makeSmallBtn("Ban","#ff4d6d");
            private final HBox   box     = new HBox(6, editBtn, banBtn);
            { box.setAlignment(Pos.CENTER_LEFT);
                editBtn.setOnAction(e -> onEditUser(getTableView().getItems().get(getIndex())));
                banBtn.setOnAction(e  -> onBanUserRow(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : box); }
        });
        styleTable(usersTable);
        usersTable.setItems(new FilteredList<>(allUsers, p -> true));
        if (userCountLabel != null) userCountLabel.setText(allUsers.size() + " users");
    }

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
            private final Button viewBtn    = makeSmallBtn("View","#4d78ff");
            private final Button suspendBtn = makeSmallBtn("Suspend","#ffb347");
            private final HBox   box        = new HBox(6, viewBtn, suspendBtn);
            { box.setAlignment(Pos.CENTER_LEFT);
                viewBtn.setOnAction(e    -> onViewPlayer(getTableView().getItems().get(getIndex())));
                suspendBtn.setOnAction(e -> onSuspendPlayer(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : box); }
        });
        styleTable(playersTable);
        playersTable.setItems(allPlayers);
    }

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
            private final Button viewBtn   = makeSmallBtn("View","#4d78ff");
            private final Button editBtn   = makeSmallBtn("Edit","#22d98a");
            private final Button cancelBtn = makeSmallBtn("Cancel","#ff4d6d");
            private final HBox   box       = new HBox(4, viewBtn, editBtn, cancelBtn);
            { box.setAlignment(Pos.CENTER_LEFT); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : box); }
        });
        styleTable(tournamentsTable);
        tournamentsTable.setItems(allTournaments);
    }

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
                        : "Medium".equals(item) ? "rgba(255,179,71,0.12)" : "rgba(34,217,138,0.1)";
                Label lbl = new Label(item);
                lbl.setStyle("-fx-text-fill:" + color + "; -fx-background-color:" + bg +
                        "; -fx-padding:2 8; -fx-background-radius:10; -fx-font-weight:bold; -fx-font-size:10;");
                setGraphic(lbl); setText(null);
            }
        });
        colRepActions.setCellFactory(col -> new TableCell<>() {
            private final Button resolveBtn = makeSmallBtn("Resolve","#22d98a");
            private final Button banBtn     = makeSmallBtn("Ban","#ff4d6d");
            private final Button dismissBtn = makeSmallBtn("Dismiss","#6b7394");
            private final HBox   box        = new HBox(4, resolveBtn, banBtn, dismissBtn);
            { box.setAlignment(Pos.CENTER_LEFT); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : box); }
        });
        styleTable(moderationTable);
        moderationTable.setItems(allReports);
    }

    private void populateDashboard() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Registrations");
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        int[]    vals = {420,380,510,470,620,780,540};
        for (int i = 0; i < days.length; i++)
            series.getData().add(new XYChart.Data<>(days[i], vals[i]));
        registrationsChart.getData().add(series);
        registrationsChart.setLegendVisible(false);
        registrationsChart.setStyle("-fx-background-color:transparent;-fx-plot-background-color:transparent;-fx-horizontal-grid-lines-visible:false;");

        List.of(
                new String[]{"#22d98a","New user registered: ShadowRift","2m ago"},
                new String[]{"#4d78ff","Tournament created: Nexus Cup Spring","8m ago"},
                new String[]{"#ff3b5c","Report filed: GhostFrame — Cheating","15m ago"},
                new String[]{"#ffb347","Match scheduled: Paper Rex vs ZETA","32m ago"},
                new String[]{"#22d98a","Team verified: Storm Pulse","1h ago"},
                new String[]{"#ff4d6d","User banned: GhostFrame","2h ago"}
        ).forEach(a -> {
            HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(9,0,9,0));
            row.setStyle("-fx-border-color:rgba(255,255,255,0.04); -fx-border-width:0 0 1 0;");
            Rectangle dot = new Rectangle(6,6); dot.setArcWidth(6); dot.setArcHeight(6);
            dot.setFill(Color.web(a[0]));
            Label msg = new Label(a[1]); msg.setStyle("-fx-text-fill:#e8eaf6; -fx-font-size:12;");
            HBox.setHgrow(msg, Priority.ALWAYS);
            Label time = new Label(a[2]); time.setStyle("-fx-text-fill:#6b7394; -fx-font-size:11;");
            row.getChildren().addAll(dot, msg, time);
            activityFeed.getChildren().add(row);
        });

        for (int i = 0; i < 5; i++) {
            PlayerModel p = allPlayers.get(i);
            HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8,0,8,0));
            row.setStyle("-fx-border-color:rgba(255,255,255,0.04); -fx-border-width:0 0 1 0;");
            Label rank = new Label(String.valueOf(p.getRank()));
            rank.setStyle("-fx-text-fill:#4a5070; -fx-font-weight:bold; -fx-font-size:14; -fx-min-width:20;");
            Label name = new Label(p.getHandle()); name.setStyle("-fx-text-fill:#e8eaf6; -fx-font-weight:bold; -fx-font-size:12;");
            HBox.setHgrow(name, Priority.ALWAYS);
            Label game = new Label(p.getGame());
            game.setStyle("-fx-text-fill:#6b7394; -fx-font-size:11; -fx-background-color:#1c2038; -fx-padding:2 8; -fx-background-radius:10;");
            String rc = p.getRating() >= 95 ? "#22d98a" : "#4d78ff";
            Label ratingLbl = new Label(String.valueOf(p.getRating()));
            ratingLbl.setStyle("-fx-text-fill:" + rc + "; -fx-font-weight:bold; -fx-font-size:12;");
            row.getChildren().addAll(rank, name, game, ratingLbl);
            topPlayersList.getChildren().add(row);
        }

        for (ReportModel r : allReports) {
            VBox card = new VBox(6); card.setPadding(new Insets(10));
            card.setStyle("-fx-background-color:#161a2e; -fx-background-radius:8; -fx-border-color:rgba(255,77,109,0.15); -fx-border-radius:8;");
            HBox header = new HBox(8); header.setAlignment(Pos.CENTER_LEFT);
            String sc = "High".equals(r.getSeverity()) ? "#ff4d6d" : "Medium".equals(r.getSeverity()) ? "#ffb347" : "#22d98a";
            Label sev  = new Label("● " + r.getSeverity()); sev.setStyle("-fx-text-fill:" + sc + "; -fx-font-size:10; -fx-font-weight:bold;");
            Label type = new Label(r.getType()); type.setStyle("-fx-text-fill:#6b7394; -fx-font-size:10;");
            Region sp  = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Label date = new Label(r.getDate()); date.setStyle("-fx-text-fill:#4a5070; -fx-font-size:10;");
            header.getChildren().addAll(sev, type, sp, date);
            Label reason = new Label(r.getReason() + " → " + r.getTarget());
            reason.setStyle("-fx-text-fill:#e8eaf6; -fx-font-size:11;");
            HBox btns = new HBox(6, makeSmallBtn("Resolve","#22d98a"), makeSmallBtn("Ban","#ff4d6d"));
            card.getChildren().addAll(header, reason, btns);
            flaggedList.getChildren().add(card);
        }

        // Live matches from real DB
        allMatchModels.stream().filter(m -> "Live".equals(m.getStatus())).forEach(m -> {
            HBox row = new HBox(14); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12,16,12,16));
            row.setStyle("-fx-background-color:#161a2e; -fx-background-radius:10; -fx-border-color:rgba(255,59,92,0.15); -fx-border-radius:10;");
            Label game  = new Label(m.getGame()); game.setStyle("-fx-text-fill:#e8eaf6; -fx-background-color:#1c2038; -fx-padding:3 8; -fx-background-radius:8; -fx-font-size:11;");
            Label t1    = new Label(m.getTeam1()); t1.setStyle("-fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:14;");
            Label score = new Label(m.getScore()); score.setStyle("-fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:18; -fx-background-color:#1c2038; -fx-padding:4 16; -fx-background-radius:8;");
            Label t2    = new Label(m.getTeam2()); t2.setStyle("-fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:14;");
            Region sp   = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            row.getChildren().addAll(game, t1, score, t2, sp, makeSmallBtn("Watch Live","#4d78ff"));
            liveMatchesList.getChildren().add(row);
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  NAV
    // ═══════════════════════════════════════════════════════════════
    @FXML private void onNavDashboard()   { showPage("Dashboard",   "Platform overview · Season 2025",   pageDashboard,   navDashboard,   "+ New Entry"); }
    @FXML private void onNavUsers()       { showPage("Users",       "Manage all registered accounts",    pageUsers,       navUsers,       "+ New User"); }
    @FXML private void onNavPlayers()     { showPage("Players",     "Player rankings · Season 2025",     pagePlayers,     navPlayers,     "+ Invite Player"); }
    @FXML private void onNavMatches()     { showPage("Matches",     "Moderation & Reports · Real DB",    pageMatches,     navMatches,     "+ Schedule Match"); }
    @FXML private void onNavTeams()       { showPage("Teams",       "Team Management · Real DB",         pageTeams,       navTeams,       "+ Create Team"); }
    @FXML private void onNavTournaments() { showPage("Tournaments", "4,800 tournaments hosted",          pageTournaments, navTournaments, "+ Host Tournament"); }
    @FXML private void onNavReports()     { showPage("Moderation",  "Pending reports & flagged content", pageModeration,  navReports,     "Review All"); }
    @FXML private void onNavModeration()  { showPage("Moderation",  "Pending reports & flagged content", pageModeration,  navModeration,  "Review All"); }
    @FXML private void onNavSettings()    { showPage("Settings",    "Platform configuration",            pageSettings,    navSettings,    "Save Changes"); }

    private void showPage(String title, String subtitle, ScrollPane page, HBox nav, String btnText) {
        List.of(pageDashboard, pageUsers, pagePlayers, pageMatches,
                        pageTeams, pageTournaments, pageModeration, pageSettings)
                .forEach(p -> { p.setVisible(false); p.setManaged(false); });
        page.setVisible(true); page.setManaged(true);
        pageTitle.setText(title); pageSubtitle.setText(subtitle); actionBtn.setText(btnText);
        if (currentNav != null)
            currentNav.setStyle("-fx-padding:9 10; -fx-background-radius:8; -fx-background-color:transparent; -fx-cursor:hand;");
        nav.setStyle("-fx-padding:9 10; -fx-background-radius:8; -fx-background-color:rgba(77,120,255,0.15); -fx-border-color:rgba(77,120,255,0.2); -fx-border-radius:8; -fx-cursor:hand;");
        currentNav = nav;
        setStatus("Viewing: " + title);
    }

    @FXML private void onNavHoverEnter(MouseEvent e) {
        HBox nav = (HBox) e.getSource();
        if (nav != currentNav) nav.setStyle("-fx-padding:9 10; -fx-background-radius:8; -fx-background-color:#161a2e; -fx-cursor:hand;");
    }
    @FXML private void onNavHoverExit(MouseEvent e) {
        HBox nav = (HBox) e.getSource();
        if (nav != currentNav) nav.setStyle("-fx-padding:9 10; -fx-background-radius:8; -fx-background-color:transparent; -fx-cursor:hand;");
    }

    @FXML private void onSearch() {
        String q = searchField.getText().toLowerCase().trim();
        FilteredList<UserModel> f = new FilteredList<>(allUsers,
                u -> q.isEmpty() || u.getUsername().toLowerCase().contains(q) || u.getEmail().toLowerCase().contains(q));
        usersTable.setItems(f);
        if (userCountLabel != null) userCountLabel.setText(f.size() + " users");
    }

    @FXML private void onFilterChange() {
        String role = filterRole != null && filterRole.getValue() != null ? filterRole.getValue() : "All Roles";
        String status = filterStatus != null && filterStatus.getValue() != null ? filterStatus.getValue() : "All Status";
        String game = filterGame != null && filterGame.getValue() != null ? filterGame.getValue() : "All Games";
        String playerGame = filterPlayerGame != null && filterPlayerGame.getValue() != null ? filterPlayerGame.getValue() : "All Games";
        FilteredList<UserModel> fu = new FilteredList<>(allUsers,
                u -> ("All Roles".equals(role) || role.equals(u.getRole()))
                        && ("All Status".equals(status) || status.equals(u.getStatus()))
                        && ("All Games".equals(game) || game.equals(u.getGame())));
        usersTable.setItems(fu);
        if (userCountLabel != null) userCountLabel.setText(fu.size() + " users");
        FilteredList<PlayerModel> fp = new FilteredList<>(allPlayers,
                p -> "All Games".equals(playerGame) || playerGame.equals(p.getGame()));
        playersTable.setItems(fp);
    }

    @FXML private void onActionBtn()          { setStatus("Action: " + actionBtn.getText()); showInfo("Action", actionBtn.getText() + " — coming soon."); }
    @FXML private void onCreateTournament()   { setStatus("Create Tournament wizard opened"); showInfo("Create Tournament","Tournament wizard — coming soon."); }
    @FXML private void onBanUser()            { setStatus("Ban User dialog opened"); showInfo("Ban User","Select a user to ban from the Users table."); }
    @FXML private void onAnnouncement()       { setStatus("Announcement composer opened"); showInfo("Announcement","Broadcast a platform-wide message."); }
    @FXML private void onExportBackup()       { setStatus("Exporting database backup…"); showInfo("Export Backup","Database backup initiated."); }
    @FXML private void onRefreshCache()       { setStatus("Cache refreshed ✓"); showInfo("Cache","Platform cache has been cleared."); }
    @FXML private void onScheduleMatch()      { setStatus("Schedule Match form opened"); showInfo("Schedule Match","Match scheduling form — coming soon."); }
    @FXML private void onCreateTeam()         { setStatus("Create Team form opened"); showInfo("Create Team","Team creation form — coming soon."); }
    @FXML private void onExportUsers()        { setStatus("Exporting user CSV…"); showInfo("Export","User data export initiated."); }
    @FXML private void onSaveSettings()       { setStatus("Settings saved ✓"); showInfo("Settings","Platform settings saved successfully."); }
    @FXML private void onResetSettings()      {
        settingPlatformName.setText("Cartix"); settingSeasonName.setText("Season 2025");
        settingMaxTeams.setText("32"); settingMaintenance.setSelected(false); settingRegistrations.setSelected(true);
        setStatus("Settings reset to defaults ✓");
    }

    private void onEditUser(UserModel u)        { showInfo("Edit User","Editing: " + u.getUsername()); }
    private void onBanUserRow(UserModel u)       { u.setStatus("Banned"); usersTable.refresh(); setStatus("Banned: " + u.getUsername()); }
    private void onViewPlayer(PlayerModel p)     { showInfo("Player Profile","Viewing: " + p.getHandle() + " · Rating: " + p.getRating()); }
    private void onSuspendPlayer(PlayerModel p)  { p.setStatus("Inactive"); playersTable.refresh(); setStatus("Suspended: " + p.getHandle()); }

    private void setStatus(String msg) { if (statusLabel != null) statusLabel.setText(msg); }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg);
        alert.getDialogPane().setStyle("-fx-background-color:#111524; -fx-border-color:rgba(77,120,255,0.3); -fx-border-radius:12;");
        alert.showAndWait();
    }

    private Label makeBadge(String text) {
        Label lbl = new Label(text);
        String style = switch (text) {
            case "Active"     -> "-fx-text-fill:#22d98a; -fx-background-color:rgba(34,217,138,0.12);";
            case "Inactive"   -> "-fx-text-fill:#9aa3c7; -fx-background-color:rgba(154,163,199,0.12);";
            case "Banned"     -> "-fx-text-fill:#ff4d6d; -fx-background-color:rgba(255,77,109,0.12);";
            case "Recruiting" -> "-fx-text-fill:#ffb347; -fx-background-color:rgba(255,179,71,0.12);";
            case "Live"       -> "-fx-text-fill:#ff4d6d; -fx-background-color:rgba(255,77,109,0.12);";
            case "Upcoming"   -> "-fx-text-fill:#4d78ff; -fx-background-color:rgba(77,120,255,0.12);";
            case "Completed"  -> "-fx-text-fill:#9aa3c7; -fx-background-color:rgba(154,163,199,0.12);";
            case "Cancelled"  -> "-fx-text-fill:#ff4d6d; -fx-background-color:rgba(255,77,109,0.08);";
            default           -> "-fx-text-fill:#e8eaf6; -fx-background-color:rgba(255,255,255,0.08);";
        };
        lbl.setStyle(style + " -fx-padding:2 8; -fx-background-radius:10; -fx-font-size:10; -fx-font-weight:bold;");
        return lbl;
    }

    private Button makeSmallBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setFocusTraversable(false);
        btn.setStyle("-fx-background-color:" + color + "; -fx-text-fill:white; -fx-font-size:11;" +
                "-fx-font-weight:bold; -fx-background-radius:8; -fx-padding:4 10; -fx-cursor:hand; -fx-border-width:0;");
        return btn;
    }

    private void styleTable(TableView<?> table) {
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(42);
    }

    // ═══════════════════════════════════════════════════════════════
    //  DATA MODELS
    // ═══════════════════════════════════════════════════════════════

    // ─── MatchAdminModel (replaces MatchModel, uses real DB) ──────
    public static final class MatchAdminModel {
        private final int id; private final String game, team1, score, team2, dateTime, status;
        public MatchAdminModel(int id, String game, String team1, String score, String team2, String dateTime, String status) {
            this.id = id; this.game = game; this.team1 = team1; this.score = score;
            this.team2 = team2; this.dateTime = dateTime; this.status = status;
        }
        public int getId()         { return id; }
        public String getGame()    { return game; }
        public String getTeam1()   { return team1; }
        public String getScore()   { return score; }
        public String getTeam2()   { return team2; }
        public String getDateTime(){ return dateTime; }
        public String getStatus()  { return status; }
    }

    // ─── TeamAdminModel (replaces TeamModel, uses real DB) ────────
    public static final class TeamAdminModel {
        private final int rank, dbId, players, matchCount, wins;
        private final String name, game, winRate, prize;
        private String status;
        public TeamAdminModel(int rank, int dbId, String name, String game, int players,
                              int matchCount, int wins, String winRate, String prize, String status) {
            this.rank = rank; this.dbId = dbId; this.name = name; this.game = game;
            this.players = players; this.matchCount = matchCount; this.wins = wins;
            this.winRate = winRate; this.prize = prize; this.status = status;
        }
        public int getRank()       { return rank; }
        public int getDbId()       { return dbId; }
        public String getName()    { return name; }
        public String getGame()    { return game; }
        public int getPlayers()    { return players; }
        public int getMatchCount() { return matchCount; }
        public int getWins()       { return wins; }
        public String getWinRate() { return winRate; }
        public String getPrize()   { return prize; }
        public String getStatus()  { return status; }
        public void setStatus(String s) { this.status = s; }
    }

    // ─── Remaining static models (unchanged) ─────────────────────
    public static final class UserModel {
        public final int id; public final String username, email, role, game, joined; public String status;
        private UserModel(int id, String username, String email, String role, String game, String joined, String status) {
            this.id=id; this.username=username; this.email=email; this.role=role; this.game=game; this.joined=joined; this.status=status; }
        public int getId(){ return id; } public String getUsername(){ return username; }
        public String getEmail(){ return email; } public String getRole(){ return role; }
        public String getGame(){ return game; } public String getJoined(){ return joined; }
        public String getStatus(){ return status; } public void setStatus(String s){ this.status=s; }
    }
    public static final class PlayerModel {
        public final int rank, matches; public final String handle, fullName, game, team, winRate; public final double kda, rating; public String status;
        private PlayerModel(int rank, String handle, String fullName, String game, String team, double kda, String winRate, int matches, double rating, String status) {
            this.rank=rank; this.handle=handle; this.fullName=fullName; this.game=game; this.team=team; this.kda=kda; this.winRate=winRate; this.matches=matches; this.rating=rating; this.status=status; }
        public int getRank(){ return rank; } public String getHandle(){ return handle; }
        public String getFullName(){ return fullName; } public String getGame(){ return game; }
        public String getTeam(){ return team; } public double getKda(){ return kda; }
        public String getWinRate(){ return winRate; } public int getMatches(){ return matches; }
        public double getRating(){ return rating; } public String getStatus(){ return status; }
        public void setStatus(String s){ this.status=s; }
    }
    public static final class TournamentModel {
        public final int id, teams; public final String name, game, prize, startDate, status;
        private TournamentModel(int id, String name, String game, int teams, String prize, String startDate, String status) {
            this.id=id; this.name=name; this.game=game; this.teams=teams; this.prize=prize; this.startDate=startDate; this.status=status; }
        public int getId(){ return id; } public String getName(){ return name; }
        public String getGame(){ return game; } public int getTeams(){ return teams; }
        public String getPrize(){ return prize; } public String getStartDate(){ return startDate; }
        public String getStatus(){ return status; }
    }
    public static final class ReportModel {
        public final int id; public final String type, target, reporter, reason, date, severity;
        public ReportModel(int id, String type, String target, String reporter, String reason, String date, String severity) {
            this.id=id; this.type=type; this.target=target; this.reporter=reporter; this.reason=reason; this.date=date; this.severity=severity; }
        public int getId(){ return id; } public String getType(){ return type; }
        public String getTarget(){ return target; } public String getReporter(){ return reporter; }
        public String getReason(){ return reason; } public String getDate(){ return date; }
        public String getSeverity(){ return severity; }
    }
}