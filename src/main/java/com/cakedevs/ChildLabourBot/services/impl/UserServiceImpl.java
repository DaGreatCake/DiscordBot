package com.cakedevs.ChildLabourBot.services.impl;

import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.exceptions.UserExistsException;
import com.cakedevs.ChildLabourBot.repository.UserRepository;
import com.cakedevs.ChildLabourBot.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public User createUser(String id, String name) throws UserExistsException {
        Optional<User> userOpt = userRepository.findUserById(id);
        if(userOpt.isPresent()) {
            throw new UserExistsException(id, name);
        }
        User user = new User(id, name);
        user = userRepository.save(user);
        return user;
    }
}
