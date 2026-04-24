package service;

import exception.*;
import exception.KaizenException.ErrorCode;
import model.*;
import repository.BadgeRepository;
import security.SecurityManager;

import java.util.*;
import java.util.logging.Logger;

/**
 * Kaizen — BadgeService.java
 * Auto-awards badges when conditions are met.
 */
public class BadgeService {

    private static final Logger LOG = Logger.getLogger(BadgeService.class.getName());

    private final BadgeRepository badgeRepo;
    private final SecurityManager security;

    // Map<ConditionType, Badge> — pre-loaded badge definitions
    private final Map<Badge.ConditionType, Badge> badgeMap = new EnumMap<>(Badge.ConditionType.class);

    public BadgeService(BadgeRepository badgeRepo, SecurityManager security)
            throws DatabaseException {
        this.badgeRepo = badgeRepo;
        this.security  = security;
        loadBadges();
    }

    private void loadBadges() throws DatabaseException {
        try {
            List<Badge> all = badgeRepo.findAll();
            for (Badge b : all) badgeMap.put(b.getConditionType(), b);
        } catch (DatabaseException e) { throw e; }
    }

    // ── Check and award badges ─────────────────────────────────────────────────
    /**
     * Generic method — called after any action that might trigger a badge.
     * Checks ALL conditions for the user and awards any newly met ones.
     */
    public <T> List<Badge> checkAndAward(int userId, T actionContext)
            throws DatabaseException {

        List<Badge> awarded = new ArrayList<>();

        // Get current stats from DB
        try {
            Set<Badge.ConditionType> earned = badgeRepo.getEarnedConditions(userId);
            Map<String, Integer> stats      = badgeRepo.getUserStats(userId);

            int postCount    = stats.getOrDefault("postCount",     0);
            int pomodoroCount= stats.getOrDefault("pomodoroCount", 0);
            int studyHours   = stats.getOrDefault("studyHours",    0);
            int quizPerfect  = stats.getOrDefault("quizPerfect",   0);
            int roomJoins    = stats.getOrDefault("roomJoins",     0);
            int streak       = stats.getOrDefault("streak",        0);
            int rank         = stats.getOrDefault("leaderboardRank",999);
            int answers      = stats.getOrDefault("answersGiven",  0);

            // Check each condition
            Map<Badge.ConditionType, Boolean> conditions = new LinkedHashMap<>();
            conditions.put(Badge.ConditionType.FIRST_POST,       postCount    >= 1);
            conditions.put(Badge.ConditionType.POMODORO_10,      pomodoroCount>= 10);
            conditions.put(Badge.ConditionType.STUDY_50H,        studyHours   >= 50);
            conditions.put(Badge.ConditionType.QUIZ_100_3X,      quizPerfect  >= 3);
            conditions.put(Badge.ConditionType.ROOM_5X,          roomJoins    >= 5);
            conditions.put(Badge.ConditionType.STREAK_7,         streak       >= 7);
            conditions.put(Badge.ConditionType.TOP_LEADERBOARD,  rank == 1);
            conditions.put(Badge.ConditionType.ANSWER_10,        answers      >= 10);

            for (Map.Entry<Badge.ConditionType, Boolean> entry : conditions.entrySet()) {
                Badge.ConditionType type = entry.getKey();
                if (entry.getValue() && !earned.contains(type)) {
                    Badge badge = badgeMap.get(type);
                    if (badge != null) {
                        UserBadge ub = new UserBadge(0, userId, badge.getId());
                        badgeRepo.awardBadge(ub);
                        awarded.add(badge);
                        LOG.info("[BADGE] Awarded: " + badge.getName() + " to user " + userId);
                    }
                }
            }
        } catch (DatabaseException e) { throw e; }

        return awarded;
    }

    // ── Get user badges ────────────────────────────────────────────────────────
    public List<Badge> getUserBadges(String token) throws KaizenException {
        User user = security.getUserFromToken(token);
        try { return badgeRepo.findByUser(user.getId()); }
        catch (DatabaseException e) { throw e; }
    }

    // ── Get all badges ─────────────────────────────────────────────────────────
    public List<Badge> getAllBadges(String token) throws KaizenException {
        security.getUserFromToken(token);
        try { return badgeRepo.findAll(); }
        catch (DatabaseException e) { throw e; }
    }

    // ── Seed default badges ────────────────────────────────────────────────────
    @SecurityManager.RequiresRole(User.Role.TEACHER)
    public void seedDefaultBadges(String token) throws KaizenException {
        security.checkAccess(token, BadgeService.class, "seedDefaultBadges");
        List<Badge> defaults = Arrays.asList(
                new Badge(0,"First Flame","Post on Fire feed for the first time",
                        "/icons/flame.png",    Badge.ConditionType.FIRST_POST),
                new Badge(0,"Focus Master","Complete 10 Pomodoro sessions",
                        "/icons/focus.png",    Badge.ConditionType.POMODORO_10),
                new Badge(0,"Bookworm","Study a total of 50 hours",
                        "/icons/book.png",     Badge.ConditionType.STUDY_50H),
                new Badge(0,"Quiz Champion","Score 100% on 3 quizzes",
                        "/icons/trophy.png",   Badge.ConditionType.QUIZ_100_3X),
                new Badge(0,"Team Player","Join a group study room 5 times",
                        "/icons/team.png",     Badge.ConditionType.ROOM_5X),
                new Badge(0,"Consistent","Maintain a 7-day study streak",
                        "/icons/streak.png",   Badge.ConditionType.STREAK_7),
                new Badge(0,"Top Learner","Reach #1 on the weekly leaderboard",
                        "/icons/star.png",     Badge.ConditionType.TOP_LEADERBOARD),
                new Badge(0,"Helper","Answer 10 questions on the Fire feed",
                        "/icons/helper.png",   Badge.ConditionType.ANSWER_10)
        );
        try {
            for (Badge b : defaults) {
                try { badgeRepo.save(b); }
                catch (DatabaseException e) {
                    if (e.getErrorCode() != ErrorCode.DB_DUPLICATE_ENTRY) throw e;
                }
            }
            loadBadges();
        } catch (DatabaseException e) { throw e; }
    }
}