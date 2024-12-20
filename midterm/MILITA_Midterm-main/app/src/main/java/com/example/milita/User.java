package com.example.milita;

import android.graphics.Bitmap;

public class User {
    private String userId;
    private String name;
    private String role;
    private String status;
    private Bitmap profileImage;

    // Constructor bao gồm ảnh đại diện
    public User(String name, String role, String status, Bitmap profileImage) {
        this.name = name;
        this.role = role;
        this.status = status;
        this.profileImage = profileImage;
    }

    // Các getter và setter
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public Bitmap getProfileImage() { return profileImage; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
    public void setProfileImage(Bitmap profileImage) { this.profileImage = profileImage; }
}
