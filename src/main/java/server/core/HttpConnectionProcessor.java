package server.core;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.http.parser.Http11Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

class HttpConnectionProcessor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HttpConnectionProcessor.class);
    private final RequestDispatcher requestDispatcher;
    private Socket clientSocket;

    public HttpConnectionProcessor(Socket socket, RequestDispatcher requestDispatcher) {
        this.clientSocket = socket;
        this.requestDispatcher = requestDispatcher;
    }

    @Override
    public void run() {
        if (clientSocket == null) {
            logger.error("Client socket is not set");
            return;
        }

        try (ClientConnection connection = new ClientConnection(clientSocket)) {
            processConnection(connection);
        } catch (Exception e) {
            logger.error("Error processing client connection", e);
        }
    }

    private void processConnection(ClientConnection connection) throws IOException {
        logger.debug("Processing client connection");
        HttpRequest request = parseRequest(connection);
        HttpResponse response = handleRequest(request);
        sendResponse(connection, response);
    }

    private HttpRequest parseRequest(ClientConnection connection) throws IOException {
        HttpRequest request = Http11Parser.parse(connection.getInputStream());
        logger.debug("Received request: path => {}, method => {}", request.getPath(), request.getMethod());
        return request;
    }

    private HttpResponse handleRequest(HttpRequest request) {
        return requestDispatcher.handleRequest(request);
    }

    private void sendResponse(ClientConnection connection, HttpResponse response) throws IOException {
        OutputStream out = connection.getOutputStream();
        out.write(response.getBytes());
        out.flush();
    }
}
