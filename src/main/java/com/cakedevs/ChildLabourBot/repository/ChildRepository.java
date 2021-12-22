package com.cakedevs.ChildLabourBot.repository;

import com.cakedevs.ChildLabourBot.entities.Child;
import com.cakedevs.ChildLabourBot.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ChildRepository extends CrudRepository<Child, String> {
    Optional<Child> findChildById(int id);
}
