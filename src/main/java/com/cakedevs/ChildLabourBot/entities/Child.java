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
    private int miningSpeed;
    private int healthPoints;
    private String user_id;

    public Child(String name) {
        this.name = name;

    }

    public Child() {}

}
