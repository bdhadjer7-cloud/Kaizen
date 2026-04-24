package model;

import java.time.LocalDateTime;
import java.util.*;

public class Resource {
    public enum ResourceType { PDF, LINK, APP, AI_TOOL, CODE, SLIDES, VIDEO }

    @User.Column(name="id")         private int id;
    @User.Column(name="user_id")    private int userId;
    @User.Column(name="course_id")  private int courseId;
    @User.Column(name="title")      private String title;
    @User.Column(name="type")       private ResourceType type;
    @User.Column(name="url")        private String url;
    @User.Column(name="rating")     private double rating;
    @User.Column(name="created_at") private LocalDateTime createdAt;

    private Set<Integer>     savedByUserIds = new HashSet<>();
    private Map<Integer,Integer> ratings   = new HashMap<>();

    public Resource() {}
    public Resource(int id, int userId, int courseId,
                    String title, ResourceType type, String url) {
        this.id=id; this.userId=userId; this.courseId=courseId;
        this.title=title; this.type=type; this.url=url; this.createdAt=LocalDateTime.now();
    }

    public void    saveBy(int uid)          { savedByUserIds.add(uid); }
    public void    unsaveBy(int uid)        { savedByUserIds.remove(uid); }
    public boolean isSavedBy(int uid)       { return savedByUserIds.contains(uid); }
    public int     getSaveCount()           { return savedByUserIds.size(); }
    public void    addRating(int uid, int stars) {
        ratings.put(uid, Math.min(5, Math.max(1, stars)));
        this.rating = ratings.values().stream().mapToInt(i->i).average().orElse(0);
    }

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id=id; }
    public int    getUserId()                    { return userId; }
    public void   setUserId(int u)               { this.userId=u; }
    public int    getCourseId()                  { return courseId; }
    public void   setCourseId(int c)             { this.courseId=c; }
    public String getTitle()                     { return title; }
    public void   setTitle(String t)             { this.title=t; }
    public ResourceType getType()                { return type; }
    public void   setType(ResourceType t)        { this.type=t; }
    public String getUrl()                       { return url; }
    public void   setUrl(String u)               { this.url=u; }
    public double getRating()                    { return rating; }
    public void   setRating(double r)            { this.rating=r; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void   setCreatedAt(LocalDateTime dt) { this.createdAt=dt; }
    public Set<Integer> getSavedByUserIds()      { return Collections.unmodifiableSet(savedByUserIds); }

    @Override public String toString() {
        return "Resource{id="+id+", type="+type+", title='"+title
                +"', rating="+String.format("%.1f",rating)+", saves="+savedByUserIds.size()+"}";
    }
}