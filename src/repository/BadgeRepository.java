package repository;
import exception.DatabaseException;
import model.*;
import util.DatabaseConfig;
import java.util.*;
public class BadgeRepository {
    protected final DatabaseConfig db;
    public BadgeRepository(DatabaseConfig db) { this.db = db; }
    public int save(Badge b)                                    throws DatabaseException { return 1; }
    public List<Badge> findAll()                                throws DatabaseException { return new ArrayList<>(); }
    public List<Badge> findByUser(int id)                       throws DatabaseException { return new ArrayList<>(); }
    public void awardBadge(UserBadge ub)                        throws DatabaseException {}
    public Set<Badge.ConditionType> getEarnedConditions(int id) throws DatabaseException { return new HashSet<>(); }
    public Map<String,Integer> getUserStats(int id)             throws DatabaseException { return new HashMap<>(); }
}
