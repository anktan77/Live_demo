package com.example.live_demo.protocol.model.response;

import java.util.Date;

public class LoginASPResponse extends Response {
    private String Id;
    private String Email;
    private int EmailConfirmed;
    private String PasswordHash;
    private String SecurityStamp;
    private String PhoneNumber;
    private int PhoneNumberConfirmed;
    private String TwoFactorEnabled;
    private Date LockoutEndDateUtc;
    private int LockoutEnabled;
    private int AccessFailedCount;
    private String UserName;
    private String Name;
    private int Coins;
    private String[] Claims;
    private String[] Roles;
    private String[] Logins;

    public LoginASPResponse(String id, String email, int emailConfirmed, String passwordHash, String securityStamp, String phoneNumber, int phoneNumberConfirmed, String twoFactorEnabled, Date lockoutEndDateUtc, int lockoutEnabled, int accessFailedCount, String userName, String name, int coins, String[] claims, String[] roles, String[] logins) {
        Id = id;
        Email = email;
        EmailConfirmed = emailConfirmed;
        PasswordHash = passwordHash;
        SecurityStamp = securityStamp;
        PhoneNumber = phoneNumber;
        PhoneNumberConfirmed = phoneNumberConfirmed;
        TwoFactorEnabled = twoFactorEnabled;
        LockoutEndDateUtc = lockoutEndDateUtc;
        LockoutEnabled = lockoutEnabled;
        AccessFailedCount = accessFailedCount;
        UserName = userName;
        Name = name;
        Coins = coins;
        Claims = claims;
        Roles = roles;
        Logins = logins;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public int getEmailConfirmed() {
        return EmailConfirmed;
    }

    public void setEmailConfirmed(int emailConfirmed) {
        EmailConfirmed = emailConfirmed;
    }

    public String getPasswordHash() {
        return PasswordHash;
    }

    public void setPasswordHash(String passwordHash) {
        PasswordHash = passwordHash;
    }

    public String getSecurityStamp() {
        return SecurityStamp;
    }

    public void setSecurityStamp(String securityStamp) {
        SecurityStamp = securityStamp;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public int getPhoneNumberConfirmed() {
        return PhoneNumberConfirmed;
    }

    public void setPhoneNumberConfirmed(int phoneNumberConfirmed) {
        PhoneNumberConfirmed = phoneNumberConfirmed;
    }

    public String getTwoFactorEnabled() {
        return TwoFactorEnabled;
    }

    public void setTwoFactorEnabled(String twoFactorEnabled) {
        TwoFactorEnabled = twoFactorEnabled;
    }

    public Date getLockoutEndDateUtc() {
        return LockoutEndDateUtc;
    }

    public void setLockoutEndDateUtc(Date lockoutEndDateUtc) {
        LockoutEndDateUtc = lockoutEndDateUtc;
    }

    public int getLockoutEnabled() {
        return LockoutEnabled;
    }

    public void setLockoutEnabled(int lockoutEnabled) {
        LockoutEnabled = lockoutEnabled;
    }

    public int getAccessFailedCount() {
        return AccessFailedCount;
    }

    public void setAccessFailedCount(int accessFailedCount) {
        AccessFailedCount = accessFailedCount;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getCoins() {
        return Coins;
    }

    public void setCoins(int coins) {
        Coins = coins;
    }

    public String[] getClaims() {
        return Claims;
    }

    public void setClaims(String[] claims) {
        Claims = claims;
    }

    public String[] getRoles() {
        return Roles;
    }

    public void setRoles(String[] roles) {
        Roles = roles;
    }

    public String[] getLogins() {
        return Logins;
    }

    public void setLogins(String[] logins) {
        Logins = logins;
    }
}
