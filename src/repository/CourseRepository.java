package repository;
import exception.DatabaseException;
import model.*;
import util.DatabaseConfig;
import java.util.*;
public class CourseRepository {
    protected final DatabaseConfig db;
    public CourseRepository(DatabaseConfig db) { this.db = db; }
    public Optional<Course> findById(int id)          throws DatabaseException { return Optional.empty(); }
    public Optional<Course> findByJoinCode(String c)  throws DatabaseException { return Optional.empty(); }
    public Map<Integer,Course> findAll()               throws DatabaseException { return new LinkedHashMap<>(); }
    public List<Course> findByTeacher(int id)          throws DatabaseException { return new ArrayList<>(); }
    public List<Course> findByStudent(int id)          throws DatabaseException { return new ArrayList<>(); }
    public List<Course> searchByTitle(String k)        throws DatabaseException { return new ArrayList<>(); }
    public int save(Course c)                          throws DatabaseException { return 1; }
    public int saveLesson(Lesson l)                    throws DatabaseException { return 1; }
    public boolean isEnrolled(int c, int s)            throws DatabaseException { return false; }
    public int enrollStudent(int c, int s)             throws DatabaseException { return 1; }
}
