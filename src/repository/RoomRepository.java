package repository;
import exception.DatabaseException;
import model.*;
import util.DatabaseConfig;
import java.util.*;
public class RoomRepository {
    protected final DatabaseConfig db;
    public RoomRepository(DatabaseConfig db) { this.db = db; }
    public int save(StudyRoom r)                         throws DatabaseException { return 1; }
    public Optional<StudyRoom> findById(int id)          throws DatabaseException { return Optional.empty(); }
    public List<StudyRoom> findAll()                     throws DatabaseException { return new ArrayList<>(); }
    public void addMember(RoomMember m)                  throws DatabaseException {}
    public void removeMember(int r,int u)                throws DatabaseException {}
    public boolean isMember(int r,int u)                 throws DatabaseException { return false; }
    public List<RoomMember> getMembers(int id)           throws DatabaseException { return new ArrayList<>(); }
    public void closeRoom(int id)                        throws DatabaseException {}
}
