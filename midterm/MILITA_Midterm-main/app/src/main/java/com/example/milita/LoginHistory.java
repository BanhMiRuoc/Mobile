package com.example.milita;

import android.graphics.Bitmap;

public class LoginHistory {
    private String account;
    private String name;
    private String time;
    private Bitmap profileImage;

    // Constructor bao gồm ảnh đại diện
    public LoginHistory(String account, String name, String time, Bitmap profileImage) {
        this.account = account;
        this.name = name;
        this.time = time;
        this.profileImage = profileImage;
    }

    // Các getter và setter
    public String getAccount() { return account; }
    public String getName() { return name; }
    public String getTime() { return time; }
    public Bitmap getProfileImage() { return profileImage; }

    public void setAccount(String account) { this.account = account; }
    public void setName(String name) { this.name = name; }
    public void setTime(String time) { this.time = time; }
    public void setProfileImage(Bitmap profileImage) { this.profileImage = profileImage; }
}
