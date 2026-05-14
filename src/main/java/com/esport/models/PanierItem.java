package com.esport.models;

import javafx.beans.property.*;

public class PanierItem {
    private final ObjectProperty<Produit> produit = new SimpleObjectProperty<>();
    private final IntegerProperty quantite = new SimpleIntegerProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();

    public PanierItem() {}

    public PanierItem(Produit produit, int quantite) {
        setProduit(produit);
        setQuantite(quantite);
        updateTotal();
    }

    public Produit getProduit() { return produit.get(); }
    public void setProduit(Produit produit) {
        this.produit.set(produit);
        updateTotal();
    }
    public ObjectProperty<Produit> produitProperty() { return produit; }

    public int getQuantite() { return quantite.get(); }
    public void setQuantite(int quantite) {
        this.quantite.set(quantite);
        updateTotal();
    }
    public IntegerProperty quantiteProperty() { return quantite; }

    public double getTotal() { return total.get(); }
    public DoubleProperty totalProperty() { return total; }

    private void updateTotal() {
        Produit p = getProduit();
        total.set(p != null ? p.getPrix() * getQuantite() : 0);
    }
}