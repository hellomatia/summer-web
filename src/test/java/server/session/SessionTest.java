package server.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {
    private Session session;

    @BeforeEach
    void setUp() {
        session = new Session();
    }

    @Test
    void ID_생성_테스트() {
        assertNotNull(session.getId());
        assertTrue(session.getId().length() > 0);
    }

    @Test
    void 속성_설정_및_조회_테스트() {
        session.setAttribute("username", "testUser");
        assertEquals("testUser", session.getAttribute("username"));
    }

    @Test
    void 속성_제거_테스트() {
        session.setAttribute("key", "value");
        session.removeAttribute("key");
        assertNull(session.getAttribute("key"));
    }

    @Test
    void 모든_속성_제거_테스트() {
        session.setAttribute("key1", "value1");
        session.setAttribute("key2", "value2");
        session.clear();
        assertNull(session.getAttribute("key1"));
        assertNull(session.getAttribute("key2"));
    }

    @Test
    void 마지막_접근_시간_테스트() {
        long initialTime = session.getLastAccessedTime();
        try {
            Thread.sleep(10); // 10ms 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        session.access();
        assertTrue(session.getLastAccessedTime() > initialTime);
    }

    @Test
    void 여러_타입의_속성_저장_테스트() {
        session.setAttribute("string", "text");
        session.setAttribute("integer", 123);
        session.setAttribute("boolean", true);

        assertEquals("text", session.getAttribute("string"));
        assertEquals(123, session.getAttribute("integer"));
        assertEquals(true, session.getAttribute("boolean"));
    }
}
