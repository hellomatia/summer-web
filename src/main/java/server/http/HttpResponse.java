package server.http;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {
    private final String version;
    private final int statusCode;
    private final String statusText;
    private final Map<String, String> headers;
    private final byte[] body;

    private HttpResponse(Builder builder) {
        this.version = builder.version;
        this.statusCode = builder.statusCode;
        this.statusText = builder.statusText;
        this.headers = Map.copyOf(builder.headers);
        this.body = builder.body;
    }

    public String getVersion() {
        return version;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public byte[] getBytes() {
        StringBuilder response = new StringBuilder();
        response.append(version).append(" ")
                .append(statusCode).append(" ")
                .append(statusText).append("\r\n");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.append(header.getKey()).append(": ")
                    .append(header.getValue()).append("\r\n");
        }

        response.append("\r\n");

        byte[] headerBytes = response.toString().getBytes();
        byte[] responseBytes = new byte[headerBytes.length + body.length];

        System.arraycopy(headerBytes, 0, responseBytes, 0, headerBytes.length);
        System.arraycopy(body, 0, responseBytes, headerBytes.length, body.length);

        return responseBytes;
    }

    @Override
    public String toString() {
        StringBuilder response = new StringBuilder();
        response.append(version).append(" ")
                .append(statusCode).append(" ")
                .append(statusText).append("\r\n");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.append(header.getKey()).append(": ")
                    .append(header.getValue()).append("\r\n");
        }

        response.append("\r\n");

        if (body != null && body.length > 0) {
            response.append(new String(body)); // Convert byte[] to String for display purposes
        }

        return response.toString();
    }

    public static class Builder {
        private String version = "HTTP/1.1";
        private int statusCode = 200;
        private String statusText = "OK";
        private Map<String, String> headers = new HashMap<>();
        private Map<String, Cookie> cookies = new LinkedHashMap<>();
        private byte[] body = new byte[0];

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder statusText(String statusText) {
            this.statusText = statusText;
            return this;
        }

        public Builder addHeader(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder addCookie(String name, String value, int maxAge, boolean httpOnly) {
            this.cookies.put(name, new Cookie(name, value, maxAge, httpOnly));
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public HttpResponse build() {
            if (!cookies.isEmpty()) {
                cookies.values().forEach(cookie -> {
                    StringBuilder cookieBuilder = new StringBuilder();
                    cookieBuilder.append(cookie.name()).append("=").append(cookie.value())
                            .append("; Path=/");

                    if (cookie.maxAge() > 0) {
                        cookieBuilder.append("; Max-Age=").append(cookie.maxAge());
                    } else if (cookie.maxAge() == 0) {
                        cookieBuilder.append("; Max-Age=0")
                                .append("; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
                    }

                    if (cookie.httpOnly()) {
                        cookieBuilder.append("; HttpOnly");
                    }

                    this.headers.put("Set-Cookie", cookieBuilder.toString());
                });
            }
            return new HttpResponse(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
