package services;

import models.Bracket;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BracketService implements IService<Bracket> {

    private Connection connection;

    public BracketService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Bracket b) throws Exception {
        String query = "INSERT INTO bracket (tournoiId, type, nbRounds) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, b.getTournoiId());
        ps.setString(2, b.getType());
        ps.setInt(3, b.getNbRounds());
        ps.executeUpdate();
        System.out.println("✅ Bracket added successfully!");
    }

    @Override
    public void update(Bracket b) throws Exception {
        String query = "UPDATE bracket SET tournoiId=?, type=?, nbRounds=? WHERE Id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, b.getTournoiId());
        ps.setString(2, b.getType());
        ps.setInt(3, b.getNbRounds());
        ps.setInt(4, b.getId());
        ps.executeUpdate();
        System.out.println("✅ Bracket updated successfully!");
    }

    @Override
    public void delete(int id) throws Exception {
        String query = "DELETE FROM bracket WHERE Id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("✅ Bracket deleted successfully!");
    }

    @Override
    public Bracket getById(int id) throws Exception {
        String query = "SELECT * FROM bracket WHERE Id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Bracket(rs.getInt("Id"), rs.getInt("tournoiId"), rs.getString("type"), rs.getInt("nbRounds"));
        }
        return null;
    }

    @Override
    public List<Bracket> getAll() throws Exception {
        List<Bracket> list = new ArrayList<>();
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM bracket");
        while (rs.next()) {
            list.add(new Bracket(rs.getInt("Id"), rs.getInt("tournoiId"), rs.getString("type"), rs.getInt("nbRounds")));
        }
        return list;
    }
}
