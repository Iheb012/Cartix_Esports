package com.esport.services;

import com.esport.models.Commande;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeService {

    public boolean sauvegarderCommande(Commande commande) {
        String sql = "INSERT INTO commande (iduser, dateCommande, montantTotal, Statut, AdresseLivraison) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, 1);
            ps.setDate(2, new java.sql.Date(commande.getDateCommande().getTime()));
            ps.setDouble(3, commande.getTotal());
            ps.setString(4, commande.getStatutLivraison());
            ps.setString(5, commande.getClientNom() + " | " + commande.getClientEmail());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) commande.setId(keys.getInt(1));
                }
            }
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Commande> getAllCommandes() {
        List<Commande> list = new ArrayList<>();
        String sql = "SELECT c.*, u.nom, u.prenom, u.email FROM commande c LEFT JOIN user u ON c.iduser = u.Id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapCommande(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean mettreAJourStatut(int id, String statut) {
        String sql = "UPDATE commande SET Statut=? WHERE Id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countCommandes() {
        String sql = "SELECT COUNT(*) FROM commande";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getChiffreAffaires() {
        String sql = "SELECT COALESCE(SUM(montantTotal), 0) FROM commande";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countEnAttente() {
        String sql = "SELECT COUNT(*) FROM commande WHERE Statut='EN_ATTENTE'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Commande mapCommande(ResultSet rs) throws SQLException {
        Commande c = new Commande();
        c.setId(rs.getInt("Id"));
        String nom = rs.getString("nom");
        String prenom = rs.getString("prenom");
        String email = rs.getString("email");
        c.setClientNom(nom != null ? nom : "Joueur");
        c.setClientEmail(email != null ? email : "joueur@esport.com");
        c.setDateCommande(rs.getDate("dateCommande"));
        c.setTotal(rs.getDouble("montantTotal"));
        c.setStatutLivraison(rs.getString("Statut"));
        return c;
    }
}
