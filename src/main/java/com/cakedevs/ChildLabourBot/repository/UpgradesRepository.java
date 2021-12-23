package com.cakedevs.ChildLabourBot.repository;

import com.cakedevs.ChildLabourBot.entities.Upgrades;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UpgradesRepository extends CrudRepository<Upgrades, String> {
    Optional<Upgrades> findUpgradesByUserid(String userid);
}
