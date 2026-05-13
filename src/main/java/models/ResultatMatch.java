package models;

public class ResultatMatch {
    private int matchId;
    private int scoreEquipe1;
    private int scoreEquipe2;
    private int gagnantId;

    public ResultatMatch() {}
    public ResultatMatch(int matchId, int scoreEquipe1, int scoreEquipe2, int gagnantId) {
        this.matchId = matchId;
        this.scoreEquipe1 = scoreEquipe1;
        this.scoreEquipe2 = scoreEquipe2;
        this.gagnantId = gagnantId;
    }

    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }
    public int getScoreEquipe1() { return scoreEquipe1; }
    public void setScoreEquipe1(int scoreEquipe1) { this.scoreEquipe1 = scoreEquipe1; }
    public int getScoreEquipe2() { return scoreEquipe2; }
    public void setScoreEquipe2(int scoreEquipe2) { this.scoreEquipe2 = scoreEquipe2; }
    public int getGagnantId() { return gagnantId; }
    public void setGagnantId(int gagnantId) { this.gagnantId = gagnantId; }

    @Override
    public String toString() {
        return "ResultatMatch{matchId=" + matchId + ", score=" + scoreEquipe1 + "-" + scoreEquipe2 + ", gagnantId=" + gagnantId + "}";
    }
}
