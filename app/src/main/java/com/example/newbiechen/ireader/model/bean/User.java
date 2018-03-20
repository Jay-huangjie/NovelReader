package com.example.newbiechen.ireader.model.bean;

/**
 * Created by zohar on 2018/3/20.
 * desc:
 */

public class User {

    private String username ;

    private String password;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
