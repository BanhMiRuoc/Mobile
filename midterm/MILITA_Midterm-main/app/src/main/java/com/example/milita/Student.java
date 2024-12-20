package com.example.milita;

import android.graphics.Bitmap;

public class Student {
    private String id;
    private String name;
    private String birthday;
    private String email;
    private String phone;
    private String studentClass;
    private String faculty;
    private Bitmap profileImage;

    public Student(String id, String name, String studentClass, Bitmap profileImage) {
        this.id = id;
        this.name = name;
        this.studentClass = studentClass;
        this.profileImage = profileImage;
    }

    // Constructor bao gồm tất cả các thuộc tính
    public Student(String id, String name, String birthday, String email, String phone, String studentClass, String faculty, Bitmap profileImage) {
        this.id = id;
        this.name = name;
        this.birthday = birthday;
        this.email = email;
        this.phone = phone;
        this.studentClass = studentClass;
        this.faculty = faculty;
        this.profileImage = profileImage;
    }

    // Các getter và setter
    public String getId() { return id; }
    public String getName() { return name; }
    public String getBirthday() { return birthday; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getStudentClass() { return studentClass; }
    public String getFaculty() { return faculty; }
    public Bitmap getProfileImage() { return profileImage; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
    public void setProfileImage(Bitmap profileImage) { this.profileImage = profileImage; }
}
