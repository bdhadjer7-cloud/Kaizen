package model;

import java.time.LocalDateTime;
import java.util.*;

public class CalendarEvent {
    public enum EventType { EXAM, ASSIGNMENT, REMINDER, SESSION }

    @User.Column(name="id")          private int id;
    @User.Column(name="user_id")     private int userId;
    @User.Column(name="title")       private String title;
    @User.Column(name="description") private String description;
    @User.Column(name="due_date")    private LocalDateTime dueDate;
    @User.Column(name="type")        private EventType type;
    @User.Column(name="course_id")   private int courseId;
    private Set<Integer> pushedToStudents = new HashSet<>();

    public CalendarEvent() {}
    public CalendarEvent(int id, int userId, String title,
                         LocalDateTime dueDate, EventType type) {
        this.id=id; this.userId=userId; this.title=title;
        this.dueDate=dueDate; this.type=type;
    }
    public CalendarEvent(int id, int userId, String title, String description,
                         LocalDateTime dueDate, EventType type) {
        this.id=id; this.userId=userId; this.title=title; this.description=description;
        this.dueDate=dueDate; this.type=type;
    }

    public void    pushToStudent(int sid)  { pushedToStudents.add(sid); }
    public boolean isPushedTo(int sid)     { return pushedToStudents.contains(sid); }
    public boolean isUpcoming()            { return dueDate!=null && dueDate.isAfter(LocalDateTime.now()); }
    public long    getDaysUntilDue()       { return dueDate==null ? -1 : java.time.Duration.between(LocalDateTime.now(), dueDate).toDays(); }

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id=id; }
    public int    getUserId()                    { return userId; }
    public void   setUserId(int u)               { this.userId=u; }
    public String getTitle()                     { return title; }
    public void   setTitle(String t)             { this.title=t; }
    public String getDescription()               { return description; }
    public void   setDescription(String d)       { this.description=d; }
    public LocalDateTime getDueDate()            { return dueDate; }
    public void   setDueDate(LocalDateTime dt)   { this.dueDate=dt; }
    public EventType getType()                   { return type; }
    public void   setType(EventType t)           { this.type=t; }
    public int    getCourseId()                  { return courseId; }
    public void   setCourseId(int c)             { this.courseId=c; }
    public Set<Integer> getPushedToStudents()    { return Collections.unmodifiableSet(pushedToStudents); }

    @Override public String toString() {
        return "CalendarEvent{id="+id+", title='"+title+"', type="+type
                +", daysLeft="+getDaysUntilDue()+"}";
    }
}