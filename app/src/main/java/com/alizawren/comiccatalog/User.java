package com.alizawren.comiccatalog;

public class User {

    private String userID;
    private String displayName;
    private String email;

    public User(String id, String displayName, String email) {
        this.userID = id;
        this.displayName = displayName;
        this.email = email;
    }

    public User() {
        this.userID = null;
        this.displayName = "???";
        this.email = "???";
    }

    public String getUserID() {
        return this.userID;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getEmail() {
        return this.email;
    }
}