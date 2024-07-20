package server.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerStatusTest {
    @Test
    void 서버_상태_초기값_확인() {
        ServerStatus status = new ServerStatus();
        assertTrue(status.isRunning());
    }

    @Test
    void 서버_상태_변경_확인() {
        ServerStatus status = new ServerStatus();
        status.stop();
        assertFalse(status.isRunning());
    }
}
