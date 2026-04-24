package model;

import java.util.*;

public class QuizQuestion {
    @User.Column(name="id")             private int id;
    @User.Column(name="quiz_id")        private int quizId;
    @User.Column(name="question_text")  private String questionText;
    @User.Column(name="option_a")       private String optionA;
    @User.Column(name="option_b")       private String optionB;
    @User.Column(name="option_c")       private String optionC;
    @User.Column(name="option_d")       private String optionD;
    @User.Column(name="correct_option") private char correctOption;

    private Map<Character,String> optionsMap = new LinkedHashMap<>();

    public QuizQuestion() {}
    public QuizQuestion(int id, int quizId, String question,
                        String a, String b, String c, String d, char correct) {
        this.id=id; this.quizId=quizId; this.questionText=question;
        this.optionA=a; this.optionB=b; this.optionC=c; this.optionD=d;
        this.correctOption=correct; rebuildMap();
    }

    private void rebuildMap() {
        optionsMap.clear();
        optionsMap.put('A',optionA); optionsMap.put('B',optionB);
        optionsMap.put('C',optionC); optionsMap.put('D',optionD);
    }

    public boolean isCorrect(char answer) {
        return Character.toUpperCase(answer) == Character.toUpperCase(correctOption);
    }
    public Map<Character,String> getOptionsMap() { return Collections.unmodifiableMap(optionsMap); }

    public int    getId()                    { return id; }
    public void   setId(int id)              { this.id=id; }
    public int    getQuizId()               { return quizId; }
    public void   setQuizId(int q)           { this.quizId=q; }
    public String getQuestionText()          { return questionText; }
    public void   setQuestionText(String t)  { this.questionText=t; }
    public String getOptionA()               { return optionA; }
    public void   setOptionA(String a)       { this.optionA=a; rebuildMap(); }
    public String getOptionB()               { return optionB; }
    public void   setOptionB(String b)       { this.optionB=b; rebuildMap(); }
    public String getOptionC()               { return optionC; }
    public void   setOptionC(String c)       { this.optionC=c; rebuildMap(); }
    public String getOptionD()               { return optionD; }
    public void   setOptionD(String d)       { this.optionD=d; rebuildMap(); }
    public char   getCorrectOption()         { return correctOption; }
    public void   setCorrectOption(char c)   { this.correctOption=c; }

    @Override public String toString() {
        return "QuizQuestion{id="+id+", correct='"+correctOption+"'}";
    }
}