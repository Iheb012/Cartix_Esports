package models;

public class ExternalTournament {
    private String source;
    private String name;
    private String game;
    private String startDate;
    private String prizePool;
    private String location;
    private String url;
    
    public ExternalTournament(String source, String name, String game, 
                              String startDate, String prizePool, 
                              String location, String url) {
        this.source = source;
        this.name = name;
        this.game = game;
        this.startDate = startDate;
        this.prizePool = prizePool;
        this.location = location;
        this.url = url;
    }
    
    // Getters
    public String getSource() { return source; }
    public String getName() { return name; }
    public String getGame() { return game; }
    public String getStartDate() { return startDate; }
    public String getPrizePool() { return prizePool; }
    public String getLocation() { return location; }
    public String getUrl() { return url; }
}