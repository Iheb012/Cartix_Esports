package utlis;

import java.sql.*;

public class MyDatabase {

    private static MyDatabase instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3306/mydatabase";
    private final String USER = "root";
    private final String PASSWORD = "";

    private MyDatabase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database successfully!");
            createTestUserIfNotExists();
            createTestAdminIfNotExists();
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }

    private void createTestUserIfNotExists() {
        try {
            String checkQuery = "SELECT COUNT(*) FROM user WHERE email = ?";
            PreparedStatement checkPs = connection.prepareStatement(checkQuery);
            checkPs.setString(1, "test@test.com");
            ResultSet rs = checkPs.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                String insertQuery = "INSERT INTO user (nom, prenom, email, mdp, role) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertPs = connection.prepareStatement(insertQuery);
                insertPs.setString(1, "Test");
                insertPs.setString(2, "User");
                insertPs.setString(3, "test@test.com");
                insertPs.setString(4, PasswordUtil.hash("test"));
                insertPs.setString(5, "user");
                insertPs.executeUpdate();
                System.out.println("Test user created:  test@test.com / test");
            }
        } catch (SQLException e) {
            System.out.println("Could not create/check test user: " + e.getMessage());
        }
    }

    private void createTestAdminIfNotExists() {
        try {
            String checkQuery = "SELECT COUNT(*) FROM user WHERE email = ?";
            PreparedStatement checkPs = connection.prepareStatement(checkQuery);
            checkPs.setString(1, "admin@test.com");
            ResultSet rs = checkPs.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                String insertQuery = "INSERT INTO user (nom, prenom, email, mdp, role) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertPs = connection.prepareStatement(insertQuery);
                insertPs.setString(1, "Admin");
                insertPs.setString(2, "Root");
                insertPs.setString(3, "admin@test.com");
                insertPs.setString(4, PasswordUtil.hash("admin"));
                insertPs.setString(5, "admin");  // 👈 role = admin
                insertPs.executeUpdate();
                System.out.println("Test admin created: admin@test.com / admin");
            }
        } catch (SQLException e) {
            System.out.println("Could not create/check test admin: " + e.getMessage());
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}