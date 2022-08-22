package server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.FileHandler;

public class Server {
    private static final int MAX_WAITING_CONNECTIONS = 12;

    private HttpServer server;
    private byte[] videoOut;

    public Server(byte[] videoOut) {
        this.videoOut = videoOut;
    }

    public void run(int portNumber) throws IOException {
        server = HttpServer.create(new InetSocketAddress(portNumber), MAX_WAITING_CONNECTIONS);
        server.setExecutor(null);

        server.createContext("/ccvideo.lua", new CCProgramHandler());
        server.createContext("/videoSize", new StringHandler(Integer.toString(videoOut.length)));
        server.createContext("/videoChunk/", new VideoChunkHandler(videoOut));

        server.start();
    }
}
