package Controllers;

import javafx.fxml.*;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.User;

public class DashboardController {

    // ── Left feed ──────────────────────────────────────────────────────────
    @FXML private Label  liveTourneyTitle;
    @FXML private VBox   liveTourneysBox;
    @FXML private VBox   newsBox;
    @FXML private HBox   trendsBox;
    @FXML private HBox   memesBox;

    // ── Game selector buttons ───────────────────────────────────────────────
    @FXML private HBox   gameSelectorBar;
    @FXML private Button btnValorant;
    @FXML private Button btnCS2;
    @FXML private Button btnLoL;
    @FXML private Button btnDota2;
    @FXML private Button btnFortnite;
    @FXML private Button btnApex;

    // ── Voice chat room ─────────────────────────────────────────────────────
    @FXML private Label   roomNameLabel;
    @FXML private VBox    voicePlayersBox;
    @FXML private Slider  volumeSlider;
    @FXML private Label   volumeLabel;
    @FXML
    private Button btnNavDashboard;
    @FXML
    private Button btnNavProduit;
    @FXML
    private Button btnNavMatches;
    @FXML
    private Button btnNavTournaments;
    @FXML
    private Button btnNavPlayers;
    @FXML
    private VBox navBox;
    @FXML
    private Button btnNavAnalytics;
    @FXML
    private Button btnNavSponsor;
    @FXML
    private Button btnNavTeams;

    // ── Game metadata ───────────────────────────────────────────────────────
    private record GameInfo(String id, String label, String dotColor,
                            String genre, String borderColor) {}

