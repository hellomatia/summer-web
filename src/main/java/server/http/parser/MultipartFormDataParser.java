package server.http.parser;

import server.http.ContentType;
import server.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class MultipartFormDataParser {
    private static final Logger logger = LoggerFactory.getLogger(MultipartFormDataParser.class);
    private static final String UTF_8 = "UTF-8";

    private MultipartFormDataParser() {
    }

    public static ParsedData parse(HttpRequest request) {
        String contentType = request.getHeaders().get("Content-Type");
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            throw new IllegalArgumentException("Not a multipart request");
        }

        String boundary = extractBoundary(contentType);
        if (boundary == null) {
            logger.error("No boundary found in multipart request");
            throw new IllegalArgumentException("No boundary found in multipart request");
        }

        String charset = extractCharset(contentType);
        return parseMultipartFormData(request.getBodyBytes(), boundary, charset);
    }

    private static String extractBoundary(String contentType) {
        String[] parts = contentType.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                return part.substring("boundary=".length()).trim();
            }
        }
        return null;
    }

    private static String extractCharset(String contentType) {
        String[] parts = contentType.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("charset=")) {
                try {
                    return part.substring("charset=".length()).trim();
                } catch (Exception e) {
                    logger.warn("Invalid charset in Content-Type, using UTF-8", e);
                }
            }
        }
        return UTF_8; // 기본값으로 UTF-8 사용
    }

    private static ParsedData parseMultipartFormData(byte[] body, String boundary,String charset) {
        Map<String, String> formData = new HashMap<>();
        Map<String, FileData> fileData = new HashMap<>();
        String boundaryString = "--" + boundary;

        try (InputStream is = new ByteArrayInputStream(body)) {
            byte[] boundaryBytes = boundaryString.getBytes(charset);
            byte[] buffer = new byte[8192];
            int bytesRead;
            boolean inHeader = false;
            StringBuilder headerBuilder = new StringBuilder();
            ByteArrayOutputStream contentBuilder = new ByteArrayOutputStream();

            while ((bytesRead = is.read(buffer)) != -1) {
                int i = 0;
                while (i < bytesRead) {
                    if (matchesBoundary(buffer, i, boundaryBytes)) {
                        if (!headerBuilder.isEmpty() || contentBuilder.size() > 0) {
                            processPartContent(headerBuilder.toString(), contentBuilder.toByteArray(), formData, fileData, charset);
                        }
                        headerBuilder = new StringBuilder();
                        contentBuilder = new ByteArrayOutputStream();
                        inHeader = true;
                        i += boundaryBytes.length;
                    } else if (inHeader) {
                        if (i + 3 < bytesRead && buffer[i] == '\r' && buffer[i+1] == '\n' && buffer[i+2] == '\r' && buffer[i+3] == '\n') {
                            inHeader = false;
                            i += 4;
                        } else {
                            headerBuilder.append((char) buffer[i]);
                            i++;
                        }
                    } else {
                        contentBuilder.write(buffer[i]);
                        i++;
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error parsing multipart form data", e);
        }

        return new ParsedData(formData, fileData);
    }

    private static boolean matchesBoundary(byte[] buffer, int index, byte[] boundary) {
        if (buffer.length - index < boundary.length) {
            return false;
        }
        for (int i = 0; i < boundary.length; i++) {
            if (buffer[index + i] != boundary[i]) {
                return false;
            }
        }
        return true;
    }

    private static void processPartContent(String headers, byte[] content, Map<String, String> formData, Map<String, FileData> fileData, String charset) throws UnsupportedEncodingException {
        String[] headerLines = headers.split("\r\n");
        String fieldName = null;
        boolean isFile = false;
        String fileName = null;
        String contentType = null;

        for (String header : headerLines) {
            if (header.startsWith("Content-Disposition: form-data;")) {
                fieldName = extractFieldName(header);
                if (header.contains("filename=")) {
                    isFile = true;
                    fileName = extractFileName(header);
                }
            } else if (header.startsWith("Content-Type:")) {
                contentType = header.substring("Content-Type:".length()).trim();
            }
        }

        if (fieldName != null) {
            if (isFile) {
                fileData.put(fieldName, new FileData(fileName, contentType, content));
            } else {
                formData.put(fieldName, new String(content, charset).trim());
            }
        }
    }

    private static String extractFieldName(String header) {
        return extractValue(header, "name=");
    }

    private static String extractFileName(String header) {
        String fileName = extractValue(header, "filename=");
        if (fileName != null) {
            try {
                // URL 디코딩을 사용하여 파일 이름 디코딩
                fileName = URLDecoder.decode(fileName, UTF_8);
            } catch (Exception e) {
                logger.warn("Failed to decode file name: " + fileName, e);
            }
        }
        return fileName;
    }

    private static String extractValue(String header, String prefix) {
        int start = header.indexOf(prefix);
        if (start != -1) {
            start += prefix.length();
            boolean inQuotes = header.charAt(start) == '"';
            if (inQuotes) {
                start++;
            }
            int end = header.indexOf(inQuotes ? '"' : ';', start);
            if (end == -1) {
                end = header.length();
            }
            return header.substring(start, end);
        }
        return null;
    }

    public static class ParsedData {
        private final Map<String, String> formData;
        private final Map<String, FileData> fileData;

        public ParsedData(Map<String, String> formData, Map<String, FileData> fileData) {
            this.formData = formData;
            this.fileData = fileData;
        }

        public Map<String, String> getFormData() {
            return formData;
        }

        public Map<String, FileData> getFileData() {
            return fileData;
        }
    }

    public static class FileData {
        private final String fileName;
        private final String contentType;
        private final String extension;
        private final byte[] content;

        public FileData(String fileName, String contentType, byte[] content) {
            this.fileName = fileName;
            this.contentType = contentType;
            this.extension = ContentType.getExtensionFromFilePath(fileName);
            this.content = content;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContentType() {
            return contentType;
        }

        public String getExtension() {
            return extension;
        }

        public byte[] getContent() {
            return content;
        }

        @Override
        public String toString() {
            return "FileData{" +
                    "fileName='" + fileName + '\'' +
                    ", contentType='" + contentType + '\'' +
                    ", contentLength=" + content.length +
                    '}';
        }
    }
}
