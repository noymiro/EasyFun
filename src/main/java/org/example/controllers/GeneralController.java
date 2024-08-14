package org.example.controllers;
import org.example.entities.Event;
import org.example.entities.User;
import org.example.responses.BasicResponse;
import org.example.utils.EmailValidator;
import org.example.utils.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.example.responses.LoginResponse;
import org.example.utils.Persist;
import java.util.List;
import static org.example.utils.Errors.*;

@RestController
public class GeneralController {


    @Autowired
    private Persist persist;




    @RequestMapping (value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public BasicResponse login (String mail, String password) {
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

    @RequestMapping (value = "add-user")
    public boolean addUser (String username, String password , String mail) {
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

    @RequestMapping (value = "/get-userByMail")
    public User getUser (String mail) {
        User user = persist.getUserByMail(mail);
        if (user != null) {
            return user;
        }
        return null;

    }

    @RequestMapping (value = "/plan-event")
    public boolean planEvent (String secret, String typeEvent,String date,String location ,int guests) {
        if (secret != null && secret.length() > 0) {
            if (typeEvent != null && typeEvent.length() > 0) {
                if (date != null && date.length() > 0) {
                    if (guests > 0) {
                        List<User> users = persist.getUsers();
                        for (User user : users) {
                            if (user.getSecret().equals(secret)) {
                                System.out.println("Event planned: " + typeEvent + " " + date + " " + guests);
                                Event event = new Event(typeEvent, date,location, guests);
                                persist.addEvent(event);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

}