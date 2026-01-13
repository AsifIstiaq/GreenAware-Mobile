package com.example.greenaware_mobile.Session;

public class UserSession {

    private static UserSession instance;

    private String userId;
    private String name;
    private String email;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public void setUser(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public void clearSession() {
        this.userId = null;
        this.name = null;
        this.email = null;
    }
}
