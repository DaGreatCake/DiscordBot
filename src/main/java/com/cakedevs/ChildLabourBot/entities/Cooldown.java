package com.cakedevs.ChildLabourBot.entities;

import javax.persistence.*;

@Entity
@Table(name = "cooldowns")
public class Cooldown {
    @Id
    private String userid;
    private long arbeitencooldown;
    private long healcooldown;
    private long kidnapcooldown;
    private long mergecooldown;
    private long neuksekscooldown;
    private long murdercooldown;

    public Cooldown(String userid) {
        this.userid = userid;
        this.arbeitencooldown = 0;
        this.healcooldown = 0;
        this.kidnapcooldown = 0;
        this.mergecooldown = 0;
        this.neuksekscooldown = 0;
    }

    public Cooldown() {}

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public long getArbeitencooldown() {
        return arbeitencooldown;
    }

    public void setArbeitencooldown(long arbeitencooldown) {
        this.arbeitencooldown = arbeitencooldown;
    }

    public long getHealcooldown() {
        return healcooldown;
    }

    public void setHealcooldown(long healcooldown) {
        this.healcooldown = healcooldown;
    }

    public long getKidnapcooldown() {
        return kidnapcooldown;
    }

    public void setKidnapcooldown(long kidnapcooldown) {
        this.kidnapcooldown = kidnapcooldown;
    }

    public long getMergecooldown() {
        return mergecooldown;
    }

    public void setMergecooldown(long mergecooldown) {
        this.mergecooldown = mergecooldown;
    }

    public long getNeuksekscooldown() {
        return neuksekscooldown;
    }

    public void setNeuksekscooldown(long neuksekscooldown) {
        this.neuksekscooldown = neuksekscooldown;
    }

    public long getMurdercooldown() {
        return murdercooldown;
    }

    public void setMurdercooldown(long murdercooldown) {
        this.murdercooldown = murdercooldown;
    }
}
