package service;

import exception.AuthException;
import model.*;
import security.SecurityManager;
import util.KaizenUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * Kaizen — NotificationService.java
 * In-app notifications — badges, messages, deadlines.
 * Uses EventBus<AppEvent> to decouple notification triggers.
 * Map<userId, List<Notification>> for fast per-user lookup.
 */
public class NotificationService {

    private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());

    public static class Notification {
        public enum NType { BADGE, MESSAGE, DEADLINE, POST_REACTION, ROOM_INVITE }

        private final int          userId;
        private final String       message;
        private final NType        type;
        private final LocalDateTime createdAt;
        private       boolean      read;

        public Notification(int userId, String message, NType type) {
            this.userId    = userId;
            this.message   = message;
            this.type      = type;
            this.createdAt = LocalDateTime.now();
            this.read      = false;
        }

        public int    getUserId()      { return userId; }
        public String getMessage()     { return message; }
        public NType  getType()        { return type; }
        public boolean isRead()        { return read; }
        public void   markRead()       { this.read = true; }
        public LocalDateTime getCreatedAt() { return createdAt; }

        @Override public String toString() {
            return "Notification{type=" + type + ", msg='" + message
                    + "', read=" + read + "}";
        }
    }

    // Map<userId, LinkedList<Notification>> — newest first per user
    private final Map<Integer, LinkedList<Notification>> store = new HashMap<>();
    private static final int MAX_PER_USER = 50;

    private final SecurityManager security;

    public NotificationService(SecurityManager security) {
        this.security = security;
    }

    //Push notification
    public void push(int userId, String message, Notification.NType type) {
        Notification notif = new Notification(userId, message, type);
        LinkedList<Notification> list = store.computeIfAbsent(userId,
                k -> new LinkedList<>());
        list.addFirst(notif); // newest first
        // Keep max 50 per user
        while (list.size() > MAX_PER_USER) list.removeLast();
        LOG.info("[NOTIF] -> user " + userId + ": " + message);
    }

    //Get notifications
    public List<Notification> getNotifications(String token) throws AuthException {
        User user = security.getUserFromToken(token);
        return Collections.unmodifiableList(
                store.getOrDefault(user.getId(), new LinkedList<>()));
    }

    //Get unread count
    public int getUnreadCount(String token) throws AuthException {
        User user = security.getUserFromToken(token);
        List<Notification> list = store.getOrDefault(user.getId(), new LinkedList<>());
        return (int) list.stream().filter(n -> !n.isRead()).count();
    }

    //Mark all read
    public void markAllRead(String token) throws AuthException {
        User user = security.getUserFromToken(token);
        store.getOrDefault(user.getId(), new LinkedList<>())
                .forEach(Notification::markRead);
    }

    //Mark one read
    public void markRead(String token, int index) throws AuthException {
        User user = security.getUserFromToken(token);
        List<Notification> list = store.getOrDefault(user.getId(), new LinkedList<>());
        if (index >= 0 && index < list.size()) list.get(index).markRead();
    }

    //Filter by type
    public List<Notification> getByType(String token, Notification.NType type)
            throws AuthException {
        User user = security.getUserFromToken(token);
        List<Notification> all = store.getOrDefault(user.getId(), new LinkedList<>());
        return KaizenUtils.filter(all, n -> n.getType() == type);
    }


    public void clearAll(String token) throws AuthException {
        User user = security.getUserFromToken(token);
        store.remove(user.getId());
    }

    //Helpers for other services to trigger notifications
    public void notifyBadge(int userId, String badgeName) {
        push(userId, "You earned the badge: " + badgeName + "!",
                Notification.NType.BADGE);
    }

    public void notifyMessage(int userId, String senderName) {
        push(userId, "New message from " + senderName,
                Notification.NType.MESSAGE);
    }

    public void notifyDeadline(int userId, String eventTitle, long daysLeft) {
        push(userId, "Deadline in " + daysLeft + " day(s): " + eventTitle,
                Notification.NType.DEADLINE);
    }

    public void notifyReaction(int userId, String reacterName, String reactionType) {
        push(userId, reacterName + " reacted " + reactionType + " to your post",
                Notification.NType.POST_REACTION);
    }

    public void notifyRoomInvite(int userId, String roomName) {
        push(userId, "You've been invited to study room: " + roomName,
                Notification.NType.ROOM_INVITE);
    }
}