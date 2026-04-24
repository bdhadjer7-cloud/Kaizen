package model;


import java.time.LocalDateTime;

public class Message {
    @User.Column(name="id")          private int id;
    @User.Column(name="sender_id")   private int senderId;
    @User.Column(name="receiver_id") private int receiverId;
    @User.Column(name="content")     private String content;
    @User.Column(name="file_url")    private String fileUrl;
    @User.Column(name="sent_at")     private LocalDateTime sentAt;
    @User.Column(name="is_read")     private boolean isRead;

    public Message() {}
    public Message(int id, int senderId, int receiverId, String content) {
        this.id=id; this.senderId=senderId; this.receiverId=receiverId;
        this.content=content; this.sentAt=LocalDateTime.now(); this.isRead=false;
    }

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id=id; }
    public int    getSenderId()                  { return senderId; }
    public void   setSenderId(int s)             { this.senderId=s; }
    public int    getReceiverId()                { return receiverId; }
    public void   setReceiverId(int r)           { this.receiverId=r; }
    public String getContent()                   { return content; }
    public void   setContent(String c)           { this.content=c; }
    public String getFileUrl()                   { return fileUrl; }
    public void   setFileUrl(String f)           { this.fileUrl=f; }
    public LocalDateTime getSentAt()             { return sentAt; }
    public void   setSentAt(LocalDateTime dt)    { this.sentAt=dt; }
    public boolean isRead()                      { return isRead; }
    public void   setRead(boolean r)             { this.isRead=r; }

    @Override public String toString() {
        return "Message{id="+id+", from="+senderId+"->"+receiverId+", read="+isRead+"}";
    }
}