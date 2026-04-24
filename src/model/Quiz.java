package model;


import java.time.LocalDateTime;
import java.util.*;

public class Quiz {
    @User.Column(name="id")         private int id;
    @User.Column(name="course_id")  private int courseId;
    @User.Column(name="teacher_id") private int teacherId;
    @User.Column(name="title")      private String title;
    @User.Column(name="time_limit") private int timeLimitSeconds;
    @User.Column(name="created_at") private LocalDateTime createdAt;

    private List<QuizQuestion>         questions = new ArrayList<>();
    private Map<Integer,QuizAttempt>   attempts  = new HashMap<>();

    public Quiz() {}
    public Quiz(int id, int courseId, int teacherId, String title, int timeLimitSec) {
        this.id=id; this.courseId=courseId; this.teacherId=teacherId;
        this.title=title; this.timeLimitSeconds=timeLimitSec; this.createdAt=LocalDateTime.now();
    }

    public void       addQuestion(QuizQuestion q)    { questions.add(q); }
    public void       recordAttempt(QuizAttempt a)   { attempts.put(a.getStudentId(), a); }
    public QuizAttempt getAttempt(int studentId)     { return attempts.get(studentId); }
    public boolean    hasAttempted(int studentId)    { return attempts.containsKey(studentId); }
    public int        getQuestionCount()             { return questions.size(); }
    public double     getAverageScore()              { return attempts.values().stream().mapToDouble(QuizAttempt::getScore).average().orElse(0); }

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id=id; }
    public int    getCourseId()                  { return courseId; }
    public void   setCourseId(int c)             { this.courseId=c; }
    public int    getTeacherId()                 { return teacherId; }
    public void   setTeacherId(int t)            { this.teacherId=t; }
    public String getTitle()                     { return title; }
    public void   setTitle(String t)             { this.title=t; }
    public int    getTimeLimitSeconds()          { return timeLimitSeconds; }
    public void   setTimeLimitSeconds(int t)     { this.timeLimitSeconds=t; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void   setCreatedAt(LocalDateTime dt) { this.createdAt=dt; }
    public List<QuizQuestion> getQuestions()     { return Collections.unmodifiableList(questions); }
    public Map<Integer,QuizAttempt> getAttempts(){ return Collections.unmodifiableMap(attempts); }

    @Override public String toString() {
        return "Quiz{id="+id+", title='"+title+"', questions="+questions.size()+", attempts="+attempts.size()+"}";
    }
}