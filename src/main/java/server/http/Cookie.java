package server.http;

public record Cookie(String name, String value, int maxAge, boolean httpOnly) {
}
