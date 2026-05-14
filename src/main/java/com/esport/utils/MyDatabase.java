package com.esport.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    private static final String URL = "jdbc:mysql://localhost:3306/esport_shop?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion MySQL réussie !");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver MySQL non trouvé !");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("❌ Erreur de connexion : " + e.getMessage());
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("🔌 Connexion MySQL fermée");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}