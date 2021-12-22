package com.cakedevs.ChildLabourBot.services;

import com.cakedevs.ChildLabourBot.entities.Child;

public interface ChildService {
    Child createChild(String name, int miningSpeed, int healthPoints, String user_id);
}
