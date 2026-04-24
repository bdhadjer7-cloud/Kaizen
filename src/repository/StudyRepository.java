package repository;
import exception.DatabaseException;
import model.StudySession;
import util.DatabaseConfig;
import java.time.*;
import java.util.*;
public class StudyRepository {
    protected final DatabaseConfig db;
    public StudyRepository(DatabaseConfig db) { this.db = db; }
    public int save(StudySession s)                                  throws DatabaseException { return 1; }
    public int getTodayMinutes(int id)                               throws DatabaseException { return 0; }
    public int getWeekMinutes(int id)                                throws DatabaseException { return 0; }
    public int getTotalMinutes(int id)                               throws DatabaseException { return 0; }
    public int getPomodoroCount(int id,LocalDate d)                  throws DatabaseException { return 0; }
    public Map<Integer,Integer> getMinutesByCourse(int id)           throws DatabaseException { return new HashMap<>(); }
    public List<LocalDate> getStudyDays(int id)                      throws DatabaseException { return new ArrayList<>(); }
    public List<StudySession> findByStudentAndCourse(int s,int c)    throws DatabaseException { return new ArrayList<>(); }
    public List<Map<String,Object>> getWeeklyLeaderboard(String f)   throws DatabaseException { return new ArrayList<>(); }
}
