package repository;
import exception.DatabaseException;
import model.*;
import util.DatabaseConfig;
import java.util.*;
public class PostRepository {
    protected final DatabaseConfig db;
    public PostRepository(DatabaseConfig db) { this.db = db; }
    public int save(Post p)                             throws DatabaseException { return 1; }
    public Optional<Post> findById(int id)              throws DatabaseException { return Optional.empty(); }
    public List<Post> findAll()                         throws DatabaseException { return new ArrayList<>(); }
    public List<Post> findTrending()                    throws DatabaseException { return new ArrayList<>(); }
    public List<Post> searchByContent(String k)         throws DatabaseException { return new ArrayList<>(); }
    public int saveComment(Comment c)                   throws DatabaseException { return 1; }
    public void saveReaction(int p,int u,Post.ReactionType r) throws DatabaseException {}
    public void delete(int id)                          throws DatabaseException {}
}
