package org.example.controllers;
import org.example.entities.User;
import org.example.responses.BasicResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.example.responses.LoginResponse;
import org.example.utils.Persist;

import javax.annotation.PostConstruct;
import java.util.List;

import org.example.utils.Errors;
import static org.example.utils.Errors.*;

@RestController
public class GeneralController {


    @Autowired
    private Persist persist;




    @RequestMapping (value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public BasicResponse login (String username, String password) {
        BasicResponse basicResponse = null;
        boolean success = false;
        Integer errorCode = null;
        if (username != null && username.length() > 0) {
            if (password != null && password.length() > 0) {
                User user = persist.login(username, password);
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
    public boolean addUser (String username, String password) {
        User userToAdd = new User(username, password);
        return persist.addUser(userToAdd);
    }

}
