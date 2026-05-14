package models;

public class Joueur extends User {
    private String pseudo;
    private int niveauELO;

    public Joueur() {}

    public Joueur(String nom, String prenom, String email, String mdp, String pseudo, int niveauELO) {
        super(nom, prenom, email, mdp, "joueur");
        this.pseudo = pseudo;
        this.niveauELO = niveauELO;
    }

    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }
    public int getNiveauELO() { return niveauELO; }
    public void setNiveauELO(int niveauELO) { this.niveauELO = niveauELO; }

    @Override
    public String toString() {
        return "Joueur{pseudo='" + pseudo + "', niveauELO=" + niveauELO + ", " + super.toString() + "}";
    }
}
