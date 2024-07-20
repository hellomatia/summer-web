package server.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final long sessionTimeout = 30 * 60 * 1000; // in milliseconds

    private SessionManager() {
    }

    public static Session createSession() {
        Session session = new Session();
        sessions.put(session.getId(), session);
        return session;
    }

    public static Session getSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            if (isExpired(session)) {
                invalidateSession(sessionId);
                return null;
            }
            session.access();
        }
        return session;
    }

    public static void invalidateSession(String sessionId) {
        sessions.remove(sessionId);
    }

    private static boolean isExpired(Session session) {
        return System.currentTimeMillis() - session.getLastAccessedTime() > sessionTimeout;
    }

    public static void cleanExpiredSessions() {
        sessions.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }
}
