package model;

import java.time.*;

public class StudySession {
    @User.Column(name="id")               private int id;
    @User.Column(name="student_id")       private int studentId;
    @User.Column(name="duration_minutes") private int durationMinutes;
    @User.Column(name="date")             private LocalDate date;
    @User.Column(name="course_id")        private int courseId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private boolean isPomodoro = true;

    public StudySession() {}
    public StudySession(int id, int studentId, int courseId, int durationMinutes) {
        this.id=id; this.studentId=studentId; this.courseId=courseId;
        this.durationMinutes=durationMinutes; this.date=LocalDate.now();
        this.startedAt=LocalDateTime.now();
    }

    public void end() {
        this.endedAt=LocalDateTime.now();
        if (startedAt!=null)
            this.durationMinutes=(int) Duration.between(startedAt, endedAt).toMinutes();
    }

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id=id; }
    public int    getStudentId()                 { return studentId; }
    public void   setStudentId(int s)            { this.studentId=s; }
    public int    getDurationMinutes()           { return durationMinutes; }
    public void   setDurationMinutes(int d)      { this.durationMinutes=d; }
    public LocalDate getDate()                   { return date; }
    public void   setDate(LocalDate d)           { this.date=d; }
    public int    getCourseId()                  { return courseId; }
    public void   setCourseId(int c)             { this.courseId=c; }
    public LocalDateTime getStartedAt()          { return startedAt; }
    public void   setStartedAt(LocalDateTime dt) { this.startedAt=dt; }
    public LocalDateTime getEndedAt()            { return endedAt; }
    public void   setEndedAt(LocalDateTime dt)   { this.endedAt=dt; }
    public boolean isPomodoro()                  { return isPomodoro; }
    public void   setPomodoro(boolean p)         { this.isPomodoro=p; }

    @Override public String toString() {
        return "StudySession{id="+id+", student="+studentId
                +", duration="+durationMinutes+"min, date="+date+"}";
    }
}