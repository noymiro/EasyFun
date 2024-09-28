// src/main/java/org/example/utils/PersistService.java
package org.example.utils;

import org.example.entities.Item;
import org.example.entities.User;
import org.example.entities.Event;

import java.util.List;

public interface PersistService {
    User login(String mail, String password);
    boolean checkIfUsernameAvailable(String username);
    boolean addUser(User user);
    List<User> getUsers();
    String getSecretUserByMail(String mail);
    boolean addEvent(Event event);
    List<Event> getEvents();
    Event getEventBySecret(String secret);
    Event getEventById(int id);
    void updateEvent(Event event);
    Event isSimilarEventExists(String eventType, String location, Integer guests, Float budget);
    List<Object[]> getPersonalArea(String secret);
    void addItem(Object item);
    List<Item> getItems();
    void deleteItem(int id);
    List<Item> getItemsByCategory(String category);
    List<Item> getItemsByCategoryAndBudget(String category, float remainingBudget,String location);
}