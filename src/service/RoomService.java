package service;

import exception.*;
import exception.KaizenException.ErrorCode;
import model.*;
import repository.RoomRepository;
import security.InputSanitizer;
import security.SecurityManager;
import util.KaizenUtils;
import util.Validator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * Kaizen — RoomService.java (Person B)
 * Group study rooms — create, join, leave, chat.
 */
public class RoomService {

    private static final Logger LOG = Logger.getLogger(RoomService.class.getName());

    private final RoomRepository  roomRepo;
    private final SecurityManager security;
    private final BadgeService    badgeService;

    // Map<roomId, List<String>> — live chat messages (in-memory during session)
    private final Map<Integer, List<String>> liveChat = new HashMap<>();

    public RoomService(RoomRepository roomRepo, SecurityManager security,
                       BadgeService badgeService) {
        this.roomRepo     = roomRepo;
        this.security     = security;
        this.badgeService = badgeService;
    }

    // ── Create room ────────────────────────────────────────────────────────────
    public StudyRoom createRoom(String token, String name, int courseId,
                                int maxMembers, LocalDateTime scheduledAt)
            throws KaizenException {

        User user = security.getUserFromToken(token);

        ValidationException ve = new ValidationException();
        Validator.notBlank(name, "name", ve);
        if (ve.hasErrors()) throw ve;
        Validator.maxLength(name, "name", 200);

        if (maxMembers < 2 || maxMembers > 50) {
            ValidationException e = new ValidationException();
            e.addError("maxMembers", "Room must allow between 2 and 50 members.");
            throw e;
        }

        try { name = InputSanitizer.sanitizeAll(name); }
        catch (SecurityViolationException e) { throw e; }

        StudyRoom room = new StudyRoom(0, name, courseId,
                user.getId(), maxMembers, scheduledAt);

        try {
            int id = roomRepo.save(room);
            room.setId(id);
            liveChat.put(id, new ArrayList<>());
            // Creator auto-joins
            joinRoom(token, id);
            LOG.info("[ROOM] Created: " + name + " by " + user.getEmail());
            return room;
        } catch (DatabaseException e) { throw e; }
    }

    // ── Join room ──────────────────────────────────────────────────────────────
    public void joinRoom(String token, int roomId) throws KaizenException {
        User user = security.getUserFromToken(token);
        Validator.positiveId(roomId, "roomId");

        try {
            StudyRoom room = roomRepo.findById(roomId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Room not found: " + roomId, "Study room not found."));

            if (room.getMemberCount() >= room.getMaxMembers())
                throw new BusinessException(ErrorCode.BUSINESS_ROOM_FULL,
                        "Room " + roomId + " is full (" + room.getMaxMembers() + ")",
                        "This study room is full. Please join another.");

            if (roomRepo.isMember(roomId, user.getId())) return; // already in

            RoomMember member = new RoomMember(0, roomId, user.getId());
            roomRepo.addMember(member);

            // Check badge
            badgeService.checkAndAward(user.getId(), room);

            LOG.info("[ROOM] " + user.getEmail() + " joined room " + roomId);
        } catch (DatabaseException e) { throw e; }
    }

    // ── Leave room ─────────────────────────────────────────────────────────────
    public void leaveRoom(String token, int roomId) throws KaizenException {
        User user = security.getUserFromToken(token);
        Validator.positiveId(roomId, "roomId");
        try { roomRepo.removeMember(roomId, user.getId()); }
        catch (DatabaseException e) { throw e; }
    }

    // ── Send chat message ──────────────────────────────────────────────────────
    public void sendChatMessage(String token, int roomId, String message)
            throws KaizenException {

        User user = security.getUserFromToken(token);

        try { message = InputSanitizer.sanitizeAll(message); }
        catch (SecurityViolationException e) { throw e; }

        ValidationException ve = new ValidationException();
        Validator.notBlank(message, "message", ve);
        if (ve.hasErrors()) throw ve;
        Validator.maxLength(message, "message", 500);

        try {
            if (!roomRepo.isMember(roomId, user.getId()))
                throw new AuthException(ErrorCode.AUTH_FORBIDDEN,
                        "Non-member tried to chat in room " + roomId,
                        "You must be in the room to send messages.");
        } catch (DatabaseException e) { throw e; }

        String formatted = "[" + user.getName() + "]: " + message;
        liveChat.computeIfAbsent(roomId, k -> new ArrayList<>()).add(formatted);
        LOG.info("[ROOM] Chat in " + roomId + " by " + user.getName());
    }

    // ── Get live chat ──────────────────────────────────────────────────────────
    public List<String> getLiveChat(String token, int roomId) throws KaizenException {
        security.getUserFromToken(token);
        return Collections.unmodifiableList(
                liveChat.getOrDefault(roomId, Collections.emptyList()));
    }

    // ── Close room ─────────────────────────────────────────────────────────────
    public void closeRoom(String token, int roomId) throws KaizenException {
        User user = security.getUserFromToken(token);
        Validator.positiveId(roomId, "roomId");
        try {
            StudyRoom room = roomRepo.findById(roomId).orElseThrow(() ->
                    new BusinessException(ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
                            "Room not found: " + roomId, "Room not found."));
            if (room.getCreatedBy() != user.getId() && !user.isTeacher())
                throw new AuthException(ErrorCode.AUTH_FORBIDDEN,
                        "Non-creator tried to close room " + roomId,
                        "Only the room creator can close it.");
            roomRepo.closeRoom(roomId);
            liveChat.remove(roomId);
        } catch (DatabaseException e) { throw e; }
    }

    // ── Get all live rooms ─────────────────────────────────────────────────────
    public List<StudyRoom> getLiveRooms(String token) throws KaizenException {
        security.getUserFromToken(token);
        try {
            List<StudyRoom> all = roomRepo.findAll();
            return KaizenUtils.filter(all, StudyRoom::isLive);
        } catch (DatabaseException e) { throw e; }
    }

    // ── Get room members ───────────────────────────────────────────────────────
    public List<RoomMember> getRoomMembers(String token, int roomId)
            throws KaizenException {
        security.getUserFromToken(token);
        Validator.positiveId(roomId, "roomId");
        try { return roomRepo.getMembers(roomId); }
        catch (DatabaseException e) { throw e; }
    }
}