package model;

import java.time.LocalDateTime;
import java.util.*;

public class FlashcardSet {
    @User.Column(name="id")         private int id;
    @User.Column(name="course_id")  private int courseId;
    @User.Column(name="teacher_id") private int teacherId;
    @User.Column(name="title")      private String title;
    @User.Column(name="created_at") private LocalDateTime createdAt;

    private List<Flashcard>      cards      = new ArrayList<>();
    private Map<Integer,Boolean> masteryMap = new HashMap<>();

    public FlashcardSet() {}
    public FlashcardSet(int id, int courseId, int teacherId, String title) {
        this.id=id; this.courseId=courseId; this.teacherId=teacherId;
        this.title=title; this.createdAt=LocalDateTime.now();
    }

    public void addCard(Flashcard c)          { cards.add(c); masteryMap.put(c.getId(), false); }
    public void markMastered(int cardId)      { masteryMap.put(cardId, true); }
    public void markReview(int cardId)        { masteryMap.put(cardId, false); }
    public boolean isMastered(int cardId)     { return masteryMap.getOrDefault(cardId, false); }
    public int  getMasteredCount()            { return (int)masteryMap.values().stream().filter(v->v).count(); }
    public double getMasteryPercent()         { return cards.isEmpty() ? 0 : (double)getMasteredCount()/cards.size()*100; }
    public Flashcard getNextReviewCard()      { return cards.stream().filter(c->!isMastered(c.getId())).findFirst().orElse(null); }

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id=id; }
    public int    getCourseId()                  { return courseId; }
    public void   setCourseId(int c)             { this.courseId=c; }
    public int    getTeacherId()                 { return teacherId; }
    public void   setTeacherId(int t)            { this.teacherId=t; }
    public String getTitle()                     { return title; }
    public void   setTitle(String t)             { this.title=t; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void   setCreatedAt(LocalDateTime dt) { this.createdAt=dt; }
    public List<Flashcard>      getCards()       { return Collections.unmodifiableList(cards); }
    public Map<Integer,Boolean> getMasteryMap()  { return Collections.unmodifiableMap(masteryMap); }

    @Override public String toString() {
        return "FlashcardSet{id="+id+", title='"+title+"', cards="+cards.size()
                +", mastery="+String.format("%.0f",getMasteryPercent())+"% }";
    }
}