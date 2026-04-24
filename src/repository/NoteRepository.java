package repository;
import exception.DatabaseException;
import model.Note;
import util.DatabaseConfig;
import java.util.*;
public class NoteRepository {
    protected final DatabaseConfig db;
    public NoteRepository(DatabaseConfig db) { this.db = db; }
    public int save(Note n)                                  throws DatabaseException { return 1; }
    public Optional<Note> findById(int id)                   throws DatabaseException { return Optional.empty(); }
    public List<Note> findByStudent(int id)                  throws DatabaseException { return new ArrayList<>(); }
    public List<Note> findByStudentAndCourse(int s,int c)    throws DatabaseException { return new ArrayList<>(); }
    public int update(Note n)                                throws DatabaseException { return 1; }
    public void delete(int id)                               throws DatabaseException {}
}
