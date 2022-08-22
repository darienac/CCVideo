package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;

public class VideoChunkHandler implements HttpHandler {
    public static final int PACKET_SIZE_MAX = 32768;

    private final byte[] video;

    public VideoChunkHandler(byte[] video) {
        this.video = video;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int chunkId = Integer.parseInt(exchange.getRequestURI().toString().substring("/videoChunk/".length()));
        byte[] out = Arrays.copyOfRange(video, PACKET_SIZE_MAX * chunkId, Math.min(PACKET_SIZE_MAX * (chunkId + 1), video.length));

        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        OutputStream respBody = exchange.getResponseBody();
        respBody.write(out);
        respBody.close();
    }
}
