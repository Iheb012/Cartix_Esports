package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Comment;
import models.Post;
import services.CommunityService;
import org.example.MyDataBase;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminCommunityController {

    @FXML private VBox adminPostsContainer;
    @FXML private ScrollPane adminScrollPane;
    @FXML private TextField searchAdminField;
    @FXML private Label totalPostsLabel;
    @FXML private Label totalSignalesLabel;
    @FXML private Button btnTousLesPosts;
    @FXML private Button btnUsersSignales;
    @FXML private VBox adminMembersContainer;

    // ── Boutons de navigation sidebar gauche ──
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavUserManagement;
    @FXML private Button btnNavPlayers;
    @FXML private Button btnNavMatches;
    @FXML private Button btnNavTeams;
    @FXML private Button btnNavTournaments;
    @FXML private Button btnNavCommunityHub;
    @FXML private Button btnNavProductShop;
    @FXML private Button btnNavSponsor;
    @FXML private Button btnNavReports;
    @FXML private Button btnNavSettings;

    private final CommunityService service = new CommunityService();
    private List<Post> allPosts = new ArrayList<>();
    private String currentTab = "tous";

    private static final String STYLE_ADMIN_ACTIVE =
            "-fx-background-color: #1f6feb22;" +
                    "-fx-background-radius: 7;" +
                    "-fx-padding: 10 12;" +
                    "-fx-border-color: #1f6feb;" +
                    "-fx-border-radius: 7;" +
                    "-fx-border-width: 0 0 0 3;" +
                    "-fx-alignment: CENTER_LEFT;" +
                    "-fx-text-fill: #1f6feb;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;";

    private static final String STYLE_ADMIN_INACTIVE =
            "-fx-background-color: transparent;" +
                    "-fx-padding: 10 12;" +
                    "-fx-background-radius: 7;" +
                    "-fx-alignment: CENTER_LEFT;" +
                    "-fx-text-fill: #8b949e;" +
                    "-fx-font-size: 13px;" +
                    "-fx-border-width: 0;";

    @FXML
    public void initialize() {
        service.initCommentTable();
        service.initNotificationTable();
        setActiveAdminButton(btnTousLesPosts);
        loadAdminMembers();
        loadPostsFromDB();

        if (searchAdminField != null) {
            searchAdminField.textProperty().addListener((obs, oldVal, newVal) ->
                    filterAdminPosts(newVal.trim().toLowerCase()));
        }
    }

    private void setActiveAdminButton(Button active) {
        if (btnTousLesPosts != null) btnTousLesPosts.setStyle(STYLE_ADMIN_INACTIVE);
        if (btnUsersSignales != null) btnUsersSignales.setStyle(STYLE_ADMIN_INACTIVE);
        if (active != null) active.setStyle(STYLE_ADMIN_ACTIVE);
    }

    // ══════════════════════════════════════
    // NAVIGATION SIDEBAR GAUCHE
    // ══════════════════════════════════════

    private void navigateTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) (btnNavDashboard != null ? btnNavDashboard.getScene().getWindow()
                    : adminPostsContainer.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("❌ Erreur navigation vers " + fxmlPath + " : " + e.getMessage());
        }
    }

    @FXML
    private void handleNavDashboard() {
        navigateTo("/fxml/Dashboard.fxml");
    }

    @FXML
    private void handleNavUserManagement() {
        navigateTo("/fxml/UserManagement.fxml");
    }

    @FXML
    private void handleNavPlayers() {
        navigateTo("/fxml/Players.fxml");
    }

    @FXML
    private void handleNavMatches() {
        navigateTo("/fxml/Matches.fxml");
    }

    @FXML
    private void handleNavTeams() {
        navigateTo("/fxml/Teams.fxml");
    }

    @FXML
    private void handleNavTournaments() {
        navigateTo("/fxml/Tournaments.fxml");
    }

    @FXML
    private void handleNavCommunityHub() {
        // Déjà sur cette page, ne rien faire
    }

    @FXML
    private void handleNavProductShop() {
        navigateTo("/fxml/ProductShop.fxml");
    }

    @FXML
    private void handleNavSponsor() {
        navigateTo("/fxml/Sponsor.fxml");
    }

    @FXML
    private void handleNavReports() {
        navigateTo("/fxml/Reports.fxml");
    }

    @FXML
    private void handleNavSettings() {
        navigateTo("/fxml/Settings.fxml");
    }

    @FXML
    private void handleNavProfile() {
        navigateTo("/fxml/Profile.fxml");
    }

    // ══════════════════════════════════════
    // CHARGER POSTS DEPUIS DB
    // ══════════════════════════════════════
    private void loadPostsFromDB() {
        allPosts = service.getAllPosts();
        updateStats();
        refreshAdminView();
    }

    // ══════════════════════════════════════
    // MEMBRES DANS LA SIDEBAR
    // ══════════════════════════════════════
    private void loadAdminMembers() {
        if (adminMembersContainer == null) return;
        adminMembersContainer.getChildren().clear();
        List<String> members = service.getAllMemberNames();
        for (String name : members) {
            Label lbl = new Label("👤  " + name);
            lbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-padding: 2 0;");
            adminMembersContainer.getChildren().add(lbl);
        }
    }

    // ══════════════════════════════════════
    // STATS
    // ══════════════════════════════════════
    private void updateStats() {
        if (totalPostsLabel != null) totalPostsLabel.setText(String.valueOf(allPosts.size()));
        long signales = countSignaledPosts();
        if (totalSignalesLabel != null) totalSignalesLabel.setText(String.valueOf(signales));
    }

    private long countSignaledPosts() {
        String sql = "SELECT COUNT(*) FROM reaction WHERE Type LIKE 'signale_%'";
        try {
            Connection conn = MyDataBase.getInstance();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            System.out.println("❌ Erreur countSignaled : " + e.getMessage());
        }
        return 0;
    }

    // ══════════════════════════════════════
    // ONGLETS
    // ══════════════════════════════════════
    @FXML
    private void handleTousLesPosts() {
        currentTab = "tous";
        setActiveAdminButton(btnTousLesPosts);
        loadPostsFromDB();
    }

    @FXML
    private void handlePostsSignales() {
        currentTab = "signales";
        setActiveAdminButton(null);
        loadSignaledPosts();
    }

    @FXML
    private void handleUsersSignales() {
        currentTab = "users";
        setActiveAdminButton(btnUsersSignales);
        loadSignaledUsers();
    }

    // ══════════════════════════════════════
    // RAFRAICHIR VUE ADMIN
    // ══════════════════════════════════════
    private void refreshAdminView() {
        adminPostsContainer.getChildren().clear();
        if (allPosts.isEmpty()) {
            Label empty = new Label("📭 Aucun post disponible");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px; -fx-padding: 20;");
            adminPostsContainer.getChildren().add(empty);
            return;
        }
        for (Post post : allPosts) adminPostsContainer.getChildren().add(buildAdminPostCard(post));
    }

    // ══════════════════════════════════════
    // POSTS SIGNALÉS
    // ══════════════════════════════════════
    private void loadSignaledPosts() {
        adminPostsContainer.getChildren().clear();
        String sql = "SELECT DISTINCT p.Id, p.auteurId, u.nom, p.Titre, " +
                "p.Contenue, p.Type, p.DatePub, p.DatePub AS DateCreation " +
                "FROM post p LEFT JOIN user u ON p.auteurId = u.Id " +
                "INNER JOIN reaction r ON r.Type LIKE CONCAT('signale_post_', p.Id) " +
                "ORDER BY p.DatePub DESC";
        try {
            Connection conn = MyDataBase.getInstance();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            List<Post> signaled = new ArrayList<>();
            while (rs.next()) {
                LocalDate datePub = rs.getDate("DatePub") != null ? rs.getDate("DatePub").toLocalDate() : LocalDate.now();
                LocalDateTime createdAt = rs.getTimestamp("DateCreation") != null ? rs.getTimestamp("DateCreation").toLocalDateTime() : datePub.atStartOfDay();
                Post post = new Post(rs.getInt("Id"), rs.getInt("auteurId"),
                        rs.getString("nom") != null ? rs.getString("nom") : "Anonyme",
                        rs.getString("Titre"), rs.getString("Contenue"), rs.getString("Type"), datePub, createdAt);
                service.loadReactions(post);
                service.loadComments(post);
                signaled.add(post);
                adminPostsContainer.getChildren().add(buildAdminPostCard(post));
            }
            if (signaled.isEmpty()) {
                Label empty = new Label("✅ Aucun post signalé !");
                empty.setStyle("-fx-text-fill: #3fb950; -fx-font-size: 14px; -fx-padding: 20;");
                adminPostsContainer.getChildren().add(empty);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur loadSignaled : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════
    // USERS SIGNALÉS
    // ══════════════════════════════════════
    private void loadSignaledUsers() {
        adminPostsContainer.getChildren().clear();
        String sql = "SELECT DISTINCT u.Id, u.nom, u.email, COUNT(r.Id) as nb_signalements " +
                "FROM user u INNER JOIN reaction r ON r.Type LIKE CONCAT('signale_user_', u.Id) " +
                "GROUP BY u.Id ORDER BY nb_signalements DESC";
        try {
            Connection conn = MyDataBase.getInstance();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            boolean found = false;
            while (rs.next()) {
                found = true;
                adminPostsContainer.getChildren().add(buildUserSignaledCard(
                        rs.getInt("Id"), rs.getString("nom"), rs.getString("email"), rs.getInt("nb_signalements")));
            }
            if (!found) {
                Label empty = new Label("✅ Aucun utilisateur signalé !");
                empty.setStyle("-fx-text-fill: #3fb950; -fx-font-size: 14px; -fx-padding: 20;");
                adminPostsContainer.getChildren().add(empty);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur loadSignaledUsers : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════
    // CARD POST ADMIN
    // ══════════════════════════════════════
    private VBox buildAdminPostCard(Post post) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #161b22; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #30363d; -fx-border-radius: 10; -fx-border-width: 1;");

        HBox header = new HBox(10);
        header.setStyle("-fx-alignment: CENTER_LEFT;");

        Label author = new Label("👤 " + post.getAuthor());
        author.setStyle("-fx-text-fill: #1f6feb; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label date = new Label("🕐 " + post.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        date.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");
        Label idLabel = new Label("ID: " + post.getId());
        idLabel.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 10px; -fx-background-color: #21262d; -fx-padding: 2 6; -fx-background-radius: 4;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnSignaler = new Button("🚨 Signaler user");
        btnSignaler.setStyle("-fx-background-color: #f39c1222; -fx-text-fill: #f39c12; -fx-font-size: 11px; -fx-border-color: #f39c12; -fx-border-radius: 4; -fx-border-width: 1; -fx-padding: 3 8;");
        btnSignaler.setOnAction(e -> signalerUser(post));

        Button btnBloquer = new Button("🔨 Bloquer");
        btnBloquer.setStyle("-fx-background-color: #e74c3c22; -fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-border-color: #e74c3c; -fx-border-radius: 4; -fx-border-width: 1; -fx-padding: 3 8;");
        btnBloquer.setOnAction(e -> bloquerUser(post));

        Button btnDelete = new Button("🗑 Supprimer");
        btnDelete.setStyle("-fx-background-color: #e74c3c22; -fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-border-color: #e74c3c; -fx-border-radius: 4; -fx-border-width: 1; -fx-padding: 3 8;");
        btnDelete.setOnAction(e -> {
            if (showStyledConfirm("Supprimer le post", "Supprimer le post de " + post.getAuthor() + " ?")) {
                service.deletePost(post.getId());
                allPosts.remove(post);
                adminPostsContainer.getChildren().remove(card);
                updateStats();
            }
        });

        header.getChildren().addAll(idLabel, author, spacer, date, btnSignaler, btnBloquer, btnDelete);

        Label content = new Label(post.getContent());
        content.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px;");
        content.setWrapText(true);

        HBox stats = new HBox(15);
        stats.setStyle("-fx-padding: 5 0 0 0;");
        Label likesLbl = new Label("👍 " + post.getLikes() + " likes");
        likesLbl.setStyle("-fx-text-fill: #1f6feb; -fx-font-size: 12px;");
        Label dislikesLbl = new Label("👎 " + post.getDislikes() + " dislikes");
        dislikesLbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        Label commentsLbl = new Label("💬 " + post.getComments().size() + " commentaires");
        commentsLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
        stats.getChildren().addAll(likesLbl, dislikesLbl, commentsLbl);

        VBox commentsBox = new VBox(5);
        commentsBox.setStyle("-fx-padding: 5 0 0 0;");
        if (!post.getComments().isEmpty()) {
            commentsBox.getChildren().add(new Separator());
            for (Comment comment : post.getComments())
                commentsBox.getChildren().add(buildAdminCommentRow(comment, post, commentsBox, commentsLbl));
        }

        card.getChildren().addAll(header, content, stats, commentsBox);
        return card;
    }

    private HBox buildAdminCommentRow(Comment comment, Post post, VBox commentsBox, Label commentsLbl) {
        HBox row = new HBox(10);
        row.setStyle("-fx-background-color: #0d1117; -fx-padding: 8 10; -fx-background-radius: 5; -fx-border-color: #30363d; -fx-border-radius: 5; -fx-border-width: 1;");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label authorLbl = new Label("👤 " + comment.getAuthor() + " :");
        authorLbl.setStyle("-fx-text-fill: #1f6feb; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label textLbl = new Label(comment.getContent());
        textLbl.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");
        textLbl.setWrapText(true);
        HBox.setHgrow(textLbl, Priority.ALWAYS);
        Button btnDeleteComment = new Button("🗑");
        btnDeleteComment.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-padding: 2 6;");
        btnDeleteComment.setOnAction(e -> {
            if (showStyledConfirm("Supprimer le commentaire", "Supprimer ce commentaire de " + comment.getAuthor() + " ?")) {
                deleteCommentFromDB(comment.getId());
                post.getComments().remove(comment);
                commentsBox.getChildren().remove(row);
                commentsLbl.setText("💬 " + post.getComments().size() + " commentaires");
            }
        });
        row.getChildren().addAll(authorLbl, textLbl, btnDeleteComment);
        return row;
    }

    private void deleteCommentFromDB(int commentId) {
        String sql = "DELETE FROM commentaire WHERE id = ?";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, commentId);
            stmt.executeUpdate();
            System.out.println("✅ Commentaire supprimé de la DB : " + commentId);
        } catch (SQLException e) {
            System.out.println("❌ Erreur deleteComment : " + e.getMessage());
        }
    }

    private VBox buildUserSignaledCard(int userId, String nom, String email, int nbSignalements) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #161b22; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #f39c12; -fx-border-radius: 10; -fx-border-width: 1;");
        HBox header = new HBox(10);
        header.setStyle("-fx-alignment: CENTER_LEFT;");
        Label userLabel = new Label("👤 " + nom);
        userLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label emailLabel = new Label(email);
        emailLabel.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label signalCount = new Label("🚨 " + nbSignalements + " signalement(s)");
        signalCount.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold; -fx-font-size: 12px;");
        Button btnBan = new Button("🔨 Bannir");
        btnBan.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 4; -fx-padding: 4 10;");
        btnBan.setOnAction(e -> {
            if (showStyledConfirm("Bannir l'utilisateur", "Bannir " + nom + " ?"))
                showStyledInfo("✅ Action", nom + " a été banni !");
        });
        header.getChildren().addAll(userLabel, spacer, emailLabel, signalCount, btnBan);
        card.getChildren().add(header);
        return card;
    }

    private void signalerUser(Post post) {
        if (showStyledConfirm("Signaler l'utilisateur",
                "Signaler l'utilisateur " + post.getAuthor() + " pour ce post ?\n\n\"" + post.getContent() + "\"")) {
            String sql = "INSERT INTO reaction (userid, Type) VALUES (1, ?)";
            try {
                Connection conn = MyDataBase.getInstance();
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, "signale_user_" + post.getAuteurId());
                stmt.executeUpdate();
                updateStats();
                showStyledInfo("✅ Signalement", "L'utilisateur " + post.getAuthor() + " a été signalé !");
            } catch (SQLException e) {
                System.out.println("❌ Erreur signalement : " + e.getMessage());
            }
        }
    }

    private void bloquerUser(Post post) {
        if (showStyledConfirm("Bloquer l'utilisateur",
                "Bloquer " + post.getAuthor() + " ?\n\n\"" + post.getContent() + "\"")) {
            String sql = "UPDATE user SET Status = 'bloque' WHERE Id = ?";
            try {
                Connection conn = MyDataBase.getInstance();
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, post.getAuteurId());
                stmt.executeUpdate();
                showStyledInfo("🔨 Action", "L'utilisateur " + post.getAuthor() + " a été bloqué !");
            } catch (SQLException e) {
                System.out.println("❌ Erreur bloquerUser : " + e.getMessage());
            }
        }
    }

    private void filterAdminPosts(String keyword) {
        adminPostsContainer.getChildren().clear();
        if (keyword.isEmpty()) { refreshAdminView(); return; }
        boolean found = false;
        for (Post post : allPosts) {
            if (post.getContent().toLowerCase().contains(keyword) || post.getAuthor().toLowerCase().contains(keyword)) {
                adminPostsContainer.getChildren().add(buildAdminPostCard(post));
                found = true;
            }
        }
        if (!found) {
            Label empty = new Label("🔍 Aucun résultat pour \"" + keyword + "\"");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px; -fx-padding: 20;");
            adminPostsContainer.getChildren().add(empty);
        }
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════
    private boolean showStyledConfirm(String title, String message) {
        Stage stage = new Stage(); stage.initStyle(StageStyle.UNDECORATED); stage.setAlwaysOnTop(true);
        VBox root = new VBox(); root.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-width: 1;");
        Label titleLbl = new Label(title); titleLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-color: #0d1117; -fx-border-color: #30363d; -fx-border-width: 0 0 1 0;");
        Label msg = new Label(message); msg.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-padding: 20 15;"); msg.setWrapText(true);
        HBox buttons = new HBox(10); buttons.setStyle("-fx-padding: 10 15 15 15; -fx-alignment: CENTER_RIGHT; -fx-background-color: #161b22;");
        Button btnOk = new Button("OK"); btnOk.setStyle("-fx-background-color: #1f6feb; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        Button btnCancel = new Button("Annuler"); btnCancel.setStyle("-fx-background-color: #21262d; -fx-text-fill: #ffffff; -fx-background-radius: 6; -fx-padding: 8 20; -fx-border-color: #30363d; -fx-border-radius: 6; -fx-cursor: hand;");
        final boolean[] result = {false};
        btnOk.setOnAction(e -> { result[0] = true; stage.close(); }); btnCancel.setOnAction(e -> stage.close());
        buttons.getChildren().addAll(btnCancel, btnOk); root.getChildren().addAll(titleLbl, msg, buttons);
        stage.setScene(new Scene(root, 400, 200)); stage.showAndWait();
        return result[0];
    }

    private void showStyledInfo(String title, String message) {
        Stage stage = new Stage(); stage.initStyle(StageStyle.UNDECORATED); stage.setAlwaysOnTop(true);
        VBox root = new VBox(); root.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-width: 1;");
        Label titleLbl = new Label(title); titleLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-color: #0d1117; -fx-border-color: #30363d; -fx-border-width: 0 0 1 0;");
        Label msg = new Label(message); msg.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-padding: 20 15;"); msg.setWrapText(true);
        HBox buttons = new HBox(10); buttons.setStyle("-fx-padding: 10 15 15 15; -fx-alignment: CENTER_RIGHT; -fx-background-color: #161b22;");
        Button btnOk = new Button("OK"); btnOk.setStyle("-fx-background-color: #1f6feb; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        btnOk.setOnAction(e -> stage.close());
        buttons.getChildren().add(btnOk); root.getChildren().addAll(titleLbl, msg, buttons);
        stage.setScene(new Scene(root, 400, 180)); stage.showAndWait();
    }
}