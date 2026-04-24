package repository;
import exception.DatabaseException;
import model.*;
import util.DatabaseConfig;
import java.util.*;
public class ResourceRepository {
    protected final DatabaseConfig db;
    public ResourceRepository(DatabaseConfig db) { this.db = db; }
    public int save(Resource r)                              throws DatabaseException { return 1; }
    public Optional<Resource> findById(int id)               throws DatabaseException { return Optional.empty(); }
    public List<Resource> findByCourse(int id)               throws DatabaseException { return new ArrayList<>(); }
    public List<Resource> findByType(Resource.ResourceType t) throws DatabaseException { return new ArrayList<>(); }
    public List<Resource> searchByTitle(String k)            throws DatabaseException { return new ArrayList<>(); }
    public void saveRating(int r,int u,int s)                throws DatabaseException {}
    public void saveToLibrary(int r,int u)                   throws DatabaseException {}
    public void delete(int id)                               throws DatabaseException {}
}
