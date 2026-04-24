package model;

public class Badge {
    public enum ConditionType {
        FIRST_POST, POMODORO_10, STUDY_50H, QUIZ_100_3X,
        ROOM_5X, STREAK_7, TOP_LEADERBOARD, ANSWER_10
    }

    @User.Column(name="id")             private int id;
    @User.Column(name="name")           private String name;
    @User.Column(name="description")    private String description;
    @User.Column(name="icon_url")       private String iconUrl;
    @User.Column(name="condition_type") private ConditionType conditionType;

    public Badge() {}
    public Badge(int id, String name, String description,
                 String iconUrl, ConditionType conditionType) {
        this.id=id; this.name=name; this.description=description;
        this.iconUrl=iconUrl; this.conditionType=conditionType;
    }

    public int    getId()                            { return id; }
    public void   setId(int id)                      { this.id=id; }
    public String getName()                          { return name; }
    public void   setName(String n)                  { this.name=n; }
    public String getDescription()                   { return description; }
    public void   setDescription(String d)           { this.description=d; }
    public String getIconUrl()                       { return iconUrl; }
    public void   setIconUrl(String u)               { this.iconUrl=u; }
    public ConditionType getConditionType()          { return conditionType; }
    public void   setConditionType(ConditionType c)  { this.conditionType=c; }

    @Override public String toString() {
        return "Badge{id="+id+", name='"+name+"', condition="+conditionType+"}";
    }
}