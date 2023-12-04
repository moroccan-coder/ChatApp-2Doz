package com.example.needlove.Model;

import com.google.firebase.firestore.Exclude;

public class Friends {


    String id;
    String username;
    String pic;

    public Friends() {
    }

    public Friends(String username, String pic) {
        this.username = username;
        this.pic = pic;
    }

    public String getUsername() {
        return username;
    }

    public String getPic() {
        return pic;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }
}
