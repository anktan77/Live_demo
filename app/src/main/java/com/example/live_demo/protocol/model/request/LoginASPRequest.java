package com.example.live_demo.protocol.model.request;

public class LoginASPRequest extends Request {

    public String IdPerson ;
    public String Email ;
    public String Name ;
    public String ImageView ;
    public String Phone ;
    public int Coins ;

    public LoginASPRequest(LoginASPRequest loginASPRequest) {
    }

    public LoginASPRequest(String idPerson, String email, String name, String imageView, String phone, int coins) {
        IdPerson = idPerson;
        Email = email;
        Name = name;
        ImageView = imageView;
        Phone = phone;
        Coins = coins;
    }
}
