package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * PandaScore API Service
 * ─────────────────────────────────────────────────────────────
 * Free tier: 1,000 requests/hour, no credit card needed.
 * Sign up at https://pandascore.co → Dashboard → copy your token.
 *
 * Usage in MatchesController:
 *   PandaScoreService ps = new PandaScoreService();
 *   List<ProMatch> upcoming = ps.getUpcomingMatches(10);
 *   List<ProMatch> live     = ps.getLiveMatches();
 *   List<ProMatch> past     = ps.getPastMatches(10);
 */
public class PandaScoreService {

    // ── CONFIG ─────────────────────────────────────────────────────────────
    // Paste your token from https://app.pandascore.co/dashboard
    private static final String API_TOKEN = "gy1tzZBiuvQmxV7FGki2bC7da1pcJHtZn8rGLGmd6m_BsPvFJmA";
    private static final String BASE_URL   = "https://api.pandascore.co";

    private final HttpClient  httpClient;
    private final ObjectMapper mapper;
    /** Lazy-filled from {@code GET /videogames} — needed for {@code /teams} (filter uses videogame_id). */
    private volatile Map<String, Integer> videogameSlugToId;

    public PandaScoreService() {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper     = new ObjectMapper();
    }

    // ── DATA MODEL ─────────────────────────────────────────────────────────
    /** Lightweight representation of a pro match returned by the API. */
    public static class ProMatch {
        public int    id;
        public String name;          // e.g. "Fnatic vs Team Liquid"
        public String status;        // "not_started" | "running" | "finished"
        public String scheduledAt;   // ISO-8601 datetime
        public String game;          // "cs-go", "league-of-legends", etc.
        public String tournament;    // Tournament name
        public String league;        // League name
        public String team1Name;
        public String team2Name;
        public String team1Logo;     // URL to team logo image
        public String team2Logo;
        public int    score1;
        public int    score2;
        public String winner;        // winning team name (or "" if not finished)
        public String streamUrl;     // Twitch/YouTube stream link

        /** Maps PandaScore status → your app's statut values */
        public String toAppStatus() {
            return switch (status == null ? "" : status) {
                case "running"     -> "en_cours";
                case "not_started" -> "planifie";
                case "finished"    -> "termine";
                case "canceled"    -> "annule";
                default            -> "planifie";
            };
        }

        @Override
        public String toString() {
            return team1Name + " vs " + team2Name + " [" + status + "] " + scheduledAt;
        }
    }

    // ── PUBLIC METHODS ─────────────────────────────────────────────────────

    /** Upcoming matches (all games). Pass a game slug to filter, e.g. "cs-go". */
    public List<ProMatch> getUpcomingMatches(int pageSize) {
        return fetchMatches("/matches/upcoming?page[size]=" + pageSize + "&sort=scheduled_at");
    }

    /** Currently running (live) matches. */
    public List<ProMatch> getLiveMatches() {
        return fetchMatches("/matches/running?page[size]=20");
    }

    /** Recently finished matches (all games). */
    public List<ProMatch> getPastMatches(int pageSize) {
        return fetchMatches("/matches/past?page[size]=" + pageSize + "&sort=-scheduled_at");
    }

    /**
     * PandaScore {@code filter[videogame]} slug (not the in-app display name).
     * See <a href="https://developers.pandascore.co/">PandaScore docs</a> — e.g. {@code r6-siege}, {@code rl}.
     */
    public static String videogameSlugForAppGame(String gameId) {
        if (gameId == null || gameId.isBlank()) return "valorant";
        return switch (gameId.trim()) {
            case "CS2" -> "cs-go";
            case "LoL" -> "league-of-legends";
            case "Dota2" -> "dota-2";
            case "R6 Siege" -> "r6-siege";
            case "Rocket League" -> "rl";
            default -> "valorant";
        };
    }

    /** Recently finished matches for a videogame. */
    public List<ProMatch> getPastByGame(String videogameSlug, int pageSize) {
        List<ProMatch> a = fetchMatches(matchesUrlFiltered("past", pageSize, "&sort=-scheduled_at", videogameSlug));
        if (a.isEmpty() && "rl".equals(videogameSlug)) {
            return fetchMatches(matchesUrlFiltered("past", pageSize, "&sort=-scheduled_at", "rocket-league"));
        }
        return a;
    }

    /** Upcoming matches for a videogame. */
    public List<ProMatch> getUpcomingByGame(String videogameSlug, int pageSize) {
        List<ProMatch> a = fetchMatches(matchesUrlFiltered("upcoming", pageSize, "&sort=scheduled_at", videogameSlug));
        if (a.isEmpty() && "rl".equals(videogameSlug)) {
            return fetchMatches(matchesUrlFiltered("upcoming", pageSize, "&sort=scheduled_at", "rocket-league"));
        }
        return a;
    }

