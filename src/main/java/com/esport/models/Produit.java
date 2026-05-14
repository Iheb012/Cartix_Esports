package com.esport.models;

import javafx.beans.property.*;

public class Produit {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final DoubleProperty prix = new SimpleDoubleProperty();
    private final IntegerProperty stock = new SimpleIntegerProperty();
    private final StringProperty categorie = new SimpleStringProperty();
    private final StringProperty imageUrl = new SimpleStringProperty();
    private final DoubleProperty note = new SimpleDoubleProperty();
    private final IntegerProperty nbAvis = new SimpleIntegerProperty();

    public Produit() {}

    public Produit(int id, String nom, double prix, int stock, String categorie, String imageUrl) {
        setId(id);
        setNom(nom);
        setPrix(prix);
        setStock(stock);
        setCategorie(categorie);
        setImageUrl(imageUrl);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }
    public StringProperty nomProperty() { return nom; }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }

    public double getPrix() { return prix.get(); }
    public void setPrix(double prix) { this.prix.set(prix); }
    public DoubleProperty prixProperty() { return prix; }

    public int getStock() { return stock.get(); }
    public void setStock(int stock) { this.stock.set(stock); }
    public IntegerProperty stockProperty() { return stock; }

    public String getCategorie() { return categorie.get(); }
    public void setCategorie(String categorie) { this.categorie.set(categorie); }
    public StringProperty categorieProperty() { return categorie; }

    public String getImageUrl() { return imageUrl.get(); }
    public void setImageUrl(String imageUrl) { this.imageUrl.set(imageUrl); }
    public StringProperty imageUrlProperty() { return imageUrl; }

    public double getNote() { return note.get(); }
    public void setNote(double note) { this.note.set(note); }
    public DoubleProperty noteProperty() { return note; }

    public int getNbAvis() { return nbAvis.get(); }
    public void setNbAvis(int nbAvis) { this.nbAvis.set(nbAvis); }
    public IntegerProperty nbAvisProperty() { return nbAvis; }
}