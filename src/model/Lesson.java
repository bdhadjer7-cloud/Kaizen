package model;

public class Lesson {

    @User.Column(name = "id")        private int    id;
    @User.Column(name = "course_id") private int    courseId;
    @User.Column(name = "title")     private String title;
    @User.Column(name = "content")   private String content;
    @User.Column(name = "file_url")  private String fileUrl;

    public Lesson() {}

    public Lesson(int id, int courseId, String title, String content) {
        this.id       = id;
        this.courseId = courseId;
        this.title    = title;
        this.content  = content;
    }

    public int    getId()                    { return id; }
    public void   setId(int id)              { this.id = id; }
    public int    getCourseId()              { return courseId; }
    public void   setCourseId(int courseId)  { this.courseId = courseId; }
    public String getTitle()                 { return title; }
    public void   setTitle(String title)     { this.title = title; }
    public String getContent()               { return content; }
    public void   setContent(String content) { this.content = content; }
    public String getFileUrl()               { return fileUrl; }
    public void   setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    @Override
    public String toString() {
        return "Lesson{id=" + id + ", courseId=" + courseId + ", title='" + title + "'}";
    }
}
