package service;

import model.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Provides demo/sample data for the JavaFX frontend.
 * Used when no database is connected, so the UI can be tested standalone.
 */
public class DemoDataService {

    private static DemoDataService instance;
    private User currentUser;
    private final List<Course> courses = new ArrayList<>();
    private final List<Post> posts = new ArrayList<>();
    private final List<Message> messages = new ArrayList<>();
    private final List<StudyRoom> rooms = new ArrayList<>();
    private final List<Resource> resources = new ArrayList<>();
    private final List<Note> notes = new ArrayList<>();
    private final Map<String, String> passwords = new HashMap<>();

    private DemoDataService() {
        initDemoData();
    }

    public static DemoDataService getInstance() {
        if (instance == null) instance = new DemoDataService();
        return instance;
    }

    private void initDemoData() {
        // Demo user
        currentUser = new User(1, "Saffih Bouchra", "bouchra@univ-oran.dz",
                "hashed", User.Role.STUDENT, null,
                "Computer science student passionate about building collaborative study tools.\nCo-creator of Kaizen with Hadjer -- because studying alone is overrated.",
                LocalDateTime.now());
        currentUser.setMeta("university", "University of Oran");
        currentUser.setMeta("totalXp", 2840);
        currentUser.setMeta("dayStreak", 7);
        currentUser.setMeta("rank", "Scholar");
        currentUser.setMeta("username", "bouchra");
        currentUser.awardBadge("First Flame");
        currentUser.awardBadge("Focus Master");
        currentUser.awardBadge("Bookworm");
        passwords.put("bouchra@univ-oran.dz", "password123");

        // Courses
        Course la = new Course(1, "Linear Algebra",
                "Vectors, matrices, eigenvalues and their real-world applications in computer science.",
                100, "LA2025");
        la.addLesson(new Lesson(1, 1, "Vectors & Spaces", "Introduction to vector spaces..."));
        la.addLesson(new Lesson(2, 1, "Vector Operations", "Addition, scalar multiplication..."));
        la.addLesson(new Lesson(3, 1, "Matrix Operations", "Matrix multiplication, transpose..."));
        la.addLesson(new Lesson(4, 1, "Determinants", "Computing determinants..."));
        la.addLesson(new Lesson(5, 1, "Eigenvalues & Eigenvectors",
                "An eigenvector of a square matrix A is a non-zero vector v such that multiplying A by v only changes the scale of v, never its direction. The scalar lambda by which it is scaled is called the eigenvalue.\n\nCore Definition: A*v = lambda*v\n\nThink of it this way: most vectors get rotated when you multiply them by a matrix. Eigenvectors are special -- they only get stretched or compressed, never tilted."));
        la.addLesson(new Lesson(6, 1, "Characteristic Polynomial", "Finding eigenvalues using det(A - lambda*I) = 0..."));
        la.addLesson(new Lesson(7, 1, "Diagonalization", "Diagonalizing matrices..."));
        courses.add(la);

        Course wd = new Course(2, "Web Development Fundamentals",
                "HTML, CSS, JavaScript and building modern responsive interfaces.",
                101, "WD2025");
        for (int i = 1; i <= 20; i++) {
            wd.addLesson(new Lesson(20 + i, 2, "Web Lesson " + i, "Content for lesson " + i));
        }
        courses.add(wd);

        Course dsa = new Course(3, "Data Structures & Algorithms",
                "Trees, graphs, sorting and searching -- the foundations of CS problem solving.",
                102, "DSA2025");
        for (int i = 1; i <= 20; i++) {
            dsa.addLesson(new Lesson(50 + i, 3, "DSA Lesson " + i, "Content for lesson " + i));
        }
        courses.add(dsa);

        // Posts
        Post p1 = new Post(1, 2, "Can someone clarify the difference between eigenvalues and singular values? I keep mixing them up when studying PCA. Are they ever the same?", Post.PostType.TEXT);
        p1.addTag("Question");
        p1.setLikesCount(12);
        p1.setCreatedAt(LocalDateTime.now().minusDays(1));
        posts.add(p1);

        Post p2 = new Post(2, 3, "Quiz Champion earned!\nScored 100% on Data Structures Quiz 2 - +50 XP", Post.PostType.TEXT);
        p2.addTag("Achievement");
        p2.setLikesCount(12);
        p2.setCreatedAt(LocalDateTime.now().minusDays(1));
        posts.add(p2);

        // Messages
        messages.add(new Message(1, 2, 1, "Did you finish the DFS?"));
        messages.add(new Message(2, 3, 1, "New course has been uploaded!"));
        messages.add(new Message(3, 4, 1, "Did you finish the DFS?"));
        messages.add(new Message(4, 5, 1, "I have completed all the chapters wbu?"));

        // Study Rooms
        StudyRoom r1 = new StudyRoom(1, "med students", 0, 1, 50, LocalDateTime.now());
        r1.setLive(true);
        rooms.add(r1);

        StudyRoom r2 = new StudyRoom(2, "IT & Tech", 0, 2, 100, LocalDateTime.now());
        r2.setLive(true);
        rooms.add(r2);

        StudyRoom r3 = new StudyRoom(3, "focus & achieve", 0, 3, 200, LocalDateTime.now());
        r3.setLive(true);
        rooms.add(r3);

        // Resources
        resources.add(new Resource(1, 1, 1, "Fiche TD TL", Resource.ResourceType.PDF, ""));
        resources.add(new Resource(2, 1, 0, "MinMax-- Agent", Resource.ResourceType.AI_TOOL, ""));

        // Notes
        notes.add(new Note(1, 1, 1, "Eigenvalues Formula",
                "det(A - lambda*I) = 0\nFor 2x2:\nlambda^2 - tr(A)*lambda + det(A) = 0"));
    }

    // Auth
    public User login(String email, String password) {
        String stored = passwords.get(email);
        if (stored != null && stored.equals(password)) {
            return currentUser;
        }
        return null;
    }

    public User register(String name, String email, String password, User.Role role) {
        User u = new User(99, name, email, "hashed", role, null, "", LocalDateTime.now());
        u.setMeta("username", name.toLowerCase().replace(" ", "."));
        u.setMeta("totalXp", 0);
        u.setMeta("dayStreak", 0);
        u.setMeta("rank", "Beginner");
        passwords.put(email, password);
        currentUser = u;
        return u;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User u) { this.currentUser = u; }
    public List<Course> getCourses() { return courses; }
    public List<Post> getPosts() { return posts; }
    public List<Message> getMessages() { return messages; }
    public List<StudyRoom> getRooms() { return rooms; }
    public List<Resource> getResources() { return resources; }
    public List<Note> getNotes() { return notes; }

    public String[] getContactNames() {
        return new String[]{"Bensaid Hadjer", "Sara Kaidi", "Ait abdessamie", "Dellali Fatima Zahra"};
    }

    public String[] getContactInitials() {
        return new String[]{"BH", "SK", "MA", "FZ"};
    }

    public String[] getContactColors() {
        return new String[]{"#4361EE", "#E07B4C", "#A0AEC0", "#7C5CFC"};
    }

    public boolean[] getContactOnline() {
        return new boolean[]{true, true, false, true};
    }
}
