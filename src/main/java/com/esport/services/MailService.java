package com.esport.services;

public class MailService {
    public boolean envoyerEmail(String destinataire, String sujet, String contenuHtml) {
        System.out.println("📧 Email envoyé à: " + destinataire);
        System.out.println("   Sujet: " + sujet);
        return true;
    }
}