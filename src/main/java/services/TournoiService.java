package services;

import models.Joueur;
import models.Tournoi;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournoiService implements IService<Tournoi> {

    private Connection connection;

    public TournoiService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Tournoi t) throws Exception {
        String query = "INSERT INTO tournoi (nom, jeu, dateDebut, dateFin, maxEquipe, statut, organisateurId, mot_de_passe) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, t.getNom());
        ps.setString(2, t.getJeu());
        ps.setDate(3, t.getDateDebut());
        ps.setDate(4, t.getDateFin());
        ps.setInt(5, t.getMaxEquipe());
        ps.setString(6, t.getStatut());
        ps.setInt(7, t.getOrganisateurId());
        ps.setString(8, t.getMotDePasse());
        ps.executeUpdate();
        System.out.println("✅ Tournoi ajouté !");
    }

    @Override
    public void update(Tournoi t) throws Exception {
        String query = "UPDATE tournoi SET nom=?, jeu=?, dateDebut=?, dateFin=?, maxEquipe=?, statut=?, organisateurId=?, mot_de_passe=? WHERE Id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, t.getNom());
        ps.setString(2, t.getJeu());
        ps.setDate(3, t.getDateDebut());
        ps.setDate(4, t.getDateFin());
        ps.setInt(5, t.getMaxEquipe());
        ps.setString(6, t.getStatut());
        ps.setInt(7, t.getOrganisateurId());
        ps.setString(8, t.getMotDePasse());
        ps.setInt(9, t.getId());
        ps.executeUpdate();
        System.out.println("✅ Tournoi modifié !");
    }

    @Override
    public void delete(int id) throws Exception {
        String query = "DELETE FROM tournoi WHERE Id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("✅ Tournoi supprimé !");
    }

    @Override
    public Tournoi getById(int id) throws Exception {
        String query = "SELECT * FROM tournoi WHERE Id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapResultSet(rs);
        }
        return null;
    }

    @Override
    public List<Tournoi> getAll() throws Exception {
        List<Tournoi> list = new ArrayList<>();
        String query = "SELECT * FROM tournoi";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    public void incrementInscrits(int tournoiId) throws Exception {
        String query = "UPDATE tournoi SET nbInscrits = nbInscrits + 1 WHERE Id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, tournoiId);
        ps.executeUpdate();
    }

    public void decrementInscrits(int tournoiId) throws Exception {
        String query = "UPDATE tournoi SET nbInscrits = nbInscrits - 1 WHERE Id = ? AND nbInscrits > 0";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, tournoiId);
        ps.executeUpdate();
    }

    private Tournoi mapResultSet(ResultSet rs) throws SQLException {
        Tournoi t = new Tournoi(
                rs.getInt("Id"),
                rs.getString("nom"),
                rs.getString("jeu"),
                rs.getDate("dateDebut"),
                rs.getDate("dateFin"),
                rs.getInt("maxEquipe"),
                rs.getString("statut")
        );
        t.setOrganisateurId(rs.getInt("organisateurId"));
        t.setMotDePasse(rs.getString("mot_de_passe"));
        t.setNbInscrits(rs.getInt("nbInscrits"));
        return t;
    }
    // Ajouter un joueur au tournoi
    public void inscrireJoueur(int tournoiId, int joueurId) throws Exception {
        // Vérifier si déjà inscrit
        String checkQuery = "SELECT * FROM tournoi_inscription WHERE tournoiId = ? AND joueurId = ?";
        PreparedStatement checkPs = connection.prepareStatement(checkQuery);
        checkPs.setInt(1, tournoiId);
        checkPs.setInt(2, joueurId);
        ResultSet rs = checkPs.executeQuery();

        if (rs.next()) {
            throw new Exception("Vous êtes déjà inscrit à ce tournoi");
        }

        // Vérifier les places disponibles
        Tournoi t = getById(tournoiId);
        if (t.getNbInscrits() >= t.getMaxEquipe()) {
            throw new Exception("Tournoi complet");
        }

        // Ajouter l'inscription
        String query = "INSERT INTO tournoi_inscription (tournoiId, joueurId) VALUES (?, ?)";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, tournoiId);
        ps.setInt(2, joueurId);
        ps.executeUpdate();

        // Incrémenter le compteur
        incrementInscrits(tournoiId);
    }

    // Désinscrire un joueur
    public void desinscrireJoueur(int tournoiId, int joueurId) throws Exception {
        String query = "DELETE FROM tournoi_inscription WHERE tournoiId = ? AND joueurId = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, tournoiId);
        ps.setInt(2, joueurId);
        ps.executeUpdate();

        decrementInscrits(tournoiId);
    }

    // Récupérer la liste des joueurs inscrits à un tournoi
    public List<Joueur> getJoueursInscrits(int tournoiId) throws Exception {
        List<Joueur> joueurs = new ArrayList<>();
        String query = "SELECT j.* FROM tournoi_inscription ti " +
                "JOIN joueur j ON ti.joueurId = j.id " +
                "WHERE ti.tournoiId = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, tournoiId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Joueur j = new Joueur();
            j.setId(rs.getInt("id"));
            j.setPseudo(rs.getString("pseudo"));
            j.setNom(rs.getString("nom"));
            j.setPrenom(rs.getString("prenom"));
            j.setGame(rs.getString("game"));
            joueurs.add(j);
        }
        return joueurs;
    }

    // Vérifier si un joueur est inscrit
    public boolean isJoueurInscrit(int tournoiId, int joueurId) throws Exception {
        String query = "SELECT 1 FROM tournoi_inscription WHERE tournoiId = ? AND joueurId = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, tournoiId);
        ps.setInt(2, joueurId);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }
}