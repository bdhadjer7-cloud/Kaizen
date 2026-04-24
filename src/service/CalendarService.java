package service;

import exception.*;
import exception.KaizenException.ErrorCode;
import model.*;
import repository.CalendarRepository;
import repository.CourseRepository;
import security.InputSanitizer;
import security.SecurityManager;
import util.KaizenUtils;
import util.Validator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * Kaizen — CalendarService.java
 * Calendar events, deadlines, reminders.
 */
public class CalendarService {

    private static final Logger LOG = Logger.getLogger(CalendarService.class.getName());

    private final CalendarRepository calendarRepo;
    private final CourseRepository   courseRepo;
    private final SecurityManager    security;
    private final EmailService       emailService;

    public CalendarService(CalendarRepository calendarRepo,
                           CourseRepository courseRepo,
                           SecurityManager security,
                           EmailService emailService) {
        this.calendarRepo = calendarRepo;
        this.courseRepo   = courseRepo;
        this.security     = security;
        this.emailService = emailService;
    }

    //add event
    public CalendarEvent addEvent(String token, String title,
                                  String description,
                                  LocalDateTime dueDate,
                                  CalendarEvent.EventType type,
                                  int courseId) throws KaizenException {

        User user = security.getUserFromToken(token);

        ValidationException ve = new ValidationException();
        Validator.notBlank(title, "title", ve);
        if (ve.hasErrors()) throw ve;
        Validator.maxLength(title, "title", 200);

        if (dueDate == null || dueDate.isBefore(LocalDateTime.now())) {
            ValidationException e = new ValidationException();
            e.addError("dueDate", "Due date must be in the future.");
            throw e;
        }

        try { title = InputSanitizer.sanitizeAll(title); }
        catch (SecurityViolationException e) { throw e; }

        CalendarEvent event = new CalendarEvent(0, user.getId(),
                title, description, dueDate, type);
        event.setCourseId(courseId);

        try {
            int id = calendarRepo.save(event);
            event.setId(id);
            LOG.info("[CALENDAR] Event added: " + title + " due=" + dueDate);
            return event;
        } catch (DatabaseException e) { throw e; }
    }

    //Push deadline to students (TEACHER ~~pettt)
    @SecurityManager.RequiresRole(User.Role.TEACHER)
    public void pushDeadlineToStudents(String token, int courseId,
                                       String title, LocalDateTime dueDate,
                                       CalendarEvent.EventType type)
            throws KaizenException {

        security.checkAccess(token, CalendarService.class, "pushDeadlineToStudents");
        User teacher = security.getUserFromToken(token);

        ValidationException ve = new ValidationException();
        Validator.notBlank(title, "title", ve);
        if (ve.hasErrors()) throw ve;

        if (dueDate.isBefore(LocalDateTime.now())) {
            ValidationException e = new ValidationException();
            e.addError("dueDate", "Due date must be in the future.");
            throw e;
        }

        try {
            // Get all enrolled students
            List<Course> courses = courseRepo.findByTeacher(teacher.getId());
            Course target = KaizenUtils.findFirst(courses, c -> c.getId() == courseId)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.BUSINESS_COURSE_NOT_FOUND,
                            "Course not found: " + courseId, "Course not found."));

            Set<Integer> studentIds = target.getEnrolledStudentIds();
            CalendarEvent event = new CalendarEvent(0, teacher.getId(),
                    title, null, dueDate, type);
            event.setCourseId(courseId);

            for (int studentId : studentIds) {
                CalendarEvent studentEvent = new CalendarEvent(0, studentId,
                        title, null, dueDate, type);
                studentEvent.setCourseId(courseId);
                studentEvent.pushToStudent(studentId);
                calendarRepo.save(studentEvent);
            }

            LOG.info("[CALENDAR] Pushed deadline '" + title + "' to "
                    + studentIds.size() + " students");
        } catch (DatabaseException e) { throw e; }
    }

    //Get upcoming events
    public List<CalendarEvent> getUpcoming(String token) throws KaizenException {
        User user = security.getUserFromToken(token);
        try {
            List<CalendarEvent> all = calendarRepo.findByUser(user.getId());
            List<CalendarEvent> upcoming = KaizenUtils.filter(all,
                    CalendarEvent::isUpcoming);
            return KaizenUtils.sortBy(upcoming,
                    e -> e.getDueDate().toEpochSecond(java.time.ZoneOffset.UTC),
                    false); // earliest first
        } catch (DatabaseException e) { throw e; }
    }

    //Get events by type
    public List<CalendarEvent> getByType(String token, CalendarEvent.EventType type)
            throws KaizenException {
        User user = security.getUserFromToken(token);
        try {
            List<CalendarEvent> all = calendarRepo.findByUser(user.getId());
            return KaizenUtils.filter(all, e -> e.getType() == type);
        } catch (DatabaseException e) { throw e; }
    }

    //Delete event
    public void deleteEvent(String token, int eventId) throws KaizenException {
        User user = security.getUserFromToken(token);
        Validator.positiveId(eventId, "eventId");
        try {
            CalendarEvent event = calendarRepo.findById(eventId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Event not found: " + eventId, "Event not found."));
            if (event.getUserId() != user.getId() && !user.isTeacher())
                throw new AuthException(ErrorCode.AUTH_FORBIDDEN,
                        "Not owner of event " + eventId,
                        "You can only delete your own events.");
            calendarRepo.delete(eventId);
        } catch (DatabaseException e) { throw e; }
    }

    //Send reminder emails (called by scheduler)
    public void sendDueReminders() {
        try {
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
            List<CalendarEvent> due = calendarRepo.findDueTomorrow(tomorrow);

            for (CalendarEvent event : due) {
                try {
                    emailService.sendDeadlineReminder(
                            "user@kaizen.app",
                            "Student",
                            "Course",
                            event.getDueDate().toString()
                    );
                } catch (Exception e) {
                    LOG.warning("[CALENDAR] Reminder email failed: " + e.getMessage());
                }
            }
            LOG.info("[CALENDAR] Sent " + due.size() + " reminders");
        } catch (DatabaseException e) {
            LOG.severe("[CALENDAR] Reminder check failed: " + e.getMessage());
        }
    }
}