package models;

public class Equipe {
    private int id;
    private String nom;
    private int capitaineId;
    private int nbMembres;
    private String Jeu;
    private String Style;

    public Equipe() {}

    public Equipe(String nom, int capitaineId, int nbMembres) {
        this.nom = nom;
        this.capitaineId = capitaineId;
        this.nbMembres = nbMembres;
    }

    public Equipe(int id, String nom, int capitaineId, int nbMembres) {
        this.id = id;
        this.nom = nom;
        this.capitaineId = capitaineId;
        this.nbMembres = nbMembres;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getCapitaineId() { return capitaineId; }
    public void setCapitaineId(int capitaineId) { this.capitaineId = capitaineId; }

    public int getNbMembres() { return nbMembres; }
    public void setNbMembres(int nbMembres) { this.nbMembres = nbMembres; }

    // Add these getters and setters for Jeu and Style
    public String getJeu() { return Jeu; }
    public void setJeu(String Jeu) { this.Jeu = Jeu; }

    public String getStyle() { return Style; }
    public void setStyle(String Style) { this.Style = Style; }

    @Override
    public String toString() {
        return "Equipe{id=" + id + ", nom='" + nom + "', capitaineId=" + capitaineId +
                ", nbMembres=" + nbMembres + ", Jeu='" + Jeu + "', Style='" + Style + "'}";
    }
}