package test;

import models.*;
import services.*;

import java.sql.Date;
import java.sql.Timestamp;

public class test {
    public static void main(String[] args) {
        try {
            // USER
            UserService userService = new UserService();

            User u1 = new User("Ben Ali", "Yassine", "yassine@gmail.com", "pass123", "joueur");
            userService.add(u1);

            System.out.println("Users:");
            for (User u : userService.getAll()) {
                System.out.println(u);
            }

            // TOURNOI
            TournoiService tournoiService = new TournoiService();

            Tournoi t1 = new Tournoi("Spring Cup", "League of Legends",
                    Date.valueOf("2026-06-01"),
                    Date.valueOf("2026-06-15"),
                    16,
                    "planifié");

            tournoiService.add(t1);

            System.out.println("\nTournois:");
            tournoiService.getAll().forEach(System.out::println);

            // EQUIPE
            EquipeService equipeService = new EquipeService();

            Equipe e1 = new Equipe("Team Alpha", 1, 5);
            equipeService.add(e1);

            System.out.println("\nEquipes:");
            equipeService.getAll().forEach(System.out::println);

            // SPONSOR
            SponsorService sponsorService = new SponsorService();

            Sponsor s1 = new Sponsor("TechCorp", "Informatique", 50000.0f,
                    Timestamp.valueOf("2026-01-01 00:00:00"),
                    Timestamp.valueOf("2026-12-31 00:00:00"));

            sponsorService.add(s1);

            System.out.println("\nSponsors:");
            sponsorService.getAll().forEach(System.out::println);

            //  EVENEMENT
            EvenementService evenementService = new EvenementService();

            Evenement ev1 = new Evenement("Gaming Expo", 1,
                    Date.valueOf("2026-07-10"),
                    Date.valueOf("2026-07-12"),
                    "Tunis",
                    20000.0f,
                    1);

            evenementService.add(ev1);

            System.out.println("\nEvenements:");
            evenementService.getAll().forEach(System.out::println);

            //  MATCH
            MatchService matchService = new MatchService();

            Match m1 = new Match(1, 2,
                    Date.valueOf("2026-06-05"),
                    "planifié",
                    1,
                    "Trophée",
                    Timestamp.valueOf("2026-06-05 18:00:00"));

            matchService.add(m1);

            System.out.println("\nMatchs:");
            matchService.getAll().forEach(System.out::println);

            // PRODUIT
            ProduitService produitService = new ProduitService();

            Produit p1 = new Produit("Casque Gaming Pro", "Casque haute qualité", 89.99f, 50, "Accessoires");
            produitService.add(p1);

            System.out.println("\nProduits:");
            produitService.getAll().forEach(System.out::println);

            //  COMMANDE
            CommandeService commandeService = new CommandeService();

            Commande c1 = new Commande(1,
                    Date.valueOf("2026-05-04"),
                    149.99f,
                    "en attente",
                    "Tunis");

            commandeService.add(c1);

            System.out.println("\nCommandes:");
            commandeService.getAll().forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
