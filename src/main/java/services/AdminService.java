
package services;

import models.Administrateur;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    private final Connection connection = MyDatabase.getInstance().getConnection();

    public List<Administrateur> getAll() {
        List<Administrateur> list = new ArrayList<>();
        try {
            String query = "SELECT u.*, a.niveauAcces, a.token, a.sessionActive, " +
                    "a.tentativesEchouees, a.dateExpiration " +
                    "FROM user u JOIN administration a ON u.Id = a.id " +
                    "WHERE u.role = 'admin'";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Administrateur(
                        rs.getInt("Id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("mdp"),
                        rs.getInt("niveauAcces"),
                        rs.getString("token"),
                        rs.getInt("sessionActive"),
                        rs.getInt("tentativesEchouees"),
                        rs.getString("dateExpiration")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countUsers() {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT COUNT(*) FROM user WHERE role = 'JOUEUR'");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countTournaments() {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT COUNT(*) FROM tournoi");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countLive() {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT COUNT(*) FROM tournoi WHERE status = 'LIVE'");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<UserService.UserRow> getAllUsers() {
        List<UserService.UserRow> list = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM user");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new UserService.UserRow(
                        rs.getInt("Id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void banUser(int userId) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE user SET role = 'BANNI' WHERE Id = ?");
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteUser(int userId) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM user WHERE Id = ?");
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}