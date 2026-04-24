package service;

import exception.*;
import exception.KaizenException.ErrorCode;
import model.*;
import repository.QuizRepository;
import security.InputSanitizer;
import security.SecurityManager;
import util.Validator;

import java.util.*;
import java.util.logging.Logger;

/**
 * Kaizen — QuizService.java (Person B)
 * Quiz creation, attempts, scoring, statistics.
 */
public class QuizService {

    private static final Logger LOG = Logger.getLogger(QuizService.class.getName());

    private final QuizRepository  quizRepo;
    private final SecurityManager security;

    public QuizService(QuizRepository quizRepo, SecurityManager security) {
        this.quizRepo = quizRepo;
        this.security = security;
    }

    // ── Create quiz (TEACHER only) ─────────────────────────────────────────────
    @SecurityManager.RequiresRole(User.Role.TEACHER)
    public Quiz createQuiz(String token, int courseId, String title, int timeLimitSec)
            throws KaizenException {

        security.checkAccess(token, QuizService.class, "createQuiz");
        User teacher = security.getUserFromToken(token);

        ValidationException ve = new ValidationException();
        Validator.notBlank(title, "title", ve);
        if (ve.hasErrors()) throw ve;
        Validator.maxLength(title, "title", 200);

        if (timeLimitSec < 60 || timeLimitSec > 7200) {
            ValidationException e = new ValidationException();
            e.addError("timeLimit","Time limit must be between 1 and 120 minutes.");
            throw e;
        }

        try { title = InputSanitizer.sanitizeAll(title); }
        catch (SecurityViolationException e) { throw e; }

        Quiz quiz = new Quiz(0, courseId, teacher.getId(), title, timeLimitSec);
        try {
            int id = quizRepo.save(quiz);
            quiz.setId(id);
            LOG.info("[QUIZ] Created: " + title);
            return quiz;
        } catch (DatabaseException e) { throw e; }
    }

    // ── Add question (TEACHER only) ────────────────────────────────────────────
    @SecurityManager.RequiresRole(User.Role.TEACHER)
    public QuizQuestion addQuestion(String token, int quizId, String questionText,
                                    String a, String b, String c, String d, char correct)
            throws KaizenException {

        security.checkAccess(token, QuizService.class, "addQuestion");

        ValidationException ve = new ValidationException();
        Validator.notBlank(questionText, "questionText", ve);
        Validator.notBlank(a, "optionA", ve); Validator.notBlank(b, "optionB", ve);
        Validator.notBlank(c, "optionC", ve); Validator.notBlank(d, "optionD", ve);
        if (ve.hasErrors()) throw ve;

        char correctUpper = Character.toUpperCase(correct);
        if ("ABCD".indexOf(correctUpper) == -1) {
            ValidationException e2 = new ValidationException();
            e2.addError("correctOption", "Correct option must be A, B, C, or D.");
            throw e2;
        }

        try {
            questionText = InputSanitizer.sanitizeAll(questionText);
            a = InputSanitizer.sanitizeAll(a); b = InputSanitizer.sanitizeAll(b);
            c = InputSanitizer.sanitizeAll(c); d = InputSanitizer.sanitizeAll(d);
        } catch (SecurityViolationException e) { throw e; }

        QuizQuestion question = new QuizQuestion(0, quizId,
                questionText, a, b, c, d, correctUpper);

        try {
            int id = quizRepo.saveQuestion(question);
            question.setId(id);
            return question;
        } catch (DatabaseException e) { throw e; }
    }

    // ── Submit attempt ─────────────────────────────────────────────────────────
    public QuizAttempt submitAttempt(String token, int quizId,
                                     Map<Integer, Character> answers)
            throws KaizenException {

        User student = security.getUserFromToken(token);

        if (!student.isStudent())
            throw new AuthException(ErrorCode.AUTH_FORBIDDEN,
                    "Non-student tried to attempt quiz",
                    "Only students can attempt quizzes.");

        Validator.positiveId(quizId, "quizId");

        // Check not already attempted
        try {
            if (quizRepo.hasAttempted(student.getId(), quizId))
                throw new BusinessException(ErrorCode.BUSINESS_QUIZ_ALREADY_ATTEMPTED,
                        student.getId() + " already attempted quiz " + quizId,
                        "You have already attempted this quiz.");

            Quiz quiz = quizRepo.findById(quizId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Quiz not found: " + quizId, "Quiz not found."));

            QuizAttempt attempt = new QuizAttempt(0, student.getId(), quizId);
            answers.forEach(attempt::recordAnswer);

            double score = attempt.calculateScore(quiz.getQuestions());
            attempt.setScore(score);

            int id = quizRepo.saveAttempt(attempt);
            attempt.setId(id);
            quiz.recordAttempt(attempt);

            LOG.info("[QUIZ] Attempt: student=" + student.getId()
                    + " quiz=" + quizId + " score=" + score);
            return attempt;
        } catch (DatabaseException e) { throw e; }
    }

    // ── Get quiz with questions ────────────────────────────────────────────────
    public Quiz getQuiz(String token, int quizId) throws KaizenException {
        security.getUserFromToken(token);
        Validator.positiveId(quizId, "quizId");
        try {
            return quizRepo.findById(quizId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Quiz not found: " + quizId, "Quiz not found."));
        } catch (DatabaseException e) { throw e; }
    }

    // ── Get quizzes by course ──────────────────────────────────────────────────
    public List<Quiz> getQuizzesByCourse(String token, int courseId)
            throws KaizenException {
        security.getUserFromToken(token);
        Validator.positiveId(courseId, "courseId");
        try { return quizRepo.findByCourse(courseId); }
        catch (DatabaseException e) { throw e; }
    }

    // ── Get statistics (TEACHER only) ──────────────────────────────────────────
    @SecurityManager.RequiresRole(User.Role.TEACHER)
    public Map<String, Object> getQuizStats(String token, int quizId)
            throws KaizenException {
        security.checkAccess(token, QuizService.class, "getQuizStats");
        Validator.positiveId(quizId, "quizId");
        try {
            Quiz quiz = quizRepo.findById(quizId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Quiz not found: " + quizId, "Quiz not found."));
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalAttempts",  quiz.getAttempts().size());
            stats.put("averageScore",   String.format("%.1f%%", quiz.getAverageScore()));
            stats.put("questionCount",  quiz.getQuestionCount());
            stats.put("passingRate",    getPassingRate(quiz));
            return stats;
        } catch (DatabaseException e) { throw e; }
    }

    private String getPassingRate(Quiz quiz) {
        if (quiz.getAttempts().isEmpty()) return "N/A";
        long passed = quiz.getAttempts().values().stream()
                .filter(a -> a.getScore() >= 50).count();
        return String.format("%.0f%%", (double) passed / quiz.getAttempts().size() * 100);
    }
}