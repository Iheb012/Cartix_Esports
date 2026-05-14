package services;

import models.User;
import utils.MyDatabase;
import utils.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {

    private Connection getConn() {
        return MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(User user) throws Exception {
        String query = "INSERT INTO user (nom, prenom, email, mdp, role) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = getConn().prepareStatement(query);
        ps.setString(1, user.getNom());
        ps.setString(2, user.getPrenom());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getMdp());
        ps.setString(5, user.getRole());
        ps.executeUpdate();
    }

    @Override
    public void update(User user) throws Exception {
        String query = "UPDATE user SET nom=?, prenom=?, email=?, mdp=?, role=? WHERE Id=?";
        PreparedStatement ps = getConn().prepareStatement(query);
        ps.setString(1, user.getNom());
        ps.setString(2, user.getPrenom());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getMdp());
        ps.setString(5, user.getRole());
        ps.setInt(6, user.getId());
        ps.executeUpdate();
    }

    @Override
    public void delete(int id) throws Exception {
        String query = "DELETE FROM user WHERE Id=?";
        PreparedStatement ps = getConn().prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public User getById(int id) throws Exception {
        String query = "SELECT * FROM user WHERE Id=?";
        PreparedStatement ps = getConn().prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapResultSetToUser(rs);
        }
        return null;
    }

    @Override
    public List<User> getAll() throws Exception {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";
        Statement st = getConn().createStatement();
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            users.add(mapResultSetToUser(rs));
        }
        return users;
    }

    public User authenticate(String email, String password) throws Exception {
        String query = "SELECT * FROM user WHERE email = ?";
        PreparedStatement ps = getConn().prepareStatement(query);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            String storedHash = rs.getString("mdp");
            if (PasswordUtil.verify(password, storedHash)) {
                return mapResultSetToUser(rs);
            }
        }
        return null;
    }

    public User findByEmail(String email) {
        try {
            String query = "SELECT * FROM user WHERE email = ?";
            PreparedStatement ps = getConn().prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getUsersByRole(String role) throws Exception {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE role = ?";
        PreparedStatement ps = getConn().prepareStatement(query);
        ps.setString(1, role);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            users.add(mapResultSetToUser(rs));
        }
        return users;
    }

    public int getStats() throws Exception {
        String query = "SELECT COUNT(*) FROM user";
        Statement st = getConn().createStatement();
        ResultSet rs = st.executeQuery(query);
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public List<UserRow> getAllRows() {
        List<UserRow> list = new ArrayList<>();
        try {
            String query = "SELECT * FROM user";
            Statement st = getConn().createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                list.add(new UserRow(
                        rs.getInt("Id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("role")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("Id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("mdp"),
                rs.getString("role")
        );
    }

    public static class UserRow {
        private final int id;
        private final String nom;
        private final String prenom;
        private final String email;
        private final String role;

        public UserRow(int id, String nom, String prenom,
                       String email, String role) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
            this.role = role;
        }

        public int getId() { return id; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getFullName() { return prenom + " " + nom; }
    }
}