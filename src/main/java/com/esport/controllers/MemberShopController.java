package com.esport.controllers;

import com.esport.models.Commande;
import com.esport.models.PanierItem;
import com.esport.models.Produit;
import com.esport.services.CommandeService;
import com.esport.services.MailService;
import com.esport.services.PDFService;
import com.esport.services.PanierService;
import com.esport.services.ProduitService;
import com.esport.services.StripeService;
import com.esport.services.SetupService;
import com.esport.models.SetupItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.Node;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class MemberShopController {

    @FXML private StackPane contentStack;
    @FXML private VBox pageAccueil, pageCommandes, pagePaiement, pageFacture, pageLivraison, pageConfigurateur, pageNotifications, pageEmail, pageNegociateur;
    @FXML private VBox toastContainer;
    @FXML private TextField searchField;
    @FXML private FlowPane productsGrid;
    @FXML private ScrollPane productsScroll;
    @FXML private Label lblTotalProducts, lblWelcome;
    @FXML private Button btnAccueil, btnCommandes, btnPaiement, btnFactures, btnLivraison, btnConfigurateur, btnEmail, btnNotifications, btnThemeToggle;
    @FXML private Label lblNotifBadge;

    // Theme
    @FXML private BorderPane rootPane;
    @FXML private HBox topBar, navBar;
    @FXML private VBox sidebar, centerContent, panierCard;
    private boolean isDarkMode = true;
    private String themeCard = "#1a2a4a";
    private String themeCardBorder = "rgba(77,120,255,0.15)";
    private String themeText = "#e8eaf6";
    private String themeMuted = "#6b7394";
    private String themeInput = "#161a2e";

    // Panier page
    @FXML private TableView<PanierItem> panierTable;
    @FXML private TableColumn<PanierItem, String> colPanierNom;
    @FXML private TableColumn<PanierItem, Double> colPanierPrix;
    @FXML private TableColumn<PanierItem, Integer> colPanierQte;
    @FXML private TableColumn<PanierItem, Double> colPanierTotal;
    @FXML private Label lblTotalPanier, lblNbArticles, lblIaAnalyse, lblIaTotal;
    @FXML private VBox iaPredictiveBox, iaSuggestionsList;
    @FXML private TextField tfCodePromo;
    @FXML private Button btnAppliquerPromo;
    @FXML private Label lblReduction;

    private String codePromoActif = "";
    private double tauxReduction = 0;

    private final java.util.Map<String, Double> codesPromo = new java.util.HashMap<>();
    {
        codesPromo.put("BIENVENUE10", 0.10);
        codesPromo.put("CARTIX20", 0.20);
        codesPromo.put("ESPORT5", 0.05);
        codesPromo.put("PROMO15", 0.15);
    }

    @FXML public void appliquerCodePromo() {
        String code = tfCodePromo.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            showToast("Entrez un code promo", "warning");
            return;
        }
        Double taux = codesPromo.get(code);
        if (taux != null) {
            codePromoActif = code;
            tauxReduction = taux;
            lblReduction.setText("💰 Réduction appliquée : -" + String.format("%.0f", taux * 100) + "%");
            lblReduction.setTextFill(javafx.scene.paint.Color.web("#22d98a"));
            tfCodePromo.setDisable(true);
            btnAppliquerPromo.setDisable(true);
            rafraichirPanier();
            showToast("Code \" + code + \" appliqué, -" + String.format("%.0f", taux * 100) + "% !", "success");
        } else {
            lblReduction.setText("❌ Code invalide");
            lblReduction.setTextFill(javafx.scene.paint.Color.web("#ff4d6d"));
            showToast("Code promo \"" + code + "\" invalide", "error");
        }
    }

    // Commandes page
    @FXML private TableView<Commande> commandesTable;
    @FXML private TableColumn<Commande, Integer> colCmdId;
    @FXML private TableColumn<Commande, Date> colCmdDate;
    @FXML private TableColumn<Commande, Double> colCmdTotal;
    @FXML private TableColumn<Commande, String> colCmdStatut;
    @FXML private TableColumn<Commande, Void> colCmdAction;

    // Paiement page
    @FXML private TextField tfMontantPaiement, tfDescPaiement;
    @FXML private TextField tfCardNumber, tfCardExpiry, tfCardCvv, tfCardHolder;
    @FXML private ComboBox<Commande> cbPaiementCommande;
    @FXML private Label lblStatutPaiement;
    @FXML private ProgressIndicator progressPaiement;

    // Facture page
    @FXML private ComboBox<Commande> cbFactureCommande;
    @FXML private Label lblStatutFacture;

    // Livraison page
    @FXML private ComboBox<Commande> cbLivraisonCommande;
    @FXML private VBox livraisonCard;
    @FXML private VBox setupResultCard, setupPCList, setupPeriphList, budgetBarContainer;
    @FXML private HBox setupScreenRow, setupChaiseRow;
    @FXML private TextField tfBudget;
    @FXML private Button btnGenererSetup;
    @FXML private ProgressIndicator progressSetup;
    @FXML private Label lblSetupPC, lblSetupPeriph, lblSetupScreen, lblSetupChaise, lblSetupTotal;
    @FXML private VBox notificationsList;
    @FXML private ScrollPane notifScroll;
    @FXML private ScrollPane configScroll;
    @FXML private Region progressFill;
    @FXML private Label lblLivraisonStatut;
    @FXML private VBox step1, step2, step3, step4;

    // Email page
    @FXML private TextField tfEmailDest, tfEmailSujet;
    @FXML private TextArea taEmailContenu;
    @FXML private Label lblStatutEmail;
    @FXML private ProgressIndicator progressEmail;

    // Negociateur page
    @FXML private VBox negociateurCartList, offersContainer;
    @FXML private Label lblNegociateurTotal, lblNegociateurNbArticles, lblNegociateurMsg;
    @FXML private Button btnNegociateur;

    private final ProduitService produitService = new ProduitService();
    private final PanierService panierService = new PanierService();
    private final CommandeService commandeService = new CommandeService();
    private final StripeService stripeService = new StripeService();
    private final PDFService pdfService = new PDFService();
    private final MailService mailService = new MailService();
    private final SetupService setupService = new SetupService();

    private ObservableList<Produit> produitsList = FXCollections.observableArrayList();
    private ObservableList<PanierItem> cartItems = FXCollections.observableArrayList();
    private ObservableList<Commande> commandesList = FXCollections.observableArrayList();
    @FXML private VBox flashBanner;
    @FXML private Label lblFlashTimer, lblFlashDesc;

    private String userName = "Joueur";
    private String userEmail = "joueur@esport.com";
    private LocalDateTime flashEndTime;
    private Timeline flashTimeline;

    // Notifications
    private static class NotifEntry {
        final String message;
        final String type;
        final String time;
        NotifEntry(String message, String type) {
            this.message = message;
            this.type = type;
            this.time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        }
    }
    private final ObservableList<NotifEntry> notifHistory = FXCollections.observableArrayList();
    private int notifUnread = 0;
    private SetupService.SetupResult currentSetup;

    @FXML
    public void initialize() {
        chargerProduits();
        chargerCommandes();
        setupSearch();
        setupPanierTable();
        setupCommandesTable();
        setupFactureComboBox();
        setupPaiementComboBox();
        chargerNomUtilisateur();
        demarrerCompteARebours();
        if (productsScroll != null) productsScroll.viewportBoundsProperty().addListener((obs, old, b) -> {
            javafx.scene.Node vp = productsScroll.lookup(".viewport");
            if (vp != null) vp.setStyle("-fx-background-color: transparent;");
        });
        if (notifScroll != null) notifScroll.viewportBoundsProperty().addListener((obs, old, b) -> {
            javafx.scene.Node vp = notifScroll.lookup(".viewport");
            if (vp != null) vp.setStyle("-fx-background-color: transparent;");
        });
        if (configScroll != null) configScroll.viewportBoundsProperty().addListener((obs, old, b) -> {
            javafx.scene.Node vp = configScroll.lookup(".viewport");
            if (vp != null) vp.setStyle("-fx-background-color: transparent;");
        });
        setupLivraison();
        goToAccueil();
    }

    // ==================== COMPTE À REBOURS ====================
    private void demarrerCompteARebours() {
        flashEndTime = LocalDateTime.now().plusHours(2).plusMinutes(30);
        flashTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            long secs = LocalDateTime.now().until(flashEndTime, ChronoUnit.SECONDS);
            if (secs <= 0) {
                lblFlashTimer.setText("00 : 00 : 00");
                flashTimeline.stop();
                return;
            }
            long h = secs / 3600;
            long m = (secs % 3600) / 60;
            long s = secs % 60;
            lblFlashTimer.setText(String.format("%02d : %02d : %02d", h, m, s));
        }));
        flashTimeline.setCycleCount(Timeline.INDEFINITE);
        flashTimeline.play();
    }

    // ==================== NAVIGATION ====================
    private void setActiveButton(Button active) {
        String fs = "-fx-font-size: 14;";
        Button[] buttons = {btnAccueil, btnCommandes, btnPaiement, btnFactures, btnLivraison, btnConfigurateur, btnNegociateur, btnEmail};
        for (Button b : buttons) {
            b.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7394; -fx-font-weight: bold; " + fs + " -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        }
        active.setStyle("-fx-background-color: #4d78ff; -fx-text-fill: white; -fx-font-weight: bold; " + fs + " -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
    }

    @FXML public void goToAccueil() {
        setActiveButton(btnAccueil);
        switchPage(pageAccueil);
        rafraichirPanier();
    }
    @FXML public void goToCommandes() {
        setActiveButton(btnCommandes);
        switchPage(pageCommandes);
        chargerCommandes();
    }
    @FXML public void goToPaiement() {
        setActiveButton(btnPaiement);
        cbPaiementCommande.setItems(commandesList);
        cbPaiementCommande.setValue(null);
        chargerNomUtilisateur();
        double totalPanier = cartItems.stream().mapToDouble(PanierItem::getTotal).sum();
        tfMontantPaiement.setText(String.format(java.util.Locale.US, "%.2f", totalPanier));
        switchPage(pagePaiement);
    }

    private void chargerNomUtilisateur() {
        try (var conn = com.esport.services.DatabaseConnection.getConnection();
             var ps = conn.prepareStatement("SELECT nom, prenom, email FROM user WHERE Id=1");
             var rs = ps.executeQuery()) {
            if (rs.next()) {
                userName = rs.getString("nom");
                userEmail = rs.getString("email");
                tfDescPaiement.setText("Paiement par " + userName + " - Cartix");
                lblWelcome.setText("👋 Bienvenue, " + userName + " !");
            }
        } catch (Exception e) {
            tfDescPaiement.setText("Paiement sur Cartix");
        }
    }

    // ==================== THEME ====================
    @FXML public void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    private void applyTheme() {
        String bg = isDarkMode ? "#08090f" : "#f0f2f5";
        String sideTop = isDarkMode ? "#0d0f1a" : "#ffffff";
        String card = isDarkMode ? "#1a2a4a" : "#ffffff";
        String input = isDarkMode ? "#161a2e" : "#e8ecf0";
        String text = isDarkMode ? "#e8eaf6" : "#1a1a2e";
        String muted = isDarkMode ? "#6b7394" : "#6b7280";
        String border = isDarkMode ? "rgba(255,255,255,0.06)" : "rgba(0,0,0,0.08)";
        String cardBorder = isDarkMode ? "rgba(77,120,255,0.15)" : "rgba(77,120,255,0.2)";

        themeCard = card;
        themeCardBorder = cardBorder;
        themeText = text;
        themeMuted = muted;
        themeInput = input;

        btnThemeToggle.setText(isDarkMode ? "🌙" : "☀️");

        if (rootPane != null)
            rootPane.setStyle("-fx-background-color: " + bg + "; -fx-font-family: 'Segoe UI Emoji', 'Segoe UI', 'System', sans-serif;");
        if (topBar != null)
            topBar.setStyle("-fx-background-color: " + sideTop + "; -fx-padding: 0 24; -fx-border-color: " + border + "; -fx-border-width: 0 0 1 0;");
        if (sidebar != null) {
            sidebar.setStyle("-fx-background-color: " + sideTop + "; -fx-padding: 20 10; -fx-border-color: " + border + "; -fx-border-width: 0 1 0 0;");
            String sidebarBtn = "-fx-background-color: transparent; -fx-text-fill: " + muted + "; -fx-font-size: 13; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 12; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-max-width: Infinity;";
            for (javafx.scene.Node node : sidebar.getChildren()) {
                if (node instanceof Button b && !"🛒  Product Shop".equals(b.getText())) {
                    b.setStyle(sidebarBtn);
                }
            }
        }
        if (navBar != null)
            navBar.setStyle("-fx-background-color: " + card + "; -fx-background-radius: 12; -fx-padding: 10 20; -fx-border-color: " + cardBorder + "; -fx-border-radius: 12;");
        if (panierCard != null)
            panierCard.setStyle("-fx-background-color: " + card + "; -fx-background-radius: 12; -fx-padding: 20; -fx-border-color: " + cardBorder + "; -fx-border-radius: 12;");
        if (flashBanner != null)
            flashBanner.setStyle("-fx-background-color: " + (isDarkMode ? "linear-gradient(to right, #1a2a4a, #2a1a3a)" : "linear-gradient(to right, #ffffff, #faf0f5)") + "; -fx-background-radius: 12; -fx-padding: 14 20; -fx-border-color: #ff4d6d; -fx-border-radius: 12; -fx-border-width: 1.5;");

        String inputStyle = "-fx-background-color: " + input + "; -fx-text-fill: " + text + "; -fx-prompt-text-fill: " + muted + "; -fx-border-color: rgba(0,0,0,0.1); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;";
        String tableStyle = "-fx-background-color: " + card + "; -fx-control-inner-background: " + card + "; -fx-text-fill: " + text + "; -fx-selection-bar: #4d78ff; -fx-selection-bar-non-focused: #2a3a5a; -fx-table-cell-border-color: rgba(77,120,255,0.08); -fx-border-color: " + cardBorder + "; -fx-border-radius: 10; -fx-background-radius: 10;";

        if (searchField != null)
            searchField.setStyle("-fx-background-color: " + card + "; -fx-text-fill: " + text + "; -fx-prompt-text-fill: " + muted + "; -fx-border-color: " + cardBorder + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 12 16; -fx-font-size: 13;");
        if (panierTable != null) panierTable.setStyle(tableStyle);
        if (commandesTable != null) commandesTable.setStyle(tableStyle);
        if (lblWelcome != null) {
            lblWelcome.setStyle("-fx-font-size: 13;");
            lblWelcome.setTextFill(javafx.scene.paint.Color.web(muted));
        }
        if (lblFlashDesc != null)
            lblFlashDesc.setStyle("-fx-text-fill: " + text + "; -fx-font-size: 13;");
        if (lblTotalProducts != null) {
            lblTotalProducts.setStyle("-fx-font-size: 14;");
            lblTotalProducts.setTextFill(javafx.scene.paint.Color.web(muted));
        }

        // Page cards
        String pageCard = "-fx-background-color: " + card + "; -fx-background-radius: 12; -fx-padding: 25; -fx-border-color: " + cardBorder + "; -fx-border-radius: 12;";
        for (VBox page : new VBox[]{pageCommandes, pagePaiement, pageFacture, pageLivraison, pageConfigurateur, pageNotifications, pageEmail}) {
            if (page != null) {
                for (javafx.scene.Node child : page.getChildren()) {
                    if (child instanceof VBox vb) {
                        String cur = vb.getStyle();
                        if (cur != null && cur.contains("background-color")) {
                            vb.setStyle(pageCard);
                        }
                    }
                }
            }
        }

        // Payment/email fields & ComboBoxes
        String fieldStyle = "-fx-background-color: " + input + "; -fx-text-fill: " + text + "; -fx-prompt-text-fill: " + muted + "; -fx-border-color: rgba(0,0,0,0.1); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;";
        String comboStyle = "-fx-background-color: " + input + "; -fx-text-fill: " + text + "; -fx-border-color: rgba(0,0,0,0.1); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;";
        for (TextField tf : new TextField[]{tfMontantPaiement, tfDescPaiement, tfCardNumber, tfCardExpiry, tfCardCvv, tfCardHolder, tfEmailDest, tfEmailSujet}) {
            if (tf != null) tf.setStyle(fieldStyle);
        }
        for (ComboBox<?> cb : new ComboBox[]{cbPaiementCommande, cbFactureCommande, cbLivraisonCommande}) {
            if (cb != null) cb.setStyle(comboStyle);
        }
        if (taEmailContenu != null)
            taEmailContenu.setStyle("-fx-background-color: " + input + "; -fx-text-fill: " + text + "; -fx-prompt-text-fill: " + muted + "; -fx-border-color: rgba(0,0,0,0.1); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12;");

        if (livraisonCard != null)
            livraisonCard.setStyle("-fx-background-color: " + sideTop + "; -fx-background-radius: 12; -fx-padding: 25; -fx-border-color: " + cardBorder + "; -fx-border-radius: 12;");
        if (setupResultCard != null)
            setupResultCard.setStyle("-fx-background-color: " + sideTop + "; -fx-background-radius: 12; -fx-padding: 25; -fx-border-color: " + cardBorder + "; -fx-border-radius: 12;");
        if (progressFill != null && progressFill.getParent() instanceof StackPane track)
            track.setStyle("-fx-background-color: " + input + "; -fx-background-radius: 6; -fx-border-color: rgba(0,0,0,0.06); -fx-border-radius: 6;");
        if (iaPredictiveBox != null)
            iaPredictiveBox.setStyle("-fx-background-color: " + (isDarkMode ? "rgba(168,85,247,0.08)" : "rgba(168,85,247,0.05)") + "; -fx-background-radius: 10; -fx-padding: 12; -fx-border-color: rgba(168,85,247,0.2); -fx-border-radius: 10;");

        // Active nav button
        setActiveButton(btnAccueil);

        // Regenerate product cards with new theme
        afficherProduits();
    }

    @FXML public void goToFacture() {
        setActiveButton(btnFactures);
        switchPage(pageFacture);
        chargerFactureComboBox();
    }
    @FXML public void goToLivraison() {
        setActiveButton(btnLivraison);
        switchPage(pageLivraison);
        cbLivraisonCommande.setItems(commandesList);
        cbLivraisonCommande.setValue(null);
        livraisonCard.setVisible(false);
        livraisonCard.setManaged(false);
    }
    @FXML public void goToConfigurateur() {
        setActiveButton(btnConfigurateur);
        switchPage(pageConfigurateur);
        setupResultCard.setVisible(false);
        setupResultCard.setManaged(false);
    }

    @FXML public void genererSetup() {
        String budgetText = tfBudget.getText().trim();
        if (budgetText.isEmpty()) {
            showToast("Entrez un budget", "warning");
            return;
        }
        double budget;
        try {
            budget = Double.parseDouble(budgetText.replace(",", "."));
        } catch (NumberFormatException e) {
            showToast("Budget invalide", "error");
            return;
        }
        if (budget <= 0) {
            showToast("Le budget doit être supérieur à 0", "error");
            return;
        }

        progressSetup.setVisible(true);
        btnGenererSetup.setDisable(true);

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
        pause.setOnFinished(e -> {
            try {
                currentSetup = setupService.genererConfiguration(budget);
                afficherSetupResult();
            } catch (Exception ex) {
                showToast("Erreur lors de la génération", "error");
            }
            progressSetup.setVisible(false);
            btnGenererSetup.setDisable(false);
        });
        pause.play();
    }

    private void afficherSetupResult() {
        if (currentSetup == null) return;
        setupResultCard.setVisible(true);
        setupResultCard.setManaged(true);

        // PC section
        lblSetupPC.setText("💻 PC (" + String.format("%.0f", currentSetup.pcCost) + " TND)");
        setupPCList.getChildren().clear();
        for (SetupItem item : currentSetup.pcItems) {
            setupPCList.getChildren().add(creerSetupRow(item, "#22d98a"));
        }

        // Peripherals
        lblSetupPeriph.setText("🖱️ PÉRIPHÉRIQUES (" + String.format("%.0f", currentSetup.peripheralCost) + " TND)");
        setupPeriphList.getChildren().clear();
        for (SetupItem item : currentSetup.peripheralItems) {
            setupPeriphList.getChildren().add(creerSetupRow(item, "#4d78ff"));
        }

        // Screen
        lblSetupScreen.setText("🖥️ ÉCRAN (" + String.format("%.0f", currentSetup.screenCost) + " TND)");
        setupScreenRow.getChildren().clear();
        if (currentSetup.screenItem != null) {
            setupScreenRow.getChildren().add(creerSetupRow(currentSetup.screenItem, "#ffa500"));
        } else {
            setupScreenRow.setVisible(false);
        }

        // Chair
        lblSetupChaise.setText("🪑 CHAISE (" + String.format("%.0f", currentSetup.chaiseCost) + " TND)");
        setupChaiseRow.getChildren().clear();
        if (currentSetup.chaiseItem != null) {
            setupChaiseRow.getChildren().add(creerSetupRow(currentSetup.chaiseItem, "#a855f7"));
        } else {
            setupChaiseRow.setVisible(false);
        }

        // Budget bars
        budgetBarContainer.getChildren().clear();
        double t = currentSetup.totalCost;
        String[][] barDefs = {
            {"PC", String.format("%.0f", currentSetup.pcCost), String.format("%.0f", t > 0 ? currentSetup.pcCost / t * 100 : 0), "#4d78ff"},
            {"Périphériques", String.format("%.0f", currentSetup.peripheralCost), String.format("%.0f", t > 0 ? currentSetup.peripheralCost / t * 100 : 0), "#22d98a"},
            {"Écran", String.format("%.0f", currentSetup.screenCost), String.format("%.0f", t > 0 ? currentSetup.screenCost / t * 100 : 0), "#ffa500"},
            {"Chaise", String.format("%.0f", currentSetup.chaiseCost), String.format("%.0f", t > 0 ? currentSetup.chaiseCost / t * 100 : 0), "#a855f7"}
        };
        for (String[] def : barDefs) {
            budgetBarContainer.getChildren().add(creerBudgetBar(def[0], def[1], def[2], def[3]));
        }

        // Total
        String remainingText = currentSetup.remaining >= 0 ?
            String.format("Solde restant : %.2f TND ", currentSetup.remaining) + (currentSetup.remaining < 1 ? "(parfait !)" : "") :
            String.format("Dépassement : %.2f TND", Math.abs(currentSetup.remaining));
        lblSetupTotal.setText("📊 Total: " + String.format("%.2f TND", currentSetup.totalCost) + " | " + remainingText);
        lblSetupTotal.setTextFill(javafx.scene.paint.Color.web(currentSetup.remaining >= 0 ? "#22d98a" : "#ff4d6d"));
    }

    private HBox creerSetupRow(SetupItem item, String accentColor) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #1a2a4a; -fx-background-radius: 8; -fx-padding: 10 14; -fx-border-color: rgba(77,120,255,0.1); -fx-border-radius: 8;");
        Label iconLbl = new Label(item.getIcon());
        iconLbl.setStyle("-fx-font-size: 16;");
        Label nameLbl = new Label(item.getLabel());
        nameLbl.setStyle("-fx-text-fill: #e8eaf6; -fx-font-size: 13; -fx-font-weight: bold;");
        nameLbl.setWrapText(true);
        nameLbl.setMaxWidth(350);
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label priceLbl = new Label(String.format("%.2f TND", item.getPrice()));
        priceLbl.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-size: 13; -fx-font-weight: bold;");
        if (item.isEstimated()) {
            Label estLbl = new Label("(estimé)");
            estLbl.setStyle("-fx-text-fill: #ffa500; -fx-font-size: 11; -fx-font-style: italic;");
            row.getChildren().addAll(iconLbl, nameLbl, spacer, priceLbl, estLbl);
        } else {
            row.getChildren().addAll(iconLbl, nameLbl, spacer, priceLbl);
        }
        return row;
    }

    private HBox creerBudgetBar(String label, String cost, String pct, String color) {
        HBox bar = new HBox(8);
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #6b7394; -fx-font-size: 12; -fx-font-weight: bold; -fx-min-width: 110;");
        StackPane track = new StackPane();
        track.setPrefHeight(14);
        track.setMaxWidth(300);
        track.setStyle("-fx-background-color: #161a2e; -fx-background-radius: 7;");
        Region fill = new Region();
        double pctVal = 0;
        try { pctVal = Math.min(Double.parseDouble(pct), 100); } catch (Exception e) {}
        fill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 7;");
        fill.setPrefWidth(300 * pctVal / 100.0);
        track.getChildren().add(fill);
        Label pctLbl = new Label(pct + "%");
        pctLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12; -fx-font-weight: bold; -fx-min-width: 40;");
        Label costLbl = new Label(cost + " TND");
        costLbl.setStyle("-fx-text-fill: #e8eaf6; -fx-font-size: 12;");
        bar.getChildren().addAll(lbl, track, pctLbl, costLbl);
        return bar;
    }

    @FXML public void ajouterSetupAuPanier() {
        if (currentSetup == null) return;
        int added = 0;
        for (SetupItem item : currentSetup.pcItems) {
            if (item.getProduct() != null && !item.isEstimated()) {
                ajouterAuPanier(item.getProduct());
                added++;
            }
        }
        for (SetupItem item : currentSetup.peripheralItems) {
            if (item.getProduct() != null && !item.isEstimated()) {
                ajouterAuPanier(item.getProduct());
                added++;
            }
        }
        if (currentSetup.screenItem != null && currentSetup.screenItem.getProduct() != null && !currentSetup.screenItem.isEstimated()) {
            ajouterAuPanier(currentSetup.screenItem.getProduct());
            added++;
        }
        if (currentSetup.chaiseItem != null && currentSetup.chaiseItem.getProduct() != null && !currentSetup.chaiseItem.isEstimated()) {
            ajouterAuPanier(currentSetup.chaiseItem.getProduct());
            added++;
        }
        showToast(added + " article(s) ajoutés au panier depuis la configuration", "success");
    }

    @FXML public void resetConfigurateur() {
        tfBudget.clear();
        setupResultCard.setVisible(false);
        setupResultCard.setManaged(false);
        currentSetup = null;
        setupPCList.getChildren().clear();
        setupPeriphList.getChildren().clear();
        setupScreenRow.getChildren().clear();
        setupChaiseRow.getChildren().clear();
        budgetBarContainer.getChildren().clear();
    }

    @FXML public void goToNotifications() {
        switchPage(pageNotifications);
        notifUnread = 0;
        if (lblNotifBadge != null) {
            lblNotifBadge.setVisible(false);
            lblNotifBadge.setText("0");
        }
        afficherNotifications();
    }
    @FXML public void viderNotifications() {
        notifHistory.clear();
        notificationsList.getChildren().clear();
        notifUnread = 0;
        if (lblNotifBadge != null) {
            lblNotifBadge.setVisible(false);
            lblNotifBadge.setText("0");
        }
    }
    private void afficherNotifications() {
        notificationsList.getChildren().clear();
        if (notifHistory.isEmpty()) {
            Label empty = new Label("📭 Aucune notification");
            empty.setStyle("-fx-text-fill: #6b7394; -fx-font-size: 14; -fx-padding: 20;");
            empty.setAlignment(javafx.geometry.Pos.CENTER);
            notificationsList.getChildren().add(empty);
            return;
        }
        for (int i = notifHistory.size() - 1; i >= 0; i--) {
            NotifEntry e = notifHistory.get(i);
            HBox row = new HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            String icon;
            switch (e.type) {
                case "success": icon = "✅"; break;
                case "error": icon = "❌"; break;
                case "warning": icon = "⚠️"; break;
                default: icon = "ℹ️"; break;
            }
            Label iconLbl = new Label(icon);
            iconLbl.setStyle("-fx-font-size: 16;");
            Label msgLbl = new Label(e.message);
            msgLbl.setStyle("-fx-text-fill: " + themeText + "; -fx-font-size: 13;");
            msgLbl.setWrapText(true);
            msgLbl.setMaxWidth(400);
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            Label timeLbl = new Label(e.time);
            timeLbl.setStyle("-fx-text-fill: " + themeMuted + "; -fx-font-size: 11;");
            row.getChildren().addAll(iconLbl, msgLbl, spacer, timeLbl);
            row.setStyle("-fx-background-color: " + themeCard + "; -fx-background-radius: 8; -fx-padding: 10 14; -fx-border-color: " + themeCardBorder + "; -fx-border-radius: 8;");
            notificationsList.getChildren().add(row);
        }
    }
    @FXML public void goToEmail() {
        setActiveButton(btnEmail);
        switchPage(pageEmail);
    }

    @FXML public void sidebarPlaceholder() {
        showToast("Module en cours d'intégration par un autre collaborateur", "info");
    }

    @FXML public void goToNegociateur() {
        setActiveButton(btnNegociateur);
        switchPage(pageNegociateur);
        afficherPanierNegociateur();
    }

    private void afficherPanierNegociateur() {
        negociateurCartList.getChildren().clear();
        if (cartItems.isEmpty()) {
            Label empty = new Label("🛒 Votre panier est vide — ajoutez des produits d'abord");
            empty.setStyle("-fx-text-fill: #6b7394; -fx-font-size: 14; -fx-padding: 20;");
            empty.setAlignment(javafx.geometry.Pos.CENTER);
            negociateurCartList.getChildren().add(empty);
            lblNegociateurTotal.setText("0.00 TND");
            lblNegociateurNbArticles.setText("📦 0 art.");
            lblNegociateurMsg.setText("💬 IA : Ajoutez des articles à votre panier depuis l'Accueil pour que je puisse vous aider à négocier !");
            offersContainer.getChildren().clear();
            return;
        }

        for (PanierItem item : cartItems) {
            HBox row = new HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #0d0f1a; -fx-background-radius: 8; -fx-padding: 10 14; -fx-border-color: rgba(77,120,255,0.1); -fx-border-radius: 8;");
            Label icon = new Label("📦");
            icon.setStyle("-fx-font-size: 16;");
            Label name = new Label(item.getProduit().getNom());
            name.setStyle("-fx-text-fill: #e8eaf6; -fx-font-size: 13; -fx-font-weight: bold;");
            name.setWrapText(true);
            name.setMaxWidth(200);
            Region spacer1 = new Region();
            HBox.setHgrow(spacer1, javafx.scene.layout.Priority.ALWAYS);
            Label qty = new Label("x" + item.getQuantite());
            qty.setStyle("-fx-text-fill: #6b7394; -fx-font-size: 13;");
            Label price = new Label(String.format("%.2f TND", item.getTotal()));
            price.setStyle("-fx-text-fill: #22d98a; -fx-font-size: 13; -fx-font-weight: bold;");
            row.getChildren().addAll(icon, name, spacer1, qty, price);
            negociateurCartList.getChildren().add(row);
        }

        double total = cartItems.stream().mapToDouble(PanierItem::getTotal).sum();
        lblNegociateurTotal.setText(String.format("%.2f TND", total));
        lblNegociateurNbArticles.setText("📦 " + cartItems.size() + " art.");

        lblNegociateurMsg.setText("💬 IA : Je vois que votre panier est à " + String.format("%.2f TND", total) + ". J'ai analysé vos articles et je peux vous proposer des offres spéciales !");
        offersContainer.getChildren().clear();
    }

    @FXML public void genererOffresNegociateur() {
        if (cartItems.isEmpty()) {
            showToast("Panier vide — ajoutez des produits d'abord", "warning");
            return;
        }

        double total = cartItems.stream().mapToDouble(PanierItem::getTotal).sum();
        boolean hasExpensiveItem = cartItems.stream().anyMatch(item -> item.getProduit().getPrix() > 50);

        // Find a mousepad for offer 2
        java.util.List<Produit> allProducts = produitService.getAllProduits();
        Produit tapis = trouverProduitParCategorie(allProducts, "Tapis");

        offersContainer.getChildren().clear();

        // Message
        lblNegociateurMsg.setText("💬 IA : Voici les meilleures offres que je peux vous proposer pour votre panier de " + String.format("%.2f TND", total) + " :");

        // Offer 1: -5% on total, no condition
        VBox offer1 = creerOffreCard(
                "🎁 OFFRE 1 : -5% sur le total",
                "💰 " + String.format("%.2f TND", total * 0.95) + " au lieu de " + String.format("%.2f TND", total),
                "Condition : aucune, offre de bienvenue",
                0.05, null, true
        );

        // Offer 2: -10% + free mousepad, condition: at least 1 item > 300
        String offer2Cond = hasExpensiveItem ? "✅ Condition remplie (article > 300 TND)" : "❌ Condition : ajouter 1 article à plus de 50€";
        VBox offer2 = creerOffreCard(
                "🎁 OFFRE 2 : -10% " + (tapis != null ? "+ tapis offert" : ""),
                "💰 " + String.format("%.2f TND", total * 0.90) + " au lieu de " + String.format("%.2f TND", total) + (tapis != null ? " + " + tapis.getNom() + " gratuit" : ""),
                "Condition : " + offer2Cond,
                0.10, tapis, hasExpensiveItem
        );

        // Offer 3: -15% sur cette commande, condition: finaliser maintenant
        VBox offer3 = creerOffreCard(
                "🎁 OFFRE 3 : -15% sur cette commande",
                "💰 " + String.format("%.2f TND", total * 0.85) + " au lieu de " + String.format("%.2f TND", total),
                "Condition : finaliser cette commande maintenant",
                0.15, null, true
        );

        offersContainer.getChildren().addAll(offer1, offer2, offer3);

        showToast("3 offres spéciales générées par l'IA !", "success");
    }

    private VBox creerOffreCard(String title, String priceText, String condition, double reduction, Produit freeProduct, boolean conditionMet) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #0d0f1a; -fx-background-radius: 10; -fx-padding: 16; -fx-border-color: rgba(168,85,247,0.2); -fx-border-radius: 10;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: #a855f7; -fx-font-size: 14; -fx-font-weight: bold;");

        Label priceLbl = new Label(priceText);
        priceLbl.setStyle("-fx-text-fill: #22d98a; -fx-font-size: 13; -fx-font-weight: bold;");

        Label condLbl = new Label(condition);
        condLbl.setStyle("-fx-text-fill: " + (conditionMet ? "#22d98a" : "#ff4d6d") + "; -fx-font-size: 12;");

        Button acceptBtn = new Button("💰 Accepter");
        acceptBtn.setStyle("-fx-background-color: #4d78ff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-size: 13;");
        acceptBtn.setOnAction(e -> accepterOffre(reduction, freeProduct, conditionMet));

        HBox btnRow = new HBox();
        btnRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        btnRow.getChildren().add(acceptBtn);

        card.getChildren().addAll(titleLbl, priceLbl, condLbl, btnRow);
        return card;
    }

    private void accepterOffre(double reduction, Produit freeProduct, boolean conditionMet) {
        if (!conditionMet) {
            showToast("Condition non remplie pour cette offre", "error");
            return;
        }

        tauxReduction = reduction;
        codePromoActif = "IA_NEGO";

        if (freeProduct != null) {
            ajouterAuPanier(freeProduct);
        }

        rafraichirPanier();
        double total = cartItems.stream().mapToDouble(PanierItem::getTotal).sum();
        double totalReduit = total * (1 - tauxReduction);
        showToast("Offre acceptée ! Réduction de " + String.format("%.0f", reduction * 100) + "% appliquée" + (freeProduct != null ? " + " + freeProduct.getNom() + " ajouté" : ""), "success");
        goToPaiement();
        tfMontantPaiement.setText(String.format(java.util.Locale.US, "%.2f", totalReduit));
    }

    // ==================== LIVRAISON ====================
    private double livraisonPct = 0;

    private void setupLivraison() {
        cbLivraisonCommande.setOnAction(e -> {
            Commande cmd = cbLivraisonCommande.getValue();
            if (cmd != null) majLivraisonProgress(cmd);
        });
        progressFill.parentProperty().addListener((obs, old, parent) -> {
            if (parent instanceof StackPane sp) {
                sp.widthProperty().addListener((wObs, oldW, newW) ->
                        progressFill.setPrefWidth(newW.doubleValue() * livraisonPct / 100.0));
                Platform.runLater(() -> progressFill.setPrefWidth(sp.getWidth() * livraisonPct / 100.0));
            }
        });
    }

    @FXML public void actualiserLivraison() {
        chargerCommandes();
        Commande cmd = cbLivraisonCommande.getValue();
        if (cmd != null) {
            // refresh from DB
            try (var conn = com.esport.services.DatabaseConnection.getConnection();
                 var ps = conn.prepareStatement("SELECT * FROM commande WHERE id=?")) {
                ps.setInt(1, cmd.getId());
                var rs = ps.executeQuery();
                if (rs.next()) {
                    cmd.setStatutLivraison(rs.getString("statut_livraison"));
                    cmd.setTotal(rs.getDouble("total"));
                }
            } catch (Exception ex) { /* ignore */ }
            majLivraisonProgress(cmd);
            showToast("Livraison actualisée", "success");
        }
    }

    private void majLivraisonProgress(Commande cmd) {
        livraisonCard.setVisible(true);
        livraisonCard.setManaged(true);

        String statut = cmd.getStatutLivraison() != null ? cmd.getStatutLivraison() : "EN_ATTENTE";
        int activeStep;
        String iconText;
        switch (statut) {
            case "EXPEDIEE":
                livraisonPct = 33; activeStep = 2; iconText = "🚚 Expédiée"; break;
            case "EN_COURS_DE_LIVRAISON":
                livraisonPct = 66; activeStep = 3; iconText = "📦 En livraison"; break;
            case "LIVREE":
                livraisonPct = 100; activeStep = 4; iconText = "🏠 Livrée"; break;
            case "ANNULEE":
                livraisonPct = 0; activeStep = -1; iconText = "❌ Annulée"; break;
            default:
                livraisonPct = 0; activeStep = 1; iconText = "✅ Commandée"; break;
        }

        // color
        String fillColor;
        if (statut.equals("ANNULEE")) {
            fillColor = "#ff4d6d";
        } else if (livraisonPct >= 100) {
            fillColor = "#22d98a";
        } else {
            fillColor = "#4d78ff";
        }

        // progress fill width via binding
        if (progressFill.getParent() instanceof StackPane sp) {
            progressFill.setPrefWidth(sp.getWidth() * livraisonPct / 100.0);
        }
        progressFill.setStyle("-fx-background-color: " + fillColor + "; -fx-background-radius: 6;");

        // status text
        lblLivraisonStatut.setText("Statut: " + iconText);
        lblLivraisonStatut.setTextFill(javafx.scene.paint.Color.web(fillColor));

        // step indicators
        VBox[] steps = new VBox[]{step1, step2, step3, step4};
        for (int i = 0; i < 4; i++) {
            Label stepLabel = (Label) steps[i].getChildren().get(1);
            Label iconLabel = (Label) steps[i].getChildren().get(0);
            if (statut.equals("ANNULEE")) {
                stepLabel.setTextFill(javafx.scene.paint.Color.web("#6b7394"));
                iconLabel.setOpacity(0.4);
            } else if (i + 1 <= activeStep) {
                stepLabel.setTextFill(javafx.scene.paint.Color.web(fillColor));
                iconLabel.setOpacity(1.0);
            } else {
                stepLabel.setTextFill(javafx.scene.paint.Color.web("#6b7394"));
                iconLabel.setOpacity(0.4);
            }
        }
    }

    private void switchPage(VBox page) {
        pageAccueil.setVisible(false);
        pageAccueil.setManaged(false);
        pageCommandes.setVisible(false);
        pageCommandes.setManaged(false);
        pagePaiement.setVisible(false);
        pagePaiement.setManaged(false);
        pageFacture.setVisible(false);
        pageFacture.setManaged(false);
        pageLivraison.setVisible(false);
        pageLivraison.setManaged(false);
        pageConfigurateur.setVisible(false);
        pageConfigurateur.setManaged(false);
        pageNotifications.setVisible(false);
        pageNotifications.setManaged(false);
        pageEmail.setVisible(false);
        pageEmail.setManaged(false);
        pageNegociateur.setVisible(false);
        pageNegociateur.setManaged(false);

        page.setVisible(true);
        page.setManaged(true);
    }

    // ==================== PRODUITS ====================
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
        card.setStyle("-fx-background-color: " + themeCard + "; -fx-background-radius: 12; -fx-border-color: " + themeCardBorder + "; -fx-border-radius: 12;");
        card.setPrefWidth(210);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER);

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(180, 140);
        imageContainer.setStyle("-fx-background-color: " + themeInput + "; -fx-background-radius: 10;");

        ImageView productImage = new ImageView();
        productImage.setFitWidth(160);
        productImage.setFitHeight(120);
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
        nomLabel.setStyle("-fx-text-fill: " + themeText + "; -fx-font-size: 14; -fx-font-weight: bold;");
        nomLabel.setWrapText(true);
        nomLabel.setAlignment(Pos.CENTER);

        Label prixLabel = new Label(String.format("%.2f TND", p.getPrix()));
        prixLabel.setStyle("-fx-text-fill: #ff4d6d; -fx-font-size: 16; -fx-font-weight: bold;");

        Label stockLabel;
        if (p.getStock() <= 5) {
            stockLabel = new Label("\uD83D\uDFE0 " + p.getStock());
            stockLabel.setStyle("-fx-background-color: rgba(255,165,0,0.15); -fx-text-fill: #ffa500; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 2 10; -fx-font-size: 12;");
        } else {
            stockLabel = new Label("Stock: " + p.getStock());
            stockLabel.setStyle("-fx-text-fill: " + themeMuted + "; -fx-font-size: 12; -fx-font-weight: bold;");
        }

        Button addBtn = new Button("🛒 Ajouter");
        addBtn.setStyle("-fx-background-color: #4d78ff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 12; -fx-font-size: 13;");

        if (p.getStock() <= 0) {
            addBtn.setDisable(true);
            addBtn.setText("❌ Rupture");
        } else {
            addBtn.setOnMouseClicked(e -> ajouterAuPanier(p));
        }

        Label noteLabel = new Label(etoiles(p.getNote()) + " " + String.format("%.1f", p.getNote()) + "/5 (" + p.getNbAvis() + " avis)");
        noteLabel.setStyle("-fx-text-fill: " + themeMuted + "; -fx-font-size: 11;");
        noteLabel.setAlignment(Pos.CENTER);

        // 3D hover effect
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(javafx.scene.paint.Color.rgb(77, 120, 255, 0.3));
        shadow.setRadius(0);
        shadow.setOffsetY(0);
        card.setEffect(null);
        javafx.animation.ScaleTransition scaleUp = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), card);
        scaleUp.setToX(1.04);
        scaleUp.setToY(1.04);
        javafx.animation.ScaleTransition scaleDown = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        javafx.animation.TranslateTransition liftUp = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(200), card);
        liftUp.setToY(-6);
        javafx.animation.TranslateTransition liftDown = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(200), card);
        liftDown.setToY(0);
        javafx.animation.Timeline shadowIn = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(200),
                        new javafx.animation.KeyValue(shadow.radiusProperty(), 20),
                        new javafx.animation.KeyValue(shadow.offsetYProperty(), 8))
        );
        javafx.animation.Timeline shadowOut = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(200),
                        new javafx.animation.KeyValue(shadow.radiusProperty(), 0),
                        new javafx.animation.KeyValue(shadow.offsetYProperty(), 0))
        );
        card.setOnMouseEntered(e -> {
            card.setEffect(shadow);
            scaleUp.play();
            liftUp.play();
            shadowIn.play();
        });
        card.setOnMouseExited(e -> {
            scaleDown.play();
            liftDown.play();
            shadowOut.play();
            shadowOut.setOnFinished(ev -> card.setEffect(null));
        });

        card.getChildren().addAll(imageContainer, nomLabel, prixLabel, stockLabel, noteLabel, addBtn);
        productsGrid.getChildren().add(card);
    }

    private void ajouterAuPanier(Produit p) {
        if (p.getStock() <= 0) {
            showToast(p.getNom() + " — stock épuisé", "error");
            return;
        }

        for (PanierItem item : cartItems) {
            if (item.getProduit().getId() == p.getId()) {
                if (item.getQuantite() + 1 <= p.getStock()) {
                    item.setQuantite(item.getQuantite() + 1);
                } else {
                    showToast("Stock limité (" + p.getStock() + ") pour " + p.getNom(), "warning");
                }
                rafraichirPanier();
                return;
            }
        }
        cartItems.add(new PanierItem(p, 1));
        rafraichirPanier();
        showToast(p.getNom() + " ajouté au panier", "success");
    }

    // ==================== PANIER ====================
    private void setupPanierTable() {
        colPanierNom.setCellValueFactory(cellData -> cellData.getValue().getProduit().nomProperty());
        colPanierPrix.setCellValueFactory(cellData -> cellData.getValue().getProduit().prixProperty().asObject());
        colPanierQte.setCellValueFactory(cellData -> cellData.getValue().quantiteProperty().asObject());
        colPanierTotal.setCellValueFactory(cellData -> cellData.getValue().totalProperty().asObject());

        // Éditeur de quantité
        colPanierQte.setCellFactory(column -> new TableCell<PanierItem, Integer>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 999, 1);
            {
                spinner.valueProperty().addListener((obs, old, newVal) -> {
                    PanierItem item = getTableView().getItems().get(getIndex());
                    if (item != null && newVal != null) {
                        item.setQuantite(newVal);
                        rafraichirPanier();
                    }
                });
            }
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    spinner.getValueFactory().setValue(item);
                    setGraphic(spinner);
                }
            }
        });

        panierTable.setItems(cartItems);
    }

    private void rafraichirPanier() {
        panierTable.refresh();
        double total = cartItems.stream().mapToDouble(PanierItem::getTotal).sum();
        double totalReduit = total * (1 - tauxReduction);
        if (tauxReduction > 0) {
            lblTotalPanier.setText(String.format("%.2f TND", totalReduit));
        } else {
            lblTotalPanier.setText(String.format("%.2f TND", total));
        }
        lblNbArticles.setText(cartItems.size() + " article(s)");
        mettreAJourIaPredictive();
    }

    // ==================== IA PRÉDICTIVE ====================
    private java.util.List<Produit> iaSuggestedProducts = new java.util.ArrayList<>();

    private void mettreAJourIaPredictive() {
        if (iaPredictiveBox == null) return;
        iaSuggestedProducts.clear();
        iaSuggestionsList.getChildren().clear();

        if (cartItems.isEmpty()) {
            iaPredictiveBox.setVisible(false);
            iaPredictiveBox.setManaged(false);
            return;
        }

        // Detect what's in the cart
        boolean hasSouris = false, hasClavier = false, hasCasque = false;
        boolean hasTapis = false, hasEcran = false;
        boolean hasComposant = false;

        java.util.Set<String> cartCategories = new java.util.HashSet<>();
        for (PanierItem item : cartItems) {
            String cat = item.getProduit().getCategorie().toLowerCase();
            cartCategories.add(cat);
            if (cat.contains("souris")) hasSouris = true;
            else if (cat.contains("clavier")) hasClavier = true;
            else if (cat.contains("casque")) hasCasque = true;
            else if (cat.contains("tapis")) hasTapis = true;
            else if (cat.contains("ecran")) hasEcran = true;
            else if (cat.contains("composant")) hasComposant = true;
        }

        // Build suggestions
        java.util.List<Produit> allProducts = produitService.getAllProduits();
        java.util.List<String> analyseLines = new java.util.ArrayList<>();

        // Rule 1: mouse + no mousepad → suggest mousepad
        if (hasSouris && !hasTapis) {
            Produit tapis = trouverProduitParCategorie(allProducts, "Tapis");
            if (tapis != null && !dejaDansPanier(tapis)) {
                iaSuggestedProducts.add(tapis);
                analyseLines.add("🖱️ Les clients qui achètent une souris ajoutent aussi un tapis (85%)");
            }
        }

        // Rule 2: keyboard + no headset → suggest headset
        if (hasClavier && !hasCasque) {
            Produit casque = trouverProduitParCategorie(allProducts, "Casque");
            if (casque != null && !dejaDansPanier(casque)) {
                iaSuggestedProducts.add(casque);
                analyseLines.add("⌨️ 78% des clients avec un clavier ajoutent un casque gaming");
            }
        }

        // Rule 3: PC components + no screen → suggest screen
        if (hasComposant && !hasEcran) {
            Produit ecran = trouverProduitParCategorie(allProducts, "Ecran");
            if (ecran != null && !dejaDansPanier(ecran)) {
                iaSuggestedProducts.add(ecran);
                analyseLines.add("🖥️ Un écran gaming complète parfaitement votre setup PC (92%)");
            }
        }

        // Rule 4: has peripherals but no mouse → suggest mouse
        if ((hasClavier || hasCasque) && !hasSouris) {
            Produit souris = trouverProduitParCategorie(allProducts, "Souris");
            if (souris != null && !dejaDansPanier(souris)) {
                iaSuggestedProducts.add(souris);
                analyseLines.add("🖱️ Une souris gaming est indispensable avec votre setup (95%)");
            }
        }

        if (iaSuggestedProducts.isEmpty()) {
            iaPredictiveBox.setVisible(false);
            iaPredictiveBox.setManaged(false);
            return;
        }

        // Analysis text
        StringBuilder analyse = new StringBuilder("📊 Analyse de votre panier :\n");
        for (String line : analyseLines) {
            analyse.append("• ").append(line).append("\n");
        }
        lblIaAnalyse.setText(analyse.toString().trim());

        // Product suggestions
        double totalSuggestions = 0;
        for (Produit p : iaSuggestedProducts) {
            HBox row = new HBox(8);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label icon = new Label(p.getCategorie().contains("Souris") ? "\uD83D\uDDB1\uFE0F" :
                    p.getCategorie().contains("Clavier") ? "\u2328\uFE0F" :
                    p.getCategorie().contains("Casque") ? "\uD83C\uDFA7" :
                    p.getCategorie().contains("Tapis") ? "\uD83D\uDFE9" :
                    p.getCategorie().contains("Ecran") ? "\uD83D\uDDA5\uFE0F" : "\uD83D\uDCE6");
            icon.setStyle("-fx-font-size: 14;");
            Label name = new Label(p.getNom());
            name.setStyle("-fx-text-fill: #e8eaf6; -fx-font-size: 12; -fx-font-weight: bold;");
            name.setWrapText(true);
            name.setMaxWidth(150);
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            Label price = new Label(String.format("%.2f TND", p.getPrix()));
            price.setStyle("-fx-text-fill: #22d98a; -fx-font-size: 12; -fx-font-weight: bold;");
            row.getChildren().addAll(icon, name, spacer, price);
            iaSuggestionsList.getChildren().add(row);
            totalSuggestions += p.getPrix();
        }

        // Total if added
        double currentTotal = cartItems.stream().mapToDouble(PanierItem::getTotal).sum();
        double newTotal = currentTotal + totalSuggestions;
        lblIaTotal.setText("💰 Si vous ajoutez ces articles : " + String.format("%.2f TND → %.2f TND", currentTotal, newTotal));

        iaPredictiveBox.setVisible(true);
        iaPredictiveBox.setManaged(true);
    }

    private Produit trouverProduitParCategorie(java.util.List<Produit> produits, String categorie) {
        return produits.stream()
                .filter(p -> categorie.equalsIgnoreCase(p.getCategorie()))
                .findFirst().orElse(null);
    }

    private boolean dejaDansPanier(Produit p) {
        return cartItems.stream().anyMatch(item -> item.getProduit().getId() == p.getId());
    }

    @FXML public void ajouterIaSuggestions() {
        for (Produit p : iaSuggestedProducts) {
            ajouterAuPanier(p);
        }
        showToast(iaSuggestedProducts.size() + " article(s) ajoutés via l'IA prédictive", "success");
        iaPredictiveBox.setVisible(false);
        iaPredictiveBox.setManaged(false);
    }

    @FXML public void ignorerIaSuggestions() {
        iaPredictiveBox.setVisible(false);
        iaPredictiveBox.setManaged(false);
        showToast("Suggestions ignorées", "warning");
    }

    @FXML public void viderPanier() {
        if (!cartItems.isEmpty()) {
            cartItems.clear();
            codePromoActif = "";
            tauxReduction = 0;
            lblReduction.setText("");
            tfCodePromo.setDisable(false);
            btnAppliquerPromo.setDisable(false);
            tfCodePromo.setText("");
            rafraichirPanier();
            showToast("Panier vidé", "success");
        }
    }

    @FXML public void passerCommande() {
        if (cartItems.isEmpty()) {
            showToast("Panier vide — ajoutez des produits", "warning");
            return;
        }

        double total = cartItems.stream().mapToDouble(PanierItem::getTotal).sum();
        double totalFinal = total * (1 - tauxReduction);

        Commande commande = new Commande();
        commande.setClientNom(userName);
        commande.setClientEmail(userEmail);
        commande.setDateCommande(new Date());
        commande.setTotal(totalFinal);
        commande.setStatutLivraison("EN_ATTENTE");

        if (commandeService.sauvegarderCommande(commande)) {
            for (PanierItem item : cartItems) {
                produitService.diminuerStock(item.getProduit().getId(), item.getQuantite());
            }
            chargerProduits();

            showToast("Commande #" + commande.getId() + " confirmée (" + String.format("%.2f TND", totalFinal) + ")", "success");
            String promoHtml = tauxReduction > 0 ? "<p>Code promo: " + codePromoActif + " (-" + String.format("%.0f", tauxReduction * 100) + "%)</p>" : "";
            mailService.envoyerEmail(commande.getClientEmail(),
                    "Confirmation de commande #" + commande.getId(),
                    "<html><body style='font-family:sans-serif;'><h2>Merci pour votre commande !</h2>" +
                            "<p><strong>Cartix</strong> - Esport Shop</p>" +
                            "<p>Commande N°" + commande.getId() + "</p>" +
                            "<p>Montant: " + String.format("%.2f TND", totalFinal) + "</p>" +
                            promoHtml +
                            "<p>Statut: En attente de traitement</p></body></html>");

            cartItems.clear();
            codePromoActif = "";
            tauxReduction = 0;
            lblReduction.setText("");
            tfCodePromo.setDisable(false);
            btnAppliquerPromo.setDisable(false);
            tfCodePromo.setText("");
            rafraichirPanier();
            chargerCommandes();
        } else {
            showToast("Erreur lors de la commande", "error");
        }
    }

    // ==================== COMMANDES ====================
    private void setupCommandesTable() {
        colCmdId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCmdDate.setCellValueFactory(new PropertyValueFactory<>("dateCommande"));
        colCmdTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colCmdStatut.setCellValueFactory(new PropertyValueFactory<>("statutLivraison"));

        // Badges statuts
        colCmdStatut.setCellFactory(column -> new TableCell<Commande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(creerBadgeStatut(item));
                    setText(null);
                }
            }
        });

        commandesTable.setItems(commandesList);
    }

    private void chargerCommandes() {
        commandesList.clear();
        commandesList.addAll(commandeService.getAllCommandes());
    }

    // ==================== PAIEMENT STRIPE ====================
    @FXML public void lancerPaiement() {
        String montant = tfMontantPaiement.getText();
        String description = tfDescPaiement.getText();
        String cardNumber = tfCardNumber.getText();
        String cardExpiry = tfCardExpiry.getText();
        String cardCvv = tfCardCvv.getText();
        String cardHolder = tfCardHolder.getText();

        if (montant == null || montant.isEmpty()) {
            showToast("Entrez un montant", "error");
            return;
        }
        double montantVal;
        try {
            montantVal = Double.parseDouble(montant.replace(",", "."));
        } catch (NumberFormatException e) {
            showToast("Montant invalide", "error");
            return;
        }
        if (montantVal <= 0) {
            showToast("Le montant doit être supérieur à 0", "error");
            return;
        }
        if (cardNumber == null || cardNumber.replace(" ", "").length() < 16) {
            showToast("Numéro de carte invalide (16 chiffres requis)", "error");
            return;
        }
        if (cardExpiry == null || cardExpiry.length() < 4) {
            showToast("Date d'expiration invalide (MM/AA)", "error");
            return;
        }
        if (cardCvv == null || cardCvv.length() < 3) {
            showToast("CVV invalide (3 chiffres requis)", "error");
            return;
        }
        if (cardHolder == null || cardHolder.isEmpty()) {
            showToast("Entrez le nom du titulaire", "error");
            return;
        }

        progressPaiement.setVisible(true);
        lblStatutPaiement.setText("Traitement en cours...");

        new Thread(() -> {
            try {
                Thread.sleep(1500);
                String paymentId = stripeService.creerPaiement(montantVal, "EUR", description);

                Platform.runLater(() -> {
                    progressPaiement.setVisible(false);
                    lblStatutPaiement.setText("✅ Paiement réussi ! ID: " + paymentId);
                    showToast("Paiement de " + montant + " TND accepté", "success");

                    Commande selectedCmd = cbPaiementCommande.getValue();
                    if (selectedCmd != null) {
                        commandeService.mettreAJourStatut(selectedCmd.getId(), "PAYEE");
                    } else {
                        Commande commande = new Commande();
                        commande.setClientNom(userName);
                        commande.setClientEmail(userEmail);
                        commande.setDateCommande(new Date());
                        commande.setTotal(montantVal);
                        commande.setStatutLivraison("PAYEE");
                        commandeService.sauvegarderCommande(commande);
                    }
                    chargerCommandes();

                    tfMontantPaiement.clear();
                    tfDescPaiement.clear();
                    tfCardNumber.clear();
                    tfCardExpiry.clear();
                    tfCardCvv.clear();
                    tfCardHolder.clear();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressPaiement.setVisible(false);
                    lblStatutPaiement.setText("❌ Erreur: " + e.getMessage());
                });
            }
        }).start();
    }

    // ==================== PAIEMENT COMBOBOX ====================
    private void setupPaiementComboBox() {
        cbPaiementCommande.setItems(commandesList);
        cbPaiementCommande.setCellFactory(lv -> new ListCell<Commande>() {
            @Override
            protected void updateItem(Commande item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null :
                        "Commande #" + item.getId() + " - " + String.format("%.2f TND", item.getTotal()) + " - " + item.getStatutLivraison());
            }
        });
        cbPaiementCommande.setOnAction(e -> {
            Commande c = cbPaiementCommande.getValue();
            if (c != null) {
                tfMontantPaiement.setText(String.format(java.util.Locale.US, "%.2f", c.getTotal()));
                tfDescPaiement.setText("Paiement commande #" + c.getId() + " - " + c.getClientNom());
            }
        });
    }

    // ==================== FACTURE PDF ====================
    private ObservableList<Commande> getFacturesPayees() {
        return FXCollections.observableArrayList(
                commandesList.stream().filter(c -> "PAYEE".equals(c.getStatutLivraison())).toList()
        );
    }

    private void setupFactureComboBox() {
        cbFactureCommande.setCellFactory(lv -> new ListCell<Commande>() {
            @Override
            protected void updateItem(Commande item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null :
                        "Commande #" + item.getId() + " - " + String.format("%.2f TND", item.getTotal()) + " - " + item.getStatutLivraison());
            }
        });
    }

    private void chargerFactureComboBox() {
        cbFactureCommande.setItems(getFacturesPayees());
    }

    @FXML public void genererFacture() {
        Commande c = cbFactureCommande.getValue();
        if (c != null) {
            String path = System.getProperty("user.home") + "/Desktop/facture_" + c.getId() + ".pdf";
            pdfService.genererFacture(c, path);
            lblStatutFacture.setText("✅ Facture générée: " + path);
            showToast("Facture PDF générée sur le Bureau", "success");
        } else {
            showToast("Sélectionnez une commande", "warning");
        }
    }

    // ==================== EMAIL ====================
    @FXML public void envoyerEmail() {
        String dest = tfEmailDest.getText();
        String sujet = tfEmailSujet.getText();
        String contenu = taEmailContenu.getText();

        if (dest.isEmpty()) {
            showToast("Entrez un destinataire", "error");
            return;
        }

        progressEmail.setVisible(true);
        lblStatutEmail.setText("Envoi en cours...");

        new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            boolean success = mailService.envoyerEmail(dest, sujet.isEmpty() ? "Message de Cartix" : sujet, contenu);

            Platform.runLater(() -> {
                progressEmail.setVisible(false);
                if (success) {
                    lblStatutEmail.setText("✅ Email envoyé à " + dest);
                    showToast("Email envoyé à " + tfEmailDest.getText(), "success");
                    tfEmailDest.clear();
                    tfEmailSujet.clear();
                    taEmailContenu.clear();
                } else {
                    lblStatutEmail.setText("❌ Échec de l'envoi");
                }
            });
        }).start();
    }

    // ==================== ÉTOILES ====================
    private String etoiles(double note) {
        int full = (int) Math.round(note);
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 5; i++)
            s.append(i < full ? "\u2605" : "\u2606");
        return s.toString();
    }

    // ==================== BADGES ====================
    private Label creerBadgeStatut(String statut) {
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
                break;
        }
        return badge;
    }

    // ==================== TOAST ====================
    private void showToast(String message, String type) {
        if (toastContainer == null) return;
        // Log to history
        notifHistory.add(new NotifEntry(message, type));
        notifUnread++;
        if (lblNotifBadge != null) {
            lblNotifBadge.setText(String.valueOf(notifUnread));
            lblNotifBadge.setVisible(true);
        }
        String bgColor, icon;
        switch (type) {
            case "success": bgColor = "#22d98a"; icon = "✅"; break;
            case "error": bgColor = "#ff4d6d"; icon = "❌"; break;
            case "warning": bgColor = "#ffa500"; icon = "⚠️"; break;
            default: bgColor = "#4d78ff"; icon = "ℹ️"; break;
        }
        HBox toast = new HBox(10);
        toast.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        toast.setStyle("-fx-background-color: #1a2a4a; -fx-background-radius: 10; -fx-padding: 12 16; -fx-border-color: " + bgColor + "; -fx-border-radius: 10; -fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 4);");
        toast.setMaxWidth(380);
        toast.setTranslateX(400);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18;");
        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-text-fill: #e8eaf6; -fx-font-size: 13; -fx-font-weight: bold;");
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(280);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7394; -fx-font-size: 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 4;");
        closeBtn.setOnAction(e -> {
            javafx.animation.Timeline fadeOut = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(200),
                            new javafx.animation.KeyValue(toast.translateXProperty(), 400))
            );
            fadeOut.setOnFinished(e2 -> toastContainer.getChildren().remove(toast));
            fadeOut.play();
        });

        toast.getChildren().addAll(iconLabel, msgLabel, closeBtn);
        toastContainer.getChildren().add(toast);

        javafx.animation.Timeline slideIn = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(300),
                        new javafx.animation.KeyValue(toast.translateXProperty(), 0,
                                javafx.animation.Interpolator.EASE_OUT))
        );
        slideIn.play();

        javafx.animation.PauseTransition timer = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(4));
        timer.setOnFinished(e -> {
            javafx.animation.Timeline fadeOut = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(300),
                            new javafx.animation.KeyValue(toast.translateXProperty(), 400))
            );
            fadeOut.setOnFinished(e2 -> toastContainer.getChildren().remove(toast));
            fadeOut.play();
        });
        timer.play();
    }

}