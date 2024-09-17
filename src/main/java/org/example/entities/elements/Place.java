package org.example.entities.elements;

import org.example.entities.Item;

public class Place  extends Item {
    private int id;
    private String address;
    private String phoneNumber;
    private String website;
    private String email;
    private String openingHours;
    private String closingHours;
    private String image;

    public Place() {
    }

    public Place(String name, String category, float price, String description, String address, String phoneNumber, String website, String email, String openingHours, String closingHours, String image, int id) {
        super(name, category, price, description, id);
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.website = website;
        this.email = email;
        this.openingHours = openingHours;
        this.closingHours = closingHours;
        this.image = image;

    }

    public Place(String name, String category, float price, String description,int id) {
        super(name, category, price, description,id);

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getClosingHours() {
        return closingHours;
    }

    public void setClosingHours(String closingHours) {
        this.closingHours = closingHours;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}