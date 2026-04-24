package repository;
import exception.DatabaseException;
import model.*;
import util.DatabaseConfig;
import java.util.*;
public class QuizRepository {
    protected final DatabaseConfig db;
    public QuizRepository(DatabaseConfig db) { this.db = db; }
    public int save(Quiz q)                              throws DatabaseException { return 1; }
    public Optional<Quiz> findById(int id)               throws DatabaseException { return Optional.empty(); }
    public List<Quiz> findByCourse(int id)               throws DatabaseException { return new ArrayList<>(); }
    public int saveQuestion(QuizQuestion q)              throws DatabaseException { return 1; }
    public int saveAttempt(QuizAttempt a)                throws DatabaseException { return 1; }
    public boolean hasAttempted(int s,int q)             throws DatabaseException { return false; }
}
