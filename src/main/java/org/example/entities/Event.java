package org.example.entities;
import org.example.entities.User;

public class Event {
    private int amountInvited;
    private String date;
    private String description;
    private int id;
    private String location;
    private String name;
    private float budget;
    private User owner;

    public Event(int amountInvited, String date, String description, int id, String location, String name, float budget, User owner) {
        this.amountInvited = amountInvited;
        this.date = date;
        this.description = description;
        this.id = id;
        this.location = location;
        this.name = name;
        this.budget = budget;
        this.owner = owner;
    }

    public Event(String name, int amountInvited, String date, String location, float budget, User owner) {
        this.amountInvited = amountInvited;
        this.date = date;
        this.location = location;
        this.name = name;
        this.budget = budget;
        this.owner = owner;
    }

    public Event(User owner) {
        this.owner = owner;
    }

    public Event (){

    }

    public int getAmountInvited() {
        return amountInvited;
    }

    public void setAmountInvited(int amountInvited) {
        this.amountInvited = amountInvited;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getBudget() {
        return budget;
    }

    public void setBudget(float budget) {
        this.budget = budget;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

}