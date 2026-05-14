package tests;

import models.Sponsor;
import services.SponsorService;

import models.*;
import services.*;

import java.sql.Date;
import java.sql.Timestamp;

public class test {
    public static void main(String[] args) {
        try {
            SponsorService service = new SponsorService();

            // Créer un sponsor
            Sponsor s1 = new Sponsor(
                    "TechCorp",
                    "Informatique",
                    50000.0f,
                    Timestamp.valueOf("2026-01-01 00:00:00"),
                    Timestamp.valueOf("2026-12-31 00:00:00")
            );

            // Utiliser add (au lieu de addPending)
            service.add(s1);
            System.out.println("Sponsor ajouté");

            // Utiliser getAll (au lieu de getApproved)
            System.out.println("Liste des sponsors :");
            for (Sponsor sp : service.getAll()) {
                System.out.println(" - " + sp.getNom());
            }

            // Utiliser getById
            if (s1.getId() != 0) {
                Sponsor found = service.getById(s1.getId());
                System.out.println("Sponsor trouvé par ID: " + found);
            }

            // Utiliser update
            s1.setNom("TechCorp Updated");
            service.update(s1);
            System.out.println("Sponsor mis à jour");

            // Utiliser delete
            if (s1.getId() != 0) {
                service.delete(s1.getId());
                System.out.println("Sponsor supprimé");
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