package server.session;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {
    private Map<String, Session> sessions;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field sessionsField = SessionManager.class.getDeclaredField("sessions");
        sessionsField.setAccessible(true);
        sessions = (ConcurrentHashMap<String, Session>) sessionsField.get(null);
        sessions.clear();
    }

    @AfterEach
    void tearDown() {
        sessions.clear();
    }

    @Test
    void 세션_생성_테스트() {
        Session session = SessionManager.createSession();
        assertNotNull(session);
        assertTrue(sessions.containsKey(session.getId()));
    }

    @Test
    void 세션_조회_테스트() {
        Session createdSession = SessionManager.createSession();
        Session retrievedSession = SessionManager.getSession(createdSession.getId());
        assertEquals(createdSession.getId(), retrievedSession.getId());
    }

    @Test
    void 존재하지_않는_세션_조회_테스트() {
        Session session = SessionManager.getSession("non-existent-id");
        assertNull(session);
    }

    @Test
    void 세션_무효화_테스트() {
        Session session = SessionManager.createSession();
        String sessionId = session.getId();
        SessionManager.invalidateSession(sessionId);
        assertNull(SessionManager.getSession(sessionId));
    }

    @Test
    void 만료된_세션_조회_테스트() throws NoSuchFieldException, IllegalAccessException {
        Session session = SessionManager.createSession();
        String sessionId = session.getId();

        // 세션의 마지막 접근 시간을 30분 이전으로 설정
        Field lastAccessedTimeField = Session.class.getDeclaredField("lastAccessedTime");
        lastAccessedTimeField.setAccessible(true);
        lastAccessedTimeField.set(session, System.currentTimeMillis() - 31 * 60 * 1000);

        assertNull(SessionManager.getSession(sessionId));
    }

    @Test
    void 만료된_세션_정리_테스트() throws NoSuchFieldException, IllegalAccessException {
        Session session1 = SessionManager.createSession();
        Session session2 = SessionManager.createSession();

        // 세션의 마지막 접근 시간을 30분 이전으로 설정
        Field lastAccessedTimeField = Session.class.getDeclaredField("lastAccessedTime");
        lastAccessedTimeField.setAccessible(true);
        lastAccessedTimeField.set(session1, System.currentTimeMillis() - 31 * 60 * 1000);
        lastAccessedTimeField.set(session2, System.currentTimeMillis() - 31 * 60 * 1000);

        SessionManager.cleanExpiredSessions();
        assertTrue(sessions.isEmpty());
    }
}
