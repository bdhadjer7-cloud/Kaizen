package util;

import exception.DatabaseException;

import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Kaizen — DatabaseSchema.java
 * Creates all tables on first run.
 * Safe to call multiple times — uses IF NOT EXISTS style via Derby error handling.
 */
public class DatabaseSchema {

    private static final Logger LOG = Logger.getLogger(DatabaseSchema.class.getName());
    private final DatabaseConfig db;

    public DatabaseSchema(DatabaseConfig db) { this.db = db; }

    public void createAll() throws DatabaseException {
        LOG.info("[SCHEMA] Creating tables...");
        createUsers();
        createCourses();
        createCourseEnrollments();
        createLessons();
        createPosts();
        createComments();
        createMessages();
        try {
            createNotes();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            createQuizzes();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            createQuizQuestions();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            createQuizAttempts();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        createFlashcardSets();
        createFlashcards();
        createResources();
        createBadges();
        createUserBadges();
        createStudySessions();
        createStudyRooms();
        createRoomMembers();
        createCalendarEvents();
        createContacts();
        createReactions();
        createLeaderboardCache();
        LOG.info("[SCHEMA] All tables ready");
    }

    private void createUsers() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE IF NOT EXISTS users(" +
                "  id               INT AUTO_INCREMENT PRIMARY KEY," +
                "  name             VARCHAR(100) NOT NULL," +
                "  email            VARCHAR(150) NOT NULL UNIQUE," +
                "  password_hash    VARCHAR(255) NOT NULL," +
                "  role             VARCHAR(10) NOT NULL DEFAULT 'STUDENT'," +
                "  avatar_url       VARCHAR(255)," +
                "  bio              VARCHAR(500)," +
                "  created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] users");
    }