    /** Live matches for a videogame. */
    public List<ProMatch> getLiveByGame(String videogameSlug) {
        List<ProMatch> a = fetchMatches(matchesUrlFiltered("running", 24, "", videogameSlug));
        if (a.isEmpty() && "rl".equals(videogameSlug)) {
            return fetchMatches(matchesUrlFiltered("running", 24, "", "rocket-league"));
        }
        return a;
    }

    /**
     * Correct PandaScore shape: {@code /matches/running?page[size]=20&filter[videogame]=cs-go}
     * (prefix URLs like {@code /cs-go/matches/...} return 404 on current API.)
     */
    private static String matchesUrlFiltered(String kind, int pageSize, String extraQuery, String slug) {
        String enc = URLEncoder.encode(slug, StandardCharsets.UTF_8);
        String q = extraQuery == null ? "" : extraQuery;
        if (!q.isEmpty() && !q.startsWith("&")) {
            q = "&" + q;
        }
        return "/matches/" + kind + "?page[size]=" + pageSize + q + "&filter%5Bvideogame%5D=" + enc;
    }

    // ── TEAMS (world ranking feed by title) ───────────────────────────────

    /** Lightweight team row from PandaScore {@code /teams}. */
    public static class ProTeam {
        public int id;
        public String name;
        public String acronym;
        public String imageUrl;
        public String slug;
        /** Higher = better when API provides it (else 0). */
        public int rankingScore;
    }

    /** Roster row from {@code GET /teams/{id}}. */
    public static class ProTeamPlayer {
        public int id;
        public String name;
        public String slug;
        public String role;
        public String nationality;
        public String imageUrl;
        public boolean active;
    }

    /** Full team payload from {@code GET /teams/{id_or_slug}}. */
    public static class ProTeamDetail {
        public int id;
        public String name;
        public String acronym;
        public String slug;
        public String imageUrl;
        public String darkImageUrl;
        public String location;
        public String modifiedAt;
        public String videogameName;
        public String videogameSlug;
        public final List<ProTeamPlayer> players = new ArrayList<>();
    }

