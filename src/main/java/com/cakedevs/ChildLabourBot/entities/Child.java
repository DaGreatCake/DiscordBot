package com.cakedevs.ChildLabourBot.entities;

import javax.persistence.*;

@Entity
@Table(name = "child")
public class Child {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private int miningspeed;
    private int healthpoints;
    private String userid;

    public Child(String name, int miningspeed, int healthpoints, String userid) {
        this.name = name;
        this.miningspeed = miningspeed;
        this.healthpoints = healthpoints;
        this.userid = userid;
    }

    public Child() {}

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMiningspeed() {
        return miningspeed;
    }

    public void setMiningspeed(int miningspeed) {
        this.miningspeed = miningspeed;
    }

    public int getHealthpoints() {
        return healthpoints;
    }

    public void setHealthpoints(int healthpoints) {
        this.healthpoints = healthpoints;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
