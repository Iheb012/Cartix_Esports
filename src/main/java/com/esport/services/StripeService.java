package com.esport.services;

public class StripeService {
    public String creerPaiement(double montant, String devise, String description) {
        System.out.println("💳 Paiement: " + montant + " " + devise);
        return "payment_" + System.currentTimeMillis();
    }
}