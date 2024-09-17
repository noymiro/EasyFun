package org.example.utils;

import net.bytebuddy.implementation.bytecode.Addition;
import org.example.entities.Item;
import org.example.entities.User;
import org.example.entities.Event;
import org.example.entities.elements.Attraction;
import org.example.entities.elements.EventAddition;
import org.example.entities.elements.Food;
import org.example.entities.elements.Place;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Session;
import javax.annotation.PostConstruct;
import javax.persistence.Query;
import java.sql.*;
import java.util.List;

@Transactional
@Component
@SuppressWarnings("unchecked")
public class Persist {

    private final SessionFactory sessionFactory;
    private Connection connection;


    @Autowired
    public Persist(SessionFactory sf) {
        this.sessionFactory = sf;
    }

    @PostConstruct
    public void init() {
        createDbConnection(Constants.DB_USERNAME, Constants.DB_PASSWORD);

    }


    private void createDbConnection(String username, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/easyfun", username, password);
            System.out.println("Connection successful!");
            System.out.println();
        } catch (Exception e) {
            System.out.println("Cannot create DB connection!");
        }
    }


    public User login(String mail, String password) {
        User user = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            Query query = session.createQuery("FROM User WHERE mail = :mail AND password = :password");
            query.setParameter("mail", mail);
            query.setParameter("password", password);
            user = (User) query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public boolean checkIfUsernameAvailable(String username) {
        boolean available = false;
        try {
            Session session = sessionFactory.getCurrentSession();
            Query query = session.createQuery("FROM User WHERE username = :username");
            query.setParameter("username", username);
            if (query.getResultList().isEmpty()) {
                available = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return available;
    }

    public boolean addUser(User user) {
        boolean success = false;
        try {
            Session session = sessionFactory.getCurrentSession();
            session.save(user);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public List<User> getUsers() {
        List<User> users = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            users = session.createQuery("FROM User").getResultList();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return users;
    }

    public String getSecretUserByMail(String mail) {
        String secret = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            Query query = session.createQuery("FROM User WHERE mail = :mail");
            query.setParameter("mail", mail);
            User user = (User) query.getSingleResult();
            secret = user.getSecret();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return secret;

    }

    public boolean addEvent(Event event) {
        boolean success = false;
        try {
            Session session = sessionFactory.getCurrentSession();
            session.save(event);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public List<Event> getEvents() {
        List<Event> events = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            events = session.createQuery("FROM Event").getResultList();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return events;
    }

    public Event getEventBySecret(String secret) {
        Event event = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            Query query = session.createQuery("FROM Event WHERE secretOfUser = :secret");
            query.setParameter("secret", secret);
            event = (Event) query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    public Event getEventById(int id) {
        Event event = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            Query query = session.createQuery("FROM Event WHERE id = :id");
            query.setParameter("id", id);
            event = (Event) query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    public void updateEvent(Event event) {
        try {
            Session session = sessionFactory.getCurrentSession();
            session.update(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Event isSimilarEventExists(String eventType, String location, Integer guests, Float budget) {
        Event event = null;
        try {
            Session session = sessionFactory.getCurrentSession();

            // Calculate the budget range with a 10% tolerance
            float lowerBudgetBound = budget * 0.9f;
            float upperBudgetBound = budget * 1.1f;

            // Calculate the guests range with a 10% tolerance
            int lowerGuestsBound = Math.round(guests * 0.9f);
            int upperGuestsBound = Math.round(guests * 1.1f);

            // Query to check if an event with the same type, location, guests, and budget exists
            String hql = "FROM Event WHERE typeEvent = :eventType AND location = :location AND guests BETWEEN :lowerGuestsBound AND :upperGuestsBound AND budget BETWEEN :lowerBudgetBound AND :upperBudgetBound";
            Query query = session.createQuery(hql);
            query.setParameter("eventType", eventType);
            query.setParameter("location", location);
            query.setParameter("lowerGuestsBound", lowerGuestsBound);
            query.setParameter("upperGuestsBound", upperGuestsBound);
            query.setParameter("lowerBudgetBound", lowerBudgetBound);
            query.setParameter("upperBudgetBound", upperBudgetBound);
            List<Event> results = query.getResultList();

            if (!results.isEmpty()) {
                event = results.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    public List<Object[]> getPersonalArea(String secret) {
        List<Object[]> results = null;
        try {
            Session session = sessionFactory.getCurrentSession();

            // JOIN query to retrieve user, events, and elements based on user secret
            String hql = "SELECT u, e, el FROM User u " +
                    "JOIN Event e ON u.secret = e.secretOfUser " +
                    "JOIN e.elementsOfEvent el " +
                    "WHERE u.secret = :secret";
            Query query = session.createQuery(hql);
            query.setParameter("secret", secret);
            results = query.getResultList();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }




    public void addItem(Object item) {
        try {
            Session session = sessionFactory.getCurrentSession();
            session.save(item);
            if (item instanceof Food) {
                Food food = (Food) item;
                session.save(food);
            } else if (item instanceof Attraction) {
                Attraction attraction = (Attraction) item;
                session.save(attraction);
            } else if (item instanceof EventAddition) {
                EventAddition eventAddition = (EventAddition) item;
                session.save(eventAddition);
            } else if (item instanceof Place) {
                Place place = (Place) item;
                session.save(place);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}