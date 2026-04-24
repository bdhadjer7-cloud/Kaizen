package service;

import exception.*;
import exception.KaizenException.ErrorCode;
import model.*;
import repository.MessageRepository;
import security.InputSanitizer;
import security.RateLimiter;
import security.SecurityManager;
import util.Validator;

import java.util.*;
import java.util.logging.Logger;

/**
 * Kaizen — MessageService.java
 * Private messaging between users.
 */
public class MessageService {

    private static final Logger LOG = Logger.getLogger(MessageService.class.getName());

    private final MessageRepository messageRepo;
    private final SecurityManager   security;
    private final RateLimiter       rateLimiter;

    public MessageService(MessageRepository messageRepo, SecurityManager security) {
        this.messageRepo = messageRepo;
        this.security    = security;
        this.rateLimiter = security.getRateLimiter();
    }

    // ── Send message ───────────────────────────────────────────────────────────
    public Message sendMessage(String token, int receiverId, String content)
            throws KaizenException {

        User sender = security.getUserFromToken(token);

        // Rate limit — 30 messages per minute
        try { rateLimiter.check(sender.getEmail(), "MESSAGE"); }
        catch (SecurityViolationException e) {
            throw new AuthException(ErrorCode.SECURITY_RATE_LIMIT_EXCEEDED,
                    e.getMessage(), e.getAttackerInfo());
        }

        // Validate
        ValidationException ve = new ValidationException();
        Validator.notBlank(content, "content", ve);
        if (ve.hasErrors()) throw ve;
        Validator.maxLength(content, "content", 2000);
        Validator.positiveId(receiverId, "receiverId");

        // Cannot message yourself
        if (sender.getId() == receiverId)
            throw new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                    "User tried to message themselves",
                    "You cannot send a message to yourself.");

        // Sanitize
        try { content = InputSanitizer.sanitizeAll(content); }
        catch (SecurityViolationException e) { throw e; }

        Message msg = new Message(0, sender.getId(), receiverId, content);

        try {
            int id = messageRepo.save(msg);
            msg.setId(id);
            LOG.info("[MSG] Sent: " + sender.getId() + " -> " + receiverId);
            return msg;
        } catch (DatabaseException e) { throw e; }
    }

    // ── Get conversation ───────────────────────────────────────────────────────
    public Conversation getConversation(String token, int otherUserId)
            throws KaizenException {

        User user = security.getUserFromToken(token);
        Validator.positiveId(otherUserId, "otherUserId");

        try {
            List<Message> messages = messageRepo.findConversation(
                    user.getId(), otherUserId);
            Conversation conv = new Conversation(user.getId(), otherUserId);
            for (Message m : messages) conv.addMessage(m);
            return conv;
        } catch (DatabaseException e) { throw e; }
    }

    // ── Get all conversations ─────────────────────────────────────────────────
    public Map<Integer, Conversation> getAllConversations(String token)
            throws KaizenException {
        User user = security.getUserFromToken(token);
        try {
            return messageRepo.findAllConversations(user.getId());
        } catch (DatabaseException e) { throw e; }
    }

    // ── Mark as read ───────────────────────────────────────────────────────────
    public void markAsRead(String token, int senderId) throws KaizenException {
        User user = security.getUserFromToken(token);
        try { messageRepo.markAsRead(senderId, user.getId()); }
        catch (DatabaseException e) { throw e; }
    }

    // ── Unread count ───────────────────────────────────────────────────────────
    public int getUnreadCount(String token) throws KaizenException {
        User user = security.getUserFromToken(token);
        try { return messageRepo.countUnread(user.getId()); }
        catch (DatabaseException e) { throw e; }
    }

    // ── Search messages ────────────────────────────────────────────────────────
    public List<Message> searchMessages(String token, String keyword)
            throws KaizenException {
        User user = security.getUserFromToken(token);
        try { keyword = InputSanitizer.sanitizeSQL(keyword); }
        catch (SecurityViolationException e) { throw e; }
        try { return messageRepo.searchByContent(user.getId(), keyword); }
        catch (DatabaseException e) { throw e; }
    }

    // ── Delete message ─────────────────────────────────────────────────────────
    public void deleteMessage(String token, int messageId) throws KaizenException {
        User user = security.getUserFromToken(token);
        Validator.positiveId(messageId, "messageId");
        try {
            Message msg = messageRepo.findById(messageId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Message not found: " + messageId, "Message not found."));
            if (msg.getSenderId() != user.getId())
                throw new AuthException(ErrorCode.AUTH_FORBIDDEN,
                        "User " + user.getId() + " tried to delete msg " + messageId,
                        "You can only delete your own messages.");
            messageRepo.delete(messageId);
        } catch (DatabaseException e) { throw e; }
    }
}
