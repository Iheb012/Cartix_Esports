package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MyDatabase {

    private static MyDatabase instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3306/cartix_esports";
    private final String USER = "root";
    private final String PASSWORD = "";

    private MyDatabase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database successfully!");
            ensureMatchGameColumn();
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }

    /** Adds optional `jeu` column so local matches can be tied to a title (Matches page filter). */
    private void ensureMatchGameColumn() {
        if (connection == null) return;
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("ALTER TABLE `match` ADD COLUMN `jeu` VARCHAR(32) NULL DEFAULT NULL");
            System.out.println("Added column `match`.`jeu` for per-game local matches.");
        } catch (SQLException e) {
            if (e.getErrorCode() != 1060) {
                System.out.println("Note: could not add `match`.`jeu` column: " + e.getMessage());
            }
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