package models;

public class Joueur {
    private int id;
    private String pseudo;
    private String nom;
    private String prenom;
    private String game;
    private String region;
    private int equipeId;
    private int kills;
    private int deaths;
    private int assists;
    private double kda;
    private String winRate;
    private String status;
    private boolean favorite;
    private int rank;
    private int matches;
    private int userId;  // Ajouter cet attribut
    public Joueur() {}

    // Getters
    public int getId() { return id; }
    public String getPseudo() { return pseudo; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getGame() { return game; }
    public String getRegion() { return region; }
    public int getEquipeId() { return equipeId; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getAssists() { return assists; }
    public double getKda() { return kda; }
    public String getWinRate() { return winRate; }
    public String getStatus() { return status; }
    public boolean isFavorite() { return favorite; }
    public int getRank() { return rank; }
    public int getMatches() { return matches; }
    public int getUserId() { return userId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setGame(String game) { this.game = game; }
    public void setRegion(String region) { this.region = region; }
    public void setEquipeId(int equipeId) { this.equipeId = equipeId; }
    public void setKills(int kills) { this.kills = kills; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public void setAssists(int assists) { this.assists = assists; }
    public void setKda(double kda) { this.kda = kda; }
    public void setWinRate(String winRate) { this.winRate = winRate; }
    public void setStatus(String status) { this.status = status; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public void setRank(int rank) { this.rank = rank; }
    public void setMatches(int matches) { this.matches = matches; }
    public void setUserId(int userId) { this.userId = userId; }

}