package model;

public class Flashcard {
    @User.Column(name="id")         private int id;
    @User.Column(name="set_id")     private int setId;
    @User.Column(name="front_text") private String frontText;
    @User.Column(name="back_text")  private String backText;
    private boolean flipped = false;

    public Flashcard() {}
    public Flashcard(int id, int setId, String front, String back) {
        this.id=id; this.setId=setId; this.frontText=front; this.backText=back;
    }

    public String getCurrentFace() { return flipped ? backText : frontText; }
    public void   flip()           { flipped = !flipped; }
    public void   reset()          { flipped = false; }

    public int    getId()              { return id; }
    public void   setId(int id)        { this.id=id; }
    public int    getSetId()           { return setId; }
    public void   setSetId(int s)      { this.setId=s; }
    public String getFrontText()       { return frontText; }
    public void   setFrontText(String f){ this.frontText=f; }
    public String getBackText()        { return backText; }
    public void   setBackText(String b){ this.backText=b; }
    public boolean isFlipped()         { return flipped; }

    @Override public String toString() {
        return "Flashcard{id="+id+", front='"+frontText+"', flipped="+flipped+"}";
    }
}