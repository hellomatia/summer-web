package server.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseTest {
    @Test
    void 기본_응답_생성() {
        HttpResponse response = HttpResponse.builder().build();

        assertEquals("HTTP/1.1", response.getVersion());
        assertEquals(200, response.getStatusCode());
        assertEquals("OK", response.getStatusText());
        assertTrue(response.getHeaders().isEmpty());
        assertEquals("", new String(response.getBody()));
    }

    @Test
    void 커스텀_응답_생성() {
        HttpResponse response = HttpResponse.builder()
                .version("HTTP/2.0")
                .statusCode(404)
                .statusText("Not Found")
                .addHeader("Content-Type", "text/plain")
                .body("Resource not found".getBytes())
                .build();

        assertEquals("HTTP/2.0", response.getVersion());
        assertEquals(404, response.getStatusCode());
        assertEquals("Not Found", response.getStatusText());
        assertEquals("text/plain", response.getHeaders().get("Content-Type"));
        assertEquals("Resource not found", new String(response.getBody()));
    }

    @Test
    void 여러_헤더_추가() {
        HttpResponse response = HttpResponse.builder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Cache-Control", "no-cache")
                .build();

        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        assertEquals("no-cache", response.getHeaders().get("Cache-Control"));
        assertEquals(2, response.getHeaders().size());
    }

    @Test
    void 쿠키_추가() {
        HttpResponse response = HttpResponse.builder()
                .addCookie("sid", "12", 30 * 60, true)
                .build();

        assertEquals("sid=12; Path=/; Max-Age=1800; HttpOnly", response.getHeaders().get("Set-Cookie"));
    }

    @Test
    void toString_메소드_검증() {
        HttpResponse response = HttpResponse.builder()
                .statusCode(201)
                .statusText("Created")
                .addHeader("Location", "/resource/123")
                .body("{\"id\":123}".getBytes())
                .build();

        String expectedString = "HTTP/1.1 201 Created\r\n" +
                "Location: /resource/123\r\n" +
                "\r\n" +
                "{\"id\":123}";

        assertEquals(expectedString, response.toString());
    }

    @Test
    void 빈_본문_응답() {
        HttpResponse response = HttpResponse.builder()
                .statusCode(204)
                .statusText("No Content")
                .build();

        assertFalse(response.toString().contains("\r\n\r\n\r\n"));
        assertTrue(response.toString().endsWith("\r\n\r\n"));
    }

    @Test
    void 헤더_불변성_검증() {
        HttpResponse.Builder builder = HttpResponse.builder()
                .addHeader("X-Custom", "Value");

        HttpResponse response = builder.build();

        assertThrows(UnsupportedOperationException.class, () -> {
            response.getHeaders().put("New-Header", "New-Value");
        });
    }
}
