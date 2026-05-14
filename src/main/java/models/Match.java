package models;

import java.sql.Date;
import java.sql.Timestamp;

public class Match {
    private int id;
    private int equipe1Id;
    private int equipe2Id;
    private Date dateMatch;
    private String statut;
    private int tournoiId;
    private String recompense;
    private Timestamp dateObtentionRecompense;
    /** Dashboard game id: Valorant, CS2, LoL, … — null means show for any selected game */
    private String jeu;

    public Match() {}

    public Match(int equipe1Id, int equipe2Id, Date dateMatch, String statut, int tournoiId, String recompense, Timestamp dateObtentionRecompense) {
        this(equipe1Id, equipe2Id, dateMatch, statut, tournoiId, recompense, dateObtentionRecompense, null);
    }

    public Match(int equipe1Id, int equipe2Id, Date dateMatch, String statut, int tournoiId, String recompense, Timestamp dateObtentionRecompense, String jeu) {
        this.equipe1Id = equipe1Id;
        this.equipe2Id = equipe2Id;
        this.dateMatch = dateMatch;
        this.statut = statut;
        this.tournoiId = tournoiId;
        this.recompense = recompense;
        this.dateObtentionRecompense = dateObtentionRecompense;
        this.jeu = jeu;
    }

    public Match(int id, int equipe1Id, int equipe2Id, Date dateMatch, String statut, int tournoiId, String recompense, Timestamp dateObtentionRecompense) {
        this(id, equipe1Id, equipe2Id, dateMatch, statut, tournoiId, recompense, dateObtentionRecompense, null);
    }

    public Match(int id, int equipe1Id, int equipe2Id, Date dateMatch, String statut, int tournoiId, String recompense, Timestamp dateObtentionRecompense, String jeu) {
        this.id = id;
        this.equipe1Id = equipe1Id;
        this.equipe2Id = equipe2Id;
        this.dateMatch = dateMatch;
        this.statut = statut;
        this.tournoiId = tournoiId;
        this.recompense = recompense;
        this.dateObtentionRecompense = dateObtentionRecompense;
        this.jeu = jeu;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEquipe1Id() { return equipe1Id; }
    public void setEquipe1Id(int equipe1Id) { this.equipe1Id = equipe1Id; }
    public int getEquipe2Id() { return equipe2Id; }
    public void setEquipe2Id(int equipe2Id) { this.equipe2Id = equipe2Id; }
    public Date getDateMatch() { return dateMatch; }
    public void setDateMatch(Date dateMatch) { this.dateMatch = dateMatch; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public int getTournoiId() { return tournoiId; }
    public void setTournoiId(int tournoiId) { this.tournoiId = tournoiId; }
    public String getRecompense() { return recompense; }
    public void setRecompense(String recompense) { this.recompense = recompense; }
    public Timestamp getDateObtentionRecompense() { return dateObtentionRecompense; }
    public void setDateObtentionRecompense(Timestamp dateObtentionRecompense) { this.dateObtentionRecompense = dateObtentionRecompense; }
    public String getJeu() { return jeu; }
    public void setJeu(String jeu) { this.jeu = jeu; }

    @Override
    public String toString() {
        return "Match{id=" + id + ", equipe1Id=" + equipe1Id + ", equipe2Id=" + equipe2Id +
               ", dateMatch=" + dateMatch + ", statut='" + statut + "', tournoiId=" + tournoiId +
               ", recompense='" + recompense + "'}";
    }
}
