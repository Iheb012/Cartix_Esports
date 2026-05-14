package Controllers;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Joueur;
import models.User;
import services.JoueurService;
import utils.MyDatabase;
import utils.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PlayersController {

    private int currentUserId;
    private Connection connection;

    // TableView et colonnes
    @FXML private TableView<Joueur> playersTable;
    @FXML private TableColumn<Joueur, Integer> colRank;
    @FXML private TableColumn<Joueur, String> colName;
    @FXML private TableColumn<Joueur, String> colTeam;
    @FXML private TableColumn<Joueur, String> colGame;
    @FXML private TableColumn<Joueur, Integer> colKills;
    @FXML private TableColumn<Joueur, Integer> colDeaths;
    @FXML private TableColumn<Joueur, Integer> colAssists;
    @FXML private TableColumn<Joueur, Double> colKDA;
    @FXML private TableColumn<Joueur, String> colWR;
    @FXML private TableColumn<Joueur, String> colStatus;
    @FXML private TableColumn<Joueur, Void> colFavorite;

    // Filtres
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterGameCombo;
    @FXML private ComboBox<String> filterRegionCombo;
    @FXML private ComboBox<String> filterRankCombo;

    // Amis
    @FXML private TextField searchFriendField;
    @FXML private VBox friendRequestsBox;
    @FXML private VBox friendsListBox;
    @FXML private Label friendCountLabel;
    @FXML private Label requestCountLabel;

    // Services
    private JoueurService joueurService;
    private List<Joueur> allJoueurs;
    private FilteredList<Joueur> filteredList;

    @FXML
    public void initialize() {


        connection = MyDatabase.getInstance().getConnection();
        joueurService = new JoueurService();

        User loggedUser = Session.getInstance().getCurrentUser();
        if (loggedUser != null) {
            try {
                Joueur joueur = joueurService.getByUserId(loggedUser.getId());
                if (joueur != null) {
                    currentUserId = joueur.getId();
                } else {
                    currentUserId = 1;
                }
            } catch (Exception e) {
                currentUserId = 1;
            }
        } else {
            currentUserId = 1;
        }

        System.out.println("✅ Initialisation PlayersController - User ID: " + currentUserId);

        // Configuration des colonnes
        colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colName.setCellValueFactory(new PropertyValueFactory<>("pseudo"));
        colTeam.setCellValueFactory(new PropertyValueFactory<>("equipeId"));
        colGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colKills.setCellValueFactory(new PropertyValueFactory<>("kills"));
        colDeaths.setCellValueFactory(new PropertyValueFactory<>("deaths"));
        colAssists.setCellValueFactory(new PropertyValueFactory<>("assists"));
        colKDA.setCellValueFactory(new PropertyValueFactory<>("kda"));
        colWR.setCellValueFactory(new PropertyValueFactory<>("winRate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Lignes alternées + hover
        playersTable.setRowFactory(tv -> new TableRow<Joueur>() {
            @Override
            protected void updateItem(Joueur p, boolean empty) {
                super.updateItem(p, empty);
                String base = (empty || p == null) ? "-fx-background-color: transparent;"
                        : getIndex() % 2 == 0
                        ? "-fx-background-color: transparent; -fx-border-color: transparent transparent #1E1E2E transparent; -fx-border-width: 0 0 1 0;"
                        : "-fx-background-color: rgba(28,28,41,0.55); -fx-border-color: transparent transparent #1E1E2E transparent; -fx-border-width: 0 0 1 0;";
                setStyle(base);
                setOnMouseEntered(e -> setStyle("-fx-background-color: rgba(56,143,255,0.07); -fx-cursor: hand;"));
                setOnMouseExited(e -> setStyle(base));
            }
        });

        // Colonne PLAYER
        colName.setCellFactory(col -> new TableCell<Joueur, String>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label l = new Label(v);
                l.setStyle("-fx-text-fill:#E8E8F0; -fx-font-size:14px; -fx-font-weight:bold;");
                setGraphic(l);
                setText(null);
            }
        });

        // Colonne KILLS
        colKills.setCellFactory(col -> new TableCell<Joueur, Integer>() {
            @Override
            protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label l = new Label(String.valueOf(v));
                l.setStyle("-fx-text-fill:#2DFF9F; -fx-font-family:'Courier New'; -fx-font-size:13px; -fx-font-weight:bold;");
                setGraphic(l);
                setText(null);
            }
        });

        // Colonne DEATHS
        colDeaths.setCellFactory(col -> new TableCell<Joueur, Integer>() {
            @Override
            protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label l = new Label(String.valueOf(v));
                l.setStyle("-fx-text-fill:#FF4D6A; -fx-font-family:'Courier New'; -fx-font-size:13px; -fx-font-weight:bold;");
                setGraphic(l);
                setText(null);
            }
        });

        // Colonne ASSISTS
        colAssists.setCellFactory(col -> new TableCell<Joueur, Integer>() {
            @Override
            protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label l = new Label(String.valueOf(v));
                l.setStyle("-fx-text-fill:#388FFF; -fx-font-family:'Courier New'; -fx-font-size:13px; -fx-font-weight:bold;");
                setGraphic(l);
                setText(null);
            }
        });

        // Colonne KDA
        colKDA.setCellFactory(col -> new TableCell<Joueur, Double>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                String color = v >= 3.0 ? "#FFB830" : "#C8C8D8";
                Label l = new Label(String.format("%.2f", v));
                l.setStyle("-fx-text-fill:" + color + "; -fx-font-family:'Courier New'; -fx-font-size:13px; -fx-font-weight:bold;");
                setGraphic(l);
                setText(null);
            }
        });

        // Colonne STATUS
        colStatus.setCellFactory(col -> new TableCell<Joueur, String>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                String color;
                switch (v.toLowerCase()) {
                    case "active":
                        color = "#2DFF9F";
                        break;
                    case "bench":
                        color = "#FFB830";
                        break;
                    default:
                        color = "#5A5A7A";
                        break;
                }
                Label dot = new Label("●");
                dot.setStyle("-fx-text-fill:" + color + "; -fx-font-size:9px;");
                Label txt = new Label(v.toUpperCase());
                txt.setStyle("-fx-text-fill:" + color + "; -fx-font-size:11px; -fx-font-weight:bold;");
                HBox box = new HBox(5, dot, txt);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
                setText(null);
            }
        });

        // Colonne favori
        colFavorite.setCellFactory(col -> new TableCell<Joueur, Void>() {
            private final Button starBtn = new Button("☆");
            {
                starBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FFD700; -fx-font-size: 14px; -fx-cursor: hand;");
                starBtn.setOnAction(e -> {
                    Joueur j = getTableView().getItems().get(getIndex());
                    try {
                        joueurService.toggleFavorite(j.getId());
                        loadPlayers();
                        applyFilters();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Joueur j = getTableView().getItems().get(getIndex());
                    starBtn.setText(j.isFavorite() ? "★" : "☆");
                    setGraphic(starBtn);
                }
            }
        });

        // Configuration des ComboBox
        filterGameCombo.getItems().addAll("Tous", "Valorant", "CS2", "LoL", "Dota2", "Fortnite", "Apex");
        filterGameCombo.setValue("Tous");
        filterGameCombo.setOnAction(e -> applyFilters());

        filterRegionCombo.getItems().addAll("Tous", "EUW", "NA", "KR", "EUNE", "BR");
        filterRegionCombo.setValue("Tous");
        filterRegionCombo.setOnAction(e -> applyFilters());

        filterRankCombo.getItems().addAll("Tous", "Top 1-10", "Top 11-20", "Top 21-50");
        filterRankCombo.setValue("Tous");
        filterRankCombo.setOnAction(e -> applyFilters());

        // Double-clic sur une ligne
        playersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Joueur selected = playersTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showAlert("Équipe", "Équipe ID: " + selected.getEquipeId());
                }
            }
        });

        loadPlayers();
        loadFriendRequests();
        loadFriendsList();
    }

    private void loadPlayers() {
        try {
            // Avant : seulement Top 10 KDA
            // allJoueurs = joueurService.getBestKDA(10);

            // Après : tous les joueurs
            allJoueurs = joueurService.getAll();
            filteredList = new FilteredList<>(FXCollections.observableArrayList(allJoueurs), p -> true);
            playersTable.setItems(filteredList);
            System.out.println("✅ " + allJoueurs.size() + " joueurs chargés");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void applyFilters() {
        if (filteredList == null) return;

        String game = filterGameCombo.getValue();
        String region = filterRegionCombo.getValue();
        String rankFilter = filterRankCombo.getValue();
        String search = searchField.getText().toLowerCase();

        filteredList.setPredicate(joueur -> {
            if (!"Tous".equals(game) && !game.equals(joueur.getGame())) return false;
            if (!"Tous".equals(region) && !region.equals(joueur.getRegion())) return false;
            if (!"Tous".equals(rankFilter)) {
                int rank = joueur.getRank();
                if (rankFilter.equals("Top 1-10") && (rank < 1 || rank > 10)) return false;
                if (rankFilter.equals("Top 11-20") && (rank < 11 || rank > 20)) return false;
                if (rankFilter.equals("Top 21-50") && (rank < 21 || rank > 50)) return false;
            }
            if (!search.isEmpty() && !joueur.getPseudo().toLowerCase().contains(search)) return false;
            return true;
        });
    }

    private void loadFriendRequests() {
        try {
            List<Joueur> requests = getPendingFriendRequests(currentUserId);
            friendRequestsBox.getChildren().clear();

            for (Joueur requester : requests) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #1C1C29; -fx-background-radius: 6; -fx-padding: 6 8;");

                Label nameLabel = new Label(requester.getPseudo());
                nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

                Label gameLabel = new Label(requester.getGame());
                gameLabel.setStyle("-fx-text-fill: #388FFF; -fx-font-size: 10px; -fx-background-color: #0F0F17; -fx-background-radius: 4; -fx-padding: 2 6;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button acceptBtn = new Button("✓");
                acceptBtn.setStyle("-fx-background-color: #2DFF9F; -fx-text-fill: #0F0F17; -fx-background-radius: 4; -fx-font-size: 11px; -fx-min-width: 28; -fx-cursor: hand;");
                acceptBtn.setOnAction(e -> {
                    try {
                        acceptFriendRequest(currentUserId, requester.getId());
                        loadFriendRequests();
                        loadFriendsList();
                        showAlert("Succès", requester.getPseudo() + " est maintenant votre ami");
                    } catch (Exception ex) {
                        showAlert("Erreur", ex.getMessage());
                    }
                });

                Button rejectBtn = new Button("✗");
                rejectBtn.setStyle("-fx-background-color: #FF4D6A; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px; -fx-min-width: 28; -fx-cursor: hand;");
                rejectBtn.setOnAction(e -> {
                    try {
                        rejectFriendRequest(currentUserId, requester.getId());
                        loadFriendRequests();
                        showAlert("Info", "Demande refusée");
                    } catch (Exception ex) {
                        showAlert("Erreur", ex.getMessage());
                    }
                });

                row.getChildren().addAll(nameLabel, gameLabel, spacer, acceptBtn, rejectBtn);
                friendRequestsBox.getChildren().add(row);
            }
            requestCountLabel.setText(requests.size() + " pending");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFriendsList() {
        try {
            List<Joueur> friends = getFriendsList(currentUserId);
            friendsListBox.getChildren().clear();

            for (Joueur friend : friends) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #1C1C29; -fx-background-radius: 6; -fx-padding: 6 8;");

                Label onlineDot = new Label("●");
                onlineDot.setStyle("-fx-text-fill: #2DFF9F; -fx-font-size: 8px;");

                Label nameLabel = new Label(friend.getPseudo());
                nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

                Label gameLabel = new Label(friend.getGame());
                gameLabel.setStyle("-fx-text-fill: #388FFF; -fx-font-size: 10px; -fx-background-color: #0F0F17; -fx-background-radius: 4; -fx-padding: 2 6;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button chatBtn = new Button("💬");
                chatBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #388FFF; -fx-font-size: 12px; -fx-cursor: hand;");

                row.getChildren().addAll(onlineDot, nameLabel, gameLabel, spacer, chatBtn);
                friendsListBox.getChildren().add(row);
            }
            friendCountLabel.setText(friends.size() + " friends");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sendFriendRequest() {
        String pseudo = searchFriendField.getText().trim();
        if (pseudo.isEmpty()) {
            showAlert("Info", "Entrez un pseudo");
            return;
        }

        try {
            sendFriendRequestToDB(currentUserId, pseudo);
            searchFriendField.clear();
            showAlert("Succès", "Demande envoyée à " + pseudo);
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void sendFriendRequestToDB(int senderId, String receiverPseudo) throws Exception {
        String findJoueurQuery = "SELECT id FROM joueur WHERE pseudo = ?";
        PreparedStatement findPs = connection.prepareStatement(findJoueurQuery);
        findPs.setString(1, receiverPseudo);
        ResultSet rs = findPs.executeQuery();

        if (!rs.next()) {
            throw new Exception("Joueur non trouvé: " + receiverPseudo);
        }
        int receiverId = rs.getInt("id");

        if (senderId == receiverId) {
            throw new Exception("Vous ne pouvez pas vous ajouter vous-même");
        }

        String checkQuery = "SELECT * FROM friend_request WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
        PreparedStatement checkPs = connection.prepareStatement(checkQuery);
        checkPs.setInt(1, senderId);
        checkPs.setInt(2, receiverId);
        checkPs.setInt(3, receiverId);
        checkPs.setInt(4, senderId);
        ResultSet checkRs = checkPs.executeQuery();

        if (checkRs.next()) {
            String status = checkRs.getString("status");
            if ("PENDING".equals(status)) {
                throw new Exception("Demande déjà envoyée");
            } else if ("ACCEPTED".equals(status)) {
                throw new Exception("Vous êtes déjà amis");
            }
        }

        String query = "INSERT INTO friend_request (sender_id, receiver_id, status) VALUES (?, ?, 'PENDING')";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, senderId);
        ps.setInt(2, receiverId);
        ps.executeUpdate();
        System.out.println("✅ Demande envoyée de " + senderId + " à " + receiverId);
    }

    private List<Joueur> getPendingFriendRequests(int joueurId) throws Exception {
        List<Joueur> requests = new ArrayList<>();
        String query = "SELECT j.id, j.pseudo, j.nom, j.prenom, j.game " +
                "FROM friend_request fr " +
                "JOIN joueur j ON fr.sender_id = j.id " +
                "WHERE fr.receiver_id = ? AND fr.status = 'PENDING'";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, joueurId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Joueur j = new Joueur();
            j.setId(rs.getInt("id"));
            j.setPseudo(rs.getString("pseudo"));
            j.setNom(rs.getString("nom"));
            j.setPrenom(rs.getString("prenom"));
            j.setGame(rs.getString("game"));
            requests.add(j);
        }
        return requests;
    }

    private void acceptFriendRequest(int joueurId, int senderId) throws Exception {
        String updateQuery = "UPDATE friend_request SET status = 'ACCEPTED' WHERE sender_id = ? AND receiver_id = ?";
        PreparedStatement updatePs = connection.prepareStatement(updateQuery);
        updatePs.setInt(1, senderId);
        updatePs.setInt(2, joueurId);
        updatePs.executeUpdate();

        String insertQuery = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?), (?, ?)";
        PreparedStatement insertPs = connection.prepareStatement(insertQuery);
        insertPs.setInt(1, joueurId);
        insertPs.setInt(2, senderId);
        insertPs.setInt(3, senderId);
        insertPs.setInt(4, joueurId);
        insertPs.executeUpdate();
        System.out.println("✅ Ami ajouté : " + joueurId + " <-> " + senderId);
    }

    private void rejectFriendRequest(int joueurId, int senderId) throws Exception {
        String query = "DELETE FROM friend_request WHERE sender_id = ? AND receiver_id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, senderId);
        ps.setInt(2, joueurId);
        ps.executeUpdate();
        System.out.println("❌ Demande refusée : " + senderId + " -> " + joueurId);
    }

    private List<Joueur> getFriendsList(int joueurId) throws Exception {
        List<Joueur> friends = new ArrayList<>();
        String query = "SELECT j.id, j.pseudo, j.nom, j.prenom, j.game " +
                "FROM friends f JOIN joueur j ON f.friend_id = j.id " +
                "WHERE f.user_id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, joueurId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Joueur j = new Joueur();
            j.setId(rs.getInt("id"));
            j.setPseudo(rs.getString("pseudo"));
            j.setNom(rs.getString("nom"));
            j.setPrenom(rs.getString("prenom"));
            j.setGame(rs.getString("game"));
            friends.add(j);
        }
        return friends;
    }

    @FXML
    public void onSearch() {
        applyFilters();
    }

    @FXML
    public void filterAll() {
        loadPlayers();
    }

    @FXML
    public void filterFraggers() {
        try {
            playersTable.setItems(FXCollections.observableArrayList(joueurService.getTopFraggers(10)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void filterKDA() {
        try {
            playersTable.setItems(FXCollections.observableArrayList(joueurService.getBestKDA(10)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void filterAssists() {
        try {
            playersTable.setItems(FXCollections.observableArrayList(joueurService.getMostAssists(10)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void filterMVP() {
        try {
            playersTable.setItems(FXCollections.observableArrayList(joueurService.getMVPs()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showFavorites() {
        try {
            playersTable.setItems(FXCollections.observableArrayList(joueurService.getFavorites()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showTopWinRate() {
        try {
            playersTable.setItems(FXCollections.observableArrayList(joueurService.getTopWinRate(10)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void navigate(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxml));
            Stage stage = (Stage) playersTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1440, 960));
        } catch (Exception e) {
            System.out.println("❌ Could not load: " + fxml);
        }
    }

    @FXML
    public void navDashboard() {
        navigate("dashboard.fxml");
    }

    @FXML
    public void navPlayers() {
        navigate("players.fxml");
    }

    @FXML
    public void navMatches() {
        navigate("matches.fxml");
    }

    @FXML
    public void navTeams() {
        navigate("teams.fxml");
    }

    @FXML
    public void navTournaments() {
        navigate("tournaments.fxml");
    }

    @FXML
    public void navAnalytics() {
        navigate("analytics.fxml");
    }

    @FXML
    public void navProduits() {
        navigate("produit.fxml");
    }

    @FXML
    public void navSponsors() {
        navigate("sponsor.fxml");
    }
}