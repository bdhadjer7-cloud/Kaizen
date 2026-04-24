package repository;
import exception.DatabaseException;
import model.CalendarEvent;
import util.DatabaseConfig;
import java.time.*;
import java.util.*;
public class CalendarRepository {
    protected final DatabaseConfig db;
    public CalendarRepository(DatabaseConfig db) { this.db = db; }
    public int save(CalendarEvent e)                          throws DatabaseException { return 1; }
    public Optional<CalendarEvent> findById(int id)           throws DatabaseException { return Optional.empty(); }
    public List<CalendarEvent> findByUser(int id)             throws DatabaseException { return new ArrayList<>(); }
    public List<CalendarEvent> findDueTomorrow(LocalDateTime d) throws DatabaseException { return new ArrayList<>(); }
    public void delete(int id)                                throws DatabaseException {}
}
