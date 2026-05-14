package services;

import models.Evenement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService {
    private static final String URL = "jdbc:mysql://localhost:3306/cartix_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Types d'événements
    public static final int TYPE_LAN = 1;
    public static final int TYPE_ONLINE = 2;
    public static final int TYPE_PRESS = 3;
    public static final int TYPE_PRODUCT = 4;
    public static final int TYPE_MEETUP = 5;
    public static final int TYPE_CHARITY = 6;

    public static String typeLabelFromCode(int code) {
        return switch (code) {
            case TYPE_LAN -> "LAN Tournament";
            case TYPE_ONLINE -> "Online Event";
            case TYPE_PRESS -> "Press Conference";
            case TYPE_PRODUCT -> "Product Launch";
            case TYPE_MEETUP -> "Community Meetup";
            case TYPE_CHARITY -> "Charity Event";
            default -> "Unknown";
        };
    }

    public static int typeCodeFromLabel(String label) {
        return switch (label) {
            case "LAN Tournament" -> TYPE_LAN;
            case "Online Event" -> TYPE_ONLINE;
            case "Press Conference" -> TYPE_PRESS;
            case "Product Launch" -> TYPE_PRODUCT;
            case "Community Meetup" -> TYPE_MEETUP;
            case "Charity Event" -> TYPE_CHARITY;
            default -> 0;
        };
    }

    // GET ALL - Récupérer tous les événements
    public List<Evenement> getAll() throws SQLException {
        List<Evenement> events = new ArrayList<>();
        String sql = "SELECT * FROM evenements ORDER BY id";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                events.add(mapResultSetToEvenement(rs));
            }
        }
        return events;
    }

    // GET APPROVED - Récupérer les événements approuvés triés par date
    public List<Evenement> getApprovedOrderedByStart() throws SQLException {
        List<Evenement> events = new ArrayList<>();
        String sql = "SELECT * FROM evenements WHERE statut = 'APPROVED' ORDER BY date_debut";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                events.add(mapResultSetToEvenement(rs));
            }
        }
        return events;
    }

    // GET PENDING - Récupérer les événements en attente
    public List<Evenement> getPending() throws SQLException {
        List<Evenement> events = new ArrayList<>();
        String sql = "SELECT * FROM evenements WHERE statut = 'PENDING' ORDER BY id";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                events.add(mapResultSetToEvenement(rs));
            }
        }
        return events;
    }

    // ADD - Ajouter un événement
    public void add(Evenement evenement) throws SQLException {
        String sql = "INSERT INTO evenements (nom, type, date_debut, date_fin, lieu, budget_total, sponsor_id, statut, organisateur_nom, organisateur_email, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, evenement.getNom());
            stmt.setInt(2, evenement.getType());
            stmt.setDate(3, evenement.getDateDebut());
            stmt.setDate(4, evenement.getDateFin());
            stmt.setString(5, evenement.getLieu());
            stmt.setFloat(6, evenement.getBudgetTotal());
            if (evenement.getSponsorId() > 0) {
                stmt.setInt(7, evenement.getSponsorId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.setString(8, evenement.getStatut() != null ? evenement.getStatut() : "PENDING");
            stmt.setString(9, evenement.getOrganisateurNom());
            stmt.setString(10, evenement.getOrganisateurEmail());
            stmt.setString(11, evenement.getDescription());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    evenement.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // INSERT PENDING - Ajouter un événement en attente (pour l'interface utilisateur)
    public void insertPending(Evenement evenement) throws SQLException {
        add(evenement);
    }

    // UPDATE - Mettre à jour un événement
    public void update(Evenement evenement) throws SQLException {
        String sql = "UPDATE evenements SET nom=?, type=?, date_debut=?, date_fin=?, lieu=?, budget_total=?, sponsor_id=?, statut=?, organisateur_nom=?, organisateur_email=?, description=? WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, evenement.getNom());
            stmt.setInt(2, evenement.getType());
            stmt.setDate(3, evenement.getDateDebut());
            stmt.setDate(4, evenement.getDateFin());
            stmt.setString(5, evenement.getLieu());
            stmt.setFloat(6, evenement.getBudgetTotal());
            if (evenement.getSponsorId() > 0) {
                stmt.setInt(7, evenement.getSponsorId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.setString(8, evenement.getStatut());
            stmt.setString(9, evenement.getOrganisateurNom());
            stmt.setString(10, evenement.getOrganisateurEmail());
            stmt.setString(11, evenement.getDescription());
            stmt.setInt(12, evenement.getId());

            stmt.executeUpdate();
        }
    }

    // DELETE - Supprimer un événement
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM evenements WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Méthode utilitaire pour convertir un ResultSet en objet Evenement
    private Evenement mapResultSetToEvenement(ResultSet rs) throws SQLException {
        Evenement e = new Evenement();
        e.setId(rs.getInt("id"));
        e.setNom(rs.getString("nom"));
        e.setType(rs.getInt("type"));
        e.setDateDebut(rs.getDate("date_debut"));
        e.setDateFin(rs.getDate("date_fin"));
        e.setLieu(rs.getString("lieu"));
        e.setBudgetTotal(rs.getFloat("budget_total"));
        e.setSponsorId(rs.getInt("sponsor_id"));
        e.setStatut(rs.getString("statut"));
        e.setOrganisateurNom(rs.getString("organisateur_nom"));
        e.setOrganisateurEmail(rs.getString("organisateur_email"));
        e.setDescription(rs.getString("description"));
        return e;
    }
}