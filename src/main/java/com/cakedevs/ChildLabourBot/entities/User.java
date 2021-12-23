package com.cakedevs.ChildLabourBot.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Comparator;

@Entity
@Table(name = "user")
public class User {
    @Id
    @Column
    private String id;
    private String name;
    private int bedrock;
    private int status;

    public User(String id, String name) {
        this.id = id;
        this.name = name;
        bedrock = 0;
        status = 0;
    }

    public User() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBedrock() {
        return bedrock;
    }

    public void setBedrock(int bedrock) {
        this.bedrock = bedrock;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
