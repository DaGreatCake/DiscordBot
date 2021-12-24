package com.cakedevs.ChildLabourBot.services;

import com.cakedevs.ChildLabourBot.entities.Cooldown;

public interface CooldownService {
    Cooldown createCooldown(String userid);
}
