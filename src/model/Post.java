package model;

import java.time.LocalDateTime;
import java.util.*;

public class Post {
    public enum PostType     { TEXT, IMAGE, LINK, FILE }
    public enum ReactionType { FIRE, INSIGHTFUL, HELPFUL, STAR }

    @User.Column(name="id")          private int id;
    @User.Column(name="user_id")     private int userId;
    @User.Column(name="content")     private String content;
    @User.Column(name="type")        private PostType type;
    @User.Column(name="likes_count") private int likesCount;
    @User.Column(name="created_at")  private LocalDateTime createdAt;

    private Map<ReactionType,Set<Integer>> reactions = new EnumMap<>(ReactionType.class);
    private List<Comment> comments = new ArrayList<>();
    private Set<String>   tags     = new LinkedHashSet<>();

    public Post() {
        for (ReactionType r : ReactionType.values()) reactions.put(r, new HashSet<>());
    }
    public Post(int id, int userId, String content, PostType type) {
        this(); this.id=id; this.userId=userId;
        this.content=content; this.type=type; this.createdAt=LocalDateTime.now();
    }

    public void addReaction(ReactionType t, int uid)    { reactions.get(t).add(uid); }
    public void removeReaction(ReactionType t, int uid) { reactions.get(t).remove(uid); }
    public int  getReactionCount(ReactionType t)        { return reactions.get(t).size(); }
    public int  getTotalReactions()                     { return reactions.values().stream().mapToInt(Set::size).sum(); }
    public void addComment(Comment c)                   { comments.add(c); }
    public void addTag(String t)                        { tags.add(t.toLowerCase()); }
    public boolean hasTag(String t)                     { return tags.contains(t.toLowerCase()); }

    public <T extends Comment> List<T> filterComments(List<T> list, Comment.CommentType type) {
        List<T> r = new ArrayList<>();
        for (T c : list) if (c.getCommentType()==type) r.add(c);
        return r;
    }

    public int    getId()                         { return id; }
    public void   setId(int id)                   { this.id=id; }
    public int    getUserId()                     { return userId; }
    public void   setUserId(int u)                { this.userId=u; }
    public String getContent()                    { return content; }
    public void   setContent(String c)            { this.content=c; }
    public PostType getType()                     { return type; }
    public void   setType(PostType t)             { this.type=t; }
    public int    getLikesCount()                 { return likesCount; }
    public void   setLikesCount(int l)            { this.likesCount=l; }
    public LocalDateTime getCreatedAt()           { return createdAt; }
    public void   setCreatedAt(LocalDateTime dt)  { this.createdAt=dt; }
    public List<Comment> getComments()            { return Collections.unmodifiableList(comments); }
    public Set<String>   getTags()                { return Collections.unmodifiableSet(tags); }
    public Map<ReactionType,Set<Integer>> getReactions() { return Collections.unmodifiableMap(reactions); }

    @Override public String toString() {
        return "Post{id="+id+", userId="+userId+", type="+type
                +", reactions="+getTotalReactions()+", comments="+comments.size()+"}";
    }
}