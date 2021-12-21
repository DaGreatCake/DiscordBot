package com.cakedevs.services;

import com.cakedevs.entities.User;
import com.cakedevs.exceptions.UserExistsException;

public interface UserService {

    User createUser(String id, String name) throws UserExistsException;
}
