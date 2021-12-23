package com.cakedevs.ChildLabourBot.services.impl;

import com.cakedevs.ChildLabourBot.entities.Child;
import com.cakedevs.ChildLabourBot.repository.ChildRepository;
import com.cakedevs.ChildLabourBot.services.ChildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChildServiceImpl implements ChildService {
    @Autowired
    private ChildRepository childRepository;

    @Override
    public Child createChild(String name, int miningSpeed, int healthPoints, String user_id) {
        Child child = new Child(name, miningSpeed, healthPoints, user_id);
        child = childRepository.save(child);
        return child;
    }
}