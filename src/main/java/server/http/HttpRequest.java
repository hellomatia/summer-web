package server.http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final Map<String, String> cookies;
    private final Map<String, String> queryParams;
    private final String body;
    private final byte[] bodyBytes;

    private HttpRequest(Builder builder) {
        this.method = builder.method;
        this.path = builder.path;
        this.version = builder.version;
        this.headers = Map.copyOf(builder.headers);
        this.cookies = Map.copyOf(builder.cookies);
        this.queryParams = Map.copyOf(builder.queryParams);
        this.body = builder.body;
        this.bodyBytes = builder.bodyBytes;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getCookie(String name) {
        return cookies.get(name);
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getQueryParams() {
        return new HashMap<>(queryParams);
    }

    public String getBody() {
        return body;
    }

    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", version='" + version + '\'' +
                ", headers=" + headers +
                ", queryParams=" + queryParams +
                ", body='" + body + '\'' +
                '}';
    }

    public static class Builder {
        private String method;
        private String path;
        private String version;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> cookies = new HashMap<>();
        private Map<String, String> queryParams = new HashMap<>();
        private String body;
        private byte[] bodyBytes;

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder addHeader(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder addCookie(String name, String value) {
            this.cookies.put(name, value);
            return this;
        }

        public String getHeader(String name) {
            return this.headers.get(name);
        }

        public Builder addQueryParam(String name, String value) {
            this.queryParams.put(name, value);
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder bodyBytes(byte[] bodyBytes) {
            this.bodyBytes = bodyBytes;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
