package services;

import models.Joueur;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JoueurService {

    private Connection connection;

    public JoueurService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // Récupérer tous les joueurs
    public List<Joueur> getAll() throws Exception {
        List<Joueur> list = new ArrayList<>();
        String query = "SELECT * FROM joueur";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    // Récupérer par ID
    public Joueur getById(int id) throws Exception {
        String query = "SELECT * FROM joueur WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapResultSet(rs);
        }
        return null;
    }

    // Filtrer par jeu
    public List<Joueur> getByGame(String game) throws Exception {
        List<Joueur> list = new ArrayList<>();
        String query = "SELECT * FROM joueur WHERE game = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, game);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    // Filtrer par région
    public List<Joueur> getByRegion(String region) throws Exception {
        List<Joueur> list = new ArrayList<>();
        String query = "SELECT * FROM joueur WHERE region = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, region);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    // Filtrer par plage de rank
    public List<Joueur> getByRankRange(int minRank, int maxRank) throws Exception {
        List<Joueur> list = new ArrayList<>();
        String query = "SELECT * FROM joueur WHERE rank BETWEEN ? AND ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, minRank);
        ps.setInt(2, maxRank);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    // Top Fraggers (plus de kills)
    public List<Joueur> getTopFraggers(int limit) throws Exception {
        List<Joueur> list = new ArrayList<>();
        String query = "SELECT * FROM joueur ORDER BY kills DESC LIMIT ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    // Best KDA
    public List<Joueur> getBestKDA(int limit) throws Exception {
        List<Joueur> list = new ArrayList<>();
        String query = "SELECT * FROM joueur ORDER BY kda DESC LIMIT ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    // Most Assists
    public List<Joueur> getMostAssists(int limit) throws Exception {
        List<Joueur> list = new ArrayList<>();
        String query = "SELECT * FROM joueur ORDER BY assists DESC LIMIT ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    // MVPs (KDA > 4.0 et minimum 30 matchs)
    public List<Joueur> getMVPs() throws Exception {
        List<Joueur> list = new ArrayList<>();
        String query = "SELECT * FROM joueur WHERE kda > 4.0 AND matches >= 30 ORDER BY kda DESC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    // Top Win Rate
    public List<Joueur> getTopWinRate(int limit) throws Exception {
        List<Joueur> list = new ArrayList<>();
        String query = "SELECT * FROM joueur ORDER BY CAST(winRate AS UNSIGNED) DESC LIMIT ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    // Favoris
    public List<Joueur> getFavorites() throws Exception {
        List<Joueur> list = new ArrayList<>();
        String query = "SELECT * FROM joueur WHERE favorite = TRUE";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    // Ajouter/retirer des favoris
    public void toggleFavorite(int id) throws Exception {
        String query = "UPDATE joueur SET favorite = NOT favorite WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // Mapper ResultSet vers Joueur
    private Joueur mapResultSet(ResultSet rs) throws SQLException {
        Joueur j = new Joueur();
        j.setId(rs.getInt("id"));
        j.setPseudo(rs.getString("pseudo"));
        j.setNom(rs.getString("nom"));
        j.setPrenom(rs.getString("prenom"));
        j.setGame(rs.getString("game"));
        j.setRegion(rs.getString("region"));
        j.setEquipeId(rs.getInt("equipeId"));
        j.setKills(rs.getInt("kills"));
        j.setDeaths(rs.getInt("deaths"));
        j.setAssists(rs.getInt("assists"));
        j.setKda(rs.getDouble("kda"));
        j.setWinRate(rs.getString("winRate"));
        j.setStatus(rs.getString("status"));
        j.setFavorite(rs.getBoolean("favorite"));
        j.setRank(rs.getInt("rank"));
        j.setMatches(rs.getInt("matches"));
        return j;
    }
    // Envoyer une demande d'ami
    public void sendFriendRequest(int senderId, String receiverPseudo) throws Exception {
        // D'abord, trouver l'ID du destinataire
        String findUserQuery = "SELECT Id FROM joueur WHERE pseudo = ?";
        PreparedStatement findPs = connection.prepareStatement(findUserQuery);
        findPs.setString(1, receiverPseudo);
        ResultSet rs = findPs.executeQuery();

        if (!rs.next()) {
            throw new Exception("Joueur non trouvé: " + receiverPseudo);
        }
        int receiverId = rs.getInt("Id");

        if (senderId == receiverId) {
            throw new Exception("Vous ne pouvez pas vous ajouter vous-même");
        }

        // Vérifier si une demande existe déjà
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
                throw new Exception("Demande déjà envoyée ou reçue");
            } else if ("ACCEPTED".equals(status)) {
                throw new Exception("Vous êtes déjà amis");
            }
        }

        // Insérer la demande
        String query = "INSERT INTO friend_request (sender_id, receiver_id, status) VALUES (?, ?, 'PENDING')";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, senderId);
        ps.setInt(2, receiverId);
        ps.executeUpdate();
    }

    // Récupérer les demandes d'amis reçues
    public List<Joueur> getPendingFriendRequests(int userId) throws Exception {
        List<Joueur> requests = new ArrayList<>();
        String query = "SELECT u.Id, u.pseudo, u.nom, u.prenom, u.game, fr.created_at " +
                "FROM friend_request fr " +
                "JOIN user u ON fr.sender_id = u.Id " +
                "WHERE fr.receiver_id = ? AND fr.status = 'PENDING'";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Joueur j = new Joueur();
            j.setId(rs.getInt("Id"));
            j.setPseudo(rs.getString("pseudo"));
            j.setNom(rs.getString("nom"));
            j.setPrenom(rs.getString("prenom"));
            j.setGame(rs.getString("game"));
            requests.add(j);
        }
        return requests;
    }

    // Accepter une demande d'ami
    public void acceptFriendRequest(int userId, int senderId) throws Exception {
        // Mettre à jour le statut de la demande
        String updateQuery = "UPDATE friend_request SET status = 'ACCEPTED' WHERE sender_id = ? AND receiver_id = ?";
        PreparedStatement updatePs = connection.prepareStatement(updateQuery);
        updatePs.setInt(1, senderId);
        updatePs.setInt(2, userId);
        updatePs.executeUpdate();

        // Ajouter dans la table friends (dans les deux sens)
        String insertQuery = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?), (?, ?)";
        PreparedStatement insertPs = connection.prepareStatement(insertQuery);
        insertPs.setInt(1, userId);
        insertPs.setInt(2, senderId);
        insertPs.setInt(3, senderId);
        insertPs.setInt(4, userId);
        insertPs.executeUpdate();
    }

    // Refuser une demande d'ami
    public void rejectFriendRequest(int userId, int senderId) throws Exception {
        String query = "DELETE FROM friend_request WHERE sender_id = ? AND receiver_id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, senderId);
        ps.setInt(2, userId);
        ps.executeUpdate();
    }

    // Récupérer la liste des amis
    public List<Joueur> getFriendsList(int userId) throws Exception {
        List<Joueur> friends = new ArrayList<>();
        String query = "SELECT u.Id, u.pseudo, u.nom, u.prenom, u.game " +
                "FROM friends f JOIN user u ON f.friend_id = u.Id " +
                "WHERE f.user_id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Joueur j = new Joueur();
            j.setId(rs.getInt("Id"));
            j.setPseudo(rs.getString("pseudo"));
            j.setNom(rs.getString("nom"));
            j.setPrenom(rs.getString("prenom"));
            j.setGame(rs.getString("game"));
            friends.add(j);
        }
        return friends;
    }
    public Joueur getByUserId(int userId) throws Exception {
        String query = "SELECT * FROM joueur WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Joueur j = new Joueur();
            j.setId(rs.getInt("id"));
            j.setPseudo(rs.getString("pseudo"));
            j.setNom(rs.getString("nom"));
            j.setPrenom(rs.getString("prenom"));
            j.setGame(rs.getString("game"));
            j.setUserId(rs.getInt("id"));
            return j;
        }
        return null;

    }
    // Dans JoueurService.java, ajoute ces méthodes

    public void update(Joueur j) throws Exception {
        String query = "UPDATE joueur SET pseudo=?, nom=?, prenom=?, game=?, kills=?, deaths=?, assists=?, kda=?, winRate=?, status=?, rank=?, matches=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, j.getPseudo());
        ps.setString(2, j.getNom());
        ps.setString(3, j.getPrenom());
        ps.setString(4, j.getGame());
        ps.setInt(5, j.getKills());
        ps.setInt(6, j.getDeaths());
        ps.setInt(7, j.getAssists());
        ps.setDouble(8, j.getKda());
        ps.setString(9, j.getWinRate());
        ps.setString(10, j.getStatus());
        ps.setInt(11, j.getRank());
        ps.setInt(12, j.getMatches());
        ps.setInt(13, j.getId());
        ps.executeUpdate();
    }

    public void delete(int id) throws Exception {
        String query = "DELETE FROM joueur WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}