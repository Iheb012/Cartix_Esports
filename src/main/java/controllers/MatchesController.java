package controllers;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.AppScene;
import models.Equipe;
import models.Match;
import models.ResultatMatch;
import services.*;

import java.awt.Desktop;
import java.net.URI;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MatchesController {

    // ── Stats ──────────────────────────────────────────────────────────────
    @FXML private Label statTotalVal;
    @FXML private Label statLiveVal;
    @FXML private Label statUpcomingVal;
    @FXML private Label statDoneVal;
    @FXML private Label statWinRateVal;

    // ── Filter / Search ────────────────────────────────────────────────────
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> sortCombo;

    // ── Match List ─────────────────────────────────────────────────────────
    @FXML private VBox matchListBox;
    @FXML private VBox proLiveBox;
    @FXML private HBox matchGameBar;
    @FXML private VBox upcomingPreviewBox;
    @FXML private VBox recentPreviewBox;
    @FXML private VBox loadingOverlay;
    @FXML private ImageView loadingLogo;

    // ── Form ───────────────────────────────────────────────────────────────
    @FXML private Label formTitle;
    @FXML private ComboBox<String> team1Combo;
    @FXML private ComboBox<String> team2Combo;
    @FXML private ComboBox<String> gameCombo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> statutCombo;
    @FXML private TextField tournoiField;
    @FXML private TextField recompenseField;
    @FXML private Label formError;
    @FXML private Button btnSave;

    // ── Analytics ──────────────────────────────────────────────────────────
    @FXML private Label analyticsSectionTitle;
    @FXML private Label analyticsGameInsight;
    @FXML private Label analyticsWins;
    @FXML private Label analyticsLosses;
    @FXML private Label analyticsTotal;
    @FXML private ProgressBar winRateBar;
    @FXML private Label winRateLabel;
    @FXML private VBox analyticsBreakdown;

    @FXML private HBox userBarButton;
    @FXML private VBox userFlyoutPanel;
    @FXML private Label userBarChevron;

    // ── Services ───────────────────────────────────────────────────────────
    private final MatchService         matchService    = new MatchService();
    private final ResultatMatchService resultatService = new ResultatMatchService();
    private final PandaScoreService    pandaScore      = new PandaScoreService();

    // ── State ──────────────────────────────────────────────────────────────
    private List<Equipe>  allEquipes   = new ArrayList<>();
    private List<Match>   allMatches   = new ArrayList<>();

    // FIX 1: Store pro matches so they survive filter/search refreshes
    private List<PandaScoreService.ProMatch> proLive     = new ArrayList<>();
    private List<PandaScoreService.ProMatch> proUpcoming = new ArrayList<>();
    private List<PandaScoreService.ProMatch> proPast     = new ArrayList<>();
    private boolean proMatchesLoaded = false;

    private Match  editingMatch = null;
    private String selectedGameId = "Valorant";
    private TranslateTransition logoFloat;
    private boolean userPanelOpen;

    private static final class GameBtnMeta {
        private final String id;
        private final String label;
        private final String dotColor;
        private final String genre;

        GameBtnMeta(String id, String label, String dotColor, String genre) {
            this.id = id;
            this.label = label;
            this.dotColor = dotColor;
            this.genre = genre;
        }

        String getId() { return id; }
        String getLabel() { return label; }
        String getDotColor() { return dotColor; }
        String getGenre() { return genre; }
    }

    private static final GameBtnMeta[] MATCH_GAMES = {
            new GameBtnMeta("Valorant", "Valorant", "#E04444", "FPS"),
            new GameBtnMeta("CS2", "CS2", "#e6ff25", "FPS"),
            new GameBtnMeta("LoL", "League of Legends", "#0099ff", "MOBA"),
            new GameBtnMeta("Dota2", "Dota 2", "#fb00ff", "MOBA"),
            new GameBtnMeta("R6 Siege", "R6 Siege", "#08ff15", "Tactical"),
            new GameBtnMeta("Rocket League", "Rocket League", "#00D4D4", "Sports"),
    };

    // ─────────────────────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        selectedGameId = "Valorant";

        try {
            EquipeService equipeService = new EquipeService();
            allEquipes = equipeService.getAll();
        } catch (Exception e) {
            System.out.println("⚠️ Could not load teams: " + e.getMessage());
        }

        List<String> teamNames = allEquipes.stream()
                .map(eq -> eq.getId() + " - " + eq.getNom())
                .collect(Collectors.toList());
        team1Combo.setItems(FXCollections.observableArrayList(teamNames));
        team2Combo.setItems(FXCollections.observableArrayList(teamNames));

        List<String> gameIds = java.util.Arrays.stream(MATCH_GAMES).map(GameBtnMeta::getId).collect(Collectors.toList());
        gameCombo.setItems(FXCollections.observableArrayList(gameIds));
        gameCombo.setValue(selectedGameId);

        statusFilter.setItems(FXCollections.observableArrayList(
                "All", "planifie", "en_cours", "termine", "annule"));
        statusFilter.setValue("All");

        statutCombo.setItems(FXCollections.observableArrayList(
                "planifie", "en_cours", "termine", "annule"));

        sortCombo.setItems(FXCollections.observableArrayList(
                "Date (newest)", "Date (oldest)", "Status", "Team name"));
        sortCombo.setValue("Date (newest)");

        buildMatchGameBar();
        setupLoadingLogoAnimation();

        loadAllData();
        loadProMatchesAsync();
    }

    private void buildMatchGameBar() {
        matchGameBar.getChildren().clear();
        for (GameBtnMeta g : MATCH_GAMES) {
            Button b = new Button();
            b.setUserData(g.getId());
            b.setPrefHeight(52);
            b.setPrefWidth(158);
            b.setOnAction(this::onMatchGameBarClicked);
            styleGameBarButton(b, g.getId().equals(selectedGameId), g);
            matchGameBar.getChildren().add(b);
        }
    }

    private void styleGameBarButton(Button btn, boolean active, GameBtnMeta g) {
        String border = active ? "-fx-border-color: " + g.getDotColor() + "; -fx-border-radius: 10; -fx-border-width: 1.2;" : "";
        btn.setStyle("-fx-background-color: #1C1C29; -fx-background-radius: 10; -fx-cursor: hand; " + border);
        VBox content = new VBox(3);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setStyle("-fx-padding: 0 0 0 10;");
        HBox nameRow = new HBox(5);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: " + g.getDotColor() + "; -fx-font-size: 9px;");
        Label name = new Label(g.getLabel());
        name.setStyle("-fx-text-fill: " + (active ? "white" : "#949499")
                + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        nameRow.getChildren().addAll(dot, name);
        Label genre = new Label(g.getGenre());
        genre.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #777;"
                + "-fx-font-size: 8px; -fx-background-radius: 4; -fx-padding: 2 5;");
        content.getChildren().addAll(nameRow, genre);
        btn.setGraphic(content);
        attachButtonMicroHover(btn);
    }

    private void onMatchGameBarClicked(ActionEvent e) {
        Button clicked = (Button) e.getSource();
        String id = (String) clicked.getUserData();
        if (id == null || id.equals(selectedGameId)) return;
        selectedGameId = id;
        gameCombo.setValue(id);
        proLive = new ArrayList<>();
        proUpcoming = new ArrayList<>();
        proPast = new ArrayList<>();
        for (Node n : matchGameBar.getChildren()) {
            if (n instanceof Button b) {
                String bid = (String) b.getUserData();
                GameBtnMeta meta = java.util.Arrays.stream(MATCH_GAMES)
                        .filter(x -> x.getId().equals(bid)).findFirst().orElse(MATCH_GAMES[0]);
                styleGameBarButton(b, bid.equals(selectedGameId), meta);
            }
        }
        proMatchesLoaded = false;
        showLoadingOverlay(true);
        refreshUI();
        loadProMatchesAsync();
    }

    private void setupLoadingLogoAnimation() {
        java.net.URL res = getClass().getResource("/logoteam.png");
        if (res != null) {
            loadingLogo.setImage(new Image(res.toExternalForm(), true));
        }
        logoFloat = new TranslateTransition(Duration.millis(850), loadingLogo);
        logoFloat.setFromY(0);
        logoFloat.setToY(-16);
        logoFloat.setAutoReverse(true);
        logoFloat.setCycleCount(Animation.INDEFINITE);
    }

    private void showLoadingOverlay(boolean show) {
        loadingOverlay.setVisible(show);
        loadingOverlay.setManaged(show);
        if (show) {
            if (logoFloat != null) logoFloat.play();
        } else {
            if (logoFloat != null) logoFloat.stop();
            loadingLogo.setTranslateY(0);
        }
    }

    private boolean localMatchForSelectedGame(Match m) {
        String j = m.getJeu();
        if (j == null || j.isBlank() || j.equalsIgnoreCase(selectedGameId)) return true;
        return "StarCraft 2".equalsIgnoreCase(j) && "Rocket League".equals(selectedGameId);
    }

    // ─────────────────────────────────────────────────────────────────────
    // LOCAL DATA
    // ─────────────────────────────────────────────────────────────────────
    private void loadAllData() {
        try { allMatches = matchService.getAll(); }
        catch (Exception e) { allMatches = new ArrayList<>(); }
        refreshUI();
    }

    // FIX 2: main list = local + pro live only; upcoming/recent in bottom panes
    private void refreshUI() {
        matchListBox.getChildren().clear();
        if (proLiveBox != null) {
            proLiveBox.getChildren().clear();
        }

        List<Match> filtered = applyLocalFilters();
        if (filtered.isEmpty()) {
            Label empty = new Label("No local matches for this game. Create one and pick its game below.");
            empty.setWrapText(true);
            empty.setStyle("-fx-text-fill: #555; -fx-font-size: 13px; -fx-padding: 10 0;");
            matchListBox.getChildren().add(empty);
        } else {
            int i = 0;
            for (Match m : filtered) {
                Node card = buildHeroLocalCard(m);
                matchListBox.getChildren().add(card);
                animateCardIn(card, i++);
            }
        }

        if (proMatchesLoaded) {
            appendFilteredProLiveOnly();
        }

        List<Match> localScoped = allMatches.stream()
                .filter(this::localMatchForSelectedGame)
                .collect(Collectors.toList());

        updateStats(localScoped, proLive, proUpcoming, proPast);
        updateAnalytics(localScoped, proLive, proUpcoming, proPast);
        refreshPreviewColumns();
    }

    // ─────────────────────────────────────────────────────────────────────
    // PANDASCORE — async load per selected game
    // ─────────────────────────────────────────────────────────────────────
    private void loadProMatchesAsync() {
        showLoadingOverlay(true);
        final String slug = PandaScoreService.videogameSlugForAppGame(selectedGameId);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<PandaScoreService.ProMatch> live = pandaScore.getLiveByGame(slug);
                List<PandaScoreService.ProMatch> upcoming = pandaScore.getUpcomingByGame(slug, 12);
                List<PandaScoreService.ProMatch> past = pandaScore.getPastByGame(slug, 12);
                Platform.runLater(() -> {
                    proLive = live != null ? live : new ArrayList<>();
                    proUpcoming = upcoming != null ? upcoming : new ArrayList<>();
                    proPast = past != null ? past : new ArrayList<>();
                    proMatchesLoaded = true;
                    showLoadingOverlay(false);
                    refreshUI();
                });
                return null;
            }
        };
        task.setOnFailed(ev -> Platform.runLater(() -> {
            proLive = new ArrayList<>();
            proUpcoming = new ArrayList<>();
            proPast = new ArrayList<>();
            proMatchesLoaded = true;
            showLoadingOverlay(false);
            refreshUI();
        }));
        new Thread(task, "pandascore-matches").start();
    }

    /** Pro live cards in the scroll list (tab + search apply). */
    private void appendFilteredProLiveOnly() {
        String search = searchField != null && searchField.getText() != null
                ? searchField.getText().toLowerCase().trim() : "";
        List<PandaScoreService.ProMatch> filteredLive = filterProList(proLive, search);

        if (proLiveBox == null) return;
        if (filteredLive.isEmpty()) {
            Label empty = new Label("No live pro matches for this title right now.");
            empty.setStyle("-fx-text-fill: #555; -fx-font-size: 11px; -fx-padding: 4 0 0 0;");
            proLiveBox.getChildren().add(empty);
            return;
        }

        int idx = 0;
        for (PandaScoreService.ProMatch m : filteredLive) {
            Node card = buildHeroProCard(m);
            proLiveBox.getChildren().add(card);
            animateCardIn(card, idx++);
        }
    }

    private void refreshPreviewColumns() {
        upcomingPreviewBox.getChildren().clear();
        recentPreviewBox.getChildren().clear();
        if (!proMatchesLoaded) {
            Label w = new Label("Loading…");
            w.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");
            upcomingPreviewBox.getChildren().add(w);
            recentPreviewBox.getChildren().add(new Label(""));
            return;
        }
        String search = searchField != null && searchField.getText() != null
                ? searchField.getText().toLowerCase().trim() : "";
        List<PandaScoreService.ProMatch> up = filterProBySearch(proUpcoming, search);
        List<PandaScoreService.ProMatch> pa = filterProBySearch(proPast, search);

        if (up.isEmpty()) {
            Label l = new Label("None scheduled.");
            l.setStyle("-fx-text-fill: #555; -fx-font-size: 10px;");
            upcomingPreviewBox.getChildren().add(l);
        } else {
            int n = Math.min(8, up.size());
            for (int i = 0; i < n; i++) {
                upcomingPreviewBox.getChildren().add(buildProPreviewRow(up.get(i), false));
            }
        }
        if (pa.isEmpty()) {
            Label l = new Label("No recent results.");
            l.setStyle("-fx-text-fill: #555; -fx-font-size: 10px;");
            recentPreviewBox.getChildren().add(l);
        } else {
            int n = Math.min(10, pa.size());
            for (int i = 0; i < n; i++) {
                recentPreviewBox.getChildren().add(buildProPreviewRow(pa.get(i), true));
            }
        }
    }

    private List<PandaScoreService.ProMatch> filterProBySearch(List<PandaScoreService.ProMatch> list, String search) {
        if (list == null) return new ArrayList<>();
        if (search.isEmpty()) return new ArrayList<>(list);
        return list.stream().filter(m ->
                (m.team1Name != null && m.team1Name.toLowerCase().contains(search)) ||
                        (m.team2Name != null && m.team2Name.toLowerCase().contains(search)) ||
                        (m.game != null && m.game.toLowerCase().contains(search)) ||
                        (m.tournament != null && m.tournament.toLowerCase().contains(search)) ||
                        (m.league != null && m.league.toLowerCase().contains(search))
        ).collect(Collectors.toList());
    }

    private HBox buildProPreviewRow(PandaScoreService.ProMatch m, boolean finished) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #131428; -fx-background-radius: 8; -fx-padding: 6 8;");
        String left = truncate(m.team1Name, 14) + " vs " + truncate(m.team2Name, 14);
        Label main = new Label(left);
        main.setStyle("-fx-text-fill: #CCC; -fx-font-size: 10px;");
        HBox.setHgrow(main, Priority.ALWAYS);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label right = new Label();
        if (finished) {
            right.setText(m.score1 + "–" + m.score2);
            right.setStyle("-fx-text-fill: #1DB876; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            String ds = m.scheduledAt == null || m.scheduledAt.isEmpty() ? "TBD"
                    : m.scheduledAt.replace("T", " ").replace("Z", "").substring(0, Math.min(16, m.scheduledAt.length()));
            right.setText(ds);
            right.setStyle("-fx-text-fill: #888; -fx-font-size: 9px;");
        }
        row.getChildren().addAll(main, sp, right);
        if (m.streamUrl != null && !m.streamUrl.isEmpty() && !finished) {
            Button b = new Button("▶");
            b.setStyle("-fx-background-color: rgba(145,70,255,0.2); -fx-text-fill: #9146FF; -fx-font-size: 9px; -fx-cursor: hand; -fx-padding: 2 6;");
            b.setOnAction(ev -> {
                try { Desktop.getDesktop().browse(new URI(m.streamUrl)); }
                catch (Exception ex) { System.out.println("Open stream: " + ex.getMessage()); }
            });
            row.getChildren().add(b);
        }
        attachPreviewRowHover(row);
        return row;
    }

    private List<PandaScoreService.ProMatch> filterProList(
            List<PandaScoreService.ProMatch> list,
            String search) {
        if (list == null) return new ArrayList<>();
        if (search == null || search.isBlank()) return new ArrayList<>(list);
        String s = search.toLowerCase().trim();
        return list.stream().filter(m ->
                (m.team1Name != null && m.team1Name.toLowerCase().contains(s)) ||
                        (m.team2Name != null && m.team2Name.toLowerCase().contains(s)) ||
                        (m.game != null && m.game.toLowerCase().contains(s)) ||
                        (m.tournament != null && m.tournament.toLowerCase().contains(s)) ||
                        (m.league != null && m.league.toLowerCase().contains(s))
        ).collect(Collectors.toList());
    }

    /** Left accent + right hero strip (game art = gradient by selected title). */
    private HBox buildHeroProCard(PandaScoreService.ProMatch m) {
        String[] pal = gameHeroPalette(selectedGameId);
        HBox card = new HBox(0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(118);
        card.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(10);
        left.setPadding(new Insets(16, 18, 16, 20));
        HBox.setHgrow(left, Priority.ALWAYS);
        left.setStyle("-fx-background-radius: 18 0 0 18; "
                + "-fx-background-color: linear-gradient(to bottom right, " + pal[0] + "CC, " + pal[1] + "EE);");

        String[] badge = getBadgeStyle(m.status);
        Label badgeLbl = new Label(badge[0]);
        badgeLbl.setStyle("-fx-background-color: " + badge[1] + "; -fx-text-fill: " + badge[2]
                + "; -fx-font-size: 9px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 4 10;");

        Label title = new Label(truncate(m.team1Name, 24) + "  vs  " + truncate(m.team2Name, 24));
        title.setWrapText(true);
        title.setStyle("-fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: bold;");

        String sched = (m.scheduledAt == null || m.scheduledAt.isEmpty()) ? "Schedule TBD"
                : m.scheduledAt.replace("T", " ").replace("Z", "");
        if (sched.length() > 40) sched = sched.substring(0, 40) + "…";
        String league = m.league != null ? m.league : "";
        String tourn = m.tournament != null ? m.tournament : "";
        String desc = (!league.isEmpty() ? league + " · " : "")
                + (!tourn.isEmpty() ? truncate(tourn, 44) : "Pro match")
                + " · " + sched;
        Label sub = new Label(desc);
        sub.setWrapText(true);
        sub.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 11px;");

        HBox actions = new HBox(10);
        Button watch = new Button("📺 Watch");
        watch.setStyle("-fx-background-color: rgba(255,255,255,0.92); -fx-text-fill: #111; "
                + "-fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 18; -fx-cursor: hand;");
        if (m.streamUrl != null && !m.streamUrl.isEmpty()) {
            watch.setOnAction(e -> {
                try { Desktop.getDesktop().browse(new URI(m.streamUrl)); }
                catch (Exception ex) { System.out.println("Open stream: " + ex.getMessage()); }
            });
        } else {
            watch.setDisable(true);
            watch.setOpacity(0.45);
        }
        Button info = new Button("PandaScore");
        info.setStyle("-fx-background-color: transparent; -fx-text-fill: white; "
                + "-fx-border-color: rgba(255,255,255,0.55); -fx-border-radius: 20; -fx-background-radius: 20; "
                + "-fx-padding: 8 16; -fx-cursor: hand;");
        info.setOnAction(e -> {
            try { Desktop.getDesktop().browse(new URI("https://pandascore.co/matches/" + m.id)); }
            catch (Exception ex) { System.out.println("Open match: " + ex.getMessage()); }
        });
        actions.getChildren().addAll(watch, info);
        attachButtonMicroHover(watch);
        attachButtonMicroHover(info);

        left.getChildren().addAll(badgeLbl, title, sub, actions);

        VBox right = new VBox(6);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.setPadding(new Insets(12, 18, 12, 12));
        right.setMinWidth(196);
        right.setPrefWidth(228);
        right.setMaxWidth(260);
        right.setStyle("-fx-background-radius: 0 18 18 0; "
                + "-fx-background-color: linear-gradient(to left, #080810, " + pal[1] + ");");

        if ("running".equals(m.status) || "finished".equals(m.status)) {
            Label score = new Label(m.score1 + " : " + m.score2);
            score.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
            right.getChildren().add(score);
        }

        Label mark = new Label(pal[3]);
        mark.setStyle("-fx-text-fill: rgba(255,255,255,0.05); -fx-font-size: 46px; -fx-font-weight: bold;");
        right.getChildren().add(mark);

        HBox logos = new HBox(-14);
        logos.setAlignment(Pos.CENTER_RIGHT);
        try {
            if (m.team1Logo != null && !m.team1Logo.isEmpty()) {
                ImageView i1 = new ImageView(new Image(m.team1Logo, 72, 72, true, true, true));
                i1.setFitHeight(72);
                i1.setFitWidth(72);
                logos.getChildren().add(i1);
            }
            if (m.team2Logo != null && !m.team2Logo.isEmpty()) {
                ImageView i2 = new ImageView(new Image(m.team2Logo, 72, 72, true, true, true));
                i2.setFitHeight(72);
                i2.setFitWidth(72);
                logos.getChildren().add(i2);
            }
        } catch (Exception ignored) { }
        right.getChildren().add(logos);

        card.getChildren().addAll(left, right);
        card.setStyle("-fx-background-radius: 18; -fx-border-radius: 18; "
                + "-fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 1; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.45), 22, 0, 0, 8);");
        attachCardHover(card);
        return card;
    }

    private String[] getBadgeStyle(String status) {
        return switch (status == null ? "" : status) {
            case "running"     -> new String[]{"●LIVE", "rgba(242,51,51,0.18)",   "#F23333"};
            case "not_started" -> new String[]{"SOON",  "rgba(255,192,26,0.18)",  "#FFC01A"};
            default            -> new String[]{"DONE",  "rgba(29,184,118,0.18)",  "#1DB876"};
        };
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    /** [vibrantLeft, darkBase, accentDot, watermark] */
    private String[] gameHeroPalette(String gameId) {
        return switch (gameId != null ? gameId : "") {
            case "Valorant" -> new String[]{"#6B2FD4", "#140a22", "#E04444", "VALO"};
            case "CS2" -> new String[]{"#5a6510", "#121407", "#e6ff25", "CS2"};
            case "LoL" -> new String[]{"#104878", "#081018", "#0099ff", "LOL"};
            case "Dota2" -> new String[]{"#601860", "#100810", "#fb00ff", "DOTA"};
            case "R6 Siege" -> new String[]{"#0d5c24", "#071207", "#08ff15", "R6"};
            case "Rocket League" -> new String[]{"#006060", "#051414", "#00D4D4", "RL"};
            default -> new String[]{"#5B2D8C", "#1a0d2e", "#E04444", "GAME"};
        };
    }

    private void animateCardIn(Node node, int index) {
        node.setOpacity(0);
        node.setTranslateY(14);
        FadeTransition fade = new FadeTransition(Duration.millis(260), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition move = new TranslateTransition(Duration.millis(280), node);
        move.setFromY(14);
        move.setToY(0);
        ParallelTransition pt = new ParallelTransition(fade, move);
        pt.setDelay(Duration.millis(index * 42L));
        pt.play();
    }

    private void attachCardHover(HBox card) {
        ScaleTransition in = new ScaleTransition(Duration.millis(180), card);
        in.setToX(1.012);
        in.setToY(1.025);
        ScaleTransition out = new ScaleTransition(Duration.millis(180), card);
        out.setToX(1);
        out.setToY(1);
        card.setOnMouseEntered(e -> in.playFromStart());
        card.setOnMouseExited(e -> out.playFromStart());
    }

    private void attachPreviewRowHover(HBox row) {
        ScaleTransition in = new ScaleTransition(Duration.millis(140), row);
        in.setToX(1.01);
        in.setToY(1.03);
        ScaleTransition out = new ScaleTransition(Duration.millis(140), row);
        out.setToX(1);
        out.setToY(1);
        row.setOnMouseEntered(e -> in.playFromStart());
        row.setOnMouseExited(e -> out.playFromStart());
    }

    private void attachButtonMicroHover(Button btn) {
        ScaleTransition in = new ScaleTransition(Duration.millis(120), btn);
        in.setToX(1.04);
        in.setToY(1.04);
        ScaleTransition out = new ScaleTransition(Duration.millis(120), btn);
        out.setToX(1);
        out.setToY(1);
        btn.setOnMouseEntered(e -> in.playFromStart());
        btn.setOnMouseExited(e -> out.playFromStart());
    }

    // ─────────────────────────────────────────────────────────────────────
    // LOCAL MATCH FILTERING
    // ─────────────────────────────────────────────────────────────────────
    private List<Match> applyLocalFilters() {
        List<Match> scoped = allMatches.stream()
                .filter(this::localMatchForSelectedGame)
                .collect(Collectors.toList());

        String search = searchField != null && searchField.getText() != null
                ? searchField.getText().toLowerCase().trim() : "";

        List<Match> result = scoped.stream().filter(m -> {
            String statusVal = statusFilter != null ? statusFilter.getValue() : "All";
            boolean statusOk = statusVal == null || "All".equals(statusVal)
                    || statusVal.equalsIgnoreCase(m.getStatut());
            boolean searchOk = search.isEmpty()
                    || getTeamName(m.getEquipe1Id()).toLowerCase().contains(search)
                    || getTeamName(m.getEquipe2Id()).toLowerCase().contains(search)
                    || (m.getStatut()    != null && m.getStatut().toLowerCase().contains(search))
                    || (m.getRecompense() != null && m.getRecompense().toLowerCase().contains(search));
            return statusOk && searchOk;
        }).collect(Collectors.toList());

        String sort = sortCombo != null ? sortCombo.getValue() : "Date (newest)";
        if (sort != null) {
            switch (sort) {
                case "Date (oldest)" -> result.sort((a, b) ->
                        a.getDateMatch() == null ? 1 : b.getDateMatch() == null ? -1 :
                                a.getDateMatch().compareTo(b.getDateMatch()));
                case "Status" -> result.sort((a, b) ->
                        a.getStatut() == null ? 1 : a.getStatut().compareTo(
                                b.getStatut() == null ? "" : b.getStatut()));
                default -> result.sort((a, b) ->
                        a.getDateMatch() == null ? 1 : b.getDateMatch() == null ? -1 :
                                b.getDateMatch().compareTo(a.getDateMatch()));
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────
    // LOCAL MATCH CARD (hero layout — palette follows match game tag)
    // ─────────────────────────────────────────────────────────────────────
    private HBox buildHeroLocalCard(Match m) {
        String gid = m.getJeu() != null && !m.getJeu().isBlank() ? m.getJeu() : selectedGameId;
        String[] pal = gameHeroPalette(gid);
        HBox card = new HBox(0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(108);
        card.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(8);
        left.setPadding(new Insets(14, 16, 14, 18));
        HBox.setHgrow(left, Priority.ALWAYS);
        left.setStyle("-fx-background-radius: 16 0 0 16; "
                + "-fx-background-color: linear-gradient(to bottom right, " + pal[0] + "CC, " + pal[1] + "EE);");

        String statLower = m.getStatut() == null ? "" : m.getStatut().toLowerCase();
        final String[] badge;
        if ("en_cours".equals(statLower)) {
            badge = new String[]{"● LIVE", "rgba(242,51,51,0.22)", "#F23333"};
        } else if ("planifie".equals(statLower)) {
            badge = new String[]{"SOON", "rgba(255,192,26,0.22)", "#FFC01A"};
        } else if ("termine".equals(statLower)) {
            badge = new String[]{"DONE", "rgba(29,184,118,0.22)", "#1DB876"};
        } else {
            badge = new String[]{"—", "rgba(100,100,120,0.22)", "#AAA"};
        }
        Label badgeLbl = new Label(badge[0]);
        badgeLbl.setStyle("-fx-background-color: " + badge[1] + "; -fx-text-fill: " + badge[2]
                + "; -fx-font-size: 9px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 4 10;");

        Label title = new Label(truncate(getTeamName(m.getEquipe1Id()), 22) + "  vs  "
                + truncate(getTeamName(m.getEquipe2Id()), 22));
        title.setWrapText(true);
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        String when = m.getDateMatch() != null ? m.getDateMatch().toString() : "TBD";
        Label sub = new Label(getGameMeta(gid).getLabel() + " · " + when);
        sub.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 11px;");

        HBox actions = new HBox(10);
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-text-fill: #111; "
                + "-fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 7 16; -fx-cursor: hand;");
        editBtn.setOnAction(e -> loadMatchIntoForm(m));
        Button delBtn = new Button("Delete");
        delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FFB4B4; "
                + "-fx-border-color: rgba(255,180,180,0.45); -fx-border-radius: 18; -fx-background-radius: 18; "
                + "-fx-padding: 7 14; -fx-cursor: hand;");
        delBtn.setOnAction(e -> deleteMatch(m));
        actions.getChildren().addAll(editBtn, delBtn);
        attachButtonMicroHover(editBtn);
        attachButtonMicroHover(delBtn);

        left.getChildren().addAll(badgeLbl, title, sub, actions);

        VBox right = new VBox(8);
        right.setAlignment(Pos.CENTER);
        right.setMinWidth(120);
        right.setPrefWidth(140);
        right.setPadding(new Insets(10, 14, 10, 10));
        right.setStyle("-fx-background-radius: 0 16 16 0; "
                + "-fx-background-color: linear-gradient(to left, #07070d, " + pal[1] + ");");

        ResultatMatch res = null;
        try { res = resultatService.getByMatchId(m.getId()); } catch (Exception ignored) { }
        Label score = new Label(res != null ? res.getScoreEquipe1() + " : " + res.getScoreEquipe2() : "— : —");
        score.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label mark = new Label(pal[3]);
        mark.setStyle("-fx-text-fill: rgba(255,255,255,0.06); -fx-font-size: 40px; -fx-font-weight: bold;");
        right.getChildren().addAll(score, mark);

        card.getChildren().addAll(left, right);
        card.setStyle("-fx-background-radius: 16; -fx-border-radius: 16; "
                + "-fx-border-color: rgba(255,255,255,0.07); -fx-border-width: 1; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 18, 0, 0, 6);");
        attachCardHover(card);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────
    // STATS — FIX 6: include pro match counts
    // ─────────────────────────────────────────────────────────────────────
    private void updateStats(List<Match> local,
                             List<PandaScoreService.ProMatch> live,
                             List<PandaScoreService.ProMatch> upcoming,
                             List<PandaScoreService.ProMatch> past) {

        long localTotal    = local.size();
        long localLive     = local.stream().filter(m -> "en_cours".equalsIgnoreCase(m.getStatut())).count();
        long localUpcoming = local.stream().filter(m -> "planifie".equalsIgnoreCase(m.getStatut())).count();
        long localDone     = local.stream().filter(m -> "termine".equalsIgnoreCase(m.getStatut())).count();

        long proLiveCount     = live.size();
        long proUpcomingCount = upcoming.size();
        long proDoneCount     = past.size();

        statTotalVal.setText(String.valueOf(localTotal + proLiveCount + proUpcomingCount + proDoneCount));
        statLiveVal.setText(String.valueOf(localLive + proLiveCount));
        statUpcomingVal.setText(String.valueOf(localUpcoming + proUpcomingCount));
        statDoneVal.setText(String.valueOf(localDone + proDoneCount));

        long wins = 0, totalWithResult = 0;
        for (Match m : local) {
            if ("termine".equalsIgnoreCase(m.getStatut())) {
                try {
                    ResultatMatch r = resultatService.getByMatchId(m.getId());
                    if (r != null) { totalWithResult++; wins++; }
                } catch (Exception ignored) {}
            }
        }
        int wr = totalWithResult > 0 ? (int) (wins * 100 / totalWithResult) : 0;
        statWinRateVal.setText(wr + "%");
    }

    // ─────────────────────────────────────────────────────────────────────
    // ANALYTICS & HISTORY (local only — no change needed)
    // ─────────────────────────────────────────────────────────────────────
    private void updateAnalytics(List<Match> matches,
                                 List<PandaScoreService.ProMatch> proLiveL,
                                 List<PandaScoreService.ProMatch> proUpcomingL,
                                 List<PandaScoreService.ProMatch> proPastL) {
        GameBtnMeta meta = getGameMeta(selectedGameId);
        if (analyticsSectionTitle != null) {
            analyticsSectionTitle.setText("Season — " + meta.getLabel());
            analyticsSectionTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + meta.getDotColor() + ";");
        }

        long winsWithResult = 0, lossesWithResult = 0, totalWithResult = 0;
        for (Match m : matches) {
            if ("termine".equalsIgnoreCase(m.getStatut())) {
                try {
                    ResultatMatch r = resultatService.getByMatchId(m.getId());
                    if (r != null) {
                        totalWithResult++;
                        if (r.getGagnantId() > 0) winsWithResult++; else lossesWithResult++;
                    }
                } catch (Exception ignored) {}
            }
        }
        analyticsWins.setText(winsWithResult + "W");
        analyticsLosses.setText(lossesWithResult + "L");
        int pl = proLiveL != null ? proLiveL.size() : 0;
        int pu = proUpcomingL != null ? proUpcomingL.size() : 0;
        int pp = proPastL != null ? proPastL.size() : 0;
        long proFinished = proPastL != null
                ? proPastL.stream().filter(pm -> "finished".equals(pm.status)).count() : 0;
        int proN = pl + pu + pp;
        analyticsTotal.setText(String.valueOf(matches.size() + proN));
        double wr = totalWithResult > 0 ? (double) winsWithResult / totalWithResult : 0;
        winRateBar.setProgress(wr);
        winRateLabel.setText(String.format("%.0f%% local wins (finished w/ winner) · %d decided", wr * 100, totalWithResult));

        if (analyticsGameInsight != null) {
            analyticsGameInsight.setText(meta.getGenre() + " · PandaScore for " + meta.getLabel()
                    + ": " + pl + " live, " + pu + " upcoming, " + proFinished + " finished in feed.");
        }

        analyticsBreakdown.getChildren().clear();
        long total = matches.size();
        long live = matches.stream().filter(m -> "en_cours".equalsIgnoreCase(m.getStatut())).count();
        long upcoming2 = matches.stream().filter(m -> "planifie".equalsIgnoreCase(m.getStatut())).count();
        long done = matches.stream().filter(m -> "termine".equalsIgnoreCase(m.getStatut())).count();
        long cancelled = matches.stream().filter(m -> "annule".equalsIgnoreCase(m.getStatut())).count();
        long denom = Math.max(1, total);
        String gl = meta.getLabel();
        analyticsBreakdown.getChildren().addAll(
                buildBreakdownRow(gl + " · your live", live, denom, meta.getDotColor()),
                buildBreakdownRow(gl + " · your upcoming", upcoming2, denom, "#FFC01A"),
                buildBreakdownRow(gl + " · your completed", done, denom, "#1DB876"),
                buildBreakdownRow(gl + " · cancelled", cancelled, denom, "#555"));
        int proSum = Math.max(1, proN);
        if (proN > 0) {
            Separator sep = new Separator();
            sep.setStyle("-fx-background-color: #252540;");
            analyticsBreakdown.getChildren().add(sep);
            Label api = new Label("PandaScore — " + gl);
            api.setStyle("-fx-text-fill: #8F3FFF; -fx-font-size: 11px; -fx-font-weight: bold;");
            analyticsBreakdown.getChildren().add(api);
            analyticsBreakdown.getChildren().addAll(
                    buildBreakdownRow("Pro live", pl, proSum, "#F23333"),
                    buildBreakdownRow("Pro upcoming", pu, proSum, "#FFC01A"),
                    buildBreakdownRow("Pro in feed", pp, proSum, "#1DB876"));
        }
    }

    private VBox buildBreakdownRow(String label, long count, long total, String color) {
        VBox row = new VBox(3);
        HBox header = new HBox();
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #949499; -fx-font-size: 11px;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label cnt = new Label(count + " / " + total);
        cnt.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        header.getChildren().addAll(lbl, sp, cnt);
        ProgressBar pb = new ProgressBar(total > 0 ? (double) count / total : 0);
        pb.setPrefWidth(358);
        pb.setStyle("-fx-accent: " + color + "; -fx-pref-height: 5;");
        row.getChildren().addAll(header, pb);
        return row;
    }

    private GameBtnMeta getGameMeta(String gameId) {
        return java.util.Arrays.stream(MATCH_GAMES)
                .filter(g -> g.getId().equals(gameId))
                .findFirst()
                .orElse(MATCH_GAMES[0]);
    }

    private void loadMatchIntoForm(Match m) {
        editingMatch = m;
        formTitle.setText("Edit Match #" + m.getId());
        btnSave.setText("Update Match");
        formError.setText("");
        for (String item : team1Combo.getItems())
            if (item.startsWith(m.getEquipe1Id() + " - ")) { team1Combo.setValue(item); break; }
        for (String item : team2Combo.getItems())
            if (item.startsWith(m.getEquipe2Id() + " - ")) { team2Combo.setValue(item); break; }
        if (m.getDateMatch() != null) datePicker.setValue(m.getDateMatch().toLocalDate());
        statutCombo.setValue(m.getStatut());
        tournoiField.setText(String.valueOf(m.getTournoiId()));
        recompenseField.setText(m.getRecompense() != null ? m.getRecompense() : "");
        String gj = m.getJeu();
        gameCombo.setValue(gj != null && !gj.isBlank() ? gj : selectedGameId);
    }

    @FXML public void clearForm() {
        editingMatch = null;
        formTitle.setText("New Match");
        btnSave.setText("Save Match");
        formError.setText("");
        team1Combo.setValue(null); team2Combo.setValue(null);
        datePicker.setValue(null); statutCombo.setValue(null);
        tournoiField.clear(); recompenseField.clear();
        gameCombo.setValue(selectedGameId);
    }

    @FXML public void saveMatch() {
        formError.setText("");
        if (team1Combo.getValue() == null || team2Combo.getValue() == null) {
            formError.setText("Please select both teams."); return;
        }
        if (team1Combo.getValue().equals(team2Combo.getValue())) {
            formError.setText("Team 1 and Team 2 cannot be the same."); return;
        }
        if (datePicker.getValue() == null) { formError.setText("Please select a match date."); return; }
        if (statutCombo.getValue() == null) { formError.setText("Please select a status."); return; }
        if (gameCombo.getValue() == null || gameCombo.getValue().isBlank()) {
            formError.setText("Please select a game for this match."); return;
        }

        int eq1 = Integer.parseInt(team1Combo.getValue().split(" - ")[0].trim());
        int eq2 = Integer.parseInt(team2Combo.getValue().split(" - ")[0].trim());
        Date date = Date.valueOf(datePicker.getValue());
        String statut = statutCombo.getValue();
        int tournoiId = 0;
        try { tournoiId = Integer.parseInt(tournoiField.getText().trim()); } catch (Exception ignored) {}
        String recompense = recompenseField.getText().trim();
        String jeuTag = gameCombo.getValue();

        try {
            if (editingMatch == null) {
                Match created = new Match(eq1, eq2, date, statut, tournoiId, recompense, (Timestamp) null);
                created.setJeu(jeuTag);
                matchService.add(created);
                showAlert("✅ Match created successfully!");
            } else {
                editingMatch.setEquipe1Id(eq1); editingMatch.setEquipe2Id(eq2);
                editingMatch.setDateMatch(date); editingMatch.setStatut(statut);
                editingMatch.setTournoiId(tournoiId); editingMatch.setRecompense(recompense);
                editingMatch.setJeu(jeuTag);
                matchService.update(editingMatch);
                showAlert("✅ Match updated successfully!");
            }
            clearForm();
            loadAllData();
        } catch (Exception e) { formError.setText("❌ Error: " + e.getMessage()); }
    }

    private void deleteMatch(Match m) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Match");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete: " + getTeamName(m.getEquipe1Id()) + " vs " + getTeamName(m.getEquipe2Id()) + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try { resultatService.delete(m.getId()); } catch (Exception ignored) {}
                try { matchService.delete(m.getId()); loadAllData(); }
                catch (Exception e) { showAlert("❌ Delete failed: " + e.getMessage()); }
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────
    // USER PANEL (Discord-style flyout)
    // ─────────────────────────────────────────────────────────────────────
    @FXML
    public void toggleUserPanel() {
        userPanelOpen = !userPanelOpen;
        if (userFlyoutPanel != null) {
            userFlyoutPanel.setVisible(userPanelOpen);
            userFlyoutPanel.setManaged(userPanelOpen);
            if (userPanelOpen) {
                userFlyoutPanel.setOpacity(0);
                FadeTransition ft = new FadeTransition(Duration.millis(160), userFlyoutPanel);
                ft.setFromValue(0);
                ft.setToValue(1);
                TranslateTransition tt = new TranslateTransition(Duration.millis(160), userFlyoutPanel);
                tt.setFromY(6);
                tt.setToY(0);
                new ParallelTransition(ft, tt).play();
            }
        }
        if (userBarChevron != null) {
            userBarChevron.setText(userPanelOpen ? "▼" : "▲");
        }
    }

    @FXML
    public void onUserProfile(ActionEvent event) {
        showAlert("Profile view can connect to your account screen when it is ready.");
    }

    @FXML
    public void onUserStatus(ActionEvent event) {
        showAlert("Custom status — hook to your profile service when available.");
    }

    @FXML
    public void logout(ActionEvent event) {
        showAlert("You have been logged out.");
    }

    @FXML public void onSearch()       { refreshUI(); }
    @FXML public void onStatusFilter() { refreshUI(); }
    @FXML public void onSort()         { refreshUI(); }

    // ─────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────
    private String getTeamName(int id) {
        return allEquipes.stream().filter(e -> e.getId() == id)
                .map(Equipe::getNom).findFirst().orElse("Team " + id);
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Match Center"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private void navigate(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, AppScene.WIDTH, AppScene.HEIGHT));
        } catch (Exception e) { System.out.println("❌ Could not load: " + fxml + " → " + e.getMessage()); }
    }

    @FXML public void navDashboard(ActionEvent e)   { navigate(e, "dashboard.fxml"); }
    @FXML public void navPlayers(ActionEvent e)     { navigate(e, "players.fxml"); }
    @FXML public void navMatches(ActionEvent e)     { navigate(e, "matches.fxml"); }
    @FXML public void navTeams(ActionEvent e)       { navigate(e, "teams.fxml"); }
    @FXML public void navTournaments(ActionEvent e) { navigate(e, "tournaments.fxml"); }
    @FXML public void navAnalytics(ActionEvent e)   { navigate(e, "analytics.fxml"); }
    @FXML public void navProduits(ActionEvent e)    { navigate(e, "produit.fxml"); }
    @FXML public void navSponsors(ActionEvent e)    { navigate(e, "sponsor.fxml"); }
    @FXML public void navSettings(ActionEvent e)    { navigate(e, "admin.fxml"); }
}