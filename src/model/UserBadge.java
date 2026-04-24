package model;

import java.time.LocalDateTime;

public class UserBadge {
    @User.Column(name="id")        private int id;
    @User.Column(name="user_id")   private int userId;
    @User.Column(name="badge_id")  private int badgeId;
    @User.Column(name="earned_at") private LocalDateTime earnedAt;

    public UserBadge() {}
    public UserBadge(int id, int userId, int badgeId) {
        this.id=id; this.userId=userId; this.badgeId=badgeId;
        this.earnedAt=LocalDateTime.now();
    }

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id=id; }
    public int    getUserId()                    { return userId; }
    public void   setUserId(int u)               { this.userId=u; }
    public int    getBadgeId()                   { return badgeId; }
    public void   setBadgeId(int b)              { this.badgeId=b; }
    public LocalDateTime getEarnedAt()           { return earnedAt; }
    public void   setEarnedAt(LocalDateTime dt)  { this.earnedAt=dt; }

    @Override public String toString() {
        return "UserBadge{userId="+userId+", badgeId="+badgeId+", earned="+earnedAt+"}";
    }
}