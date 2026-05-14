package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ExchangeRate-API — Currency conversion for sponsor budgets
 * ─────────────────────────────────────────────────────────────
 * FREE tier: 1,500 requests/month, no credit card.
 * Sign up at https://www.exchangerate-api.com → free plan → copy API key.
 *
 * Use case: sponsors may send budgets in USD/EUR/GBP.
 * This service converts any budget to TND (Tunisian Dinar) or any target.
 *
 * Usage:
 *   ExchangeRateService fx = new ExchangeRateService();
 *   double tnd = fx.convert(5000, "USD", "TND");
 *   // → 15,350.00 TND
 *
 *   Map<String,Double> rates = fx.getCommonRates("USD");
 *   // → {EUR: 0.92, GBP: 0.79, TND: 3.07, ...}
 */
public class ExchangeRateService {

    // Get free key at https://www.exchangerate-api.com
    private static final String API_KEY  = "1664ea9c6ef977b2e14360c2";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";

    private final HttpClient   httpClient;
    private final ObjectMapper mapper;

    public ExchangeRateService() {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper     = new ObjectMapper();
    }

    // ── Data model ────────────────────────────────────────────────────────
    public static class ConversionResult {
        public String fromCurrency;
        public String toCurrency;
        public double originalAmount;
        public double convertedAmount;
        public double rate;
        public String formatted() {
            return String.format("%.2f %s = %.2f %s (rate: %.4f)",
                    originalAmount, fromCurrency, convertedAmount, toCurrency, rate);
        }
    }

    // ── Public methods ────────────────────────────────────────────────────

    /**
     * Convert an amount from one currency to another.
     * @param amount   e.g. 5000
     * @param from     e.g. "USD"
     * @param to       e.g. "TND"
     */
    public double convert(double amount, String from, String to) {
        try {
            double rate = getRate(from, to);
            return Math.round(amount * rate * 100.0) / 100.0;
        } catch (Exception e) {
            System.out.println("⚠️ Currency conversion failed: " + e.getMessage());
            return amount; // fallback: return original
        }
    }

    /** Full conversion result with metadata */
    public ConversionResult convertFull(double amount, String from, String to) {
        ConversionResult result = new ConversionResult();
        result.fromCurrency   = from;
        result.toCurrency     = to;
        result.originalAmount = amount;
        try {
            result.rate            = getRate(from, to);
            result.convertedAmount = Math.round(amount * result.rate * 100.0) / 100.0;
        } catch (Exception e) {
            result.rate            = 1.0;
            result.convertedAmount = amount;
        }
        return result;
    }

    /**
     * Get common rates from a base currency.
     * Useful for showing a currency selector in the sponsor form.
     */
    public Map<String, Double> getCommonRates(String baseCurrency) {
        Map<String, Double> rates = new LinkedHashMap<>();
        String[] targets = {"EUR", "USD", "GBP", "TND", "MAD", "DZD", "AED"};
        try {
            String url = BASE_URL + API_KEY + "/latest/" + baseCurrency.toUpperCase();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)).GET().build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());
            JsonNode ratesNode = root.path("conversion_rates");
            for (String t : targets) {
                if (ratesNode.has(t)) rates.put(t, ratesNode.path(t).asDouble());
            }
        } catch (Exception e) {
            System.out.println("⚠️ ExchangeRate fetch failed: " + e.getMessage());
        }
        return rates;
    }

    /** Get all supported currencies */
    public Map<String, Double> getAllRates(String baseCurrency) {
        Map<String, Double> rates = new LinkedHashMap<>();
        try {
            String url = BASE_URL + API_KEY + "/latest/" + baseCurrency.toUpperCase();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)).GET().build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());
            JsonNode ratesNode = root.path("conversion_rates");
            ratesNode.fields().forEachRemaining(e -> rates.put(e.getKey(), e.getValue().asDouble()));
        } catch (Exception e) {
            System.out.println("⚠️ ExchangeRate getAllRates failed: " + e.getMessage());
        }
        return rates;
    }

    // ── Internal ──────────────────────────────────────────────────────────
    private double getRate(String from, String to) throws Exception {
        String url = BASE_URL + API_KEY + "/pair/" + from.toUpperCase() + "/" + to.toUpperCase();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url)).GET().build();
        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(response.body());
        if (!"success".equals(root.path("result").asText()))
            throw new Exception("API error: " + root.path("error-type").asText());
        return root.path("conversion_rate").asDouble(1.0);
    }
}
