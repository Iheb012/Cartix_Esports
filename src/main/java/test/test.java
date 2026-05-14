package test;

import models.User;
import services.UserService;

import java.sql.Date;
import java.sql.Timestamp;

public class test {
    public static void main(String[] args) {
        try {
            // USER
            UserService userService = new UserService();

            User u1 = new User("Ben Ali", "Yassine", "yassine@gmail.com", "pass123", "joueur");
            userService.add(u1);

            System.out.println("Users:");
            for (User u : userService.getAll()) {
                System.out.println(u);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
