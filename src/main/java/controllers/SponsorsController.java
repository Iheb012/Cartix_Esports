package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import models.Evenement;
import models.Sponsor;
import services.ClearbitService;
import services.EvenementService;
import services.ExchangeRateService;
import services.SponsorService;

import javax.mail.*;
import javax.mail.internet.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class SponsorsController {

    @FXML private VBox itemsList;
    @FXML private VBox formPanel;
    @FXML private VBox sponsorFields;
    @FXML private VBox evenementFields;
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;
    @FXML private Label formFeedback;
    @FXML private Label statSponsors;
    @FXML private Label statBudget;
    @FXML private Label statEvenements;
    @FXML private Label statMoyenne;
    @FXML private HBox statsRow;
    @FXML private HBox tableHeader;
    @FXML private Button tabSponsors;
    @FXML private Button tabEvenements;
    @FXML private Button saveBtn;

    @FXML private TextField fieldSponsorNom;
    @FXML private TextField fieldLogoUrl;
    @FXML private ComboBox<String> fieldSecteur;
    @FXML private TextField fieldBudget;
    @FXML private TextField fieldContact;
    @FXML private VBox emailPreviewCard;
    @FXML private Label emailPreviewSubject;
    @FXML private Label emailPreviewSnippet;

    @FXML private ComboBox<String> fieldEvType;
    @FXML private TextField fieldEvNom;
    @FXML private TextField fieldEvDebut;
    @FXML private TextField fieldEvFin;
    @FXML private TextField fieldEvLieu;
    @FXML private TextField fieldEvBudget;
    @FXML private ComboBox<String> fieldEvSponsor;
    @FXML private TextField fieldOrgNom;
    @FXML private TextField fieldOrgEmail;
    @FXML private TextArea fieldEvDesc;

    private final SponsorService sponsorService = new SponsorService();
    private final EvenementService evenementService = new EvenementService();
    private final ClearbitService clearbit = new ClearbitService();
    private final ExchangeRateService exchangeRate = new ExchangeRateService();

    private List<Sponsor> allSponsors = new ArrayList<>();
    private List<Evenement> allEvents = new ArrayList<>();
    private Sponsor editingSponsor = null;
    private boolean showingSponsors = true;

    private TextField searchField;
    private ComboBox<String> filterSecteur;
    private ComboBox<String> filterBudget;
    private ComboBox<String> currencyCombo;
    private Label convertedLabel;
    private TextField fieldSecteurCustom;

    private static final String EMAIL_FROM = "votre_email@gmail.com";
    private static final String EMAIL_PASSWORD = "votre_mot_de_passe_app";

    @FXML
    public void initialize() {
        setupSectorComboBox();
        setupEventTypeComboBox();
        setupEmailPreviewListeners();
        injectSearchBar();
        injectCurrencyConverter();
        setupAutoFillOnNameBlur();
        setupHoverEffects();
        loadAllData();
        showSponsors();
    }

    private void setupSectorComboBox() {
        fieldSecteur.setItems(FXCollections.observableArrayList(
                "Tech & Gaming", "Hardware", "Energy Drinks", "Apparel & Merch",
                "Streaming & Media", "Telecom", "Finance", "Automotive",
                "Food & Beverage", "Other"
        ));

        fieldSecteurCustom = new TextField();
        fieldSecteurCustom.setPromptText("Précisez le secteur...");
        fieldSecteurCustom.setPrefHeight(32);
        fieldSecteurCustom.setStyle(
                "-fx-background-color: rgba(8,4,18,0.95); -fx-text-fill: white;" +
                        "-fx-background-radius: 12; -fx-border-color: rgba(190,242,100,0.45);" +
                        "-fx-border-radius: 12; -fx-padding: 0 12;");
        fieldSecteurCustom.setVisible(false);
        fieldSecteurCustom.setManaged(false);

        int index = sponsorFields.getChildren().indexOf(fieldSecteur) + 1;
        sponsorFields.getChildren().add(index, fieldSecteurCustom);

        fieldSecteur.setOnAction(e -> {
            boolean isOther = "Other".equals(fieldSecteur.getValue());
            fieldSecteurCustom.setVisible(isOther);
            fieldSecteurCustom.setManaged(isOther);
        });
    }

    private void setupEventTypeComboBox() {
        fieldEvType.setItems(FXCollections.observableArrayList(
                "LAN Tournament", "Online Event", "Press Conference",
                "Product Launch", "Community Meetup", "Charity Event"
        ));
        fieldEvType.setValue("LAN Tournament");
    }

    private void setupAutoFillOnNameBlur() {
        fieldSponsorNom.focusedProperty().addListener((obs, was, isNow) -> {
            if (!isNow && !fieldSponsorNom.getText().trim().isEmpty()) {
                autoFillCompanyInfo(fieldSponsorNom.getText().trim());
            }
        });
    }

    private void setupHoverEffects() {
        if (statsRow != null) {
            for (Node n : statsRow.getChildren()) {
                if (n instanceof VBox) attachHoverScale(n, 1.045);
            }
        }
        attachHoverScale(formPanel, 1.018);
    }

    private void attachHoverScale(Node node, double hoverScale) {
        ScaleTransition grow = new ScaleTransition(Duration.millis(170), node);
        grow.setToX(hoverScale);
        grow.setToY(hoverScale);
        ScaleTransition shrink = new ScaleTransition(Duration.millis(170), node);
        shrink.setToX(1.0);
        shrink.setToY(1.0);
        node.setOnMouseEntered(e -> grow.playFromStart());
        node.setOnMouseExited(e -> shrink.playFromStart());
    }

    private void attachHeroHover(HBox card, String baseStyle, String hoverStyle) {
        ScaleTransition grow = new ScaleTransition(Duration.millis(200), card);
        grow.setToX(1.025);
        grow.setToY(1.025);
        ScaleTransition shrink = new ScaleTransition(Duration.millis(200), card);
        shrink.setToX(1.0);
        shrink.setToY(1.0);
        card.setOnMouseEntered(e -> {
            card.setStyle(hoverStyle);
            grow.playFromStart();
        });
        card.setOnMouseExited(e -> {
            card.setStyle(baseStyle);
            shrink.playFromStart();
        });
    }

    private void animateCardAppear(Node node, int index) {
        node.setOpacity(0);
        node.setTranslateY(22);
        FadeTransition fade = new FadeTransition(Duration.millis(340), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(340), node);
        slide.setFromY(22);
        slide.setToY(0);
        ParallelTransition parallel = new ParallelTransition(fade, slide);
        parallel.setDelay(Duration.millis(40L * index));
        parallel.play();
    }

    private void setupEmailPreviewListeners() {
        Runnable r = this::updateEmailPreviewWidget;
        if (fieldSponsorNom != null)
            fieldSponsorNom.textProperty().addListener((o, a, b) -> { if (showingSponsors) Platform.runLater(r); });
        if (fieldContact != null)
            fieldContact.textProperty().addListener((o, a, b) -> { if (showingSponsors) Platform.runLater(r); });
    }

    private void updateEmailPreviewWidget() {
        if (emailPreviewCard == null) return;
        String nom = fieldSponsorNom != null ? fieldSponsorNom.getText().trim() : "";
        String em = fieldContact != null ? fieldContact.getText().trim() : "";
        boolean valid = showingSponsors && em.contains("@") && nom.length() >= 2;
        emailPreviewCard.setVisible(valid);
        emailPreviewCard.setManaged(valid);
        if (!valid) return;
        emailPreviewSubject.setText("Cartix Esports — Demande de partenariat reçue · " + nom);
        emailPreviewSnippet.setText("Bonjour " + nom + ",\n\nVotre demande sera examinée par un administrateur.\n\n— Cartix Esports");
    }

    private void injectSearchBar() {
        HBox searchRow = new HBox(8);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchRow.setStyle("-fx-padding: 12 14; -fx-background-color: rgba(18,10,32,0.72);" +
                "-fx-background-radius: 18; -fx-border-color: rgba(167,139,250,0.22);" +
                "-fx-border-radius: 18; -fx-border-width: 1;");

        searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher sponsor...");
        searchField.setPrefHeight(32);
        searchField.setStyle("-fx-background-color: rgba(8,4,18,0.9); -fx-text-fill: white;" +
                "-fx-background-radius: 12; -fx-padding: 0 12;");
        searchField.textProperty().addListener((obs, o, n) -> applyFiltersAndRender());

        filterSecteur = new ComboBox<>();
        filterSecteur.setItems(FXCollections.observableArrayList(
                "Tous les secteurs", "Tech & Gaming", "Hardware", "Energy Drinks",
                "Apparel & Merch", "Streaming & Media", "Telecom", "Finance", "Other"));
        filterSecteur.setValue("Tous les secteurs");
        filterSecteur.setOnAction(e -> applyFiltersAndRender());

        filterBudget = new ComboBox<>();
        filterBudget.setItems(FXCollections.observableArrayList(
                "Tous budgets", "< 1000 dt", "1000-5000 dt", "5000-20000 dt", "> 20000 dt"));
        filterBudget.setValue("Tous budgets");
        filterBudget.setOnAction(e -> applyFiltersAndRender());

        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(e -> {
            searchField.clear();
            filterSecteur.setValue("Tous les secteurs");
            filterBudget.setValue("Tous budgets");
            applyFiltersAndRender();
        });

        searchRow.getChildren().addAll(searchField, filterSecteur, filterBudget, resetBtn);
        VBox tableParent = (VBox) tableHeader.getParent();
        tableParent.getChildren().add(tableParent.getChildren().indexOf(tableHeader), searchRow);
    }

    private void applyFiltersAndRender() {
        if (allSponsors == null) return;
        String query = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String sector = filterSecteur != null ? filterSecteur.getValue() : "Tous les secteurs";
        String budgetFilter = filterBudget != null ? filterBudget.getValue() : "Tous budgets";

        List<Sponsor> filtered = allSponsors.stream().filter(s -> {
            boolean matchSearch = query.isEmpty() || s.getNom().toLowerCase().contains(query);
            boolean matchSector = "Tous les secteurs".equals(sector) || sector.equals(s.getSecteur());
            float b = s.getBudgetAllouee();
            boolean matchBudget = switch (budgetFilter) {
                case "< 1000 dt" -> b < 1000;
                case "1000-5000 dt" -> b >= 1000 && b <= 5000;
                case "5000-20000 dt" -> b > 5000 && b <= 20000;
                case "> 20000 dt" -> b > 20000;
                default -> true;
            };
            return matchSearch && matchSector && matchBudget;
        }).collect(Collectors.toList());
        renderSponsors(filtered);
    }

    private void loadAllData() {
        try {
            allSponsors = sponsorService.getAll();
        } catch (Exception e) {
            allSponsors = new ArrayList<>();
            System.out.println("Erreur chargement sponsors: " + e.getMessage());
        }
        try {
            allEvents = evenementService.getAll();
        } catch (Exception e) {
            allEvents = new ArrayList<>();
            System.out.println("Erreur chargement events: " + e.getMessage());
        }
        refreshEvSponsorChoices();
        updateStats();
    }

    private void refreshEvSponsorChoices() {
        if (fieldEvSponsor == null) return;
        fieldEvSponsor.getItems().clear();
        fieldEvSponsor.getItems().add("0|— Aucun —");
        for (Sponsor s : allSponsors) {
            fieldEvSponsor.getItems().add(s.getId() + "|" + s.getNom());
        }
        fieldEvSponsor.setValue("0|— Aucun —");
    }

    private int parseEvSponsorPick() {
        if (fieldEvSponsor == null || fieldEvSponsor.getValue() == null) return 0;
        String v = fieldEvSponsor.getValue();
        int pipe = v.indexOf('|');
        if (pipe <= 0) return 0;
        try {
            return Integer.parseInt(v.substring(0, pipe).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateStats() {
        long count = allSponsors.size();
        double totalBudget = allSponsors.stream().mapToDouble(Sponsor::getBudgetAllouee).sum();
        statSponsors.setText(String.valueOf(count));
        statBudget.setText(String.format("%.0f dt", totalBudget));
        statEvenements.setText(String.valueOf(allEvents.size()));
        statMoyenne.setText(count > 0 ? String.format("%.0f dt", totalBudget / count) : "0 dt");
    }

    private void renderSponsors(List<Sponsor> list) {
        itemsList.getChildren().clear();
        if (list.isEmpty()) {
            Label empty = new Label("Aucun sponsor trouvé");
            empty.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 13px; -fx-padding: 28;");
            itemsList.getChildren().add(empty);
            return;
        }
        int i = 0;
        for (Sponsor s : list) {
            HBox card = buildSponsorCard(s);
            itemsList.getChildren().add(card);
            animateCardAppear(card, i++);
        }
    }

    private HBox buildSponsorCard(Sponsor s) {
        HBox card = new HBox(0);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(100);
        card.setPadding(new Insets(0));
        card.setStyle("-fx-background-color: linear-gradient(to right, rgba(62,28,115,0.95), rgba(12,5,24,0.98));" +
                "-fx-background-radius: 16; -fx-border-color: rgba(167,139,250,0.3); -fx-border-radius: 16;");

        VBox left = new VBox(8);
        left.setPadding(new Insets(16, 22, 16, 22));
        HBox.setHgrow(left, Priority.ALWAYS);

        Label title = new Label(s.getNom());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label info = new Label(s.getSecteur() + " · Budget: " + (int)s.getBudgetAllouee() + " dt");
        info.setStyle("-fx-text-fill: rgba(230,220,255,0.78); -fx-font-size: 11px;");

        HBox btnRow = new HBox(10);
        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 6 16;");
        editBtn.setOnAction(e -> loadSponsorIntoForm(s));
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 6 16;");
        deleteBtn.setOnAction(e -> deleteSponsor(s));
        btnRow.getChildren().addAll(editBtn, deleteBtn);

        left.getChildren().addAll(title, info, btnRow);
        card.getChildren().add(left);
        return card;
    }

    private void renderEvenements() {
        itemsList.getChildren().clear();
        try {
            allEvents = evenementService.getAll();
        } catch (Exception e) {
            allEvents = new ArrayList<>();
        }
        updateStats();

        if (allEvents.isEmpty()) {
            Label empty = new Label("Aucun événement disponible");
            empty.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 13px; -fx-padding: 28;");
            itemsList.getChildren().add(empty);
            return;
        }

        int i = 0;
        for (Evenement ev : allEvents) {
            HBox card = buildEventCard(ev);
            itemsList.getChildren().add(card);
            animateCardAppear(card, i++);
        }
    }

    private HBox buildEventCard(Evenement e) {
        HBox card = new HBox(0);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(100);
        card.setPadding(new Insets(0));
        card.setStyle("-fx-background-color: linear-gradient(to right, rgba(52,22,98,0.96), rgba(8,4,20,0.99));" +
                "-fx-background-radius: 16; -fx-border-color: rgba(167,139,250,0.32); -fx-border-radius: 16;");

        VBox left = new VBox(8);
        left.setPadding(new Insets(16, 22, 16, 22));
        HBox.setHgrow(left, Priority.ALWAYS);

        Label title = new Label(e.getNom());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        String typeLabel = EvenementService.typeLabelFromCode(e.getType());
        Label info = new Label(typeLabel + " · " + e.getLieu() + " · Budget: " + (int)e.getBudgetTotal() + " dt");
        info.setStyle("-fx-text-fill: rgba(230,220,255,0.78); -fx-font-size: 11px;");

        left.getChildren().addAll(title, info);
        card.getChildren().add(left);
        return card;
    }

    @FXML
    public void showSponsors() {
        showingSponsors = true;
        tabSponsors.setStyle("-fx-background-color: linear-gradient(to right, #6d28d9, #a855f7); -fx-text-fill: white; -fx-background-radius: 14;");
        tabEvenements.setStyle("-fx-background-color: transparent; -fx-text-fill: #9b8bb8;");
        sponsorFields.setVisible(true);
        sponsorFields.setManaged(true);
        evenementFields.setVisible(false);
        evenementFields.setManaged(false);
        formTitle.setText("Nouveau sponsor");
        saveBtn.setText("Ajouter");
        updateTableHeader(true);
        applyFiltersAndRender();
    }

    @FXML
    public void showEvenements() {
        showingSponsors = false;
        refreshEvSponsorChoices();
        tabEvenements.setStyle("-fx-background-color: linear-gradient(to right, #6d28d9, #a855f7); -fx-text-fill: white; -fx-background-radius: 14;");
        tabSponsors.setStyle("-fx-background-color: transparent; -fx-text-fill: #9b8bb8;");
        sponsorFields.setVisible(false);
        sponsorFields.setManaged(false);
        evenementFields.setVisible(true);
        evenementFields.setManaged(true);
        formTitle.setText("Nouvel événement");
        saveBtn.setText("Ajouter");
        updateTableHeader(false);
        renderEvenements();
    }

    private void updateTableHeader(boolean isSponsor) {
        tableHeader.getChildren().clear();
        Label lbl = new Label(isSponsor ? "Sponsors" : "Événements");
        lbl.setStyle("-fx-text-fill: #c4b5fd; -fx-font-size: 12px; -fx-font-weight: bold;");
        tableHeader.getChildren().add(lbl);
    }

    @FXML
    public void onSave() {
        if (showingSponsors) saveSponsor();
        else saveEvenement();
    }

    private void saveSponsor() {
        String nom = fieldSponsorNom.getText().trim();
        String secteur = fieldSecteur.getValue();
        String budgetStr = fieldBudget.getText().trim();

        if ("Other".equals(secteur) && fieldSecteurCustom != null && !fieldSecteurCustom.getText().trim().isEmpty()) {
            secteur = fieldSecteurCustom.getText().trim();
        }

        if (nom.isEmpty()) {
            setFeedback("Le nom est obligatoire", "#F23333");
            return;
        }
        if (secteur == null) {
            setFeedback("Choisissez un secteur", "#F23333");
            return;
        }
        float budget;
        try {
            budget = Float.parseFloat(budgetStr);
        } catch (NumberFormatException e) {
            setFeedback("Budget invalide", "#F23333");
            return;
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp later = Timestamp.valueOf(LocalDateTime.now().plusYears(1));

        try {
            if (editingSponsor == null) {
                Sponsor sp = new Sponsor(nom, secteur, budget, now, later);
                sponsorService.add(sp);
                setFeedback("Sponsor ajouté avec succès", "#1DB876");
                sendConfirmationEmail(fieldContact.getText().trim(), nom, true);
            } else {
                editingSponsor.setNom(nom);
                editingSponsor.setSecteur(secteur);
                editingSponsor.setBudgetAllouee(budget);
                sponsorService.update(editingSponsor);
                setFeedback("Sponsor mis à jour", "#1DB876");
                editingSponsor = null;
            }
            onClearForm();
            loadAllData();
            applyFiltersAndRender();
        } catch (Exception e) {
            setFeedback("Erreur: " + e.getMessage(), "#F23333");
        }
    }

    private void saveEvenement() {
        String nom = fieldEvNom.getText().trim();
        String typeLabel = fieldEvType.getValue();
        String debutStr = fieldEvDebut.getText().trim();
        String finStr = fieldEvFin.getText().trim();
        String lieu = fieldEvLieu.getText().trim();
        String budgetStr = fieldEvBudget.getText().trim();

        if (nom.isEmpty()) {
            setFeedback("Le nom est obligatoire", "#F23333");
            return;
        }
        if (debutStr.isEmpty() || finStr.isEmpty()) {
            setFeedback("Dates obligatoires (AAAA-MM-JJ)", "#F23333");
            return;
        }

        Date dDebut, dFin;
        try {
            dDebut = Date.valueOf(LocalDate.parse(debutStr));
            dFin = Date.valueOf(LocalDate.parse(finStr));
        } catch (DateTimeParseException e) {
            setFeedback("Format de date invalide", "#F23333");
            return;
        }

        float budget;
        try {
            budget = Float.parseFloat(budgetStr);
        } catch (NumberFormatException e) {
            setFeedback("Budget invalide", "#F23333");
            return;
        }

        int typeCode = EvenementService.typeCodeFromLabel(typeLabel);
        int sponsorPick = parseEvSponsorPick();

        Evenement ev = new Evenement(nom, typeCode, dDebut, dFin, lieu, budget, sponsorPick);

        try {
            evenementService.add(ev);
            setFeedback("Événement ajouté avec succès", "#1DB876");
            onClearForm();
            loadAllData();
            renderEvenements();
        } catch (Exception e) {
            setFeedback("Erreur: " + e.getMessage(), "#F23333");
        }
    }

    private void sendConfirmationEmail(String toEmail, String sponsorName, boolean pendingRequest) {
        if (toEmail == null || !toEmail.contains("@")) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                try {
                    Properties props = new Properties();
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.port", "587");

                    Session session = Session.getInstance(props, new Authenticator() {
                        @Override protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                        }
                    });

                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(EMAIL_FROM));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                    message.setSubject("Cartix Esports - Demande reçue");

                    MimeBodyPart textPart = new MimeBodyPart();
                    textPart.setText("Bonjour " + sponsorName + ",\n\nVotre demande a été reçue.\n\n— Cartix Esports");

                    MimeBodyPart htmlPart = new MimeBodyPart();
                    htmlPart.setContent("<html><body><h2>Bonjour " + sponsorName + "</h2><p>Votre demande a été reçue.</p></body></html>", "text/html");

                    MimeMultipart multipart = new MimeMultipart("alternative");
                    multipart.addBodyPart(textPart);
                    multipart.addBodyPart(htmlPart);
                    message.setContent(multipart);

                    Transport.send(message);
                } catch (Exception e) {
                    System.out.println("Email failed: " + e.getMessage());
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    private void injectCurrencyConverter() {
        Label converterLabel = new Label("Convertisseur de devises");
        converterLabel.setStyle("-fx-text-fill: #949499; -fx-font-size: 10px;");

        HBox converterRow = new HBox(8);
        currencyCombo = new ComboBox<>();
        currencyCombo.setItems(FXCollections.observableArrayList("EUR", "USD", "GBP", "TND"));
        currencyCombo.setValue("EUR");

        convertedLabel = new Label("—");
        convertedLabel.setStyle("-fx-text-fill: #bef264; -fx-font-size: 11px;");

        Button convertBtn = new Button("Convertir → TND");
        convertBtn.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(fieldBudget.getText());
                double result = exchangeRate.convert(amount, currencyCombo.getValue(), "TND");
                convertedLabel.setText(String.format("%.2f %s = %.2f TND", amount, currencyCombo.getValue(), result));
            } catch (NumberFormatException ex) {
                convertedLabel.setText("Budget invalide");
            }
        });

        converterRow.getChildren().addAll(currencyCombo, convertBtn);
        sponsorFields.getChildren().addAll(converterLabel, converterRow, convertedLabel);
    }

    private void autoFillCompanyInfo(String name) {
        Task<Optional<ClearbitService.CompanyInfo>> task = new Task<>() {
            @Override protected Optional<ClearbitService.CompanyInfo> call() {
                return clearbit.searchCompany(name);
            }
        };
        task.setOnSucceeded(e -> task.getValue().ifPresent(c -> Platform.runLater(() -> {
            if (fieldLogoUrl.getText().trim().isEmpty())
                fieldLogoUrl.setText(c.logoUrl);
        })));
        new Thread(task).start();
    }

    private void deleteSponsor(Sponsor s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Supprimer " + s.getNom() + " ?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    sponsorService.delete(s.getId());
                    loadAllData();
                    applyFiltersAndRender();
                } catch (Exception e) {
                    setFeedback("Erreur: " + e.getMessage(), "#F23333");
                }
            }
        });
    }

    private void loadSponsorIntoForm(Sponsor s) {
        editingSponsor = s;
        fieldSponsorNom.setText(s.getNom());
        fieldSecteur.setValue(s.getSecteur());
        fieldBudget.setText(String.valueOf((int) s.getBudgetAllouee()));
        formTitle.setText("Modifier: " + s.getNom());
        saveBtn.setText("Mettre à jour");
        if (!showingSponsors) showSponsors();
    }

    @FXML
    public void onClearForm() {
        editingSponsor = null;
        fieldSponsorNom.clear();
        fieldLogoUrl.clear();
        fieldSecteur.setValue(null);
        fieldBudget.clear();
        fieldContact.clear();
        if (fieldSecteurCustom != null) {
            fieldSecteurCustom.clear();
            fieldSecteurCustom.setVisible(false);
            fieldSecteurCustom.setManaged(false);
        }
        if (fieldEvNom != null) fieldEvNom.clear();
        if (fieldEvDebut != null) fieldEvDebut.clear();
        if (fieldEvFin != null) fieldEvFin.clear();
        if (fieldEvLieu != null) fieldEvLieu.clear();
        if (fieldEvBudget != null) fieldEvBudget.clear();
        if (fieldOrgNom != null) fieldOrgNom.clear();
        if (fieldOrgEmail != null) fieldOrgEmail.clear();
        if (fieldEvDesc != null) fieldEvDesc.clear();
        if (fieldEvSponsor != null) fieldEvSponsor.setValue("0|— Aucun —");
        if (convertedLabel != null) convertedLabel.setText("—");
        formFeedback.setText("");
        formTitle.setText(showingSponsors ? "Nouveau sponsor" : "Nouvel événement");
        saveBtn.setText(showingSponsors ? "Ajouter" : "Ajouter");
    }

    private void setFeedback(String msg, String color) {
        Platform.runLater(() -> {
            formFeedback.setText(msg);
            formFeedback.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px;");
        });
    }

    @FXML public void navDashboard() {}
    @FXML public void navPlayers() {}
    @FXML public void navMatches() {}
    @FXML public void navTeams() {}
    @FXML public void navTournaments() {}
    @FXML public void navAnalytics() {}
    @FXML public void navProduits() {}
    @FXML public void navSponsors() {}
}