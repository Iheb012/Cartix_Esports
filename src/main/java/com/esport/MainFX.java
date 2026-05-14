package com.esport;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        openAdminProduits();
        openMemberShop();

        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.close();
    }

    private void openAdminProduits() {
        try {
            // Chemin CORRECT : /fxml/admin_produits.fxml
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/admin_produits.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1100, 700);
            Stage stage = new Stage();
            stage.setTitle("👑 Cartix - Administration");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openMemberShop() {
        try {
            // Chemin CORRECT : /fxml/shop_member.fxml
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/shop_member.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1300, 800);
            Stage stage = new Stage();
            stage.setTitle("🛒 Cartix - Espace Membre");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}