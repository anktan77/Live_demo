package com.example.live_demo.protocol.model.response;

import java.util.Date;

public class LoginASPResponse extends Response {
    public  int IDcc;
    public String IdPerson ;
    public String Email ;
    public String Name ;
    public String ImageView ;
    public String Phone ;
    public int Coins ;

    public LoginASPResponse(int IDcc, String idPerson, String email, String name, String imageView, String phone, int coins) {
        this.IDcc = IDcc;
        IdPerson = idPerson;
        Email = email;
        Name = name;
        ImageView = imageView;
        Phone = phone;
        Coins = coins;
    }

    public int getIDcc() {
        return IDcc;
    }

    public void setIDcc(int IDcc) {
        this.IDcc = IDcc;
    }

    public String getIdPerson() {
        return IdPerson;
    }

    public void setIdPerson(String idPerson) {
        IdPerson = idPerson;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImageView() {
        return ImageView;
    }

    public void setImageView(String imageView) {
        ImageView = imageView;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public int getCoins() {
        return Coins;
    }

    public void setCoins(int coins) {
        Coins = coins;
    }
}
