package com.example.needlove.Model;

import com.google.firebase.firestore.Exclude;

public class LikesModel {

    String id;
    String username;
    String pic;

    public LikesModel() {
    }

    public LikesModel(String username, String pic) {
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
