package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class ClearbitService {

    public Optional<CompanyInfo> searchCompany(String companyName) {
        try {
            String query = companyName.trim().toLowerCase().replace(" ", "");
            String apiUrl = "https://autocomplete.clearbit.com/v1/companies/suggest?query=" + query;

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String jsonResponse = response.toString();

            // Parsing simple sans Gson (recherche du premier résultat)
            String name = extractJsonValue(jsonResponse, "name");
            String domain = extractJsonValue(jsonResponse, "domain");

            if (name != null && domain != null) {
                String logoUrl = "https://logo.clearbit.com/" + domain;
                return Optional.of(new CompanyInfo(name, domain, logoUrl));
            }
        } catch (Exception e) {
            System.out.println("Clearbit lookup failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;

        startIndex += searchKey.length();
        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) return null;

        return json.substring(startIndex, endIndex);
    }

    public static class CompanyInfo {
        public final String name;
        public final String domain;
        public final String logoUrl;

        public CompanyInfo(String name, String domain, String logoUrl) {
            this.name = name;
            this.domain = domain;
            this.logoUrl = logoUrl;
        }
    }
}