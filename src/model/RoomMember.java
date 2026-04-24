package model;

import java.time.LocalDateTime;

public class RoomMember {
    @User.Column(name="id")        private int id;
    @User.Column(name="room_id")   private int roomId;
    @User.Column(name="user_id")   private int userId;
    @User.Column(name="joined_at") private LocalDateTime joinedAt;
    private boolean isOnline = true;

    public RoomMember() {}
    public RoomMember(int id, int roomId, int userId) {
        this.id=id; this.roomId=roomId; this.userId=userId;
        this.joinedAt=LocalDateTime.now();
    }

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id=id; }
    public int    getRoomId()                    { return roomId; }
    public void   setRoomId(int r)               { this.roomId=r; }
    public int    getUserId()                    { return userId; }
    public void   setUserId(int u)               { this.userId=u; }
    public LocalDateTime getJoinedAt()           { return joinedAt; }
    public void   setJoinedAt(LocalDateTime dt)  { this.joinedAt=dt; }
    public boolean isOnline()                    { return isOnline; }
    public void   setOnline(boolean o)           { this.isOnline=o; }

    @Override public String toString() {
        return "RoomMember{roomId="+roomId+", userId="+userId+", online="+isOnline+"}";
    }
}