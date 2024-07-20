package server.http;

import java.util.Arrays;

public enum ContentType {
    HTML("html", "text/html; charset=UTF-8"),
    CSS("css", "text/css; charset=UTF-8"),
    JS("js", "application/javascript; charset=UTF-8"),
    ICO("ico", "image/x-icon"),
    PNG("png", "image/png"),
    JPEG("jpeg", "image/jpeg"),
    SVG("svg", "image/svg+xml"),
    FORM_URLENCODED("", "application/x-www-form-urlencoded; charset=UTF-8"),
    TXT("txt", "text/plain; charset=UTF-8");

    private final String extension;
    private final String mimeType;

    ContentType(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public static String getMimeTypeFromFilePath(String filePath) {
        String extension = getExtensionFromFilePath(filePath);
        return Arrays.stream(values())
                .filter(ct -> ct.extension.equalsIgnoreCase(extension))
                .map(ct -> ct.mimeType)
                .findFirst()
                .orElse("text/plain; charset=UTF-8");
    }

    public static String getExtensionFromFilePath(String filePath) {
        int dotIndex = filePath.lastIndexOf('.');
        return (dotIndex > 0) ? filePath.substring(dotIndex + 1) : "";
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public boolean isTextBased() {
        return this == HTML || this == CSS || this == JS || this == FORM_URLENCODED || this == TXT;
    }
}
