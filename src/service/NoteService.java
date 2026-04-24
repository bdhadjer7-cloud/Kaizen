package service;

import exception.*;
import exception.KaizenException.ErrorCode;
import model.Note;
import model.User;
import repository.NoteRepository;
import security.InputSanitizer;
import security.SecurityManager;
import util.KaizenUtils;
import util.Validator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * Kaizen — NoteService.java (Person B)
 * Personal notes management for students.
 */
public class NoteService {

    private static final Logger LOG = Logger.getLogger(NoteService.class.getName());
    private static final long MAX_NOTE_SIZE = 50_000; // 50KB

    private final NoteRepository  noteRepo;
    private final SecurityManager security;

    public NoteService(NoteRepository noteRepo, SecurityManager security) {
        this.noteRepo = noteRepo;
        this.security = security;
    }


    public Note createNote(String token, int courseId, String title, String content)
            throws KaizenException {

        User user = security.getUserFromToken(token);

        if (!user.isStudent())
            throw new AuthException(ErrorCode.AUTH_FORBIDDEN,
                    "Non-student tried to create note",
                    "Only students can create notes.");

        ValidationException ve = new ValidationException();
        Validator.notBlank(title,   "title",   ve);
        Validator.notBlank(content, "content", ve);
        if (ve.hasErrors()) throw ve;
        Validator.maxLength(title, "title", 200);

        if (content.length() > MAX_NOTE_SIZE)
            throw new FileException(ErrorCode.FILE_TOO_LARGE,
                    "Note content too large: " + content.length(),
                    "Note is too long. Max size is 50,000 characters.");

        try {
            title   = InputSanitizer.sanitizeAll(title);
            content = InputSanitizer.sanitizeAll(content);
        } catch (SecurityViolationException e) { throw e; }

        Note note = new Note(0, user.getId(), courseId, title, content);

        try {
            int id = noteRepo.save(note);
            note.setId(id);
            LOG.info("[NOTE] Created: '" + title + "' by " + user.getEmail());
            return note;
        } catch (DatabaseException e) { throw e; }
    }


    public Note updateNote(String token, int noteId, String title, String content)
            throws KaizenException {

        User user = security.getUserFromToken(token);
        Validator.positiveId(noteId, "noteId");

        try {
            Note note = noteRepo.findById(noteId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Note not found: " + noteId, "Note not found."));

            if (note.getStudentId() != user.getId())
                throw new AuthException(ErrorCode.AUTH_FORBIDDEN,
                        user.getId() + " tried to edit note " + noteId,
                        "You can only edit your own notes.");

            if (title != null) note.setTitle(InputSanitizer.sanitizeAll(title));
            if (content != null) {
                if (content.length() > MAX_NOTE_SIZE)
                    throw new FileException(ErrorCode.FILE_TOO_LARGE,
                            "Note content too large: " + content.length(),
                            "Note is too long. Max size is 50,000 characters.");
                note.setContent(InputSanitizer.sanitizeAll(content));
            }
            note.setUpdatedAt(LocalDateTime.now());

            noteRepo.update(note);
            return note;
        } catch (DatabaseException | SecurityViolationException e) { throw e; }
    }


    public void deleteNote(String token, int noteId) throws KaizenException {
        User user = security.getUserFromToken(token);
        Validator.positiveId(noteId, "noteId");
        try {
            Note note = noteRepo.findById(noteId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Note not found: " + noteId, "Note not found."));
            if (note.getStudentId() != user.getId())
                throw new AuthException(ErrorCode.AUTH_FORBIDDEN,
                        "Not owner of note " + noteId, "You can only delete your own notes.");
            noteRepo.delete(noteId);
        } catch (DatabaseException e) { throw e; }
    }


    public List<Note> getMyNotes(String token) throws KaizenException {
        User user = security.getUserFromToken(token);
        try { return noteRepo.findByStudent(user.getId()); }
        catch (DatabaseException e) { throw e; }
    }


    public List<Note> getNotesByCourse(String token, int courseId)
            throws KaizenException {
        User user = security.getUserFromToken(token);
        Validator.positiveId(courseId, "courseId");
        try { return noteRepo.findByStudentAndCourse(user.getId(), courseId); }
        catch (DatabaseException e) { throw e; }
    }


    public List<Note> searchNotes(String token, String keyword) throws KaizenException {
        User user = security.getUserFromToken(token);
        ValidationException ve = new ValidationException();
        Validator.notBlank(keyword, "keyword", ve);
        if (ve.hasErrors()) throw ve;
        try { keyword = InputSanitizer.sanitizeSQL(keyword); }
        catch (SecurityViolationException e) { throw e; }
        try {
            List<Note> all = noteRepo.findByStudent(user.getId());
            final String kw = keyword;
            return KaizenUtils.filter(all, n -> n.matches(kw));
        } catch (DatabaseException e) { throw e; }
    }


    public void addTag(String token, int noteId, String tag) throws KaizenException {
        User user = security.getUserFromToken(token);
        Validator.positiveId(noteId, "noteId");
        ValidationException ve = new ValidationException();
        Validator.notBlank(tag, "tag", ve);
        if (ve.hasErrors()) throw ve;
        Validator.maxLength(tag, "tag", 50);
        try {
            tag = InputSanitizer.sanitizeAll(tag);
            Note note = noteRepo.findById(noteId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Note not found: " + noteId, "Note not found."));
            if (note.getStudentId() != user.getId())
                throw new AuthException(ErrorCode.AUTH_FORBIDDEN,
                        "Not owner", "Not your note.");
            note.addTag(tag);
            noteRepo.update(note);
        } catch (DatabaseException | SecurityViolationException e) { throw e; }
    }


    public String exportToText(String token, int noteId) throws KaizenException {
        User user = security.getUserFromToken(token);
        try {
            Note note = noteRepo.findById(noteId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Note not found: " + noteId, "Note not found."));
            if (note.getStudentId() != user.getId())
                throw new AuthException(ErrorCode.AUTH_FORBIDDEN,
                        "Not owner", "Not your note.");
            return "# " + note.getTitle() + "\n\n" + note.getContent()
                    + "\n\nTags: " + note.getTags()
                    + "\nUpdated: " + note.getUpdatedAt();
        } catch (DatabaseException e) { throw e; }
    }
}