    // ── User session ───────────────────────────────────────────────────────
    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
    }

    private final GameInfo[] GAMES = {
            new GameInfo("Valorant", "Valorant",        "#E04444", "FPS",  "#E04444"),
            new GameInfo("CS2",      "CS2",             "#FFA000", "FPS",  "transparent"),
            new GameInfo("LoL",      "League of Legends","#388FFF","MOBA", "transparent"),
            new GameInfo("Dota2",    "Dota 2",          "#9F3FFF", "MOBA", "transparent"),
            new GameInfo("Fortnite", "Fortnite",        "#38D68A", "BR",   "transparent"),
            new GameInfo("Apex",     "Apex Legends",    "#FF7B3A", "BR",   "transparent"),
    };

    private String currentGame = "Valorant";

    private record VoicePlayer(String initials, String name, String avatarColor, boolean speaking) {}

    private final VoicePlayer[] VOICE_PLAYERS = {
            new VoicePlayer("TF", "TurboFrag",   "#388FFF", false),
            new VoicePlayer("NO", "NightOwl99",  "#8F3FFF", false),
            new VoicePlayer("SM", "SnipeMaster", "#38D68A", false),
            new VoicePlayer("RQ", "RiftQueen",   "#E04444", false),
    };

    // ───────────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        buildGameButtons();
        loadGameContent("Valorant");
        buildVoicePlayers();
        bindVolume();
    }

    private void buildGameButtons() {
        Button[] btns = { btnValorant, btnCS2, btnLoL, btnDota2, btnFortnite, btnApex };
        for (int i = 0; i < btns.length; i++) {
            GameInfo g = GAMES[i];
            Button btn = btns[i];
            btn.setUserData(g.id());

            VBox content = new VBox(4);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setStyle("-fx-padding: 0 0 0 14;");

            HBox nameRow = new HBox(6);
            nameRow.setAlignment(Pos.CENTER_LEFT);
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: " + g.dotColor() + "; -fx-font-size: 10px;");
            Label name = new Label(g.label());
            name.setStyle("-fx-text-fill: " + (g.id().equals("Valorant") ? "white" : "#949499")
                    + "; -fx-font-size: 13px; -fx-font-weight: bold;");
            nameRow.getChildren().addAll(dot, name);

            HBox tagRow = new HBox(6);
            tagRow.setAlignment(Pos.CENTER_LEFT);
            Label genreTag = new Label(g.genre());
            genreTag.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #949499;"
                    + "-fx-font-size: 9px; -fx-background-radius: 4; -fx-padding: 2 6;");
            tagRow.getChildren().add(genreTag);

            if (g.id().equals("Valorant")) {
                Label selTag = new Label("Selected");
                selTag.setStyle("-fx-background-color: rgba(224,68,68,0.18); -fx-text-fill: #E04444;"
                        + "-fx-font-size: 9px; -fx-background-radius: 4; -fx-padding: 2 6;");
                tagRow.getChildren().add(selTag);
            }

            content.getChildren().addAll(nameRow, tagRow);
            btn.setGraphic(content);
            btn.setText("");
        }
    }

    @FXML
    public void onGameSelected(javafx.event.ActionEvent e) {
        Button clicked = (Button) e.getSource();
        String gameId = (String) clicked.getUserData();
        currentGame = gameId;
        updateGameButtonStyles(gameId);
        loadGameContent(gameId);
    }

    private void updateGameButtonStyles(String selectedId) {
        Button[] btns = { btnValorant, btnCS2, btnLoL, btnDota2, btnFortnite, btnApex };
        for (int i = 0; i < btns.length; i++) {
            GameInfo g = GAMES[i];
            boolean active = g.id().equals(selectedId);
            String border = active ? "-fx-border-color: " + g.dotColor() + "; -fx-border-radius: 12; -fx-border-width: 1.5;" : "";
            btns[i].setStyle("-fx-background-color: #1C1C29; -fx-background-radius: 12; -fx-cursor: hand; " + border);

            VBox content = (VBox) btns[i].getGraphic();
            if (content != null) {
                HBox nameRow = (HBox) content.getChildren().get(0);
                Label nameLbl = (Label) nameRow.getChildren().get(1);
                nameLbl.setStyle("-fx-text-fill: " + (active ? "white" : "#949499")
                        + "; -fx-font-size: 13px; -fx-font-weight: bold;");

                HBox tagRow = (HBox) content.getChildren().get(1);
                tagRow.getChildren().clear();
                Label genreTag = new Label(g.genre());
                genreTag.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #949499;"
                        + "-fx-font-size: 9px; -fx-background-radius: 4; -fx-padding: 2 6;");
                tagRow.getChildren().add(genreTag);
                if (active) {
                    Label selTag = new Label("Selected");
                    selTag.setStyle("-fx-background-color: rgba(224,68,68,0.18); -fx-text-fill: #E04444;"
                            + "-fx-font-size: 9px; -fx-background-radius: 4; -fx-padding: 2 6;");
                    tagRow.getChildren().add(selTag);
                }
            }
        }
    }

    private void loadGameContent(String game) {
        String label = switch (game) {
            case "LoL"   -> "League of Legends";
            case "Dota2" -> "Dota 2";
            case "Apex"  -> "Apex Legends";
            default      -> game;
        };
        liveTourneyTitle.setText("Live tournaments — " + label);

        liveTourneysBox.getChildren().clear();
        Object[][] tours = {
                { true,  "VCT Champions 2025", "Team Liquid vs Sentinels", "142K", "$500K" },
                { true,  "VCT EMEA Stage 2",   "Fnatic vs NAVI",           "89K",  "$200K" },
                { false, "VCT Pacific Open",    "TBD",                      "—",    "$100K" },
        };
        for (Object[] t : tours)
            liveTourneysBox.getChildren().add(
                    buildTourneyRow((boolean) t[0], (String) t[1], (String) t[2], (String) t[3], (String) t[4]));

        newsBox.getChildren().clear();
        Object[][] news = {
                { "ROSTER", "Fnatic signs new IGL ahead of VCT EMEA",      "2h ago",  "rgba(242,51,51,0.15)",  "#F23333" },
                { "PATCH",  "Valorant 9.08 nerfs Jett and buffs Chamber",   "5h ago",  "rgba(56,143,255,0.15)", "#388FFF" },
                { "RESULT", "Team Liquid eliminates NaVi in dramatic 5-map","8h ago",  "rgba(29,184,118,0.15)", "#1DB876" },
                { "EVENT",  "VCT Champions 2025 group stage revealed",       "12h ago", "rgba(255,192,26,0.15)", "#FFC01A" },
        };
        for (Object[] n : news)
            newsBox.getChildren().add(
                    buildNewsRow((String) n[0], (String) n[1], (String) n[2], (String) n[3], (String) n[4]));

        trendsBox.getChildren().clear();
        String[][] trends = {
                { "#VCTChampions", "42K tweets" },
                { "#Valorant",     "38K tweets" },
                { "#FnaticWin",    "21K tweets" },
                { "#NewPatch",     "17K tweets" },
        };
        for (String[] tr : trends)
            trendsBox.getChildren().add(buildTrendCard(tr[0], tr[1]));

        memesBox.getChildren().clear();
        String[] memes = {
                "\"When your teammate instalocks Reyna...\"",
                "\"Peak VCT gameplay\"",
                "\"When the economy round fails\"",
        };
        for (String m : memes)
            memesBox.getChildren().add(buildMemeCard(m));
    }

    private HBox buildTourneyRow(boolean live, String name, String teams,
                                 String viewers, String prize) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #1C1C29; -fx-background-radius: 10; -fx-padding: 10 14;");

        Label badge = new Label(live ? "LIVE" : "Soon");
        badge.setStyle(live
                ? "-fx-background-color: rgba(242,51,51,0.18); -fx-text-fill: #F23333; -fx-font-size: 9px;"
                + "-fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 3 7;"
                : "-fx-background-color: rgba(255,160,20,0.18); -fx-text-fill: #FFA014; -fx-font-size: 9px;"
                + "-fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 3 7;");

        VBox info = new VBox(2);
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label teamsLbl = new Label(teams);
        teamsLbl.setStyle("-fx-text-fill: #949499; -fx-font-size: 11px;");
        info.getChildren().addAll(nameLbl, teamsLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox right = new VBox(2);
        right.setAlignment(Pos.CENTER_RIGHT);
        Label viewersLbl = new Label(viewers + " viewers");
        viewersLbl.setStyle("-fx-text-fill: #949499; -fx-font-size: 11px;");
        Label prizeLbl = new Label(prize);
        prizeLbl.setStyle("-fx-text-fill: #FFC01A; -fx-font-size: 13px; -fx-font-weight: bold;");
        right.getChildren().addAll(viewersLbl, prizeLbl);

        Button watchBtn = new Button("Watch");
        watchBtn.setStyle("-fx-background-color: #388FFF; -fx-text-fill: white; -fx-font-size: 11px;"
                + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 14;");

        row.getChildren().addAll(badge, info, spacer, right, watchBtn);
        return row;
    }

    private HBox buildNewsRow(String tag, String title, String time,
                              String tagBg, String tagColor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #1C1C29; -fx-background-radius: 8; -fx-padding: 10 14;");

        Label tagLbl = new Label(tag);
        tagLbl.setStyle("-fx-background-color: " + tagBg + "; -fx-text-fill: " + tagColor + ";"
                + "-fx-font-size: 9px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 3 7;");

        VBox info = new VBox(2);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        Label timeLbl = new Label(time);
        timeLbl.setStyle("-fx-text-fill: #949499; -fx-font-size: 10px;");
        info.getChildren().addAll(titleLbl, timeLbl);

        row.getChildren().addAll(tagLbl, info);
        return row;
    }

    private VBox buildTrendCard(String tag, String count) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color: #1C1C29; -fx-background-radius: 8;"
                + "-fx-padding: 10 14; -fx-min-width: 160;");
        Label tagLbl = new Label(tag);
        tagLbl.setStyle("-fx-text-fill: #388FFF; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label countLbl = new Label(count);
        countLbl.setStyle("-fx-text-fill: #949499; -fx-font-size: 10px;");
        card.getChildren().addAll(tagLbl, countLbl);
        return card;
    }

    private VBox buildMemeCard(String caption) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: #1C1C29; -fx-background-radius: 10;"
                + "-fx-min-width: 200; -fx-min-height: 130;");
        Label img = new Label("GIF / Image");
        img.setStyle("-fx-text-fill: #555; -fx-font-size: 11px; -fx-min-height: 90;"
                + "-fx-min-width: 200; -fx-alignment: center;"
                + "-fx-background-color: #1A1A28;");
        Label cap = new Label(caption);
        cap.setStyle("-fx-text-fill: #949499; -fx-font-size: 9px; -fx-padding: 6 8;");
        cap.setWrapText(true);
        card.getChildren().addAll(img, cap);
        return card;
    }

    private void buildVoicePlayers() {
        voicePlayersBox.getChildren().clear();
        for (VoicePlayer p : VOICE_PLAYERS)
            voicePlayersBox.getChildren().add(buildVoicePlayerRow(p));
    }

    private HBox buildVoicePlayerRow(VoicePlayer p) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #1C1C2E; -fx-padding: 10 14;"
                + "-fx-border-color: transparent transparent #22223A transparent; -fx-border-width: 0 0 1 0;");

        Label avatar = new Label(p.initials());
        avatar.setStyle("-fx-background-color: " + p.avatarColor() + "; -fx-background-radius: 20;"
                + "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-min-width: 38; -fx-min-height: 38; -fx-alignment: center;");

        Label nameLbl = new Label(p.name());
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        HBox.setHgrow(nameLbl, Priority.ALWAYS);

        StackPane avatarPane = new StackPane(avatar);
        Label statusDot = new Label("●");
        statusDot.setStyle("-fx-text-fill: #38D68A; -fx-font-size: 8px;");
        StackPane.setAlignment(statusDot, Pos.BOTTOM_RIGHT);
        avatarPane.getChildren().add(statusDot);

        Button micBtn = new Button("🎙");
        micBtn.setStyle("-fx-background-color: #252540; -fx-text-fill: #949499;"
                + "-fx-background-radius: 8; -fx-min-width: 30; -fx-min-height: 30;"
                + "-fx-font-size: 13px; -fx-cursor: hand;");

        Button headBtn = new Button("🎧");
        headBtn.setStyle("-fx-background-color: #252540; -fx-text-fill: #949499;"
                + "-fx-background-radius: 8; -fx-min-width: 30; -fx-min-height: 30;"
                + "-fx-font-size: 13px; -fx-cursor: hand;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(avatarPane, nameLbl, spacer, micBtn, headBtn);
        return row;
    }

    private void bindVolume() {
        volumeLabel.setText((int) volumeSlider.getValue() + "%");
        volumeSlider.valueProperty().addListener((obs, old, val) ->
                volumeLabel.setText(val.intValue() + "%"));
    }

    // ── Navigation ── fixed: no /views/ prefix, exact lowercase filenames ───
    private void navigate(String fxml) {
        try {
            System.out.println("🔍 Chargement de: " + fxml);
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxml));
            Stage stage = (Stage) btnNavDashboard.getScene().getWindow();
            stage.setScene(new Scene(root, 1440, 960));
            stage.show();
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement " + fxml + ": " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger " + fxml);
        }
    }

    @FXML

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
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