package models;

import java.sql.Date;

public class Tournoi {
    private int id;
    private String nom;
    private String jeu;
    private Date dateDebut;
    private Date dateFin;
    private int maxEquipe;
    private String statut;
    private int organisateurId;
    private String motDePasse;
    private int nbInscrits;

    public Tournoi() {}

    public Tournoi(String nom, String jeu, Date dateDebut, Date dateFin, int maxEquipe, String statut) {
        this.nom = nom;
        this.jeu = jeu;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.maxEquipe = maxEquipe;
        this.statut = statut;
    }

    public Tournoi(int id, String nom, String jeu, Date dateDebut, Date dateFin, int maxEquipe, String statut) {
        this.id = id;
        this.nom = nom;
        this.jeu = jeu;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.maxEquipe = maxEquipe;
        this.statut = statut;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getJeu() { return jeu; }
    public void setJeu(String jeu) { this.jeu = jeu; }
    public Date getDateDebut() { return dateDebut; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }
    public Date getDateFin() { return dateFin; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }
    public int getMaxEquipe() { return maxEquipe; }
    public void setMaxEquipe(int maxEquipe) { this.maxEquipe = maxEquipe; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public int getOrganisateurId() { return organisateurId; }
    public void setOrganisateurId(int organisateurId) { this.organisateurId = organisateurId; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public int getNbInscrits() { return nbInscrits; }
    public void setNbInscrits(int nbInscrits) { this.nbInscrits = nbInscrits; }

    @Override
    public String toString() {
        return "Tournoi{id=" + id + ", nom='" + nom + "', jeu='" + jeu +
                "', nbInscrits=" + nbInscrits + "/" + maxEquipe + ", statut='" + statut + "'}";
    }
}