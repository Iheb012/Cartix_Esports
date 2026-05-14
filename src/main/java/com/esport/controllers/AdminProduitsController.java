package com.esport.controllers;

import com.esport.models.Produit;
import com.esport.services.ProduitService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class AdminProduitsController {

    @FXML private TextField searchField;
    @FXML private TableView<Produit> produitsTable;
    @FXML private TableColumn<Produit, Integer> colId;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, Double> colPrix;
    @FXML private TableColumn<Produit, Integer> colStock;
    @FXML private TableColumn<Produit, String> colCategorie;
    @FXML private Label lblTotalProduits, lblStockBas, lblValeurStock;

    private final ProduitService produitService = new ProduitService();
    private ObservableList<Produit> produitsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurerTableau();
        chargerProduits();
        setupSearch();
    }

    private void configurerTableau() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));

        colStock.setCellFactory(column -> new TableCell<Produit, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else if (item <= 5) {
                    Label badge = new Label("\uD83D\uDFE0 Stock bas (" + item + ")");
                    badge.setStyle("-fx-background-color: rgba(255,165,0,0.15); -fx-text-fill: #ffa500; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 3 12; -fx-font-size: 12;");
                    setGraphic(badge);
                    setText(null);
                } else {
                    setGraphic(null);
                    setText(String.valueOf(item));
                    setStyle("-fx-text-fill: #e8eaf6;");
                }
            }
        });

        produitsTable.setItems(produitsList);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, newVal) -> filtrerProduits(newVal));
    }

    private void filtrerProduits(String search) {
        if (search == null || search.isEmpty()) {
        produitsTable.setItems(produitsList);
        } else {
            String searchLower = search.toLowerCase();
            ObservableList<Produit> filtered = FXCollections.observableArrayList(
                    produitsList.stream()
                            .filter(p -> p.getNom().toLowerCase().contains(searchLower) ||
                                    (p.getCategorie() != null && p.getCategorie().toLowerCase().contains(searchLower)))
                            .toList()
            );
            produitsTable.setItems(filtered);
        }
    }

    private void chargerProduits() {
        produitsList.clear();
        produitsList.addAll(produitService.getAllProduits());
        mettreAJourStats();
    }

    private void mettreAJourStats() {
        lblTotalProduits.setText(String.valueOf(produitsList.size()));
        lblStockBas.setText(String.valueOf(produitService.countStockBas()));
        lblValeurStock.setText(String.format("%.0f TND", produitService.getValeurStock()));
    }

    @FXML
    public void ajouterProduit() {
        showProductForm(null);
    }

    @FXML
    public void modifierProduit() {
        Produit selected = produitsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showProductForm(selected);
        } else {
            showAlert("Attention", "Veuillez sélectionner un produit");
        }
    }

    @FXML
    public void supprimerProduit() {
        Produit selected = produitsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText(null);
            confirm.setContentText("Supprimer " + selected.getNom() + " ?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    produitService.supprimerProduit(selected.getId());
                    chargerProduits();
                    showAlert("Succès", "Produit supprimé !");
                }
            });
        } else {
            showAlert("Attention", "Veuillez sélectionner un produit");
        }
    }

    @FXML
    public void rafraichir() {
        chargerProduits();
    }

    @FXML
    public void openCommandes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_commandes.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 750);
            Stage stage = new Stage();
            stage.setTitle("👑 Cartix - Commandes");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProductForm(Produit produit) {
        boolean isEdit = produit != null;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier le produit" : "Ajouter un produit");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nomField = new TextField(isEdit ? produit.getNom() : "");
        nomField.setPromptText("Nom");

        TextField prixField = new TextField(isEdit ? String.valueOf(produit.getPrix()) : "");
        prixField.setPromptText("Prix");

        TextField stockField = new TextField(isEdit ? String.valueOf(produit.getStock()) : "");
        stockField.setPromptText("Stock");

        TextField categorieField = new TextField(isEdit ? produit.getCategorie() : "");
        categorieField.setPromptText("Catégorie");

        TextField descField = new TextField(isEdit ? produit.getDescription() : "");
        descField.setPromptText("Description");

        TextField imageField = new TextField(isEdit ? produit.getImageUrl() : "");
        imageField.setPromptText("URL de l'image");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Prix (TND):"), 0, 2);
        grid.add(prixField, 1, 2);
        grid.add(new Label("Stock:"), 0, 3);
        grid.add(stockField, 1, 3);
        grid.add(new Label("Catégorie:"), 0, 4);
        grid.add(categorieField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(button -> {
            if (button == ButtonType.OK) {
                try {
                    if (isEdit) {
                        produit.setNom(nomField.getText());
                        produit.setDescription(descField.getText());
                        produit.setPrix(Double.parseDouble(prixField.getText()));
                        produit.setStock(Integer.parseInt(stockField.getText()));
                        produit.setCategorie(categorieField.getText());
                        produitService.modifierProduit(produit);
                    } else {
                        Produit newProduit = new Produit();
                        newProduit.setNom(nomField.getText());
                        newProduit.setDescription(descField.getText());
                        newProduit.setPrix(Double.parseDouble(prixField.getText()));
                        newProduit.setStock(Integer.parseInt(stockField.getText()));
                        newProduit.setCategorie(categorieField.getText());
                        produitService.ajouterProduit(newProduit);
                    }
                    chargerProduits();
                    showAlert("Succès", isEdit ? "Produit modifié !" : "Produit ajouté !");
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Prix et stock doivent être des nombres");
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}