package com.scheduler.service;

import com.scheduler.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    /**
     * Create List of Users
     * @return
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(User.builder().name("Sunny").occupation("Cricketer").salary(99827).mobileNo(983426449).location("UP").build());
        users.add(User.builder().name("Rahul").occupation("Pilot").salary(45784).mobileNo(983547449).location("Bihar").build());
        users.add(User.builder().name("Aman").occupation("Manager").salary(45794).mobileNo(743426449).location("Delhi").build());
        users.add(User.builder().name("Nikhil").occupation("Youtuber").salary(534738).mobileNo(87626449).location("Gujarat").build());
        return users;
    }
}
