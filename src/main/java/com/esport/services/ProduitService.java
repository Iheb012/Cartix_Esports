package com.esport.services;

import com.esport.models.Produit;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService {

    public List<Produit> getAllProduits() {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM pro";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapProduit(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Produit getProduitById(int id) {
        String sql = "SELECT * FROM pro WHERE Id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapProduit(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean ajouterProduit(Produit p) {
        String sql = "INSERT INTO pro (nom, description, prix, stock, categorie, imageUrl) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrix());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getCategorie());
            ps.setString(6, p.getImageUrl());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) p.setId(keys.getInt(1));
                }
            }
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean modifierProduit(Produit p) {
        String sql = "UPDATE pro SET nom=?, description=?, prix=?, stock=?, categorie=?, imageUrl=? WHERE Id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrix());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getCategorie());
            ps.setString(6, p.getImageUrl());
            ps.setInt(7, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean diminuerStock(int id, int quantite) {
        String sql = "UPDATE pro SET stock = stock - ? WHERE Id = ? AND stock >= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantite);
            ps.setInt(2, id);
            ps.setInt(3, quantite);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimerProduit(int id) {
        String sql = "DELETE FROM pro WHERE Id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countProduits() {
        String sql = "SELECT COUNT(*) FROM pro";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countStockBas() {
        String sql = "SELECT COUNT(*) FROM pro WHERE stock <= 5";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getValeurStock() {
        String sql = "SELECT COALESCE(SUM(prix * stock), 0) FROM pro";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean ajouterNote(int produitId, int note) {
        String sql = "UPDATE pro SET note = ((note * nbAvis) + ?) / (nbAvis + 1), nbAvis = nbAvis + 1 WHERE Id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, note);
            ps.setInt(2, produitId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Produit mapProduit(ResultSet rs) throws SQLException {
        Produit p = new Produit();
        p.setId(rs.getInt("Id"));
        p.setNom(rs.getString("nom"));
        p.setDescription(rs.getString("description"));
        p.setPrix(rs.getDouble("prix"));
        p.setStock(rs.getInt("stock"));
        p.setCategorie(rs.getString("categorie"));
        String url = rs.getString("imageUrl");
        p.setImageUrl(url != null ? url : "");
        p.setNote(rs.getDouble("note"));
        p.setNbAvis(rs.getInt("nbAvis"));
        return p;
    }
}
