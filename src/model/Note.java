package model;


import java.time.LocalDateTime;
import java.util.*;

public class Note {
    @User.Column(name="id")         private int id;
    @User.Column(name="student_id") private int studentId;
    @User.Column(name="course_id")  private int courseId;
    @User.Column(name="title")      private String title;
    @User.Column(name="content")    private String content;
    @User.Column(name="updated_at") private LocalDateTime updatedAt;
    private Set<String> tags = new LinkedHashSet<>();

    public Note() {}
    public Note(int id, int studentId, int courseId, String title, String content) {
        this.id=id; this.studentId=studentId; this.courseId=courseId;
        this.title=title; this.content=content; this.updatedAt=LocalDateTime.now();
    }

    public void    addTag(String t)    { tags.add(t.toLowerCase()); }
    public boolean hasTag(String t)    { return tags.contains(t.toLowerCase()); }
    public Set<String> getTags()       { return Collections.unmodifiableSet(tags); }

    public <T extends CharSequence> boolean matches(T keyword) {
        String kw = keyword.toString().toLowerCase();
        return title.toLowerCase().contains(kw) || content.toLowerCase().contains(kw);
    }

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id=id; }
    public int    getStudentId()                 { return studentId; }
    public void   setStudentId(int s)            { this.studentId=s; }
    public int    getCourseId()                  { return courseId; }
    public void   setCourseId(int c)             { this.courseId=c; }
    public String getTitle()                     { return title; }
    public void   setTitle(String t)             { this.title=t; }
    public String getContent()                   { return content; }
    public void   setContent(String c)           { this.content=c; }
    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public void   setUpdatedAt(LocalDateTime dt) { this.updatedAt=dt; }
    public void   setTags(Set<String> t)         { this.tags=t; }

    @Override public String toString() {
        return "Note{id="+id+", title='"+title+"', tags="+tags+"}";
    }
}