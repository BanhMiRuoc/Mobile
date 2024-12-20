package com.example.milita;

import android.graphics.Bitmap;

public class Certificate {
    private String id;
    private String name;
    private String dateCreated;
    private String des;               // Description
    private String organization;
    private String school;
    private String session;
    private Bitmap profileImage;

    public Certificate (String name, String session, String dateCreated, Bitmap profileImage) {
        this.name = name;
        this.session = session;
        this.dateCreated = dateCreated;
        this.profileImage = profileImage;
    }

    // Constructor including all attributes
    public Certificate(String name, String dateCreated, String des, String organization, String school, String session, Bitmap profileImage) {
        this.name = name;
        this.dateCreated = dateCreated;
        this.des = des;
        this.organization = organization;
        this.school = school;
        this.session = session;
        this.profileImage = profileImage;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDateCreated() { return dateCreated; }
    public String getDes() { return des; }
    public String getOrganization() { return organization; }
    public String getSchool() { return school; }
    public String getSession() { return session; }
    public Bitmap getProfileImage() { return profileImage; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
    public void setDes(String des) { this.des = des; }
    public void setOrganization(String organization) { this.organization = organization; }
    public void setSchool(String school) { this.school = school; }
    public void setSession(String session) { this.session = session; }
    public void setProfileImage(Bitmap profileImage) { this.profileImage = profileImage; }
}
