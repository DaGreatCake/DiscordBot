package com.cakedevs.ChildLabourBot.repository;

import com.cakedevs.ChildLabourBot.entities.Cooldown;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CooldownRepository extends CrudRepository<Cooldown, String> {
    Optional<Cooldown> findCooldownByUserid(String userid);

}
