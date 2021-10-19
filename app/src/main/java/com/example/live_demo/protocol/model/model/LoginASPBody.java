package com.example.live_demo.protocol.model.model;

public class LoginASPBody {
    private String Email;
    private String Password;

    public LoginASPBody(String email, String password) {
        Email = email;
        Password = password;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
