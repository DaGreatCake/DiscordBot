package com.cakedevs.ChildLabourBot.services;

import com.cakedevs.ChildLabourBot.entities.User;
import com.cakedevs.ChildLabourBot.exceptions.UserExistsException;

public interface UserService {

    User createUser(String id, String name) throws UserExistsException;
}
