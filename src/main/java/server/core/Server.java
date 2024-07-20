package server.core;

import server.handler.RequestHandler;
import server.handler.StaticFileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private final Port port;
    private final ThreadPool threadPool;
    private final ServerStatus status;
    private final ConnectionAcceptor connectionAcceptor;
    private final RequestDispatcher requestDispatcher;

    public Server(int port, int threadPoolSize, String basePackage) throws IOException {
        this.port = new Port(port);
        this.threadPool = new ThreadPool(threadPoolSize);
        this.status = new ServerStatus();
        this.connectionAcceptor = new ConnectionAcceptor(this.port);
        this.requestDispatcher = new RequestDispatcher(scanHandlers(basePackage), new StaticFileHandler());
    }

    private List<RequestHandler> scanHandlers(String basePackage) {
        return new HandlerScanner(basePackage).scanForHandlers();
    }

    public void start() {
        logger.debug("Listening for connection on port {} ....", port.getValue());
        while (status.isRunning()) {
            processNextConnection();
        }
        shutdown();
    }

    private void processNextConnection() {
        try {
            Socket clientSocket = connectionAcceptor.accept();
            threadPool.execute(new HttpConnectionProcessor(clientSocket, requestDispatcher));
        } catch (IOException e) {
            handleAcceptError(e);
        }
    }

    private void handleAcceptError(IOException e) {
        if (status.isRunning()) {
            logger.error("Error accepting client connection", e);
        }
    }

    public void stop() {
        status.stop();
        connectionAcceptor.close();
    }

    private void shutdown() {
        threadPool.shutdown();
    }
}
