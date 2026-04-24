package model;


import java.time.LocalDateTime;

public class Comment {
    public enum CommentType { QUESTION, ANSWER, FEEDBACK }

    @User.Column(name="id")           private int id;
    @User.Column(name="post_id")      private int postId;
    @User.Column(name="user_id")      private int userId;
    @User.Column(name="content")      private String content;
    @User.Column(name="comment_type") private CommentType commentType;
    @User.Column(name="created_at")   private LocalDateTime createdAt;

    public Comment() {}
    public Comment(int id, int postId, int userId, String content, CommentType type) {
        this.id=id; this.postId=postId; this.userId=userId;
        this.content=content; this.commentType=type; this.createdAt=LocalDateTime.now();
    }

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id=id; }
    public int    getPostId()                    { return postId; }
    public void   setPostId(int p)               { this.postId=p; }
    public int    getUserId()                    { return userId; }
    public void   setUserId(int u)               { this.userId=u; }
    public String getContent()                   { return content; }
    public void   setContent(String c)           { this.content=c; }
    public CommentType getCommentType()          { return commentType; }
    public void   setCommentType(CommentType t)  { this.commentType=t; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void   setCreatedAt(LocalDateTime dt) { this.createdAt=dt; }

    @Override public String toString() {
        return "Comment{id="+id+", type="+commentType+", userId="+userId+"}";
    }
}