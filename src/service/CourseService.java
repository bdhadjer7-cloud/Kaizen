package service;

import exception.*;
import exception.KaizenException.ErrorCode;
import model.*;
import repository.CourseRepository;
import repository.UserRepository;
import security.InputSanitizer;
import security.SecurityManager;
import util.Cache;
import util.KaizenUtils;
import util.Page;
import util.Validator;

import java.util.*;
import java.util.logging.Logger;

/**
 * Kaizen — CourseService.java
 * Business logic for courses, lessons, enrollments.
 */
public class CourseService {

    private static final Logger LOG = Logger.getLogger(CourseService.class.getName());

    private final CourseRepository courseRepo;
    private final UserRepository   userRepo;
    private final SecurityManager  security;
    private final Cache<Integer, Course> courseCache = new Cache<>(30_000); // 30s cache

    public CourseService(CourseRepository courseRepo, UserRepository userRepo,
                         SecurityManager security) {
        this.courseRepo = courseRepo;
        this.userRepo   = userRepo;
        this.security   = security;
    }

    //Create course (TEACHER only)
    @SecurityManager.RequiresRole(User.Role.TEACHER)
    public Course createCourse(String token, String title, String description)
            throws KaizenException {

        security.checkAccess(token, CourseService.class, "createCourse");

        // Validate
        ValidationException ve = new ValidationException();
        Validator.notBlank(title, "title", ve);
        Validator.notBlank(description, "description", ve);
        if (ve.hasErrors()) throw ve;
        Validator.maxLength(title, "title", 200);
        Validator.maxLength(description, "description", 1000);

        // Sanitize
        try {
            title       = InputSanitizer.sanitizeAll(title);
            description = InputSanitizer.sanitizeAll(description);
        } catch (SecurityViolationException e) { throw e; }

        User teacher = security.getUserFromToken(token);
        String joinCode = generateJoinCode();

        Course course = new Course(0, title, description, teacher.getId(), joinCode);

        try {
            int id = courseRepo.save(course);
            course.setId(id);
            courseCache.put(id, course);
            LOG.info("[COURSE] Created: " + title + " by " + teacher.getEmail());
            return course;
        } catch (DatabaseException e) { throw e; }
    }

    //Enroll student
    public void enrollStudent(String token, String joinCode)
            throws KaizenException {

        User student = security.getUserFromToken(token);

        // Sanitize join code
        try { joinCode = InputSanitizer.sanitizeSQL(joinCode); }
        catch (SecurityViolationException e) { throw e; }

        ValidationException ve = new ValidationException();
        Validator.notBlank(joinCode, "joinCode", ve);
        if (ve.hasErrors()) throw ve;

        Course course;
        try {
            final String safeJoinCode = joinCode;

            course = courseRepo.findByJoinCode(safeJoinCode)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.BUSINESS_INVALID_JOIN_CODE,
                            "No course with join code: " + safeJoinCode,
                            "Invalid join code. Please check and try again."
                    ));
        } catch (DatabaseException e) { throw e; }

        // Check not already enrolled
        try {
            if (courseRepo.isEnrolled(course.getId(), student.getId()))
                throw new BusinessException(ErrorCode.BUSINESS_ALREADY_ENROLLED,
                        student.getId() + " already enrolled in " + course.getId(),
                        "You are already enrolled in this course.");
        } catch (DatabaseException e) { throw e; }

        try {
            courseRepo.enrollStudent(course.getId(), student.getId());
            courseCache.invalidate(course.getId());
            LOG.info("[COURSE] Enrolled: " + student.getEmail() + " in " + course.getTitle());
        } catch (DatabaseException e) { throw e; }
    }

    //Get course by ID
    public Course getCourse(String token, int courseId)
            throws KaizenException {
        security.getUserFromToken(token); // validates session
        Validator.positiveId(courseId, "courseId");

        // Check cache first
        Optional<Course> cached = courseCache.get(courseId);
        if (cached.isPresent()) return cached.get();

        try {
            Course course = courseRepo.findById(courseId)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.BUSINESS_COURSE_NOT_FOUND,
                            "Course not found: " + courseId,
                            "Course not found."));
            courseCache.put(courseId, course);
            return course;
        } catch (DatabaseException e) { throw e; }
    }

    //Get my courses
    public List<Course> getMyCourses(String token) throws KaizenException {
        User user = security.getUserFromToken(token);
        try {
            if (user.isTeacher()) return courseRepo.findByTeacher(user.getId());
            else                  return courseRepo.findByStudent(user.getId());
        } catch (DatabaseException e) { throw e; }
    }

    //Add lesson (TEACHER only)
    @SecurityManager.RequiresRole(User.Role.TEACHER)
    public Lesson addLesson(String token, int courseId, String title, String content)
            throws KaizenException {

        security.checkAccess(token, CourseService.class, "addLesson");

        ValidationException ve = new ValidationException();
        Validator.notBlank(title,   "title",   ve);
        Validator.notBlank(content, "content", ve);
        if (ve.hasErrors()) throw ve;

        try {
            title   = InputSanitizer.sanitizeAll(title);
            content = InputSanitizer.sanitizeAll(content);
        } catch (SecurityViolationException e) { throw e; }

        Lesson lesson = new Lesson(0, courseId, title, content);
        try {
            int id = courseRepo.saveLesson(lesson);
            lesson.setId(id);
            courseCache.invalidate(courseId);
            LOG.info("[COURSE] Lesson added: " + title);
            return lesson;
        } catch (DatabaseException e) { throw e; }
    }

    //Get all courses (discover)
    public Page<Course> getAllCourses(String token, int page, int size)
            throws KaizenException {
        security.getUserFromToken(token);
        try {
            Map<Integer, Course> all = courseRepo.findAll();
            List<Course> list = new ArrayList<>(all.values());
            return KaizenUtils.paginate(list, page, size);
        } catch (DatabaseException e) { throw e; }
    }

    //Search courses
    public List<Course> searchCourses(String token, String keyword)
            throws KaizenException {
        security.getUserFromToken(token);
        try {
            keyword = InputSanitizer.sanitizeSQL(keyword);
        } catch (SecurityViolationException e) { throw e; }
        try { return courseRepo.searchByTitle(keyword); }
        catch (DatabaseException e) { throw e; }
    }

    private String generateJoinCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        java.security.SecureRandom rnd = new java.security.SecureRandom();
        for (int i = 0; i < 8; i++) code.append(chars.charAt(rnd.nextInt(chars.length())));
        return code.toString();
    }
}
