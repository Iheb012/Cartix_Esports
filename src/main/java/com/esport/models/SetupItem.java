package com.esport.models;

public class SetupItem {
    private final String label;
    private final String icon;
    private final String category;
    private final Produit product;
    private final double price;
    private final boolean estimated;

    public SetupItem(String label, String icon, String category, Produit product, double price, boolean estimated) {
        this.label = label;
        this.icon = icon;
        this.category = category;
        this.product = product;
        this.price = price;
        this.estimated = estimated;
    }

    public String getLabel() { return label; }
    public String getIcon() { return icon; }
    public String getCategory() { return category; }
    public Produit getProduct() { return product; }
    public double getPrice() { return price; }
    public boolean isEstimated() { return estimated; }
}
