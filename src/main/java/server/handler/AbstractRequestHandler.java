package server.handler;

import server.http.ContentType;
import server.http.HttpResponse;

import static server.util.FileUtils.readFileContent;

public abstract class AbstractRequestHandler implements RequestHandler {
    private static final String ERROR_PAGE_PATH = "/static/error/";

    protected HttpResponse.Builder ok(byte[] body) {
        return HttpResponse.builder()
                .statusCode(200)
                .statusText("OK")
                .body(body);
    }

    protected HttpResponse.Builder redirect(String url) {
        return HttpResponse.builder()
                .statusCode(302)
                .statusText("Found")
                .addHeader("Location", url);
    }

    protected HttpResponse.Builder notFound() {
        byte[] errorContent = readErrorPage(404);
        return HttpResponse.builder()
                .statusCode(404)
                .statusText("Not Found")
                .addHeader("Content-Type", ContentType.HTML.getMimeType())
                .body(errorContent);
    }

    protected HttpResponse.Builder internalServerError() {
        byte[] errorContent = readErrorPage(500);
        return HttpResponse.builder()
                .statusCode(500)
                .statusText("Internal Server Error")
                .addHeader("Content-Type", ContentType.HTML.getMimeType())
                .body(errorContent);
    }

    private byte[] readErrorPage(int errorCode) {
        String errorPagePath = ERROR_PAGE_PATH + errorCode + "." + ContentType.HTML.getExtension();
        try {
            return readFileContent(errorPagePath);
        } catch (NullPointerException e) {
            return "<html><body><h1>404 Not Found</h1></body></html>".getBytes();
        }
    }
}
