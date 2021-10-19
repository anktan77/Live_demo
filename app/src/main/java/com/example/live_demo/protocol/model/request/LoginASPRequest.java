package com.example.live_demo.protocol.model.request;

public class LoginASPRequest extends Request {
    public String Email;
    public String Password;

    public LoginASPRequest(String email, String password) {
        this.Email = email;
        this.Password = password;
    }
}