    /**
     * Single team with roster (PandaScore {@code /teams/{id}}).
     */
    public ProTeamDetail getTeamById(int teamId) {
        if (teamId <= 0) return null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/teams/" + teamId))
                    .header("Authorization", "Bearer " + API_TOKEN)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println("⚠️ PandaScore team HTTP " + response.statusCode() + " id=" + teamId);
                return null;
            }
            JsonNode root = mapper.readTree(response.body());
            if (!root.isObject()) return null;
            return parseTeamDetail(root);
        } catch (Exception e) {
            System.out.println("❌ PandaScore team fetch: " + e.getMessage());
            return null;
        }
    }

    private ProTeamDetail parseTeamDetail(JsonNode node) {
        ProTeamDetail d = new ProTeamDetail();
        d.id = node.path("id").asInt();
        d.name = node.path("name").asText("Team");
        d.acronym = node.path("acronym").asText("");
        d.slug = node.path("slug").asText("");
        d.imageUrl = node.path("image_url").asText("");
        d.darkImageUrl = node.path("dark_mode_image_url").asText("");
        d.location = node.path("location").asText("");
        d.modifiedAt = node.path("modified_at").asText("");
        JsonNode vg = node.path("current_videogame");
        d.videogameName = vg.path("name").asText("");
        d.videogameSlug = vg.path("slug").asText("");
        JsonNode players = node.path("players");
        if (players.isArray()) {
            for (JsonNode p : players) {
                ProTeamPlayer pl = new ProTeamPlayer();
                pl.id = p.path("id").asInt();
                pl.name = p.path("name").asText("");
                pl.slug = p.path("slug").asText("");
                pl.role = p.path("role").asText("");
                pl.nationality = p.path("nationality").asText("");
                pl.imageUrl = p.path("image_url").asText("");
                pl.active = p.path("active").asBoolean(true);
                d.players.add(pl);
            }
        }
        return d;
    }

    /**
     * Top teams for a title. PandaScore {@code /teams} expects {@code filter[videogame_id]} (integer),
     * not a string slug — we resolve the slug via {@code /videogames}.
     */
    public List<ProTeam> getTopTeams(String videogameSlug, int limit) {
        int n = Math.min(Math.max(limit, 1), 50);
        Integer vid = resolveVideogameId(videogameSlug);
        if (vid != null && vid > 0) {
            List<ProTeam> a = fetchTeams("/teams?page[size]=" + n + "&filter%5Bvideogame_id%5D=" + vid + "&sort=-score");
            if (!a.isEmpty()) return a;
            List<ProTeam> b = fetchTeams("/teams?page[size]=" + n + "&filter%5Bvideogame_id%5D=" + vid);
            if (!b.isEmpty()) return b;
        }
        String enc = URLEncoder.encode(videogameSlug, StandardCharsets.UTF_8);
        return fetchTeams("/teams?page[size]=" + n + "&filter%5Bvideogame%5D=" + enc);
    }

    private Integer resolveVideogameId(String slug) {
        ensureVideogameCatalog();
        if (videogameSlugToId == null) return null;
        Integer id = videogameSlugToId.get(slug);
        if (id != null) return id;
        if ("rl".equals(slug)) return videogameSlugToId.get("rocket-league");
        return null;
    }

    private void ensureVideogameCatalog() {
        if (videogameSlugToId != null) return;
        synchronized (this) {
            if (videogameSlugToId != null) return;
            Map<String, Integer> map = new HashMap<>();
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/videogames?page[size]=100"))
                        .header("Authorization", "Bearer " + API_TOKEN)
                        .header("Accept", "application/json")
                        .GET()
                        .build();
                HttpResponse<String> response =
                        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    System.out.println("⚠️ PandaScore videogames HTTP " + response.statusCode());
                    videogameSlugToId = map;
                    return;
                }
                JsonNode root = mapper.readTree(response.body());
                if (!root.isArray()) {
                    videogameSlugToId = map;
                    return;
                }
                for (JsonNode node : root) {
                    int id = node.path("id").asInt(0);
                    String slug = node.path("slug").asText("");
                    if (id > 0 && !slug.isEmpty()) {
                        map.put(slug, id);
                    }
                }
            } catch (Exception e) {
                System.out.println("❌ PandaScore videogames: " + e.getMessage());
            }
            videogameSlugToId = map;
        }
    }

    private List<ProTeam> fetchTeams(String endpoint) {
        List<ProTeam> result = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Authorization", "Bearer " + API_TOKEN)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println("⚠️ PandaScore teams HTTP " + response.statusCode() + " for " + endpoint);
                return result;
            }
            JsonNode root = mapper.readTree(response.body());
            if (!root.isArray()) return result;
            for (JsonNode node : root) {
                result.add(parseTeam(node));
            }
        } catch (Exception e) {
            System.out.println("❌ PandaScore teams fetch failed: " + e.getMessage());
        }
        return result;
    }

    private ProTeam parseTeam(JsonNode node) {
        ProTeam t = new ProTeam();
        t.id = node.path("id").asInt();
        t.name = node.path("name").asText("Team");
        t.acronym = node.path("acronym").asText("");
        t.imageUrl = node.path("image_url").asText("");
        if (t.imageUrl.isEmpty()) {
            t.imageUrl = node.path("light_image_url").asText("");
        }
        t.slug = node.path("slug").asText("");
        t.rankingScore = node.path("score").asInt(0);
        return t;
    }

    // ── INTERNAL ──────────────────────────────────────────────────────────

    private List<ProMatch> fetchMatches(String endpoint) {
        List<ProMatch> result = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Authorization", "Bearer " + API_TOKEN)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("⚠️ PandaScore HTTP " + response.statusCode()
                        + " for " + endpoint);
                return result;
            }

            JsonNode root = mapper.readTree(response.body());
            if (!root.isArray()) return result;

            for (JsonNode node : root) {
                result.add(parseMatch(node));
            }

        } catch (Exception e) {
            System.out.println("❌ PandaScore fetch failed: " + e.getMessage());
        }
        return result;
    }

    private ProMatch parseMatch(JsonNode node) {
        ProMatch m = new ProMatch();
        m.id          = node.path("id").asInt();
        m.name        = node.path("name").asText("");
        m.status      = node.path("status").asText("not_started");
        m.scheduledAt = node.path("scheduled_at").asText("");

        // Game
        JsonNode gameNode = node.path("videogame");
        m.game = gameNode.path("slug").asText("unknown");

        // Tournament & League
        JsonNode tournNode = node.path("tournament");
        m.tournament = tournNode.path("name").asText("");

        JsonNode leagueNode = node.path("league");
        m.league = leagueNode.path("name").asText("");

        // Teams
        JsonNode opponents = node.path("opponents");
        if (opponents.isArray() && opponents.size() >= 1) {
            JsonNode op1 = opponents.get(0).path("opponent");
            m.team1Name = op1.path("name").asText("TBD");
            m.team1Logo = op1.path("image_url").asText("");
        } else {
            m.team1Name = "TBD";
        }
        if (opponents.isArray() && opponents.size() >= 2) {
            JsonNode op2 = opponents.get(1).path("opponent");
            m.team2Name = op2.path("name").asText("TBD");
            m.team2Logo = op2.path("image_url").asText("");
        } else {
            m.team2Name = "TBD";
        }

        // Score (results array)
        JsonNode results = node.path("results");
        if (results.isArray() && results.size() >= 1) {
            m.score1 = results.get(0).path("score").asInt(0);
        }
        if (results.isArray() && results.size() >= 2) {
            m.score2 = results.get(1).path("score").asInt(0);
        }

        // Winner
        JsonNode winner = node.path("winner");
        m.winner = winner.path("name").asText("");

        // Stream
        JsonNode streams = node.path("streams_list");
        if (streams.isArray() && streams.size() > 0) {
            m.streamUrl = streams.get(0).path("raw_url").asText("");
        }

        return m;
    }
}
