package com.esport.services;

import com.esport.models.Produit;
import com.esport.models.SetupItem;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

public class SetupService {

    private static final Map<String, String> ICONS = new LinkedHashMap<>();
    private static final Map<String, String> LABELS = new LinkedHashMap<>();

    static {
        ICONS.put("cpu", "\u26A1"); ICONS.put("gpu", "\uD83C\uDFAE"); ICONS.put("ram", "\uD83E\uDDE0");
        ICONS.put("ssd", "\uD83D\uDCBE"); ICONS.put("motherboard", "\uD83D\uDD0C");
        ICONS.put("souris", "\uD83D\uDDB1\uFE0F"); ICONS.put("clavier", "\u2328\uFE0F");
        ICONS.put("casque", "\uD83C\uDFA7"); ICONS.put("tapis", "\uD83D\uDFE9");
        ICONS.put("ecran", "\uD83D\uDDA5\uFE0F"); ICONS.put("chaise", "\uD83D\uDEBA");
        ICONS.put("pack", "\uD83D\uDCE6");

        LABELS.put("cpu", "Processeur"); LABELS.put("gpu", "Carte graphique");
        LABELS.put("ram", "RAM"); LABELS.put("ssd", "Stockage SSD");
        LABELS.put("motherboard", "Carte mère");
        LABELS.put("souris", "Souris"); LABELS.put("clavier", "Clavier");
        LABELS.put("casque", "Casque"); LABELS.put("tapis", "Tapis de souris");
        LABELS.put("ecran", "Écran"); LABELS.put("chaise", "Chaise");
    }

    private static final Map<String, List<String>> TYPE_KEYWORDS = new LinkedHashMap<>();
    static {
        TYPE_KEYWORDS.put("cpu", Arrays.asList("cpu", "processeur", "ryzen", "core i", "amd ryzen", "intel core"));
        TYPE_KEYWORDS.put("gpu", Arrays.asList("gpu", "rtx", "radeon", "carte graphique", "geforce", "nvidia"));
        TYPE_KEYWORDS.put("ram", Arrays.asList("ram", "ddr5", "ddr4", "vengeance", "trident", "crucial"));
        TYPE_KEYWORDS.put("ssd", Arrays.asList("ssd", "nvme", "990 pro", "980 pro", "sn850", "samsung", "wd black"));
        TYPE_KEYWORDS.put("motherboard", Arrays.asList("motherboard", "carte mère", "b760", "z790", "b650", "tomahawk", "strix", "aorus", "msi mag", "gigabyte", "asus rog"));
        TYPE_KEYWORDS.put("souris", Arrays.asList("souris", "mouse", "deathadder", "viper", "g502", "logitech", "razer"));
        TYPE_KEYWORDS.put("clavier", Arrays.asList("clavier", "keyboard", "k70", "apex", "blackwidow", "corsair", "steelseries", "razer"));
        TYPE_KEYWORDS.put("casque", Arrays.asList("casque", "headset", "headphone", "g pro x", "logitech", "hyperx"));
        TYPE_KEYWORDS.put("ecran", Arrays.asList("ecran", "monitor", "lg", "msi", "ultragear", "27gp"));
    }

    private static final List<String> TYPE_PRIORITY = Arrays.asList(
        "gpu", "cpu", "ecran", "ram", "ssd", "motherboard",
        "clavier", "souris", "casque", "tapis", "chaise"
    );

    public static class SetupResult {
        public final List<SetupItem> pcItems = new ArrayList<>();
        public final List<SetupItem> peripheralItems = new ArrayList<>();
        public SetupItem screenItem;
        public SetupItem chaiseItem;
        public double totalCost;
        public double pcCost;
        public double peripheralCost;
        public double screenCost;
        public double chaiseCost;
        public double remaining;
    }

