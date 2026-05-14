package services;

import models.ExternalTournament;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PandaScoreApiService {

    // 🔑 INSÈRE TA CLÉ API ICI
    private static final String API_KEY = "YftWi4Cm-ns4I7yQHoeTDSmYR5d9ZPRCLRqMZSHsnd9Zbuw1FIU";
    
    private static final String BASE_URL = "https://api.pandascore.co";

    public List<ExternalTournament> getUpcomingTournaments(String game) {
        List<ExternalTournament> tournaments = new ArrayList<>();

        String gameParam = switch (game.toLowerCase()) {
            case "valorant" -> "valorant";
            case "cs2" -> "csgo";
            case "league-of-legends" -> "lol";
            case "dota2" -> "dota2";
            default -> game.toLowerCase();
        };

        // Endpoint pour récupérer les tournois d'un jeu
        String urlString = BASE_URL + "/" + gameParam + "/tournaments?token=" + API_KEY + "&per_page=20";

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            int responseCode = conn.getResponseCode();
            System.out.println("📡 API Response Code: " + responseCode);

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject t = jsonArray.getJSONObject(i);
                    String name = t.optString("name", "Unknown Tournament");
                    String startDate = t.optString("begin_at", "").split("T")[0];
                    String prizePool = t.optString("prizepool", "TBD");
                    String location = t.optString("city", "Online");

                    // Option 1: Recherche Google
                    String urlLink = "https://www.google.com/search?q=" + java.net.URLEncoder.encode(name + " " + game + " tournament", "UTF-8");

                    // Option 2: Liquipedia (si tu préfères)
                    // String urlLink = "https://liquipedia.net/" + gameParam + "/" + name.replace(" ", "_");

                    tournaments.add(new ExternalTournament(
                            "PandaScore",
                            name,
                            game.toUpperCase(),
                            startDate,
                            prizePool,
                            location,
                            urlLink
                    ));
                }
                System.out.println("✅ " + tournaments.size() + " tournois récupérés pour " + game);
            } else {
                // Lire l'erreur pour debug
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                System.err.println("❌ API Error " + responseCode + ": " + errorResponse.toString());
                return getDemoTournaments(game);
            }
            
            conn.disconnect();

        } catch (Exception e) {
            System.err.println("❌ Exception: " + e.getMessage());
            e.printStackTrace();
            return getDemoTournaments(game);
        }

        return tournaments;
    }

    private List<ExternalTournament> getDemoTournaments(String game) {
        List<ExternalTournament> demos = new ArrayList<>();
        String gameUpper = game.toUpperCase();
        
        demos.add(new ExternalTournament(
            "PandaScore (Demo)", 
            "VCT Champions 2025", 
            gameUpper, 
            "2026-06-01", 
            "$1,000,000", 
            "Paris", 
            "#"
        ));
        demos.add(new ExternalTournament(
            "PandaScore (Demo)", 
            "IEM Cologne 2025", 
            gameUpper, 
            "2026-07-15", 
            "$500,000", 
            "Cologne", 
            "#"
        ));
        demos.add(new ExternalTournament(
            "PandaScore (Demo)", 
            "World Championship", 
            gameUpper, 
            "2026-08-20", 
            "$2,000,000", 
            "Seoul", 
            "#"
        ));
        
        return demos;
    }
}