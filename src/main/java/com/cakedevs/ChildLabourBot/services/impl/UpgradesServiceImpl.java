package com.cakedevs.ChildLabourBot.services.impl;

import com.cakedevs.ChildLabourBot.entities.Upgrades;
import com.cakedevs.ChildLabourBot.repository.UpgradesRepository;
import com.cakedevs.ChildLabourBot.services.UpgradesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpgradesServiceImpl implements UpgradesService {
    @Autowired
    private UpgradesRepository upgradesRepository;

    @Override
    public Upgrades createUpgrades(String userid) {
        Upgrades upgrades = new Upgrades(userid);
        upgrades = upgradesRepository.save(upgrades);
        return upgrades;
    }
}
