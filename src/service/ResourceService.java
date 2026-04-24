package service;

import exception.*;
import exception.KaizenException.ErrorCode;
import model.*;
import repository.ResourceRepository;
import security.InputSanitizer;
import security.SecurityManager;
import util.Validator;

import java.util.*;
import java.util.logging.Logger;

/**
 * Kaizen — ResourceService.java
 * Sharing PDFs, links, AI tools, code, slides.
 */
public class ResourceService {

    private static final Logger LOG = Logger.getLogger(ResourceService.class.getName());
    private static final Set<String> ALLOWED_DOMAINS = new HashSet<>(Arrays.asList(
            "github.com","youtube.com","drive.google.com","docs.google.com",
            "arxiv.org","wikipedia.org","wolfram.com","notion.so","stackoverflow.com"
    ));

    private final ResourceRepository resourceRepo;
    private final SecurityManager    security;

    public ResourceService(ResourceRepository resourceRepo, SecurityManager security) {
        this.resourceRepo = resourceRepo;
        this.security     = security;
    }

    // ── Share resource ─────────────────────────────────────────────────────────
    public Resource shareResource(String token, int courseId, String title,
                                  Resource.ResourceType type, String url)
            throws KaizenException {

        User user = security.getUserFromToken(token);

        ValidationException ve = new ValidationException();
        Validator.notBlank(title, "title", ve);
        Validator.notBlank(url,   "url",   ve);
        if (ve.hasErrors()) throw ve;
        Validator.maxLength(title, "title", 200);
        Validator.maxLength(url,   "url",   500);

        // Sanitize
        try {
            title = InputSanitizer.sanitizeAll(title);
            url   = InputSanitizer.sanitizeSQL(url);
            InputSanitizer.sanitizeXSS(url);
        } catch (SecurityViolationException e) { throw e; }

        // Validate URL format
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            ValidationException e = new ValidationException();
            e.addError("url", "URL must start with http:// or https://");
            throw e;
        }

        Resource resource = new Resource(0, user.getId(), courseId,
                title, type, url);

        try {
            int id = resourceRepo.save(resource);
            resource.setId(id);
            LOG.info("[RESOURCE] Shared: " + title + " by " + user.getEmail());
            return resource;
        } catch (DatabaseException e) { throw e; }
    }

    // ── Get resources by course ────────────────────────────────────────────────
    public List<Resource> getResourcesByCourse(String token, int courseId)
            throws KaizenException {
        security.getUserFromToken(token);
        Validator.positiveId(courseId, "courseId");
        try { return resourceRepo.findByCourse(courseId); }
        catch (DatabaseException e) { throw e; }
    }

    // ── Get resources by type ──────────────────────────────────────────────────
    public List<Resource> getResourcesByType(String token, Resource.ResourceType type)
            throws KaizenException {
        security.getUserFromToken(token);
        try { return resourceRepo.findByType(type); }
        catch (DatabaseException e) { throw e; }
    }

    // ── Rate resource ──────────────────────────────────────────────────────────
    public void rateResource(String token, int resourceId, int stars)
            throws KaizenException {
        User user = security.getUserFromToken(token);
        if (stars < 1 || stars > 5) {
            ValidationException e = new ValidationException();
            e.addError("stars", "Rating must be between 1 and 5.");
            throw e;
        }
        try {
            Resource res = resourceRepo.findById(resourceId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Resource not found: " + resourceId, "Resource not found."));
            res.addRating(user.getId(), stars);
            resourceRepo.saveRating(resourceId, user.getId(), stars);
        } catch (DatabaseException e) { throw e; }
    }

    // ── Save resource to library ───────────────────────────────────────────────
    public void saveToLibrary(String token, int resourceId) throws KaizenException {
        User user = security.getUserFromToken(token);
        Validator.positiveId(resourceId, "resourceId");
        try {
            resourceRepo.saveToLibrary(resourceId, user.getId());
            LOG.info("[RESOURCE] Saved to library by " + user.getId());
        } catch (DatabaseException e) { throw e; }
    }

    // ── Search resources ───────────────────────────────────────────────────────
    public List<Resource> searchResources(String token, String keyword)
            throws KaizenException {
        security.getUserFromToken(token);
        try { keyword = InputSanitizer.sanitizeSQL(keyword); }
        catch (SecurityViolationException e) { throw e; }
        try { return resourceRepo.searchByTitle(keyword); }
        catch (DatabaseException e) { throw e; }
    }

    // ── Delete resource ────────────────────────────────────────────────────────
    public void deleteResource(String token, int resourceId) throws KaizenException {
        User user = security.getUserFromToken(token);
        Validator.positiveId(resourceId, "resourceId");
        try {
            Resource res = resourceRepo.findById(resourceId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Resource not found: " + resourceId, "Resource not found."));
            if (res.getUserId() != user.getId() && !user.isTeacher())
                throw new AuthException(ErrorCode.AUTH_FORBIDDEN,
                        "Not owner of resource " + resourceId,
                        "You can only delete your own resources.");
            resourceRepo.delete(resourceId);
        } catch (DatabaseException e) { throw e; }
    }
}