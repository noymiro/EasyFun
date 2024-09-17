package org.example.entities;

public class Item {
    private String name;
    private String category;
    private float price;
    private String description;
    private int id;

    public Item() {
    }

    public Item(String name, String category, float price, String description, int id) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}