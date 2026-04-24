package service;

import exception.*;
import exception.KaizenException.ErrorCode;
import model.*;
import repository.PostRepository;
import security.InputSanitizer;
import security.RateLimiter;
import security.SecurityManager;
import util.KaizenUtils;
import util.Page;
import util.Validator;

import java.util.*;
import java.util.logging.Logger;

/**
 * Kaizen — PostService.java
 * Fire Feed logic — posts, comments, reactions, trending.
 */
public class PostService {

    private static final Logger LOG = Logger.getLogger(PostService.class.getName());

    private final PostRepository  postRepo;
    private final SecurityManager security;
    private final RateLimiter     rateLimiter;

    public PostService(PostRepository postRepo, SecurityManager security) {
        this.postRepo    = postRepo;
        this.security    = security;
        this.rateLimiter = security.getRateLimiter();
    }


    public Post createPost(String token, String content, Post.PostType type)
            throws KaizenException {

        User user = security.getUserFromToken(token);

        // Rate limit
        try { rateLimiter.check(user.getEmail(), "POST"); }
        catch (SecurityViolationException e) {
            throw new AuthException(ErrorCode.SECURITY_RATE_LIMIT_EXCEEDED,
                    e.getMessage(), e.getAttackerInfo());
        }

        // Validate
        ValidationException ve = new ValidationException();
        Validator.notBlank(content, "content", ve);
        if (ve.hasErrors()) throw ve;
        Validator.maxLength(content, "content", 2000);

        // Sanitize — XSS + SQL
        try { content = InputSanitizer.sanitizeAll(content); }
        catch (SecurityViolationException e) { throw e; }

        Post post = new Post(0, user.getId(), content, type);

        try {
            int id = postRepo.save(post);
            post.setId(id);
            LOG.info("[POST] Created by: " + user.getEmail());
            return post;
        } catch (DatabaseException e) { throw e; }
    }


    public void addReaction(String token, int postId, Post.ReactionType reaction)
            throws KaizenException {

        User user = security.getUserFromToken(token);
        Validator.positiveId(postId, "postId");

        try {
            Post post = postRepo.findById(postId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Post not found: " + postId, "Post not found."));
            post.addReaction(reaction, user.getId());
            postRepo.saveReaction(postId, user.getId(), reaction);
            LOG.info("[POST] Reaction " + reaction + " on post " + postId);
        } catch (DatabaseException e) { throw e; }
    }


    public Comment addComment(String token, int postId,
                              String content, Comment.CommentType type)
            throws KaizenException {

        User user = security.getUserFromToken(token);

        ValidationException ve = new ValidationException();
        Validator.notBlank(content, "content", ve);
        if (ve.hasErrors()) throw ve;
        Validator.maxLength(content, "content", 1000);

        try { content = InputSanitizer.sanitizeAll(content); }
        catch (SecurityViolationException e) { throw e; }

        Comment comment = new Comment(0, postId, user.getId(), content, type);

        try {
            int id = postRepo.saveComment(comment);
            comment.setId(id);
            return comment;
        } catch (DatabaseException e) { throw e; }
    }
    public Page<Post> getTrendingPosts(String token, int page, int size)
            throws KaizenException {
        security.getUserFromToken(token);
        try {
            List<Post> posts = postRepo.findTrending();
            List<Post> sorted = KaizenUtils.sortBy(posts,
                    p -> p.getTotalReactions(), true);
            return KaizenUtils.paginate(sorted, page, size);
        } catch (DatabaseException e) { throw e; }
    }


    public Page<Post> getFeed(String token, int page, int size)
            throws KaizenException {
        security.getUserFromToken(token);
        try {
            List<Post> posts = postRepo.findAll();
            return KaizenUtils.paginate(posts, page, size);
        } catch (DatabaseException e) { throw e; }
    }


    public List<Post> searchPosts(String token, String keyword)
            throws KaizenException {
        security.getUserFromToken(token);
        try { keyword = InputSanitizer.sanitizeSQL(keyword); }
        catch (SecurityViolationException e) { throw e; }
        try { return postRepo.searchByContent(keyword); }
        catch (DatabaseException e) { throw e; }
    }


    public void deletePost(String token, int postId) throws KaizenException {
        User user = security.getUserFromToken(token);
        Validator.positiveId(postId, "postId");
        try {
            Post post = postRepo.findById(postId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Post not found: " + postId, "Post not found."));
            if (post.getUserId() != user.getId() && !user.isTeacher())
                throw new AuthException(ErrorCode.AUTH_FORBIDDEN,
                        user.getId() + " tried to delete post " + postId,
                        "You can only delete your own posts.");
            postRepo.delete(postId);
        } catch (DatabaseException e) { throw e; }
    }
}
