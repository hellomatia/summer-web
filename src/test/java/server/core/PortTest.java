package server.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PortTest {
    @Test
    void 유효한_포트_번호로_Port_객체_생성() {
        assertDoesNotThrow(() -> new Port(8080));
        Port port = new Port(8080);
        assertEquals(8080, port.getValue());
    }

    @Test
    void 최소_유효_포트_번호_1로_Port_객체_생성() {
        assertDoesNotThrow(() -> new Port(1));
        Port port = new Port(1);
        assertEquals(1, port.getValue());
    }

    @Test
    void 최대_유효_포트_번호_65535로_Port_객체_생성() {
        assertDoesNotThrow(() -> new Port(65535));
        Port port = new Port(65535);
        assertEquals(65535, port.getValue());
    }

    @Test
    void 포트_번호_0으로_객체_생성시_예외_발생() {
        assertThrows(IllegalArgumentException.class, () -> new Port(0));
    }

    @Test
    void 음수_포트_번호로_객체_생성시_예외_발생() {
        assertThrows(IllegalArgumentException.class, () -> new Port(-1));
    }

    @Test
    void 포트_번호_65535_초과시_예외_발생() {
        assertThrows(IllegalArgumentException.class, () -> new Port(65536));
    }
}