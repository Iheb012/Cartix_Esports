package models;

public class Administrateur extends User {

    private int niveauAcces;
    private String token;
    private int sessionActive;
    private int tentativesEchouees;
    private String dateExpiration;

    public Administrateur(int id, String nom, String prenom, String email,
                          String mdp, int niveauAcces, String token,
                          int sessionActive, int tentativesEchouees,
                          String dateExpiration) {
        super(id, nom, prenom, email, mdp, "admin");
        this.niveauAcces = niveauAcces;
        this.token = token;
        this.sessionActive = sessionActive;
        this.tentativesEchouees = tentativesEchouees;
        this.dateExpiration = dateExpiration;
    }

    public int getNiveauAcces() { return niveauAcces; }
    public String getToken() { return token; }
    public int getSessionActive() { return sessionActive; }
    public int getTentativesEchouees() { return tentativesEchouees; }
    public String getDateExpiration() { return dateExpiration; }

    public void setNiveauAcces(int niveauAcces) { this.niveauAcces = niveauAcces; }
    public void setToken(String token) { this.token = token; }
    public void setSessionActive(int sessionActive) { this.sessionActive = sessionActive; }
    public void setTentativesEchouees(int tentativesEchouees) { this.tentativesEchouees = tentativesEchouees; }
    public void setDateExpiration(String dateExpiration) { this.dateExpiration = dateExpiration; }
}
