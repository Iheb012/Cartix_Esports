package services;

import models.Match;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchService implements IService<Match> {

    private Connection connection;

    public MatchService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Match m) throws Exception {
        String query = "INSERT INTO `match` (equipe1Id, equipe2Id, dateMatch, Statut, tournoiId, Recompense, DateObtentionRecompense, jeu) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, m.getEquipe1Id());
        ps.setInt(2, m.getEquipe2Id());
        ps.setDate(3, m.getDateMatch());
        ps.setString(4, m.getStatut());
        ps.setInt(5, m.getTournoiId());
        ps.setString(6, m.getRecompense());
        ps.setTimestamp(7, m.getDateObtentionRecompense());
        ps.setString(8, m.getJeu());
        ps.executeUpdate();
        System.out.println("✅ Match added successfully!");
    }

    @Override
    public void update(Match m) throws Exception {
        String query = "UPDATE `match` SET equipe1Id=?, equipe2Id=?, dateMatch=?, Statut=?, tournoiId=?, Recompense=?, DateObtentionRecompense=?, jeu=? WHERE Id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, m.getEquipe1Id());
        ps.setInt(2, m.getEquipe2Id());
        ps.setDate(3, m.getDateMatch());
        ps.setString(4, m.getStatut());
        ps.setInt(5, m.getTournoiId());
        ps.setString(6, m.getRecompense());
        ps.setTimestamp(7, m.getDateObtentionRecompense());
        ps.setString(8, m.getJeu());
        ps.setInt(9, m.getId());
        ps.executeUpdate();
        System.out.println("✅ Match updated successfully!");
    }

    @Override
    public void delete(int id) throws Exception {
        String query = "DELETE FROM `match` WHERE Id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("✅ Match deleted successfully!");
    }

    @Override
    public Match getById(int id) throws Exception {
        String query = "SELECT * FROM `match` WHERE Id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Match match = new Match(
                rs.getInt("Id"),
                rs.getInt("equipe1Id"),
                rs.getInt("equipe2Id"),
                rs.getDate("dateMatch"),
                rs.getString("Statut"),
                rs.getInt("tournoiId"),
                rs.getString("Recompense"),
                rs.getTimestamp("DateObtentionRecompense")
            );
            try {
                match.setJeu(rs.getString("jeu"));
            } catch (SQLException ignored) { }
            return match;
        }
        return null;
    }

    @Override
    public List<Match> getAll() throws Exception {
        List<Match> list = new ArrayList<>();
        String query = "SELECT * FROM `match`";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            Match match = new Match(
                rs.getInt("Id"),
                rs.getInt("equipe1Id"),
                rs.getInt("equipe2Id"),
                rs.getDate("dateMatch"),
                rs.getString("Statut"),
                rs.getInt("tournoiId"),
                rs.getString("Recompense"),
                rs.getTimestamp("DateObtentionRecompense")
            );
            try {
                match.setJeu(rs.getString("jeu"));
            } catch (SQLException ignored) { }
            list.add(match);
        }
        return list;
    }
}
