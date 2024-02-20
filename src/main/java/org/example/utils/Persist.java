package org.example.utils;
import org.example.entities.User;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Session;
import javax.annotation.PostConstruct;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Transactional
@Component
@SuppressWarnings("unchecked")
public class Persist {

    private final SessionFactory sessionFactory;
    private Connection connection ;


    @Autowired
    public Persist(SessionFactory sf) {
        this.sessionFactory = sf;
    }

    @PostConstruct
    public void init () {
       createDbConnection(Constants.DB_USERNAME, Constants.DB_PASSWORD);

    }


    private void createDbConnection(String username, String password){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/easyFun", username, password);
            System.out.println("Connection successful!");
            System.out.println();
        }catch (Exception e){
            System.out.println("Cannot create DB connection!");
        }
    }


    public User login (String username, String password) {
        User user = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id, secret FROM users WHERE username = ? AND password = ? ");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String secret = resultSet.getString("secret");
                user = new User();
                user.setId(id);
                user.setSecret(secret);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;

    }

    public boolean checkIfUsernameAvailable (String username) {
        boolean available = false;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT username FROM users WHERE username = ?");
            preparedStatement.setString(1,username);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                available = true;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return available;
    }

    public boolean addUser (User user) {
        boolean success = false;
        try {
            if (checkIfUsernameAvailable(user.getUsername())) {
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users (username, password) VALUES ( ? , ? )");
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getPassword());
                preparedStatement.executeUpdate();
                success = true;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return success;
    }


}
