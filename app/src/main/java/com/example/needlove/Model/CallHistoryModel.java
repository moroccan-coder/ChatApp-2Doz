package com.example.needlove.Model;

import com.google.firebase.Timestamp;

public class CallHistoryModel {

    String uid;
    String username;
    String country;
    String pic;
    String callingtime;
    Timestamp time;

    public CallHistoryModel() {
    }

    public CallHistoryModel(String uid, String username, String country, String pic, String callingtime, Timestamp time) {
        this.uid = uid;
        this.username = username;
        this.country = country;
        this.pic = pic;
        this.callingtime = callingtime;
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getCountry() {
        return country;
    }

    public String getPic() {
        return pic;
    }

    public String getCallingtime() {
        return callingtime;
    }

    public Timestamp getTime() {
        return time;
    }
}
