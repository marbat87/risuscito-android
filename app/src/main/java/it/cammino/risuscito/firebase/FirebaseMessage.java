package it.cammino.risuscito.firebase;

public class FirebaseMessage {

    private String id;
    private String dateSent;
    private String content;
    private String sender;
//    private String messageType;
    private String receiver;
    private boolean read;

    public FirebaseMessage() {}

    public FirebaseMessage(String text, String name, String data, String email) {
        this.content = text;
        this.sender = name;
        this.dateSent = data;
        this.receiver = email;
        this.read = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getDateSent() {
        return dateSent;
    }

    public void setDateSent(String dateSent) {
        this.dateSent = dateSent;
    }

//    public String getMessageType() {
//        return messageType;
//    }

//    public void setMessageType(String messageType) {
//        this.messageType = messageType;
//    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
