package com.example.needlove.Model;

public class UsersDiscover {
    private String uid;
    private String username;
    private int age;
    private String profileimage;
    private String country;
    private boolean online;

    public UsersDiscover() {
    }

    public UsersDiscover(String uid, String username, int age, String profileimage, String country , boolean online) {
        this.uid = uid;
        this.username = username;
        this.age = age;
        this.profileimage = profileimage;
        this.country = country;
        this.online = online;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public boolean isOnline() {
        return online;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
