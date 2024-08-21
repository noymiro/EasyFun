package org.example.entities;

public class Event {
    private String date;
    private String typeEvent;
    private int id;
    private String location;
    private float budget;
    private int guests;
    private String secretOfUser;


    public Event (){

    }

    public Event(String typeEvent, String date,String location, int guests) {
        this.typeEvent = typeEvent;
        this.date = date;
        this.location = location;
        this.guests = guests;

    }

    public Event(String typeEvent, String date,String location, int guests, float budget, String secretOfUser) {
        this.typeEvent = typeEvent;
        this.date = date;
        this.location = location;
        this.guests = guests;
        this.budget = budget;
        this.secretOfUser = secretOfUser;

    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTypeEvent() {
        return typeEvent;
    }

    public void setTypeEvent(String description) {
        this.typeEvent = description;
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


    public float getBudget() {
        return budget;
    }

    public void setBudget(float budget) {
        this.budget = budget;
    }


    public int getGuests() {
        return guests;
    }

    public void setGuests(int guests) {
        this.guests = guests;
    }

    public String getSecretOfUser() {
        return secretOfUser;
    }

    public void setSecretOfUser(String secretOfUser) {
        this.secretOfUser = secretOfUser;
    }
}