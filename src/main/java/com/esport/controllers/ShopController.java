package com.esport.controllers;

import com.esport.models.Commande;
import com.esport.models.PanierItem;
import com.esport.models.Produit;
import com.esport.services.CommandeService;
import com.esport.services.PanierService;
import com.esport.services.ProduitService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.Date;

public class ShopController {

    @FXML private TextField searchField;
    @FXML private FlowPane productsGrid;
    @FXML private ListView<PanierItem> cartListView;
    @FXML private Label lblCartTotal, lblCartCount, lblWelcome, lblTotalProducts;

    private final ProduitService produitService = new ProduitService();
    private final PanierService panierService = new PanierService();
    private final CommandeService commandeService = new CommandeService();
    private ObservableList<Produit> produitsList = FXCollections.observableArrayList();
    private ObservableList<PanierItem> cartItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        chargerProduits();
        setupSearch();
        setupCartList();
        lblWelcome.setText("Bienvenue, Joueur !");
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, newVal) -> filtrerProduits(newVal));
    }

    private void filtrerProduits(String search) {
        productsGrid.getChildren().clear();
        String searchLower = search == null ? "" : search.toLowerCase();
        produitsList.stream()
                .filter(p -> p.getNom().toLowerCase().contains(searchLower) ||
                        (p.getCategorie() != null && p.getCategorie().toLowerCase().contains(searchLower)))
                .forEach(this::ajouterCarteProduit);
    }

    private void setupCartList() {
        cartListView.setItems(cartItems);
        cartListView.setCellFactory(lv -> new ListCell<PanierItem>() {
            @Override
            protected void updateItem(PanierItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox content = new HBox(10);
                    content.setAlignment(Pos.CENTER_LEFT);
                    content.setPadding(new Insets(8));

                    VBox infoBox = new VBox(3);
                    Label nameLabel = new Label(item.getProduit().getNom());
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
                    Label priceLabel = new Label(String.format("%.2f €", item.getProduit().getPrix()));
                    priceLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 12;");
                    infoBox.getChildren().addAll(nameLabel, priceLabel);

                    Spinner<Integer> qtySpinner = new Spinner<>(1, item.getProduit().getStock(), item.getQuantite());
                    qtySpinner.setStyle("-fx-font-size: 12;");
                    qtySpinner.valueProperty().addListener((obs, old, newVal) -> {
                        item.setQuantite(newVal);
                        updateCartTotal();
                    });

                    Label totalLabel = new Label(String.format("%.2f €", item.getTotal()));
                    totalLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 13; -fx-font-weight: bold;");
                    totalLabel.setPrefWidth(70);

                    Button removeBtn = new Button("✖");
                    removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4444; -fx-font-size: 14; -fx-cursor: hand;");
                    removeBtn.setOnAction(e -> {
                        cartItems.remove(item);
                        updateCartTotal();
                    });

                    content.getChildren().addAll(infoBox, qtySpinner, totalLabel, removeBtn);
                    setGraphic(content);
                }
            }
        });
    }

    private void chargerProduits() {
        produitsList.clear();
        produitsList.addAll(produitService.getAllProduits());
        lblTotalProducts.setText(produitsList.size() + " produits disponibles");
        afficherProduits();
    }

    private void afficherProduits() {
        productsGrid.getChildren().clear();
        for (Produit p : produitsList) {
            ajouterCarteProduit(p);
        }
    }

    private void ajouterCarteProduit(Produit p) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        card.setPrefWidth(200);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);

        // Image
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(160, 160);
        imageContainer.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 10;");

        ImageView productImage = new ImageView();
        productImage.setFitWidth(140);
        productImage.setFitHeight(140);
        productImage.setPreserveRatio(true);

        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            try {
                String imgPath = "/images/" + p.getImageUrl();
                var resource = getClass().getResource(imgPath);
                if (resource != null) {
                    productImage.setImage(new Image(resource.toExternalForm(), true));
                } else {
                    Label defaultIcon = new Label("🖼");
                    defaultIcon.setFont(Font.font(50));
                    imageContainer.getChildren().add(defaultIcon);
                }
            } catch (Exception e) {
                Label defaultIcon = new Label("🎮");
                defaultIcon.setFont(Font.font(50));
                imageContainer.getChildren().add(defaultIcon);
            }
        } else {
            Label defaultIcon = new Label("🎮");
            defaultIcon.setFont(Font.font(50));
            imageContainer.getChildren().add(defaultIcon);
        }

        if (productImage.getImage() != null && !imageContainer.getChildren().contains(productImage)) {
            imageContainer.getChildren().add(productImage);
        }

        Label nomLabel = new Label(p.getNom());
        nomLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        nomLabel.setWrapText(true);
        nomLabel.setAlignment(Pos.CENTER);

        Label prixLabel = new Label(String.format("%.2f €", p.getPrix()));
        prixLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 16; -fx-font-weight: bold;");

        Label stockLabel = new Label("Stock: " + p.getStock());
        if (p.getStock() <= 5) {
            stockLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11; -fx-font-weight: bold;");
        } else {
            stockLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");
        }

        Button addBtn = new Button("🛒 Ajouter");
        addBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");

        if (p.getStock() <= 0) {
            addBtn.setDisable(true);
            addBtn.setText("❌ Rupture");
            addBtn.setStyle("-fx-background-color: #555; -fx-text-fill: #aaa; -fx-background-radius: 8; -fx-padding: 6 12;");
        } else {
            addBtn.setOnMouseClicked(e -> ajouterAuPanier(p));
        }

        card.getChildren().addAll(imageContainer, nomLabel, prixLabel, stockLabel, addBtn);
        productsGrid.getChildren().add(card);
    }

    private void ajouterAuPanier(Produit p) {
        if (p.getStock() <= 0) {
            showAlert("Stock épuisé", "Ce produit n'est plus disponible");
            return;
        }

        for (PanierItem item : cartItems) {
            if (item.getProduit().getId() == p.getId()) {
                if (item.getQuantite() + 1 <= p.getStock()) {
                    item.setQuantite(item.getQuantite() + 1);
                } else {
                    showAlert("Stock limité", "Stock maximum: " + p.getStock());
                }
                updateCartTotal();
                cartListView.refresh();
                return;
            }
        }
        cartItems.add(new PanierItem(p, 1));
        updateCartTotal();
        showTemporaryMessage("✓ " + p.getNom() + " ajouté");
    }

    private void updateCartTotal() {
        double total = cartItems.stream().mapToDouble(PanierItem::getTotal).sum();
        lblCartTotal.setText(String.format("%.2f €", total));
        lblCartCount.setText(cartItems.size() + " article(s)");
    }

    private void showTemporaryMessage(String message) {
        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 20; -fx-font-size: 12;");
        msgLabel.setAlignment(Pos.CENTER);

        StackPane root = (StackPane) productsGrid.getParent().getParent();
        root.getChildren().add(msgLabel);
        StackPane.setAlignment(msgLabel, Pos.TOP_CENTER);
        StackPane.setMargin(msgLabel, new Insets(10, 0, 0, 0));

        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
            Platform.runLater(() -> root.getChildren().remove(msgLabel));
        }).start();
    }

    @FXML
    public void viderPanier() {
        if (!cartItems.isEmpty()) {
            cartItems.clear();
            updateCartTotal();
        }
    }

    @FXML
    public void passerCommande() {
        if (cartItems.isEmpty()) {
            showAlert("Panier vide", "Ajoutez des produits avant de commander");
            return;
        }

        double total = cartItems.stream().mapToDouble(PanierItem::getTotal).sum();

        Commande commande = new Commande();
        commande.setClientNom("Joueur");
        commande.setClientEmail("joueur@esport.com");
        commande.setDateCommande(new Date());
        commande.setTotal(total);
        commande.setStatutLivraison("EN_ATTENTE");

        if (commandeService.sauvegarderCommande(commande)) {
            showAlert("✓ Commande confirmée !",
                    "Commande #" + commande.getId() + "\nTotal: " + String.format("%.2f €", total));
            cartItems.clear();
            updateCartTotal();
        } else {
            showAlert("Erreur", "Problème lors de la commande");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}