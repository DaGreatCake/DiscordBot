package com.cakedevs.ChildLabourBot.entities;

import javax.persistence.*;

@Entity
@Table(name = "upgrades")
public class Upgrades {
    @Id
    private String userid;
    private int speedupgrade;
    private int maxchildsupgrade;

    public Upgrades(String userid) {
        this.userid = userid;
        this.speedupgrade = 1;
        this.maxchildsupgrade = 1;
    }

    public Upgrades() {}

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public int getSpeedupgrade() {
        return speedupgrade;
    }

    public void setSpeedupgrade(int speedupgrade) {
        this.speedupgrade = speedupgrade;
    }

    public int getMaxchildsupgrade() {
        return maxchildsupgrade;
    }

    public void setMaxchildsupgrade(int maxchildsupgrade) {
        this.maxchildsupgrade = maxchildsupgrade;
    }
}
