package server.handler;

import server.handler.annotation.Handler;
import server.handler.annotation.HttpMethod;
import server.http.HttpRequest;
import server.http.HttpResponse;

import java.lang.reflect.Method;

public abstract class CustomRequestHandler extends AbstractRequestHandler {
    @Override
    public HttpResponse handle(HttpRequest request) {
        String httpMethod = request.getMethod();
        for (Method method : this.getClass().getDeclaredMethods()) {
            HttpMethod httpMethodAnnotation = method.getAnnotation(HttpMethod.class);
            if (httpMethodAnnotation != null && httpMethodAnnotation.value().equalsIgnoreCase(httpMethod)) {
                try {
                    return (HttpResponse) method.invoke(this, request);
                } catch (Exception e) {
                    return internalServerError().build();
                }
            }
        }
        return notFound().build();
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        Handler handlerAnnotation = this.getClass().getAnnotation(Handler.class);
        if (handlerAnnotation != null) {
            String path = handlerAnnotation.value();
            if (path.isEmpty() || !request.getPath().equals(path)) {
                return false;
            }
        }
        for (Method method : this.getClass().getDeclaredMethods()) {
            HttpMethod httpMethodAnnotation = method.getAnnotation(HttpMethod.class);
            if (httpMethodAnnotation != null && httpMethodAnnotation.value().equalsIgnoreCase(request.getMethod())) {
                return true;
            }
        }
        return false;
    }
}
