package com.esport.services;

import com.esport.models.PanierItem;
import com.esport.models.Produit;
import java.util.ArrayList;
import java.util.List;

public class PanierService {
    private final List<PanierItem> panier = new ArrayList<>();

    public void ajouterAuPanier(Produit produit, int quantite) {
        for (PanierItem item : panier) {
            if (item.getProduit().getId() == produit.getId()) {
                if (item.getQuantite() + quantite <= produit.getStock()) {
                    item.setQuantite(item.getQuantite() + quantite);
                }
                return;
            }
        }
        if (quantite <= produit.getStock()) {
            panier.add(new PanierItem(produit, quantite));
        }
    }

    public void retirerDuPanier(int produitId) {
        panier.removeIf(item -> item.getProduit().getId() == produitId);
    }

    public void modifierQuantite(int produitId, int quantite) {
        if (quantite <= 0) {
            retirerDuPanier(produitId);
            return;
        }
        for (PanierItem item : panier) {
            if (item.getProduit().getId() == produitId) {
                item.setQuantite(quantite);
                return;
            }
        }
    }

    public List<PanierItem> getPanier() {
        return new ArrayList<>(panier);
    }

    public void viderPanier() {
        panier.clear();
    }

    public double getTotal() {
        return panier.stream().mapToDouble(PanierItem::getTotal).sum();
    }

    public int getNombreItems() {
        return panier.size();
    }
}