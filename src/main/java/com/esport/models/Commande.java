package com.esport.models;

import javafx.beans.property.*;
import java.util.Date;
import java.util.List;

public class Commande {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty clientNom = new SimpleStringProperty();
    private final StringProperty clientEmail = new SimpleStringProperty();
    private final ObjectProperty<Date> dateCommande = new SimpleObjectProperty<>();
    private final DoubleProperty total = new SimpleDoubleProperty();
    private final StringProperty statutLivraison = new SimpleStringProperty();
    private List<PanierItem> items;

    public Commande() {}

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getClientNom() { return clientNom.get(); }
    public void setClientNom(String nom) { this.clientNom.set(nom); }
    public StringProperty clientNomProperty() { return clientNom; }

    public String getClientEmail() { return clientEmail.get(); }
    public void setClientEmail(String email) { this.clientEmail.set(email); }
    public StringProperty clientEmailProperty() { return clientEmail; }

    public Date getDateCommande() { return dateCommande.get(); }
    public void setDateCommande(Date date) { this.dateCommande.set(date); }
    public ObjectProperty<Date> dateCommandeProperty() { return dateCommande; }

    public double getTotal() { return total.get(); }
    public void setTotal(double total) { this.total.set(total); }
    public DoubleProperty totalProperty() { return total; }

    public String getStatutLivraison() { return statutLivraison.get(); }
    public void setStatutLivraison(String statut) { this.statutLivraison.set(statut); }
    public StringProperty statutLivraisonProperty() { return statutLivraison; }

    public List<PanierItem> getItems() { return items; }
    public void setItems(List<PanierItem> items) { this.items = items; }
}