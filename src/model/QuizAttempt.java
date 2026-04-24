package model;

import java.time.LocalDateTime;
import java.util.*;

public class QuizAttempt {
    @User.Column(name="id")           private int id;
    @User.Column(name="student_id")   private int studentId;
    @User.Column(name="quiz_id")      private int quizId;
    @User.Column(name="score")        private double score;
    @User.Column(name="attempted_at") private LocalDateTime attemptedAt;
    private Map<Integer,Character> answers = new LinkedHashMap<>();

    public QuizAttempt() {}
    public QuizAttempt(int id, int studentId, int quizId) {
        this.id=id; this.studentId=studentId; this.quizId=quizId;
        this.attemptedAt=LocalDateTime.now();
    }

    public void recordAnswer(int questionId, char answer) { answers.put(questionId, answer); }
    public char getAnswer(int questionId)  { return answers.getOrDefault(questionId, ' '); }

    public <T extends QuizQuestion> double calculateScore(List<T> questions) {
        if (questions.isEmpty()) return 0;
        long correct = questions.stream()
                .filter(q -> answers.containsKey(q.getId()) && q.isCorrect(answers.get(q.getId())))
                .count();
        this.score = (double) correct / questions.size() * 100;
        return this.score;
    }

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id=id; }
    public int    getStudentId()                 { return studentId; }
    public void   setStudentId(int s)            { this.studentId=s; }
    public int    getQuizId()                    { return quizId; }
    public void   setQuizId(int q)               { this.quizId=q; }
    public double getScore()                     { return score; }
    public void   setScore(double s)             { this.score=s; }
    public LocalDateTime getAttemptedAt()        { return attemptedAt; }
    public void   setAttemptedAt(LocalDateTime d){ this.attemptedAt=d; }
    public Map<Integer,Character> getAnswers()   { return Collections.unmodifiableMap(answers); }

    @Override public String toString() {
        return "QuizAttempt{id="+id+", student="+studentId+", score="+String.format("%.1f",score)+"%}";
    }
}