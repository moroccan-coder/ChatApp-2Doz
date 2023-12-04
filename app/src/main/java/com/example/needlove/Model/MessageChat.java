package com.example.needlove.Model;

import com.google.firebase.Timestamp;

public class MessageChat {

    Timestamp time;
    String from;
    String message;
    String pic;
    String type;
    String msgID;
    boolean seen;

    public MessageChat() {
    }


    public MessageChat(String msgId,Timestamp time, String from, String message, String pic, String type,boolean seen) {
        this.time = time;
        this.msgID = msgId;
        this.from = from;
        this.message = message;
        this.pic = pic;
        this.type = type;
        this.seen = seen;
    }

    public Timestamp getTime() {
        return time;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public String getPic() {
        return pic;
    }

    public String getType() {
        return type;
    }

    public String getMsgID() {
        return msgID;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }
}