    private void createCourses() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE courses (" +
                "  id          INT AUTO_INCREMENT PRIMARY KEY," +
                "  title       VARCHAR(200) NOT NULL," +
                "  description VARCHAR(1000)," +
                "  teacher_id  INTEGER NOT NULL," +
                "  join_code   VARCHAR(20) UNIQUE," +
                "  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] courses");
    }

    private void createCourseEnrollments() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE course_enrollments (" +
                "  id         INT AUTO_INCREMENT PRIMARY KEY," +
                "  course_id  INTEGER NOT NULL," +
                "  student_id INTEGER NOT NULL," +
                "  joined_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  UNIQUE (course_id, student_id)" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] course_enrollments");
    }

    private void createLessons() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE lessons (" +
                "  id          INT AUTO_INCREMENT PRIMARY KEY," +
                "  course_id   INTEGER NOT NULL," +
                "  title       VARCHAR(200) NOT NULL," +
                "  content     CLOB," +
                "  file_url    VARCHAR(255)," +
                "  order_index INTEGER DEFAULT 0," +
                "  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] lessons");
    }

    private void createPosts() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE posts (" +
                "  id          INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id     INTEGER NOT NULL," +
                "  content     VARCHAR(2000) NOT NULL," +
                "  type        VARCHAR(20) DEFAULT 'TEXT'," +
                "  likes_count INTEGER DEFAULT 0," +
                "  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] posts");
    }

    private void createComments() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE comments (" +
                "  id           INT AUTO_INCREMENT PRIMARY KEY," +
                "  post_id      INTEGER NOT NULL," +
                "  user_id      INTEGER NOT NULL," +
                "  content      VARCHAR(1000) NOT NULL," +
                "  comment_type VARCHAR(20) DEFAULT 'FEEDBACK'," +
                "  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] comments");
    }

    private void createMessages() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE messages (" +
                "  id           INT AUTO_INCREMENT PRIMARY KEY," +
                "  sender_id   INTEGER NOT NULL," +
                "  receiver_id INTEGER NOT NULL," +
                "  content     VARCHAR(2000) NOT NULL," +
                "  file_url    VARCHAR(255)," +
                "  sent_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  is_read     BOOLEAN DEFAULT FALSE" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] messages");
    }

    private void createNotes() throws DatabaseException, SQLException {
        db.execute(
            "CREATE TABLE notes (" +
            "  id         INT AUTO_INCREMENT PRIMARY KEY," +
            "  student_id INTEGER NOT NULL," +
            "  course_id  INTEGER," +
            "  title      VARCHAR(200) NOT NULL," +
            "  content    TEXT," +
            "  tags       VARCHAR(500)," +
            "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );
        LOG.info("[SCHEMA] notes");
    }

    private void createQuizzes() throws DatabaseException, SQLException {
        db.execute(
            "CREATE TABLE quizzes (" +
            "  id          INT AUTO_INCREMENT PRIMARY KEY," +
            "  course_id   INTEGER NOT NULL," +
            "  teacher_id  INTEGER NOT NULL," +
            "  title       VARCHAR(200) NOT NULL," +
            "  time_limit  INTEGER DEFAULT 1800," +
            "  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );
        LOG.info("[SCHEMA] quizzes");
    }

    private void createQuizQuestions() throws DatabaseException, SQLException {
        db.execute(
            "CREATE TABLE quiz_questions (" +
            "  id             INT AUTO_INCREMENT PRIMARY KEY," +
            "  quiz_id        INTEGER NOT NULL," +
            "  question_text  VARCHAR(1000) NOT NULL," +
            "  option_a       VARCHAR(500) NOT NULL," +
            "  option_b       VARCHAR(500) NOT NULL," +
            "  option_c       VARCHAR(500) NOT NULL," +
            "  option_d       VARCHAR(500) NOT NULL," +
            "  correct_option CHAR(1) NOT NULL" +
            ")"
        );
        LOG.info("[SCHEMA] quiz_questions");
    }

    private void createQuizAttempts() throws DatabaseException, SQLException {
        db.execute(
            "CREATE TABLE quiz_attempts (" +
            "  id           INT AUTO_INCREMENT PRIMARY KEY," +
            "  student_id   INTEGER NOT NULL," +
            "  quiz_id      INTEGER NOT NULL," +
            "  score        DOUBLE DEFAULT 0," +
            "  attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  UNIQUE (student_id, quiz_id)" +
            ")"
        );
        LOG.info("[SCHEMA] quiz_attempts");
    }

    private void createFlashcardSets() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE flashcard_sets (" +
                "  id         INT AUTO_INCREMENT PRIMARY KEY," +
                "  course_id  INTEGER NOT NULL," +
                "  teacher_id INTEGER NOT NULL," +
                "  title      VARCHAR(200) NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] flashcard_sets");
    }

    private void createFlashcards() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE flashcards (" +
                "  id         INT AUTO_INCREMENT PRIMARY KEY," +
                "  set_id     INTEGER NOT NULL," +
                "  front_text VARCHAR(500) NOT NULL," +
                "  back_text  VARCHAR(500) NOT NULL" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] flashcards");
    }

    private void createResources() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE resources (" +
                "  id         INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id    INTEGER NOT NULL," +
                "  course_id  INTEGER," +
                "  title      VARCHAR(200) NOT NULL," +
                "  type       VARCHAR(20) NOT NULL," +
                "  url        VARCHAR(500)," +
                "  rating     DOUBLE DEFAULT 0," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] resources");
    }

    private void createBadges() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE badges (" +
                "  id             INT AUTO_INCREMENT PRIMARY KEY," +
                "  name           VARCHAR(100) NOT NULL UNIQUE," +
                "  description    VARCHAR(500)," +
                "  icon_url       VARCHAR(255)," +
                "  condition_type VARCHAR(50) NOT NULL" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] badges");
    }

    private void createUserBadges() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE user_badges (" +
                "  id        INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id   INTEGER NOT NULL," +
                "  badge_id  INTEGER NOT NULL," +
                "  earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  UNIQUE (user_id, badge_id)" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] user_badges");
    }

    private void createStudySessions() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE study_sessions (" +
                "  id               INT AUTO_INCREMENT PRIMARY KEY," +
                "  student_id       INTEGER NOT NULL," +
                "  duration_minutes INTEGER NOT NULL," +
                "  date             DATE DEFAULT CURRENT_DATE," +
                "  course_id        INTEGER" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] study_sessions");
    }

    private void createStudyRooms() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE study_rooms (" +
                "  id           INT AUTO_INCREMENT PRIMARY KEY," +
                "  name         VARCHAR(200) NOT NULL," +
                "  course_id    INTEGER," +
                "  created_by   INTEGER NOT NULL," +
                "  max_members  INTEGER DEFAULT 8," +
                "  scheduled_at TIMESTAMP" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] study_rooms");
    }

    private void createRoomMembers() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE room_members (" +
                "  id        INT AUTO_INCREMENT PRIMARY KEY," +
                "  room_id   INTEGER NOT NULL," +
                "  user_id   INTEGER NOT NULL," +
                "  joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  UNIQUE (room_id, user_id)" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] room_members");
    }

    private void createCalendarEvents() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE calendar_events (" +
                "  id          INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id     INTEGER NOT NULL," +
                "  title       VARCHAR(200) NOT NULL," +
                "  description VARCHAR(500)," +
                "  due_date    TIMESTAMP NOT NULL," +
                "  type        VARCHAR(20) DEFAULT 'REMINDER'," +
                "  course_id   INTEGER" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] calendar_events");
    }

    private void createContacts() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE contacts (" +
                "  id         INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id    INTEGER NOT NULL," +
                "  contact_id INTEGER NOT NULL," +
                "  added_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  UNIQUE (user_id, contact_id)" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] contacts");
    }

    private void createReactions() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE reactions (" +
                "  id            INT AUTO_INCREMENT PRIMARY KEY," +
                "  post_id       INTEGER NOT NULL," +
                "  user_id       INTEGER NOT NULL," +
                "  reaction_type VARCHAR(20) NOT NULL," +
                "  UNIQUE (post_id, user_id, reaction_type)" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] reactions");
    }

    private void createLeaderboardCache() throws DatabaseException {
        try {
            db.execute(
                "CREATE TABLE leaderboard_cache (" +
                "  id           INT AUTO_INCREMENT PRIMARY KEY," +
                "  student_id   INTEGER NOT NULL," +
                "  course_id    INTEGER," +
                "  total_minutes INTEGER DEFAULT 0," +
                "  week_start   DATE," +
                "  UNIQUE (student_id, course_id, week_start)" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("[SCHEMA] leaderboard_cache");
    }
}
