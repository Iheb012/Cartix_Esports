package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * YouTube Data API v3 — Stream Status Service
 * Replaces TwitchService. No 2FA, no OAuth, just an API key.
 * Free: 10,000 units/day. Checking a stream = 1 unit.
 *
 * Get your key: console.cloud.google.com
 * → Enable "YouTube Data API v3" → Credentials → API Key
 */
public class YouTubeStreamService {

    private static final String API_KEY  = "YOUR_YOUTUBE_API_KEY_HERE";
    private static final String BASE_URL = "https://www.googleapis.com/youtube/v3";

    private final HttpClient   httpClient;
    private final ObjectMapper mapper;

    public YouTubeStreamService() {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper     = new ObjectMapper();
    }

    // ── Data model ────────────────────────────────────────────────────────
    public static class StreamInfo {
        public String  videoId;
        public String  title;
        public int     viewerCount;
        public boolean isLive;
        public String  thumbnailUrl;
        public String  channelName;
        public String  watchUrl() { return "https://youtube.com/watch?v=" + videoId; }
    }

    // ── Public methods ────────────────────────────────────────────────────

    /**
     * Check if a YouTube stream URL is live and get its info.
     * Accepts: "https://youtube.com/watch?v=VIDEOID"
     *          "https://youtube.com/live/VIDEOID"
     *          or a raw video ID
     */
    public Optional<StreamInfo> getStreamInfo(String youtubeUrl) {
        String videoId = extractVideoId(youtubeUrl);
        if (videoId == null || videoId.isEmpty()) return Optional.empty();
        return getStreamInfoById(videoId);
    }

    /**
     * Given a raw stream URL from PandaScore, detect if it's YouTube
     * and return stream info. Works alongside the old Twitch check.
     */
    public Optional<StreamInfo> getStreamInfoFromUrl(String rawUrl) {
        if (rawUrl == null) return Optional.empty();
        if (!rawUrl.contains("youtube.com") && !rawUrl.contains("youtu.be"))
            return Optional.empty();
        return getStreamInfo(rawUrl);
    }

    public Optional<StreamInfo> getStreamInfoById(String videoId) {
        try {
            String url = BASE_URL + "/videos"
                    + "?part=snippet,liveStreamingDetails"
                    + "&id=" + videoId
                    + "&key=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root  = mapper.readTree(response.body());
            JsonNode items = root.path("items");

            if (!items.isArray() || items.size() == 0) return Optional.empty();

            JsonNode item    = items.get(0);
            JsonNode snippet = item.path("snippet");
            JsonNode live    = item.path("liveStreamingDetails");

            // Only return if actually live
            String liveBroadcastContent = snippet.path("liveBroadcastContent").asText("");
            if (!"live".equals(liveBroadcastContent)) return Optional.empty();

            StreamInfo info = new StreamInfo();
            info.videoId     = videoId;
            info.isLive      = true;
            info.title       = snippet.path("title").asText("");
            info.channelName = snippet.path("channelTitle").asText("");
            info.thumbnailUrl = snippet.path("thumbnails").path("medium")
                    .path("url").asText("");

            // Viewer count (may be absent for some streams)
            String viewers = live.path("concurrentViewers").asText("0");
            try { info.viewerCount = Integer.parseInt(viewers); }
            catch (NumberFormatException ignored) { info.viewerCount = 0; }

            return Optional.of(info);

        } catch (Exception e) {
            System.out.println("⚠️ YouTube stream check failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    // ── Helper: extract video ID from various YouTube URL formats ─────────
    private String extractVideoId(String url) {
        if (url == null) return null;
        // Raw ID (11 chars, no slashes)
        if (!url.contains("/") && !url.contains("?")) return url;
        // youtu.be/ID
        if (url.contains("youtu.be/")) {
            return url.split("youtu\\.be/")[1].split("[?&]")[0];
        }
        // youtube.com/watch?v=ID
        if (url.contains("v=")) {
            return url.split("v=")[1].split("[?&]")[0];
        }
        // youtube.com/live/ID
        if (url.contains("/live/")) {
            return url.split("/live/")[1].split("[?&?]")[0];
        }
        return null;
    }
}