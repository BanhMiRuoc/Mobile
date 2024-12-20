package com.example.ex1.Model;

//import androidx.room.Entity;
//import androidx.room.PrimaryKey;
//@Entity(tableName = "students")
public class Item {
//    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String mail;
    private String number;

    public Item(int id, String name, String mail, String number) {
        this.id = id;
        this.name = name;
        this.mail = mail;
        this.number = number;
    }

    public Item(String name, String mail, String number) {
        this.name = name;
        this.mail = mail;
        this.number = number;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mail='" + mail + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}