    public SetupResult genererConfiguration(double budget) {
        SetupResult result = new SetupResult();
        List<Produit> allProducts = chargerTousProduits();

        // Group by type, pick the single best product per type
        Map<String, Produit> bestPerType = new LinkedHashMap<>();
        for (Produit p : allProducts) {
            if (p.getPrix() <= 0 || p.getStock() <= 0) continue;
            String type = detecterType(p);
            if (type == null) continue;
            Produit existing = bestPerType.get(type);
            double score = p.getNote() * Math.log(Math.max(1, p.getNbAvis()) + 1) / p.getPrix();
            double existingScore = existing != null
                    ? existing.getNote() * Math.log(Math.max(1, existing.getNbAvis()) + 1) / existing.getPrix()
                    : -1;
            if (existing == null || score > existingScore) {
                bestPerType.put(type, p);
            }
        }

        // Sort types by priority, then include as many as budget allows
        double used = 0;
        Set<String> included = new LinkedHashSet<>();
        for (String type : TYPE_PRIORITY) {
            Produit best = bestPerType.get(type);
            if (best == null) continue;
            if (used + best.getPrix() > budget) continue;
            included.add(type);
            used += best.getPrix();
        }

        // Distribute into result sections
        for (String type : TYPE_PRIORITY) {
            if (!included.contains(type)) continue;
            Produit p = bestPerType.get(type);
            SetupItem item = new SetupItem(
                    (LABELS.containsKey(type) ? LABELS.get(type) + " : " : "") + p.getNom(),
                    ICONS.getOrDefault(type, "\uD83D\uDCE6"),
                    type, p, p.getPrix(), false);

            if ("ecran".equals(type)) {
                result.screenItem = item;
                result.screenCost += p.getPrix();
            } else if ("chaise".equals(type)) {
                result.chaiseItem = item;
                result.chaiseCost += p.getPrix();
            } else if ("souris".equals(type) || "clavier".equals(type) || "casque".equals(type) || "tapis".equals(type)) {
                result.peripheralItems.add(item);
                result.peripheralCost += p.getPrix();
            } else {
                result.pcItems.add(item);
                result.pcCost += p.getPrix();
            }
        }

        result.totalCost = used;
        result.remaining = budget - used;
        return result;
    }

    private String detecterType(Produit p) {
        String nom = p.getNom().toLowerCase();
        String desc = p.getDescription().toLowerCase();
        String cat = p.getCategorie().toLowerCase();

        // First try by category name
        if ("ecran".equals(cat)) return "ecran";
        if ("souris".equals(cat)) return "souris";
        if ("clavier".equals(cat)) return "clavier";
        if ("casque".equals(cat)) return "casque";
        if ("tapis".equals(cat)) return "tapis";
        if ("chaise".equals(cat)) return "chaise";

        // For Composant category, detect by keywords
        if (cat.contains("composant")) {
            for (Map.Entry<String, List<String>> e : TYPE_KEYWORDS.entrySet()) {
                for (String kw : e.getValue()) {
                    if (nom.contains(kw) || desc.contains(kw)) return e.getKey();
                }
            }
        }

        // Fallback: scan all keywords
        for (Map.Entry<String, List<String>> e : TYPE_KEYWORDS.entrySet()) {
            for (String kw : e.getValue()) {
                if (nom.contains(kw) || desc.contains(kw)) return e.getKey();
            }
        }

        return "pack";
    }

    private List<Produit> chargerTousProduits() {
        List<Produit> list = new ArrayList<>();
        try (var conn = DatabaseConnection.getConnection();
             var ps = conn.prepareStatement("SELECT * FROM pro");
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                Produit p = new Produit();
                p.setId(rs.getInt("Id"));
                p.setNom(rs.getString("nom"));
                p.setDescription(rs.getString("description"));
                p.setPrix(rs.getDouble("prix"));
                p.setStock(rs.getInt("stock"));
                p.setCategorie(rs.getString("categorie"));
                p.setImageUrl(rs.getString("imageUrl"));
                p.setNote(rs.getDouble("note"));
                p.setNbAvis(rs.getInt("nbAvis"));
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
