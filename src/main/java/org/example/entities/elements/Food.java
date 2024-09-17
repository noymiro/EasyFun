package org.example.entities.elements;

import org.example.entities.Item;

public class Food extends Item {
    private String ingredients;
    private String image;

    public Food() {
    }



    public Food(String name, String category, float price, String description,int id) {
        super(name, category, price, description,id);

    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
