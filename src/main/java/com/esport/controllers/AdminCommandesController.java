package com.esport.controllers;

import com.esport.models.Commande;
import com.esport.services.CommandeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminCommandesController {

    @FXML private TableView<Commande> commandesTable;
    @FXML private TableColumn<Commande, Integer> colId;
    @FXML private TableColumn<Commande, String> colClient;
    @FXML private TableColumn<Commande, Date> colDate;
    @FXML private TableColumn<Commande, Double> colTotal;
    @FXML private TableColumn<Commande, String> colStatut;

    @FXML private Label lblTotal, lblEnAttente, lblPayee, lblExpediee, lblEnCours, lblLivree, lblAnnulee, lblCA;

    private final CommandeService commandeService = new CommandeService();
    private ObservableList<Commande> commandesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClient.setCellValueFactory(cellData -> cellData.getValue().clientNomProperty());
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCommande"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutLivraison"));

        colTotal.setCellFactory(column -> new TableCell<Commande, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(String.format("%.2f TND", item));
                    setStyle("-fx-text-fill: #ff4d6d; -fx-font-weight: bold;");
                }
            }
        });

        colDate.setCellFactory(column -> new TableCell<Commande, Date>() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : sdf.format(item));
            }
        });

        colStatut.setCellFactory(column -> new TableCell<Commande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); }
                else { setGraphic(creerBadge(item)); setText(null); }
            }
        });

        commandesTable.setItems(commandesList);
        chargerCommandes();
    }

    @FXML public void rafraichir() { chargerCommandes(); }

    private void chargerCommandes() {
        commandesList.clear();
        commandesList.addAll(commandeService.getAllCommandes());
        mettreAJourStats();
    }

    private void mettreAJourStats() {
        int total = commandesList.size();
        long enAttente = commandesList.stream().filter(c -> "EN_ATTENTE".equals(c.getStatutLivraison())).count();
        long payee = commandesList.stream().filter(c -> "PAYEE".equals(c.getStatutLivraison())).count();
        long expediee = commandesList.stream().filter(c -> "EXPEDIEE".equals(c.getStatutLivraison())).count();
        long enCours = commandesList.stream().filter(c -> "EN_COURS_DE_LIVRAISON".equals(c.getStatutLivraison())).count();
        long livree = commandesList.stream().filter(c -> "LIVREE".equals(c.getStatutLivraison())).count();
        long annulee = commandesList.stream().filter(c -> "ANNULEE".equals(c.getStatutLivraison())).count();
        double ca = commandesList.stream().mapToDouble(Commande::getTotal).sum();

        lblTotal.setText(String.valueOf(total));
        lblEnAttente.setText(String.valueOf(enAttente));
        lblPayee.setText(String.valueOf(payee));
        lblExpediee.setText(String.valueOf(expediee));
        lblEnCours.setText(String.valueOf(enCours));
        lblLivree.setText(String.valueOf(livree));
        lblAnnulee.setText(String.valueOf(annulee));
        lblCA.setText(String.format("%.2f TND", ca));
    }

    @FXML public void confirmerCommande() {
        Commande c = commandesTable.getSelectionModel().getSelectedItem();
        if (c == null) { showAlert("Sélection", "Sélectionnez une commande"); return; }
        String s = c.getStatutLivraison();
        if ("LIVREE".equals(s) || "ANNULEE".equals(s)) {
            showAlert("Impossible", "Commande déjà " + (s.equals("LIVREE") ? "livrée" : "annulée"));
            return;
        }
        if ("EN_ATTENTE".equals(s)) {
            commandeService.mettreAJourStatut(c.getId(), "PAYEE");
        } else if ("PAYEE".equals(s)) {
            commandeService.mettreAJourStatut(c.getId(), "EXPEDIEE");
        } else if ("EXPEDIEE".equals(s)) {
            commandeService.mettreAJourStatut(c.getId(), "EN_COURS_DE_LIVRAISON");
        } else {
            showAlert("Info", "Statut actuel: " + s);
            return;
        }
        chargerCommandes();
    }

    @FXML public void livrerCommande() {
        Commande c = commandesTable.getSelectionModel().getSelectedItem();
        if (c == null) { showAlert("Sélection", "Sélectionnez une commande"); return; }
        String s = c.getStatutLivraison();
        if ("LIVREE".equals(s)) { showAlert("Déjà livrée", ""); return; }
        if ("ANNULEE".equals(s)) { showAlert("Annulée", "Impossible"); return; }
        if (!confirmer("Livrer la commande #" + c.getId() + " ?")) return;
        commandeService.mettreAJourStatut(c.getId(), "LIVREE");
        chargerCommandes();
    }

    @FXML public void annulerCommande() {
        Commande c = commandesTable.getSelectionModel().getSelectedItem();
        if (c == null) { showAlert("Sélection", "Sélectionnez une commande"); return; }
        String s = c.getStatutLivraison();
        if ("LIVREE".equals(s) || "ANNULEE".equals(s)) {
            showAlert("Impossible", "Commande déjà " + (s.equals("LIVREE") ? "livrée" : "annulée"));
            return;
        }
        if (!confirmer("Annuler la commande #" + c.getId() + " ?")) return;
        commandeService.mettreAJourStatut(c.getId(), "ANNULEE");
        chargerCommandes();
    }

    private Label creerBadge(String statut) {
        Label badge = new Label();
        badge.setStyle("-fx-background-radius: 12; -fx-padding: 3 12; -fx-font-size: 12; -fx-font-weight: bold;");
        switch (statut) {
            case "EN_ATTENTE":
                badge.setText("\uD83D\uDFE1 En attente");
                badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(255,165,0,0.15); -fx-text-fill: #ffa500;");
                break;
            case "PAYEE":
                badge.setText("\uD83D\uDFE2 Payée");
                badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(34,217,138,0.15); -fx-text-fill: #22d98a;");
                break;
            case "EXPEDIEE":
                badge.setText("\uD83D\uDD35 Expédiée");
                badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(52,152,219,0.15); -fx-text-fill: #3498db;");
                break;
            case "EN_COURS_DE_LIVRAISON":
                badge.setText("\uD83D\uDE9A En cours");
                badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(168,85,247,0.15); -fx-text-fill: #a855f7;");
                break;
            case "LIVREE":
                badge.setText("\uD83D\uDFE2 Livrée");
                badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(34,197,94,0.15); -fx-text-fill: #22c55e;");
                break;
            case "ANNULEE":
                badge.setText("\uD83D\uDD34 Annulée");
                badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(255,77,109,0.15); -fx-text-fill: #ff4d6d;");
                break;
            default:
                badge.setText(statut);
                badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(107,115,148,0.15); -fx-text-fill: #6b7394;");
        }
        return badge;
    }

    private boolean confirmer(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirmation"); a.setHeaderText(null); a.setContentText(msg);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}
