package server.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionAcceptor {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionAcceptor.class);
    private final ServerSocket serverSocket;

    public ConnectionAcceptor(Port port) throws IOException {
        this.serverSocket = new ServerSocket(port.getValue());
    }

    public Socket accept() throws IOException {
        return serverSocket.accept();
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
