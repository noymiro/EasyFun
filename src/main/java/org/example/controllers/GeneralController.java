package org.example.controllers;

import org.example.entities.Event;
import org.example.entities.User;
import org.example.responses.BasicResponse;
import org.example.utils.EmailValidator;
import org.example.utils.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.example.responses.LoginResponse;
import org.example.utils.Persist;

import java.util.List;

import static org.example.utils.Errors.*;

@RestController
public class GeneralController {


    @Autowired
    private Persist persist;


    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public BasicResponse login(String mail, String password) {
        BasicResponse basicResponse = null;
        boolean success = false;
        Integer errorCode = null;
        if (mail != null && mail.length() > 0) {
            if (password != null && password.length() > 0) {
                User user = persist.login(mail, password);
                if (user != null) {
                    basicResponse = new LoginResponse(true, errorCode, user.getId(), user.getSecret());
                } else {
                    errorCode = ERROR_LOGIN_WRONG_CREDS;
                }
            } else {
                errorCode = ERROR_SIGN_UP_NO_PASSWORD;
            }
        } else {
            errorCode = ERROR_SIGN_UP_NO_USERNAME;
        }
        if (errorCode != null) {
            basicResponse = new BasicResponse(success, errorCode);
        }
        return basicResponse;
    }

    @RequestMapping(value = "add-user")
    public boolean addUser(String username, String password, String mail) {
        if (username != null && username.length() > 0) {
            if (password != null && password.length() > 0) {
                if (mail != null && mail.length() > 0) {
                    if (EmailValidator.isValid(mail)) {
                        List<User> users = persist.getUsers();
                        for (User user : users) {
                            if (user.getMail().equals(mail)) {
                                return false;
                            }
                        }
                        if (PasswordValidator.isValid(password)) {
                            for (User user : users) {
                                if (user.getUsername().equals(username)) {
                                    return false;
                                }
                            }
                            User user = new User(username, password, mail);
                            persist.addUser(user);
                            System.out.println("User added: " + user.getUsername());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @RequestMapping(value = "/get-userByMail")
    public String getUser(String mail) {
        String secretUserByMail = persist.getSecretUserByMail(mail);
        if (
                secretUserByMail != null && secretUserByMail.length() > 0) {
            return secretUserByMail;
        }
        return null;

    }

    @RequestMapping(value = "/plan-event")
    public int planEvent(String secret, String typeEvent, String date, String location, int guests, float budget) {
        if (secret != null && secret.length() > 0) {
            if (typeEvent != null && typeEvent.length() > 0) {
                if (date != null && date.length() > 0) {
                    if (guests > 0) {
                        List<User> users = persist.getUsers();
                        for (User user : users) {
                            System.out.println(user.getSecret());
                            if (user.getSecret().equals(secret)) {
                                System.out.println("Event planned: " + typeEvent + " " + date + " " + guests + " " + location + " " + budget);
                                Event event = new Event(typeEvent, date, location, guests, budget, secret);
                                persist.addEvent(event);
                                return event.getId();
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    @RequestMapping(value = "/save-selection", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public boolean saveSelection(@RequestParam(name = "eventId", required = true) int eventId, @RequestBody List<String> elements) {
        boolean success = false;
        if (eventId > 0 && elements != null && !elements.isEmpty()) {
            Event event = persist.getEventById(eventId);
            if (event != null) {
                event.setElementsOfEvent(elements);
                persist.updateEvent(event);
                success = true;
            }
        }
        return success;
    }

    @RequestMapping(value = "/personal-area", method = RequestMethod.GET)
    public List<Object[]> personalArea(@RequestParam String secret) {
        if (secret != null && !secret.isEmpty()) {
            List<User> users = persist.getUsers();
            for (User user : users) {
                if (user.getSecret().equals(secret)) {
                    return persist.getPersonalArea(secret);
                }
            }
        }
        return null;
    }


}