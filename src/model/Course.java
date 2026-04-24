package model;

import java.time.LocalDateTime;
import java.util.*;

public class Course {

    //@Column annotation
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(java.lang.annotation.ElementType.FIELD)
    public @interface Column { String name(); }

    //Fields
    @Column(name = "id")          private int    id;
    @Column(name = "title")       private String title;
    @Column(name = "description") private String description;
    @Column(name = "teacher_id")  private int    teacherId;
    @Column(name = "join_code")   private String joinCode;
    @Column(name = "created_at")  private LocalDateTime createdAt;

    //Complex Data Structures
    private Map<Integer, Lesson> lessons = new LinkedHashMap<>();
    private Set<Integer> enrolledStudentIds = new HashSet<>();
    private List<String> announcements = new ArrayList<>();
    private Set<String> tags = new LinkedHashSet<>();

    // Bidirectional Graph for O(1) lookups in both directions
    // 1. What does this lesson unlock? (prerequisiteId -> set of targetIds)
    private Map<Integer, Set<Integer>> nextLessonsMap = new HashMap<>();

    // 2. What does this lesson require? (targetId -> set of prerequisiteIds)
    private Map<Integer, Set<Integer>> requiredPrereqsMap = new HashMap<>();

    //Constructors
    public Course() {}

    public Course(int id, String title, String description,
                  int teacherId, String joinCode) {
        this.id = id; this.title = title;
        this.description = description;
        this.teacherId = teacherId;
        this.joinCode = joinCode;
        this.createdAt = LocalDateTime.now();
    }

    //Lesson management
    public void addLesson(Lesson lesson) {
        lessons.put(lesson.getId(), lesson);
        nextLessonsMap.putIfAbsent(lesson.getId(), new HashSet<>());
        requiredPrereqsMap.putIfAbsent(lesson.getId(), new HashSet<>());
    }

    public Lesson getLessonById(int lessonId) {
        return lessons.get(lessonId);
    }

    public List<Lesson> getAllLessons() {
        return new ArrayList<>(lessons.values()); // Defensive copy
    }

    //Student Progress Calculation

    //Calculate specific student's progress ratio 0.0 -> 1.0
    public double getStudentProgressRatio(Set<Integer> studentCompletedLessonIds) {
        if (lessons.isEmpty()) return 0.0;

        // Count how many of the completed IDs actually belong to THIS course
        long validCompletions = studentCompletedLessonIds.stream()
                .filter(lessons::containsKey)
                .count();

        return (double) validCompletions / lessons.size();
    }

    //Display progress like "65%" based on student progress
    public String getStudentProgressPercent(Set<Integer> studentCompletedLessonIds) {
        return (int)(getStudentProgressRatio(studentCompletedLessonIds) * 100) + "%";
    }

    //O(1) Bidirectional Graph
    public void addPrerequisite(int prerequisiteId, int targetLessonId) {
        nextLessonsMap.computeIfAbsent(prerequisiteId, k -> new HashSet<>()).add(targetLessonId);
        requiredPrereqsMap.computeIfAbsent(targetLessonId, k -> new HashSet<>()).add(prerequisiteId);
    }

    public Set<Integer> getUnlockedAfter(int lessonId) {
        return Collections.unmodifiableSet(nextLessonsMap.getOrDefault(lessonId, Collections.emptySet()));
    }

    //O(1) check if a student has completed all required dependencies.
    public boolean canAccessLesson(int lessonId, Set<Integer> studentCompletedLessonIds) {
        Set<Integer> requirements = requiredPrereqsMap.getOrDefault(lessonId, Collections.emptySet());
        return studentCompletedLessonIds.containsAll(requirements);
    }

    //Kahn's Algorithm (Topological Sort).Correctly orders lessons accounting for all complex dependency chains.
    public List<Integer> getLessonOrderTopological() {
        Map<Integer, Integer> inDegrees = new HashMap<>();

        // 1. Initialize all nodes
        for (int lessonId : lessons.keySet()) {
            inDegrees.put(lessonId, 0);
        }

        //Count dependencies (in-degrees)
        for (Set<Integer> targets : nextLessonsMap.values()) {
            for (int target : targets) {
                inDegrees.put(target, inDegrees.get(target) + 1);
            }
        }

        //Queue nodes with 0 prerequisites
        Queue<Integer> queue = new LinkedList<>();
        for (Map.Entry<Integer, Integer> entry : inDegrees.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        //Sort
        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int current = queue.poll();
            order.add(current);

            for (int next : nextLessonsMap.getOrDefault(current, Collections.emptySet())) {
                inDegrees.put(next, inDegrees.get(next) - 1);
                if (inDegrees.get(next) == 0) {
                    queue.add(next); // Unlocked!
                }
            }
        }

        return order;
    }

    //Enrollment
    public void enrollStudent(int studentId)   { enrolledStudentIds.add(studentId); }
    public void unenrollStudent(int studentId) { enrolledStudentIds.remove(studentId); }
    public boolean hasStudent(int studentId)   { return enrolledStudentIds.contains(studentId); }
    public int getEnrollmentCount()            { return enrolledStudentIds.size(); }
    public Set<Integer> getEnrolledStudentIds(){ return Collections.unmodifiableSet(enrolledStudentIds); }

    //Announcements
    public void addAnnouncement(String msg)    { announcements.add(0, msg); }
    public List<String> getAnnouncements()     { return Collections.unmodifiableList(announcements); }

    //Tags & Utility
    public void addTag(String tag)             { tags.add(tag.toLowerCase()); }
    public boolean hasTag(String tag)          { return tags.contains(tag.toLowerCase()); }
    public Set<String> getTags()               { return Collections.unmodifiableSet(tags); }

    //Encapsulated Getters & Setters
    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }
    public String getTitle()       { return title; }
    public void   setTitle(String t){ this.title = t; }
    public String getDescription() { return description; }
    public void   setDescription(String d) { this.description = d; }
    public int    getTeacherId()   { return teacherId; }
    public void   setTeacherId(int t) { this.teacherId = t; }
    public String getJoinCode()    { return joinCode; }
    public void   setJoinCode(String c) { this.joinCode = c; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime dt) { this.createdAt = dt; }

    @Override public String toString() {
        return "Course{id=" + id + ", title='" + title + "', students="
                + enrolledStudentIds.size() + ", lessons=" + lessons.size() + "}";
    }
}