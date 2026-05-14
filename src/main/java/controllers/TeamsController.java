package controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Equipe;
import models.Match;
import models.ResultatMatch;
import services.*;
import utils.AppScene;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeamsController {

    // ── Grid / game bar ────────────────────────────────────────────────────
    @FXML private FlowPane  teamsGrid;
    @FXML private TextField searchField;
    @FXML private HBox      teamsGameBar;
    @FXML private StackPane loadingOverlay;
    @FXML private ImageView loadingLogo;

    // ── Pro team drawer ────────────────────────────────────────────────────
    @FXML private HBox   teamDrawerLayer;
    @FXML private Region teamDrawerBackdrop;
    @FXML private VBox   teamDetailDrawer;
    @FXML private VBox   teamDetailContent;

    // ── Right panel — local team form ──────────────────────────────────────
    @FXML private Label            formTitle;
    @FXML private TextField        teamNameField;
    @FXML private TextField        capitaineField;
    @FXML private TextField        nbMembresField;
    @FXML private ComboBox<String> teamGameCombo;
    @FXML private ComboBox<String> teamStyleCombo;
    @FXML private Label            formError;
    @FXML private Button           btnSaveTeam;

    // ── Stats ──────────────────────────────────────────────────────────────
    @FXML private Label statTotalTeams;
    @FXML private Label statLocalTeams;
    @FXML private Label statProTeams;

    // ── Services ───────────────────────────────────────────────────────────
    private final PandaScoreService    pandaScore      = new PandaScoreService();
    private final EquipeService        equipeService   = new EquipeService();
    private final MatchService         matchService    = new MatchService();
    private final ResultatMatchService resultatService = new ResultatMatchService();

    // ── State ──────────────────────────────────────────────────────────────
    private String selectedGameId = "Valorant";
    private TranslateTransition logoFloat;
    private volatile int teamDetailLoadToken;
    private List<PandaScoreService.ProTeam> proTeams   = new ArrayList<>();
    private List<Equipe>                    localTeams = new ArrayList<>();
    private Equipe editingTeam = null;

    // ── Card layout constants ──────────────────────────────────────────────
    private static final int    CARDS_PER_ROW = 4;
    private static final double CARD_WIDTH    = 248;
    private static final double CARD_GAP      = 14;

    // ── Game definitions ───────────────────────────────────────────────────
    private static final class GameBtnMeta {
        final String id, label, dotColor;
        GameBtnMeta(String id, String label, String dotColor) {
            this.id = id; this.label = label; this.dotColor = dotColor;
        }
    }

    private static final GameBtnMeta[] TEAM_GAMES = {
            new GameBtnMeta("Valorant",      "Valorant",          "#E04444"),
            new GameBtnMeta("CS2",           "CS2",               "#e6ff25"),
            new GameBtnMeta("LoL",           "League of Legends", "#0099ff"),
            new GameBtnMeta("Dota2",         "Dota 2",            "#fb00ff"),
            new GameBtnMeta("R6 Siege",      "R6 Siege",          "#08ff15"),
            new GameBtnMeta("Rocket League", "Rocket League",     "#00D4D4"),
    };

    private static final String[] PLAY_STYLES = {
            "Aggressive", "Defensive", "Balanced", "Support", "Rush", "Strategic"
    };

    // ─────────────────────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        selectedGameId = "Valorant";
        buildTeamsGameBar();
        setupLoadingLogoAnimation();

        List<String> gameIds = java.util.Arrays.stream(TEAM_GAMES)
                .map(g -> g.id).collect(Collectors.toList());
        teamGameCombo.setItems(FXCollections.observableArrayList(gameIds));
        teamGameCombo.setValue(selectedGameId);
        teamStyleCombo.setItems(FXCollections.observableArrayList(PLAY_STYLES));
        teamStyleCombo.setValue("Balanced");

        loadLocalTeams();
        loadProTeamsAsync();
    }

    // ─────────────────────────────────────────────────────────────────────
    // GRID RENDERING — VBox of rows, 4 cards per row
    // ─────────────────────────────────────────────────────────────────────
    private void loadLocalTeams() {
        try { localTeams = equipeService.getAll(); }
        catch (Exception e) { localTeams = new ArrayList<>(); }
        refreshGrid();
    }

    private void refreshGrid() {
        teamsGrid.getChildren().clear();
        String q = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase() : "";

        // ── PRO TEAMS ────────────────────────────────────────────────────
        List<PandaScoreService.ProTeam> filteredPro = proTeams.stream().filter(t -> {
            if (q.isEmpty()) return true;
            String n  = t.name    != null ? t.name.toLowerCase()    : "";
            String ac = t.acronym != null ? t.acronym.toLowerCase() : "";
            return n.contains(q) || ac.contains(q);
        }).collect(Collectors.toList());

        if (!filteredPro.isEmpty()) {
            // FIX 1: Section header — full-width, alone on its own row
            teamsGrid.getChildren().add(makeSectionHeader(
                    "🌍  WORLD TOP 10  ·  " + selectedGameId, "#388FFF"));

            // FIX 2: 4-cards-per-row layout
            renderCardsInRows(filteredPro.stream()
                    .map(t -> buildProTeamCard(filteredPro.indexOf(t) + 1, t))
                    .collect(Collectors.toList()));
        }

        // ── LOCAL TEAMS ───────────────────────────────────────────────────
        List<Equipe> filteredLocal = localTeams.stream().filter(e ->
                q.isEmpty() || (e.getNom() != null && e.getNom().toLowerCase().contains(q))
        ).collect(Collectors.toList());

        if (!filteredLocal.isEmpty()) {
            teamsGrid.getChildren().add(makeSectionHeader("📁  MY LOCAL TEAMS", "#388FFF"));

            renderCardsInRows(filteredLocal.stream()
                    .map(this::buildLocalTeamCard)
                    .collect(Collectors.toList()));
        }

        updateStats();
    }

    /** Renders a list of cards in rows of CARDS_PER_ROW inside the FlowPane */
    private void renderCardsInRows(List<Node> cards) {
        int animIdx = teamsGrid.getChildren().size();

        // Use an HBox row approach — add rows of 4 into the FlowPane
        // FlowPane wraps automatically; we enforce 4-per-row by setting card width
        // and prefWrapLength in FXML. Here we just add cards directly and let
        // the FlowPane handle wrapping, but we ensure each card is exactly CARD_WIDTH.
        for (int i = 0; i < cards.size(); i++) {
            Node card = cards.get(i);
            animateCardIn(card, animIdx++);
            teamsGrid.getChildren().add(card);
        }
    }

    /** Full-width section header — occupies entire row in FlowPane */
    private HBox makeSectionHeader(String text, String color) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 15px;" +
                "-fx-font-weight: bold; -fx-padding: 18 0 8 0;");

        HBox row = new HBox(lbl);
        row.setAlignment(Pos.CENTER_LEFT);
        // Must be wider than prefWrapLength to force a full-width "break"
        row.setPrefWidth(1200);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle("-fx-padding: 0;");
        return row;
    }

    private void updateStats() {
        if (statTotalTeams != null) statTotalTeams.setText(String.valueOf(localTeams.size() + proTeams.size()));
        if (statLocalTeams != null) statLocalTeams.setText(String.valueOf(localTeams.size()));
        if (statProTeams   != null) statProTeams.setText(String.valueOf(proTeams.size()));
    }

    // ─────────────────────────────────────────────────────────────────────
    // FIX 3: PRO TEAM CARD — darker, rank always visible
    // ─────────────────────────────────────────────────────────────────────
    private VBox buildProTeamCard(int rank, PandaScoreService.ProTeam t) {
        String[] pal = teamPalette(selectedGameId);

        VBox card = new VBox(10);
        card.setPrefWidth(CARD_WIDTH);
        card.setMinWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        card.setMinHeight(172);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(
                "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-color: rgba(255,255,255,0.07);" +
                        "-fx-background-color: " + pal[0] + ";" +
                        "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.65),18,0,0,8);"
        );

        // ── Top row: rank + logo + name + PRO badge ───────────────────────
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        // FIX: Rank label — always a fixed-width box so it never gets squished
        StackPane rankBox = new StackPane();
        rankBox.setMinWidth(36); rankBox.setMaxWidth(36);
        rankBox.setMinHeight(36); rankBox.setMaxHeight(36);
        rankBox.setStyle("-fx-background-color: rgba(0,0,0,0.35); -fx-background-radius: 8;");
        Label rkLbl = new Label("#" + rank);
        rkLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 13px;" +
                "-fx-font-weight: bold;");
        rankBox.getChildren().add(rkLbl);

        // Logo box
        StackPane logoBox = new StackPane();
        logoBox.setMinSize(42, 42); logoBox.setMaxSize(42, 42);
        logoBox.setStyle("-fx-background-color: rgba(0,0,0,0.28); -fx-background-radius: 10;");
        try {
            if (t.imageUrl != null && !t.imageUrl.isEmpty()) {
                ImageView iv = new ImageView(new Image(t.imageUrl, 38, 38, true, true, true));
                iv.setFitWidth(38); iv.setFitHeight(38);
                logoBox.getChildren().add(iv);
            } else {
                Label ph = new Label(t.acronym != null && !t.acronym.isEmpty()
                        ? t.acronym.substring(0, Math.min(3, t.acronym.length())) : "?");
                ph.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
                logoBox.getChildren().add(ph);
            }
        } catch (Exception ex) {
            Label ph = new Label("?");
            ph.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            logoBox.getChildren().add(ph);
        }

        // Name + meta
        VBox names = new VBox(2);
        HBox.setHgrow(names, Priority.ALWAYS);
        Label titleLbl = new Label(t.name != null ? t.name : "Team");
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(CARD_WIDTH - 120);
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        String sub = (t.acronym != null && !t.acronym.isBlank() ? t.acronym + " · " : "")
                + selectedGameId
                + (t.rankingScore > 0 ? "  ·  " + t.rankingScore + " pts" : "");
        Label metaLbl = new Label(sub);
        metaLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 9px;");
        names.getChildren().addAll(titleLbl, metaLbl);

        // PRO badge
        Label proBadge = new Label("PRO");
        proBadge.setStyle("-fx-background-color: rgba(143,63,255,0.25); -fx-text-fill: #9B6FE0;" +
                "-fx-font-size: 8px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 2 6;");

        top.getChildren().addAll(rankBox, logoBox, names, proBadge);

        // ── Rank watermark ────────────────────────────────────────────────
        Label watermark = new Label("#" + rank);
        watermark.setStyle("-fx-text-fill: rgba(255,255,255,0.04); -fx-font-size: 32px; -fx-font-weight: bold;");

        // ── View Roster button ────────────────────────────────────────────
        Button detailBtn = new Button("View Roster →");
        detailBtn.setPrefWidth(CARD_WIDTH - 32);
        detailBtn.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: rgba(255,255,255,0.85);" +
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 8;" +
                "-fx-padding: 5 12; -fx-cursor: hand;");
        detailBtn.setOnAction(ev -> openProTeamDetailPanel(t));

        card.getChildren().addAll(top, watermark, detailBtn);
        attachCardHover(card);
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) openProTeamDetailPanel(t);
        });
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────
    // FIX 4: LOCAL TEAM CARD — darker, same width as pro cards
    // ─────────────────────────────────────────────────────────────────────
    private VBox buildLocalTeamCard(Equipe e) {
        String jeu   = safeGet(e::getJeu);
        String style = safeGet(e::getStyle);
        String[] pal = teamPalette(jeu != null ? jeu : selectedGameId);

        // Darker version of the palette color
        String darkBg = darkenColor(pal[0]);

        VBox card = new VBox(10);
        card.setPrefWidth(CARD_WIDTH);
        card.setMinWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        card.setMinHeight(190);
        card.setPadding(new Insets(14, 16, 12, 16));
        card.setStyle(
                "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-color: rgba(255,255,255,0.07);" +
                        "-fx-background-color: " + darkBg + ";" +
                        "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.65),18,0,0,8);"
        );

        // ── Top: avatar + name + LOCAL badge ─────────────────────────────
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.setMinSize(42, 42); avatar.setMaxSize(42, 42);
        avatar.setStyle("-fx-background-color: rgba(0,0,0,0.32); -fx-background-radius: 10;");
        String initials = e.getNom() != null && !e.getNom().isEmpty()
                ? e.getNom().substring(0, Math.min(2, e.getNom().length())).toUpperCase() : "??";
        Label init = new Label(initials);
        init.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        avatar.getChildren().add(init);

        VBox names = new VBox(2);
        HBox.setHgrow(names, Priority.ALWAYS);
        Label nameLbl = new Label(e.getNom() != null ? e.getNom() : "Unnamed");
        nameLbl.setWrapText(true);
        nameLbl.setMaxWidth(CARD_WIDTH - 110);
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        String gameTag  = jeu   != null && !jeu.isBlank()   ? jeu   : "—";
        String styleTag = style != null && !style.isBlank() ? style : "—";
        Label meta = new Label(gameTag + " · " + styleTag);
        meta.setStyle("-fx-text-fill: rgba(255,255,255,0.45); -fx-font-size: 9px;");
        names.getChildren().addAll(nameLbl, meta);

        Label localBadge = new Label("LOCAL");
        localBadge.setStyle("-fx-background-color: rgba(56,143,255,0.2); -fx-text-fill: #388FFF;" +
                "-fx-font-size: 8px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 2 6;");

        top.getChildren().addAll(avatar, names, localBadge);

        // ── Stats row ─────────────────────────────────────────────────────
        double winRate   = computeWinRate(e.getId());
        int    matchCount = countMatches(e.getId());
        HBox stats = new HBox(8);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.getChildren().addAll(
                makeStatChip("👥", String.valueOf(e.getNbMembres()), "Members",  "#1DB876"),
                makeStatChip("🏆", String.format("%.0f%%", winRate),  "Win Rate",
                        winRate >= 60 ? "#1DB876" : winRate >= 40 ? "#FFC01A" : "#F23333"),
                makeStatChip("⚔",  String.valueOf(matchCount),         "Matches",  "#8F3FFF")
        );

        // ── Captain ───────────────────────────────────────────────────────
        Label captLbl = new Label("Cap. ID: " + e.getCapitaineId());
        captLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.25); -fx-font-size: 9px;");

        // ── Actions ───────────────────────────────────────────────────────
        HBox actions = new HBox(6);
        Button editBtn = new Button("✏ Edit");
        editBtn.setStyle("-fx-background-color: rgba(56,143,255,0.18); -fx-text-fill: #388FFF;" +
                "-fx-font-size: 9px; -fx-font-weight: bold; -fx-background-radius: 7;" +
                "-fx-padding: 4 9; -fx-cursor: hand;");
        editBtn.setOnAction(ev -> loadTeamIntoForm(e));

        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-background-color: rgba(242,51,51,0.15); -fx-text-fill: #F23333;" +
                "-fx-font-size: 9px; -fx-background-radius: 7; -fx-padding: 4 7; -fx-cursor: hand;");
        deleteBtn.setOnAction(ev -> deleteTeam(e));

        Region actSpacer = new Region();
        HBox.setHgrow(actSpacer, Priority.ALWAYS);

        Button detailBtn = new Button("Details →");
        detailBtn.setStyle("-fx-background-color: rgba(143,63,255,0.18); -fx-text-fill: #8F3FFF;" +
                "-fx-font-size: 9px; -fx-font-weight: bold; -fx-background-radius: 7;" +
                "-fx-padding: 4 9; -fx-cursor: hand;");
        detailBtn.setOnAction(ev -> openLocalTeamDetail(e, winRate, matchCount));

        actions.getChildren().addAll(editBtn, deleteBtn, actSpacer, detailBtn);

        card.getChildren().addAll(top, stats, captLbl, actions);
        attachCardHover(card);
        return card;
    }

    /** Darker version: reduce brightness by mixing with a very dark base */
    private String darkenColor(String hexOrCss) {
        // Map each game's already-dark palette to an even darker variant
        return switch (hexOrCss) {
            case "#6B2FD4" -> "#3D1A78";   // Valorant
            case "#6a7518" -> "#3a400d";   // CS2
            case "#1a5a8a" -> "#0d2f48";   // LoL
            case "#702070" -> "#3a103a";   // Dota2
            case "#0d6e2a" -> "#063915";   // R6
            case "#006060" -> "#003030";   // RL
            default        -> "#1C1C2E";
        };
    }

    // ─────────────────────────────────────────────────────────────────────
    // STAT CHIP
    // ─────────────────────────────────────────────────────────────────────
    private HBox makeStatChip(String icon, String value, String label, String color) {
        VBox chip = new VBox(1);
        chip.setAlignment(Pos.CENTER);
        Label v = new Label(icon + " " + value);
        v.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 8px;");
        chip.getChildren().addAll(v, l);
        HBox box = new HBox(chip);
        box.setStyle("-fx-background-color: rgba(0,0,0,0.28); -fx-background-radius: 7; -fx-padding: 5 7;");
        return box;
    }

    // ─────────────────────────────────────────────────────────────────────
    // WIN RATE & MATCH COUNT
    // ─────────────────────────────────────────────────────────────────────
    private double computeWinRate(int teamId) {
        try {
            long played = 0, wins = 0;
            for (Match m : matchService.getAll()) {
                if (!"termine".equalsIgnoreCase(m.getStatut())) continue;
                if (m.getEquipe1Id() != teamId && m.getEquipe2Id() != teamId) continue;
                played++;
                ResultatMatch r = resultatService.getByMatchId(m.getId());
                if (r != null && r.getGagnantId() == teamId) wins++;
            }
            return played > 0 ? (double) wins / played * 100 : 0;
        } catch (Exception ignored) { return 0; }
    }

    private int countMatches(int teamId) {
        try {
            return (int) matchService.getAll().stream()
                    .filter(m -> m.getEquipe1Id() == teamId || m.getEquipe2Id() == teamId)
                    .count();
        } catch (Exception ignored) { return 0; }
    }

    // ─────────────────────────────────────────────────────────────────────
    // PRO TEAM ASYNC LOAD
    // ─────────────────────────────────────────────────────────────────────
    private void loadProTeamsAsync() {
        showLoadingOverlay(true);
        final String slug = PandaScoreService.videogameSlugForAppGame(selectedGameId);
        Task<List<PandaScoreService.ProTeam>> task = new Task<>() {
            @Override protected List<PandaScoreService.ProTeam> call() {
                return pandaScore.getTopTeams(slug, 10);
            }
        };
        task.setOnSucceeded(ev -> Platform.runLater(() -> {
            showLoadingOverlay(false);
            proTeams = task.getValue() != null ? task.getValue() : new ArrayList<>();
            refreshGrid();
        }));
        task.setOnFailed(ev -> Platform.runLater(() -> {
            showLoadingOverlay(false);
            proTeams = new ArrayList<>();
            refreshGrid();
        }));
        new Thread(task, "pandascore-teams").start();
    }

    // ─────────────────────────────────────────────────────────────────────
    // DRAWER — LOCAL TEAM DETAIL
    // ─────────────────────────────────────────────────────────────────────
    private void openLocalTeamDetail(Equipe e, double winRate, int matchCount) {
        if (teamDrawerLayer == null) return;
        teamDrawerLayer.setManaged(true);
        teamDrawerLayer.setVisible(true);
        teamDrawerLayer.setPickOnBounds(true);
        teamDrawerBackdrop.setOpacity(0);
        teamDetailDrawer.setTranslateX(480);
        TranslateTransition slide = new TranslateTransition(Duration.millis(320), teamDetailDrawer);
        slide.setFromX(480); slide.setToX(0); slide.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition bd = new FadeTransition(Duration.millis(240), teamDrawerBackdrop);
        bd.setFromValue(0); bd.setToValue(1);
        new ParallelTransition(slide, bd).play();
        populateLocalTeamDetail(e, winRate, matchCount);
        animateTeamDetailContentIn();
    }

    private void populateLocalTeamDetail(Equipe e, double winRate, int matchCount) {
        teamDetailContent.getChildren().clear();
        String jeu   = safeGet(e::getJeu);
        String style = safeGet(e::getStyle);
        String[] pal = teamPalette(jeu != null ? jeu : selectedGameId);

        VBox hero = new VBox(8);
        hero.setPadding(new Insets(18));
        hero.setStyle("-fx-background-color: " + darkenColor(pal[0]) + "; -fx-background-radius: 14;");
        StackPane av = new StackPane();
        av.setMinSize(60, 60); av.setMaxSize(60, 60);
        av.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 12;");
        String ini = e.getNom() != null && !e.getNom().isEmpty()
                ? e.getNom().substring(0, Math.min(2, e.getNom().length())).toUpperCase() : "??";
        Label initLbl = new Label(ini);
        initLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        av.getChildren().add(initLbl);
        Label teamName = new Label(e.getNom() != null ? e.getNom() : "Team");
        teamName.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        String gt = jeu   != null && !jeu.isBlank()   ? jeu   : "—";
        String st = style != null && !style.isBlank() ? style : "—";
        Label teamMeta = new Label(gt + "  ·  " + st + "  ·  " + e.getNbMembres() + " members");
        teamMeta.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 11px;");
        hero.getChildren().addAll(av, teamName, teamMeta);
        teamDetailContent.getChildren().add(hero);

        HBox statsRow = new HBox(10);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.setPadding(new Insets(4, 0, 4, 0));
        addDetailStat(statsRow, "🏆", String.format("%.0f%%", winRate), "Win Rate",
                winRate >= 60 ? "#1DB876" : winRate >= 40 ? "#FFC01A" : "#F23333");
        addDetailStat(statsRow, "⚔",  String.valueOf(matchCount), "Matches", "#8F3FFF");
        addDetailStat(statsRow, "👥", String.valueOf(e.getNbMembres()), "Members", "#388FFF");
        teamDetailContent.getChildren().add(statsRow);

        Label wrHead = new Label("Win Rate");
        wrHead.setStyle("-fx-text-fill: #949499; -fx-font-size: 11px;");
        ProgressBar wrBar = new ProgressBar(winRate / 100.0);
        wrBar.setPrefWidth(368);
        String barColor = winRate >= 60 ? "#1DB876" : winRate >= 40 ? "#FFC01A" : "#F23333";
        wrBar.setStyle("-fx-accent: " + barColor + "; -fx-pref-height: 7;");
        Label wrPct = new Label(String.format("%.1f%% across %d matches", winRate, matchCount));
        wrPct.setStyle("-fx-text-fill: " + barColor + "; -fx-font-size: 11px;");
        teamDetailContent.getChildren().addAll(wrHead, wrBar, wrPct);

        if (style != null && !style.isBlank()) {
            HBox sb = new HBox(8);
            sb.setAlignment(Pos.CENTER_LEFT);
            sb.setPadding(new Insets(4, 0, 0, 0));
            Label sIcon = new Label("🎯 Play Style");
            sIcon.setStyle("-fx-text-fill: #949499; -fx-font-size: 11px;");
            Label sVal = new Label(style);
            sVal.setStyle("-fx-background-color: rgba(143,63,255,0.18); -fx-text-fill: #8F3FFF;" +
                    "-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 3 10;");
            sb.getChildren().addAll(sIcon, sVal);
            teamDetailContent.getChildren().add(sb);
        }

        Label matchHead = new Label("Recent Match History");
        matchHead.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 0 2 0;");
        teamDetailContent.getChildren().add(matchHead);
        try {
            List<Match> history = matchService.getAll().stream()
                    .filter(m -> (m.getEquipe1Id() == e.getId() || m.getEquipe2Id() == e.getId())
                            && "termine".equalsIgnoreCase(m.getStatut()))
                    .collect(Collectors.toList());
            if (history.isEmpty()) {
                Label noH = new Label("No completed matches yet.");
                noH.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");
                teamDetailContent.getChildren().add(noH);
            } else {
                for (Match m : history.subList(0, Math.min(6, history.size()))) {
                    ResultatMatch r = null;
                    try { r = resultatService.getByMatchId(m.getId()); } catch (Exception ignored2) {}
                    HBox row = new HBox(8);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setStyle("-fx-background-color: #141420; -fx-background-radius: 8; -fx-padding: 7 10;");
                    Label dateL = new Label(m.getDateMatch() != null ? m.getDateMatch().toString() : "?");
                    dateL.setStyle("-fx-text-fill: #555; -fx-font-size: 10px; -fx-min-width: 75;");
                    String opp = m.getEquipe1Id() == e.getId()
                            ? "vs Team " + m.getEquipe2Id() : "vs Team " + m.getEquipe1Id();
                    Label oppLbl = new Label(opp);
                    oppLbl.setStyle("-fx-text-fill: #CCC; -fx-font-size: 11px;");
                    HBox.setHgrow(oppLbl, Priority.ALWAYS);
                    boolean won = r != null && r.getGagnantId() == e.getId();
                    Label score = new Label(r != null ? r.getScoreEquipe1() + "–" + r.getScoreEquipe2() : "—");
                    score.setStyle("-fx-text-fill: " + (won ? "#1DB876" : "#F23333") +
                            "; -fx-font-size: 12px; -fx-font-weight: bold;");
                    Label wl = new Label(r == null ? "?" : (won ? "W" : "L"));
                    wl.setStyle("-fx-background-color:" + (won ? "rgba(29,184,118,0.18)" : "rgba(242,51,51,0.18)")
                            + "; -fx-text-fill:" + (won ? "#1DB876" : "#F23333")
                            + "; -fx-font-size:9px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:2 6;");
                    row.getChildren().addAll(dateL, oppLbl, score, wl);
                    teamDetailContent.getChildren().add(row);
                }
            }
        } catch (Exception ignored) {}

        Button editBtn = new Button("✏ Edit this team");
        editBtn.setPrefWidth(368);
        editBtn.setStyle("-fx-background-color: #388FFF; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 10 18; -fx-cursor: hand; -fx-font-size: 13px;");
        editBtn.setOnAction(ev -> { loadTeamIntoForm(e); hideTeamDrawer(); });
        teamDetailContent.getChildren().add(editBtn);
    }

    private void addDetailStat(HBox container, String icon, String value, String label, String color) {
        VBox chip = new VBox(2);
        chip.setAlignment(Pos.CENTER);
        chip.setStyle("-fx-background-color: #141420; -fx-background-radius: 10; -fx-padding: 10 16;");
        Label v = new Label(icon + " " + value);
        v.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #555; -fx-font-size: 10px;");
        chip.getChildren().addAll(v, l);
        HBox.setHgrow(chip, Priority.ALWAYS);
        container.getChildren().add(chip);
    }

    // ─────────────────────────────────────────────────────────────────────
    // DRAWER — PRO TEAM DETAIL
    // ─────────────────────────────────────────────────────────────────────
    private void openProTeamDetailPanel(PandaScoreService.ProTeam t) {
        if (t == null || teamDrawerLayer == null) return;
        final int loadToken = ++teamDetailLoadToken;
        teamDrawerLayer.setManaged(true);
        teamDrawerLayer.setVisible(true);
        teamDrawerLayer.setPickOnBounds(true);
        teamDetailContent.getChildren().setAll(makeTeamDetailLoading());
        teamDrawerBackdrop.setOpacity(0);
        teamDetailDrawer.setTranslateX(480);
        TranslateTransition slide = new TranslateTransition(Duration.millis(320), teamDetailDrawer);
        slide.setFromX(480); slide.setToX(0); slide.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition bd = new FadeTransition(Duration.millis(240), teamDrawerBackdrop);
        bd.setFromValue(0); bd.setToValue(1);
        new ParallelTransition(slide, bd).play();
        Task<PandaScoreService.ProTeamDetail> task = new Task<>() {
            @Override protected PandaScoreService.ProTeamDetail call() { return pandaScore.getTeamById(t.id); }
        };
        task.setOnSucceeded(ev -> Platform.runLater(() -> {
            if (loadToken != teamDetailLoadToken) return;
            PandaScoreService.ProTeamDetail d = task.getValue();
            if (d == null) { teamDetailContent.getChildren().setAll(new Label("Could not load team.")); return; }
            populateProTeamDetail(d);
            animateTeamDetailContentIn();
        }));
        task.setOnFailed(ev -> Platform.runLater(() -> {
            if (loadToken != teamDetailLoadToken) return;
            teamDetailContent.getChildren().setAll(new Label("Network error."));
        }));
        new Thread(task, "pandascore-team-detail").start();
    }

    private VBox makeTeamDetailLoading() {
        ProgressIndicator pi = new ProgressIndicator(); pi.setPrefSize(44, 44);
        VBox box = new VBox(12, pi); box.setAlignment(Pos.CENTER); box.setPadding(new Insets(48, 0, 48, 0));
        Label hint = new Label("Fetching roster from PandaScore…");
        hint.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        box.getChildren().add(hint);
        return box;
    }

    private void animateTeamDetailContentIn() {
        if (teamDetailContent == null) return;
        teamDetailContent.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(220), teamDetailContent);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void populateProTeamDetail(PandaScoreService.ProTeamDetail d) {
        teamDetailContent.getChildren().clear();
        HBox hero = new HBox(14); hero.setAlignment(Pos.CENTER_LEFT);
        StackPane logoBox = new StackPane();
        logoBox.setMinSize(72, 72); logoBox.setMaxSize(72, 72);
        logoBox.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 14;");
        try {
            String img = d.imageUrl != null && !d.imageUrl.isEmpty() ? d.imageUrl : d.darkImageUrl;
            if (img != null && !img.isEmpty()) {
                ImageView iv = new ImageView(new Image(img, 64, 64, true, true, true));
                iv.setFitWidth(64); iv.setFitHeight(64); logoBox.getChildren().add(iv);
            } else {
                Label ph = new Label(d.acronym != null && !d.acronym.isEmpty() ? d.acronym : "?");
                ph.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
                logoBox.getChildren().add(ph);
            }
        } catch (Exception ignored) {}
        VBox titles = new VBox(4);
        Label name = new Label(d.name != null ? d.name : "Team");
        name.setWrapText(true);
        name.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        StringBuilder meta = new StringBuilder();
        if (d.acronym != null && !d.acronym.isBlank()) meta.append(d.acronym).append(" · ");
        if (d.videogameName != null && !d.videogameName.isBlank()) meta.append(d.videogameName);
        else if (selectedGameId != null) meta.append(selectedGameId);
        if (d.location != null && !d.location.isBlank()) meta.append(" · ").append(d.location);
        Label sub = new Label(meta.toString());
        sub.setWrapText(true); sub.setStyle("-fx-text-fill: #949499; -fx-font-size: 12px;");
        titles.getChildren().addAll(name, sub);
        Region grow = new Region(); HBox.setHgrow(grow, Priority.ALWAYS);
        hero.getChildren().addAll(logoBox, titles, grow);
        teamDetailContent.getChildren().add(hero);

        if (!d.players.isEmpty()) {
            Label rosterHead = new Label("Roster (" + d.players.size() + ")");
            rosterHead.setStyle("-fx-text-fill: #388FFF; -fx-font-size: 13px; -fx-font-weight: bold;");
            teamDetailContent.getChildren().add(rosterHead);
            for (PandaScoreService.ProTeamPlayer p : d.players)
                teamDetailContent.getChildren().add(buildPlayerRow(p));
        } else {
            Label emptyR = new Label("No roster players returned.");
            emptyR.setWrapText(true); emptyR.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");
            teamDetailContent.getChildren().add(emptyR);
        }

        Button ps = new Button("View on PandaScore");
        ps.setPrefWidth(368);
        ps.setStyle("-fx-background-color: #388FFF; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 10 18; -fx-cursor: hand;");
        ps.setOnAction(e -> {
            try { Desktop.getDesktop().browse(new URI("https://pandascore.co/teams/"
                    + (d.slug != null && !d.slug.isBlank() ? d.slug : d.id))); }
            catch (Exception ex) { System.out.println("Browse: " + ex.getMessage()); }
        });
        teamDetailContent.getChildren().add(ps);
    }

    private HBox buildPlayerRow(PandaScoreService.ProTeamPlayer p) {
        HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #141420; -fx-background-radius: 10; -fx-padding: 8 10;");
        StackPane av = new StackPane();
        av.setMinSize(34, 34); av.setMaxSize(34, 34);
        av.setStyle("-fx-background-color: #1e1e30; -fx-background-radius: 8;");
        try {
            if (p.imageUrl != null && !p.imageUrl.isEmpty()) {
                ImageView iv = new ImageView(new Image(p.imageUrl, 30, 30, true, true, true));
                iv.setFitWidth(30); iv.setFitHeight(30); av.getChildren().add(iv);
            } else {
                String initial = (p.name != null && !p.name.isEmpty())
                        ? p.name.substring(0, 1).toUpperCase() : "?";
                Label l = new Label(initial);
                l.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px; -fx-font-weight: bold;");
                av.getChildren().add(l);
            }
        } catch (Exception ignored) {}
        VBox tx = new VBox(2);
        Label nm = new Label(p.name != null ? p.name : "Player");
        nm.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        StringBuilder bits = new StringBuilder();
        if (p.role        != null && !p.role.isBlank())        bits.append(p.role);
        if (p.nationality != null && !p.nationality.isBlank()) {
            if (!bits.isEmpty()) bits.append(" · "); bits.append(p.nationality);
        }
        if (!p.active) { if (!bits.isEmpty()) bits.append(" · "); bits.append("inactive"); }
        Label sm = new Label(bits.isEmpty() ? " " : bits.toString());
        sm.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");
        tx.getChildren().addAll(nm, sm); HBox.setHgrow(tx, Priority.ALWAYS);
        row.getChildren().addAll(av, tx);
        return row;
    }

    // ─────────────────────────────────────────────────────────────────────
    // GAME BAR
    // ─────────────────────────────────────────────────────────────────────
    private void buildTeamsGameBar() {
        teamsGameBar.getChildren().clear();
        for (GameBtnMeta g : TEAM_GAMES) {
            Button b = new Button(); b.setUserData(g.id);
            b.setPrefHeight(48); b.setPrefWidth(148);
            b.setOnAction(this::onTeamsGameClicked);
            styleTeamsGameButton(b, g.id.equals(selectedGameId), g);
            teamsGameBar.getChildren().add(b);
        }
    }

    private void styleTeamsGameButton(Button btn, boolean active, GameBtnMeta g) {
        String border = active ? "-fx-border-color:" + g.dotColor + ";-fx-border-radius:10;-fx-border-width:1.2;" : "";
        btn.setStyle("-fx-background-color: #141420; -fx-background-radius: 10; -fx-cursor: hand; " + border);
        HBox row = new HBox(6); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(0, 0, 0, 10));
        Label dot = new Label("●"); dot.setStyle("-fx-text-fill: " + g.dotColor + "; -fx-font-size: 9px;");
        Label name = new Label(g.label);
        name.setStyle("-fx-text-fill: " + (active ? "white" : "#777") + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        row.getChildren().addAll(dot, name);
        btn.setGraphic(row);
        attachBtnHover(btn);
    }

    private void onTeamsGameClicked(ActionEvent e) {
        Button clicked = (Button) e.getSource();
        String id = (String) clicked.getUserData();
        if (id == null || id.equals(selectedGameId)) return;
        selectedGameId = id;
        for (Node n : teamsGameBar.getChildren()) {
            if (n instanceof Button b) {
                String bid = (String) b.getUserData();
                GameBtnMeta meta = java.util.Arrays.stream(TEAM_GAMES)
                        .filter(x -> x.id.equals(bid)).findFirst().orElse(TEAM_GAMES[0]);
                styleTeamsGameButton(b, bid.equals(selectedGameId), meta);
            }
        }
        proTeams = new ArrayList<>();
        loadProTeamsAsync();
    }

    // ─────────────────────────────────────────────────────────────────────
    // FORM CRUD
    // ─────────────────────────────────────────────────────────────────────
    @FXML public void saveTeam() {
        formError.setText("");
        String name = teamNameField.getText() != null ? teamNameField.getText().trim() : "";
        if (name.isEmpty()) { formError.setText("Team name is required."); return; }
        int cap = 0;
        try { cap = Integer.parseInt(capitaineField.getText().trim()); } catch (Exception ignored) {}
        int nb = 5;
        try { nb = Integer.parseInt(nbMembresField.getText().trim()); } catch (Exception ignored) {}
        String game  = teamGameCombo.getValue();
        String style = teamStyleCombo.getValue();
        try {
            if (editingTeam == null) {
                Equipe eq = new Equipe(0, name, cap, nb);
                try { eq.setJeu(game); }   catch (Exception ignored) {}
                try { eq.setStyle(style); } catch (Exception ignored) {}
                equipeService.add(eq);
                showAlert("✅ Team \"" + name + "\" created!");
            } else {
                editingTeam.setNom(name); editingTeam.setCapitaineId(cap); editingTeam.setNbMembres(nb);
                try { editingTeam.setJeu(game); }   catch (Exception ignored) {}
                try { editingTeam.setStyle(style); } catch (Exception ignored) {}
                equipeService.update(editingTeam);
                showAlert("✅ Team updated!");
            }
            clearForm(); loadLocalTeams();
        } catch (Exception ex) { formError.setText("❌ " + ex.getMessage()); }
    }

    @FXML public void clearForm() {
        editingTeam = null; formTitle.setText("New Team"); btnSaveTeam.setText("Create Team"); formError.setText("");
        teamNameField.clear(); capitaineField.clear(); nbMembresField.clear();
        teamGameCombo.setValue(selectedGameId); teamStyleCombo.setValue("Balanced");
    }

    private void loadTeamIntoForm(Equipe e) {
        editingTeam = e; formTitle.setText("Edit Team #" + e.getId()); btnSaveTeam.setText("Update Team"); formError.setText("");
        teamNameField.setText(e.getNom() != null ? e.getNom() : "");
        capitaineField.setText(String.valueOf(e.getCapitaineId()));
        nbMembresField.setText(String.valueOf(e.getNbMembres()));
        try { if (e.getJeu()   != null) teamGameCombo.setValue(e.getJeu()); }   catch (Exception ignored) {}
        try { if (e.getStyle() != null) teamStyleCombo.setValue(e.getStyle()); } catch (Exception ignored) {}
    }

    private void deleteTeam(Equipe e) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Team"); confirm.setHeaderText(null);
        confirm.setContentText("Delete \"" + e.getNom() + "\"? This cannot be undone.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try { equipeService.delete(e.getId()); loadLocalTeams(); }
                catch (Exception ex) { showAlert("❌ " + ex.getMessage()); }
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────
    // HELPERS & ANIMATIONS
    // ─────────────────────────────────────────────────────────────────────
    private String[] teamPalette(String gameId) {
        return switch (gameId != null ? gameId : "") {
            case "Valorant"      -> new String[]{"#6B2FD4", "#1a0d2e"};
            case "CS2"           -> new String[]{"#6a7518", "#121407"};
            case "LoL"           -> new String[]{"#1a5a8a", "#081018"};
            case "Dota2"         -> new String[]{"#702070", "#100810"};
            case "R6 Siege"      -> new String[]{"#0d6e2a", "#071207"};
            case "Rocket League" -> new String[]{"#006060", "#051414"};
            default              -> new String[]{"#5B2D8C", "#1a0d2e"};
        };
    }

    private void setupLoadingLogoAnimation() {
        java.net.URL res = getClass().getResource("/logoteam.png");
        if (res != null && loadingLogo != null)
            loadingLogo.setImage(new Image(res.toExternalForm(), true));
        if (loadingLogo != null) {
            logoFloat = new TranslateTransition(Duration.millis(850), loadingLogo);
            logoFloat.setFromY(0); logoFloat.setToY(-16);
            logoFloat.setAutoReverse(true); logoFloat.setCycleCount(Animation.INDEFINITE);
        }
    }

    private void showLoadingOverlay(boolean show) {
        if (loadingOverlay == null) return;
        loadingOverlay.setVisible(show); loadingOverlay.setManaged(show);
        if (show) { if (logoFloat != null) logoFloat.play(); }
        else      { if (logoFloat != null) logoFloat.stop(); if (loadingLogo != null) loadingLogo.setTranslateY(0); }
    }

    private void animateCardIn(Node node, int index) {
        node.setOpacity(0); node.setTranslateY(24);
        FadeTransition fade = new FadeTransition(Duration.millis(300), node); fade.setFromValue(0); fade.setToValue(1);
        TranslateTransition move = new TranslateTransition(Duration.millis(320), node); move.setFromY(24); move.setToY(0);
        ParallelTransition pt = new ParallelTransition(fade, move);
        pt.setDelay(Duration.millis(index * 50L)); pt.play();
    }

    private void attachCardHover(VBox card) {
        ScaleTransition in  = new ScaleTransition(Duration.millis(180), card); in.setToX(1.03);  in.setToY(1.04);
        ScaleTransition out = new ScaleTransition(Duration.millis(180), card); out.setToX(1.0); out.setToY(1.0);
        card.setOnMouseEntered(e -> in.playFromStart());
        card.setOnMouseExited(e -> out.playFromStart());
    }

    private void attachBtnHover(Button btn) {
        ScaleTransition in  = new ScaleTransition(Duration.millis(120), btn); in.setToX(1.04);  in.setToY(1.04);
        ScaleTransition out = new ScaleTransition(Duration.millis(120), btn); out.setToX(1.0); out.setToY(1.0);
        btn.setOnMouseEntered(e -> in.playFromStart());
        btn.setOnMouseExited(e -> out.playFromStart());
    }

    private void hideTeamDrawer() {
        if (teamDrawerLayer == null || !teamDrawerLayer.isVisible() || teamDetailDrawer == null) {
            hideTeamDrawerImmediate(); return;
        }
        TranslateTransition slide = new TranslateTransition(Duration.millis(260), teamDetailDrawer);
        slide.setFromX(teamDetailDrawer.getTranslateX()); slide.setToX(480); slide.setInterpolator(Interpolator.EASE_IN);
        FadeTransition bd = new FadeTransition(Duration.millis(200), teamDrawerBackdrop);
        bd.setFromValue(teamDrawerBackdrop.getOpacity()); bd.setToValue(0);
        ParallelTransition pt = new ParallelTransition(slide, bd);
        pt.setOnFinished(e -> {
            teamDrawerLayer.setVisible(false); teamDrawerLayer.setManaged(false); teamDrawerLayer.setPickOnBounds(false);
            if (teamDetailContent != null) teamDetailContent.getChildren().clear();
            teamDetailDrawer.setTranslateX(480);
        });
        pt.play();
    }

    private void hideTeamDrawerImmediate() {
        if (teamDrawerLayer == null) return;
        teamDrawerLayer.setVisible(false); teamDrawerLayer.setManaged(false); teamDrawerLayer.setPickOnBounds(false);
        if (teamDetailDrawer != null) teamDetailDrawer.setTranslateX(480);
        if (teamDrawerBackdrop != null) teamDrawerBackdrop.setOpacity(0);
        if (teamDetailContent != null) teamDetailContent.getChildren().clear();
    }

    private String safeGet(java.util.function.Supplier<String> fn) {
        try { return fn.get(); } catch (Exception ignored) { return null; }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Teams"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    // ─────────────────────────────────────────────────────────────────────
    // FXML HANDLERS
    // ─────────────────────────────────────────────────────────────────────
    @FXML public void onSearch()            { refreshGrid(); }
    @FXML public void onTeamDetailClose()   { hideTeamDrawer(); }
    @FXML public void onTeamDrawerBackdrop(MouseEvent e) { hideTeamDrawer(); }
    @FXML public void onCreateTeam()        { clearForm(); }
    @FXML public void onInvitePlayer()      {}
    @FXML public void onManageTeam()        {}
    @FXML public void filterAll()           { loadLocalTeams(); }
    @FXML public void filterMine()          { loadLocalTeams(); }
    @FXML public void filterRecruiting()    {}
    @FXML public void filterTop()           { loadProTeamsAsync(); }

    // ─────────────────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────────────────
    private void navigate(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxml));
            Stage stage = (Stage) teamsGrid.getScene().getWindow();
            stage.setScene(new Scene(root, AppScene.WIDTH, AppScene.HEIGHT));
        } catch (Exception e) { System.out.println("❌ Could not load: " + fxml + " → " + e.getMessage()); }
    }

    @FXML public void navDashboard()   { navigate("dashboard.fxml"); }
    @FXML public void navPlayers()     { navigate("players.fxml"); }
    @FXML public void navMatches()     { navigate("matches.fxml"); }
    @FXML public void navTeams()       { navigate("teams.fxml"); }
    @FXML public void navTournaments() { navigate("tournaments.fxml"); }
    @FXML public void navAnalytics()   { navigate("analytics.fxml"); }
    @FXML public void navProduits()    { navigate("produit.fxml"); }
    @FXML public void navSponsors()    { navigate("sponsor.fxml"); }
    @FXML public void navSettings()    { navigate("admin.fxml"); }
}