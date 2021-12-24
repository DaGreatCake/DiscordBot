package com.cakedevs.ChildLabourBot.services.impl;

import com.cakedevs.ChildLabourBot.entities.Cooldown;
import com.cakedevs.ChildLabourBot.repository.CooldownRepository;
import com.cakedevs.ChildLabourBot.services.CooldownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CooldownServiceImpl implements CooldownService {
    @Autowired
    private CooldownRepository cooldownRepository;

    @Override
    public Cooldown createCooldown(String userid) {
        Cooldown cooldown = new Cooldown(userid);
        cooldown = cooldownRepository.save(cooldown);
        return cooldown;
    }
}
