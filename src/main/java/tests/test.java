package tests;

import models.*;
import services.*;

import java.sql.Date;
import java.sql.Timestamp;

public class test {
    public static void main(String[] args) {
        try {
            // ── EQUIPE ────────────────────────────────────────────────────
            EquipeService equipeService = new EquipeService();

            Equipe e1 = new Equipe(0, "Team Alpha", 1, 5);
            equipeService.add(e1);

            Equipe e2 = new Equipe(0, "Team Beta", 2, 5);
            equipeService.add(e2);

            System.out.println("Equipes:");
            equipeService.getAll().forEach(System.out::println);

            // ── MATCH ─────────────────────────────────────────────────────
            MatchService matchService = new MatchService();

            // Grab the real IDs that were just inserted
            int id1 = equipeService.getAll().get(0).getId();
            int id2 = equipeService.getAll().get(1).getId();

            Match m1 = new Match(id1, id2,
                    Date.valueOf("2026-06-05"),
                    "planifie",
                    1,
                    "Trophée",
                    (Timestamp) null);
            m1.setJeu("Valorant");
            matchService.add(m1);

            Match m2 = new Match(id1, id2,
                    Date.valueOf("2026-06-10"),
                    "termine",
                    1,
                    "500 DT",
                    (Timestamp) null);
            m2.setJeu("CS2");
            matchService.add(m2);

            System.out.println("\nMatchs:");
            matchService.getAll().forEach(System.out::println);

            // ── RESULTAT MATCH ────────────────────────────────────────────
            ResultatMatchService resultatService = new ResultatMatchService();

            int matchId = matchService.getAll().stream()
                    .filter(m -> "termine".equalsIgnoreCase(m.getStatut()))
                    .findFirst()
                    .map(Match::getId)
                    .orElse(-1);

            if (matchId != -1) {
                ResultatMatch r1 = new ResultatMatch(matchId, id1, 2, 1);
                resultatService.add(r1);
                System.out.println("\nResultats:");
                resultatService.getAll().forEach(System.out::println);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}