package com.example.textme.FirebaseItems;

import java.util.ArrayList;

public class User {
    public String email;
    public String name;
    public String photo;
    public String photoCompressed;
    public String desc;


    public User() { }

    public User(String email, String name, String photo, String photoCompress, String desc) {
        this.email = email;
        this.name = name;
        this.photo = photo;
        this.photoCompressed = photoCompress;
        this.desc = desc;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPhotoCompressed() {
        return photoCompressed;
    }

    public void setPhotoCompressed(String photoCompress) {
        this.photoCompressed = photoCompress;
    }
}
