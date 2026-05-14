package com.esport.controllers;

import com.esport.models.Produit;
import com.esport.services.ProduitService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

public class AdminController {

    @FXML private TextField searchField;
    @FXML private TableView<Produit> produitsTable;
    @FXML private TableColumn<Produit, Integer> colId;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, Double> colPrix;
    @FXML private TableColumn<Produit, Integer> colStock;
    @FXML private TableColumn<Produit, String> colCategorie;
    @FXML private TableColumn<Produit, String> colImage;
    @FXML private TableColumn<Produit, Void> colActions;
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
        colImage.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));

        // Style personnalisé pour le stock
        colStock.setCellFactory(column -> new TableCell<Produit, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    if (item <= 5) {
                        setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: white;");
                    }
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
        lblValeurStock.setText(String.format("%.0f €", produitService.getValeurStock()));
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
            showAlert("Attention", "Veuillez sélectionner un produit à modifier");
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
            showAlert("Attention", "Veuillez sélectionner un produit à supprimer");
        }
    }

    @FXML
    public void rafraichir() {
        chargerProduits();
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
        nomField.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: white; -fx-padding: 8;");

        TextField prixField = new TextField(isEdit ? String.valueOf(produit.getPrix()) : "");
        prixField.setPromptText("Prix");
        prixField.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: white; -fx-padding: 8;");

        TextField stockField = new TextField(isEdit ? String.valueOf(produit.getStock()) : "");
        stockField.setPromptText("Stock");
        stockField.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: white; -fx-padding: 8;");

        TextField categorieField = new TextField(isEdit ? produit.getCategorie() : "");
        categorieField.setPromptText("Catégorie");
        categorieField.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: white; -fx-padding: 8;");

        TextField imageField = new TextField(isEdit ? produit.getImageUrl() : "");
        imageField.setPromptText("URL de l'image");
        imageField.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: white; -fx-padding: 8;");

        TextArea descArea = new TextArea(isEdit ? produit.getDescription() : "");
        descArea.setPromptText("Description");
        descArea.setPrefHeight(80);
        descArea.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: white; -fx-padding: 8;");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prix (€):"), 0, 1);
        grid.add(prixField, 1, 1);
        grid.add(new Label("Stock:"), 0, 2);
        grid.add(stockField, 1, 2);
        grid.add(new Label("Catégorie:"), 0, 3);
        grid.add(categorieField, 1, 3);
        grid.add(new Label("Image URL:"), 0, 4);
        grid.add(imageField, 1, 4);
        grid.add(new Label("Description:"), 0, 5);
        grid.add(descArea, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(button -> {
            if (button == ButtonType.OK) {
                try {
                    if (isEdit) {
                        produit.setNom(nomField.getText());
                        produit.setPrix(Double.parseDouble(prixField.getText()));
                        produit.setStock(Integer.parseInt(stockField.getText()));
                        produit.setCategorie(categorieField.getText());
                        produit.setImageUrl(imageField.getText());
                        produit.setDescription(descArea.getText());
                        produitService.modifierProduit(produit);
                    } else {
                        Produit newProduit = new Produit();
                        newProduit.setNom(nomField.getText());
                        newProduit.setPrix(Double.parseDouble(prixField.getText()));
                        newProduit.setStock(Integer.parseInt(stockField.getText()));
                        newProduit.setCategorie(categorieField.getText());
                        newProduit.setImageUrl(imageField.getText());
                        newProduit.setDescription(descArea.getText());
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

    @FXML
    public void quitter() {
        Platform.exit();
    }
}