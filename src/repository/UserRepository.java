package repository;
import exception.DatabaseException;
import model.User;
import util.DatabaseConfig;
public class UserRepository {
    protected final DatabaseConfig db;
    public UserRepository(DatabaseConfig db) { this.db = db; }
    public User findByEmail(String e)          throws DatabaseException { return null; }
    public int  save(User u)                   throws DatabaseException { return 1; }
    public int  update(User u)                 throws DatabaseException { return 0; }
    public boolean existsByEmail(String e)     throws DatabaseException { return false; }
}
