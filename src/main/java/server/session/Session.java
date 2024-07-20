package server.session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Session {
    private final String id;
    private final Map<String, Object> storage;
    private long lastAccessedTime;

    public Session() {
        this.id = UUID.randomUUID().toString();
        this.storage = new HashMap<>();
        this.lastAccessedTime = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setAttribute(String key, Object value) {
        storage.put(key, value);
    }

    public Object getAttribute(String key) {
        return storage.get(key);
    }

    public void removeAttribute(String key) {
        storage.remove(key);
    }

    public void clear() {
        storage.clear();
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void access() {
        this.lastAccessedTime = System.currentTimeMillis();
    }
}
