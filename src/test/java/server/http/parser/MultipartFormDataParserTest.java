package server.http.parser;
import org.junit.jupiter.api.Test;
import server.http.HttpRequest;
import server.http.parser.MultipartFormDataParser.ParsedData;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MultipartFormDataParserTest {
    private static final String BOUNDARY = "---------------------------1234567890";

    @Test
    void 멀티파트_요청_파싱_성공() throws UnsupportedEncodingException {
        HttpRequest request = createMockMultipartRequest();
        ParsedData parsedData = MultipartFormDataParser.parse(request);

        assertNotNull(parsedData);
        assertEquals(3, parsedData.getFormData().size());
        assertEquals(1, parsedData.getFileData().size());
    }

    @Test
    void 폼_데이터_추출_확인() throws UnsupportedEncodingException {
        HttpRequest request = createMockMultipartRequest();
        ParsedData parsedData = MultipartFormDataParser.parse(request);

        Map<String, String> formData = parsedData.getFormData();
        assertEquals("홍길동", formData.get("name"));
        assertEquals("hong@example.com", formData.get("email"));
        assertEquals("password123", formData.get("password"));
    }

    @Test
    void 멀티파트가_아닌_요청_처리() {
        HttpRequest request = HttpRequest.builder()
                .method("POST")
                .path("/user/create")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .body("name=홍길동&email=hong@example.com")
                .build();

        assertThrows(IllegalArgumentException.class, () -> MultipartFormDataParser.parse(request));
    }

    @Test
    void 경계값_없는_멀티파트_요청_처리() {
        HttpRequest request = HttpRequest.builder()
                .method("POST")
                .path("/user/create")
                .addHeader("Content-Type", "multipart/form-data")
                .body("some body content")
                .build();

        assertThrows(IllegalArgumentException.class, () -> MultipartFormDataParser.parse(request));
    }

    private HttpRequest createMockMultipartRequest() throws UnsupportedEncodingException {
        String body = "--" + BOUNDARY + "\r\n" +
                "Content-Disposition: form-data; name=\"name\"\r\n\r\n" +
                "홍길동\r\n" +
                "--" + BOUNDARY + "\r\n" +
                "Content-Disposition: form-data; name=\"email\"\r\n\r\n" +
                "hong@example.com\r\n" +
                "--" + BOUNDARY + "\r\n" +
                "Content-Disposition: form-data; name=\"password\"\r\n\r\n" +
                "password123\r\n" +
                "--" + BOUNDARY + "\r\n" +
                "Content-Disposition: form-data; name=\"userImage\"; filename=\"profile.jpg\"\r\n" +
                "Content-Type: image/jpeg\r\n\r\n" +
                "더미 이미지 데이터\r\n" +
                "--" + BOUNDARY + "--";

        return HttpRequest.builder()
                .method("POST")
                .path("/user/create")
                .addHeader("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
                .bodyBytes(body.getBytes("UTF-8"))
                .build();
    }
}
