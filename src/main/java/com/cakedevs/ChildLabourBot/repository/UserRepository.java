package com.cakedevs.ChildLabourBot.repository;

import com.cakedevs.ChildLabourBot.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    Optional<User> findUserById(String id);
}
