package com.example.needlove.Model;

import com.google.firebase.Timestamp;

public class MessagesList {

    private String id;
    private int msgUnread;
    private Timestamp time;
    private String lastmsg;
    private String username;
    private String pic;

    public MessagesList() {
    }

    public MessagesList(String id, int msgUnread, Timestamp time, String lastmsg, String username, String pic) {
        this.id = id;
        this.msgUnread = msgUnread;
        this.time = time;
        this.lastmsg = lastmsg;
        this.username = username;
        this.pic = pic;
    }

    public String getId() {
        return id;
    }

    public int getMsgUnread() {
        return msgUnread;
    }

    public Timestamp getTime() {
        return time;
    }

    public String getLastmsg() {
        return lastmsg;
    }

    public String getUsername() {
        return username;
    }

    public String getPic() {
        return pic;
    }

    public void setId(String id) {
        this.id = id;
    }
}
