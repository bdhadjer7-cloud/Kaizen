package service;

import exception.*;
import model.*;
import repository.StudyRepository;
import security.SecurityManager;
import util.Cache;
import util.KaizenUtils;
import util.Validator;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

/**
 * Kaizen — StudyService.java (Person B)
 * Pomodoro sessions, study statistics, leaderboard.
 */
public class StudyService {

    private static final Logger LOG = Logger.getLogger(StudyService.class.getName());
    private static final int POMODORO_MINUTES = 25;

    private final StudyRepository studyRepo;
    private final SecurityManager security;
    private final BadgeService    badgeService;

    // Cache leaderboard for 1 minute
    private final Cache<String, List<Map<String, Object>>> leaderboardCache
            = new Cache<>(60_000);

    public StudyService(StudyRepository studyRepo, SecurityManager security,
                        BadgeService badgeService) {
        this.studyRepo    = studyRepo;
        this.security     = security;
        this.badgeService = badgeService;
    }

    // ── Record study session ───────────────────────────────────────────────────
    public StudySession recordSession(String token, int courseId, int durationMinutes)
            throws KaizenException {

        User user = security.getUserFromToken(token);

        if (durationMinutes <= 0 || durationMinutes > 480) {
            ValidationException e = new ValidationException();
            e.addError("duration","Duration must be between 1 and 480 minutes.");
            throw e;
        }

        StudySession session = new StudySession(0, user.getId(),
                courseId, durationMinutes);
        session.setPomodoro(durationMinutes == POMODORO_MINUTES);

        try {
            int id = studyRepo.save(session);
            session.setId(id);
            leaderboardCache.invalidate("global");
            leaderboardCache.invalidate("course_" + courseId);

            // Check badges after each session
            badgeService.checkAndAward(user.getId(), session);

            LOG.info("[STUDY] Session: " + user.getEmail()
                    + " " + durationMinutes + "min course=" + courseId);
            return session;
        } catch (DatabaseException e) { throw e; }
    }

    // ── Get study stats ────────────────────────────────────────────────────────
    public Map<String, Object> getStats(String token) throws KaizenException {
        User user = security.getUserFromToken(token);
        try {
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("todayMinutes",    studyRepo.getTodayMinutes(user.getId()));
            stats.put("weekMinutes",     studyRepo.getWeekMinutes(user.getId()));
            stats.put("totalMinutes",    studyRepo.getTotalMinutes(user.getId()));
            stats.put("todayHours",      String.format("%.1fh",
                    (int) stats.get("todayMinutes") / 60.0));
            stats.put("weekHours",       String.format("%.1fh",
                    (int) stats.get("weekMinutes")  / 60.0));
            stats.put("streak",          getDailyStreak(user.getId()));
            stats.put("pomodorosToday",  studyRepo.getPomodoroCount(user.getId(),
                    LocalDate.now()));
            stats.put("byCourse",        studyRepo.getMinutesByCourse(user.getId()));
            return stats;
        } catch (DatabaseException e) { throw e; }
    }

    // ── Get leaderboard ────────────────────────────────────────────────────────
    public List<Map<String, Object>> getLeaderboard(String token, String filter)
            throws KaizenException {
        User user = security.getUserFromToken(token);

        // Check cache
        String cacheKey = "leaderboard_" + filter;
        Optional<List<Map<String, Object>>> cached = leaderboardCache.get(cacheKey);
        if (cached.isPresent()) return cached.get();

        try {
            List<Map<String, Object>> board = studyRepo.getWeeklyLeaderboard(filter);
            leaderboardCache.put(cacheKey, board);
            return board;
        } catch (DatabaseException e) { throw e; }
    }

    // ── Daily streak ───────────────────────────────────────────────────────────
    public int getDailyStreak(int userId) throws DatabaseException {
        try {
            List<LocalDate> studyDays = studyRepo.getStudyDays(userId);
            if (studyDays.isEmpty()) return 0;

            studyDays = KaizenUtils.sortBy(studyDays,
                    d -> d.toEpochDay(), true); // newest first

            int streak = 0;
            LocalDate expected = LocalDate.now();
            for (LocalDate day : studyDays) {
                if (day.equals(expected)) {
                    streak++;
                    expected = expected.minusDays(1);
                } else break;
            }
            return streak;
        } catch (DatabaseException e) { throw e; }
    }

    // ── Get sessions by course ─────────────────────────────────────────────────
    public List<StudySession> getSessionsByCourse(String token, int courseId)
            throws KaizenException {
        User user = security.getUserFromToken(token);
        Validator.positiveId(courseId, "courseId");
        try { return studyRepo.findByStudentAndCourse(user.getId(), courseId); }
        catch (DatabaseException e) { throw e; }
    }
}