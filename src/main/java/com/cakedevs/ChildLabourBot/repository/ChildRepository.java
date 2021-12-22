package com.cakedevs.ChildLabourBot.repository;

import com.cakedevs.ChildLabourBot.entities.Child;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ChildRepository extends CrudRepository<Child, Long> {
    Optional<Child> findChildById(long id);
    List<Child> findChildsByUserid(String userid);
}