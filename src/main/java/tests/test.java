package tests;

import models.Sponsor;
import services.SponsorService;

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
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}