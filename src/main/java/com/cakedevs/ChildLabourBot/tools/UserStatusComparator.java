package com.cakedevs.ChildLabourBot.tools;

import com.cakedevs.ChildLabourBot.entities.User;

import java.util.Comparator;

public class UserStatusComparator implements Comparator<User> {
    public int compare(User user1, User user2) {
        return user1.getStatus() - user2.getStatus();
    }
}