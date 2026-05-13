package services;

import models.ResultatMatch;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResultatMatchService {

    private Connection connection;

    public ResultatMatchService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    public void add(ResultatMatch r) throws Exception {
        String query = "INSERT INTO resultatmatch (matchId, scoreEquipe1, scoreEquipe2, gagnantId) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, r.getMatchId());
        ps.setInt(2, r.getScoreEquipe1());
        ps.setInt(3, r.getScoreEquipe2());
        ps.setInt(4, r.getGagnantId());
        ps.executeUpdate();
        System.out.println("✅ ResultatMatch added successfully!");
    }

    public void update(ResultatMatch r) throws Exception {
        String query = "UPDATE resultatmatch SET scoreEquipe1=?, scoreEquipe2=?, gagnantId=? WHERE matchId=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, r.getScoreEquipe1());
        ps.setInt(2, r.getScoreEquipe2());
        ps.setInt(3, r.getGagnantId());
        ps.setInt(4, r.getMatchId());
        ps.executeUpdate();
        System.out.println("✅ ResultatMatch updated successfully!");
    }

    public void delete(int matchId) throws Exception {
        String query = "DELETE FROM resultatmatch WHERE matchId=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, matchId);
        ps.executeUpdate();
        System.out.println("✅ ResultatMatch deleted successfully!");
    }

    public ResultatMatch getByMatchId(int matchId) throws Exception {
        String query = "SELECT * FROM resultatmatch WHERE matchId=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, matchId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new ResultatMatch(
                rs.getInt("matchId"),
                rs.getInt("scoreEquipe1"),
                rs.getInt("scoreEquipe2"),
                rs.getInt("gagnantId")
            );
        }
        return null;
    }

    public List<ResultatMatch> getAll() throws Exception {
        List<ResultatMatch> list = new ArrayList<>();
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM resultatmatch");
        while (rs.next()) {
            list.add(new ResultatMatch(
                rs.getInt("matchId"),
                rs.getInt("scoreEquipe1"),
                rs.getInt("scoreEquipe2"),
                rs.getInt("gagnantId")
            ));
        }
        return list;
    }
}
