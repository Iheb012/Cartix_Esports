package utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class MyDatabase {

    private static MyDatabase instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3306/mydatabase";
    private final String USER = "root";
    private final String PASSWORD = "";

    private MyDatabase() {
        connect();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database successfully!");
        } catch (Exception e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            // Reconnect if connection is null or closed
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (Exception e) {
            connect();
        }
        return connection;
    }
}