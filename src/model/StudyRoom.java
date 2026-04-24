package model;

import java.time.LocalDateTime;
import java.util.*;

public class StudyRoom {
    @User.Column(name="id")           private int id;
    @User.Column(name="name")         private String name;
    @User.Column(name="course_id")    private int courseId;
    @User.Column(name="created_by")   private int createdBy;
    @User.Column(name="max_members")  private int maxMembers;
    @User.Column(name="scheduled_at") private LocalDateTime scheduledAt;
    private boolean isLive = false;

    private Set<Integer>     memberIds    = new LinkedHashSet<>();
    private Map<Integer,Integer> studyMinutes = new HashMap<>();
    private List<String>     chatMessages = new ArrayList<>();

    public StudyRoom() {}
    public StudyRoom(int id, String name, int courseId, int createdBy,
                     int maxMembers, LocalDateTime scheduledAt) {
        this.id=id; this.name=name; this.courseId=courseId;
        this.createdBy=createdBy; this.maxMembers=maxMembers; this.scheduledAt=scheduledAt;
    }

    public boolean join(int uid)    { if (memberIds.size()>=maxMembers) return false; memberIds.add(uid); studyMinutes.putIfAbsent(uid,0); return true; }
    public void    leave(int uid)   { memberIds.remove(uid); }
    public boolean isFull()         { return memberIds.size()>=maxMembers; }
    public int     getMemberCount() { return memberIds.size(); }
    public void    addStudyMinutes(int uid, int mins) { studyMinutes.merge(uid, mins, Integer::sum); }
    public int     getStudyMinutes(int uid)           { return studyMinutes.getOrDefault(uid,0); }
    public void    addChatMessage(String msg)         { chatMessages.add(msg); }

    public LinkedHashMap<Integer,Integer> getRoomLeaderboard() {
        LinkedHashMap<Integer,Integer> sorted = new LinkedHashMap<>();
        studyMinutes.entrySet().stream()
                .sorted(Map.Entry.<Integer,Integer>comparingByValue().reversed())
                .forEach(e -> sorted.put(e.getKey(), e.getValue()));
        return sorted;
    }

    public int    getId()                          { return id; }
    public void   setId(int id)                    { this.id=id; }
    public String getName()                        { return name; }
    public void   setName(String n)                { this.name=n; }
    public int    getCourseId()                    { return courseId; }
    public void   setCourseId(int c)               { this.courseId=c; }
    public int    getCreatedBy()                   { return createdBy; }
    public void   setCreatedBy(int c)              { this.createdBy=c; }
    public int    getMaxMembers()                  { return maxMembers; }
    public void   setMaxMembers(int m)             { this.maxMembers=m; }
    public LocalDateTime getScheduledAt()          { return scheduledAt; }
    public void   setScheduledAt(LocalDateTime dt) { this.scheduledAt=dt; }
    public boolean isLive()                        { return isLive; }
    public void   setLive(boolean l)               { this.isLive=l; }
    public Set<Integer>  getMemberIds()            { return Collections.unmodifiableSet(memberIds); }
    public List<String>  getChatMessages()         { return Collections.unmodifiableList(chatMessages); }

    @Override public String toString() {
        return "StudyRoom{id="+id+", name='"+name+"', members="
                +memberIds.size()+"/"+maxMembers+", live="+isLive+"}";
    }
}
