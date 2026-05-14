package models;

public class Bracket {
    private int id;
    private int tournoiId;
    private String type;
    private int nbRounds;

    public Bracket() {}
    public Bracket(int tournoiId, String type, int nbRounds) {
        this.tournoiId = tournoiId;
        this.type = type;
        this.nbRounds = nbRounds;
    }
    public Bracket(int id, int tournoiId, String type, int nbRounds) {
        this.id = id;
        this.tournoiId = tournoiId;
        this.type = type;
        this.nbRounds = nbRounds;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTournoiId() { return tournoiId; }
    public void setTournoiId(int tournoiId) { this.tournoiId = tournoiId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getNbRounds() { return nbRounds; }
    public void setNbRounds(int nbRounds) { this.nbRounds = nbRounds; }

    @Override
    public String toString() {
        return "Bracket{id=" + id + ", tournoiId=" + tournoiId + ", type='" + type + "', nbRounds=" + nbRounds + "}";
    }
}
