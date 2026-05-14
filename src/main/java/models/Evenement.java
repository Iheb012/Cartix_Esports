package models;

import java.sql.Date;

public class Evenement {
    private int id;
    private String nom;
    private int type;
    private Date dateDebut;
    private Date dateFin;
    private String lieu;
    private float budgetTotal;
    private int sponsorId;
    private String statut;           // PENDING, APPROVED, REJECTED
    private String organisateurNom;
    private String organisateurEmail;
    private String description;

    public Evenement() {}

    public Evenement(String nom, int type, Date dateDebut, Date dateFin, String lieu, float budgetTotal, int sponsorId) {
        this.nom = nom;
        this.type = type;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.budgetTotal = budgetTotal;
        this.sponsorId = sponsorId;
        this.statut = "PENDING";
    }

    public Evenement(int id, String nom, int type, Date dateDebut, Date dateFin, String lieu, float budgetTotal, int sponsorId) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.budgetTotal = budgetTotal;
        this.sponsorId = sponsorId;
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public int getType() { return type; }
    public Date getDateDebut() { return dateDebut; }
    public Date getDateFin() { return dateFin; }
    public String getLieu() { return lieu; }
    public float getBudgetTotal() { return budgetTotal; }
    public int getSponsorId() { return sponsorId; }
    public String getStatut() { return statut; }
    public String getOrganisateurNom() { return organisateurNom; }
    public String getOrganisateurEmail() { return organisateurEmail; }
    public String getDescription() { return description; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setType(int type) { this.type = type; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public void setBudgetTotal(float budgetTotal) { this.budgetTotal = budgetTotal; }
    public void setSponsorId(int sponsorId) { this.sponsorId = sponsorId; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setOrganisateurNom(String organisateurNom) { this.organisateurNom = organisateurNom; }
    public void setOrganisateurEmail(String organisateurEmail) { this.organisateurEmail = organisateurEmail; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Evenement{id=" + id + ", nom='" + nom + "', lieu='" + lieu + "', dateDebut=" + dateDebut + ", dateFin=" + dateFin + "}";
    }
}