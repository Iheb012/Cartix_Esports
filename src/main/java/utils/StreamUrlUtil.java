package utils;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns common stream URLs into something JavaFX {@code WebView} can load.
 */
public final class StreamUrlUtil {

    public enum StreamPlatform {
        TWITCH, KICK, YOUTUBE, OTHER
    }

    /** Parsed stream target for embed / thumbnails. */
    public static final class ResolvedStream {
        public final StreamPlatform platform;
        /** Twitch or Kick login. */
        public final String channel;
        /** YouTube video id when platform is YOUTUBE. */
        public final String youtubeVideoId;
        public final String originalUrl;

        public ResolvedStream(StreamPlatform platform, String channel, String youtubeVideoId, String originalUrl) {
            this.platform = platform;
            this.channel = channel;
            this.youtubeVideoId = youtubeVideoId;
            this.originalUrl = originalUrl;
        }
    }

    private static final Pattern YT_WATCH = Pattern.compile("[?&]v=([a-zA-Z0-9_-]{6,})");
    private static final Pattern YT_SHORT = Pattern.compile("youtu\\.be/([a-zA-Z0-9_-]{6,})");
    private static final Pattern TWITCH_PATH = Pattern.compile("twitch\\.tv/([^/?#]+)");
    private static final Pattern KICK_PATH = Pattern.compile("kick\\.com/([^/?#]+)");

    private StreamUrlUtil() {}

    /**
     * Twitch CDN preview (updates while live). Works even when the channel blocks iframe embeds.
     */
    public static String twitchLiveThumbnailUrl(String channel) {
        if (channel == null || channel.isBlank()) return null;
        String c = channel.trim().toLowerCase();
        if (!c.matches("[a-z0-9_]{2,64}")) return null;
        return "https://static-cdn.jtvnw.net/previews-ttv/live_user_" + c + "-1280x720.jpg";
    }

    public static ResolvedStream resolve(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ResolvedStream(StreamPlatform.OTHER, null, null, raw);
        }
        String u = raw.trim();

        Matcher tw = TWITCH_PATH.matcher(u);
        if (tw.find()) {
            String channel = tw.group(1);
            if ("videos".equalsIgnoreCase(channel) || "directory".equalsIgnoreCase(channel)) {
                return new ResolvedStream(StreamPlatform.OTHER, null, null, u);
            }
            return new ResolvedStream(StreamPlatform.TWITCH, channel, null, u);
        }

        Matcher kick = KICK_PATH.matcher(u);
        if (kick.find()) {
            String ch = kick.group(1);
            if ("video".equalsIgnoreCase(ch) || "categories".equalsIgnoreCase(ch)) {
                return new ResolvedStream(StreamPlatform.OTHER, null, null, u);
            }
            return new ResolvedStream(StreamPlatform.KICK, ch, null, u);
        }

        if (u.contains("youtube.com/embed/")) {
            Matcher em = Pattern.compile("embed/([a-zA-Z0-9_-]{6,})").matcher(u);
            if (em.find()) {
                return new ResolvedStream(StreamPlatform.YOUTUBE, null, em.group(1), u);
            }
        }
        Matcher mWatch = YT_WATCH.matcher(u);
        if (mWatch.find()) {
            return new ResolvedStream(StreamPlatform.YOUTUBE, null, mWatch.group(1), u);
        }
        Matcher mShort = YT_SHORT.matcher(u);
        if (mShort.find()) {
            return new ResolvedStream(StreamPlatform.YOUTUBE, null, mShort.group(1), u);
        }

        return new ResolvedStream(StreamPlatform.OTHER, null, null, u);
    }

    public static String toWatchableUrl(String raw) {
        if (raw == null || raw.isBlank()) return raw;
        String u = raw.trim();

        if (u.contains("youtube.com/embed/")) return u;
        if (u.contains("youtube.com/watch")) {
            Matcher m = YT_WATCH.matcher(u);
            if (m.find()) {
                return "https://www.youtube.com/embed/" + m.group(1) + "?autoplay=1&rel=0";
            }
        }
        if (u.contains("youtu.be/")) {
            Matcher m = YT_SHORT.matcher(u);
            if (m.find()) {
                return "https://www.youtube.com/embed/" + m.group(1) + "?autoplay=1&rel=0";
            }
        }

        Matcher tw = TWITCH_PATH.matcher(u);
        if (tw.find()) {
            String channel = tw.group(1);
            if ("videos".equalsIgnoreCase(channel) || "directory".equalsIgnoreCase(channel)) {
                return u;
            }
            return "https://player.twitch.tv/?channel=" + channel
                    + "&parent=localhost&parent=127.0.0.1";
        }

        Matcher kick = KICK_PATH.matcher(u);
        if (kick.find()) {
            String ch = kick.group(1);
            return "https://player.kick.com/" + ch + "?autoplay=true";
        }

        return u;
    }

    public static String titleFromUrl(String raw) {
        try {
            URI uri = URI.create(raw);
            return uri.getHost() != null ? uri.getHost() : "Live";
        } catch (Exception e) {
            return "Live";
        }
    }
}
