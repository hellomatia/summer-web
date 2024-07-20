package server.core;

import server.handler.RequestHandler;
import server.handler.StaticFileHandler;
import server.http.HttpRequest;
import server.http.HttpResponse;

import java.util.List;

public class RequestDispatcher {
    private final List<RequestHandler> requestHandlers;
    private final StaticFileHandler staticFileHandler;

    public RequestDispatcher(List<RequestHandler> requestHandlers, StaticFileHandler staticFileHandler) {
        this.requestHandlers = requestHandlers;
        this.staticFileHandler = staticFileHandler;
    }

    public HttpResponse handleRequest(HttpRequest request) {
        return requestHandlers.stream()
                .filter(handler -> handler.canHandle(request))
                .findFirst()
                .map(handler -> handler.handle(request))
                .orElseGet(() -> staticFileHandler.handle(request));
    }
}
