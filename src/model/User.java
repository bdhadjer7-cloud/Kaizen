package model;

import java.time.LocalDateTime;
import java.util.*;

public class User {
    public enum Role { STUDENT, TEACHER }

    //DB column annotation used by ReflectionMapper
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(java.lang.annotation.ElementType.FIELD)
    public @interface Column {
        String name(); // maps field → DB column name
    }

    //Fields
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "role")
    private Role role;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "bio")
    private String bio;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    //Set<Integer> — course IDs this user is enrolled in. Set = no duplicates O(1) contains check
    private Set<Integer> enrolledCourseIds = new HashSet<>();

    //List<String> — badge names earned by this user ordered by earn date
    private List<String> earnedBadges = new ArrayList<>();

    //Map<String, Object> — flexible metadata stored Used for extra profile fields without DB schema changes.e.g. {"university": "Oran", "github": "url", "streak": 7}

    private Map<String, Object> metadata = new HashMap<>();

    //Constructors
    //Default — required for ReflectionMapper
    public User() {}

    // Full constructor — used when loading a complete row from DB
    public User(int id, String name, String email,
                String passwordHash, Role role,
                String avatarUrl, String bio,
                LocalDateTime createdAt) {
        this.id           = id;
        this.name         = name;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.avatarUrl    = avatarUrl;
        this.bio          = bio;
        this.createdAt    = createdAt;
    }

    // Registration constructor — id + createdAt assigned by DB
    public User(String name, String email, String passwordHash, Role role) {
        this.name         = name;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.role         = role;
    }

    //Generics — typed collection methods

    //Generic method: add any value to metadata map <T> makes it flexible — store String, Integer, Boolean, etc.
    public <T> void setMeta(String key, T value) {
        metadata.put(key, value);
    }

    //Generic getter with cast — retrieves typed metadata e.g. String uni = user.getMeta("university", String.class);
    public <T> T getMeta(String key, Class<T> type) {
        Object val = metadata.get(key);
        if (val == null) return null;
        return type.cast(val);
    }

    //Course enrollment helpers

    public void enrollIn(int courseId) {
        enrolledCourseIds.add(courseId);
    }

    public void unenrollFrom(int courseId) {
        enrolledCourseIds.remove(courseId);
    }

    public boolean isEnrolledIn(int courseId) {
        return enrolledCourseIds.contains(courseId);
    }

    public Set<Integer> getEnrolledCourseIds() {
        return Collections.unmodifiableSet(enrolledCourseIds);
    }

    //Badge helpers

    public void awardBadge(String badgeName) {
        if (!earnedBadges.contains(badgeName)) {
            earnedBadges.add(badgeName);
        }
    }

    public boolean hasBadge(String badgeName) {
        return earnedBadges.contains(badgeName);
    }

    public List<String> getEarnedBadges() {
        return Collections.unmodifiableList(earnedBadges);
    }

    //UI helpers
    //Returns initials for the avatar circle "Saffih Bouchra" → "SB"
    public String getInitials() {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1)
                + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    public boolean isTeacher() { return Role.TEACHER.equals(role); }
    public boolean isStudent() { return Role.STUDENT.equals(role); }
    public String getRoleLabel() { return isTeacher() ? "Teacher" : "Student"; }

    //Getters & Setters
    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }

    public String getEmail()                    { return email; }
    public void setEmail(String email)          { this.email = email; }

    public String getPasswordHash()             { return passwordHash; }
    public void setPasswordHash(String h)       { this.passwordHash = h; }

    public Role getRole()                       { return role; }
    public void setRole(Role role)              { this.role = role; }

    public String getAvatarUrl()                { return avatarUrl; }
    public void setAvatarUrl(String url)        { this.avatarUrl = url; }

    public String getBio()                      { return bio; }
    public void setBio(String bio)              { this.bio = bio; }

    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime dt)  { this.createdAt = dt; }

    public Map<String, Object> getMetadata()    { return metadata; }
    public void setEnrolledCourseIds(Set<Integer> ids) { this.enrolledCourseIds = ids; }
    public void setEarnedBadges(List<String> badges)   { this.earnedBadges = badges; }

    //toString / equals / hashCode
    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', role=" + role
                + ", courses=" + enrolledCourseIds.size()
                + ", badges=" + earnedBadges.size() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return id == ((User) o).id;
    }

    @Override
    public int hashCode() { return Integer.hashCode(id); }
}