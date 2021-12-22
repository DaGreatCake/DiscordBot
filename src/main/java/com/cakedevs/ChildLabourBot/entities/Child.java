package com.cakedevs.ChildLabourBot.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "child")
public class Child {
    @Id
    @Column
    private int id;
    private String name;
    private int mining_speed;
    private int healthpoints;
    private String user_id;

    public Child(String name, int mining_speed, int healthpoints, String user_id) {
        this.name = name;
        this.mining_speed = mining_speed;
        this.healthpoints = healthpoints;
        this.user_id = user_id;
    }

    public Child() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMining_speed() {
        return mining_speed;
    }

    public void setMining_speed(int mining_speed) {
        this.mining_speed = mining_speed;
    }

    public int getHealthpoints() {
        return healthpoints;
    }

    public void setHealthpoints(int healthpoints) {
        this.healthpoints = healthpoints;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
