package com.esport.services;

import com.esport.models.Commande;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

public class PDFService {

    private String escapePdf(String text) {
        return text.replace("\\", "\\\\")
                   .replace("(", "\\(")
                   .replace(")", "\\)");
    }

    public String genererFacture(Commande commande, String cheminFichier) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String dateStr = commande.getDateCommande() != null ? sdf.format(commande.getDateCommande()) : "N/A";

        String[] lines = {
            "FACTURE N\u00B0 " + commande.getId(),
            "========================================",
            "Date:   " + dateStr,
            "Client: " + commande.getClientNom(),
            "Email:  " + commande.getClientEmail(),
            "Statut: " + commande.getStatutLivraison(),
            "----------------------------------------",
            "Total:  " + String.format("%.2f TND", commande.getTotal()),
            "========================================",
            "",
            "Merci pour votre achat !",
            "Cartix - Esport Shop"
        };

        StringBuilder content = new StringBuilder();
        content.append("BT\n/F1 12 Tf\n");

        int x = 72;
        int y = 780;
        for (String line : lines) {
            content.append("1 0 0 1 ").append(x).append(" ").append(y).append(" Tm\n");
            content.append("(").append(escapePdf(line)).append(") Tj\n");
            y -= line.isEmpty() ? 12 : 18;
        }
        content.append("ET\n");

        byte[] contentBytes = content.toString().getBytes(StandardCharsets.ISO_8859_1);
        int contentLen = contentBytes.length;

        try (FileOutputStream fos = new FileOutputStream(cheminFichier)) {
            StringBuilder pdf = new StringBuilder();
            pdf.append("%PDF-1.4\n");

            int o1 = pdf.length();
            pdf.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
            int o2 = pdf.length();
            pdf.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
            int o3 = pdf.length();
            pdf.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842]\n/Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n");
            int o4 = pdf.length();
            pdf.append("4 0 obj\n<< /Length ").append(contentLen).append(" >>\nstream\n");
            pdf.append(new String(contentBytes, StandardCharsets.ISO_8859_1));
            pdf.append("\nendstream\nendobj\n");
            int o5 = pdf.length();
            pdf.append("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n");

            int xrefOffset = pdf.length();
            pdf.append("xref\n0 6\n");
            pdf.append("0000000000 65535 f \n");
            pdf.append(String.format("%010d 00000 n \n", o1));
            pdf.append(String.format("%010d 00000 n \n", o2));
            pdf.append(String.format("%010d 00000 n \n", o3));
            pdf.append(String.format("%010d 00000 n \n", o4));
            pdf.append(String.format("%010d 00000 n \n", o5));

            pdf.append("trailer\n<< /Size 6 /Root 1 0 R >>\n");
            pdf.append("startxref\n").append(xrefOffset).append("\n");
            pdf.append("%%EOF");

            fos.write(pdf.toString().getBytes(StandardCharsets.ISO_8859_1));
            fos.flush();

            System.out.println("PDF generée: " + cheminFichier);
        } catch (IOException e) {
            System.err.println("Erreur PDF: " + e.getMessage());
        }

        return cheminFichier;
    }
}
