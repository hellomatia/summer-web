package server.core;

public class Port {
    private final int value;

    public Port(int value) {
        if (value <= 0 || value > 65535) {
            throw new IllegalArgumentException("Invalid port number");
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
