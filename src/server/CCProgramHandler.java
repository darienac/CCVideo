package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

public class CCProgramHandler implements HttpHandler {
    private static final String FILE_PATH = "web/ccvideo.lua";

    private final byte[] content;

    public CCProgramHandler() {
        try {
            content = Files.readAllBytes(Path.of(FILE_PATH));
        } catch (IOException ex) {
            throw new RuntimeException("Unable to find file at " + FILE_PATH);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String out = new String(content);
        // TODO: require user to provide their public ip address
        out = out.replace("{HOSTNAME}", "73.253.126.143");
        out = out.replace("{PORT}", String.valueOf(exchange.getLocalAddress().getPort()));
        out = out.replace("{PACKET_SIZE_MAX}", String.valueOf(VideoChunkHandler.PACKET_SIZE_MAX));

        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        OutputStream respBody = exchange.getResponseBody();
        respBody.write(out.getBytes());
        respBody.close();
    }
}
