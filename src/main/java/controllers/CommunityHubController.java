package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Comment;
import models.Message;
import models.Notification;
import models.Post;
import services.CommunityService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommunityHubController {

    // ── Posts ──
    @FXML private TextArea postInput;
    @FXML private VBox postsContainer;
    @FXML private Label charCount;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField searchField;
    @FXML private TextField searchDateField;
    @FXML private Label postCountLabel;
    @FXML private Label memberCountLabel;
    @FXML private VBox topMembersContainer;

    // ── Messages (FXML) ──
    @FXML private VBox messagesContainer;
    @FXML private VBox messagesList;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private TextField messageInputField;
    @FXML private Label activeRoomLabel;
    @FXML private Button btnGeneral;
    @FXML private Button btnTournois;
    @FXML private Button btnAmis;
    @FXML private HBox messageInputBar;

    // ── Notifications ──
    @FXML private Label bellIcon;
    @FXML private Label notificationBadge;

    // ── Boutons de navigation feed central ──
    @FXML private Button btnFeed;
    @FXML private Button btnTendances;
    @FXML private Button btnTopPosts;
    @FXML private Button btnMesPosts;
    @FXML private Button btnChat;

    // ── Boutons de navigation sidebar gauche ──
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavPlayers;
    @FXML private Button btnNavMatches;
    @FXML private Button btnNavTeams;
    @FXML private Button btnNavTournaments;
    @FXML private Button btnNavCommunityHub;
    @FXML private Button btnNavProductShop;
    @FXML private Button btnNavSponsor;

    private final CommunityService service = new CommunityService();
    private final String currentUser = "Anaghim";
    private final int currentUserId = 1;

    private final List<Post> allPosts = new ArrayList<>();
    private final List<VBox> allCards = new ArrayList<>();
    private String currentTab = "recents";
    private String currentRoom = "général";

    private Button activeNavButton;

    @FXML
    public void initialize() {
        service.initMessageTable();
        service.initCommentTable();
        service.initNotificationTable();

        reloadPostsFromDB();
        refreshView();

        postInput.textProperty().addListener((obs, oldVal, newVal) -> {
            int count = newVal.length();
            charCount.setText(count + " / 280");
            if (count > 280) postInput.setText(oldVal);
        });

        updateMemberCount();
        loadTopMembers();

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) ->
                    filterPosts(newVal.trim().toLowerCase()));
        }

        if (messagesList != null) {
            messagesList.getChildren().add(buildSystemMessage("Bienvenue dans #général ! 👋"));
        }

        setActiveNavButton(btnFeed);

        refreshNotificationBadge();
        if (bellIcon != null) {
            bellIcon.setOnMouseClicked(e -> showNotificationsPopup());
        }
    }

    private void reloadPostsFromDB() {
        allPosts.clear();
        allCards.clear();
        List<Post> dbPosts = service.getAllPosts();
        for (Post post : dbPosts) {
            allPosts.add(post);
            allCards.add(buildPostCard(post));
        }
        updatePostCount();
    }

    // ══════════════════════════════════════
    // NAVIGATION SIDEBAR GAUCHE
    // ══════════════════════════════════════

    private void navigateTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) (btnNavDashboard != null ? btnNavDashboard.getScene().getWindow()
                    : btnFeed.getScene().getWindow());
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

    // ══════════════════════════════════════
    // GESTION DES BOUTONS DE NAVIGATION FEED (coloration bleue)
    // ══════════════════════════════════════

    private void resetAllNavButtons() {
        Button[] navButtons = {btnFeed, btnTendances, btnTopPosts, btnMesPosts, btnChat};
        String inactiveStyle = "-fx-background-color: #21262d; " +
                "-fx-text-fill: #8b949e; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 8 18; " +
                "-fx-border-width: 0; " +
                "-fx-cursor: hand;";
        for (Button btn : navButtons) {
            if (btn != null) btn.setStyle(inactiveStyle);
        }
    }

    private void setActiveNavButton(Button button) {
        resetAllNavButtons();
        if (button != null) {
            String activeStyle = "-fx-background-color: #1f6feb; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 13px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 8 18; " +
                    "-fx-border-width: 0; " +
                    "-fx-cursor: hand;";
            button.setStyle(activeStyle);
            activeNavButton = button;
        }
    }

    // ══════════════════════════════════════
    // PUBLIER
    // ══════════════════════════════════════
    @FXML
    private void handlePublish() {
        String content = postInput.getText().trim();
        if (content.isEmpty()) {
            showStyledAlert("⚠️ Champ vide", "Écris quelque chose avant de publier !");
            return;
        }
        Post post = service.createPost(currentUser, content);
        postInput.clear();
        if (!post.isHidden()) {
            reloadPostsFromDB();
            currentTab = "mesposts";
            setActiveNavButton(btnMesPosts);
            refreshView();
        } else {
            showStyledAlert("⚠️ Post masqué",
                    "Ton post contient des mots inappropriés et a été masqué.");
        }
    }

    // ══════════════════════════════════════
    // COMPTEURS
    // ══════════════════════════════════════
    private void updatePostCount() {
        if (postCountLabel != null) postCountLabel.setText(String.valueOf(allPosts.size()));
    }

    private void updateMemberCount() {
        if (memberCountLabel != null) memberCountLabel.setText(String.valueOf(service.countMembers()));
    }

    private void loadTopMembers() {
        if (topMembersContainer == null) return;
        List<String> members = service.getAllMemberNames();
        String[] emojis = {"👤", "👤", "👤", "👤", "👤"};
        for (int i = 0; i < Math.min(members.size(), 8); i++) {
            HBox row = new HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label icon = new Label(i < emojis.length ? emojis[i] : "👤");
            icon.setStyle("-fx-font-size: 14px;");
            Label name = new Label(members.get(i));
            name.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12px;");
            row.getChildren().addAll(icon, name);
            topMembersContainer.getChildren().add(row);
        }
    }

    // ══════════════════════════════════════
    // UPDATE POST EN DB
    // ══════════════════════════════════════
    private void updatePostInDB(int postId, String newContent) {
        String sql = "UPDATE post SET Contenue = ?, Titre = ? WHERE Id = ?";
        try {
            Connection conn = org.example.MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newContent);
            stmt.setString(2, newContent.substring(0, Math.min(newContent.length(), 255)));
            stmt.setInt(3, postId);
            stmt.executeUpdate();
            System.out.println("✅ Post modifié en DB : " + postId);
        } catch (SQLException e) {
            System.out.println("❌ Erreur updatePost : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════
    // ONGLETS FEED
    // ══════════════════════════════════════
    @FXML
    private void handleFeed() {
        reloadPostsFromDB();
        currentTab = "recents";
        setActiveNavButton(btnFeed);
        refreshView();
    }

    @FXML
    private void handleTendances() {
        reloadPostsFromDB();
        currentTab = "tendances";
        setActiveNavButton(btnTendances);
        refreshView();
    }

    @FXML
    private void handleTopPosts() {
        reloadPostsFromDB();
        currentTab = "topposts";
        setActiveNavButton(btnTopPosts);
        refreshView();
    }

    @FXML
    private void handleMesPosts() {
        reloadPostsFromDB();
        currentTab = "mesposts";
        setActiveNavButton(btnMesPosts);
        refreshView();
    }

    @FXML
    private void handleChat() {
        currentTab = "chat";
        setActiveNavButton(btnChat);
        refreshView();
    }

    private void showMessageInputBar() {
        if (messageInputBar != null) {
            messageInputBar.setVisible(true);
            messageInputBar.setManaged(true);
        }
        if (messageInputField != null) {
            messageInputField.setPromptText("Écrire un message...");
            Platform.runLater(() -> messageInputField.requestFocus());
        }
        if (messagesScrollPane != null) {
            Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
        }
    }

    // ══════════════════════════════════════
    // RAFRAICHIR LA VUE
    // ══════════════════════════════════════
    private void refreshView() {
        boolean isMessages = currentTab.equals("messages");

        messagesContainer.setVisible(isMessages);
        messagesContainer.setManaged(isMessages);
        scrollPane.setVisible(!isMessages);
        scrollPane.setManaged(!isMessages);
        messageInputBar.setVisible(isMessages);
        messageInputBar.setManaged(isMessages);

        if (isMessages) {
            showMessageInputBar();
            return;
        }

        postsContainer.getChildren().clear();

        switch (currentTab) {
            case "recents" -> {
                boolean feedFound = false;
                for (int i = 0; i < allPosts.size(); i++) {
                    Post p = allPosts.get(i);
                    if (p.getAuteurId() != currentUserId && !p.getAuthor().equalsIgnoreCase(currentUser)) {
                        postsContainer.getChildren().add(allCards.get(i));
                        feedFound = true;
                    }
                }
                if (!feedFound)
                    postsContainer.getChildren().add(buildEmptyLabel("📭 Aucun post de la communauté pour le moment !"));
            }
            case "populaires" -> {
                List<Integer> indices = getSortedIndicesByLikes();
                boolean popFound = false;
                for (int i : indices) {
                    Post p = allPosts.get(i);
                    if (p.getAuteurId() != currentUserId && !p.getAuthor().equalsIgnoreCase(currentUser)) {
                        postsContainer.getChildren().add(allCards.get(i));
                        popFound = true;
                    }
                }
                if (!popFound)
                    postsContainer.getChildren().add(buildEmptyLabel("📭 Aucun post populaire pour le moment !"));
            }
            case "nonlus" -> {
                boolean nonLusFound = false;
                for (int i = 0; i < allPosts.size(); i++) {
                    Post p = allPosts.get(i);
                    if (p.getAuteurId() != currentUserId && !p.getAuthor().equalsIgnoreCase(currentUser)
                            && p.getLikes() == 0 && p.getComments().isEmpty()) {
                        postsContainer.getChildren().add(allCards.get(i));
                        nonLusFound = true;
                    }
                }
                if (!nonLusFound)
                    postsContainer.getChildren().add(buildEmptyLabel("✅ Aucun post non lu !"));
            }
            case "tendances" -> postsContainer.getChildren().add(buildTendancesUI());
            case "topposts"  -> postsContainer.getChildren().add(buildTopPostsUI());
            case "mesposts"  -> {
                boolean found = false;
                for (int i = 0; i < allPosts.size(); i++) {
                    Post p = allPosts.get(i);
                    if (p.getAuteurId() == currentUserId || p.getAuthor().equalsIgnoreCase(currentUser)) {
                        postsContainer.getChildren().add(allCards.get(i));
                        found = true;
                    }
                }
                if (!found)
                    postsContainer.getChildren().add(buildEmptyLabel("📝 Tu n'as pas encore publié de post !"));
            }
            case "chat" -> postsContainer.getChildren().add(buildChatRoomsUI());
        }
    }

    private VBox buildChatRoomsUI() {
        VBox box = new VBox(12);
        box.setStyle("-fx-background-color: #161b22; -fx-padding: 20;" +
                "-fx-background-radius: 10; -fx-border-color: #30363d;" +
                "-fx-border-radius: 10; -fx-border-width: 1;");

        Label title = new Label("💬 Salons de discussion");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: bold;");
        box.getChildren().addAll(title, new Separator());

        String[][] rooms = {
                {"général", "🌐", "Discussions générales entre membres"},
                {"tournois", "🏆", "Échange autour des tournois"},
                {"amis", "🤝", "Salon privé entre amis"}
        };

        for (String[] room : rooms) {
            VBox card = new VBox(5);
            card.setStyle("-fx-background-color: #0d1117; -fx-padding: 15; -fx-background-radius: 8;" +
                    "-fx-border-color: #30363d; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;");
            card.setOnMouseClicked(e -> {
                currentRoom = room[0];
                currentTab = "messages";
                switchRoom(room[0], "# " + room[0], null);
                refreshView();
            });

            Label name = new Label(room[1] + "  " + room[0]);
            name.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 15px; -fx-font-weight: bold;");
            Label desc = new Label(room[2]);
            desc.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
            card.getChildren().addAll(name, desc);
            box.getChildren().add(card);
        }
        return box;
    }

    // ══════════════════════════════════════
    // GESTION DES SALONS
    // ══════════════════════════════════════
    @FXML
    private void handleRoomGeneral() {
        switchRoom("général", "# général", btnGeneral);
    }

    @FXML
    private void handleRoomTournois() {
        switchRoom("tournois", "# tournois", btnTournois);
    }

    @FXML
    private void handleRoomAmis() {
        switchRoom("amis", "👥 amis", btnAmis);
    }

    private void switchRoom(String roomName, String roomLabel, Button activeBtn) {
        currentRoom = roomName;
        if (activeRoomLabel != null) activeRoomLabel.setText(roomLabel);
        if (messageInputField != null) {
            messageInputField.setPromptText("Écrire un message dans " + roomLabel + "...");
            messageInputField.clear();
        }
        if (messagesList != null) {
            messagesList.getChildren().clear();
            messagesList.getChildren().add(buildSystemMessage("Bienvenue dans " + roomLabel + " ! 👋"));
            List<Message> dbMessages = service.loadMessagesByRoom(roomName);
            for (Message msg : dbMessages) {
                HBox bubble = buildMessageBubble(msg.getAuthor(), msg.getContent(), messagesList, activeRoomLabel);
                messagesList.getChildren().add(bubble);
            }
        }

        String styleNormal = "-fx-background-color: transparent; -fx-text-fill: #8b949e;" +
                "-fx-font-size: 13px; -fx-background-radius: 6; -fx-alignment: CENTER_LEFT;" +
                "-fx-padding: 8 10; -fx-border-width: 0; -fx-cursor: hand;";
        String styleActive = "-fx-background-color: #1f6feb22; -fx-text-fill: #1f6feb;" +
                "-fx-font-size: 13px; -fx-background-radius: 6; -fx-alignment: CENTER_LEFT;" +
                "-fx-padding: 8 10; -fx-border-width: 0; -fx-cursor: hand;";

        if (btnGeneral  != null) btnGeneral.setStyle(styleNormal);
        if (btnTournois != null) btnTournois.setStyle(styleNormal);
        if (btnAmis     != null) btnAmis.setStyle(styleNormal);
        if (activeBtn   != null) activeBtn.setStyle(styleActive);

        showMessageInputBar();
        Platform.runLater(() -> { if (messagesScrollPane != null) messagesScrollPane.setVvalue(1.0); });
    }

    // ══════════════════════════════════════
    // ENVOYER UN MESSAGE
    // ══════════════════════════════════════
    @FXML
    private void handleSendMessage() {
        if (messageInputField == null || messagesList == null) return;
        String text = messageInputField.getText().trim();
        if (text.isEmpty()) return;
        Message saved = service.saveMessage(currentRoom, currentUser, text);
        HBox messageBubble = buildMessageBubble(saved.getAuthor(), saved.getContent(), messagesList, activeRoomLabel);
        messagesList.getChildren().add(messageBubble);
        messageInputField.clear();
        messageInputField.requestFocus();
        Platform.runLater(() -> { if (messagesScrollPane != null) messagesScrollPane.setVvalue(1.0); });
    }

    // ══════════════════════════════════════
    // RECHERCHE PAR MOT CLÉ
    // ══════════════════════════════════════
    private void filterPosts(String keyword) {
        postsContainer.getChildren().clear();
        if (keyword.isEmpty()) { refreshView(); return; }
        boolean found = false;
        for (int i = 0; i < allPosts.size(); i++) {
            if (allPosts.get(i).getContent().toLowerCase().contains(keyword) ||
                    allPosts.get(i).getAuthor().toLowerCase().contains(keyword)) {
                postsContainer.getChildren().add(allCards.get(i));
                found = true;
            }
        }
        if (!found)
            postsContainer.getChildren().add(buildEmptyLabel("🔍 Aucun résultat pour \"" + keyword + "\""));
    }

    // ══════════════════════════════════════
    // RECHERCHE PAR DATE
    // ══════════════════════════════════════
    @FXML
    private void handleSearchByDate() {
        String dateText = searchDateField.getText().trim();
        if (dateText.isEmpty()) { showStyledAlert("⚠️ Champ vide", "Entrez une date au format AAAA-MM-JJ"); return; }
        if (!dateText.matches("\\d{4}-\\d{2}-\\d{2}")) {
            showStyledAlert("⚠️ Format incorrect", "La date doit être au format AAAA-MM-JJ\nExemple : 2026-05-11");
            return;
        }
        try {
            LocalDate searchDate = LocalDate.parse(dateText);
            postsContainer.getChildren().clear();
            boolean found = false;
            for (int i = 0; i < allPosts.size(); i++) {
                Post p = allPosts.get(i);
                if (p.getCreatedAt().toLocalDate().equals(searchDate)) {
                    postsContainer.getChildren().add(allCards.get(i));
                    found = true;
                }
            }
            if (!found) {
                VBox emptyBox = new VBox(10);
                emptyBox.setStyle("-fx-background-color: #161b22; -fx-padding: 20; -fx-background-radius: 10;" +
                        "-fx-border-color: #30363d; -fx-border-radius: 10; -fx-border-width: 1; -fx-alignment: CENTER;");
                Label emptyLabel = new Label("🔍 Aucun post trouvé pour le " + searchDate);
                emptyLabel.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
                Label hintLabel = new Label("Essaie une autre date");
                hintLabel.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
                emptyBox.getChildren().addAll(emptyLabel, hintLabel);
                postsContainer.getChildren().add(emptyBox);
            }
        } catch (Exception e) {
            showStyledAlert("⚠️ Date invalide", "La date entrée n'est pas valide.\nExemple : 2026-05-11");
        }
    }

    @FXML
    private void handleClearDateFilter() {
        if (searchDateField != null) searchDateField.clear();
        refreshView();
    }

    // ══════════════════════════════════════
    // CONSTRUIRE UNE CARD DE POST
    // ══════════════════════════════════════
    private VBox buildPostCard(Post post) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #161b22; -fx-padding: 15;" +
                "-fx-background-radius: 10; -fx-border-color: #30363d;" +
                "-fx-border-radius: 10; -fx-border-width: 1;");

        HBox header = new HBox(10);
        header.setStyle("-fx-alignment: CENTER_LEFT;");

        Label author = new Label("👤 " + post.getAuthor());
        author.setStyle("-fx-text-fill: #1f6feb; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label time = new Label("🕐 " + post.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        time.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label content = new Label(post.getContent());
        content.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px;");
        content.setWrapText(true);

        boolean isMyPost = post.getAuthor().equals(currentUser) || post.getAuteurId() == currentUserId;

        if (isMyPost) {
            Button btnEdit = new Button("✏ Modifier");
            btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: #1f6feb;" +
                    "-fx-font-size: 11px; -fx-cursor: hand; -fx-border-color: #1f6feb;" +
                    "-fx-border-radius: 4; -fx-border-width: 1; -fx-padding: 3 8;");
            btnEdit.setOnAction(e -> showStyledInput("Modifier le post", "Modifier ton post :", post.getContent()).ifPresent(newContent -> {
                updatePostInDB(post.getId(), newContent.trim());
                post.setContent(newContent.trim());
                content.setText(newContent.trim());
            }));

            Button btnDelete = new Button("🗑 Supprimer");
            btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c;" +
                    "-fx-font-size: 11px; -fx-cursor: hand; -fx-border-color: #e74c3c;" +
                    "-fx-border-radius: 4; -fx-border-width: 1; -fx-padding: 3 8;");
            btnDelete.setOnAction(e -> {
                if (showStyledConfirm("Supprimer", "Veux-tu vraiment supprimer ce post ?")) {
                    service.deletePost(post.getId());
                    int idx = allPosts.indexOf(post);
                    allPosts.remove(idx);
                    allCards.remove(idx);
                    refreshView();
                    updatePostCount();
                }
            });
            header.getChildren().addAll(author, spacer, time, btnEdit, btnDelete);
        } else {
            header.getChildren().addAll(author, spacer, time);
        }

        HBox actions = new HBox(10);
        actions.setStyle("-fx-padding: 5 0 0 0;");

        Label likeCount    = new Label("" + post.getLikes());
        likeCount.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
        Label dislikeCount = new Label("" + post.getDislikes());
        dislikeCount.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");

        String styleNormal   = "-fx-background-color: #21262d; -fx-text-fill: white;" +
                "-fx-background-radius: 5; -fx-border-color: #30363d;" +
                "-fx-border-radius: 5; -fx-border-width: 1; -fx-font-size: 12px; -fx-padding: 5 12;";
        String styleLiked    = "-fx-background-color: #1f6feb22; -fx-text-fill: #1f6feb;" +
                "-fx-background-radius: 5; -fx-border-color: #1f6feb;" +
                "-fx-border-radius: 5; -fx-border-width: 1; -fx-font-size: 12px; -fx-padding: 5 12;";
        String styleDisliked = "-fx-background-color: #e74c3c22; -fx-text-fill: #e74c3c;" +
                "-fx-background-radius: 5; -fx-border-color: #e74c3c;" +
                "-fx-border-radius: 5; -fx-border-width: 1; -fx-font-size: 12px; -fx-padding: 5 12;";

        Button btnLike    = new Button("👍 Like");
        Button btnDislike = new Button("👎 Dislike");
        btnLike.setStyle(styleNormal);
        btnDislike.setStyle(styleNormal);

        final boolean[] isLiked    = {false};
        final boolean[] isDisliked = {false};

        btnLike.setOnAction(e -> {
            if (isLiked[0]) {
                isLiked[0] = false; post.setLikes(post.getLikes() - 1);
                btnLike.setStyle(styleNormal); service.removeReaction(post.getId(), "like");
            } else {
                isLiked[0] = true; post.setLikes(post.getLikes() + 1);
                btnLike.setStyle(styleLiked); service.saveReaction(post.getId(), "like");
                if (isDisliked[0]) {
                    isDisliked[0] = false; post.setDislikes(post.getDislikes() - 1);
                    btnDislike.setStyle(styleNormal); dislikeCount.setText("" + post.getDislikes());
                    service.removeReaction(post.getId(), "dislike");
                }
            }
            likeCount.setText("" + post.getLikes());
        });

        btnDislike.setOnAction(e -> {
            if (isDisliked[0]) {
                isDisliked[0] = false; post.setDislikes(post.getDislikes() - 1);
                btnDislike.setStyle(styleNormal); service.removeReaction(post.getId(), "dislike");
            } else {
                isDisliked[0] = true; post.setDislikes(post.getDislikes() + 1);
                btnDislike.setStyle(styleDisliked); service.saveReaction(post.getId(), "dislike");
                if (isLiked[0]) {
                    isLiked[0] = false; post.setLikes(post.getLikes() - 1);
                    btnLike.setStyle(styleNormal); likeCount.setText("" + post.getLikes());
                    service.removeReaction(post.getId(), "like");
                }
            }
            dislikeCount.setText("" + post.getDislikes());
        });

        actions.getChildren().addAll(btnLike, likeCount, btnDislike, dislikeCount);

        Separator sep = new Separator();
        VBox commentsBox = new VBox(8);
        commentsBox.setStyle("-fx-padding: 5 0 0 0;");
        for (Comment c : post.getComments()) commentsBox.getChildren().add(buildCommentUI(c));

        HBox commentInput = new HBox(8);
        TextField commentField = new TextField();
        commentField.setPromptText("Ajouter un commentaire...");
        commentField.setStyle("-fx-control-inner-background: #0d1117; -fx-text-fill: white;" +
                "-fx-prompt-text-fill: #8b949e; -fx-background-radius: 5;" +
                "-fx-border-color: #30363d; -fx-border-radius: 5;" +
                "-fx-border-width: 1; -fx-padding: 7 10;");
        HBox.setHgrow(commentField, Priority.ALWAYS);

        Button btnComment = new Button("💬 Envoyer");
        btnComment.setStyle("-fx-background-color: #1f6feb; -fx-text-fill: white;" +
                "-fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 7 12;");
        btnComment.setOnAction(e -> {
            String text = commentField.getText().trim();
            if (text.isEmpty()) return;
            Comment comment = service.addComment(post, currentUser, text);
            commentField.clear();
            if (!comment.isHidden()) commentsBox.getChildren().add(buildCommentUI(comment));
            else showStyledAlert("⚠️ Commentaire masqué", "Ton commentaire contient des mots inappropriés.");
        });
        commentField.setOnAction(e -> btnComment.fire());
        commentInput.getChildren().addAll(commentField, btnComment);
        card.getChildren().addAll(header, content, actions, sep, commentsBox, commentInput);
        return card;
    }

    private HBox buildCommentUI(Comment comment) {
        HBox box = new HBox(8);
        box.setStyle("-fx-background-color: #0d1117; -fx-padding: 8 10;" +
                "-fx-background-radius: 5; -fx-border-color: #30363d;" +
                "-fx-border-radius: 5; -fx-border-width: 1;");
        Label author = new Label("👤 " + comment.getAuthor() + " :");
        author.setStyle("-fx-text-fill: #1f6feb; -fx-font-weight: bold; -fx-font-size: 12px;");
        Label text = new Label(comment.getContent());
        text.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12px;");
        text.setWrapText(true);
        HBox.setHgrow(text, Priority.ALWAYS);

        final int[] commentLikes = {0};
        final int[] commentDislikes = {0};
        final boolean[] liked = {false};
        final boolean[] disliked = {false};

        Button btnCommentLike = new Button("👍");
        btnCommentLike.setStyle("-fx-background-color: transparent; -fx-text-fill: #8b949e; -fx-font-size: 12px; -fx-padding: 2 6; -fx-cursor: hand;");
        Button btnCommentDislike = new Button("👎");
        btnCommentDislike.setStyle("-fx-background-color: transparent; -fx-text-fill: #8b949e; -fx-font-size: 12px; -fx-padding: 2 6; -fx-cursor: hand;");
        Label commentLikeCount = new Label("0");
        commentLikeCount.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");
        Label commentDislikeCount = new Label("0");
        commentDislikeCount.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");

        btnCommentLike.setOnAction(e -> {
            if (liked[0]) { liked[0] = false; commentLikes[0]--; btnCommentLike.setStyle("-fx-background-color: transparent; -fx-text-fill: #8b949e; -fx-font-size: 12px; -fx-padding: 2 6;"); }
            else { liked[0] = true; commentLikes[0]++; btnCommentLike.setStyle("-fx-background-color: transparent; -fx-text-fill: #1f6feb; -fx-font-size: 12px; -fx-padding: 2 6;");
                if (disliked[0]) { disliked[0] = false; commentDislikes[0]--; btnCommentDislike.setStyle("-fx-background-color: transparent; -fx-text-fill: #8b949e; -fx-font-size: 12px; -fx-padding: 2 6;"); } }
            commentLikeCount.setText("" + commentLikes[0]);
        });
        btnCommentDislike.setOnAction(e -> {
            if (disliked[0]) { disliked[0] = false; commentDislikes[0]--; btnCommentDislike.setStyle("-fx-background-color: transparent; -fx-text-fill: #8b949e; -fx-font-size: 12px; -fx-padding: 2 6;"); }
            else { disliked[0] = true; commentDislikes[0]++; btnCommentDislike.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-padding: 2 6;");
                if (liked[0]) { liked[0] = false; commentLikes[0]--; btnCommentLike.setStyle("-fx-background-color: transparent; -fx-text-fill: #8b949e; -fx-font-size: 12px; -fx-padding: 2 6;"); } }
            commentDislikeCount.setText("" + commentDislikes[0]);
        });
        box.getChildren().addAll(author, text, btnCommentLike, commentLikeCount, btnCommentDislike, commentDislikeCount);
        return box;
    }

    private HBox buildMessageBubble(String author, String text, VBox msgList, Label roomLabel) {
        HBox bubble = new HBox(10);
        bubble.setStyle("-fx-padding: 4 0;");
        bubble.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox msgContent = new VBox(2);
        HBox authorRow = new HBox(8);
        Label authorLbl = new Label("👤 " + author);
        authorLbl.setStyle("-fx-text-fill: #1f6feb; -fx-font-weight: bold; -fx-font-size: 12px;");
        Label timeLbl = new Label(java.time.LocalTime.now().toString().substring(0, 5));
        timeLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 10px;");
        Button editBtn = new Button("✏");
        editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #8b949e; -fx-font-size: 11px; -fx-padding: 0 4;");
        Label textLbl = new Label(text);
        textLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
        textLbl.setWrapText(true);
        editBtn.setOnAction(e -> showStyledInput("Modifier le message", "Modifier :", textLbl.getText()).ifPresent(newText -> textLbl.setText(newText.trim() + " (modifié)")));
        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-padding: 0 4;");
        deleteBtn.setOnAction(e -> msgList.getChildren().remove(bubble));
        authorRow.getChildren().addAll(authorLbl, timeLbl, editBtn, deleteBtn);
        msgContent.getChildren().addAll(authorRow, textLbl);
        HBox.setHgrow(msgContent, Priority.ALWAYS);
        bubble.getChildren().add(msgContent);
        return bubble;
    }

    private HBox buildSystemMessage(String text) {
        HBox box = new HBox();
        box.setAlignment(javafx.geometry.Pos.CENTER);
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px; -fx-background-color: #0d1117; -fx-padding: 4 12; -fx-background-radius: 10;");
        box.getChildren().add(lbl);
        return box;
    }

    private VBox buildTendancesUI() {
        VBox box = new VBox(12);
        box.setStyle("-fx-background-color: #161b22; -fx-padding: 20; -fx-background-radius: 10; -fx-border-color: #30363d; -fx-border-radius: 10; -fx-border-width: 1;");
        Label title = new Label("🔥 Tendance Gaming");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: bold;");
        box.getChildren().add(title);
        box.getChildren().add(new Separator());
        String[] noms = {"🏆 Cartixe Championship","🎮 League of Legends Cup","⚔️ Valorant Masters","🕹️ FIFA World Tour","🔫 CS2 Pro League","🏁 Rocket League GP","🎯 Fortnite Battle Royale"};
        for (int i = 0; i < noms.length; i++) {
            HBox row = new HBox(10);
            row.setStyle("-fx-background-color: #0d1117; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #30363d; -fx-border-radius: 8; -fx-border-width: 1;");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label rank = new Label("#" + (i + 1));
            rank.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px; -fx-min-width: 30;");
            Label name = new Label(noms[i]);
            name.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold;");
            row.getChildren().addAll(rank, name);
            box.getChildren().add(row);
        }
        return box;
    }

    private VBox buildTopPostsUI() {
        VBox box = new VBox(12);
        box.setStyle("-fx-background-color: #161b22; -fx-padding: 20; -fx-background-radius: 10; -fx-border-color: #30363d; -fx-border-radius: 10; -fx-border-width: 1;");
        Label title = new Label("⭐ Top Posts — Meilleurs posts");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label subtitle = new Label("Classement par nombre de likes");
        subtitle.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
        box.getChildren().addAll(title, subtitle, new Separator());
        if (allPosts.isEmpty()) { box.getChildren().add(buildEmptyLabel("Aucun post pour le moment !")); return box; }
        List<Integer> sorted = getSortedIndicesByLikes();
        List<Integer> withLikes = new ArrayList<>();
        for (int idx : sorted) if (allPosts.get(idx).getLikes() > 0) withLikes.add(idx);
        if (withLikes.isEmpty()) { box.getChildren().add(buildEmptyLabel("😔 Aucun post n'a encore reçu de like !")); return box; }
        String[] medals = {"🥇","🥈","🥉","4️⃣","5️⃣"};
        for (int i = 0; i < withLikes.size(); i++) {
            Post p = allPosts.get(withLikes.get(i));
            HBox row = new HBox(12);
            String borderColor = i == 0 ? "#FFD700" : i == 1 ? "#C0C0C0" : i == 2 ? "#CD7F32" : "#30363d";
            row.setStyle("-fx-background-color: #0d1117; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8; -fx-border-width: " + (i < 3 ? "2" : "1") + ";");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label medal = new Label(i < medals.length ? medals[i] : "#" + (i + 1));
            medal.setStyle("-fx-font-size: " + (i < 3 ? "18" : "13") + "px; -fx-min-width: 35;");
            VBox info = new VBox(4);
            Label authorLbl = new Label("👤 " + p.getAuthor());
            authorLbl.setStyle("-fx-text-fill: #1f6feb; -fx-font-size: 12px; -fx-font-weight: bold;");
            Label contentLbl = new Label(p.getContent().length() > 60 ? p.getContent().substring(0, 60) + "..." : p.getContent());
            contentLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
            Label statsLbl = new Label("💬 " + p.getComments().size() + " commentaire(s)  •  👎 " + p.getDislikes() + " dislike(s)");
            statsLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");
            info.getChildren().addAll(authorLbl, contentLbl, statsLbl);
            HBox.setHgrow(info, Priority.ALWAYS);
            VBox likesBox = new VBox(2);
            likesBox.setAlignment(javafx.geometry.Pos.CENTER);
            Label likesNum = new Label(String.valueOf(p.getLikes()));
            likesNum.setStyle("-fx-text-fill: #1f6feb; -fx-font-weight: bold; -fx-font-size: 20px;");
            Label likesText = new Label("👍 likes");
            likesText.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 10px;");
            likesBox.getChildren().addAll(likesNum, likesText);
            row.getChildren().addAll(medal, info, likesBox);
            box.getChildren().add(row);
        }
        return box;
    }

    private List<Integer> getSortedIndicesByLikes() {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < allPosts.size(); i++) indices.add(i);
        indices.sort((a, b) -> allPosts.get(b).getLikes() - allPosts.get(a).getLikes());
        return indices;
    }

    private Label buildEmptyLabel(String message) {
        Label lbl = new Label(message);
        lbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px; -fx-padding: 20;");
        return lbl;
    }

    private void showStyledAlert(String title, String message) {
        Stage stage = new Stage(); stage.initStyle(StageStyle.UNDECORATED); stage.setAlwaysOnTop(true);
        VBox root = new VBox(); root.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-width: 1;");
        Label titleLbl = new Label(title); titleLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-color: #0d1117; -fx-border-color: #30363d; -fx-border-width: 0 0 1 0;");
        Label msg = new Label(message); msg.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-padding: 20 15;"); msg.setWrapText(true);
        Button btnOk = new Button("OK"); btnOk.setStyle("-fx-background-color: #1f6feb; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        btnOk.setOnAction(e -> stage.close());
        HBox buttons = new HBox(10); buttons.setStyle("-fx-padding: 10 15 15 15; -fx-alignment: CENTER_RIGHT; -fx-background-color: #161b22;"); buttons.getChildren().add(btnOk);
        root.getChildren().addAll(titleLbl, msg, buttons);
        stage.setScene(new Scene(root, 380, 180)); stage.showAndWait();
    }

    private boolean showStyledConfirm(String title, String message) {
        Stage stage = new Stage(); stage.initStyle(StageStyle.UNDECORATED); stage.setAlwaysOnTop(true);
        VBox root = new VBox(); root.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-width: 1;");
        Label titleLbl = new Label(title); titleLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-color: #0d1117; -fx-border-color: #30363d; -fx-border-width: 0 0 1 0;");
        Label msg = new Label(message); msg.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-padding: 20 15;"); msg.setWrapText(true);
        Button btnOk = new Button("OK"); btnOk.setStyle("-fx-background-color: #1f6feb; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        Button btnCancel = new Button("Annuler"); btnCancel.setStyle("-fx-background-color: #21262d; -fx-text-fill: #ffffff; -fx-background-radius: 6; -fx-padding: 8 20; -fx-border-color: #30363d; -fx-border-radius: 6; -fx-cursor: hand;");
        final boolean[] result = {false};
        btnOk.setOnAction(e -> { result[0] = true; stage.close(); }); btnCancel.setOnAction(e -> stage.close());
        HBox buttons = new HBox(10); buttons.setStyle("-fx-padding: 10 15 15 15; -fx-alignment: CENTER_RIGHT; -fx-background-color: #161b22;"); buttons.getChildren().addAll(btnCancel, btnOk);
        root.getChildren().addAll(titleLbl, msg, buttons);
        stage.setScene(new Scene(root, 380, 200)); stage.showAndWait();
        return result[0];
    }

    private Optional<String> showStyledInput(String title, String message, String defaultValue) {
        Stage stage = new Stage(); stage.initStyle(StageStyle.UNDECORATED); stage.setAlwaysOnTop(true);
        VBox root = new VBox(); root.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-width: 1;");
        Label titleLbl = new Label(title); titleLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-color: #0d1117; -fx-border-color: #30363d; -fx-border-width: 0 0 1 0;");
        Label msg = new Label(message); msg.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-padding: 10 15 5 15;");
        TextField input = new TextField(defaultValue); input.setStyle("-fx-control-inner-background: #0d1117; -fx-text-fill: #ffffff; -fx-background-radius: 6; -fx-border-color: #30363d; -fx-border-radius: 6; -fx-border-width: 1; -fx-padding: 8 10;");
        VBox content = new VBox(5); content.setStyle("-fx-padding: 5 15 10 15; -fx-background-color: #161b22;"); content.getChildren().addAll(msg, input);
        Button btnOk = new Button("OK"); btnOk.setStyle("-fx-background-color: #1f6feb; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        Button btnCancel = new Button("Annuler"); btnCancel.setStyle("-fx-background-color: #21262d; -fx-text-fill: #ffffff; -fx-background-radius: 6; -fx-padding: 8 20; -fx-border-color: #30363d; -fx-border-radius: 6; -fx-cursor: hand;");
        final String[] result = {null};
        btnOk.setOnAction(e -> { result[0] = input.getText().trim(); stage.close(); }); btnCancel.setOnAction(e -> stage.close()); input.setOnAction(e -> { result[0] = input.getText().trim(); stage.close(); });
        HBox buttons = new HBox(10); buttons.setStyle("-fx-padding: 10 15 15 15; -fx-alignment: CENTER_RIGHT; -fx-background-color: #161b22;"); buttons.getChildren().addAll(btnCancel, btnOk);
        root.getChildren().addAll(titleLbl, content, buttons);
        stage.setScene(new Scene(root, 380, 220)); stage.showAndWait();
        return result[0] != null && !result[0].isEmpty() ? Optional.of(result[0]) : Optional.empty();
    }

    // ══════════════════════════════════════
    // NOTIFICATIONS
    // ══════════════════════════════════════
    private void refreshNotificationBadge() {
        int count = service.countUnreadNotifications(currentUserId);
        if (notificationBadge != null) {
            notificationBadge.setText(String.valueOf(count));
            notificationBadge.setVisible(count > 0);
        }
    }

    private void showNotificationsPopup() {
        List<Notification> notifs = service.getNotificationsByUser(currentUserId);
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initOwner(bellIcon.getScene().getWindow());
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-width: 1;");
        Label titleLbl = new Label("🔔 Notifications");
        titleLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");
        titleLbl.setPadding(new javafx.geometry.Insets(15, 15, 5, 15));
        VBox listBox = new VBox(4);
        listBox.setPadding(new javafx.geometry.Insets(5, 10, 10, 10));
        listBox.setMaxHeight(400);
        if (notifs.isEmpty()) {
            Label empty = new Label("Aucune notification");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
            empty.setPadding(new javafx.geometry.Insets(15, 0, 15, 5));
            listBox.getChildren().add(empty);
        } else {
            for (Notification n : notifs) {
                VBox item = new VBox(3);
                item.setStyle("-fx-background-color: " + (n.isLu() ? "#0d1117" : "#1f2d3d") + "; -fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #30363d; -fx-border-radius: 6;");
                item.setOnMouseClicked(e -> { if (!n.isLu()) { service.markNotificationAsRead(n.getId()); refreshNotificationBadge(); } stage.close(); });
                item.setCursor(javafx.scene.Cursor.HAND);
                Label msgLbl = new Label(n.getMessage()); msgLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12px;"); msgLbl.setWrapText(true);
                Label timeLbl = new Label(n.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm"))); timeLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 10px;");
                item.getChildren().addAll(msgLbl, timeLbl);
                listBox.getChildren().add(item);
            }
        }
        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setStyle("-fx-background: #161b22; -fx-background-color: #161b22; -fx-border-color: #30363d;");
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(Math.min(400, listBox.getChildren().size() * 70 + 20));
        root.getChildren().addAll(new VBox(titleLbl), scroll);
        stage.setScene(new Scene(root)); stage.setWidth(380); stage.setHeight(450);
        javafx.geometry.Bounds bounds = bellIcon.localToScreen(bellIcon.getBoundsInLocal());
        if (bounds != null) { stage.setX(bounds.getMaxX() - 380); stage.setY(bounds.getMaxY() + 5); }
        stage.focusedProperty().addListener((obs, oldVal, newVal) -> { if (!newVal) stage.close(); });
        stage.show();
        refreshNotificationBadge();
    }
}