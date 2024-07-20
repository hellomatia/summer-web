package server.core;

import java.io.*;
import java.net.Socket;

public class ClientConnection implements AutoCloseable {
    private static final int BUFFER_SIZE = 8192;
    private final Socket socket;
    private final BufferedInputStream inputStream;
    private final BufferedOutputStream outputStream;

    public ClientConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE);
        this.outputStream = new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE);
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
        socket.close();
    }
}
