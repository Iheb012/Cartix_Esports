package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private static final String URL      = "jdbc:mysql://localhost:3306/mydatabase?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static Connection instance;

    public static Connection getInstance() throws SQLException {
        try {
            // ✅ Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver MySQL non trouvé : " + e.getMessage());
        }

        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion DB réussie !");
        }
        return instance;
    }
}