package server.http.parser;

import server.http.HttpRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class Http11Parser {
    private Http11Parser() {
    }

    public static HttpRequest parse(InputStream inputStream) throws IOException {
        HttpRequest.Builder builder = HttpRequest.builder();

        parseRequestLine(inputStream, builder);
        parseHeaders(inputStream, builder);
        parseBody(inputStream, builder);

        return builder.build();
    }

    private static void parseRequestLine(InputStream inputStream, HttpRequest.Builder builder) throws IOException {
        String requestLine = readLine(inputStream);
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IllegalArgumentException("Empty request line");
        }
        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid request line: " + requestLine);
        }
        builder.method(parts[0]);
        parsePathAndQueryParams(parts[1], builder);
        builder.version(parts[2]);
    }

    private static void parseHeaders(InputStream inputStream, HttpRequest.Builder builder) throws IOException {
        String headerLine;
        while (!(headerLine = readLine(inputStream)).isEmpty()) {
            int colonIndex = headerLine.indexOf(':');
            if (colonIndex > 0) {
                String name = headerLine.substring(0, colonIndex).trim();
                String value = headerLine.substring(colonIndex + 1).trim();
                builder.addHeader(name, value);

                if (name.equalsIgnoreCase("Cookie")) {
                    parseCookies(value, builder);
                }
            }
        }
    }

    private static void parseCookies(String cookieHeader, HttpRequest.Builder builder) {
        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2) {
                String name = parts[0].trim();
                String value = urlDecode(parts[1].trim());
                builder.addCookie(name, value);
            }
        }
    }

    private static void parsePathAndQueryParams(String fullPath, HttpRequest.Builder builder) {
        String decodedPath = urlDecode(fullPath);
        String[] pathParts = decodedPath.split("\\?", 2);
        builder.path(pathParts[0]);

        if (pathParts.length > 1) {
            parseQueryParams(pathParts[1], builder);
        }
    }

    private static void parseQueryParams(String queryString, HttpRequest.Builder builder) {
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = urlDecode(pair.substring(0, idx));
                String value = idx < pair.length() - 1 ? urlDecode(pair.substring(idx + 1)) : "";
                builder.addQueryParam(key, value);
            } else {
                builder.addQueryParam(urlDecode(pair), "");
            }
        }
    }

    private static String urlDecode(String value) {
        if (value == null) {
            return null;
        }
        try {
            return java.net.URLDecoder.decode(value, "UTF-8");
        } catch (IllegalArgumentException e) {
            return value.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is unsupported", e);
        }
    }

    private static void parseBody(InputStream inputStream, HttpRequest.Builder builder) throws IOException {
        String contentLengthHeader = builder.getHeader("Content-Length");
        if (contentLengthHeader != null) {
            int contentLength = Integer.parseInt(contentLengthHeader);
            byte[] bodyBytes = new byte[contentLength];
            int bytesRead = 0;
            int read;
            while (bytesRead < contentLength && (read = inputStream.read(bodyBytes, bytesRead, contentLength - bytesRead)) != -1) {
                bytesRead += read;
            }
            builder.bodyBytes(bodyBytes);
            String contentType = builder.getHeader("Content-Type");
            if (contentType != null) {
                builder.body(new String(bodyBytes, "UTF-8").trim());
            }
        }
    }

    private static String readLine(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int lastByte = -1;
        int currentByte;
        while ((currentByte = inputStream.read()) != -1) {
            if (lastByte == '\r' && currentByte == '\n') {
                baos.write(lastByte);
                baos.write(currentByte);
                break;
            }
            if (lastByte != -1) {
                baos.write(lastByte);
            }
            lastByte = currentByte;
        }
        if (baos.size() == 0) {
            return "";
        }
        return new String(baos.toByteArray(), "UTF-8").trim();
    }
}
