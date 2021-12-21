package com.cakedevs.repository;

import com.cakedevs.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, String> {
    Optional<User> findUserById(String id);
}
