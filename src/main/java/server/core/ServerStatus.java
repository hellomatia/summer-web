package server.core;

public class ServerStatus {
    private volatile boolean running = true;

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }
}
