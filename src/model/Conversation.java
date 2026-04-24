package model;

import java.util.*;

public class Conversation {
    private int userAId;
    private int userBId;
    private LinkedList<Message>      messages     = new LinkedList<>();
    private Map<Integer,Integer>     unreadCounts = new HashMap<>();

    public Conversation(int userAId, int userBId) {
        this.userAId=userAId; this.userBId=userBId;
        unreadCounts.put(userAId, 0); unreadCounts.put(userBId, 0);
    }

    public void addMessage(Message m) {
        messages.addFirst(m);
        unreadCounts.merge(m.getReceiverId(), 1, Integer::sum);
    }
    public void    markAllRead(int uid)      { unreadCounts.put(uid, 0); }
    public int     getUnreadCount(int uid)   { return unreadCounts.getOrDefault(uid, 0); }
    public Message getLastMessage()          { return messages.isEmpty() ? null : messages.getFirst(); }
    public int     getMessageCount()         { return messages.size(); }

    public <T extends CharSequence> List<Message> search(T keyword) {
        String kw = keyword.toString().toLowerCase();
        List<Message> r = new ArrayList<>();
        for (Message m : messages)
            if (m.getContent()!=null && m.getContent().toLowerCase().contains(kw)) r.add(m);
        return r;
    }

    public int    getUserAId()                          { return userAId; }
    public int    getUserBId()                          { return userBId; }
    public LinkedList<Message> getMessages()            { return messages; }
    public Map<Integer,Integer> getUnreadCounts()       { return Collections.unmodifiableMap(unreadCounts); }

    @Override public String toString() {
        return "Conversation{users="+userAId+"<->"+userBId+", messages="+messages.size()+"}";
    }
}