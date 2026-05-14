
package services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class DiscordAuthService {

    private final String clientId = "1503040357187850360";
    private final String clientSecret = "EZA2VlLUQmoL_PChHZA07VqTvROK5alf";
    private final String redirectUri = "http://localhost:8091/callback";

    public String getAuthUrl() {
        return "https://discord.com/oauth2/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=identify%20email";
    }

    public String getAccessToken(String code) throws IOException {
        URL url = new URL("https://discord.com/api/oauth2/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept", "application/json");

        String params =
                "client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&grant_type=authorization_code" +
                        "&code=" + code +
                        "&redirect_uri=" + encode(redirectUri);

        byte[] postData = params.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length",
                String.valueOf(postData.length));
        conn.getOutputStream().write(postData);

        int responseCode = conn.getResponseCode();
        InputStream is = responseCode >= 400
                ? conn.getErrorStream() : conn.getInputStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) response.append(line);

        return new JSONObject(response.toString()).getString("access_token");
    }

    public JSONObject getUserInfo(String token) throws IOException {
        URL url = new URL("https://discord.com/api/users/@me");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + token);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) response.append(line);

        return new JSONObject(response.toString());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}