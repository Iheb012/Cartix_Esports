package models;

import java.sql.Timestamp;

public class Sponsor {
    private int id;
    private String nom;
    private String secteur;
    private float budgetAllouee;
    private Timestamp dateDebutContrat;
    private Timestamp dateFinContrat;
    private String statut;        // PENDING, APPROVED, REJECTED
    private String contactEmail;  // Email de contact
    private String logoUrl;       // URL du logo

    public Sponsor() {}

    public Sponsor(String nom, String secteur, float budgetAllouee, Timestamp dateDebutContrat, Timestamp dateFinContrat) {
        this.nom = nom;
        this.secteur = secteur;
        this.budgetAllouee = budgetAllouee;
        this.dateDebutContrat = dateDebutContrat;
        this.dateFinContrat = dateFinContrat;
        this.statut = "PENDING";
    }

    public Sponsor(int id, String nom, String secteur, float budgetAllouee, Timestamp dateDebutContrat, Timestamp dateFinContrat) {
        this.id = id;
        this.nom = nom;
        this.secteur = secteur;
        this.budgetAllouee = budgetAllouee;
        this.dateDebutContrat = dateDebutContrat;
        this.dateFinContrat = dateFinContrat;
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getSecteur() { return secteur; }
    public float getBudgetAllouee() { return budgetAllouee; }
    public Timestamp getDateDebutContrat() { return dateDebutContrat; }
    public Timestamp getDateFinContrat() { return dateFinContrat; }
    public String getStatut() { return statut; }
    public String getContactEmail() { return contactEmail; }
    public String getLogoUrl() { return logoUrl; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setSecteur(String secteur) { this.secteur = secteur; }
    public void setBudgetAllouee(float budgetAllouee) { this.budgetAllouee = budgetAllouee; }
    public void setDateDebutContrat(Timestamp dateDebutContrat) { this.dateDebutContrat = dateDebutContrat; }
    public void setDateFinContrat(Timestamp dateFinContrat) { this.dateFinContrat = dateFinContrat; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    @Override
    public String toString() {
        return nom;
    }
}