package repository;
import exception.DatabaseException;
import model.*;
import util.DatabaseConfig;
import java.util.*;
public class MessageRepository {
    protected final DatabaseConfig db;
    public MessageRepository(DatabaseConfig db) { this.db = db; }
    public int save(Message m)                                    throws DatabaseException { return 1; }
    public Optional<Message> findById(int id)                     throws DatabaseException { return Optional.empty(); }
    public List<Message> findConversation(int a,int b)            throws DatabaseException { return new ArrayList<>(); }
    public Map<Integer,Conversation> findAllConversations(int id) throws DatabaseException { return new HashMap<>(); }
    public void markAsRead(int from,int to)                       throws DatabaseException {}
    public int countUnread(int id)                                throws DatabaseException { return 0; }
    public List<Message> searchByContent(int id,String k)         throws DatabaseException { return new ArrayList<>(); }
    public void delete(int id)                                    throws DatabaseException {}
}
