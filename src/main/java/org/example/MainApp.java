package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // ✅ Interface USER
        Parent userRoot = FXMLLoader.load(getClass().getResource("/community_hub.fxml"));
        Scene userScene = new Scene(userRoot, 1280, 960);
        stage.setTitle("CartixeSports - Community Hub");
        stage.setScene(userScene);
        stage.show();

        // ✅ Interface ADMIN
        Stage adminStage = new Stage();
        Parent adminRoot = FXMLLoader.load(getClass().getResource("/admin_community.fxml"));
        Scene adminScene = new Scene(adminRoot, 1280, 960);
        adminStage.setTitle("CartixeSports - Admin Panel");
        adminStage.setScene(adminScene);
        adminStage.show();
    }

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        try {
            MyDataBase.getInstance();
        } catch (Exception e) {
            System.out.println("❌ Erreur DB : " + e.getMessage());
        }
        launch(args);
    }
}